/**
 * Mars Simulation Project
 * SolarPowerSource.java
 * @version 3.1.0 2017-08-14
 * @author Scott Davis
 */
package org.mars_sim.msp.core.structure.building.function;

import java.io.Serializable;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.mars.Mars;
import org.mars_sim.msp.core.mars.OrbitInfo;
import org.mars_sim.msp.core.mars.SurfaceFeatures;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * A power source that gives a supply of power proportional
 * to the level of sunlight it receives.
 */
public class SolarPowerSource
extends PowerSource
implements Serializable {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(SolarPowerSource.class.getName());
	
	private static final double MAINTENANCE_FACTOR = 2.5D;
	
	/** In terms of solar cell degradation, NASA MER has an observable degradation rate of 0.14% per sol
	 *  on the solar cell (if starting from 100%).
	 	Here we tentatively set to 0.04% per sol instead of 0.14%, since that in 10 earth years,
	 	the efficiency will	drop down to 23.21% of the initial 100%
	 	100*(1-.04/100)^(365*10) = 23.21% */
	public static double DEGRADATION_RATE_PER_SOL = .0004; // assuming it is a constant through its mission
	
	

	/*
	 * The Solar Photovoltaic Array and the Solar Thermal Array has n number of layers of solar panels 
	 * that can be oriented toward the sun at the same time if the sun is at an oblique angle in order to
	 * extract more sunlight
	 */
	public static double NUM_LAYERS = 4D;		// in square feet
	public static double STEERABLE_ARRAY_AREA = 50D;		// in square feet
	public static double AUXILLARY_PANEL_AREA = 15D;	// in square feet
		
	public static double PI = Math.PI;
	public static double HALF_PI = PI / 2D;

	/** The solar Panel is made of triple-junction solar cells with theoretical max eff of 68% */
	private double efficiency_solar_panel = .68;

	private Coordinates location ;
	private SurfaceFeatures surface ;
	private Mars mars;
	private OrbitInfo orbitInfo;
	
	/**
	 * Constructor.
	 * @param maxPower the maximum generated power (kW).
	 */
	public SolarPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(PowerSourceType.SOLAR_POWER, maxPower);
	}

	/**
	 * Gets the current power produced by the power source.
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	//@Override
	public double getCurrentPower(Building building) {
		BuildingManager manager = building.getBuildingManager();
		if (location == null)
			location = manager.getSettlement().getCoordinates();
        if (mars == null)
        	mars = Simulation.instance().getMars();
		if (surface == null)
			surface = mars.getSurfaceFeatures();
		double area = AUXILLARY_PANEL_AREA;
		if (building.getBuildingType().equalsIgnoreCase("Solar Photovoltaic Array")) {
	        if (orbitInfo == null)
	            orbitInfo = mars.getOrbitInfo();
			double angle = orbitInfo.getSolarZenithAngle(location);
			//logger.info("angle : " + angle/ Math.PI*180D);
			// assuming the total area will change from 3 full panels to 1 panel based on the solar zenith angle 
			if (angle <= - HALF_PI) {
				angle = angle + PI;
				area = STEERABLE_ARRAY_AREA * ((NUM_LAYERS - 1)  / HALF_PI * angle + 1);
			}
			else if (angle <= 0) {
				angle = angle + HALF_PI;
				area = STEERABLE_ARRAY_AREA * ((1 - NUM_LAYERS) / HALF_PI * angle + NUM_LAYERS);
			}
			else if (angle <= HALF_PI) {
				area = STEERABLE_ARRAY_AREA * ((NUM_LAYERS - 1) / HALF_PI * angle + 1);
			}
			else {
				angle = angle - HALF_PI;
				area = STEERABLE_ARRAY_AREA * ((1 - NUM_LAYERS) / HALF_PI * angle + NUM_LAYERS);
			}
			//logger.info("area : " + area);
		}
		
		double available = surface.getSolarIrradiance(location) /1000D * efficiency_solar_panel * area; // add noise with * (.99 + RandomUtil.getRandomDouble(.2));
		double capable = getMaxPower();
		if (available >= capable)
			return capable;
		else
			return available;
	}

	@Override
	public double getAveragePower(Settlement settlement) {
		return getMaxPower() / 2D;
	}

	@Override
	public double getMaintenanceTime() {
	    return getMaxPower() * MAINTENANCE_FACTOR;
	}

	public void setEfficiency(double value) {
		 efficiency_solar_panel = value;
	}

	public double getEfficiency() {
		return efficiency_solar_panel;
	}

	@Override
	public void removeFromSettlement() {
		// TODO Auto-generated method stub

	}
}