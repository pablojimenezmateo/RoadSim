package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.HashMap;

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

		setTitle("Interface: " + this.interfaceAgent);
		setBounds(10, 10, MAXWORLDX, MAXWORLDY);
		contentPane = new PanelRadar();
		setContentPane(contentPane);

		//Show the frame
		setVisible(true);

		this.timer.start();
	}

	//Adds a new car to the GUI
	public void addCar(String ag, String id, float x, float y) {

		contentPane.addCar(ag, id, x, y);	
	}

	//Moves an existing car
	public void moveCar(String id, float x, float y) {
		contentPane.moveCar(id, x, y);
	}

	public class PanelRadar extends JPanel{

		private static final long serialVersionUID = 1L;
		//private ArrayList<Mobile> carPositions;
		private HashMap<String, Mobile> carPositions;

		private Image backGround;
		private ImageIcon mapImage = new ImageIcon(getClass().getResource("red.png"));

		public PanelRadar() {
			this.carPositions =  new HashMap<String, Mobile>();
			backGround = mapImage.getImage();

			this.setBorder(new EmptyBorder(1, 1, 1, 1));
			this.setDoubleBuffered(true);
			this.setLayout(null);
		}

		public void addCar(String ag, String id, float x, float y) {
			
			carPositions.put(id, new Mobile(x, y, id));
			repaint();
		}

		public void moveCar(String id, float x, float y) {

			Mobile m = carPositions.get(id);
			m.setX(x);
			m.setY(y);
		}

		public void paint(Graphics gi) {

			Graphics2D g = (Graphics2D) gi;

			//Draw the background
			g.drawImage(backGround, 0, 0, this);

			Line2D line;

			//Draw the segments
			for (Intersection in : map.getIntersections()) {

				g.setStroke(new BasicStroke(2));
				g.setColor(Color.GREEN);

				for (Segment s: in.getOutSegments()){

					for(Step st: s.getSteps()){

						line = new Line2D.Float(st.getOriginX(), st.getOriginY(), st.getDestinationX(), st.getDestinationY());
						g.draw(line);
					}
				}
			}

			Ellipse2D oval;

			//Draw the intersections
			for (Intersection in : map.getIntersections()) {

				g.setColor(Color.RED);
				
				oval = new Ellipse2D.Float(in.getX()-2, in.getY()-2, 4, 4);
				g.fill(oval);

				//Draw the names of the intersections
				//				g.setColor(Color.black);	
				//				g.fillRect(in.coordinates[0]-40, in.coordinates[1]+10, 100, 20);
				//				g.setColor(Color.white);
				//				g.drawString(in.id, in.coordinates[0]-35, in.coordinates[1]+25);
				//				g.setColor(Color.black);	
			}

			//Draw the cars
			for (Mobile m : carPositions.values()) {
				
				float x = m.getX();
				float y = m.getY();

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
				oval = new Ellipse2D.Float(x - 4, y - 4, 8, 8);
				g.fill(oval);
				
				g.setColor(Color.ORANGE);
				oval = new Ellipse2D.Float(x - 4, y - 4, 8, 8);
				g.draw(oval);

				//Beak
				g.setStroke(new BasicStroke(1));
				g.setColor(Color.ORANGE);
				
				Path2D beak = new Path2D.Float();
				beak.moveTo(x + 3, y - 4);
				beak.lineTo(x - 3, y + 4);
				beak.lineTo(x - 8, y);
				beak.lineTo(x + 3, y - 4);

				//g.fillPolygon(new int[] {x - 3, x - 3, x - 8}, new int[] {y - 4, y + 4, y}, 3);
				g.fill(beak);

				//Eye
				g.setColor(Color.BLACK);
				oval = new Ellipse2D.Float(x - 2, y - 2, 2, 2);
				g.fill(oval);

				//Feet
				g.setStroke(new BasicStroke(1));

				//Make the walking animation
				boolean rightStep = false;;

				if(Math.random() < 0.5) {
					rightStep = true;
				}

				Line2D.Float lineF;

				if (rightStep) {

					//Right foot
					lineF = new Line2D.Float(x + 2, y + 1, x + 4, y + 4);
					g.draw(lineF);
					lineF = new Line2D.Float(x + 2, y + 1, x + 2, y + 5);
					g.draw(lineF);
					lineF = new Line2D.Float(x + 2, y + 1, x,     y + 4);
					g.draw(lineF);

					//Left foot
					lineF = new Line2D.Float(x - 3, y + 3, x - 1, y + 5);
					g.draw(lineF);
					lineF = new Line2D.Float(x - 3, y + 3, x - 3, y + 6);
					g.draw(lineF);
					lineF = new Line2D.Float(x - 3, y + 3, x - 6, y + 5);
					g.draw(lineF);

				} else {

					//Right foot
					lineF = new Line2D.Float(x + 2, y + 3, x + 4, y + 5);
					g.draw(lineF);
					lineF = new Line2D.Float(x + 2, y + 3, x + 2, y + 6);
					g.draw(lineF);
					lineF = new Line2D.Float(x + 2, y + 3, x,     y + 5);
					g.draw(lineF);

					//Left foot
					lineF = new Line2D.Float(x - 3, y + 2, x - 1, y + 6);
					g.draw(lineF);
					lineF = new Line2D.Float(x - 3, y + 2, x - 3, y + 5);
					g.draw(lineF);
					lineF = new Line2D.Float(x - 3, y + 2, x - 6, y + 4);
					g.draw(lineF);
				}
			}			
		}

		public class Mobile {

			private float x;
			private float y;

			private String id;

			public Mobile(float x, float y, String id) {
				super();
				this.setX(x);
				this.setY(y);
				this.setId(id);

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

			public String getId() {
				return id;
			}

			public void setId(String id) {
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