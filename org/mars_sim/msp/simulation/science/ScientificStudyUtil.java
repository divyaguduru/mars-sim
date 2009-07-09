/**
 * Mars Simulation Project
 * ScientificStudyUtil.java
 * @version 2.87 2009-07-05
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.science;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mars_sim.msp.simulation.RandomUtil;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.person.NaturalAttributeManager;
import org.mars_sim.msp.simulation.person.Person;
import org.mars_sim.msp.simulation.person.ai.job.Job;
import org.mars_sim.msp.simulation.structure.Settlement;

/**
 * A utility class for scientific studies.
 */
public class ScientificStudyUtil {

    /**
     * Private constructor for utility class.
     */
    private ScientificStudyUtil() {}
    
    /**
     * Gets a list of all available collaborators that can be invited to a study.
     * @param study the scientific study.
     * @return list of potential collaborators.
     */
    public static List<Person> getAvailableCollaboratorsForInvite(ScientificStudy study) {
        List<Person> result = new ArrayList<Person>();
        
        Collection<Person> allPeople = Simulation.instance().getUnitManager().getPeople();
        Iterator<Person> i = allPeople.iterator();
        while (i.hasNext()) {
            Person person = i.next();
            boolean available = false;
            
            if (!person.equals(study.getPrimaryResearcher()) && 
                    !study.hasResearcherBeenInvited(person)) {
                Science[] collaborativeSciences = study.getScience().getCollaborativeSciences();
                Job job = person.getMind().getJob();
                if (job != null) {
                    for (int x = 0; x < collaborativeSciences.length; x++) {
                        Job[] associatedJobs = collaborativeSciences[x].getJobs();
                        for (int y = 0; y < associatedJobs.length; y++) {
                            if (job.equals(associatedJobs[y])) available = true;
                        }
                    }
                }
            }
            
            if (available) result.add(person);
        }
        
        return result;
    }
    
    /**
     * Determine the results of a study's peer review process.
     * @param study the scientific study.
     * @return true if study passes peer review, false if it fails to pass.
     */
    static boolean determinePeerReviewResults(ScientificStudy study) {
        
        double baseChance = 50D;
        
        // Modify based on primary researcher's academic aptitude attribute.
        int academicAptitude = study.getPrimaryResearcher().getNaturalAttributeManager().
                getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
        double academicAptitudeModifier = (academicAptitude - 50) / 2D;
        baseChance += academicAptitudeModifier;
        
        Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
        while (i.hasNext()) {
            Person researcher = i.next();
            double collaboratorModifier = 10D;
            
            // Modify based on collaborative researcher skill in their science.
            Science collaborativeScience = study.getCollaborativeResearchers().get(researcher);
            String skillName = ScienceUtil.getAssociatedSkill(collaborativeScience);
            int skillLevel = researcher.getMind().getSkillManager().getSkillLevel(skillName);
            collaboratorModifier *= (double) skillLevel / (double) study.getDifficultyLevel();
            
            // Modify based on researcher's academic aptitude attribute.
            int collaboratorAcademicAptitude = researcher.getNaturalAttributeManager().
                    getAttribute(NaturalAttributeManager.ACADEMIC_APTITUDE);
            double collaboratorAcademicAptitudeModifier = (collaboratorAcademicAptitude - 50) / 10D;
            collaboratorModifier += collaboratorAcademicAptitudeModifier;
            
            // Modify based on if collaborative science is different from primary science.
            if (!collaborativeScience.equals(study.getScience())) collaboratorModifier /= 2D;
            
            baseChance += collaboratorModifier;
        }
        
        // Randomly determine if study passes peer review.
        if (RandomUtil.getRandomDouble(100D) < baseChance) return true;
        else return false;
    }
    
    /**
     * Provide achievements for the completion of a study.
     * @param study the scientific study.
     */
    static void provideCompletionAchievements(ScientificStudy study) {
        
        double baseAchievement = study.getDifficultyLevel();
        Science primaryScience = study.getScience();
        
        // Add achievement credit to primary researcher.
        Person primaryResearcher = study.getPrimaryResearcher();
        primaryResearcher.addScientificAchievement(baseAchievement, primaryScience);
        
        // Add achievement credit to primary settlement.
        Settlement primarySettlement = study.getPrimarySettlement();
        primarySettlement.addScientificAchievement(baseAchievement, primaryScience);
        
        // Add achievement credit to collaborative researchers.
        double collaborativeAchievement = baseAchievement / 3D;
        Iterator<Person> i = study.getCollaborativeResearchers().keySet().iterator();
        while (i.hasNext()) {
            Person researcher = i.next();
            Science collaborativeScience = study.getCollaborativeResearchers().get(researcher);
            researcher.addScientificAchievement(collaborativeAchievement, collaborativeScience);
        }
    }
}