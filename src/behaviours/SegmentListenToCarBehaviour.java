package behaviours;

import agents.SegmentAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the SegmentAgent and listens to the cars 
 * that want to register or deregister from this segment.
 *
 * The same message works for registering or deregistering, if a car with
 * an ID that is not in this segment sends me a message, I register it, if
 * the ID is already in the segment, I deregister it.
 *
 */
public class SegmentListenToCarBehaviour extends Behaviour {

	private static final long serialVersionUID = -2533061568306629976L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtCarControl = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("carToSegment"));

	private SegmentAgent agent;

	//Constructor
	public SegmentListenToCarBehaviour(SegmentAgent agent) {

		this.agent = agent;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mtCarControl);

		if (msg != null) { //There is an agent
			
			String car = msg.getContent();
			String parts[] = car.split("#");
			
			//Register
			if (msg.getConversationId().equals("register")) { 
				
				this.agent.addCar(parts[0], Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
				
				System.out.println("I'm the car " + parts[0] + " and I'm registering in " + this.agent.getLocalName());
				
			} else if (msg.getConversationId().equals("deregister")) { //Deregister
				
				this.agent.removeCar(parts[0]);
				
				System.out.println("I'm the car " + parts[0] + " and I'm deregistering from " + this.agent.getLocalName());
				
			} else if (msg.getConversationId().equals("update")) { //Deregister
				
				this.agent.updateCar(parts[0], Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
			}
		} else block();
		
		//TODO: Do real stuff
		// -Change segment color according to capacity
		// -Update car's Dijkstra
		if (this.agent.getCars().size() > 0){

			//System.out.println("I am segment " + this.agent.getSegment().getId() + " and there are " + this.agent.getCars().size() + " cars on me! :D");
		}
	}

	@Override
	public boolean done() {

		return false;
	}
}
