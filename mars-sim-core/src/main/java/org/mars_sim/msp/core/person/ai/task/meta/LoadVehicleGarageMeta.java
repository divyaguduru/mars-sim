/**
 * Mars Simulation Project
 * LoadVehicleGarageMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.task.LoadVehicleGarage;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;

/**
 * Meta task for the LoadVehicleGarage task.
 */
public class LoadVehicleGarageMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.loadVehicleGarage"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(LoadVehicleGarageMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new LoadVehicleGarage(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

            // Check all vehicle missions occurring at the settlement.
            try {
                List<Mission> missions = LoadVehicleGarage.getAllMissionsNeedingLoading(person.getSettlement());
                result = 100D * missions.size();
            }
            catch (Exception e) {
                logger.log(Level.SEVERE, "Error finding loading missions.", e);
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

            // Job modifier.
            Job job = person.getMind().getJob();
            if (job != null) {
                result *= job.getStartTaskProbabilityModifier(LoadVehicleGarage.class)
                		* person.getSettlement().getGoodsManager().getTransportationFactor();
            }

            // Modify if operations is the person's favorite activity.
            if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Operations")) {
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
		return new LoadVehicleGarage(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)

	        if (robot.getLocationSituation() == LocationSituation.IN_SETTLEMENT) {

	            // Check all vehicle missions occurring at the settlement.
	            try {
	                List<Mission> missions = LoadVehicleGarage.getAllMissionsNeedingLoading(robot.getSettlement());
	                result = 100D * missions.size();
	            }
	            catch (Exception e) {
	                logger.log(Level.SEVERE, "Error finding loading missions.", e);
	            }


	            // Effort-driven task modifier.
	            result *= robot.getPerformanceRating();

	        }

        return result;
    }
}