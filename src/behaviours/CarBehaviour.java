package behaviours;

import agents.CarAgent;
import environment.Segment;
import environment.Step;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This behaviour is used by the CarAgent and calculates the next 
 * graphical position of the car. It also registers and deregisters
 * the car from the segments.
 * 
 * The car is registered when it enters a new segment and deregistered
 * when it leaves a segment.
 *
 */
public class CarBehaviour extends TickerBehaviour {

	private CarAgent agent;

	public CarBehaviour(CarAgent a, long timeout) {
		super(a, timeout);
		this.agent = a;
	}

	private static final long serialVersionUID = 1L;

	@Override
	protected void onTick() {

		//If I still have to move somewhere
		if(this.agent.getPath().getGraphicalPath().size() > 0){

			//Get the path
			Step next = this.agent.getPath().getGraphicalPath().get(0);

			//Set the previous segment
			if (this.agent.getPreviousSegment() == null) {

				this.agent.setPreviousSegment(next.getSegment());

				//Register
				this.informSegment(next.getSegment());
			}

			//If we are in a new segment
			if (!this.agent.getPreviousSegment().equals(next.getSegment())) {

				//Deregister from previous segment
				this.informSegment(this.agent.getPreviousSegment());

				//Set the new previous segment
				this.agent.setPreviousSegment(next.getSegment());

				//Register in the new segment
				this.informSegment(next.getSegment());
			}

			//The distance between my current position and my next desired position
			double distNext = Math.sqrt( Math.pow(agent.getX()- next.getDestinationX(), 2) + Math.pow(this.agent.getY()- next.getDestinationY(), 2));

			//TODO: This doesn't seem like a good idea
			//Go to the next leg if I am close enough
			if(distNext <= 10){

				this.agent.getPath().getGraphicalPath().remove(0);
			}

			//Magic with math to keep the car on the line between two points
			double T = this.agent.getCurrentSpeed() / distNext;

			//Next point within the line
			this.agent.setX((int)((1 - T) * this.agent.getX() + T * next.getDestinationX()));
			this.agent.setY((int)((1 - T) * this.agent.getY() + T * next.getDestinationY()));

			//Send the new position to the interface
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(this.agent.getInterfaceAgent().getName());
			msg.setConversationId(this.agent.getId());
			msg.setContent("x=" + this.agent.getX() + "y=" + this.agent.getY()); 
			myAgent.send(msg);

		} else { //I have arrived to my destination

			//TODO: Remove car from GUI
			this.stop();
		}

	}

	//This method will send a message to a given segment to register/deregister
	//to/from it
	private void informSegment(Segment segment) {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegment");
		msg.addReceiver(segment.getSegmentAgent().getAID());
		msg.setContent(this.agent.getId());

		myAgent.send(msg);
	}

}