/**
 * Mars Simulation Project
 * MSPEvent.java
 * @version 3.07 2014-12-06
 * @author Barry Evans
 */
package org.mars_sim.msp.core.events;

import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.time.MarsClock;

/**
 * This class represents a time based event that has occurred in the simulation.
 * It is aimed at being subclassed to reflect the real simulation specific
 * events.
 * An event consists of a time stamp when it occurred, a description, an
 * optional Unit that is the source of the event and an optional Object that has
 * triggered the event.
 */
public abstract class HistoricalEvent {

	/**
	 * Category of event.
	 * @see HistoricalEventManager
	 * @see HistoricalEventCategory
	 */
	private HistoricalEventCategory category;
	/** Type of historical events. */
	private EventType type;
	/** TODO Long description of historical events should be internationalizable. */
	private String description;
	/** Time event occurred. */
	private MarsClock timestamp;
	/** Source of event may be null. */
	private Object source;

	/**
	 * Construct an event with the appropriate information. The time is not
	 * defined until the evnet is registered with the Event Manager.
	 * @param category {@link HistoricalEventCategory} Category of event.
	 * @param type {@link EventType} Type of event.
	 * @param source The object that has produced the event, if this is null
	 * then it is a global simulation event. It could be a Unit or a Building.
	 * @param description Long description of event.
	 * @see org.mars_sim.msp.core.events.HistoricalEventManager#registerNewEvent(HistoricalEvent)
	 */
	public HistoricalEvent(HistoricalEventCategory category, EventType type, Object source, String description) {
		this.category = category;
		this.type = type;
		this.source = source;
		this.description = description;
		// need count++ next time: System.out.println("HistoricalEvent.java constructor");
	}

	/**
	 * Set the timestamp for this event.
	 * @see org.mars_sim.msp.core.events.HistoricalEventManager#registerNewEvent(HistoricalEvent)
	 */
	void setTimestamp(MarsClock timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Get description.
	 * @return Description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get the Unit source.
	 * @return Object as the source of event.
	 */
	public Object getSource() {
		return source;
	}

	/**
	 * Get event time.
	 * @return Time the event happened
	 */
	public MarsClock getTimestamp() {
		return timestamp;
	}

	/**
	 * Get the type of event.
	 * @return String representing the type.
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * Gets the category of the event.
	 * @return {@link HistoricalEventCategory}
	 */
	public HistoricalEventCategory getCategory() {
		return category;
	}
}