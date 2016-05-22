package behaviours;
import agents.CarAgent;
import agents.SegmentAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * This behaviour listens to the cars that want to register or deregister
 * from this segment.
 *
 */
public class CarControlBehaviour extends Behaviour {

	private static final long serialVersionUID = -2533061568306629976L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtCarControl = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("carToSegment"));

	private SegmentAgent agent;

	//Constructor
	public CarControlBehaviour(SegmentAgent agent) {

		this.agent = agent;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mtCarControl);

		if (msg != null) { //There is an agent

			//It can either be a registration or a deregistration
			if (msg.getContent().equals("register")) { //Register car

				try {

					this.agent.addCar((CarAgent) msg.getContentObject());

				} catch (UnreadableException e) {

					System.out.println("Error getting the car object.");
					e.printStackTrace();
				}

			} else if (msg.getContent().equals("deregister")) {

				try {

					this.agent.removeCar((CarAgent) msg.getContentObject());

				} catch (UnreadableException e) {

					System.out.println("Error getting the car object.");
					e.printStackTrace();
				}

			} else { //Error

				//TODO: Throw an exception
				System.out.println("Error in the communication with the segment agent " + this.agent.getSegment().getId());
			}
		}
		
		//TODO: Do real stuff
		// -Change segment color according to capacity
		// -Update car's Dijkstra
		if (this.agent.getCars().size() > 0){

			System.out.println("I am segment " + this.agent.getSegment().getId() + " and there are " + this.agent.getCars().size() + " cars on me! :D");
		}

	}

	@Override
	public boolean done() {

		return false;
	}
}
