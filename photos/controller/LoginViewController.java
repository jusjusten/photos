/**
 * @author Justin
 */
package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import photos.model.DataManager;
import photos.model.User;
// idk if i should keep this but its staying for now
import photos.model.Admin;
import photos.view.SceneManager;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller for the login view
 */
public class LoginViewController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private Button quitButton;
    
    private DataManager dataManager;
    private Stage primaryStage;

    /**
     * Initializes the controller and sets up the data manager.
     * Called automatically by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
    }

    /**
     * Sets the primary stage for scene switching.
     * @param primaryStage the main application stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Handles the login button action.
     * Validates username, creates user if needed, and switches view.
     */
    @FXML
private void handleLogin() {
    String username = usernameField.getText().trim();
    
    if (username.isEmpty()) {
        showError("Username cannot be empty");
        return;
    }
    
    boolean loggedIn = dataManager.login(username);
    if (!loggedIn) {
        showError("User not found. Please contact admin to create an account.");
        return;
    }

    // If admin is logged in
    if (dataManager.isAdminLoggedIn()) {
        try {
            switchToAdminView();
        } catch (IOException e) {
            showError("Error loading admin view: " + e.getMessage());
        }
    } else {
        // Regular user logged in
        User user = dataManager.getCurrentUser();
        try {
            switchToUserDashboard(user);
        } catch (IOException e) {
            showError("Error loading user dashboard: " + e.getMessage());
        }
    }
}

    /**
     * Handles the quit button action.
     * Saves data and closes the application.
     */
    @FXML
    private void handleQuit() {
        dataManager.saveAdmin();
        dataManager.saveCurrentUser();
        primaryStage.close();
    }

    /**
     * Switches to the admin view scene.
     * @throws IOException if FXML loading fails
     */
    private void switchToAdminView() throws IOException {
        AdminViewController controller = (AdminViewController) SceneManager.loadScene(
            primaryStage, "AdminView.fxml", "Admin Panel");
        controller.setPrimaryStage(primaryStage);
    }

    /**
     * Switches to the user dashboard view scene.
     * @param user the User object to pass to dashboard
     * @throws IOException if FXML loading fails
     */
    private void switchToUserDashboard(User user) throws IOException {
        UserDashboardViewController controller = (UserDashboardViewController) SceneManager.loadScene(
            primaryStage, "UserDashboardView.fxml", "User Dashboard - " + user.getUsername());
        controller.setPrimaryStage(primaryStage);
        controller.setCurrentUser(user);
    }

    /**
     * Displays an error alert dialog.
     * Clears input fields and refocuses the username field after the alert is dismissed.
     * 
     * @param message the error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        
        // Clear fields after showing error
        usernameField.clear();
        passwordField.clear();
        usernameField.requestFocus();
    }
}
