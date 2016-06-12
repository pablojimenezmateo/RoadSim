package agents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

	private int timeElapsed, previousMinute;

	private Set<String> aux;

	private HashMap<Integer, List<String>> events;

	private jade.wrapper.AgentContainer carContainer, segmentContainer;

	private Map map;

	private DFAgentDescription interfaceAgent;

	protected void setup() {

		this.events = new HashMap<Integer, List<String>>();
		this.aux = new HashSet<String>();

		//Get the map
		this.map = (Map) this.getArguments()[0];

		//Get the containers
		this.carContainer = (jade.wrapper.AgentContainer) this.getArguments()[1];
		this.segmentContainer = (jade.wrapper.AgentContainer) this.getArguments()[2];

		//Get the folder
		String folder = (String) this.getArguments()[3];

		//Previous minute will be used to know when to send a msg to the interface, when the minute changes
		this.previousMinute = 0;

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

		//TODO: Populate events by reading a file
		//Start at 8:00
		this.timeElapsed = 8*3600;

		//Read from file
		//Get all files from the given folder
		String url = Map.class.getClassLoader().getResource(folder).getPath();

		File[] files = new File(url).listFiles();

		//Check correct files
		BufferedReader eventsReader = null;

		for(int i=0; i<files.length; i++){

			if(files[i].getName().equals("events.csv")){

				try {
					eventsReader = new BufferedReader(new FileReader(files[i].getAbsolutePath()));
				} catch (FileNotFoundException e) {

					System.out.println("Error reading the events file.");
					e.printStackTrace();
				}
			}
		}

		//Add the events
		try {
			if (eventsReader != null) {

				String line = null;

				line = eventsReader.readLine();

				//Read  all the Intersections
				while(line != null){

					aux.add(line);
					line = eventsReader.readLine();
				}
			}

		} catch (IOException e) {

			System.out.println("Error reading the line from the events file.");
			e.printStackTrace();
		} finally {
			
			try {
				eventsReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		//Translate from hours to ticks
		for (String event : aux) {

			String time = event.split(",")[1];
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

	public int getPreviousMinute() {
		return previousMinute;
	}

	public void setPreviousMinute(int previousMinute) {
		this.previousMinute = previousMinute;
	}

	public DFAgentDescription getInterfaceAgent() {
		return interfaceAgent;
	}

	public void setInterfaceAgent(DFAgentDescription interfaceAgent) {
		this.interfaceAgent = interfaceAgent;
	}
}
