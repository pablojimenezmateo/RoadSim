package agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This agent is in charge of sending a Tick message to the rest of the
 * agents.
 * 
 * EvenManagerAgent, CarAgents and SegmentAgents need a tick to start
 * its behaviour, otherwise they won't do anything.
 *
 */
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
		sd.setType("timeKeeperAgent");
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
		sd.setType("interfaceAgent");
		dfd.addServices(sd);

		DFAgentDescription[] result = null;

		try {
			result = DFService.searchUntilFound(
					this, getDefaultDF(), dfd, null, 5000);
		} catch (FIPAException e) { e.printStackTrace(); }

		this.interfaceAgent = result[0];

		//Get the ticklength
		this.tickLength = (long) this.getArguments()[0];

		//A reference to myself
		TimeKeeperAgent timeKeeperAgent = this;

		//Because the segments and the manager don't change,
		//I'll do the search just once
		DFAgentDescription[] segmentsaux = null;
		DFAgentDescription[] manageraux = null;
		
		dfd = new DFAgentDescription();
		sd  = new ServiceDescription();
		sd.setType("segmentAgent");
		dfd.addServices(sd);

		try {
			segmentsaux = DFService.searchUntilFound(
					timeKeeperAgent, getDefaultDF(), dfd, null, 5000);
		} catch (FIPAException e) { e.printStackTrace(); }

		dfd = new DFAgentDescription();
		sd  = new ServiceDescription();
		sd.setType("eventManagerAgent");
		dfd.addServices(sd);

		try {
			manageraux = DFService.searchUntilFound(
					timeKeeperAgent, getDefaultDF(), dfd, null, 5000);
		} catch (FIPAException e) { e.printStackTrace(); }
		
		//We need this auxiliary variables so that the variables are readable
		//from the runnable
		DFAgentDescription[] segments = segmentsaux;
		DFAgentDescription[] manager = manageraux;
		
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

				//Search for cars that are currently in the DF
				DFAgentDescription[] cars = null;

				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd  = new ServiceDescription();
				sd.setType("carAgent");
				dfd.addServices(sd);

				try {
					cars = DFService.search(
							timeKeeperAgent, getDefaultDF(), dfd, null);
				} catch (FIPAException e) { e.printStackTrace(); }

				//Send a tick to all the agents
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

				if (cars != null){
					
					numberOfCars = 0;
					
					for (DFAgentDescription dfAgentDescription : cars) {
						
						msg.addReceiver(dfAgentDescription.getName());
						numberOfCars++;
					}
				}

				for (DFAgentDescription dfAgentDescription : segments) {
					msg.addReceiver(dfAgentDescription.getName());
				}

				msg.addReceiver(manager[0].getName());
				
				//It can happen that the car has already finished its execution
				//before the message arrives, so we ignore the failure
				msg.addUserDefinedParameter(ACLMessage.IGNORE_FAILURE, "true");
				msg.setOntology("tickOntology"); 
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
					MessageTemplate.MatchOntology("changeTickLengthOntology"));

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

	//Setters and getters
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