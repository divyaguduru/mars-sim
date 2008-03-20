/**
 * Mars Simulation Project
 * MineralMapLayer.java
 * @version 2.84 2008-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.tool.map;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.image.MemoryImageSource;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mars_sim.msp.simulation.Coordinates;
import org.mars_sim.msp.simulation.Simulation;
import org.mars_sim.msp.simulation.mars.MineralMap;

/**
 * A map layer showing mineral concentrations.
 */
public class MineralMapLayer implements MapLayer {

    private static String CLASS_NAME = "org.mars_sim.msp.ui.standard.tool.map.MineralMapLayer";
    private static Logger logger = Logger.getLogger(CLASS_NAME);
	
	// Domain members
	private MineralMap mineralMap;
	private int[] mineralConcentrationArray;
    private Component displayComponent;
    private Image mineralConcentrationMap;
    private Coordinates mapCenterCache;
    private String mapTypeCache;
	
    /**
     * Constructor
     * @param displayComponent the display component.
     */
    public MineralMapLayer(Component displayComponent) {
    	mineralMap = Simulation.instance().getMars().getSurfaceFeatures().getMineralMap();
    	this.displayComponent = displayComponent;
    	mineralConcentrationArray = new int[Map.DISPLAY_WIDTH * Map.DISPLAY_HEIGHT];
    }
    
	/**
     * Displays the layer on the map image.
     * @param mapCenter the location of the center of the map.
     * @param mapType the type of map.
     * @param g graphics context of the map display.
     */
	public void displayLayer(Coordinates mapCenter, String mapType, Graphics g) {
		
		if (!mapCenter.equals(mapCenterCache) || !mapType.equals(mapTypeCache)) {
			mapCenterCache = new Coordinates(mapCenter);
			mapTypeCache = mapType;
		
			int centerX = 150;
			int centerY = 150;
        
			double rho = 0D;
			if (USGSMarsMap.TYPE.equals(mapType)) rho = USGSMarsMap.PIXEL_RHO;
			else rho = CannedMarsMap.PIXEL_RHO;
        
			Coordinates location = new Coordinates(0D, 0D);
			for (int x = 0; x < Map.DISPLAY_WIDTH; x+=2) {
				for (int y = 0; y < Map.DISPLAY_HEIGHT; y+=2) {
					mapCenter.convertRectToSpherical(x - centerX, y - centerY, rho, location);
					double concentration = mineralMap.getMineralConcentration(MineralMap.HEMATITE, location);
					int concentrationInt = (int) (255 * (concentration / 100D));
					int concentrationColor = (concentrationInt << 24) | 0x000000FF;
               
					mineralConcentrationArray[x + (y * Map.DISPLAY_WIDTH)] = concentrationColor;
					mineralConcentrationArray[x + 1 + (y * Map.DISPLAY_WIDTH)] = concentrationColor;
					if (y < Map.DISPLAY_HEIGHT -1) {
						mineralConcentrationArray[x + ((y + 1) * Map.DISPLAY_WIDTH)] = concentrationColor;
						mineralConcentrationArray[x + 1 + ((y + 1) * Map.DISPLAY_WIDTH)] = concentrationColor;
					}
				}
			}
        
			// Create mineral concentration image for map
			mineralConcentrationMap = displayComponent.createImage(
					new MemoryImageSource(Map.DISPLAY_WIDTH, Map.DISPLAY_HEIGHT, 
							mineralConcentrationArray, 0, Map.DISPLAY_WIDTH));

			MediaTracker mt = new MediaTracker(displayComponent);
			mt.addImage(mineralConcentrationMap, 0);
			try {
				mt.waitForID(0);
			}
			catch (InterruptedException e) {
				logger.log(Level.SEVERE,"MineralMapLayer interrupted: " + e);
			}
		}

        // Draw the mineral concentration image
        g.drawImage(mineralConcentrationMap, 0, 0, displayComponent);
	}
}