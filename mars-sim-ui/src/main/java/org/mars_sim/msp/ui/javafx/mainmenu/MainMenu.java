/**
 * Mars Simulation Project
 * MainMenu.java
 * @version 3.1.0 2017-01-19
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.FadeTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.javafx.config.ScenarioConfigEditorFX;
import org.mars_sim.msp.ui.javafx.config.controller.MainMenuController;
import org.mars_sim.msp.ui.javafx.networking.MultiplayerMode;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.tool.StartUpLocation;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;


/*
 * The MainMenu class creates the Main Menu and the spinning Mars Globe for MSP
 */
public class MainMenu {

	// ------------------------------ FIELDS ------------------------------

	/** default logger. */
	private static Logger logger = Logger.getLogger(MainMenu.class.getName());

	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'

    public static final int WIDTH = 1024;//768-20;
    public static final int HEIGHT = 768;
    //private static final double VIEWPORT_SIZE = 768;

	// Data members
    //private double fileSize;

    public static String screen1ID = "main";
    public static String screen1File = "/fxui/fxml/MainMenu.fxml";
    public static String screen2ID = "configuration";
    public static String screen2File = "/fxui/fxml/Configuration.fxml";
    public static String screen3ID = "credits";
    public static String screen3File = "/fxui/fxml/Credits.fxml";

    //private double anchorX;
    //private double rate;

    private int currentItem = 0;
    
    private Parent root;
    //private AnchorPane root;
	private Stage stage;
	private Scene scene;

	private MainMenu mainMenu;
	private MainScene mainScene;
	private ScreensSwitcher screen;

	//private MarsProjectFX marsProjectFX;
	private transient ThreadPoolExecutor executor;
	private MultiplayerMode multiplayerMode;
	private MainMenuController mainMenuController;

	//private SpinningGlobe spinningGlobe;
	private MenuApp menuApp;

    public MainMenu() {
       	//logger.info("MainMenu's constructor is on " + Thread.currentThread().getName());
    	mainMenu = this;
 	}

    /*
     * Sets up and shows the MainMenu and prepare the stage for MainScene
     */
	@SuppressWarnings("restriction")
	public void initMainMenu(Stage stage) {
	   //logger.info("MainMenu's initAndShowGUI() is on " + Thread.currentThread().getName());
		this.stage = stage;

    	Platform.setImplicitExit(false);
		// Note: If this attribute is true, the JavaFX runtime will implicitly shutdown when the last window is closed;
		// the JavaFX launcher will call the Application.stop method and terminate the JavaFX application thread.
		// If this attribute is false, the application will continue to run normally even after the last window is closed,
		// until the application calls exit. The default value is true.

    	stage.setOnCloseRequest(e -> {
			boolean isExit = exitDialog(stage);
			if (!isExit) {
				e.consume();
			}
			else {
				Platform.exit();
	    		System.exit(0);
			}
		});
    	
/*
       screen = new ScreensSwitcher(this);
       screen.loadScreen(MainMenu.screen1ID, MainMenu.screen1File);
       //screen.loadScreen(MainMenu.screen2ID, MainMenu.screen2File);
       //screen.loadScreen(MainMenu.screen3ID, MainMenu.screen3File);
       screen.setScreen(MainMenu.screen1ID);

       //if (screen.lookup("#menuBox") == null)
		//	System.out.println("Warning: menu option box is not found");

       VBox menuBox = ((VBox) screen.lookup("#menuBox"));
       //VBox menuBox = 
       
       HBox mapBox = ((HBox) screen.lookup("#mapBox"));

       Rectangle rect = new Rectangle(WIDTH+5, HEIGHT+5);//, Color.rgb(179,53,0));//rgb(69, 56, 35));//rgb(86,70,44));//SADDLEBROWN);
       rect.setArcWidth(40);
       rect.setArcHeight(40);
       rect.setEffect(new DropShadow(40, 5, 5, Color.BLACK));//TAN)); // rgb(27,8,0)));// for bottom right edge
	   
       // 2015-11-23 Added StarfieldFX
       StarfieldFX sf = new StarfieldFX();
       Parent starfield = sf.createStars(WIDTH-30, HEIGHT-30);
*/
       menuApp = new MenuApp(mainMenu);
       root = menuApp.createContent();
       
/*
       root = new AnchorPane();
       root.setStyle(//"-fx-border-style: none; "
       			"-fx-background-color: transparent; "
       			+ "-fx-background-radius: 1px;");
       root.setPrefHeight(WIDTH);
       root.setPrefWidth(HEIGHT);

       spinningGlobe = new SpinningGlobe(this);
       Parent globe = spinningGlobe.createDraggingGlobe();   
       //globe.setTranslateX(0);
       globe.setTranslateY(80);
       //mapBox.getChildren().add(globe);     
       menuApp.getRoot().getChildren().addAll(globe);

 
       AnchorPane.setTopAnchor(rect, 0.0);
       AnchorPane.setLeftAnchor(rect, 0.0);
       AnchorPane.setTopAnchor(starfield, 0.0);
       AnchorPane.setLeftAnchor(starfield, 0.0);
       AnchorPane.setTopAnchor(mapBox, 100.0);
       AnchorPane.setLeftAnchor(mapBox, 0.0);
       AnchorPane.setTopAnchor(screen, 0.0);
       AnchorPane.setRightAnchor(screen, 0.0);

       screen.setCache(true);
       starfield.setCache(true);
       screen.setCacheHint(CacheHint.SCALE_AND_ROTATE);
       starfield.setCacheHint(CacheHint.SCALE_AND_ROTATE);

       root.getChildren().addAll(rect, starfield, mapBox, screen);
       screen.toFront();
*/   
       
	   scene = new Scene(root, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED); // Color.DARKGOLDENROD, Color.TAN);//MAROON); //TRANSPARENT);//DARKGOLDENROD);
        // Add keyboard control
	   menuApp.getSpinningGlobe().getGlobe().handleKeyboard(scene);
       // Add mouse control
	   menuApp.getSpinningGlobe().getGlobe().handleMouse(scene);

       scene.setFill(Color.BLACK);//DARKGOLDENROD);//Color.BLACK);

       scene.getStylesheets().add(this.getClass().getResource("/fxui/css/mainmenu.css").toExternalForm());
       //mainMenuScene.setFill(Color.BLACK); // if using Group, a black border will remain
       //mainMenuScene.setFill(Color.TRANSPARENT); // if using Group, a white border will remain
       scene.setCursor(Cursor.HAND);

       // Makes the menu option box fades in
       scene.setOnMouseEntered(new EventHandler<MouseEvent>(){
           public void handle(MouseEvent mouseEvent){
        	   menuApp.startAnimation();
               FadeTransition fadeTransition
                       = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
               fadeTransition.setFromValue(0.0);
               fadeTransition.setToValue(1.0);
               fadeTransition.play();
           }
       });

       // Makes the menu option box fades out
       scene.setOnMouseExited(new EventHandler<MouseEvent>(){
           public void handle(MouseEvent mouseEvent){
        	   menuApp.endAnimation();
               FadeTransition fadeTransition
                       = new FadeTransition(Duration.millis(1000), menuApp.getOptionMenu());
               fadeTransition.setFromValue(1.0);
               fadeTransition.setToValue(0.0);
               fadeTransition.play();
        	   fadeTransition.setOnFinished(e -> menuApp.clearMenuItems());
           }
       });
     
       stage.setResizable(false);
	   stage.setTitle(Simulation.title);
       stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));
       //NOTE: OR use svg file with stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));
       stage.setScene(scene);
       //2016-02-07 Added calling setMonitor()
       setMonitor(stage);
       stage.centerOnScreen();
       stage.show();

   }

	public void createMenuApp() {
		 menuApp = new MenuApp(mainMenu);
	     root = menuApp.createContent();
	}
	
	public Stage getStage() {
		return stage;
	}

   public MainScene getMainScene() {
	   return mainScene;
   }

   public MultiplayerMode getMultiplayerMode() {
	   return multiplayerMode;
	}

   public void runOne() {
	   //logger.info("MainMenu's runOne() is on " + Thread.currentThread().getName());
	   stage.setIconified(true);
	   stage.hide();
	   stage.close();
	   // creates a mainScene instance
	   mainScene = new MainScene();

       try {
    	   // Loads Scenario Config Editor
    	   Simulation.instance().getSimExecutor().execute(new ConfigEditorTask());

       } catch (Exception e) {
    	   e.printStackTrace();
    	   exitWithError("Error : could not create a new simulation ", e);
       }


   }

	public class ConfigEditorTask implements Runnable {
		  public void run() {
			  //logger.info("MarsProjectFX's ConfigEditorTask's run() is on " + Thread.currentThread().getName() );
			  new ScenarioConfigEditorFX(mainMenu); //marsProjectFX,
		  }
	}

   public void runTwo() {
	   //logger.info("MainMenu's runTwo() is on " + Thread.currentThread().getName());
	   scene.setCursor(Cursor.WAIT);

	   stage.setIconified(true);
	   stage.hide();
	   stage.close();

	   loadSim(null);

       scene.setCursor(Cursor.DEFAULT); //Change cursor to default style

   }


   /*
    * Loads the simulation file via the terminal or FileChooser.
    *
    * @param selectedFile the saved sim
    */
   public void loadSim(File selectedFile) {
	   //logger.info("MainMenu's loadSim() is on " + Thread.currentThread().getName());

	   String dir = Simulation.DEFAULT_DIR;
	   String title = null;

	   try {

		   if (selectedFile == null) {

			   FileChooser chooser = new FileChooser();
				// chooser.setInitialFileName(dir);
				// Set to user directory or go to default if cannot access
				// String userDirectoryString = System.getProperty("user.home");
			   File userDirectory = new File(dir);
			   chooser.setInitialDirectory(userDirectory);
			   chooser.setTitle(title); // $NON-NLS-1$

				// Set extension filter
			   FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter(
						"Simulation files (*.sim)", "*.sim");
			   FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter(
						"all files (*.*)", "*.*");

			   chooser.getExtensionFilters().addAll(simFilter, allFilter);

			   // Show open file dialog
			   selectedFile = chooser.showOpenDialog(stage);
		   }

		   else {
	   			// if user wants to load the default saved sim

		   }

	   } catch (NullPointerException e) {
	       System.err.println("NullPointerException in loading sim. " + e.getMessage());
	       Platform.exit();
	       System.exit(1);

	   } catch (Exception e) {
	       System.err.println("Exception in loading sim. " + e.getMessage());
	       Platform.exit();
	       System.exit(1);
	   }

	   if (selectedFile != null) {

			final File fileLocn = selectedFile;

			Platform.runLater(() -> {
				mainScene = new MainScene();
				mainScene.createLoadingIndicator();
				mainScene.showWaitStage(MainScene.LOADING);
			});

			CompletableFuture.supplyAsync(() -> submitTask(fileLocn, false));

		}

		else {
			logger.info("No file was selected. Loading is cancelled");
	        Platform.exit();
	        System.exit(1);
		}

   }

	public int submitTask(File fileLocn, boolean autosaveDefaultName) {
		Simulation.instance().getSimExecutor().execute(new LoadSimulationTask(fileLocn, autosaveDefaultName));
		return 1;
	}

   /*
    * Loads settlement data from a saved sim
    * @param fileLocn user's sim filename
    */
	public class LoadSimulationTask implements Runnable {
		File fileLocn;
		boolean autosaveDefaultName;

		LoadSimulationTask(File fileLocn, boolean autosaveDefaultName){
			this.fileLocn = fileLocn;
			this.autosaveDefaultName = autosaveDefaultName;
		}

		public void run() {
			//logger.info("LoadSimulationTask is on " + Thread.currentThread().getName() + " Thread");

			Simulation sim = Simulation.instance();
   			// Initialize the simulation.
			Simulation.createNewSimulation();

			try {
				// Loading settlement data from the default saved simulation
				sim.loadSimulation(fileLocn); // null means loading "default.sim"

	        } catch (Exception e) {
	            //e.printStackTrace();
	            exitWithError("Error : could not create a new simulation ", e);
	        }

			sim.start(autosaveDefaultName);

			while (sim.getMasterClock() == null)
				try {
					TimeUnit.MILLISECONDS.sleep(200L);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			mainScene.finalizeMainScene();
		}
	}

   public void runThree() {
	   //logger.info("MainMenu's runThree() is on " + Thread.currentThread().getName() + " Thread");
	   Simulation.instance().getSimExecutor().submit(new MultiplayerTask());
	   stage.setIconified(true);//hide();
/*
	   try {
			multiplayerMode = new MultiplayerMode(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
		executor.execute(multiplayerMode.getModeTask());
*/
	    //pool.shutdown();
	     //mainSceneStage.toFront();
   		//modtoolScene = new SettlementScene().createSettlementScene();
	    //stage.setScene(modtoolScene);
	    //stage.show();
   }

	public class MultiplayerTask implements Runnable {
		public void run() {
			try {
				multiplayerMode = new MultiplayerMode(mainMenu);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
			executor.execute(multiplayerMode.getModeTask());
		}
	}

   //public void changeScene(int toscene) {
	   //switch (toscene) {
		   	//case 1:
		   		//scene = new MenuScene().createScene();
		   	//	break;
	   		//case 2:
	   			//Scene scene = mainScene.createMainScene();
	   			//break;
	   		//case 3:
	   			//scene = modtoolScene.createScene(stage);
	   		//	break;
	   		//stage.setScene(scene);
	   //}
   //}

	/*
	* Sets the main menu controller at the start of the Simulation.instance().
	*/
	// 2015-09-27 Added setController()
	public void setController(ControlledScreen myScreenController) {
	   if (mainMenuController == null)
		   if (myScreenController instanceof MainMenuController )
			   mainMenuController = (MainMenuController) myScreenController;
	}

	public void chooseScreen(int num) {

		ObservableList<Screen> screens = Screen.getScreens();//.getScreensForRectangle(xPos, yPos, 1, 1);
    	Screen currentScreen = screens.get(num);
		Rectangle2D rect = currentScreen.getVisualBounds();

		Screen primaryScreen = Screen.getPrimary();
	}

	public void setMonitor(Stage stage) {
		// Issue: how do we tweak mars-sim to run on the "active" monitor as chosen by user ?
		// "active monitor is defined by whichever computer screen the mouse pointer is or where the command console that starts mars-sim.
		// by default MSP runs on the primary monitor (aka monitor 0 as reported by windows os) only.
		// see http://stackoverflow.com/questions/25714573/open-javafx-application-on-active-screen-or-monitor-in-multi-screen-setup/25714762#25714762

/*
		AnchorPane anchor = null;
		if (root == null) {
	       anchor = new AnchorPane();
	       anchor.setPrefWidth(WIDTH);
	       anchor.setPrefHeight(HEIGHT);
		}
*/

		StartUpLocation startUpLoc = new StartUpLocation(WIDTH, HEIGHT);
        double xPos = startUpLoc.getXPos();
        double yPos = startUpLoc.getYPos();
        // Set Only if X and Y are not zero and were computed correctly
     	//ObservableList<Screen> screens = Screen.getScreensForRectangle(xPos, yPos, 1, 1);
     	//ObservableList<Screen> screens = Screen.getScreens();
    	//System.out.println("# of monitors : " + screens.size());

        if (xPos != 0 && yPos != 0) {
            stage.setX(xPos);
            stage.setY(yPos);
            //System.out.println("Monitor 2:    x : " + xPos + "   y : " + yPos);
        } else {
            //System.out.println("calling centerOnScreen()");
            //System.out.println("Monitor 1:    x : " + xPos + "   y : " + yPos);
        }
        stage.centerOnScreen();
	}

	//public Stage getCircleStage() {
	//	return circleStage;
	//}

	//public ScreensSwitcher getScreensSwitcher() {
	//	return screen;
	//}

	public MainMenuController getMainMenuController() {
		return mainMenuController;
	}

	public SpinningGlobe getSpinningGlobe() {
		return menuApp.getSpinningGlobe();
	}

    /**
     * Exit the simulation with an error message.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void exitWithError(String message, Exception e) {
        showError(message, e);
        Platform.exit();
        System.exit(1);
    }

    /**
     * Show a modal error message dialog.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void showError(String message, Exception e) {
        if (e != null) {
            logger.log(Level.SEVERE, message, e);
        }
        else {
            logger.log(Level.SEVERE, message);
        }

        //if (!headless) {
        //	if (!OS.contains("mac"))
        //		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        	// Warning: cannot load the editor in macosx if it was a JDialog
        //}
    }

	public boolean exitDialog(Stage stage) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.initOwner(stage);
		alert.setTitle("Confirmation for Exit");//("Confirmation Dialog");
		alert.setHeaderText("Leaving mars-sim ?");
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setContentText("Note: Yes to exit mars-sim");
		ButtonType buttonTypeYes = new ButtonType("Yes");
		ButtonType buttonTypeNo = new ButtonType("No");
	   	alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
	   	Optional<ButtonType> result = alert.showAndWait();
	   	if (result.get() == buttonTypeYes){
	   		if (mainMenu.getMultiplayerMode() != null)
	   			if (mainMenu.getMultiplayerMode().getChoiceDialog() != null)
	   				mainMenu.getMultiplayerMode().getChoiceDialog().close();
	   		alert.close();
			Platform.exit();
    		System.exit(0);
	   		return true;
	   	} else {
	   		alert.close();
	   	    return false;
	   	}
   	}
    
/*
	public boolean exitDialog() {
		Boolean result = false;
		Label l = new Label("Leaving mars-sim ?");
		HBox hb = new HBox();
		JFXButton b0 = new JFXButton("Yes");
		JFXButton b1 = new JFXButton("No");
		//b0.setPadding(new Insets(2, 2, 2, 2));
		hb.getChildren().addAll(b0, b1);
		HBox.setMargin(b0, new Insets(3,3,3,3));
		HBox.setMargin(b1, new Insets(3,3,3,3));
		VBox vb = new VBox();
		vb.setPadding(new Insets(5, 5, 5, 5));
		vb.getChildren().addAll(l, hb);
		StackPane sp = new StackPane(vb);
		StackPane.setMargin(vb, new Insets(10,10,10,10));
		JFXDialog dialog = new JFXDialog();
		dialog.setDialogContainer();
		dialog.setContent(sp);
		dialog.show();
		
		b0.setOnAction(e -> {
	   		if (mainMenu.getMultiplayerMode() != null)
	   			if (mainMenu.getMultiplayerMode().getChoiceDialog() != null)
	   				mainMenu.getMultiplayerMode().getChoiceDialog().close();
			Platform.exit();
    		System.exit(0);
		});

		b1.setOnAction(e -> {
	   	    e.consume();
		});
		
		return result;
	}
*/	
	public void destroy() {
		root = null;
		screen = null;
		stage = null;
		scene = null;
		screen = null;
		mainMenu = null;
		mainScene = null;
		executor = null;
		multiplayerMode = null;
		mainMenuController = null;
		menuApp = null;
		
	}

}
