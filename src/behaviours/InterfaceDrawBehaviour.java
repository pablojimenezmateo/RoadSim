package behaviours;

import javax.swing.SwingUtilities;

import agents.InterfaceAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InterfaceDrawBehaviour extends Behaviour {

	private static final long serialVersionUID = 5169881140236331658L;

	private InterfaceAgent agent;

	//Template to listen for drawing instructions
	private MessageTemplate mt = MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
			MessageTemplate.MatchOntology("drawOntology"));


	public InterfaceDrawBehaviour(InterfaceAgent agent) {

		this.agent = agent;
	}

	@Override
	public void action() {

		//Block until tick is received
		ACLMessage msg = myAgent.receive(mt);
		
		if (msg != null) {

			//Update the position in the canvas
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					String parts[] = msg.getContent().split("#");

					for (int i=1; i < parts.length; i+=3) {

						agent.getMap().moveCar(parts[i], Float.parseFloat(parts[i+1]), Float.parseFloat(parts[i+2]));
					}

				}
			});
		} else block();
	}

	@Override
	public boolean done() {

		return false;
	}
}
