/* Mars Simulation Project
 * SpringAppLeaf.java
 * @version 3.1.0 2016-06-21
 * @author Manny Kung
 * $LastChangedDate$
 * $LastChangedRevision$
 */

package org.mars_sim.msp.ui.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@SpringBootApplication(exclude={org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration.class})
@ComponentScan
public class SpringAppLeaf {

    public static void main(String[] args) {
        SpringApplication.run(SpringAppLeaf.class, args);
    }

}