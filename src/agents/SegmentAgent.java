package agents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
 * This agent will keep track of the cars that are inside between two intersections
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
		sd.setType("segmentAgent");

		sd.setName(this.getSegment().getId());

		dfd.addServices(sd);

		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}

		//This behaviour will keep the cars updated	
		addBehaviour(new SegmentListenBehaviour(this));

		//This behaviour will send the data to the GUI
		addBehaviour(new SegmentSendToDrawBehaviour(this));		
	}

	/**
	 * Add a car to this segment
	 * 
	 * @param id ID of the car
	 * @param x X coordinate of the car
	 * @param y Y coordinate of the car
	 * @param specialColor If we have to paint it specially
	 */
	public void addCar(String id, float x, float y, boolean specialColor) {

		this.cars.put(id, new CarData(id, x, y, specialColor));
	}

	/**
	 * Remove a car from this segment
	 * 
	 * @param id ID of the car to remove
	 */
	public void removeCar(String id) {

		this.cars.remove(id);
	}

	/**
	 * Check if the car is contained in this segment
	 * 
	 * @param id ID of the car to check
	 * @return True if found, false otherwise
	 */
	public boolean containsCar(String id) {

		return this.cars.containsKey(id);
	}

	/**
	 * Updates the information of a car
	 * 
	 * @param id ID of the car to update
	 * @param x New x coordinate
	 * @param y New y coordinate
	 * @param specialColor New specialcolor
	 */
	public void updateCar(String id, float x, float y, boolean specialColor) {

		CarData aux = cars.get(id);
		aux.setX(x);
		aux.setY(y);
		aux.setSpecialColor(specialColor);
	}

	/**
	 * Creates the string with the information about this segment to
	 * notify the InterfaceAgent
	 * 
	 * @return String with the information of this segment
	 */
	public String getDrawingInformation() {

		//It is far more efficient to use this rather than a simple String
		StringBuilder ret = new StringBuilder();

		ret.append(cars.size() + "#");

		for(CarData car: cars.values()) {

			ret.append(car.getId() + "#" + Float.toString(car.getX()) + "#" + Float.toString(car.getY()) + "#" + Boolean.toString(car.getSpecialColor()) + "#");
		}

		return ret.toString();
	}

	/**
	 * This method logs the information of the segment.
	 * 
	 * @return
	 */
	public void doLog(long currentTick) {

		if (currentTick % 60 == 0) {

			int totalMinutes = (int)currentTick / 60;
			int hours = (int)(totalMinutes / 60);
			int minutes = (int)(totalMinutes % 60);

			//It is far more efficient to use this rather than a simple String
			StringBuilder ret = new StringBuilder();

			//The time properly formated
			String time = String.format("%02d", hours) + ":" + String.format("%02d", minutes);

			ret.append(time + "," + this.getSegment().getMaxSpeed() + "," + this.segment.getCurrentAllowedSpeed() + "," + this.segment.getCurrentServiceLevel() + "," + cars.size() + '\n');

			//Check if file exists
			File f = new File(Paths.get(this.segment.getLoggingDirectory() + "/" + this.getLocalName() + ".csv").toString());

			if (!f.exists()) {
				
				try {
					Files.write(Paths.get(this.segment.getLoggingDirectory() + "/" + this.getLocalName() + ".csv"), ("Time,Vmax,Vcurrent,Service,Num cars\n" + ret.toString()).getBytes());
				}catch (IOException e) {

					e.printStackTrace();
				}

			} else 

				try {
					Files.write(Paths.get(this.segment.getLoggingDirectory() + "/" + this.getLocalName() + ".csv"), ret.toString().getBytes(), StandardOpenOption.APPEND);
				}catch (IOException e) {

					e.printStackTrace();
				}
		}
	}

	/**
	 * Number of cars in this segment
	 * 
	 * @return Number of cars in this segment
	 */
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

	/**
	 * Auxiliary structure to keep track of the cars
	 *
	 */
	private class CarData {

		private String id;
		private float x, y;
		private boolean specialColor;

		public CarData(String id, float x, float y, boolean specialColor) {

			this.id = id;
			this.x = x;
			this.y = y;
			this.specialColor = specialColor;
		}

		public String getId() {
			return id;
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

		public boolean getSpecialColor() {
			return specialColor;
		}

		public void setSpecialColor(boolean specialColor) {
			this.specialColor = specialColor;
		}
	}
}
