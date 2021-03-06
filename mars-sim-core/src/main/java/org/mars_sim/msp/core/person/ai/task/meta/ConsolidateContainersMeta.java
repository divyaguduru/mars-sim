/**
 * Mars Simulation Project
 * ConsolidateContainersMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.task.ConsolidateContainers;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.robot.ai.job.Deliverybot;

/**
 * Meta task for the ConsolidateContainers task.
 */
public class ConsolidateContainersMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.consolidateContainers"); //$NON-NLS-1$

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new ConsolidateContainers(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (LocationSituation.IN_SETTLEMENT == person.getLocationSituation() ||
                LocationSituation.IN_VEHICLE == person.getLocationSituation()) {

            // Check if there are local containers that need resource consolidation.
            if (ConsolidateContainers.needResourceConsolidation(person)) {
                result = 10D;
            }

            // Effort-driven task modifier.
            result *= person.getPerformanceRating();

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
        return new ConsolidateContainers(robot);
	}

	@Override
	public double getProbability(Robot robot) {

        double result = 0D;

        if (robot.getBotMind().getRobotJob() instanceof Deliverybot)
	        if (LocationSituation.IN_SETTLEMENT == robot.getLocationSituation() ||
	                LocationSituation.IN_VEHICLE == robot.getLocationSituation()) {

	            // Check if there are local containers that need resource consolidation.
	            if (ConsolidateContainers.needResourceConsolidation(robot)) {
	                result = 10D;
	            }

	            // Effort-driven task modifier.
	            result *= robot.getPerformanceRating();
	        }

        return result;
	}
}