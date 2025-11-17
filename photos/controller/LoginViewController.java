/**
 * @author Justin
 */
package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import photos.model.DataManager;
import photos.model.User;
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
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
    }

    /**
     * Sets the primary stage for scene switching
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Handles login button action
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            showError("Username cannot be empty");
            return;
        }
        
        Admin admin = dataManager.getAdmin();

        // If user doesn't exist in admin list, create it first
        if (!username.equalsIgnoreCase(Admin.getAdminUsername()) &&
            !admin.userExists(username)) {
            boolean created = admin.createUser(username);
            if (!created) {
                showError("Failed to create user");
                return;
            }
            dataManager.saveAdmin();
        }

        boolean loggedIn = dataManager.login(username);
        if (!loggedIn) {
            showError("Login failed: user does not exist");
            return;
        }

        // If admin is logged in, DataManager sets currentUser to null
        if (dataManager.isAdminLoggedIn()) {
            try {
                switchToAdminView();
            } catch (IOException e) {
                showError("Error loading admin view: " + e.getMessage());
            }
        } else {
            User user = dataManager.getCurrentUser();
            try {
                switchToUserDashboard(user);
            } catch (IOException e) {
                showError("Error loading user dashboard: " + e.getMessage());
            }
        }
    }

    /**
     * Handles quit button action
     */
    @FXML
    private void handleQuit() {
        dataManager.saveAdmin();
        dataManager.saveCurrentUser();
        primaryStage.close();
    }

    /**
     * Switches to admin view
     */
    private void switchToAdminView() throws IOException {
        AdminViewController controller = (AdminViewController) SceneManager.loadScene(
            primaryStage, "AdminView.fxml", "Admin Panel");
        controller.setPrimaryStage(primaryStage);
    }

    /**
     * Switches to user dashboard view
     */
    private void switchToUserDashboard(User user) throws IOException {
        UserDashboardViewController controller = (UserDashboardViewController) SceneManager.loadScene(
            primaryStage, "UserDashboardView.fxml", "User Dashboard - " + user.getUsername());
        controller.setPrimaryStage(primaryStage);
        controller.setCurrentUser(user);
    }

    /**
     * Shows error message
     */
    private void showError(String message) {
        // You can implement a proper alert dialog here
        System.err.println("Error: " + message);
        // For now, just clear the fields
        usernameField.clear();
        passwordField.clear();
        usernameField.requestFocus();
    }
}