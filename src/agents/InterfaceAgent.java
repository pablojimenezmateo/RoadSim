package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

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
	
	private DFAgentDescription timeKeeperAgent;

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

		//Find the TimeKeeperAgent agent
		dfd = new DFAgentDescription();
		sd = new ServiceDescription();
		sd.setType("TimeKeeperAgent");
		dfd.addServices(sd);

		DFAgentDescription[] result = null;

		try {
			result = DFService.searchUntilFound(
					this, getDefaultDF(), dfd, null, 5000);
		} catch (FIPAException e) { e.printStackTrace(); }

		this.timeKeeperAgent = result[0];

		//Check if I need to create the GUI, for testing purposes
		boolean drawGUI = (boolean) this.getArguments()[1];

		if (drawGUI) {

			//Get the map from an argument
			Map graphicalMap = (Map) this.getArguments()[0];
			
			InterfaceAgent me = this;

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					map = new CanvasWorld(me, MAXWORLDX, MAXWORLDY, graphicalMap);	
				}
			});

			//Launch the behaviour that will add cars
			addBehaviour(new InterfaceAddCarBehaviour(this));

			//This will listen for drawing instructions
			addBehaviour(new InterfaceDrawBehaviour(this));
		}
	}

	//Send a message to the TimeKeeperAgent to change its tickLength
	public void setTick(int newTick) {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("changeTickOntology");
		msg.addReceiver(this.timeKeeperAgent.getName());
		msg.setContent(Integer.toString(newTick));
		
		this.send(msg);
	}

	//Setters and getters
	public CanvasWorld getMap() {
		return map;
	}
}
