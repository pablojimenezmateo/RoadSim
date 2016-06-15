package behaviours;

import agents.CarAgent;
import environment.Segment;
import environment.Step;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the CarAgent and calculates the next 
 * graphical position of the car. It also registers and deregisters
 * the car from the segments.
 * 
 * The car is registered when it enters a new segment and deregistered
 * when it leaves a segment.
 *
 */
public class CarBehaviour extends CyclicBehaviour {

	private CarAgent agent;
	private MessageTemplate mtTick = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("tick"));
	private boolean done = false;

	public CarBehaviour(CarAgent a, long timeout) {
		this.agent = a;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void action() {

		//Block until tick is received
		ACLMessage msg = this.agent.blockingReceive(mtTick);

		if (msg != null) {

			//If I still have to move somewhere
			if(this.agent.getPath().getGraphicalPath().size() > 0) {

				//Get the path
				Step next = this.agent.getPath().getGraphicalPath().get(0);

				//Set the previous segment
				if (this.agent.getPreviousSegment() == null) {

					this.agent.setPreviousSegment(next.getSegment());

					//Register
					this.informSegment(next.getSegment(), "register");

					//Change my speed according to the maximum allowed speed
					this.agent.setCurrentSpeed(Math.min(this.agent.getMaxSpeed(), this.agent.getPreviousSegment().getCurrentAllowedSpeed()));
					
					//If we are going under the maximum speed I'm allowed to go, or I can go, I am in a congestion, draw me differently
					if (this.agent.getCurrentSpeed() < Math.min(this.agent.getMaxSpeed(), this.agent.getPreviousSegment().getMaxSpeed())) {
						
						this.agent.setSpecialColor(true);
					} else {
						
						this.agent.setSpecialColor(false);
					}
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

						this.kill();
						break;
					}
				}

				if (!this.done) {
					
					//Proportion inside the segment
					float proportion = increment / distNext;

					this.agent.setX(((1 - proportion) * currentX + proportion * next.getDestinationX()));
					this.agent.setY(((1 - proportion) * currentY + proportion * next.getDestinationY()));

					//If we are in a new segment
					if (!this.agent.getPreviousSegment().equals(next.getSegment())) {

						//TODO: Check when this happens
						//if (this.agent.getPreviousSegment() != null) {

							//Deregister from previous segment
							this.informSegment(this.agent.getPreviousSegment(), "deregister");
						//}

						//Set the new previous segment
						this.agent.setPreviousSegment(next.getSegment());

						//Register in the new segment
						this.informSegment(next.getSegment(), "register");
						
						//If we are using the smart algorithm, recalculate
						if (this.agent.isSmart()) {
							
							this.agent.recalculate(this.agent.getPreviousSegment().getOrigin().getId());
						}
					}
					
					//Change my speed according to the maximum allowed speed
					this.agent.setCurrentSpeed(Math.min(this.agent.getMaxSpeed(), this.agent.getPreviousSegment().getCurrentAllowedSpeed()));
					
					//If we are going under the maximum speed I'm allowed to go, or I can go, I am in a congestion, draw me differently
					if (this.agent.getCurrentSpeed() < Math.min(this.agent.getMaxSpeed(), this.agent.getPreviousSegment().getMaxSpeed())) {
						
						this.agent.setSpecialColor(true);
					} else {
						
						this.agent.setSpecialColor(false);
					}

					this.informSegment(next.getSegment(), "update");
				}
			}
		}
	}

	//This method will send a message to a given segment
	private void informSegment(Segment segment, String type) {

		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegment");
		msg.setConversationId(type);
		msg.addReceiver(segment.getSegmentAgent().getAID());
		msg.setContent(this.agent.getId() + "#" + Float.toString(this.agent.getX()) + "#" + Float.toString(this.agent.getY()) + "#" + this.agent.getSpecialColor() + "#");

		myAgent.send(msg);
	}

	public void kill() {

		//Done flag
		this.done = true;

		//Deregister from previous segment
		this.informSegment(this.agent.getPreviousSegment(), "deregister");

		//Delete the car from the canvas
		if (this.agent.getInterfaceAgent() != null) {

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("deleteOntology");
			msg.addReceiver(this.agent.getInterfaceAgent().getName());
			msg.setContent(this.agent.getId());

			myAgent.send(msg);
		}
		
		//Deregister the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.agent.getAID());
		
		try {
			DFService.deregister(this.agent,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}

		this.agent.doDelete();
	}
}