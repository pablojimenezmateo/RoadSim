package main;

import java.io.IOException;

import environment.Map;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {

	private static final int numberOfCars = 1000;

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
		try {
			AgentController agent = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);

			agent.start();

		} catch (StaleProxyException e1) {

			System.out.println("Error starting the rma agent");
			e1.printStackTrace();
		}
		
		//We will use a container only for the segments
		profile = new ProfileImpl(null, 1099, null);
		profile.setParameter(Profile.CONTAINER_NAME, "Segment container");

		//Container that will hold the agents
		jade.wrapper.AgentContainer segmentContainer = rt.createAgentContainer(profile);
		
		//Load the map
		try {

			map = new Map("map/base", segmentContainer);
		} catch (IOException e) {

			System.out.println("Error reading the maps file.");
			e.printStackTrace();
		}
		
		//Create the agents
		//Interface
		try {

			AgentController agent = mainContainer.createNewAgent("Interface", "agents.InterfaceAgent", new Object[]{map});

			agent.start();

		} catch (StaleProxyException e) {

			System.out.println("Error starting the interface");
			e.printStackTrace();
		}

		//Cars
		//Create a profile for the car container
		profile = new ProfileImpl(null, 1099, null);
		profile.setParameter(Profile.CONTAINER_NAME, "Car container");

		//Container that will hold the agents
		jade.wrapper.AgentContainer carContainer = rt.createAgentContainer(profile);
		
		for (int i=0; i<numberOfCars; i++){
			
			try {

				AgentController agent = carContainer.createNewAgent("car" + Integer.toString(i), "agents.CarAgent", new Object[]{map});

				agent.start();

			} catch (StaleProxyException e) {

				System.out.println("Error starting the interface");
				e.printStackTrace();
			}
		}
	}
}
