package behaviours;

import javax.swing.SwingUtilities;

import agents.InterfaceAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is started by the InterfaceAgent and
 * will update the car position given the new coordinates.
 *
 */
public class MoveCarBehaviour extends Behaviour {

	private static final long serialVersionUID = 1L;
	private String id;
	private boolean done = false;
	private InterfaceAgent agent;
	
	public MoveCarBehaviour(InterfaceAgent a, String id) {
		
		this.id = id;
		this.agent = a;
	}

	@Override
	public void action() {
		
		//Template to listen for new movements
		MessageTemplate mt = MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
				MessageTemplate.MatchConversationId(id));
		
		ACLMessage msg = myAgent.receive(mt);
		
		if (msg != null) {
			
			//Get the data
			String cont = msg.getContent();
			
			final double x = Double.parseDouble(
					cont.substring(cont.indexOf("x=")+2, cont.indexOf("y=")));
			final double y = Double.parseDouble(
					cont.substring(cont.indexOf("y=")+2));
			
			//Update the position in the canvas
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					agent.getMap().moveCar(id, x, y);
					
				}
			});
		}
	}

	@Override
	public boolean done() {
		return done? true: false;
	}
}
