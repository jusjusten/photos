package photos.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import photos.model.DataManager;
import photos.model.User;
import photos.model.Album;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller for the user dashboard view
 */
public class UserDashboardViewController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private TextField albumNameField;
    
    @FXML
    private Button createAlbumButton;
    
    @FXML
    private Button deleteAlbumButton;
    
    @FXML
    private Button renameAlbumButton;
    
    @FXML
    private Button openAlbumButton;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private ListView<String> albumsListView;
    
    private DataManager dataManager;
    private Stage primaryStage;
    private User currentUser;
    private ObservableList<String> albumsList;

    /**
     * Initializes the controller
     */
    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        albumsList = FXCollections.observableArrayList();
        albumsListView.setItems(albumsList);
    }

    /**
     * Sets the primary stage for scene switching
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Sets the current user and updates the UI
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUI();
    }

    /**
     * Updates the UI with current user data
     */
    private void updateUI() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername());
            refreshAlbumsList();
        }
    }

    /**
     * Handles create album button action
     */
    @FXML
    private void handleCreateAlbum() {
        String albumName = albumNameField.getText().trim();
        
        if (albumName.isEmpty()) {
            showError("Album name cannot be empty");
            return;
        }
        
        if (currentUser.getAlbum(albumName) != null) {
            showError("Album already exists");
            return;
        }

        boolean created = currentUser.createAlbum(albumName);
        if (!created) {
            showError("Failed to create album");
            return;
        }
        dataManager.saveCurrentUser();
        refreshAlbumsList();
        albumNameField.clear();
    }

    /**
     * Handles delete album button action
     */
    @FXML
    private void handleDeleteAlbum() {
        String selectedAlbum = albumsListView.getSelectionModel().getSelectedItem();
        
        if (selectedAlbum == null) {
            showError("Please select an album to delete");
            return;
        }
        
        boolean deleted = currentUser.deleteAlbum(selectedAlbum);
        if (!deleted) {
            showError("Failed to delete album");
            return;
        }
        dataManager.saveCurrentUser();
        refreshAlbumsList();
    }

    /**
     * Handles rename album button action
     */
    @FXML
    private void handleRenameAlbum() {
        String selectedAlbum = albumsListView.getSelectionModel().getSelectedItem();
        String newAlbumName = albumNameField.getText().trim();
        
        if (selectedAlbum == null) {
            showError("Please select an album to rename");
            return;
        }
        
        if (newAlbumName.isEmpty()) {
            showError("New album name cannot be empty");
            return;
        }
        
        if (currentUser.getAlbum(newAlbumName) != null) {
            showError("An album with that name already exists");
            return;
        }

        boolean renamed = currentUser.renameAlbum(selectedAlbum, newAlbumName);
        if (!renamed) {
            showError("Failed to rename album");
            return;
        }
        dataManager.saveCurrentUser();
        refreshAlbumsList();
        albumNameField.clear();
    }

    /**
     * Handles open album button action
     */
    @FXML
    private void handleOpenAlbum() {
        String selectedAlbum = albumsListView.getSelectionModel().getSelectedItem();
        
        if (selectedAlbum == null) {
            showError("Please select an album to open");
            return;
        }
        
        // TODO: Implement album view opening
        showError("Album view not yet implemented");
    }

    /**
     * Handles search button action
     */
    @FXML
    private void handleSearch() {
        // TODO: Implement search functionality
        showError("Search functionality not yet implemented");
    }

    /**
     * Handles logout button action
     */
    @FXML
    private void handleLogout() {
        // Persist current user and clear session
        dataManager.logout();
        try {
            switchToLoginView();
        } catch (IOException e) {
            showError("Error loading login view: " + e.getMessage());
        }
    }

    /**
     * Refreshes the albums list
     */
    private void refreshAlbumsList() {
        albumsList.clear();
        if (currentUser != null) {
            for (Album album : currentUser.getAlbums()) {
                albumsList.add(album.getName());
            }
        }
    }

    /**
     * Switches to login view
     */
    private void switchToLoginView() throws IOException {
        LoginViewController controller = (LoginViewController) SceneManager.loadScene(
            primaryStage, "LoginView.fxml", "Photos Application");
        controller.setPrimaryStage(primaryStage);
    }

    /**
     * Shows error message
     */
    private void showError(String message) {
        // You can implement a proper alert dialog here
        System.err.println("Error: " + message);
    }
}