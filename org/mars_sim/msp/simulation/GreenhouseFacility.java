/**
 * Mars Simulation Project
 * GreenhouseFacility.java
 * @version 2.73 2001-11-11
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation;

/**
 * The GreenhouseFacility class represents the greenhouses in a settlement.
 * It defines the amount of fresh and dried foods generated by the greenhouses.
 */

public class GreenhouseFacility extends Facility {

    // Data members
    private double fullHarvestAmount; // Number of food units the greenhouse can produce at full harvest.
    private double workLoad; // Amount of work time (in millisols) tending greenhouse required during growth period for full harvest.
    private double growingWork; // Amount of work time (in millisols) completed for growing phase.
    private double workCompleted; // Amount of work time (in millisols) completed for current phase.
    private double growthPeriod; // Amount of time for growth period.
    private double growthPeriodCompleted; // Amount of time completed in current growth period.
    private String phase; // "Inactive", "Planting", "Growing" or "Harvesting"

    /** Constructor for random creation. 
     *  @param manager of the greenhouse facility
     */
    public GreenhouseFacility(FacilityManager manager) {

        // Use Facility's constructor.
        super(manager, "Greenhouse");

        // Initialize data members
        workCompleted = 0D;
        growthPeriod = 10000D; // (10 sols)
        growthPeriodCompleted = 0D;
        phase = "Inactive";

        // Determine full harvest amount.
	fullHarvestAmount = (growthPeriod / 1000D) * manager.getSettlement().getPopulationCapacity();

        // Determine work load based on full harvest amount.
        // (3200 mSols for 200 food - 4800 mSols for 300 food)
        workLoad = 16D * fullHarvestAmount;
    }

    /** Constructor for set values (used later when facilities can be built or upgraded.) 
     *  @param manager manager of the greenhouse facility
     *  @param workLoad tending work required for a full harvest (in millisols)
     *  @param growthPeriod time required to grow crops (in millisols)
     *  @param fullHarvestAmount number of food units the greenhouse can produce at full harvest
     */
    public GreenhouseFacility(FacilityManager manager, float workLoad, float growthPeriod, float fullHarvestAmount) {

        // Use Facility's constructor.

        super(manager, "Greenhouse");

        // Initialize data members.

        this.workLoad = workLoad;
        this.growthPeriod = growthPeriod;
        this.fullHarvestAmount = fullHarvestAmount;
        workCompleted = 0D;
        growthPeriodCompleted = 0D;
        phase = "Inactive";
    }

    /** Returns the harvest amount of the greenhouse. 
     *  @return number of food units the greenhouse can produce at full harvest
     */
    public double getFullHarvestAmount() {
        return fullHarvestAmount;
    }

    /** Returns the work load of the greenhouse. (in work-hours) 
     *  @return tending work required for a full harvest (in work hours)
     */
    public double getWorkLoad() {
        return workLoad;
    }

    /** Returns the work completed in this cycle in the growing phase. 
     *  @return work completed so far in the growing phase (in work hours)
     */
    public double getGrowingWork() {
        return growingWork;
    }

    /** Returns the growth period of the greenhouse. (in days) 
     *  @return time required to grow crops (in days)
     */
    public double getGrowthPeriod() {
        return growthPeriod;
    }

    /** Returns the current work completed on the current phase. (in work-hours) 
     *  @return work completed so far in this phase (in work hours)
     */
    public double getWorkCompleted() {
        return workCompleted;
    }

    /** Returns the time completed of the current growth cycle. (in days) 
     *  @return time completed in growth cycle (in days)
     */
    public double getTimeCompleted() {
        return growthPeriodCompleted;
    }

    /** Returns true if a harvest cycle has been started. 
     *  @return current phase
     */
    public String getPhase() {
        return phase;
    }

    /** Adds work to the work completed on a growth cycle. 
     *  @param work time added to growth cycle (in millisols)
     */
    public void addWorkToGrowthCycle(double time) {

        double plantingWork = 1000D;
        double harvestingWork = 10D * fullHarvestAmount;
        double workInPhase = workCompleted + time;

        if (phase.equals("Inactive"))
            phase = "Planting";

        if (phase.equals("Planting")) {
            if (workInPhase >= plantingWork) {
                workInPhase -= plantingWork;
                phase = "Growing";
            }
        }

        if (phase.equals("Growing"))
            growingWork = workInPhase;

        if (phase.equals("Harvesting")) {
            if (workInPhase >= harvestingWork) {
                workInPhase -= harvestingWork;
                double foodProduced = fullHarvestAmount * (growingWork / workLoad);
                ((StoreroomFacility) manager.getFacility("Storerooms")).addFood(foodProduced);
                phase = "Planting";
                growingWork = 0D;
                growthPeriodCompleted = 0D;
            }
        }

        workCompleted = workInPhase;
    }

    /** Override Facility's timePasses method to allow for harvest cycle. 
     *  @param time the amount of time passing (in millisols) 
     */
    void timePassing(double time) {

        if (phase.equals("Growing")) {
            growthPeriodCompleted += time;
            if (growthPeriodCompleted >= growthPeriod) {
                phase = "Harvesting";
                workCompleted = 0D;
            }
        }
    }
}
