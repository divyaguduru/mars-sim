/**
 * Mars Simulation Project
 * MaintainGroundVehicleGarageMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.malfunction.MalfunctionManager;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.MaintainGroundVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Maintenance;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.vehicle.Vehicle;

/**
 * Meta task for the MaintainGroundVehicleGarage task.
 */
public class MaintainGroundVehicleGarageMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.maintainGroundVehicleGarage"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(MaintainGroundVehicleGarageMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new MaintainGroundVehicleGarage(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

        	try {
            // Get all vehicles requiring maintenance.
                Iterator<Vehicle> i = MaintainGroundVehicleGarage.getAllVehicleCandidates(person).iterator();
                while (i.hasNext()) {
                    Vehicle vehicle = i.next();
                    MalfunctionManager manager = vehicle.getMalfunctionManager();
                    boolean hasMalfunction = manager.hasMalfunction();
                    boolean hasParts = Maintenance.hasMaintenanceParts(person, vehicle);
                    double effectiveTime = manager.getEffectiveTimeSinceLastMaintenance();
                    boolean minTime = (effectiveTime >= 1000D);
                    if (!hasMalfunction && hasParts && minTime) {
                        double entityProb = effectiveTime / 50D;
                        if (entityProb > 100D) {
                            entityProb = 100D;
                        }
                        result += entityProb;
                    }
                }
	        }
	        catch (Exception e) {
	            logger.log(Level.SEVERE,"getProbability()",e);
	        }
	
	        // Determine if settlement has available space in garage.
	        boolean garageSpace = false;
	        boolean needyVehicleInGarage = false;
	        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {
	            Settlement settlement = person.getSettlement();
	            Iterator<Building> j = settlement.getBuildingManager().getBuildings(
	                    BuildingFunction.GROUND_VEHICLE_MAINTENANCE).iterator();
	            while (j.hasNext() && !garageSpace) {
	                try {
	                    Building building = j.next();
	                    VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(
	                            BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
	                    if (garage.getCurrentVehicleNumber() < garage.getVehicleCapacity()) {
	                        garageSpace = true;
	                    }
	
	                    Iterator<Vehicle> i = garage.getVehicles().iterator();
	                    while (i.hasNext()) {
	                        if (i.next().isReservedForMaintenance()) {
	                            needyVehicleInGarage = true;
	                        }
	                    }
	                }
	                catch (Exception e) {}
	            }
	        }
	        if (!garageSpace && !needyVehicleInGarage) {
	            result = 0D;
	        }
	
	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();
	
	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(MaintainGroundVehicleGarage.class)
	            		* person.getSettlement().getGoodsManager().getTransportationFactor();
	        }
	
	        // Modify if tinkering is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Tinkering")) {
	            result *= 1.5D;
	        }
	
	        // 2015-06-07 Added Preference modifier
	        if (result > 0D) {
	            result = result + result * person.getPreference().getPreferenceScore(this)/5D;
	        }
	        
	        if (result < 0) result = 0;

        }
        
        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}