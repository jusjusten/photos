package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private ListView<String> photosListView;
    
    private Stage primaryStage;
    private User currentUser;
    private Album currentAlbum;
    private DataManager dataManager;
    private ObservableList<String> photosList;

    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        photosList = FXCollections.observableArrayList();
        photosListView.setItems(photosList);
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
            albumTitleLabel.setText(currentAlbum.getName());
            refreshPhotosList();
        }
    }

    private void refreshPhotosList() {
        photosList.clear();
        for (Photo photo : currentAlbum.getPhotos()) {
            photosList.add(photo.getFileName());
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
                refreshPhotosList();
                showInfo("Photo added successfully!");
            } else {
                showError("Photo already exists in this album");
            }
        }
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
}