package behaviours;

import agents.SegmentAgent;
import environment.Segment;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the SegmentAgent and listens to messages
 * either by cars to register, deregister or update themselves from it
 * or from the EventManagerAgent to tell them updates about its status.
 *
 */
public class SegmentListenBehaviour extends Behaviour {

	private static final long serialVersionUID = -2533061568306629976L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtCarControl = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("carToSegmentOntology"));

	private MessageTemplate mtEventManagerControl = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("eventManagerToSegmentOntology"));

	private MessageTemplate mt = MessageTemplate.or(mtCarControl, mtEventManagerControl);

	private SegmentAgent agent;

	//Constructor
	public SegmentListenBehaviour(SegmentAgent agent) {

		this.agent = agent;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mt);

		if (msg != null) { //There is a message
			
			if (msg.getOntology().equals("carToSegmentOntology")) {

				String car = msg.getContent();
				String parts[] = car.split("#");

				//Register
				if (msg.getConversationId().equals("register")) { 

					this.agent.addCar(parts[0], Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Boolean.valueOf(parts[3]));

				} else if (msg.getConversationId().equals("deregister")) { //Deregister

					this.agent.removeCar(parts[0]);

				} else if (msg.getConversationId().equals("update")) { //Update position

					this.agent.updateCar(parts[0], Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Boolean.valueOf(parts[3]));
				}
				
			} else if (msg.getOntology().equals("eventManagerToSegmentOntology")) {
				
				Segment segment = this.agent.getSegment();
				
				char serviceLevel = msg.getContent().charAt(0);
				
				segment.setCurrentServiceLevel(serviceLevel);
			}
			
		} else block();
	}

	@Override
	public boolean done() {

		return false;
	}
}
