package agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionInitiator;

public class TimeKeeperAgent extends Agent {

	private static final long serialVersionUID = 4546329963020795810L;
	private long tickLength;
	private List<AID> agents = new ArrayList<AID>();
	private int numberOfCars = 0;
	private DFAgentDescription interfaceAgent;

	protected void setup() {

		//Register the service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("TimeKeeperAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}

		//Find the interface agent
		dfd = new DFAgentDescription();
		sd = new ServiceDescription();
		sd.setType("interface");
		dfd.addServices(sd);

		DFAgentDescription[] result = null;

		try {
			result = DFService.searchUntilFound(
					this, getDefaultDF(), dfd, null, 5000);
		} catch (FIPAException e) { e.printStackTrace(); }

		this.interfaceAgent = result[0];

		//Get the ticklength
		this.tickLength = (long) this.getArguments()[0];

		//Subscribe in the DF to keep the cars list up to date
		sd = new ServiceDescription();
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
								numberOfCars++;
							}
						} else {
							// Deregistration
							getAgents().remove(aid);
							numberOfCars--;
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
		dfd = new DFAgentDescription();
		sd = new ServiceDescription();
		sd.setType("EventManagerAgent");
		dfd.addServices(sd);

		result = null;

		try {
			result = DFService.searchUntilFound(
					this, getDefaultDF(), dfd, null, 10000);
		} catch (FIPAException e) { e.printStackTrace(); }

		this.agents.add(result[0].getName());

		//This is the behaviour that sends a tick message to all the agents
		addBehaviour(new Behaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {

				//I was using a TickerBehaviour, but you cannot change the tick length
				try {
					Thread.sleep(((TimeKeeperAgent) myAgent).getTickLength());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				List<AID> agents = ((TimeKeeperAgent)myAgent).getAgents();

				//Send a tick to all the agents
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

				for (AID aid: agents) {

					msg.addReceiver(aid);
				}

				msg.setConversationId("tick"); 
				myAgent.send(msg);

				//Send the number of cars to the interface agent
				msg = new ACLMessage(ACLMessage.INFORM);
				msg.addReceiver(interfaceAgent.getName());
				msg.setOntology("numberOfCarsOntology");
				msg.setContent(Integer.toString(numberOfCars));
				myAgent.send(msg);
			}

			@Override
			public boolean done() {
				return false;
			}
		});

		//Check for tickLeght changes
		addBehaviour(new Behaviour() {

			private static final long serialVersionUID = 8455875589611369392L;

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
					MessageTemplate.MatchOntology("changeTickOntology"));

			@Override
			public void action() {

				ACLMessage msg = myAgent.receive(mt);

				if (msg != null) {

					((TimeKeeperAgent)this.myAgent).setTickLength(Long.parseLong(msg.getContent()));
				} else block();
			}

			@Override
			public boolean done() {
				return false;
			}
		});
	}

	public List<AID> getAgents() {
		return agents;
	}

	public long getTickLength() {

		return this.tickLength;
	}

	public void setTickLength(long newTick) {

		this.tickLength = newTick;
	}
}