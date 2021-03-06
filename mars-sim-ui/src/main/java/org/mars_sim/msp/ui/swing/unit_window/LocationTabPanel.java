/**
 * Mars Simulation Project
 * LocationTabPanel.java
 * @version 3.1.0 2017-02-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing.unit_window;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.location.LocationStateType;
import org.mars_sim.msp.core.mars.TerrainElevation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.steelseries.gauges.DigitialRadial;
import org.mars_sim.msp.ui.steelseries.gauges.DisplayCircular;
import org.mars_sim.msp.ui.steelseries.gauges.DisplaySingle;
import org.mars_sim.msp.ui.steelseries.tools.BackgroundColor;
import org.mars_sim.msp.ui.steelseries.tools.FrameDesign;
import org.mars_sim.msp.ui.steelseries.tools.LcdColor;
import org.mars_sim.msp.ui.steelseries.tools.Orientation;
import org.mars_sim.msp.ui.steelseries.tools.PointerType;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;

import javafx.application.Platform;

/**
 * The LocationTabPanel is a tab panel for location information.
 */
public class LocationTabPanel
extends TabPanel
implements ActionListener {

	/** default serial id. */
	private static final long serialVersionUID = 12L;

	 /** default logger.   */
	//private static Logger logger = Logger.getLogger(LocationTabPanel.class.getName());
	private static final String N = "N";
	private static final String S = "S";
	private static final String E = "E";
	private static final String W = "W";
	private static final String IN = " in ";
	private static final String AT = " at ";
	private static final String INSIDE = " inside ";
	private static final String WITHIN_THE_VINCINITY_OF = " Within the vicinity of ";
	private static final String PARKED_AT = " Parked at ";
	private static final String PARKED = " Parked";
	//private static final String ON_A_MISSION_OUTSIDE = " on a mission outside";
	private static final String OUTSIDE_ON_THE_SURFACE_OF_MARS = " outside on the surface of Mars";
	private static final String STEPPED = " Stepped";
	private static final String STORED = "Stored";

	private static final String OUTSIDE_ON_A_MISSION = " outside on a mission ";
	private static final String GONE = " gone ";
	private static final String DECOMMISSIONED = " decommmissed";
	private static final String DEAD = "Dead";
	private static final String BURIED = "Buried";

	private int themeCache;

	private double elevationCache;

	//private String locationText = "Mars";

	// 2014-11-11 Added new panels and labels
	private JPanel tpPanel =  new JPanel();
	private JPanel outsideReadingPanel = new JPanel();
	private JPanel containerPanel = new JPanel();
	//private JLabel temperatureLabel;
	//private JLabel airPressureLabel;
	private JLabel locationLabel;
	private JLabel locLabel;
	//private Color THEME_COLOR = Color.ORANGE;
	//private double airPressureCache;
	//private int temperatureCache;
	private Unit containerCache, topContainerCache;

	private JPanel coordsPanel;
	private JLabel latitudeLabel;
	private JLabel longitudeLabel;
	private JPanel centerPanel;
	//private JButton locationButton;

	private TerrainElevation terrainElevation;
	private Coordinates locationCache;
	private MainScene mainScene;

	private JButton locatorButton;

	private DisplaySingle lcdLong, lcdLat, lcdText; // lcdElev,
	private DisplayCircular gauge;//RadialQuarterN gauge;

	DecimalFormat fmt = new DecimalFormat("##0");
	DecimalFormat fmt2 = new DecimalFormat("#0.00");
    /**
     * Constructor.
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public LocationTabPanel(Unit unit, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(Msg.getString("LocationTabPanel.title"),
        		null,
        		Msg.getString("LocationTabPanel.tooltip"), unit, desktop);

    	if (terrainElevation == null)
			terrainElevation = Simulation.instance().getMars().getSurfaceFeatures().getTerrainElevation();

    	mainScene = desktop.getMainScene();

        // Initialize location header.
		JPanel titlePane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(titlePane);

		JLabel titleLabel = new JLabel(Msg.getString("LocationTabPanel.title"), JLabel.CENTER); //$NON-NLS-1$
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
		titlePane.add(titleLabel);

        // Create location panel
        JPanel locationPanel = new JPanel(new BorderLayout(5, 5));//new GridLayout(2,1,0,0));//new FlowLayout(FlowLayout.CENTER));// new BorderLayout(0,0));
        locationPanel.setBorder(new MarsPanelBorder());
        locationPanel.setBorder(new EmptyBorder(1, 1, 1, 1) );
        topContentPanel.add(locationPanel);


        // Initialize location cache
        locationCache = new Coordinates(unit.getCoordinates());
        themeCache = mainScene.getTheme();

        String dir_N_S = null;
        String dir_E_W = null;
        if (locationCache.getLatitudeDouble() >= 0)
        	dir_N_S = Msg.getString("direction.degreeSign")+"N";
        else
        	dir_N_S = Msg.getString("direction.degreeSign")+"S";

        if (locationCache.getLongitudeDouble() >= 0)
        	dir_E_W = Msg.getString("direction.degreeSign")+"E";
        else
        	dir_E_W = Msg.getString("direction.degreeSign")+"W";


        JPanel northPanel = new JPanel(new FlowLayout());
        locationPanel.add(northPanel, BorderLayout.NORTH);

        lcdLat = new DisplaySingle();
        lcdLat.setLcdUnitString(dir_N_S);
        lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
        lcdLat.setLcdInfoString("Latitude");
        //lcd1.setLcdColor(LcdColor.BLUELIGHTBLUE_LCD);
        lcdLat.setLcdColor(LcdColor.BEIGE_LCD);
        //lcdLat.setBackground(BackgroundColor.NOISY_PLASTIC);
        lcdLat.setGlowColor(Color.orange);
        //lcd1.setBorder(new EmptyBorder(5, 5, 5, 5));
        lcdLat.setDigitalFont(true);
        lcdLat.setLcdDecimals(2);
        lcdLat.setSize(new Dimension(150, 45));
        lcdLat.setMaximumSize(new Dimension(150, 45));
        lcdLat.setPreferredSize(new Dimension(150, 45));
        lcdLat.setVisible(true);
        //locationPanel.add(lcdLat, BorderLayout.WEST);
        northPanel.add(lcdLat);

        elevationCache = terrainElevation.getElevation(unit.getCoordinates());
 /*
        //System.out.println("elevation is "+ elevation);
        lcdElev = new DisplaySingle();
        lcdElev.setLcdValueFont(new Font("Serif", Font.ITALIC, 12));
        lcdElev.setLcdUnitString("km");
        lcdElev.setLcdValueAnimated(elevationCache);
        lcdElev.setLcdDecimals(3);
        lcdElev.setLcdInfoString("Elevation");
        //lcd0.setLcdColor(LcdColor.DARKBLUE_LCD);
        lcdElev.setLcdColor(LcdColor.YELLOW_LCD);//REDDARKRED_LCD);
        lcdElev.setDigitalFont(true);
        //lcd0.setBorder(new EmptyBorder(5, 5, 5, 5));
        lcdElev.setSize(new Dimension(150, 60));
        lcdElev.setMaximumSize(new Dimension(150, 60));
        lcdElev.setPreferredSize(new Dimension(150, 60));
        lcdElev.setVisible(true);
        locationPanel.add(lcdElev, BorderLayout.NORTH);
 */

        // Create center map button
        locatorButton = new JButton(ImageLoader.getIcon("locator48_orange"));
        //centerMapButton = new JButton(ImageLoader.getIcon("locator_blue"));
        locatorButton.setBorder(new EmptyBorder(1, 1, 1, 1) );
        locatorButton.addActionListener(this);
        locatorButton.setOpaque(false);
        locatorButton.setToolTipText("Locate the unit on Mars Navigator");
        locatorButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));//new Cursor(Cursor.HAND_CURSOR));

		JPanel locatorPane = new JPanel(new FlowLayout());
		locatorPane.add(locatorButton);
		//locationPanel.add(locatorPane, BorderLayout.NORTH);
	    northPanel.add(locatorPane);

        lcdLong = new DisplaySingle();
        //lcdLong.setCustomLcdForeground(getForeground());
        lcdLong.setLcdUnitString(dir_E_W);
        lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));
        lcdLong.setLcdInfoString("Longitude");
        //lcd2.setLcdColor(LcdColor.BLUELIGHTBLUE_LCD);
        lcdLong.setLcdColor(LcdColor.BEIGE_LCD);
        //setBackgroundColor(BackgroundColor.LINEN);
        lcdLong.setGlowColor(Color.yellow);
        lcdLong.setDigitalFont(true);
        lcdLong.setLcdDecimals(2);
        lcdLong.setSize(new Dimension(150, 45));
        lcdLong.setMaximumSize(new Dimension(150, 45));
        lcdLong.setPreferredSize(new Dimension(150,45));
        lcdLong.setVisible(true);
       //locationPanel.add(lcdLong, BorderLayout.EAST);
        northPanel.add(lcdLong);


        int max = -1;
        int min = 2;
        // Note: The peak of Olympus Mons is 21,229 meters (69,649 feet) above the Mars areoid (a reference datum similar to Earth's sea level). The lowest point is within the Hellas Impact Crater (marked by a flag with the letter "L").
        // The lowest point in the Hellas Impact Crater is 8,200 meters (26,902 feet) below the Mars areoid.
        if (elevationCache < -8) {
        	max = -8;
        	min = -9;
        }
        else if (elevationCache < -5) {
        	max = -5;
        	min = -9;
        }
        else if (elevationCache < -3) {
        	max = -3;
        	min = -5;
        }
        else if (elevationCache < 0) {
        	max = 1;
        	min = -1;
        }
        else if (elevationCache < 1) {
        	max = 2;
        	min = 0;
        }
        else if (elevationCache < 3) {
        	max = 5;
        	min = 0;
        }
        else if (elevationCache < 10){
        	max = 10;
        	min = 5;
        }
        else if (elevationCache < 20){
        	max = 20;
        	min = 10;
        }
        else if (elevationCache < 30){
        	max = 30;
        	min = 20;
        }

        gauge = new DisplayCircular();
        setGauge(gauge, min, max);
        locationPanel.add(gauge, BorderLayout.CENTER);

		//centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));//GridLayout(2,1,0,0)); // new BorderLayout())
/*
		// 2015-12-09 Prepare loc label
        locLabel = new JLabel();
        locLabel.setFont(font);
        //locLabel.setOpaque(false);
        locLabel.setFont(new Font("Serif", Font.PLAIN, 13));
        locLabel.setHorizontalAlignment(SwingConstants.CENTER);
 */

        String loc = "On the surface of Mars";
		lcdText = new DisplaySingle();
        lcdText.setLcdInfoString("Last Unknown Position");
        //lcdText.setLcdColor(LcdColor.REDDARKRED_LCD);
        lcdText.setGlowColor(Color.ORANGE);
        //lcdText.setBackground(Background.SATIN_GRAY);
        lcdText.setDigitalFont(true);
        lcdText.setSize(new Dimension(150, 30));
        lcdText.setMaximumSize(new Dimension(150, 30));
        lcdText.setPreferredSize(new Dimension(150,30));
        lcdText.setVisible(true);
        lcdText.setLcdNumericValues(false);
        lcdText.setLcdValueFont(new Font("Serif", Font.ITALIC, 8));
        //lcdText.setLcdText(locationText);
        lcdText.setLcdText(loc);
        lcdText.setLcdTextScrolling(true);
        //centerPanel.add(lcdText);
		//locationPanel.add(centerPanel, BorderLayout.SOUTH);
		locationPanel.add(lcdText, BorderLayout.SOUTH);

		updateLocation();

		checkTheme(true);

    }

    public void checkTheme(boolean firstRun) {
        if (mainScene != null) {
            int theme = mainScene.getTheme();

            if (themeCache != theme || firstRun) {
            	themeCache = theme;

	        	if (theme == 7) {
	                lcdText.setLcdColor(LcdColor.REDDARKRED_LCD);
	                gauge.setFrameDesign(FrameDesign.GOLD);
	                locatorButton.setIcon(ImageLoader.getIcon("locator48_orange"));
	        	}
	        	else if (theme == 6) {
	        		lcdText.setLcdColor(LcdColor.DARKBLUE_LCD);
	        		gauge.setFrameDesign(FrameDesign.STEEL);
	        		locatorButton.setIcon(ImageLoader.getIcon("locator48_blue"));
	        	}
            }
        }
    }

    public void setGauge(DisplayCircular gauge, int min, int max) {
        gauge.setDisplayMulti(false);
    	gauge.setDigitalFont(true);
        //gauge.setFrameDesign(FrameDesign.GOLD);
        //gauge.setOrientation(Orientation.EAST);//.NORTH);//.VERTICAL);
        //gauge.setPointerType(PointerType.TYPE5);
        //gauge.setTextureColor(Color.yellow);//, Texture_Color BRUSHED_METAL and PUNCHED_SHEET);
        gauge.setUnitString("km");
        gauge.setTitle("Elevation");
        //gauge.setMinValue(min);
        //gauge.setMaxValue(max);
        //gauge.setTicklabelsVisible(true);
        //gauge.setMaxNoOfMajorTicks(10);
        //gauge.setMaxNoOfMinorTicks(10);
        gauge.setBackgroundColor(BackgroundColor.NOISY_PLASTIC);//.BRUSHED_METAL);
        //alt.setGlowColor(Color.yellow);
        //gauge.setLcdColor(LcdColor.BEIGE_LCD);//.BLACK_LCD);
        //gauge.setLcdInfoString("Elevation");
        //gauge.setLcdUnitString("km");
        gauge.setLcdValueAnimated(elevationCache);
        gauge.setValueAnimated(elevationCache);
        //gauge.setValue(elevationCache);
        gauge.setLcdDecimals(3);

        //alt.setMajorTickmarkType(TICKMARK_TYPE);
        //gauge.setSize(new Dimension(250, 250));
        //gauge.setMaximumSize(new Dimension(250, 250));
        //gauge.setPreferredSize(new Dimension(250, 250));

        gauge.setSize(new Dimension(250, 250));
        gauge.setMaximumSize(new Dimension(250, 250));
        gauge.setPreferredSize(new Dimension(250, 250));

        gauge.setVisible(true);

    }

	private String getLatitudeString() {
		return locationCache.getFormattedLatitudeString();
	}

	private String getLongitudeString() {
		return locationCache.getFormattedLongitudeString();
	}


    /**
     * Action event occurs.
     *
     * @param event the action event
     */
    @SuppressWarnings("restriction")
	public void actionPerformed(ActionEvent event) {
        JComponent source = (JComponent) event.getSource();

        // If the center map button was pressed, update navigator tool.
        if (source == locatorButton) {
        	// 2015-12-19 Added codes to open the settlement map tool and center the map to
        	// show the exact/building location inside a settlement if possible
        	Person p = null;
        	Robot r = null;
        	Vehicle v = null;
        	Equipment e = null;

        	if (unit instanceof Person) {
        		p = (Person) unit;
    		    SettlementMapPanel mapPanel = desktop.getSettlementWindow().getMapPanel();

        		if (p.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

        			if (mainScene != null) {
        				mainScene.setSettlement(p.getSettlement());
        			}
        			else {
            			desktop.openToolWindow(SettlementWindow.NAME);
            			if (mainScene != null)
            				mainScene.setSettlement(p.getSettlement());
            			else
            				mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(p.getSettlement());
        			}

        			Building b = p.getBuildingLocation();
        			double xLoc = b.getXLocation();
        			double yLoc = b.getYLocation();
        			double scale = mapPanel.getScale();
        			mapPanel.reCenter();
        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
        			mapPanel.setShowBuildingLabels(true);

        			mapPanel.selectPerson(p);
            	}
        		else if (p.getLocationSituation() == LocationSituation.IN_VEHICLE) {

        			Vehicle vv = p.getVehicle();
        			if (vv.getSettlement() == null) {
        				// out there on a mission
        				desktop.centerMapGlobe(p.getCoordinates());
        			}
        			else {
        				// still parked inside a garage or within the premise of a settlement
	        			desktop.openToolWindow(SettlementWindow.NAME);
	        			//System.out.println("Just open Settlement Map Tool");

	        			if (mainScene != null)
	        				mainScene.setSettlement(vv.getSettlement());
	        			else
	        				mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(vv.getSettlement());

	        			double xLoc = vv.getXLocation();
	        			double yLoc = vv.getYLocation();
	        			double scale = mapPanel.getScale();
	        			mapPanel.reCenter();
	        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
	        			mapPanel.setShowVehicleLabels(true);

	        			mapPanel.selectPerson(p);

	        		}
            	}
        		else if (p.getLocationSituation() == LocationSituation.OUTSIDE) {
        			Vehicle vv = p.getVehicle();

        			if (vv == null) {

               			desktop.openToolWindow(SettlementWindow.NAME);
            			//System.out.println("Just open Settlement Map Tool");

               			// TODO: Case 1 : person is on a mission on the surface of Mars and just happens to step outside the vehicle temporarily

               			// TODO: Case 2 : person just happens to step outside the settlement at its vicinity temporarily
/*
            			if (mainScene != null)
            				mainScene.setSettlement(p.getSettlement());
            			else
            				mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(p.getSettlement());


        				double xLoc = p.getXLocation();
            			double yLoc = p.getYLocation();
            			double scale = mapPanel.getScale();
            			mapPanel.reCenter();
            			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
            			mapPanel.setShowBuildingLabels(true);

            			mapPanel.selectPerson(p);
*/
        			}
        			else
        				// he's stepped outside a vehicle
        				desktop.centerMapGlobe(p.getCoordinates());
        		}

        	} else if (unit instanceof Robot) {
        		r = (Robot) unit;
        		SettlementMapPanel mapPanel = desktop.getSettlementWindow().getMapPanel();

        		if (r.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
        			desktop.openToolWindow(SettlementWindow.NAME);
        			//System.out.println("Just open Settlement Map Tool");
        			if (mainScene != null)
        				mainScene.setSettlement(r.getSettlement());
        			else
        				mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(r.getSettlement());

        			Building b = r.getBuildingLocation();
        			double xLoc = b.getXLocation();
        			double yLoc = b.getYLocation();
        			double scale = mapPanel.getScale();
        			mapPanel.reCenter();
        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
        			mapPanel.setShowBuildingLabels(true);

        			mapPanel.selectRobot(r);
            	}
        		else if (r.getLocationSituation() == LocationSituation.IN_VEHICLE) {

        			Vehicle vv = r.getVehicle();
        			if (vv.getSettlement() == null) {
        				// out there on a mission
        				desktop.centerMapGlobe(r.getCoordinates());
        			}
        			else {
        				// still parked inside a garage or within the premise of a settlement
	        			desktop.openToolWindow(SettlementWindow.NAME);
	        			//System.out.println("Just open Settlement Map Tool");
	        			if (mainScene != null)
	        				mainScene.setSettlement(vv.getSettlement());
	        			else
	        				mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(vv.getSettlement());


	        			double xLoc = vv.getXLocation();
	        			double yLoc = vv.getYLocation();
	        			double scale = mapPanel.getScale();
	        			mapPanel.reCenter();
	        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
	        			mapPanel.setShowVehicleLabels(true);

	        			mapPanel.selectRobot(r);

	        		}
            	}
        		else if (r.getLocationSituation() == LocationSituation.OUTSIDE) {
        			Vehicle vv = r.getVehicle();

        			if (vv == null) {
        				// he's stepped outside the settlement temporally
               			desktop.openToolWindow(SettlementWindow.NAME);
            			//System.out.println("Just open Settlement Map Tool");


               			// TODO: Case 1 : robot is on a mission on the surface of Mars and just happens to step outside the vehicle temporarily

               			// TODO: Case 2 : robot just happens to step outside the settlement at its vicinity temporarily
/*
        				double xLoc = r.getXLocation();
            			double yLoc = r.getYLocation();
            			double scale = mapPanel.getScale();
            			mapPanel.reCenter();
            			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
            			mapPanel.setShowBuildingLabels(true);

            			mapPanel.selectRobot(r);
*/
        			}
        			else
        				// he's stepped outside a vehicle
        				desktop.centerMapGlobe(r.getCoordinates());
        		}

        	} else if (unit instanceof Vehicle) {
        		v = (Vehicle) unit;

    		    SettlementMapPanel mapPanel = desktop.getSettlementWindow().getMapPanel();

          		if (v.getSettlement() != null) {
        			desktop.openToolWindow(SettlementWindow.NAME);
        			//System.out.println("Just open Settlement Map Tool");
        			if (mainScene != null)
        				mainScene.setSettlement(v.getSettlement());
        			else
        				mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(v.getSettlement());


        			double xLoc = v.getXLocation();
        			double yLoc = v.getYLocation();
        			double scale = mapPanel.getScale();
        			mapPanel.reCenter();
        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);

        			mapPanel.setShowVehicleLabels(true);

        			//mapPanel.selectVehicleAt((int)xLoc, (int)yLoc);
            	}
        		else
        			desktop.centerMapGlobe(unit.getCoordinates());

	    	} else if (unit instanceof Equipment) {
	    		e = (Equipment) unit;
	    		SettlementMapPanel mapPanel = desktop.getSettlementWindow().getMapPanel();

	    		if (e.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	    			desktop.openToolWindow(SettlementWindow.NAME);

	    			if (mainScene != null)
	    				mainScene.setSettlement(e.getSettlement());
	    			else
	    				mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(e.getSettlement());

	    			//System.out.println("Just open Settlement Map Tool");

/*
	    			Building b = e.getBuildingLocation();
	    			double xLoc = b.getXLocation();
	    			double yLoc = b.getYLocation();
	    			double scale = mapPanel.getScale();
	    			mapPanel.reCenter();
	    			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
	    			mapPanel.setShowBuildingLabels(true);

	    			mapPanel.selectRobot(r);
 */
	        	}

	    		else if (e.getLocationSituation() == LocationSituation.IN_VEHICLE) {

	    			Vehicle vv = e.getVehicle();
	    			if (vv.getSettlement() == null) {
	    				// out there on a mission
	    				desktop.centerMapGlobe(e.getCoordinates());
	    			}
	    			else {
	    				// still parked inside a garage or within the premise of a settlement
	        			desktop.openToolWindow(SettlementWindow.NAME);
	        			//System.out.println("Just open Settlement Map Tool");
	        			if (mainScene != null)
	        				mainScene.setSettlement(vv.getSettlement());
	        			else
	        				mapPanel.getSettlementTransparentPanel().getSettlementListBox().setSelectedItem(vv.getSettlement());

	        			double xLoc = vv.getXLocation();
	        			double yLoc = vv.getYLocation();
	        			double scale = mapPanel.getScale();
	        			mapPanel.reCenter();
	        			mapPanel.moveCenter(xLoc*scale, yLoc*scale);
	        			mapPanel.setShowVehicleLabels(true);

	        			//mapPanel.selectVehicleAt((int)xLoc, (int)yLoc);

	        		}
	        	}

	    		else if (e.getLocationSituation() == LocationSituation.OUTSIDE) {

	    		}

	    		else
	    			desktop.centerMapGlobe(e.getCoordinates());

	    	}
        }

        // If the location button was pressed, open the unit window.
        //if (source == locationButton)
        //    desktop.openUnitWindow(unit.getContainerUnit(), false);
    }

    /**
     * Updates the info on this panel.
     */
    // 2014-11-11 Overhauled update()
    public void update() {

        // If unit's location has changed, update location display.
    	// TODO: if a person goes outside the settlement for servicing an equipment
    	// does the coordinate (down to how many decimal) change?
    	Coordinates location = unit.getCoordinates();
        if (!locationCache.equals(location)) {
            locationCache.setCoords(location);

            String dir_N_S = null;
            String dir_E_W = null;

            if (locationCache.getLatitudeDouble() >= 0)
            	dir_N_S = Msg.getString("direction.degreeSign") + N;
            else
            	dir_N_S = Msg.getString("direction.degreeSign") + S;

            if (locationCache.getLongitudeDouble() >= 0)
            	dir_E_W = Msg.getString("direction.degreeSign") + E;
            else
            	dir_E_W = Msg.getString("direction.degreeSign") + W;

            lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
            lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));

            lcdLat.setLcdValueAnimated(Math.abs(locationCache.getLatitudeDouble()));
            lcdLong.setLcdValueAnimated(Math.abs(locationCache.getLongitudeDouble()));

            double elevation = terrainElevation.getElevation(location);

            if (elevationCache != elevation) {
            	elevationCache = elevation;

                int max = 0;
                int min = 0;
                if (elevationCache < -8) {
                	max = -8;
                	min = -9;
                }
                else if (elevationCache < -5) {
                	max = -5;
                	min = -9;
                }
                else if (elevationCache < -3) {
                	max = -3;
                	min = -5;
                }
                else if (elevationCache < 0) {
                	max = 1;
                	min = -1;
                }
                else if (elevationCache < 1) {
                	max = 2;
                	min = 0;
                }
                else if (elevationCache < 3) {
                	max = 5;
                	min = 0;
                }
                else if (elevationCache < 10){
                	max = 10;
                	min = 5;
                }
                else if (elevationCache < 20){
                	max = 20;
                	min = 10;
                }
                else if (elevationCache < 30){
                	max = 30;
                	min = 20;
                }

                setGauge(gauge, min, max);

            }
        }

        // 2015-12-09 Prepare loc label
        // Update location button or location text label as necessary.
        Unit container = unit.getContainerUnit();
        if (containerCache != container) {
        	containerCache = container;
        	updateLocation();
        }

        Unit topContainer = unit.getTopContainerUnit();
        if (topContainerCache != topContainer) {
        	topContainerCache = topContainer;
        	updateLocation();
        }

        checkTheme(false);

    }

    /**
     * Tracks the location of a person/bot/vehicle/object
     */
    // 2015-12-09 Added updateLocation()
    public void updateLocation() {

    	String loc = null;

    	if (unit instanceof Person) {
    		Person p = (Person) unit;

    		if (p.isDead()) {

    			if (p.isBuried())
    				loc = BURIED + AT + p.getBuriedSettlement().getName();
    			else {
    				loc = DEAD + IN + p.getContainerUnit().getName();
    			}
    		}

    		else {

	    		if (p.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	    			if (p.getBuildingLocation() != null)
		    			// case A1
	    				loc = AT + p.getBuildingLocation().getNickName() + IN + topContainerCache;
	    			else
		    			// case A2
	    				loc = IN + topContainerCache;
	    		}

	    		else if (p.getLocationSituation() == LocationSituation.IN_VEHICLE) {
	    			Vehicle vehicle = (Vehicle) unit.getContainerUnit();
	    			if (vehicle.getSettlement() != null) {
	    				Building building = BuildingManager.getBuilding(vehicle);

	    				if (building == null) {
	            			Settlement settlement = (Settlement) vehicle.getContainerUnit();
	            			//System.out.println(p + " is in " + settlement.getName());

	    					//loc = " in a vehicle parked within the premise of a settlement";
	            			if (settlement != null)
		            			// case D
	            				loc = IN + containerCache + PARKED + WITHIN_THE_VINCINITY_OF + settlement;
	    				}
	    				else {
	        				// vehicle.getSettlement() <==> getTopContainerUnit()
	         				// e.g. " in LUV1 inside Garage 1 in Alpha Base;
	        				// vehicle = containerCache
	    					if (p.getBuildingLocation() != null)
		    					// case C1 : the person/vehicle is stil inside a building.  
	    						loc = IN + vehicle + INSIDE + p.getBuildingLocation() + IN + vehicle.getSettlement();
	    					else
		    					// case C2 : the person/vehicle is outside of a building.
	    						loc = IN + vehicle + IN + vehicle.getSettlement();

	    				}

	    			} else {
	        			// case E
	        			loc = IN + containerCache + GONE + OUTSIDE_ON_A_MISSION;
	    			}
	    		}

	    		else if (p.getLocationSituation() == LocationSituation.BURIED)
	    			// not possible
	    			loc = BURIED + AT + p.getBuriedSettlement().getName();

	    		if (p.getLocationStateType() == LocationStateType.SETTLEMENT_VICINITY) {//.getName().equals("Within a settlement's vicinity")) {
					loc = WITHIN_THE_VINCINITY_OF + topContainerCache;
				}

				else if (p.getLocationStateType() == LocationStateType.OUTSIDE_ON_MARS) {//.getName().equals("Outside on the surface of Mars")) {
					// case F
					loc = STEPPED + OUTSIDE_ON_THE_SURFACE_OF_MARS;
				}
    		}

    	}

    	else if (unit instanceof Robot) {
    		Robot r = (Robot) unit;

    		if (r.isSalvaged())
    			loc = " is Salvaged";

    		else if (r.getSystemCondition().isInoperable())
    			loc = " is Inoperable";

    		else {

	    		if (r.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	    			// case A
	    			Building b = r.getBuildingLocation();
	    			if (b != null)
	    				loc = AT + b.getNickName() + IN + topContainerCache;
	    		}

	    		else if (r.getLocationSituation() == LocationSituation.IN_VEHICLE) {
	     			if (r.getSettlement() != null)
	    				// case C
	           			loc = IN + containerCache + INSIDE + r.getVehicle().getBuildingLocation();// " inside a garage";
	    			else {
	         			Vehicle vehicle = (Vehicle) unit.getContainerUnit();
/*
	         			// Note: a vehicle's container unit may be null if it's outside a settlement
	        			Settlement settlement = (Settlement) vehicle.getContainerUnit();

	        			if (settlement == null)
	        				// case E
	           				loc = IN + containerCache + OUTSIDE_ON_A_MISSION;
	    				else
	    					// case D
	    					//loc = " in a vehicle parked within the premise of a settlement";
	    					loc = IN + containerCache + PARKED + WITHIN_THE_VINCINITY_OF + settlement;
*/	        			
	        			
		    			if (vehicle.getSettlement() != null) {
		    				Building building = BuildingManager.getBuilding(vehicle);

		    				if (building == null) {
		            			Settlement settlement = (Settlement) vehicle.getContainerUnit();
		            			//System.out.println(p + " is in " + settlement.getName());

		    					//loc = " in a vehicle parked within the premise of a settlement";
		            			if (settlement != null)
			            			// case E
		            				loc = IN + containerCache + PARKED + WITHIN_THE_VINCINITY_OF + settlement;
		    				}
		    				
		    				else {
		        				// vehicle.getSettlement() <==> getTopContainerUnit()
		         				// e.g. " in LUV1 inside Garage 1 in Alpha Base;
		        				// vehicle = containerCache
		    					if (r.getBuildingLocation() != null)
			    					// case D1 : the person/vehicle is stil inside a building.  
		    						loc = IN + vehicle + INSIDE + r.getBuildingLocation() + IN + vehicle.getSettlement();
		    					else
			    					// case D2 : the person/vehicle is outside of a building.
		    						loc = IN + vehicle + IN + vehicle.getSettlement();
		    				}

		    			} else {
		        			// case E
		        			loc = IN + containerCache + GONE + OUTSIDE_ON_A_MISSION;
		    			}
	        			
	        			
	    			}
	     			
	     			
	    		}

	    		else if (r.getLocationSituation() == LocationSituation.OUTSIDE) {

	    			if (r.getLocationStateType() == LocationStateType.SETTLEMENT_VICINITY) {//.getName().equals("Within a settlement's vicinity")) {
	    				loc = WITHIN_THE_VINCINITY_OF + topContainerCache;
	    			}

	    			else if (r.getLocationStateType() == LocationStateType.OUTSIDE_ON_MARS) {//.getName().equals("Outside on the surface of Mars")) {
	    				// case F
	    				loc = STEPPED + OUTSIDE_ON_THE_SURFACE_OF_MARS;
	    			}
	    		}

	    		else if (r.getLocationSituation() == LocationSituation.BURIED)
	    			loc = DECOMMISSIONED;// " decommmissed";// + r.getBuriedSettlement().getName();

	    		else if (r.getLocationSituation() == LocationSituation.IN_VEHICLE) {
	     			if (r.getSettlement() != null)
	    				// case C
	           			loc = IN + containerCache + INSIDE + r.getVehicle().getBuildingLocation();// " inside a garage";
	    			else {
	         			Vehicle vehicle = (Vehicle) unit.getContainerUnit();
	        	     	// Note: a vehicle's container unit may be null if it's outside a settlement
	        			Settlement settlement = (Settlement) vehicle.getContainerUnit();
	           			//System.out.println(r.getName() + " is in " + settlement.getName());

	    				if (settlement == null)
	        				// case E
	           				loc = IN + containerCache + OUTSIDE_ON_A_MISSION;
	    				else
	    					// case D
	    					//loc = " in a vehicle parked within the premise of a settlement";
	    					loc = IN + containerCache + PARKED + WITHIN_THE_VINCINITY_OF + settlement;
	    			}
	    		}

    		}
    	}

    	else if (unit instanceof Equipment) {
    		Equipment e = (Equipment) unit;

    		if (e.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
    			// case A
    			//loc = AT + e.getBuildingLocation().getNickName() + IN + topContainerCache;
    			loc = STORED + AT + topContainerCache;
    		}

       		else if (e.getLocationSituation() == LocationSituation.IN_VEHICLE) {
     			if (e.getSettlement() != null)
    				// case C
           			loc = IN + containerCache + INSIDE + e.getVehicle().getBuildingLocation();// " inside a garage";
    			else {
         			Vehicle vehicle = (Vehicle) unit.getContainerUnit();
        	     	// Note: a vehicle's container unit may be null if it's outside a settlement
        			Settlement settlement = (Settlement) vehicle.getContainerUnit();

    				if (settlement == null)
        				// case E
           				loc = IN + containerCache + OUTSIDE_ON_A_MISSION;
    				else
    					// case D
    					//loc = " in a vehicle parked within the premise of a settlement";
    					loc = IN + containerCache + PARKED + WITHIN_THE_VINCINITY_OF + settlement;
    			}
    		}

    		else if (e.getLocationSituation() == LocationSituation.OUTSIDE) {

    			if (e.getLocationStateType() == LocationStateType.SETTLEMENT_VICINITY) {//.getName().equals("Within a settlement's vicinity")) {
    				loc = WITHIN_THE_VINCINITY_OF + topContainerCache;
    			}

    			else if (e.getLocationStateType() == LocationStateType.OUTSIDE_ON_MARS) {//.getName().equals("Outside on the surface of Mars")) {
    				// case F
    				loc = OUTSIDE_ON_THE_SURFACE_OF_MARS;
    			}
    		}
    	}

    	else if (unit instanceof Vehicle) {
    		Vehicle v = (Vehicle) unit;
			//Settlement settlement = (Settlement) v.getContainerUnit();
			Settlement s = v.getSettlement();
   			//System.out.println(v.getName() + " is in " + settlement.getName());
   			//Unit tc = v.getTopContainerUnit();
			//Unit c = v.getContainerUnit();

   			if (v.getLocationStateType() == LocationStateType.SETTLEMENT_VICINITY)
				loc = PARKED + WITHIN_THE_VINCINITY_OF + s;

   			else if (v.getLocationStateType() == LocationStateType.OUTSIDE_ON_MARS)
				loc = OUTSIDE_ON_A_MISSION; //or OUTSIDE_ON_THE_SURFACE_OF_MARS;

   			else if (v.getLocationStateType() == LocationStateType.INSIDE_BUILDING) {
   	   			Unit tc = v.getTopContainerUnit();
   	   			s = (Settlement)tc;
   				Building b = v.getGarage(s);
   				//System.out.println(v + " is at " + s);
   				//System.out.println(v + " is at " + b);

   				// case D
				// Note: it takes a short finite amount of time to update the latest LocationStateType
   				// the vehicle would have left the building and b becomes null when LocationStateType is waiting to be updated in the next frame.
   				if (s != null && b != null)
   					loc = PARKED_AT + b.getNickName() + IN + s;
   			}

   			else {
   				Unit c = v.getContainerUnit();
   				loc = PARKED_AT + c;
   			}

		}

        lcdText.setLcdText(loc);

    }

	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		containerCache = null;
		topContainerCache = null;
		coordsPanel = null;
		latitudeLabel = null;
		longitudeLabel = null;
		centerPanel = null;
		terrainElevation = null;
		locationCache = null;
		mainScene = null;
		locatorButton = null;
		lcdLong = null;
		lcdLat = null;
		lcdText = null;
		gauge = null;
		fmt = null;
		fmt2 = null;
	}

}
