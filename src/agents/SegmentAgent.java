package agents;

import java.util.HashMap;

import behaviours.SegmentListenBehaviour;
import behaviours.SegmentSendToDrawBehaviour;
import environment.Segment;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This agent will keep track of the cars that are inside an intersection
 * and will update the data accordingly.
 *
 */
public class SegmentAgent extends Agent {

	private static final long serialVersionUID = 5681975046764849101L;
	
	//The segment this agent belongs to
	private Segment segment;
	
	//The cars that are currently on this segment
	private HashMap<String, CarData> cars;
	
	protected void setup() {
		
		//Get the segment from parameter
		this.segment = (Segment) this.getArguments()[0];
		this.segment.setSegmentAgent(this);
		
		this.cars = new HashMap<String, CarData>();
		
		//Register the service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		
		ServiceDescription sd = new ServiceDescription();
		sd.setType("segment");
		
		sd.setName(this.getSegment().getId());

		dfd.addServices(sd);
		
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}
		
		//We add the logic to the segment		
		addBehaviour(new SegmentListenBehaviour(this));
		addBehaviour(new SegmentSendToDrawBehaviour(this));		
	}

	//Add a car to this segment
	public void addCar(String id, float x, float y, int maxSpeed, int currSpeed) {
		
		this.cars.put(id, new CarData(id, x, y, maxSpeed, currSpeed));
	}
	
	//Remove a car from this segment
	public void removeCar(String id) {
		
		this.cars.remove(id);
	}
	
	//Check if the car is contained
	public boolean containsCar(String id) {
		
		return this.cars.containsKey(id);
	}

	//Updates the information of a car
	public void updateCar(String id, float x, float y, int maxSpeed, int currSpeed) {
		
		CarData aux = cars.get(id);
		aux.setX(x);
		aux.setY(y);
		aux.setMaxSpeed(maxSpeed);
		aux.setCurrentSpeed(currSpeed);
	}
	
	//Creates the string that will be sent to the InterfaceAgent
	public String getDrawingInformation() {
		
		StringBuilder ret = new StringBuilder();
		
		ret.append(cars.size() + "#");
		
		for(CarData car: cars.values()) {
			
			ret.append(car.getId() + "#" + Float.toString(car.getX()) + "#" + Float.toString(car.getY()) + "#");
		}
		
		return ret.toString();
	}
	
	//Size of the cars
	public int carsSize() {
		
		return this.getCars().size();
	}
		
	//Getters and setters
	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public HashMap<String, CarData> getCars() {
		return cars;
	}
	
	@SuppressWarnings("unused")
	private class CarData {

		private String id;
		private float x, y;
		private int maxSpeed, currentSpeed;
		
		public CarData(String id, float x, float y, int maxSpeed, int currentSpeed) {
			super();
			this.id = id;
			this.x = x;
			this.y = y;
			this.maxSpeed = maxSpeed;
			this.currentSpeed = currentSpeed;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
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

		public int getMaxSpeed() {
			return maxSpeed;
		}

		public void setMaxSpeed(int maxSpeed) {
			this.maxSpeed = maxSpeed;
		}

		public int getCurrentSpeed() {
			return currentSpeed;
		}

		public void setCurrentSpeed(int currentSpeed) {
			this.currentSpeed = currentSpeed;
		}
	}
}
