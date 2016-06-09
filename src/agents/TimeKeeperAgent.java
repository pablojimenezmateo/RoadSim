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
	private long tickLength;
	private List<AID> agents = new ArrayList<AID>();

	protected void setup() {

		//Get the ticklength
		this.tickLength = (long) this.getArguments()[0];

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
							if (!getAgents().contains(aid)) {
								getAgents().add(aid);
							}
						} else {
							// Deregistration
							getAgents().remove(aid);
						}
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );

		//Subscribe in the DF to keep the cars list up to date
		sd = new ServiceDescription();
		sd.setType("segment");

		dfTemplate = new DFAgentDescription();
		dfTemplate.addServices(sd);

		sc = new SearchConstraints();
		sc.setMaxResults(new Long(-1));

		subscribe = DFService.createSubscriptionMessage(this, getDefaultDF(), dfTemplate, sc);

		/**
		 * This behaviour will keep an eye on the SegmentAgents registered in the system
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
							if (!getAgents().contains(aid)) {
								getAgents().add(aid);
							}
						} else {
							// Deregistration
							getAgents().remove(aid);
						}
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
		} );

		//Add the EventManager to the subscribed agents
		//Find the interface agent
		DFAgentDescription dfd = new DFAgentDescription();
		sd = new ServiceDescription();
		sd.setType("EventManagerAgent");
		dfd.addServices(sd);

		DFAgentDescription[] result = null;

		try {
			result = DFService.searchUntilFound(
					this, getDefaultDF(), dfd, null, 10000);
		} catch (FIPAException e) { e.printStackTrace(); }

		this.agents.add(result[0].getName());

		addBehaviour(new TickerBehaviour(this, tickLength) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onTick() {

				List<AID> agents = ((TimeKeeperAgent)myAgent).getAgents();

				//Send a tick to all the agents
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

				for (AID aid: agents) {

					msg.addReceiver(aid);
				}

				msg.setConversationId("tick"); 
				myAgent.send(msg);
			}
		});
	}



	public List<AID> getAgents() {
		return agents;
	}
}