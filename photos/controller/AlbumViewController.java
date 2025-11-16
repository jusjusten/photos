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

public class AlbumViewController {

    @FXML
    private Label albumTitleLabel;
    
    @FXML
    private TilePane photoTilePane;
    
    private Stage primaryStage;
    private User currentUser;
    private Album currentAlbum;
    private DataManager dataManager;
    private Photo selectedPhoto;

    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setUserAndAlbum(User user, Album album) {
        this.currentUser = user;
        this.currentAlbum = album;
        updateUI();
    }

    private void updateUI() {
        if (currentAlbum != null) {
            albumTitleLabel.setText(currentAlbum.getName() + " (" + 
                                   currentAlbum.getPhotoCount() + " photos)");
            refreshPhotosDisplay();
        }
    }

    private void refreshPhotosDisplay() {
        photoTilePane.getChildren().clear();
        
        for (Photo photo : currentAlbum.getPhotos()) {
            addPhotoThumbnail(photo);
        }
    }

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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }

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

}

