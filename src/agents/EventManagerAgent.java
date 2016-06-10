package agents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
	
	private Set<String> aux;
	
	private HashMap<Integer, List<String>> events;
	
	private jade.wrapper.AgentContainer carContainer, segmentContainer;
	
	private Map map;

	protected void setup() {
		
		this.events = new HashMap<Integer, List<String>>();
		this.aux = new HashSet<String>();
		
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
		//Start at 8:00
		this.timeElapsed = 8*3600;
		
		//New cars
		String testEvent1 = "newCar#09:00#I-AP7-01#I-AP7-04#120#120";
		String testEvent2 = "newCar#09:30#I-CV10-05#I-CS22-03#120#120";
		
		//Segments
		String testEvent3 = "segment#10:00#S-AP7-03#90";
		String testEvent4 = "segment#10:00#S-AP7-04#70";
		String testEvent5 = "segment#10:15#S-AP7-01#20";
		
		//Add them to the set
		aux.add(testEvent1);
		aux.add(testEvent2);
		aux.add(testEvent3);
		aux.add(testEvent4);
		aux.add(testEvent5);
		
		//Translate from hours to ticks
		for (String event : aux) {
			
			String time = event.split("#")[1];
			int hours = Integer.parseInt(time.split(":")[0]);
			int minutes = Integer.parseInt(time.split(":")[1]);
			
			int tick = 3600 * hours + 60 * minutes;
			
			//Add it to the event queue
			if (this.getEvents().containsKey(tick)) {
				
				this.getEvents().get(tick).add(event);
			} else {
			
				this.getEvents().put(tick, new LinkedList<String>());
				this.getEvents().get(tick).add(event);
			}
		}
		
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
