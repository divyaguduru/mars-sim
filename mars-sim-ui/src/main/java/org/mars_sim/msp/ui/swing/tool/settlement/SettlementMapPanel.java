/**
 * Mars Simulation Project
 * SettlementMapPanel.java
 * @version 3.1.0 2017-02-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.settlement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JLayer;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.LayerUI;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.DesktopPane;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.SpotlightLayerUI;

/**
 * A panel for displaying the settlement map.
 */
public class SettlementMapPanel
extends JPanel
implements ClockListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// default logger.
	private static Logger logger = Logger.getLogger(SettlementMapPanel.class.getName());

	// Static members.
	private static final double PERIOD_IN_MILLISOLS = 15_000D / MarsClock.SECONDS_IN_MILLISOL;
	public static final double DEFAULT_SCALE = 10D;
	public static final double MAX_SCALE = 55D;
	public static final double MIN_SCALE = 5D / 11D;
	private static final Color MAP_BACKGROUND = new Color(181, 95, 0);

	// Data members
	private double timeCache = 0;
	private double xPos;
	private double yPos;
	private double rotation;
	private double scale;

	/** Last X mouse drag position. */
	private int xLast;
	/** Last Y mouse drag position. */
	private int yLast;

	private boolean showBuildingLabels;
	private boolean showConstructionLabels;
	private boolean showPersonLabels;
	private boolean showVehicleLabels;
	private boolean showRobotLabels;
	private boolean showDaylightLayer;

	private List<SettlementMapLayer> mapLayers;
	private Map<Settlement, Person> selectedPerson;
	private Map<Settlement, Robot> selectedRobot;

	private Building building;
	private SettlementWindow settlementWindow;
	private Settlement settlement;
	private PopUpUnitMenu menu;
	private SettlementTransparentPanel settlementTransparentPanel;

	private Graphics dbg;
	//private Graphics2D g2d;
	private Image dbImage = null;

	/** Constructor 1
	 * 	A panel for displaying a settlement map.
	 */
	public SettlementMapPanel(MainDesktopPane desktop, final SettlementWindow settlementWindow) {
		super();
		this.settlementWindow = settlementWindow;

		settlement = Simulation.instance().getUnitManager().getSettlementOList().get(0);

		setLayout(new BorderLayout());

		setDoubleBuffered(true);

		// Initialize data members.
		xPos = 0D;
		yPos = 0D;
		rotation = 0D;
		scale = DEFAULT_SCALE;
		showBuildingLabels = false;
		showConstructionLabels = false;
		showPersonLabels = false;
		showVehicleLabels = false;
		showRobotLabels = false;
		showDaylightLayer = true;//false;  // turn off by default
		selectedPerson = new HashMap<Settlement, Person>();
		selectedRobot = new HashMap<Settlement, Robot>();

		//logger.info("PERIOD_IN_MILLISOLS : " + PERIOD_IN_MILLISOLS);
		SwingUtilities.invokeLater(() -> {
			initLayers(desktop);
		});

		// Set foreground and background colors.
		setOpaque(false);
		setBackground(MAP_BACKGROUND);
		setForeground(Color.ORANGE);

		Simulation.instance().getMasterClock().addClockListener(this);

		// 2015-01-16 Added detectMouseMovement() after refactoring
		SwingUtilities.invokeLater(() -> {
			detectMouseMovement();
			setFocusable(true);
			requestFocusInWindow();
		});

		//SwingUtilities.updateComponentTreeUI(this);

        setVisible(true);
	}

	// 2015-02-09 Added initLayers()
	public void initLayers(MainDesktopPane desktop) {
		// Create map layers.
		mapLayers = new ArrayList<SettlementMapLayer>();
		mapLayers.add(new BackgroundTileMapLayer(this));
		mapLayers.add(new StructureMapLayer(this));
		mapLayers.add(new VehicleMapLayer(this));
		mapLayers.add(new PersonMapLayer(this));
		mapLayers.add(new RobotMapLayer(this));
		mapLayers.add(new LabelMapLayer(this));
		mapLayers.add(new DayNightMapLayer(this));
		
		//SwingUtilities.invokeLater(() -> {
			if (desktop.getMainScene() == null)
				settlementTransparentPanel = new SettlementTransparentPanel(desktop, this);
		//});

	}

	/** Constructor 2
	 *  A panel for initializing the display of a building svg image.
	 */
	// 2014-11-04 Added this constructor for loading an svg image
	// Called by BuildingPanel.java
	public SettlementMapPanel(Settlement settlement, Building building) {
		super();

		// Initialize data members.
		xPos = 0D;
		yPos = 0D;
		rotation = 0D;
		scale = DEFAULT_SCALE;
		this.settlement = settlement;
		this.building = building;

		mapLayers = new ArrayList<SettlementMapLayer>(1);
		StructureMapLayer layer = new StructureMapLayer(this);
		mapLayers.add(layer);

		// Set preferred size.
		setPreferredSize(new Dimension(100, 100));

		// Set foreground and background colors.
		setOpaque(true);

	}

	public void detectMouseMovement() {
/*
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent evt) {
				//setCursor(new Cursor(Cursor.HAND_CURSOR));
				if (evt.getButton() == MouseEvent.BUTTON3) {
					// Set initial mouse drag position.
					xLast = evt.getX();
					yLast = evt.getY();
				}
			}

			@Override
			public void mouseClicked(MouseEvent evt) {
				// Select person if clicked on.
				setCursor(new Cursor(Cursor.HAND_CURSOR));
				selectPersonAt(evt.getX(), evt.getY());
				selectRobotAt(evt.getX(), evt.getY());
			}

		});
*/
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent evt) {
				if (evt.getButton() == MouseEvent.BUTTON3) {
					double xDiff = evt.getX() - xLast;
					double yDiff = evt.getY() - yLast;
					xLast = evt.getX();
					yLast = evt.getY();
					//System.out.println("button3");
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
					// Move map center based on mouse drag difference.
					moveCenter(xDiff, yDiff);

				}

			}

			@Override
			public void mouseMoved(MouseEvent evt) {
				//System.out.println("mouseDragged()");
				if (evt.getButton() == MouseEvent.BUTTON3) {
					double xDiff = evt.getX() - xLast;
					double yDiff = evt.getY() - yLast;
					xLast = evt.getX();
					yLast = evt.getY();
					//System.out.println("button3");
					setCursor(new Cursor(Cursor.MOVE_CURSOR));
					// Move map center based on mouse drag difference.
					moveCenter(xDiff, yDiff);

				}
			}
		});

		//2014-11-22 Added PopClickListener() to detect mouse right click
		class PopClickListener extends MouseAdapter {

			//@Override
			public void mouseClicked(MouseEvent evt) {
				// Select person if clicked on.
				setCursor(new Cursor(Cursor.HAND_CURSOR));
				selectPersonAt(evt.getX(), evt.getY());
				selectRobotAt(evt.getX(), evt.getY());
			}

			//@Override
			public void mouseEntered(MouseEvent evt) {
				//mouseMoved(evt);

			}

			public void mouseExited(MouseEvent evt) {
				//mouseMoved(evt);
			}

			/*
			@Override
			public void mouseClicked(MouseEvent evt) {
				// Select person if clicked on.
				if (evt.getButton() == MouseEvent.BUTTON1
						|| evt.getButton() == MouseEvent.BUTTON3) {
					setCursor(new Cursor(Cursor.HAND_CURSOR));
					selectPersonAt(evt.getX(), evt.getY());
					selectRobotAt(evt.getX(), evt.getY());
				}

				else if (evt.isPopupTrigger()) {
					 setCursor(new Cursor(Cursor.HAND_CURSOR));
					 doPop(evt);
				}

			}
*/

			@Override
		    public void mousePressed(MouseEvent evt){
				setCursor(new Cursor(Cursor.HAND_CURSOR));

				if (evt.getButton() == MouseEvent.BUTTON3) {
					// Set initial mouse drag position.
					xLast = evt.getX();
					yLast = evt.getY();

					 if (evt.isPopupTrigger()) {
						 setCursor(new Cursor(Cursor.HAND_CURSOR));
						 //repaint();
						 doPop(evt);
					 }
				}
		    }

			@Override
		    public void mouseReleased(MouseEvent evt){
		    	//setCursor(new Cursor(Cursor.HAND_CURSOR));
				//setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

				if (evt.getButton() == MouseEvent.BUTTON3) {
					xLast = 0;//evt.getX();
					yLast = 0;//evt.getY();

					if (evt.isPopupTrigger()) {
						setCursor(new Cursor(Cursor.HAND_CURSOR));
						doPop(evt);
					}
				}

		    }

		    //2015-01-14 Added vehicle detection
		    private void doPop(final MouseEvent evt) {
		    	//System.out.println("doPop()");
		    	final ConstructionSite site = selectConstructionSiteAt(evt.getX(), evt.getY());
		    	final Building building = selectBuildingAt(evt.getX(), evt.getY());
		    	final Vehicle vehicle = selectVehicleAt(evt.getX(), evt.getY());
		    	final Person person = selectPersonAt(evt.getX(), evt.getY());
		    	final Robot robot = selectRobotAt(evt.getX(), evt.getY());

		    	// if NO building is selected, do NOT call popup menu
		    	if (site != null || building != null || vehicle != null || person != null || robot != null) {

	        		// 2015-01-16 Deconflict cases by the virtue of the if-else order below
    	        	// when one or more are detected
    	        	if (person != null)
    	        		menu = new PopUpUnitMenu(settlementWindow, person);
    	        	else if (robot != null)
    	        		menu = new PopUpUnitMenu(settlementWindow, robot);
    	        	else if (vehicle != null)
    	        		menu = new PopUpUnitMenu(settlementWindow, vehicle);
    	        	else if (building != null)
    	        		menu = new PopUpUnitMenu(settlementWindow, building);
    	        	else if (site != null)
    	        		menu = new PopUpUnitMenu(settlementWindow, site);

    	        	//setComponentPopupMenu(menu);
    	        	menu.show(evt.getComponent(), evt.getX(), evt.getY());
		        }
				//repaint();
		    }
		}// end of class PopClickListener

		addMouseListener(new PopClickListener());

	}

	/**
	 * Gets the settlement currently displayed.
	 * @return settlement or null if none.
	 */
	public Settlement getSettlement() {
		return settlement;
	}

	/**
	 * Gets the SettlementWindow class.
	 * @return settlementWindow or null if none.
	 */
	public SettlementWindow getSettlementWindow() {
		return settlementWindow;
	}

	/**
	 * Sets the settlement to display.
	 * @param settlement the settlement.
	 */
	// Called by SettlementTransparentPanel to update settlement
	public synchronized void setSettlement(Settlement newSettlement) {
		if (newSettlement != settlement) {

			this.settlement = newSettlement;
			if (settlementWindow != null && settlementWindow.getMarqueeTicker() != null)
				settlementWindow.getMarqueeTicker().updateSettlement(newSettlement);
			//paintDoubleBuffer();
			repaint();
		}
	}

	/**
	 * Gets the map scale.
	 * @return scale (pixels per meter).
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * Sets the map scale.
	 * @param scale (pixels per meter).
	 */
	public void setScale(double scale) {
		this.scale = scale;

		//paintDoubleBuffer();
		repaint();
	}

	/**
	 * Gets the map rotation.
	 * @return rotation (radians).
	 */
	public double getRotation() {
		return rotation;
	}

	/**
	 * Sets the map rotation.
	 * @param rotation (radians).
	 */
	public void setRotation(double rotation) {
		this.rotation = rotation;

		//paintDoubleBuffer();
		repaint();
	}

	/**
	 * Resets the position, scale and rotation of the map.
	 * Separate function that only uses one repaint.
	 */
	public void reCenter() {
		xPos = 0D;
		yPos = 0D;
		setRotation(0D);
		scale = DEFAULT_SCALE;

		//paintDoubleBuffer();
		repaint();
	}

	/**
	 * Moves the center of the map by a given number of pixels.
	 * @param xDiff the X axis pixels.
	 * @param yDiff the Y axis pixels.
	 */
	public void moveCenter(double xDiff, double yDiff) {
		xDiff /= scale;
		yDiff /= scale;

		// Correct due to rotation of map.
		double realXDiff = (Math.cos(rotation) * xDiff) + (Math.sin(rotation) * yDiff);
		double realYDiff = (Math.cos(rotation) * yDiff) - (Math.sin(rotation) * xDiff);

		xPos += realXDiff;
		yPos += realYDiff;

		//paintDoubleBuffer();
		repaint();
	}

	/**
	 * Selects a person if any person is at the given x and y pixel position.
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedPerson;
	 */
	public Person selectPersonAt(int xPixel, int yPixel) {

		Point.Double settlementPosition = convertToSettlementLocation(xPixel, yPixel);
		double range = 6D / scale;
		Person selectedPerson = null;

		Iterator<Person> i = PersonMapLayer.getPeopleToDisplay(settlement).iterator();
		while (i.hasNext()) {
			Person person = i.next();
			double distanceX = person.getXLocation() - settlementPosition.getX();
			double distanceY = person.getYLocation() - settlementPosition.getY();
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= range) {
				selectedPerson = person;
			}
		}

		if (selectedPerson != null) {
			selectPerson(selectedPerson);


			////paintDoubleBuffer();
			repaint();
		}
		return selectedPerson;
	}

	/**
	 * Selects the robot if any robot is at the given x and y pixel position.
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedRobot;
	 */
	public Robot selectRobotAt(int xPixel, int yPixel) {

		Point.Double settlementPosition = convertToSettlementLocation(xPixel, yPixel);
		double range = 6D / scale;
		Robot selectedRobot = null;

		Iterator<Robot> i = RobotMapLayer.getRobotsToDisplay(settlement).iterator();
		while (i.hasNext()) {
			Robot robot = i.next();
			double distanceX = robot.getXLocation() - settlementPosition.getX();
			double distanceY = robot.getYLocation() - settlementPosition.getY();
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= range) {
				selectedRobot = robot;
			}
		}

		if (selectedRobot != null) {
			selectRobot(selectedRobot);

			////paintDoubleBuffer();
			repaint();
		}
		return selectedRobot;
	}


	/**
	 * Selects a building
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedBuilding
	 */
	// 2014-11-22 Added building selection
	public Building selectBuildingAt(int xPixel, int yPixel) {
		//System.out.println("selectBuildingAt()");
		Point.Double clickPosition = convertToSettlementLocation(xPixel, yPixel);
		Building selectedBuilding = null;

		Iterator<Building> j = settlement.getBuildingManager().getBuildings().iterator();
		while (j.hasNext()) {
			Building building = j.next();
			//System.out.println("building : " + building.getNickName());
			if (!building.getInTransport()) {
				//System.out.println("building : " + building.getNickName());
				double width = building.getWidth();
				double length = building.getLength();
				int facing = (int) building.getFacing();
				double x = building.getXLocation();
				double y = building.getYLocation();
				double xx = 0;
				double yy = 0;

				if (facing == 0) {
					xx = width/2D;
					yy = length/2D;
				}
				else if (facing == 90){
					yy = width/2D;
					xx = length/2D;
				}
				// Loading Dock Garage
				if (facing == 180) {
					xx = width/2D;
					yy = length/2D;
				}
				else if (facing == 270){
					yy = width/2D;
					xx = length/2D;
				}

				// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
				// Fortunately, they both have the same width and length
				else if (facing == 45){
					yy = width/2D;
					xx = length/2D;
				}
				else if (facing == 135){
					yy = width/2D;
					xx = length/2D;
				}

				double c_x = clickPosition.getX();
				double c_y = clickPosition.getY();


				double distanceX = Math.round((c_x - x)*100.0)/100.0; //Math.abs(x - c_x);
				double distanceY = Math.round((c_y - y)*100.0)/100.0; //Math.abs(y - c_y);

				if (Math.abs(distanceX) <= xx && Math.abs(distanceY) <= yy) {
					selectedBuilding = building;

					//System.out.println(" x : " + distanceX + "   y : " + distanceY);

					settlementWindow.setXCoor(distanceX);
					settlementWindow.setYCoor(distanceY);

					break;
				}
			}
		}
		return selectedBuilding;
	}



	/**
	 * Selects a construction site
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selected construction site
	 */
	// 2015-12-10 Added Construction Site selection
	public ConstructionSite selectConstructionSiteAt(int xPixel, int yPixel) {
		Point.Double clickPosition = convertToSettlementLocation(xPixel, yPixel);
		ConstructionSite site = null;

		Iterator<ConstructionSite> j = settlement.getConstructionManager().getConstructionSites().iterator();
		while (j.hasNext()) {
			ConstructionSite s = j.next();

			if (!LabelMapLayer.getConstructionLabel(s).equals(Msg.getString("LabelMapLayer.noConstruction")))  {
				double width = s.getWidth();
				double length = s.getLength();
				int facing = (int) s.getFacing();
				double x = s.getXLocation();
				double y = s.getYLocation();
				double xx = 0;
				double yy = 0;

				if (facing == 0) {
					xx = width/2D;
					yy = length/2D;
				}
				else if (facing == 90){
					yy = width/2D;
					xx = length/2D;
				}
				// Loading Dock Garage
				if (facing == 180) {
					xx = width/2D;
					yy = length/2D;
				}
				else if (facing == 270){
					yy = width/2D;
					xx = length/2D;
				}

				// Note: Both ERV Base and Starting ERV Base have 45 / 135 deg facing
				// Fortunately, they both have the same width and length
				else if (facing == 45){
					yy = width/2D;
					xx = length/2D;
				}
				else if (facing == 135){
					yy = width/2D;
					xx = length/2D;
				}

				double distanceX = Math.abs(x - clickPosition.getX());
				double distanceY = Math.abs(y - clickPosition.getY());

				if (distanceX <= xx && distanceY <= yy) {
					site = s;
					break;
				}
			}
		}

		return site;
	}

/*
	// 2015-12-10 Added returnConstructionSiteList
	public static List<ConstructionSite> returnConstructionSiteList(Settlement settlement) {

		List<ConstructionSite> result = new ArrayList<ConstructionSite>();
		if (settlement != null) {
		    Iterator<ConstructionSite> i = settlement.getBuildingManager().getBuildings().iterator();
			while (i.hasNext()) {
				ConstructionSite site = i.next();
						result.add(site);
			}
		}
		return result;
	}

	// 2015-12-10 Added returnBuildingList
	public static List<Building> returnBuildingList(Settlement settlement) {

		List<Building> result = new ArrayList<Building>();
		if (settlement != null) {
		    Iterator<Building> i = settlement.getBuildingManager().getBuildings().iterator();
			while (i.hasNext()) {
				Building building = i.next();
						result.add(building);
			}
		}
		return result;
	}
*/

	/**
	 * Selects a vehicle
	 * @param xPixel the x pixel position on the displayed map.
	 * @param yPixel the y pixel position on the displayed map.
	 * @return selectedVehicle
	 */
	// 2015-01-14 Added selectVehicleAt()
	public Vehicle selectVehicleAt(int xPixel, int yPixel) {
		Point.Double settlementPosition = convertToSettlementLocation(xPixel, yPixel);

		Vehicle selectedVehicle = null;

		Iterator<Vehicle> j = returnVehicleList(settlement).iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			double width =vehicle.getWidth(); // width is on y-axis ?
			double length = vehicle.getLength(); // length is on x-axis ?
			double newRange;

			// Select whichever longer
			if (width > length)
				newRange =  width/2.0;
			else newRange = length/2.0;

			double x = vehicle.getXLocation();
			double y = vehicle.getYLocation();

			double distanceX = x - settlementPosition.getX();
			double distanceY = y - settlementPosition.getY();
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= newRange) {
				selectedVehicle = vehicle;

				////paintDoubleBuffer();
				repaint();
			}
		}
		return selectedVehicle;
	}

	/**
	 * Selects a vehicle
	 * @param xLoc the position of the template building on the displayed map.
	 * @param yLoc the position of the template building on the displayed map.
	 * @return selectedVehicle
	 */
	// 2015-04-07 Added selectVehicleAsObstacle(). Used by TransportWizard
	public Vehicle selectVehicleAsObstacle(double xLoc, double yLoc) {

		Vehicle selectedVehicle = null;

		Iterator<Vehicle> j = returnVehicleList(settlement).iterator();
		while (j.hasNext()) {
			Vehicle vehicle = j.next();
			double width =vehicle.getWidth(); // width is on y-axis ?
			double length = vehicle.getLength(); // length is on x-axis ?
			double buildingWidth = 10;
			double buildingLength = 10;
			double newRange;

			// Select whichever longer
			if (width > length)
				newRange =  (width + buildingWidth)/2.0;
			else newRange = (length + buildingLength)/2.0;

			double x = vehicle.getXLocation();
			double y = vehicle.getYLocation();

			// distances between the center of the vehicle and the center of the building
			double distanceX = x - xLoc;
			double distanceY = y - yLoc;
			double distance = Math.hypot(distanceX, distanceY);
			if (distance <= newRange) {
				selectedVehicle = vehicle;

				////paintDoubleBuffer();
				repaint();
			}
		}
		return selectedVehicle;
	}


	// // 2015-01-14 Added returnVehicleList()
	public static List<Vehicle> returnVehicleList(Settlement settlement) {

		List<Vehicle> result = new ArrayList<Vehicle>();
		if (settlement != null) {
		    Iterator<Vehicle> i = settlement.getParkedVehicles().iterator();
			while (i.hasNext()) {
				Vehicle vehicle = i.next();
						result.add(vehicle);
			}
		}
		return result;
	}

	/**
	 * Selects a person on the map.
	 * @param person the selected person.
	 */
	public void selectPerson(Person person) {
		if ((settlement != null) && (person != null)) {
			Person currentlySelected = selectedPerson.get(settlement);
			if (person.equals(currentlySelected)) {
				selectedPerson.put(settlement, null);
			} else {
				selectedPerson.put(settlement, person);
			}

			// Repaint to refresh the label display.
			repaint();
		}
	}

	/**
	 * Get the selected person for the current settlement.
	 * @return the selected person.
	 */
	public Person getSelectedPerson() {
		Person result = null;
		if (settlement != null) {
			result = selectedPerson.get(settlement);
		}
		return result;
	}

	/**
	 * Selects a robot on the map.
	 * @param robot the selected robot.
	 */
	public void selectRobot(Robot robot) {
		if ((settlement != null) && (robot != null)) {
			Robot currentlySelected = selectedRobot.get(settlement);
			if (robot.equals(currentlySelected)) {
				selectedRobot.put(settlement, null);
			} else {
				selectedRobot.put(settlement, robot);
			}

			// Repaint to refresh the label display.
			repaint();
		}
	}

	/**
	 * Get the selected Robot for the current settlement.
	 * @return the selected Robot.
	 */
	public Robot getSelectedRobot() {
		Robot result = null;
		if (settlement != null) {
			result = selectedRobot.get(settlement);
		}
		return result;
	}

	/**
	 * Convert a pixel X,Y position to a X,Y (meter) position local to the settlement in view.
	 * @param xPixel the pixel X position.
	 * @param yPixel the pixel Y position.
	 * @return the X,Y settlement position.
	 */
	public Point.Double convertToSettlementLocation(int xPixel, int yPixel) {

		Point.Double result = new Point.Double(0D, 0D);

		double xDiff1 = (getWidth() / 2) - xPixel;
		double yDiff1 = (getHeight() / 2) - yPixel;

		double xDiff2 = xDiff1 / scale;
		double yDiff2 = yDiff1 / scale;

		// Correct due to rotation of map.
		double xDiff3 = (Math.cos(rotation) * xDiff2) + (Math.sin(rotation) * yDiff2);
		double yDiff3 = (Math.cos(rotation) * yDiff2) - (Math.sin(rotation) * xDiff2);

		double newXPos = xPos + xDiff3;
		double newYPos = yPos + yDiff3;

		result.setLocation(newXPos, newYPos);

		return result;
	}

	/**
	 * Checks if building labels should be displayed.
	 * @return true if building labels should be displayed.
	 */
	public boolean isShowBuildingLabels() {
		return showBuildingLabels;
	}

	/**
	 * Sets if building labels should be displayed.
	 * @param showLabels true if building labels should be displayed.
	 */
	public void setShowBuildingLabels(boolean showLabels) {
		this.showBuildingLabels = showLabels;
		//if (showLabels) settlementTransparentPanel.getBuildingLabelMenuItem().setState(true);
		//else settlementTransparentPanel.getBuildingLabelMenuItem().setState(false);
		//paintDoubleBuffer();
		repaint();
	}

	/**
	 * Checks if construction site labels should be displayed.
	 * @return true if construction site labels should be displayed.
	 */
	public boolean isShowConstructionLabels() {
		return showConstructionLabels;
	}

	/**
	 * Sets if construction site labels should be displayed.
	 * @param showLabels true if construction site labels should be displayed.
	 */
	public void setShowConstructionLabels(boolean showLabels) {
		this.showConstructionLabels = showLabels;
		//if (showLabels) settlementTransparentPanel.getConstructionLabelMenuItem().setState(true);
		//else settlementTransparentPanel.getConstructionLabelMenuItem().setState(false);
		//paintDoubleBuffer();
		repaint();
	}

	/**
	 * Checks if person labels should be displayed.
	 * @return true if person labels should be displayed.
	 */
	public boolean isShowPersonLabels() {
		return showPersonLabels;
	}

	/**
	 * Sets if person labels should be displayed.
	 * @param showLabels true if person labels should be displayed.
	 */
	public void setShowPersonLabels(boolean showLabels) {
		this.showPersonLabels = showLabels;
		//if (showLabels) settlementTransparentPanel.getPersonLabelMenuItem().setState(true);
		//else settlementTransparentPanel.getPersonLabelMenuItem().setState(false);
		//paintDoubleBuffer();
		repaint();
	}

	/**
	 * Checks if Robot labels should be displayed.
	 * @return true if Robot labels should be displayed.
	 */
	public boolean isShowRobotLabels() {
		return showRobotLabels;
	}

	/**
	 * Sets if Robot labels should be displayed.
	 * @param showLabels true if Robot labels should be displayed.
	 */
	public void setShowRobotLabels(boolean showLabels) {
		this.showRobotLabels = showLabels;
		//if (showLabels) settlementTransparentPanel.getRobotLabelMenuItem().setState(true);
		//else settlementTransparentPanel.getRobotLabelMenuItem().setState(false);
		//paintDoubleBuffer();
		repaint();
	}
	/**
	 * Checks if vehicle labels should be displayed.
	 * @return true if vehicle labels should be displayed.
	 */
	public boolean isShowVehicleLabels() {
		return showVehicleLabels;
	}

	/**
	 * Sets if vehicle labels should be displayed.
	 * @param showLabels true if vehicle labels should be displayed.
	 */
	public void setShowVehicleLabels(boolean showLabels) {
		this.showVehicleLabels = showLabels;
		//if (showLabels) settlementTransparentPanel.getVehicleLabelMenuItem().setState(true);
		//else settlementTransparentPanel.getVehicleLabelMenuItem().setState(false);
		//paintDoubleBuffer();
		repaint();
	}


	/**
	 * Checks if DaylightLayer should be displayed.
	 * @return true if DaylightLayer should be displayed.
	 */
	public boolean isDaylightTrackingOn() {
		return showDaylightLayer;
	}

	/**
	 * Sets if DayNightLayershould be displayed.
	 * @param showDayNightLayer true if DayNightLayer should be displayed.
	 */
	public void setShowDayNightLayer(boolean showDayNightLayer) {
		this.showDaylightLayer = showDayNightLayer;

		////paintDoubleBuffer();
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;

//				long startTime = System.nanoTime();

		// Set graphics rendering hints.
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// Display all map layers.
		Iterator<SettlementMapLayer> i = mapLayers.iterator();
		while (i.hasNext()) {
			// 2014-11-04 Added building parameter
			i.next().displayLayer(g2d, settlement, building, xPos, yPos, getWidth(), getHeight(), rotation, scale);
		}

//				long endTime = System.nanoTime();
//				double timeDiff = (endTime - startTime) / 1000000D;
//				System.out.println("SMT paint time: " + (int) timeDiff + " ms");


	}


	@Override
	public void clockPulse(double time) {
		timeCache = timeCache + time;
		if (timeCache > PERIOD_IN_MILLISOLS) {
			//System.out.println("calling SettlementMapPanel's clockPulse()");
			// Repaint map panel
			repaint();
			timeCache = 0;
		}
	}

	@Override
	public void pauseChange(boolean isPaused) {
		// Do nothing
	}

	public SettlementTransparentPanel getSettlementTransparentPanel() {
		return settlementTransparentPanel;
	}

	/**
	 * Cleans up the map panel for disposal.
	 */
	public void destroy() {
		// Remove clock listener.
		Simulation.instance().getMasterClock().removeClockListener(this);

		menu = null;
		settlement = null;
		selectedPerson = null;
		building = null;
		settlementWindow = null;
		// Destroy all map layers.
		Iterator<SettlementMapLayer> i = mapLayers.iterator();
		while (i.hasNext()) {
			i.next().destroy();
		}

		dbg = null;
		dbImage = null;
	}
}