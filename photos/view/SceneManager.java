package photos.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Utility class for managing scene loading and switching.
 * Handles FXML loading and controller initialization.
 * @author Justin
 */
public class SceneManager {
    
    private static final String FXML_PATH = "/photos/view/";
    
    /**
     * Loads an FXML file, creates a scene, and switches the stage to display it.
     * 
     * @param primaryStage the stage to update
     * @param fxmlFileName the name of the FXML file (e.g., "LoginView.fxml")
     * @param windowTitle the title for the window
     * @return the controller instance loaded from the FXML file
     * @throws IOException if the FXML file cannot be loaded
     */
    public static Object loadScene(Stage primaryStage, String fxmlFileName, String windowTitle) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(FXML_PATH + fxmlFileName));
        
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle(windowTitle);
        primaryStage.show();
        
        return loader.getController();
    }
}
