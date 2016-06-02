package agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;

public class TimeKeeperAgent extends Agent {

	private static final long serialVersionUID = 4546329963020795810L;
	private static final long tickLength = 100;


	private List<AID> carAgents = new ArrayList<AID>();

	protected void setup() {

		//Subscribe in the DF to keep the cars list up to date
		ServiceDescription sd = new ServiceDescription();
		sd.setType("CarAgent");
		
		DFAgentDescription dfTemplate = new DFAgentDescription();
		dfTemplate.addServices(sd);
		
		SearchConstraints sc = new SearchConstraints();
		sc.setMaxResults(new Long(-1));
		
		ACLMessage subscribe = DFService.createSubscriptionMessage(this, getDefaultDF(), dfTemplate, sc);

		/**
		 * This behaviour will keep an eye on the CarAgents registered in the system
		 */
		addBehaviour(new SubscriptionInitiator(this, subscribe) {

			private static final long serialVersionUID = 9142354628682006078L;

			protected void handleInform(ACLMessage inform) {
				try {
					DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
					for (int i = 0; i < dfds.length; ++i) {
						AID aid = dfds[i].getName();
						if (dfds[i].getAllServices().hasNext()) {
							// Registration/Modification
							if (!getCarAgents().contains(aid)) {
								getCarAgents().add(aid);
								System.out.println("Car Agent "+aid.getLocalName()+" added to the list of searcher agents");
							}
						} else {
							// Deregistration
							getCarAgents().remove(aid);
							System.out.println("Car Agent "+aid.getLocalName()+" removed from the list of searcher agents");
						}
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );
		
		addBehaviour(new TickerBehaviour(this, tickLength) {
			
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onTick() {
				
				List<AID> carAgents = ((TimeKeeperAgent)myAgent).getCarAgents();
				
				//Send a tick to all the cars
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				
				for (AID aid: carAgents) {
					
					msg.addReceiver(aid);
				}
				
				msg.setConversationId("tick"); 
				myAgent.send(msg);
			}
		});
	}

	public List<AID> getCarAgents() {
		return carAgents;
	}

	public void setCarAgents(List<AID> carAgents) {
		this.carAgents = carAgents;
	}
}
