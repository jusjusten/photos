package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import photos.model.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.File;
import java.io.IOException;

/**
 * Controller for the album view.
 * Manages the display and interaction with photos within a specific album.
 * Allows users to add, remove, and view photos in the album.
 * 
 * @author Keegan Tu
 * @author Justin
 */
public class AlbumViewController {

    /**
     * Label displaying the album title and photo count
     */
    @FXML
    private Label albumTitleLabel;
    
    /**
     * Tile pane for displaying photo thumbnails in a grid layout
     */
    @FXML
    private TilePane photoTilePane;
    
    /**
     * The primary stage for scene transitions
     */
    private Stage primaryStage;
    
    /**
     * The currently logged-in user
     */
    private User currentUser;
    
    /**
     * The album currently being displayed
     */
    private Album currentAlbum;
    
    /**
     * Data manager instance for saving user data
     */
    private DataManager dataManager;
    
    /**
     * The currently selected photo (for removal or viewing)
     */
    private Photo selectedPhoto;

    /**
     * Initializes the controller.
     * Sets up the data manager instance.
     */
    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
    }

    /**
     * Sets the primary stage for this controller.
     * 
     * @param primaryStage the primary stage for scene transitions
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Sets the current user and album, then updates the display.
     * 
     * @param user the user viewing the album
     * @param album the album to display
     */
    public void setUserAndAlbum(User user, Album album) {
        this.currentUser = user;
        this.currentAlbum = album;
        updateUI();
    }

    /**
     * Updates the UI with current album information.
     * Displays the album name, photo count, and refreshes the photo grid.
     */
    private void updateUI() {
        if (currentAlbum != null) {
            albumTitleLabel.setText(currentAlbum.getName() + " (" + 
                                   currentAlbum.getPhotoCount() + " photos)");
            refreshPhotosDisplay();
        }
    }

    /**
     * Refreshes the photo display by clearing and reloading all thumbnails.
     */
    private void refreshPhotosDisplay() {
        photoTilePane.getChildren().clear();
        
        for (Photo photo : currentAlbum.getPhotos()) {
            addPhotoThumbnail(photo);
        }
    }

    /**
     * Adds a photo thumbnail to the tile pane.
     * Creates a clickable thumbnail with the photo image and filename.
     * 
     * @param photo the photo to add to the display
     */
    private void addPhotoThumbnail(Photo photo) {
        try {
            // Create container for photo
            VBox photoBox = new VBox(5);
            photoBox.setAlignment(Pos.CENTER);
            photoBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5;");
            
            // Load and display thumbnail
            Image image = new Image("file:" + photo.getFilePath(), 150, 150, true, true);
            ImageView imageView = new ImageView(image);
            
            // Add filename label
            Label nameLabel = new Label(photo.getFileName());
            nameLabel.setMaxWidth(150);
            nameLabel.setWrapText(true);
            nameLabel.setStyle("-fx-font-size: 10;");
            
            photoBox.getChildren().addAll(imageView, nameLabel);
            
            // Make clickable to select
            photoBox.setOnMouseClicked(e -> {
                // Clear previous selection
                photoTilePane.getChildren().forEach(node -> 
                    node.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5;")
                );
                // Highlight selected
                photoBox.setStyle("-fx-border-color: blue; -fx-border-width: 2; -fx-padding: 5;");
                selectedPhoto = photo;
            });
            
            photoTilePane.getChildren().add(photoBox);
            
        } catch (Exception e) {
            System.err.println("Error loading photo: " + e.getMessage());
        }
    }

    /**
     * Handles the add photo button action.
     * Opens a file chooser dialog to select an image file and adds it to the album.
     */
    @FXML
    private void handleAddPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", 
                "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        
        if (selectedFile != null) {
            Photo newPhoto = currentUser.addPhoto(selectedFile, currentAlbum.getName());
            
            if (newPhoto != null) {
                dataManager.saveCurrentUser();
                refreshPhotosDisplay();
                showInfo("Photo added successfully!");
            } else {
                showError("Photo already exists in this album");
            }
        }
    }

    /**
     * Handles the remove photo button action.
     * Removes the currently selected photo from the album after confirmation.
     */
    @FXML
    private void handleRemovePhoto() {
        if (selectedPhoto == null) {
            showError("Please select a photo to remove");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText("Remove Photo");
        confirm.setContentText("Remove '" + selectedPhoto.getFileName() + "' from album?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean removed = currentUser.removePhotoFromAlbum(selectedPhoto, currentAlbum.getName());
                
                if (removed) {
                    dataManager.saveCurrentUser();
                    selectedPhoto = null;
                    refreshPhotosDisplay();
                    showInfo("Photo removed successfully");
                } else {
                    showError("Failed to remove photo");
                }
            }
        });
    }

    /**
     * Handles the view photo button action.
     * Opens the photo display view for the currently selected photo.
     */
    @FXML
    private void handleViewPhoto() {
        if (selectedPhoto == null) {
            showError("Please select a photo to view");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/PhotoDisplayView.fxml"));
            Parent root = loader.load();
            
            PhotoDisplayViewController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setUserAlbumAndPhoto(currentUser, currentAlbum, selectedPhoto);
            
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Error loading photo view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the back button action.
     * Returns to the user dashboard view.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/UserDashboardView.fxml"));
            Parent root = loader.load();
            
            UserDashboardViewController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setCurrentUser(currentUser);
            
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Error returning to dashboard: " + e.getMessage());
        }
    }

    /**
     * Displays an error alert dialog.
     * 
     * @param message the error message to display
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Displays an information alert dialog.
     * 
     * @param message the information message to display
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
