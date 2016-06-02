package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import searchAlgorithms.Algorithm;
import searchAlgorithms.AlgorithmFactory;
import searchAlgorithms.Method;

import java.util.UUID;

import behaviours.CarBehaviour;
import environment.Map;
import environment.Path;
import environment.Segment;

/**
 * This code represents a mobile car, it will have an origin an a destination
 * and will get there using either the shortest or fastest paths.
 *
 */
public class CarAgent extends Agent {

	private static final long serialVersionUID = 1L;

	public static final int MAXWORLDX = 800;
	public static final int MAXWORLDY = 695;

	private double x, y;
	private int direction;
	private int curentSpeed,maxSpeed;
	private String id; 
	private DFAgentDescription interfaceAgent;
	private Map map;
	private Path path;
	private Segment previousSegment;

	protected void setup() {

		//Get the map from an argument
		this.map = (Map) this.getArguments()[0];

		//It starts in a random intersection, and ends in a random intersection
		String initialIntersection = this.map.getRandomIntersection();
		String finalIntersection = this.map.getRandomIntersection();

		//Origin an destination must be different
		while(initialIntersection.equals(finalIntersection)){
			finalIntersection = this.map.getRandomIntersection();
		}
		
		//Get the method we want
		AlgorithmFactory factory = new AlgorithmFactory();
		Algorithm alg = factory.getAlgorithm(Method.SHORTEST);
		
		//Get the desired Path from the origin to the destination
		this.path = alg.getPath(map, initialIntersection, finalIntersection);

		//Starting point
		setX(map.getIntersectionByID(initialIntersection).getX());
		setY(map.getIntersectionByID(initialIntersection).getY());

		this.maxSpeed = 120;
		this.curentSpeed = 120;

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
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(interfaceAgent.getName());
		msg.setContent("x="+this.x+"y="+this.y+"id="+this.id);
		msg.setConversationId("Car");
		send(msg);	

		//Runs the agent
		addBehaviour(new CarBehaviour(this, 50));	
	}

	//Setters and getters
	public int getDirection() {
		return direction;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
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

	public Path getPath() {
		return path;
	}

	public String getId() {
		return id;
	}

	public Segment getPreviousSegment() {
		return previousSegment;
	}

	public void setPreviousSegment(Segment previousSegment) {
		this.previousSegment = previousSegment;
	}
}
