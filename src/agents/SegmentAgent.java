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
 * This agent will keep track of the cars that are inside an intersection
 * and will update the data accordingly.
 *
 */
public class SegmentAgent extends Agent {

	private static final long serialVersionUID = 5681975046764849101L;
	
	//The segment this agent belongs to
	private Segment segment;
	
	//The cars that are currently on this segment
	private Set<String> cars;
	
	protected void setup() {
		
		//Get the segment from parameter
		this.segment = (Segment) this.getArguments()[0];
		this.segment.setSegmentAgent(this);
		
		this.cars = new HashSet<String>();
		
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
		addBehaviour(new CarControlBehaviour(this));		
	}

	//Add a car to this segment
	public void addCar(String car) {
		
		this.cars.add(car);
	}
	
	//Remove a car from this segment
	public void removeCar(String car) {
		
		this.cars.remove(car);
	}
		
	//Getters and setters
	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public Set<String> getCars() {
		return cars;
	}
}
