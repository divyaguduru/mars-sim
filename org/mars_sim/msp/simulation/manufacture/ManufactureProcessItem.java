/**
 * Mars Simulation Project
 * ManufactureProcessItem.java
 * @version 2.83 2008-01-19
 * @author Scott Davis
 */

package org.mars_sim.msp.simulation.manufacture;

import java.io.Serializable;

/**
 * A manufacturing process input or output item.
 */
public class ManufactureProcessItem implements Serializable {

	public final static String AMOUNT_RESOURCE = "resource";
	public final static String PART = "part";
	public final static String EQUIPMENT = "equipment";
	public final static String VEHICLE = "vehicle";
	
	// Data members
	private String name;
	private String type;
	private double amount;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public void setAmount(double amount) {
		this.amount = amount;
	}
}