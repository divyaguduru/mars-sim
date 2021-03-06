/**
 * Mars Simulation Project
 * JobAssignment.java
 * @version 3.08 2015-03-31
 * @author Manny Kung
 */
package org.mars_sim.msp.core.person.ai.job;

import java.io.Serializable;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;

/**
 * The JobAssignment class represents the characteristics of a job type
 */
public class JobAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    private String initiator;
    private String jobType;
    private String timeSubmitted;
    private String authorizedBy;
    private MarsClock timeAuthorized;
    private JobAssignmentType status; // JobAssignmentType.PENDING or JobAssignmentType.APPROVED
    private double jobRating = 5; // has a score of 5 if unrated 
    private int sol;
	private int solRatingSubmitted = -1; //no rating has ever been submitted

	private static MarsClock marsClock;

	public JobAssignment(String timeSubmitted, String jobType, String initiator, JobAssignmentType status, String authorizedBy) {
		// 2015-09-23 Changed the first parameter of JobAssignment.java from MarsClock to String.
		this.timeSubmitted = timeSubmitted;
		this.jobType = jobType;
		this.initiator = initiator;
		this.status = status;
		this.authorizedBy = authorizedBy;
		marsClock = Simulation.instance().getMasterClock().getMarsClock();
	}

	public String getTimeSubmitted() {
		return timeSubmitted;
	}

	public int getSolSubmitted() {
		return sol;
	}

	public void setSolSubmitted() {
		//MarsClock clock = Simulation.instance().getMasterClock().getMarsClock();
		sol = marsClock.getSolElapsedFromStart();//marsClock);
	}

	//public MarsClock getTimeAuthorized() {
	//	return timeAuthorized;
	//}

	//public void setTimeAuthorized(MarsClock time) {
	//	this.timeAuthorized = time;
	//}

	public String getJobType() {
		return jobType;
	}

	public String getInitiator() {
		return initiator;
	}

	public String getAuthorizedBy() {
		return authorizedBy;
	}

	public void setAuthorizedBy(String name) {
		authorizedBy = name;
	}

	public JobAssignmentType getStatus() {
		return status;
	}

	public void setStatus(JobAssignmentType status) {
		//System.out.println("setStatus : status is " + status);
		this.status = status;
	}

	public void setJobRating(int value) {
		jobRating = (int) (0.7 * jobRating + 0.3 * value);	
	}

	public void setSolRatingSubmitted(int sol){
		solRatingSubmitted = sol;
	}

	public int getSolRatingSubmitted(){
		return solRatingSubmitted;
	}
	
	public int getJobRating() {	
		return (int)jobRating;
	}
}
