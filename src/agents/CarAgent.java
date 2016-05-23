package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import behaviours.CarBehaviour;
import environment.Intersection;
import environment.Map;
import environment.Step;

/**
 * This code represents a mobile car.
 *
 */
public class CarAgent extends Agent {

	private static final long serialVersionUID = 1L;

	public static final int MAXWORLDX = 800;
	public static final int MAXWORLDY = 695;

	private int x, y;
	private int direction;
	private int curentSpeed,maxSpeed;
	private String id; 
	private DFAgentDescription interfaceAgent;
	private Random rnd = new Random();
	private Map map;
	private List<Step> graphicalPath;
	private List<String> path;

	protected void setup() {

		//Get the map from an argument
		this.map = (Map) this.getArguments()[0];

		//It starts in a random intersection, and ends in a random intersection
		String initialIntersection = this.map.getRandomIntersection();//this.map.getIntersectionByID("I-N340-01").getId();//this.map.getRandomIntersection();
		String finalIntersection = this.map.getRandomIntersection();//this.map.getIntersectionByID("I-CS22-01").getId();//this.map.getRandomIntersection();

		//Origin an destination must be different
		while(initialIntersection.equals(finalIntersection)){
			finalIntersection = this.map.getRandomIntersection();
		}

		//We get the shortest path from the origin to the destination
		HashMap<Intersection, Intersection> dijks = map.shortestPathsFrom(initialIntersection);
		this.graphicalPath = map.getGraphicalPath(dijks, map.getIntersectionByID(finalIntersection));

		//Debug
//		this.path = map.getPath(dijks, map.getIntersectionByID(finalIntersection));
//
//		System.out.println("I am " + this.getLocalName() + " and I am doing this trip:");
//
//		for(String inter: this.path){
//
//			System.out.println(inter + " (" + this.map.getIntersectionByID(inter).getX() + ", " + this.map.getIntersectionByID(inter).getY() + ")");
//		}

		//Starting point
		setX(map.getIntersectionByID(initialIntersection).getX());
		setY(map.getIntersectionByID(initialIntersection).getY());

		//Speeds, currently the currentSpeed belongs [1, 7]
		this.maxSpeed = 120;
		this.curentSpeed = rnd.nextInt((7 - 5) + 1) + 5;

		//Find the interface agent
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("interface");
		dfd.addServices(sd);

		DFAgentDescription[] result = null;

		try {
			result = DFService.searchUntilFound(
					this, getDefaultDF(), dfd, null, 5000);
		} catch (FIPAException e) { e.printStackTrace(); }

		this.interfaceAgent = result[0];

		//An unique identifier for the car
		this.id = UUID.randomUUID().toString();

		//We notify the interface about the new car
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM_REF);
		msg.addReceiver(interfaceAgent.getName());
		msg.setContent("x="+this.x+"y="+this.y+"id="+this.id);
		msg.setConversationId("Car");
		send(msg);	

		// Lanza BAgMovil despues de esperar 1000 mseg
		addBehaviour(new CarBehaviour(this, 1000));	
	}

	//Setters and getters
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getCurrentSpeed() {
		return curentSpeed;
	}

	public void setCurrentSpeed(int currentSpeed) {
		this.curentSpeed = currentSpeed;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public DFAgentDescription getInterfaceAgent() {
		return interfaceAgent;
	}

	public Map getMap() {
		return map;
	}

	public List<Step> getGraphicalPath() {
		return graphicalPath;
	}

	public String getId() {
		return id;
	}

	//	/*
	//	 * Este comportamiento gestiona los mensajes que le lleguen respecto a que
	//	 *   agente sensor (o el agente interfaz si ningun sensor es capaz) es con el
	//	 *   que tiene que comunicarse para enviarle sus posiciones.
	//	 */
	//	private class BRecibeNuevoAgenteSensor extends CyclicBehaviour {
	//
	//		private static final long serialVersionUID = 1L;
	//		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
	//		@Override
	//		public void action() {
	//			ACLMessage msg = receive(mt);
	//			if (msg != null) 
	//				sensorAgent = msg.getContent();			
	//			else block();
	//		}
	//	}
}
