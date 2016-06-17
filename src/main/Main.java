package main;

import java.io.IOException;

import environment.Map;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Main program, it creates everything.
 *
 */
public class Main {

	//Initial tick length, this value is ignored if the GUI is drawn
	private static final long tickLength = 1L;
	
	//Start at specific tick
	private static final long startingTick = 8*3600; //Start at 8:00
	
	//Finish the simulation at specific tick
	private static final long finishingTick = 23*3600; //End at 23:00
	
	//Random smart cars from the beginning
	private static final int numberOfCars = 0;
	
	//Draw the GUI
	private static final boolean drawGUI = true;
	
	//Start the RMA
	private static final boolean startRMA = false;
	
	//Activate segment logging
	private static final boolean segmentLogging = false;
	
	//Logging directory for the segments
	private static final String loggingDirectory = "/home/gef/Documents/SimulationResults";

	public static void main(String[] args) {

		Map map = null;

		//Get a hold on JADE runtime
		jade.core.Runtime rt = jade.core.Runtime.instance();

		//Exit the JVM when there are no more containers around
		rt.setCloseVM(true);

		//Create a profile for the main container
		Profile profile = new ProfileImpl(null, 1099, null);
		profile.setParameter(Profile.CONTAINER_NAME, "Main container");

		//Container that will hold the agents
		jade.wrapper.AgentContainer mainContainer = rt.createMainContainer(profile);

		//Start RMA
		if (startRMA) {
			try {
				AgentController agent = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);

				agent.start();

			} catch (StaleProxyException e1) {

				System.out.println("Error starting the rma agent");
				e1.printStackTrace();
			}
		}

		//We will use a container only for the segments
		profile = new ProfileImpl(null, 1099, null);
		profile.setParameter(Profile.CONTAINER_NAME, "Segment container");

		//Container that will hold the agents
		jade.wrapper.AgentContainer segmentContainer = rt.createAgentContainer(profile);

		//Load the map
		try {

			map = new Map("staticFiles/map", segmentContainer, segmentLogging, loggingDirectory);
		} catch (IOException e) {

			System.out.println("Error reading the maps file.");
			e.printStackTrace();
		}

		//Create the agents
		//Interface
		try {

			AgentController agent = mainContainer.createNewAgent("interfaceAgent", "agents.InterfaceAgent", new Object[]{map, drawGUI});

			agent.start();

		} catch (StaleProxyException e) {

			System.out.println("Error starting the interface");
			e.printStackTrace();
		}

		//TimeKeeper
		try {
			AgentController agent = mainContainer.createNewAgent("timeKeeperAgent", "agents.TimeKeeperAgent", new Object[]{tickLength, startingTick, finishingTick});

			agent.start();

		} catch (StaleProxyException e1) {

			System.out.println("Error starting the TimeKeeper agent");
			e1.printStackTrace();
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}

		//Cars
		//Create a profile for the car container
		profile = new ProfileImpl(null, 1099, null);
		profile.setParameter(Profile.CONTAINER_NAME, "Car container");

		//Container that will hold the agents
		jade.wrapper.AgentContainer carContainer = rt.createAgentContainer(profile);

		for (int i=0; i<numberOfCars; i++){

			try {

				AgentController agent = carContainer.createNewAgent("car" + Integer.toString(i) + "Agent", "agents.CarAgent", new Object[]{map, map.getRandomIntersection(), map.getRandomIntersection(), 120, "fastest"});

				agent.start();

			} catch (StaleProxyException e) {

				System.out.println("Error starting a car agent");
				e.printStackTrace();
			}
		}

		//EventManager
		try {
			AgentController agent = mainContainer.createNewAgent("eventManagerAgent", "agents.EventManagerAgent", new Object[]{map, carContainer, segmentContainer, "staticFiles/events", startingTick});

			agent.start();

		} catch (StaleProxyException e1) {

			System.out.println("Error starting the EventManager agent");
			e1.printStackTrace();
		}
	}
}
