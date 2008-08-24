/**
 * Mars Simulation Project
 * SolarPowerSource.java
 * @version 2.85 2008-08-23
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;

import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.mars.*;
import org.mars_sim.msp.simulation.structure.building.*;

/**
 * A power source that gives a supply of power proportional 
 * to the level of sunlight it receives.
 */
public class SolarPowerSource extends PowerSource implements Serializable {

	private final static String TYPE = "Solar Power Source";

    /**
     * Constructor
     * @param maxPower the maximum generated power.
     */
	public SolarPowerSource(double maxPower) {
		// Call PowerSource constructor.
		super(TYPE, maxPower);
	}

	/**
	 * Gets the current power produced by the power source.
	 * @param building the building this power source is for.
	 * @return power (kW)
	 */
	public double getCurrentPower(Building building) {
		BuildingManager manager = building.getBuildingManager();
		Coordinates location = manager.getSettlement().getCoordinates();
		SurfaceFeatures surface = Simulation.instance().getMars().getSurfaceFeatures();
		double sunlight = surface.getSurfaceSunlight(location);
		return sunlight * getMaxPower();
	}
}