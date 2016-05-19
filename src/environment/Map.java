package environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.json.JSONObject;

/**
 * Class that holds the representation of a map.
 *
 */
public class Map {

	@SuppressWarnings("unused")
	private Intersection start;
	private Integer intersectionCount;
	private Integer segmentCount;
	private List<Intersection> intersections;

	/**
	 * Default constructor.
	 * 
	 * @throws IOException
	 */
	public Map() throws IOException{

		this("map/base");
	}

	/**
	 * Constructor that builds a Map from a folder.
	 * 
	 * @param folder Folder where the files are stored.
	 */
	public Map(String folder) throws IOException{

		this.intersectionCount = 0;
		this.segmentCount = 0;

		//Get all files from the given folder
		String url = Map.class.getClassLoader().getResource(folder).toString().split(":")[1];

		File[] files = new File(url).listFiles();

		//Check correct files
		BufferedReader intersectionsReader = null, segmentsReader = null, stepsReader = null;

		for(int i=0; i<files.length; i++){
			if(files[i].getName().equals("intersections")){

				intersectionsReader = new BufferedReader(new FileReader(files[i].getAbsolutePath()));

			}else if(files[i].getName().equals("segments")){

				segmentsReader = new BufferedReader(new FileReader(files[i].getAbsolutePath()));
				
			}else if(files[i].getName().equals("steps")){

				stepsReader = new BufferedReader(new FileReader(files[i].getAbsolutePath()));
			}
		}

		if(segmentsReader == null || intersectionsReader == null || stepsReader == null){

			throw new IOException("Couldn't find the files.");
		}else{

			try{

				//This will be used later to append the segments in an efficient way
				HashMap<String, Intersection> intersectionsAux = new HashMap<String, Intersection>();
				
				String line = intersectionsReader.readLine();

				//Auxiliar structure
				this.intersections = new ArrayList<Intersection>();

				//Read  all the Intersections
				while(line != null){

					JSONObject inter = new JSONObject(line);
					
					Intersection intersection = new Intersection(inter.getString("id"), inter.getJSONObject("coordinates").getInt("x"), inter.getJSONObject("coordinates").getInt("y"));
					
					this.intersections.add(intersection);
					intersectionsAux.put(inter.getString("id"), intersection);

					line = intersectionsReader.readLine();
					this.intersectionCount++;
				}
				
				line = segmentsReader.readLine();

				//This will be used to add the steps later
				HashMap<String, Segment> segmentsAux = new HashMap<String, Segment>();
				
				//Read all the segments				
				while(line != null){

					JSONObject seg = new JSONObject(line);

					Intersection origin = null;
					Intersection destination = null;
					
					//Origin
					if(!seg.getString("origin").equals("null")){

						origin = intersectionsAux.get(seg.getString("origin"));
					}

					//Destination
					if(!seg.getString("destination").equals("null")){

						destination = intersectionsAux.get(seg.getString("destination"));
					}
					
					//Populate the map
					Segment segment = new Segment(seg.getString("id"), origin, destination, seg.getDouble("length"), seg.getInt("capacity"), seg.getInt("numberTracks"));
					
					if(origin != null){
						origin.addOutSegment(segment);
					}

					if(destination != null){
						destination.addInSegment(segment);
					}
					
					segmentsAux.put(segment.getId(), segment);
					
					line = segmentsReader.readLine();
					this.segmentCount++;
				}

				this.start = this.intersections.get(0);
				
				//Read all the steps
				line = stepsReader.readLine();
				
				//Read all the segments				
				while(line != null){

					JSONObject step = new JSONObject(line);

					//The segment the step belongs to
					String idSegment = step.getString("idSegment");
					
					//Create the step
					Step s = new Step(step.getString("id"), idSegment, step.getJSONObject("originCoordinates").getInt("x"), step.getJSONObject("originCoordinates").getInt("y"), step.getJSONObject("destinationCoordinates").getInt("x"), step.getJSONObject("destinationCoordinates").getInt("y"));
					
					//Add the steps to the segment
					segmentsAux.get(idSegment).addStep(s);				

					line = stepsReader.readLine();
				}

			}catch(Exception e){

				e.printStackTrace();
			}finally{

				intersectionsReader.close();
				segmentsReader.close();
				stepsReader.close();
			}
		}
	}

	/**
	 * Dijkstra https://en.wikipedia.org/?title=Dijkstra%27s_algorithm#Pseudocode.
	 * 
	 * @param origin
	 * @return
	 */
	public HashMap<Intersection, Intersection> shortestPathsFrom(String originID){

		HashMap<Intersection, Double> dist = new HashMap<Intersection, Double>();
		HashMap<Intersection, Intersection> prev = new HashMap<Intersection, Intersection>();
		Queue<Intersection> q = new LinkedList<Intersection>();

		Intersection origin = this.getIntersectionByID(originID);

		dist.put(origin, 0.0);
		prev.put(origin, null);

		for(Intersection intersection: this.intersections){

			if(!intersection.equals(origin)){

				dist.put(intersection, Double.MAX_VALUE);
				prev.put(intersection, null);
			}

			q.add(intersection);
		}

		while(!q.isEmpty()){

			Intersection u = minDistance(dist, q);
						
			q.remove(u);

			for(Intersection v: getNeighbours(u)){ //Each neighbour

				if(v == null){
					
					break;
				}
				
				double alt = dist.get(u) + getLenghtToNeighbour(u, v);

				if(alt < dist.get(v)){

					dist.put(v, alt);
					prev.put(v, u);
				}
			}
		}

		return prev;	
	}


	/**
	 * Given the Dijkstra result, this returns the distance to a destination.
	 * 
	 * @param prev
	 * @param destination
	 * @return
	 */
	public double getDistance(HashMap<Intersection, Intersection> prev, String destination){
		
		return this.getDistance(prev, this.getIntersectionByID(destination));
	}

	/**
	 * Same as above using Intersections instead of ID.
	 * 
	 * @param dijks
	 * @param destination
	 * @return
	 */
	public double getDistance(HashMap<Intersection, Intersection> dijks, Intersection destination){

		//Get the path
		double ret = 0;

		Intersection u = destination;

		while(dijks.get(u) != null){
			
			ret += this.getLenghtToNeighbour(u, dijks.get(u));
			u = dijks.get(u);
		}
		
		return ret;
	}
	
	/**
	 * Returns the shortest path to a destination.
	 * 
	 * @param dijks
	 * @param destination
	 * @return
	 */
	public List<String> getPath(HashMap<Intersection, Intersection> dijks, Intersection destination){

		ArrayList<String> ret = new ArrayList<String>();
		
		Intersection u = destination;

		while(dijks.get(u) != null){
			
			ret.add(0, dijks.get(u).getId());
			u = dijks.get(u);
		}
		
		ret.add(ret.size(), destination.getId());
		
		return ret;
	}
	
	public List<Step> getGraphicalPath(HashMap<Intersection, Intersection> dijks, Intersection destination){
		
		List<String> path = this.getPath(dijks, destination);
				
		List<Step> graphicalPath = new LinkedList<Step>();
		
		for( int i=0; i<path.size()-1; i++){
						
			Intersection in = this.getIntersectionByID(path.get(i));
						
			for(Segment seg: in.getOutSegments()){
				
				if (seg.getDestination().getId().equals(path.get(i+1))){
				
					graphicalPath.addAll(seg.getSteps());
				}
			}
		}
		
		return graphicalPath;
	}

	private Intersection minDistance(HashMap<Intersection, Double> dist, Queue<Intersection> queue){

		double min = Double.MAX_VALUE;
		Intersection ret = queue.peek();

		Set<Intersection> keys = dist.keySet();

		for(Intersection intersection: keys){

			if(queue.contains(intersection) && dist.get(intersection) < min){

				min = dist.get(intersection);
				ret = intersection;
			}
		}

		return ret;
	}

	private List<Intersection> getNeighbours(Intersection intersection){

		List<Intersection> ret = new ArrayList<Intersection>();
				
		for(Segment segment: intersection.getOutSegments()){

			ret.add(segment.getDestination());
		}

		return ret;
	}

	private double getLenghtToNeighbour(Intersection origin, Intersection destination){

		double ret = Double.MAX_VALUE;

		for(Segment segment: origin.getOutSegments()){

			if(segment.getDestination().equals(destination)){

				ret = segment.getLength();
				break;
			}
		}

		return ret;
	}

	/**
	 * Given the id of an intersection, it returns that intersection
	 * 
	 * @param id
	 * @return
	 */
	public Intersection getIntersectionByID(String id){

		Intersection ret = null;

		for(Intersection intersection: this.intersections){

			if(intersection.getId().equals(id)){

				ret = intersection;
				break;
			}
		}

		return ret;
	}
	
	/**
	 * Returns a random valid intersection id
	 * 
	 * @return
	 */
	public String getRandomIntersection(){
		
		Random rand = new Random();
		int randomNum = rand.nextInt(this.intersectionCount);
		
		return this.intersections.get(randomNum).getId();
	}
	
	/**
	 * Returns the list with the intersections
	 * 
	 * @return The Intersection list
	 */
	public List<Intersection> getIntersections(){
		
		return this.intersections;
	}
}
