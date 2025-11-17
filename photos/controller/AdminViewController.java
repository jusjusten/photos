package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import photos.model.DataManager;
import photos.view.SceneManager;
import photos.model.Admin;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller for the admin view
 *
 * @author Justin
 */
public class AdminViewController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private Button createUserButton;
    
    @FXML
    private Button deleteUserButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private ListView<String> usersListView;
    
    private DataManager dataManager;
    private Stage primaryStage;
    private ObservableList<String> usersList;

    /**
     * Initializes the controller and sets up the user list.
     * Called automatically by JavaFX after FXML loading.
     */
    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        usersList = FXCollections.observableArrayList();
        usersListView.setItems(usersList);
        refreshUsersList();
    }

    /**
     * Sets the primary stage for scene switching.
     * @param primaryStage the main application stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Handles the create user button action.
     * Creates a new user if the username is valid and not a duplicate.
     */
    @FXML
    private void handleCreateUser() {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            showError("Username cannot be empty");
            return;
        }
        
        if (username.equalsIgnoreCase(Admin.getAdminUsername())) {
            showError("Cannot create user 'admin'");
            return;
        }
        Admin admin = dataManager.getAdmin();
        if (admin.userExists(username)) {
            showError("User already exists");
            return;
        }

        boolean created = admin.createUser(username);
        if (!created) {
            showError("Failed to create user");
            return;
        }
        dataManager.saveAdmin();
        refreshUsersList();
        usernameField.clear();
    }

    /**
     * Handles the delete user button action.
     * Deletes the selected user if allowed.
     */
    @FXML
    private void handleDeleteUser() {
        String selectedUser = usersListView.getSelectionModel().getSelectedItem();
        
        if (selectedUser == null) {
            showError("Please select a user to delete");
            return;
        }
        
        if (selectedUser.equalsIgnoreCase(Admin.getAdminUsername())) {
            showError("Cannot delete admin user");
            return;
        }
        Admin admin = dataManager.getAdmin();
        boolean deleted = admin.deleteUser(selectedUser);
        if (!deleted) {
            showError("Failed to delete user");
            return;
        }
        dataManager.saveAdmin();
        refreshUsersList();
    }

    /**
     * Handles the logout button action.
     * Returns to the login view.
     */
    @FXML
    private void handleLogout() {
        try {
            switchToLoginView();
        } catch (IOException e) {
            showError("Error loading login view: " + e.getMessage());
        }
    }

    /**
     * Refreshes the users list from the admin model.
     */
    private void refreshUsersList() {
        usersList.clear();
        Admin admin = dataManager.getAdmin();
        for (String username : admin.listUsers()) {
            usersList.add(username);
        }
    }

    /**
     * Switches to the login view scene.
     * @throws IOException if FXML loading fails
     */
    private void switchToLoginView() throws IOException {
        LoginViewController controller = (LoginViewController) SceneManager.loadScene(
            primaryStage, "LoginView.fxml", "Photos Application");
        controller.setPrimaryStage(primaryStage);
    }

    /**
     * Shows an error message (console only).
     * @param message the error message to display
     */
    private void showError(String message) {
        // You can implement a proper alert dialog here
        System.err.println("Error: " + message);
    }
}