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
 * and will get there using either the shortest, fastest or smartest path.
 *
 */
public class CarAgent extends Agent {

	private static final long serialVersionUID = 1L;

	public static final int MAXWORLDX = 800;
	public static final int MAXWORLDY = 695;

	private float x, y;
	private int direction;
	private int curentSpeed,maxSpeed;
	private String id; 
	private DFAgentDescription interfaceAgent;
	private Map map;
	private Path path;
	private Segment previousSegment;
	private String initialIntersection, finalIntersection;
	private boolean specialColor = false;
	private boolean smart = false;
	private Algorithm alg;
	private int algorithmType;

	protected void setup() {

		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("carAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}

		//Get the map from an argument
		this.map = (Map) this.getArguments()[0];
		
		//Get the starting and final points of my trip
		this.initialIntersection = (String) this.getArguments()[1];
		this.finalIntersection = (String) this.getArguments()[2];
		
		//Get the speeds
		this.maxSpeed = (int) this.getArguments()[3];
		this.curentSpeed = 0; //(int) this.getArguments()[4];

		//Get the method we want
		AlgorithmFactory factory = new AlgorithmFactory();
		this.alg = null;
		
		String routeType = (String) this.getArguments()[4];
		
		if (routeType.equals("fastest")) {
			
			this.algorithmType = Method.FASTEST.value;
			this.alg = factory.getAlgorithm(Method.FASTEST);
			
		} else if (routeType.equals("shortest")) {
			 
			this.algorithmType = Method.SHORTEST.value;
			this.alg = factory.getAlgorithm(Method.SHORTEST);
			
		} else if (routeType.equals("smartest")) {
			
			this.algorithmType = Method.SMARTEST.value;
			this.alg = factory.getAlgorithm(Method.SMARTEST);
			this.smart = true;
		}
		
		//Get the desired Path from the origin to the destination
		this.path = alg.getPath(this.map, getInitialIntersection(), getFinalIntersection(), this.maxSpeed);
		
		//Starting point
		setX(map.getIntersectionByID(getInitialIntersection()).getX());
		setY(map.getIntersectionByID(getInitialIntersection()).getY());

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

		//An unique identifier for the car
		this.id = UUID.randomUUID().toString();

		//We notify the interface about the new car
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(interfaceAgent.getName());
		msg.setContent("x="+this.x+"y="+this.y+"id="+this.id+"algorithmType="+this.algorithmType);
		msg.setOntology("newCarOntology");
		send(msg);
		
		//Runs the agent
		addBehaviour(new CarBehaviour(this, 50));	
	}
	
	/**
	 * Recalculate the route, this will be called from the behaviour if we are smart.
	 * 
	 * @param origin ID of the intersection where the car is
	 */
	public void recalculate(String origin) {
		
		this.path = this.alg.getPath(this.map, origin, getFinalIntersection(), this.maxSpeed);
	}

	//Setters and getters
	public int getDirection() {
		return direction;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
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

	public String getInitialIntersection() {
		return initialIntersection;
	}

	public String getFinalIntersection() {
		return finalIntersection;
	}

	public boolean getSpecialColor() {
		return specialColor;
	}

	public void setSpecialColor(boolean specialColor) {
		this.specialColor = specialColor;
	}
	
	public boolean isSmart() {
		
		return this.smart;
	}

	public int getAlgorithmType() {
		return algorithmType;
	}
}
