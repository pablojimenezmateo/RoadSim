package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import javax.swing.SwingUtilities;

import behaviours.InterfaceAddCarBehaviour;
import behaviours.InterfaceDrawBehaviour;
import environment.Map;
import view.CanvasWorld;


/**
 * This agent receives all the messages related to the GUI.
 *
 */
public class InterfaceAgent extends Agent{

	private static final long serialVersionUID = 1L;

	public static final int MAXWORLDX = 800;
	public static final int MAXWORLDY = 720;

	private CanvasWorld map;

	protected void setup() {

		//Register the service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("interface");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}

		//Check if I need to create the GUI, for testing purposes
		boolean drawGUI = (boolean) this.getArguments()[1];

		if (drawGUI) {
			
			//Get the map from an argument
			Map graphicalMap = (Map) this.getArguments()[0];

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					map = new CanvasWorld(getLocalName(), MAXWORLDX, MAXWORLDY, graphicalMap);	
				}
			});

			//Launch the behaviour that will add cars
			addBehaviour(new InterfaceAddCarBehaviour(this));
			
			//This will listen for drawing instructions
			addBehaviour(new InterfaceDrawBehaviour(this));
		}
	}

	//Setters and getters
	public CanvasWorld getMap() {
		return map;
	}
}
