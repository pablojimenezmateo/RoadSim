package environment;

import java.util.List;

public class Path {
	
	//The list of intersections of this path
	List<String> intersectionPath;
	
	//The graphical lines of this path
	List<Step> graphicalPath;
	
	//The segments of this path
	List<Segment> segmentPath;
	
	/**
	 * Default constructor.
	 * 
	 * @param intersetcionPath
	 * @param graphicalPath
	 * @param segmentPath
	 */
	public Path(List<String> intersetcionPath, List<Step> graphicalPath, List<Segment> segmentPath) {
		
		this.intersectionPath = intersetcionPath;
		this.graphicalPath = graphicalPath;
		this.segmentPath = segmentPath;
	}

	//Getters
	public List<String> getIntersectionPath() {
		return intersectionPath;
	}

	public List<Step> getGraphicalPath() {
		return graphicalPath;
	}

	public List<Segment> getSegmentPath() {
		return segmentPath;
	}
}
