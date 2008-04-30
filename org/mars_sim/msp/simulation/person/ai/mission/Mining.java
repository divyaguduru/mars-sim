package org.mars_sim.msp.simulation.person.ai.mission;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Inventory;
import org.mars_sim.msp.simulation.InventoryException;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.SimulationConfig;
import org.mars_sim.msp.simulation.equipment.Bag;
import org.mars_sim.msp.simulation.equipment.EVASuit;
import org.mars_sim.msp.simulation.mars.ExploredLocation;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.PersonConfig;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.person.ai.task.CollectResources;
import org.mars_sim.msp.simulation.person.ai.task.ExploreSite;
import org.mars_sim.msp.simulation.person.ai.task.Task;
import org.mars_sim.msp.simulation.resource.AmountResource;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.structure.goods.Good;
import org.mars_sim.msp.simulation.structure.goods.GoodsUtil;
import org.mars_sim.msp.simulation.time.MarsClock;
import org.mars_sim.msp.simulation.vehicle.Rover;

public class Mining extends RoverMission {

	private static String CLASS_NAME = 
		"org.mars_sim.msp.simulation.person.ai.mission.Mining";
	private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Default description.
	public static final String DEFAULT_DESCRIPTION = "Mining";
	
	// Mission phases
	final public static String MINING_SITE = "Mining Site";
	
	// Number of bags needed for mission.
	private static final int NUMBER_OF_BAGS = 20;
	
	private static final double MINERAL_BASE_AMOUNT = 1000D;
	
	// Data members
	private ExploredLocation miningSite;
	private MarsClock miningSiteStartTime;
	private boolean endMiningSite;
	
	/**
	 * Constructor
	 * @param startingPerson the person starting the mission.
	 * @throws MissionException if error creating mission.
	 */
	public Mining(Person startingPerson) throws MissionException {
		
		// Use RoverMission constructor.
		super(DEFAULT_DESCRIPTION, startingPerson, RoverMission.MIN_PEOPLE);
		
		if (!isDone()) {
        	// Set mission capacity.
        	if (hasVehicle()) setMissionCapacity(getRover().getCrewCapacity());
        	int availableSuitNum = VehicleMission.getNumberAvailableEVASuitsAtSettlement(startingPerson.getSettlement());
        	if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
        	
			// Initialize data members.
			setStartingSettlement(startingPerson.getSettlement());
			
			// Recruit additional people to mission.
        	recruitPeopleForMission(startingPerson);
        	
        	// Determine mining site.
        	try {
        		if (hasVehicle()) {
        			double range = getVehicle().getRange();
        			miningSite = determineBestMiningSite(range, getStartingSettlement());
        			addNavpoint(new NavPoint(miningSite.getLocation(), "mining site"));
        		}
        	}
        	catch (Exception e) {
        		throw new MissionException(getPhase(), e);
        	}
        	
			// Add home settlement
			addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), 
					getStartingSettlement(), getStartingSettlement().getName()));
			
        	// Check if vehicle can carry enough supplies for the mission.
        	if (hasVehicle() && !isVehicleLoadable()) 
        		endMission("Vehicle is not loadable. (Mining)");
		}
		
		// Add mining site phase.
		addPhase(MINING_SITE);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());
	}
	
	/**
	 * Constructor with explicit data.
	 * @param members collection of mission members.
	 * @param startingSettlement the starting settlement.
	 * @param miningSite the site to mine.
	 * @param rover the rover to use.
	 * @param description the mission's description.
	 * @throws MissionException if error constructing mission.
	 */
	public Mining(Collection<Person> members, Settlement startingSettlement, 
    		ExploredLocation miningSite, Rover rover, String description) throws MissionException {
		
       	// Use RoverMission constructor.
    	super(description, (Person) members.toArray()[0], 1, rover);
		
    	// Initialize data members.
		setStartingSettlement(startingSettlement);
		this.miningSite = miningSite;
		
		// Set mission capacity.
		setMissionCapacity(getRover().getCrewCapacity());
		int availableSuitNum = VehicleMission.getNumberAvailableEVASuitsAtSettlement(startingSettlement);
    	if (availableSuitNum < getMissionCapacity()) setMissionCapacity(availableSuitNum);
    	
    	// Add mining site nav point.
    	addNavpoint(new NavPoint(miningSite.getLocation(), "mining site"));
    	
		// Add home settlement
		addNavpoint(new NavPoint(getStartingSettlement().getCoordinates(), 
				getStartingSettlement(), getStartingSettlement().getName()));
		
    	// Check if vehicle can carry enough supplies for the mission.
    	if (hasVehicle() && !isVehicleLoadable()) 
    		endMission("Vehicle is not loadable. (Mining)");
    	
		// Add mining site phase.
		addPhase(MINING_SITE);
		
		// Set initial mission phase.
		setPhase(VehicleMission.EMBARKING);
		setPhaseDescription("Embarking from " + getStartingSettlement().getName());
	}
	
	/** 
	 * Gets the weighted probability that a given person would start this mission.
	 * @param person the given person
	 * @return the weighted probability
	 */
	public static double getNewMissionProbability(Person person) {
		
		double result = 0D;
		
		if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
			Settlement settlement = person.getSettlement();
	    
			// Check if a mission-capable rover is available.
			boolean reservableRover = RoverMission.areVehiclesAvailable(settlement);
			
			// Check if minimum number of people are available at the settlement.
			// Plus one to hold down the fort.
			boolean minNum = RoverMission.minAvailablePeopleAtSettlement(settlement, (MIN_PEOPLE + 1));
			
			// Check if there are enough bags at the settlement for collecting minerals.
			boolean enoughBags = false;
			try {
				int numBags = settlement.getInventory().findNumEmptyUnitsOfClass(Bag.class);
				enoughBags = (numBags >= NUMBER_OF_BAGS);
			}
			catch (InventoryException e) {
				logger.log(Level.SEVERE, "Error checking if enough bags available.");
			}
			
			// Check for embarking missions.
			boolean embarkingMissions = VehicleMission.hasEmbarkingMissions(settlement);
	    
			// TODO: Check for available light utility vehicles.
			
			if (reservableRover && minNum && enoughBags && !embarkingMissions) {
				
				try {
					// Get available rover.
					Rover rover = (Rover) getVehicleWithGreatestRange(settlement);
					if (rover != null) {
				
						// Find best mining site.
						ExploredLocation miningSite = determineBestMiningSite(rover.getRange(), settlement);
						if (miningSite != null) {
							result = getMiningSiteValue(miningSite, settlement) * 50D;
						}
					}
				}
				catch (Exception e) {
			    	logger.log(Level.SEVERE, "Error getting mining site.", e);
				}
			}
			
			// Crowding modifier
			int crowding = settlement.getCurrentPopulationNum() - settlement.getPopulationCapacity();
			if (crowding > 0) result *= (crowding + 1);		
			
			// Job modifier.
			Job job = person.getMind().getJob();
			if (job != null) result *= job.getStartMissionProbabilityModifier(Mining.class);	
		}
		
		if (result > 0D) {
			// Check if min number of EVA suits at settlement.
			if (VehicleMission.getNumberAvailableEVASuitsAtSettlement(person.getSettlement()) < MIN_PEOPLE) 
				result = 0D;
		}
		
		return result;
	}
	
	@Override
    protected void determineNewPhase() throws MissionException {
    	if (EMBARKING.equals(getPhase())) {
    		startTravelToNextNode();
    		setPhase(VehicleMission.TRAVELLING);
    		setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
    	}
		else if (TRAVELLING.equals(getPhase())) {
			if (getCurrentNavpoint().isSettlementAtNavpoint()) {
				setPhase(VehicleMission.DISEMBARKING);
				setPhaseDescription("Disembarking at " + getCurrentNavpoint().getSettlement().getName());
			}
			else {
				setPhase(MINING_SITE);
				setPhaseDescription("Mining at " + getCurrentNavpoint().getDescription());
			}
		}
		else if (MINING_SITE.equals(getPhase())) {
			startTravelToNextNode();
			setPhase(VehicleMission.TRAVELLING);
			setPhaseDescription("Driving to " + getNextNavpoint().getDescription());
		}
		else if (DISEMBARKING.equals(getPhase())) endMission("Successfully disembarked.");
    }
    
    @Override
    protected void performPhase(Person person) throws MissionException {
    	super.performPhase(person);
    	if (MINING_SITE.equals(getPhase())) miningPhase(person);
    }
    
    private final void miningPhase(Person person) throws MissionException {
    	
    	// TODO implement
    }
    
    /**
     * Ends mining at a site.
     */
    public void endMiningAtSite() {
    	logger.info("Mining site phase ended due to external trigger.");
    	endMiningSite = true;
    	
    	// End each member's mining site task.
    	Iterator<Person> i = getPeople().iterator();
    	while (i.hasNext()) {
    		Task task = i.next().getMind().getTaskManager().getTask();
    		// TODO: Add MineSite task.
    		// if (task instanceof MineSite) ((MineSite) task).endEVA();
    		if (task instanceof CollectResources) ((CollectResources) task).endEVA();
    	}
    }
    
	/**
	 * Determines the best available mining site.
	 * @param roverRange the range of the mission rover (km).
	 * @param homeSettlement the mission home settlement.
	 * @return best explored location for mining, or null if none found.
	 * @throws MissionException if error determining mining site.
	 */
	private static ExploredLocation determineBestMiningSite(double roverRange, Settlement homeSettlement) 
			throws MissionException {
		
		ExploredLocation result = null;
		double bestValue = 0D;
		
		Iterator<ExploredLocation> i = 
			Simulation.instance().getMars().getSurfaceFeatures().getExploredLocations().iterator();
		while (i.hasNext()) {
			ExploredLocation site = i.next();
			if (!site.isMined()) {
				Coordinates siteLocation = site.getLocation();
				Coordinates homeLocation = homeSettlement.getCoordinates();
				if (homeLocation.getDistance(siteLocation) <= roverRange) {
					double value = getMiningSiteValue(site, homeSettlement);
					if (value > bestValue) {
						result = site;
						bestValue = value;
					}
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Gets the estimated mineral value of a mining site.
	 * @param site the mining site.
	 * @param settlement the settlement valuing the minerals.
	 * @return estimated value of the minerals at the site (VP).
	 * @throws MissionException if error determining the value.
	 */
	private static double getMiningSiteValue(ExploredLocation site, Settlement settlement) 
			throws MissionException {
		
		double result = 0D;
		
		Map<String, Double> concentrations = site.getEstimatedMineralConcentrations();
		Iterator<String> i = concentrations.keySet().iterator();
		while (i.hasNext()) {
			String mineralType = i.next();
			try {
				AmountResource mineralResource = AmountResource.findAmountResource(mineralType);
				Good mineralGood = GoodsUtil.getResourceGood(mineralResource);
				double mineralValue = settlement.getGoodsManager().getGoodValuePerMass(mineralGood);
				double concentration = concentrations.get(mineralType);
				double mineralAmount = (concentration / 100D) * MINERAL_BASE_AMOUNT;
				result += mineralValue * mineralAmount;
			}
			catch (Exception e) {
				throw new MissionException(null, e);
			}
		}
		
		return result;
	}
	
    /**
     * Gets the time limit of the trip based on life support capacity.
     * @param useBuffer use time buffer in estimation if true.
     * @return time (millisols) limit.
     * @throws MissionException if error determining time limit.
     */
    private static double getTotalTripTimeLimit(Rover rover, int memberNum, boolean useBuffer) 
    		throws MissionException {
    	
    	Inventory vInv = rover.getInventory();
    	
    	double timeLimit = Double.MAX_VALUE;
    	
    	PersonConfig config = SimulationConfig.instance().getPersonConfiguration();
		
    	try {
    		// Check food capacity as time limit.
    		double foodConsumptionRate = config.getFoodConsumptionRate();
    		double foodCapacity = vInv.getAmountResourceCapacity(AmountResource.FOOD);
    		double foodTimeLimit = foodCapacity / (foodConsumptionRate * memberNum);
    		if (foodTimeLimit < timeLimit) timeLimit = foodTimeLimit;
    		
    		// Check water capacity as time limit.
    		double waterConsumptionRate = config.getWaterConsumptionRate();
    		double waterCapacity = vInv.getAmountResourceCapacity(AmountResource.WATER);
    		double waterTimeLimit = waterCapacity / (waterConsumptionRate * memberNum);
    		if (waterTimeLimit < timeLimit) timeLimit = waterTimeLimit;
    		
    		// Check oxygen capacity as time limit.
    		double oxygenConsumptionRate = config.getOxygenConsumptionRate();
    		double oxygenCapacity = vInv.getAmountResourceCapacity(AmountResource.OXYGEN);
    		double oxygenTimeLimit = oxygenCapacity / (oxygenConsumptionRate * memberNum);
    		if (oxygenTimeLimit < timeLimit) timeLimit = oxygenTimeLimit;
    	}
    	catch (Exception e) {
    		throw new MissionException(null, e);
    	}
    	
    	// Convert timeLimit into millisols and use error margin.
    	timeLimit = (timeLimit * 1000D);
    	if (useBuffer) timeLimit /= Rover.LIFE_SUPPORT_RANGE_ERROR_MARGIN;
    	
    	return timeLimit;
    }
	
	@Override
	public Map<Class, Integer> getEquipmentNeededForRemainingMission(
			boolean useBuffer) throws MissionException {
    	if (equipmentNeededCache != null) return equipmentNeededCache;
    	else {
    		Map<Class, Integer> result = new HashMap<Class, Integer>();
    	
        	// Include one EVA suit per person on mission.
        	result.put(EVASuit.class, new Integer(getPeopleNumber()));
    		
    		// Include required number of bags.
    		result.put(Bag.class, new Integer(NUMBER_OF_BAGS));
    	
    		equipmentNeededCache = result;
    		return result;
    	}
	}

	@Override
	public Settlement getAssociatedSettlement() {
		return getStartingSettlement();
	}
}