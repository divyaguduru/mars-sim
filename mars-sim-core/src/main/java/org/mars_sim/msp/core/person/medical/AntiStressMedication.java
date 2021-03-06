/**
 * Mars Simulation Project
 * AntiStressMedication.java
 * @version 3.1.0 2016-10-15
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.medical;

import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.PhysicalCondition;

import java.io.Serializable;

/**
 * A medication that reduces stress.
 */
public class AntiStressMedication extends Medication implements Serializable {

    // The name of the medication.
    public static final String NAME = "Anti-stress Medication";
    
    // Stress reduction amount.
    private static final double STRESS_REDUCTION = -1.0D;
    
    // Duration (millisols).
    private static final double DURATION = 200D;
    
    /**
     * Constructor
     * @param person the person taking the medication.
     */
    public AntiStressMedication(Person person) {
        // Use Medication constructor.
        super(NAME, DURATION, person);
    }
    
    @Override
    public void timePassing(double time) {
        super.timePassing(time);
        
        // Reduce person's stress.
        PhysicalCondition condition = getPerson().getPhysicalCondition();
        condition.setStress(condition.getStress() + (STRESS_REDUCTION * time));
    }
}