package behaviours;

import agents.CarAgent;
import environment.Step;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This behaviour calculates the next position of the car.  
 *
 */
public class CarBehaviour extends Behaviour {

	private CarAgent agent;
	
	public CarBehaviour(CarAgent a) {
		
		this.agent = a;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void onWake() {

		//If I still have to move somewhere
		if(this.agent.getPath().getGraphicalPath().size() > 0){
			
			//Get the path
			Step next = this.agent.getPath().getGraphicalPath().get(0);
			
			double distNext = Math.sqrt( Math.pow(agent.getX()- next.getDestinationX(), 2) + Math.pow(this.agent.getY()- next.getDestinationY(), 2));

			//Go to the next point if I am close enough
			if(distNext <= 10){

				this.agent.getPath().getGraphicalPath().remove(0);
			}
			
			//Magic with maths to keep the car on the line between two points
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
							
			//Cycles
			this.agent.addBehaviour(new CarBehaviour(this.agent, 100));
		}
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
}