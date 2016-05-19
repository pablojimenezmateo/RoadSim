package environment;

import java.util.UUID;

/**
 * This class represents a step in a polyline of a segment.
 *
 */
public class Step {

	//Unique id
	private String id;
	
	//Id of the segment it belongs to
	private String segmentId;
	
	//Coordinates of the line
	private int originX, originY, destinationX, destinationY;
	
	//Default constructor
	public Step(){
		
		this.id = UUID.randomUUID().toString();
		this.segmentId = "";
		this.originX = 0;
		this.originY = 0;
		this.destinationX = 0;
		this.destinationY = 0;
	}
	
	//Constructor
	public Step(String id, String segmentId, int originX, int originY, int destinationX, int destinationY){
		
		this.id = id;
		this.segmentId = segmentId;
		this.originX = originX;
		this.originY = originY;
		this.destinationX = destinationX;
		this.destinationY = destinationY;
	}
	
	//Setters and getters
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSegmentId() {
		return segmentId;
	}

	public void setSegmentId(String segmentId) {
		this.segmentId = segmentId;
	}

	public int getOriginX() {
		return originX;
	}

	public void setOriginX(int originX) {
		this.originX = originX;
	}

	public int getOriginY() {
		return originY;
	}

	public void setOriginY(int originY) {
		this.originY = originY;
	}

	public int getDestinationX() {
		return destinationX;
	}

	public void setDestinationX(int destinationX) {
		this.destinationX = destinationX;
	}

	public int getDestinationY() {
		return destinationY;
	}

	public void setDestinationY(int destinationY) {
		this.destinationY = destinationY;
	}	
}
