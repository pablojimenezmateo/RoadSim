package agents;

import java.util.HashSet;
import java.util.Set;

import behaviours.CarControlBehaviour;
import environment.Segment;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

/**
 * This agent will keep track of the cars that are inside an intersection.
 *
 */
public class SegmentAgent extends Agent {

	private static final long serialVersionUID = 5681975046764849101L;
	
	//The segment this agent belongs to
	private Segment segment;
	
	//The cars that are currently on this segment
	private Set<CarAgent> cars;
	
	protected void setup() {
		
		//TODO: Triple check
		//Get the segment from parameter
		this.segment = (Segment) this.getArguments()[0];
		this.segment.setSegmentAgent(this);
		
		this.cars = new HashSet<CarAgent>();
		
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
		
		CarControlBehaviour b = new CarControlBehaviour(this);
		
		addBehaviour(b);		
	}

	//Adds a car to this segment
	public void addCar(CarAgent car) {
		
		this.cars.add(car);
	}
	
	public void removeCar(CarAgent car) {
		
		this.cars.remove(car);
	}
	
	//Getters and setters
	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public Set<CarAgent> getCars() {
		return cars;
	}
}
