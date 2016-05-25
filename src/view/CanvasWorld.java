package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import environment.Intersection;
import environment.Map;
import environment.Segment;
import environment.Step;

public class CanvasWorld extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final int FPS = 40;

	private PanelRadar contentPane;
	private Map map = null;

	private String interfaceAgent;
	public static int MAXWORLDX, MAXWORLDY;
	
	private Timer timer = new Timer(1000/this.FPS, this);

	public CanvasWorld (String interfaceAgent, int maxX, int maxY, Map map) {
		super();

		MAXWORLDX = maxX;
		MAXWORLDY = maxY;

		this.interfaceAgent = interfaceAgent;
		
		this.map = map;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setTitle("Interface: "+ this.interfaceAgent);
		setBounds(10, 10, MAXWORLDX, MAXWORLDY);
		contentPane = new PanelRadar();
		setContentPane(contentPane);

		//Show the frame
		setVisible(true);
		
		this.timer.start();
	}

	//Adds a new car to the GUI
	public void addCar(String ag, String id, int x, int y) {

		contentPane.addCar(ag, id, x, y);	
	}

	//Moves an existing car
	public void moveCar(String id, int x, int y) {
		contentPane.moveCar(id, x, y);
	}

	public class PanelRadar extends JPanel{

		private static final long serialVersionUID = 1L;
		private ArrayList<Mobile> carPositions;

		private Image backGround;
		private ImageIcon mapImage = new ImageIcon(getClass().getResource("red.png"));

		public PanelRadar() {
			carPositions =  new ArrayList<Mobile>();
			backGround = mapImage.getImage();

			this.setBorder(new EmptyBorder(1, 1, 1, 1));
			this.setDoubleBuffered(true);
			this.setLayout(null);
		}

		public void addCar(String ag, String id, int x, int y) {
			carPositions.add(new Mobile(x, y, id));
			repaint();
		}

		public void moveCar(String id, int x, int y) {

			for(Mobile m: carPositions){

				if (id.equals(m.getId())) {
					m.setX(x);
					m.setY(y);
					break;
				}
			}
		}

		public void paint(Graphics gi) {

			Graphics2D g = (Graphics2D) gi;

			//Draw the background
			g.drawImage(backGround, 0, 0, this);

			//Draw the segments
			for (Intersection in : map.getIntersections()) {

				g.setStroke(new BasicStroke(2));
				g.setColor(Color.GREEN);
				
				for (Segment s: in.getOutSegments()){
					
					for(Step st: s.getSteps()){
						
						g.drawLine(st.getOriginX(), st.getOriginY(), st.getDestinationX(), st.getDestinationY());
					}
				}
			}

			//Draw the intersections
			for (Intersection in : map.getIntersections()) {

				g.setColor(Color.RED);
				g.fillOval(in.getX()-2, in.getY()-2, 4, 4);
				
				//Draw the names of the intersections
				//				g.setColor(Color.black);	
				//				g.fillRect(in.coordinates[0]-40, in.coordinates[1]+10, 100, 20);
				//				g.setColor(Color.white);
				//				g.drawString(in.id, in.coordinates[0]-35, in.coordinates[1]+25);
				//				g.setColor(Color.black);	
			}

			//Draw the cars
			for (Mobile m : carPositions) {
				
				int x = m.getX();
				int y = m.getY();

//				g.setColor(Color.WHITE);
//				g.fillRect(x - 4, y - 8, 8, 4); //Windows
//				g.fillRect(x - 9, y- 4, 18, 8); //Chasis
//
//				g.setColor(Color.BLACK); //Borders
//				g.setStroke(new BasicStroke(1));
//				g.drawRect(x - 4, y - 8, 8, 4); //Windows
//				g.drawRect(x - 9, y - 4, 18, 8); //Chasis
//
//				//Tires
//				g.setColor(Color.GRAY);
//				g.fillOval(x - 8, y, 6, 6);
//				g.fillOval(x + 2, y, 6, 6);
				
				//Chicken
				
				//Body
				g.setColor(Color.YELLOW);
				g.fillOval(x - 4, y - 4, 8, 8);
				g.setColor(Color.ORANGE);
				g.drawOval(x - 4, y - 4, 8, 8);

				//Beak
				g.setStroke(new BasicStroke(1));
				g.setColor(Color.ORANGE);
				g.fillPolygon(new int[] {x - 3, x - 3, x - 8}, new int[] {y - 4, y + 4, y}, 3);
				
				//Eye
				g.setColor(Color.BLACK);
				g.fillOval(x - 2, y - 2, 2, 2);
				
				//Feet
				g.setStroke(new BasicStroke(1));
				
				//Make the walking animation
				boolean rightStep = false;;
				
				if(Math.random() < 0.5) {
				    rightStep = true;
				}
				
				if (rightStep) {
					
					//Right foot
					g.drawLine(x + 2, y + 1, x + 4, y + 4);
					g.drawLine(x + 2, y + 1, x + 2, y + 5);
					g.drawLine(x + 2, y + 1, x,     y + 4);
					
					//Left foot
					g.drawLine(x - 3, y + 3, x - 1, y + 5);
					g.drawLine(x - 3, y + 3, x - 3, y + 6);
					g.drawLine(x - 3, y + 3, x - 6, y + 5);
					
				} else {
					
					//Right foot
					g.drawLine(x + 2, y + 3, x + 4, y + 5);
					g.drawLine(x + 2, y + 3, x + 2, y + 6);
					g.drawLine(x + 2, y + 3, x,     y + 5);
					
					//Left foot
					g.drawLine(x - 3, y + 2, x - 1, y + 6);
					g.drawLine(x - 3, y + 2, x - 3, y + 5);
					g.drawLine(x - 3, y + 2, x - 6, y + 4);
				}
			}			
		}

		public class Mobile {
			
			int x;
			int y;
			
			String id;

			public int getX() {
				return x;
			}
			public void setX(int x) {
				this.x = x;
			}
			public int getY() {
				return y;
			}
			public void setY(int y) {
				this.y = y;
			}
			public String getId() {
				return id;
			}

			public Mobile(int x, int y, String id) {
				super();
				this.x = x;
				this.y = y;
				this.id = id;

			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == this.timer) {
			
			repaint();
		}
	}
}