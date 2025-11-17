/**
 * @author Justin
 */
package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import photos.model.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class PhotoDisplayViewController {

    @FXML
    private ImageView photoImageView;
    
    @FXML
    private Label photoNameLabel;
    
    @FXML
    private Label photoDateLabel;
    
    @FXML
    private TextArea captionArea;
    
    @FXML
    private ListView<String> tagsListView;
    
    @FXML
    private TextField tagNameField;
    
    @FXML
    private TextField tagValueField;
    
    @FXML
    private ComboBox<String> albumComboBox;
    
    private Stage primaryStage;
    private User currentUser;
    private Album currentAlbum;
    private Photo currentPhoto;
    private DataManager dataManager;
    private List<Photo> albumPhotos;
    private int currentPhotoIndex;
    private ObservableList<String> tagsList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        tagsList = FXCollections.observableArrayList();
        tagsListView.setItems(tagsList);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setUserAlbumAndPhoto(User user, Album album, Photo photo) {
        this.currentUser = user;
        this.currentAlbum = album;
        this.currentPhoto = photo;
        this.albumPhotos = album.getPhotos();
        this.currentPhotoIndex = albumPhotos.indexOf(photo);
        
        populateAlbumComboBox();
        updateDisplay();
    }

    private void updateDisplay() {
        if (currentPhoto == null) return;
        
        // Display photo
        try {
            Image image = new Image("file:" + currentPhoto.getFilePath());
            photoImageView.setImage(image);
        } catch (Exception e) {
            showError("Error loading image: " + e.getMessage());
        }
        
        // Display info
        photoNameLabel.setText(currentPhoto.getFileName());
        photoDateLabel.setText("Date: " + dateFormat.format(currentPhoto.getDateTaken()));
        
        // Display caption
        captionArea.setText(currentPhoto.getCaption());
        
        // Display tags
        refreshTagsList();
    }

    private void refreshTagsList() {
        tagsList.clear();
        for (Tag tag : currentPhoto.getTags()) {
            tagsList.add(tag.toString());
        }
    }

    private void populateAlbumComboBox() {
        ObservableList<String> albums = FXCollections.observableArrayList();
        for (Album album : currentUser.getAlbums()) {
            if (!album.getName().equals(currentAlbum.getName())) {
                albums.add(album.getName());
            }
        }
        albumComboBox.setItems(albums);
    }

    @FXML
    private void handlePrevious() {
        if (currentPhotoIndex > 0) {
            currentPhotoIndex--;
            currentPhoto = albumPhotos.get(currentPhotoIndex);
            updateDisplay();
        } else {
            showInfo("Already at first photo");
        }
    }

    @FXML
    private void handleNext() {
        if (currentPhotoIndex < albumPhotos.size() - 1) {
            currentPhotoIndex++;
            currentPhoto = albumPhotos.get(currentPhotoIndex);
            updateDisplay();
        } else {
            showInfo("Already at last photo");
        }
    }

    @FXML
    private void handleSaveCaption() {
        String newCaption = captionArea.getText();
        currentPhoto.setCaption(newCaption);
        dataManager.saveCurrentUser();
        showInfo("Caption saved");
    }

    @FXML
    private void handleAddTag() {
        String tagName = tagNameField.getText().trim();
        String tagValue = tagValueField.getText().trim();
        
        if (tagName.isEmpty() || tagValue.isEmpty()) {
            showError("Please enter both tag name and value");
            return;
        }
        
        boolean added = currentPhoto.addTag(tagName, tagValue);
        
        if (added) {
            dataManager.saveCurrentUser();
            refreshTagsList();
            tagNameField.clear();
            tagValueField.clear();
            showInfo("Tag added");
        } else {
            showError("Tag already exists");
        }
    }

    @FXML
    private void handleRemoveTag() {
        String selectedTag = tagsListView.getSelectionModel().getSelectedItem();
        
        if (selectedTag == null) {
            showError("Please select a tag to remove");
            return;
        }
        
        // Parse tag string (format: "name: value")
        String[] parts = selectedTag.split(": ", 2);
        if (parts.length == 2) {
            boolean removed = currentPhoto.removeTag(parts[0], parts[1]);
            
            if (removed) {
                dataManager.saveCurrentUser();
                refreshTagsList();
                showInfo("Tag removed");
            } else {
                showError("Failed to remove tag");
            }
        }
    }

    @FXML
    private void handleCopyPhoto() {
        String targetAlbumName = albumComboBox.getValue();
        
        if (targetAlbumName == null) {
            showError("Please select an album");
            return;
        }
        
        boolean copied = currentUser.copyPhoto(currentPhoto, currentAlbum.getName(), targetAlbumName);
        
        if (copied) {
            dataManager.saveCurrentUser();
            showInfo("Photo copied to " + targetAlbumName);
        } else {
            showError("Photo already exists in that album");
        }
    }

    @FXML
    private void handleMovePhoto() {
        String targetAlbumName = albumComboBox.getValue();
        
        if (targetAlbumName == null) {
            showError("Please select an album");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Move");
        confirm.setHeaderText("Move Photo");
        confirm.setContentText("Move photo to " + targetAlbumName + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean moved = currentUser.movePhoto(currentPhoto, currentAlbum.getName(), targetAlbumName);
                
                if (moved) {
                    dataManager.saveCurrentUser();
                    showInfo("Photo moved to " + targetAlbumName);
                    handleBack(); // Go back since photo is no longer in this album
                } else {
                    showError("Failed to move photo");
                }
            }
        });
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/AlbumView.fxml"));
            Parent root = loader.load();
            
            AlbumViewController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setUserAndAlbum(currentUser, currentAlbum);
            
            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Error returning to album: " + e.getMessage());
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
        alert.setTitle("Info");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
