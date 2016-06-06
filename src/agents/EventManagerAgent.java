package agents;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import behaviours.EventManagerBehaviour;
import environment.Map;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This agent will be the one that launches cars at specific moments
 * of the day as well as change the segments density.
 *
 */
public class EventManagerAgent extends Agent {

	private static final long serialVersionUID = 8650883603283102448L;

	private int timeElapsed;
	
	private HashMap<Integer, List<String>> events;
	
	private jade.wrapper.AgentContainer carContainer, segmentContainer;
	
	private Map map;

	protected void setup() {
		
		this.events = new HashMap<Integer, List<String>>();
		
		//Get the map
		this.map = (Map) this.getArguments()[0];
		
		//Get the containers
		this.carContainer = (jade.wrapper.AgentContainer) this.getArguments()[1];
		this.segmentContainer = (jade.wrapper.AgentContainer) this.getArguments()[2];
		
		//Register
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("EventManagerAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}
		
		//TODO: Populate events by reading a file
		String testEvent1 = "newCar#I-AP7-01#I-AP7-04#120#120";
		String testEvent2 = "newCar#I-CV10-05#I-CS22-03#120#120";
		
		this.getEvents().put(50, new LinkedList<String>());
		this.getEvents().put(80, new LinkedList<String>());
		
		this.getEvents().get(50).add(testEvent1);
		this.getEvents().get(80).add(testEvent2);
		
		addBehaviour(new EventManagerBehaviour(this));
	}

	//Getters and setter
	public int getTimeElapsed() {
		return timeElapsed;
	}

	public void setTimeElapsed(int timeElapsed) {
		this.timeElapsed = timeElapsed;
	}
	
	public void incrementeTimeElapsed() {
		
		this.timeElapsed += 1;
	}

	public HashMap<Integer, List<String>> getEvents() {
		return events;
	}

	public jade.wrapper.AgentContainer getCarContainer() {
		return carContainer;
	}

	public jade.wrapper.AgentContainer getSegmentContainer() {
		return segmentContainer;
	}
	
	public Map getMap() {
		
		return this.map;
	}
}
