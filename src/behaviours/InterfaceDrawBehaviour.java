package behaviours;

import java.util.HashMap;

import javax.swing.SwingUtilities;

import agents.InterfaceAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import view.CanvasWorld.PanelRadar.Mobile;

public class InterfaceDrawBehaviour extends Behaviour {

	private static final long serialVersionUID = 5169881140236331658L;

	private InterfaceAgent agent;

	//Template to listen for drawing instructions
	private MessageTemplate mt = MessageTemplate.or(MessageTemplate.or(MessageTemplate.and(
			MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
			MessageTemplate.MatchOntology("drawOntology")),
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
					MessageTemplate.MatchOntology("deleteOntology"))),
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
					MessageTemplate.MatchOntology("updateTimeOntology")));

	public InterfaceDrawBehaviour(InterfaceAgent agent) {

		this.agent = agent;
	}

	@Override
	public void action() {

		//Receive the drawing instructions
		ACLMessage msg = myAgent.receive(mt);

		if (msg != null) {

			if (msg.getOntology().equals("drawOntology")) {

				//Update the position in the canvas
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						String parts[] = msg.getContent().split("#");
						HashMap<String, Mobile> cars = agent.getMap().getCars();

						for (int i=1; i < parts.length; i+=4) {

							Mobile m = cars.get(parts[i]);

							if (m != null) {
								
								m.setX(Float.parseFloat(parts[i+1]));
								m.setY(Float.parseFloat(parts[i+2]));
								m.setSpecialColor(Boolean.getBoolean(parts[i+3]));
							}
						}

						agent.getMap().setCars(cars);
					}
				});
			} else if (msg.getOntology().equals("deleteOntology")) {

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						agent.getMap().deleteCar(msg.getContent());	
					}
				});
				
			} else if (msg.getOntology().equals("updateTimeOntology")) {

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						agent.getMap().setTime(msg.getContent());	
					}
				});
			}

		} else block();
	}

	@Override
	public boolean done() {

		return false;
	}
}
