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

			//The proportion of the map is 1px ~= 29m and one tick is one 1 s
			//Calculate the pixels per tick I have to move
			float increment = ((this.agent.getCurrentSpeed() * 0.2778f) * 0.035f);

			//Virtual position
			float currentX = this.agent.getX();
			float currentY = this.agent.getY();

			//The distance between my current position and my next desired position
			float distNext = (float) Math.sqrt(Math.pow(currentX - next.getDestinationX(), 2) + Math.pow(currentY - next.getDestinationY(), 2));

			//Check if we need to go to the next step
			while (increment > distNext) {

				//If there is still a node to go
				if (this.agent.getPath().getGraphicalPath().size() > 1){
					
					//Remove the already run path
					increment -= distNext;

					this.agent.getPath().getGraphicalPath().remove(0);
					next = this.agent.getPath().getGraphicalPath().get(0);

					currentX = next.getOriginX();
					currentY = next.getOriginY();

					distNext = (float) Math.sqrt(Math.pow(currentX - next.getDestinationX(), 2) + Math.pow(currentY - next.getDestinationY(), 2));
				} else {
					
					this.stop();
					break;
				}
			}

			//Proportion inside the segment
			float proportion = increment / distNext;

			this.agent.setX(((1 - proportion) * currentX + proportion * next.getDestinationX()));
			this.agent.setY(((1 - proportion) * currentY + proportion * next.getDestinationY()));

			//If we are in a new segment
			if (!this.agent.getPreviousSegment().equals(next.getSegment())) {

				//Deregister from previous segment
				this.informSegment(this.agent.getPreviousSegment());

				//Set the new previous segment
				this.agent.setPreviousSegment(next.getSegment());

				//Register in the new segment
				this.informSegment(next.getSegment());
			}

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