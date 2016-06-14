package behaviours;

import java.util.HashMap;
import java.util.List;


import agents.EventManagerAgent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class EventManagerBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 4537023518719689317L;

	private EventManagerAgent agent;

	private MessageTemplate mtTick = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("tick"));

	public EventManagerBehaviour(EventManagerAgent agent) {

		this.agent = agent;
	}

	@Override
	public void action() {

		//Block until tick is received
		ACLMessage msg = this.agent.blockingReceive(mtTick);

		if (msg != null) {
			
			int totalMinutes = this.agent.getTimeElapsed() / 60;
			int hours = (int)(totalMinutes / 60);
			int minutes = (int)(totalMinutes % 60);
			
			//If the minute has changed, notify the interface
			if (minutes != this.agent.getPreviousMinute()) {
				
				this.agent.setPreviousMinute(minutes);
				
				ACLMessage timeMsg = new ACLMessage(ACLMessage.INFORM);
				timeMsg.setOntology("updateTimeOntology");
				timeMsg.addReceiver(this.agent.getInterfaceAgent().getName());
				timeMsg.setContent(String.format("%02d", hours) + ":" + String.format("%02d", minutes));

				myAgent.send(timeMsg);
			}

			//Increment the elapsed time
			this.agent.incrementeTimeElapsed();
			
			HashMap<Integer, List<String>> events = this.agent.getEvents();
			int currentTick = this.agent.getTimeElapsed();
			int counter = 0;
			
			//Check for events that need to be fired at this tick
			if (events.containsKey(currentTick)) {
				
				//Execute all the actions
				List<String> actions = events.get(currentTick);
								
				for (String string : actions) {
					
					String parts[] = string.split(",");
					
					if (parts[0].equals("newCar")) {
						
						try {

							AgentController agent = this.agent.getCarContainer().createNewAgent("car" + Integer.toString(currentTick) + Integer.toString(counter), "agents.CarAgent", new Object[]{this.agent.getMap(), parts[2], parts[3], Integer.parseInt(parts[4]), parts[5]});

							agent.start();

						} catch (StaleProxyException e) {

							System.out.println("Error starting a car agent");
							e.printStackTrace();
						}
					} else if (parts[0].equals("segment")) {
						
						msg = new ACLMessage(ACLMessage.INFORM);
						msg.setOntology("eventManagerToSegment");
						msg.addReceiver(new AID(parts[2], AID.ISLOCALNAME));
						msg.setContent(parts[3]);
						
						myAgent.send(msg);
					}
					
					counter++;
				}
			}
		}
	}
}
