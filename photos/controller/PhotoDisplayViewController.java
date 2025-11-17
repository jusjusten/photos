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

/**
 * Controller for the photo display view.
 * Displays a full-size photo with details including caption, date, and tags.
 * Allows users to edit captions, manage tags, navigate between photos,
 * and copy or move photos to other albums.
 * 
 * @author Keegan Tu
 */
public class PhotoDisplayViewController {

    /**
     * ImageView for displaying the full-size photo
     */
    @FXML
    private ImageView photoImageView;
    
    /**
     * Label displaying the photo filename
     */
    @FXML
    private Label photoNameLabel;
    
    /**
     * Label displaying the photo date
     */
    @FXML
    private Label photoDateLabel;
    
    /**
     * Text area for editing photo caption
     */
    @FXML
    private TextArea captionArea;
    
    /**
     * ListView displaying all tags for the photo
     */
    @FXML
    private ListView<String> tagsListView;
    
    /**
     * Text field for entering tag name
     */
    @FXML
    private TextField tagNameField;
    
    /**
     * Text field for entering tag value
     */
    @FXML
    private TextField tagValueField;
    
    /**
     * ComboBox for selecting target album for copy/move operations
     */
    @FXML
    private ComboBox<String> albumComboBox;
    
    /**
     * The primary stage for scene transitions
     */
    private Stage primaryStage;
    
    /**
     * The currently logged-in user
     */
    private User currentUser;
    
    /**
     * The album containing the current photo
     */
    private Album currentAlbum;
    
    /**
     * The photo currently being displayed
     */
    private Photo currentPhoto;
    
    /**
     * Data manager instance for saving user data
     */
    private DataManager dataManager;
    
    /**
     * List of all photos in the current album for navigation
     */
    private List<Photo> albumPhotos;
    
    /**
     * Index of the current photo in the album
     */
    private int currentPhotoIndex;
    
    /**
     * Observable list of tag strings for the ListView
     */
    private ObservableList<String> tagsList;
    
    /**
     * Date formatter for displaying photo dates
     */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    /**
     * Initializes the controller.
     * Sets up the data manager and tags list.
     */
    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        tagsList = FXCollections.observableArrayList();
        tagsListView.setItems(tagsList);
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
     * Sets the current user, album, and photo to display.
     * Updates the display with the photo information.
     * 
     * @param user the user viewing the photo
     * @param album the album containing the photo
     * @param photo the photo to display
     */
    public void setUserAlbumAndPhoto(User user, Album album, Photo photo) {
        this.currentUser = user;
        this.currentAlbum = album;
        this.currentPhoto = photo;
        this.albumPhotos = album.getPhotos();
        this.currentPhotoIndex = albumPhotos.indexOf(photo);
        
        populateAlbumComboBox();
        updateDisplay();
    }

    /**
     * Updates the display with current photo information.
     * Shows the photo image, filename, date, caption, and tags.
     */
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

    /**
     * Refreshes the tags list display.
     * Clears and reloads all tags from the current photo.
     */
    private void refreshTagsList() {
        tagsList.clear();
        for (Tag tag : currentPhoto.getTags()) {
            tagsList.add(tag.toString());
        }
    }

    /**
     * Populates the album combo box with all albums except the current one.
     * Used for copy and move operations.
     */
    private void populateAlbumComboBox() {
        ObservableList<String> albums = FXCollections.observableArrayList();
        for (Album album : currentUser.getAlbums()) {
            if (!album.getName().equals(currentAlbum.getName())) {
                albums.add(album.getName());
            }
        }
        albumComboBox.setItems(albums);
    }

    /**
     * Handles the previous button action.
     * Navigates to the previous photo in the album.
     */
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

    /**
     * Handles the next button action.
     * Navigates to the next photo in the album.
     */
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

    /**
     * Handles the save caption button action.
     * Saves the caption text to the current photo.
     */
    @FXML
    private void handleSaveCaption() {
        String newCaption = captionArea.getText();
        currentPhoto.setCaption(newCaption);
        dataManager.saveCurrentUser();
        showInfo("Caption saved");
    }

    /**
     * Handles the add tag button action.
     * Adds a new tag to the current photo.
     * Validates that both tag name and value are provided.
     */
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

    /**
     * Handles the remove tag button action.
     * Removes the selected tag from the current photo.
     */
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

    /**
     * Handles the copy photo button action.
     * Copies the current photo to the selected album.
     */
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

    /**
     * Handles the move photo button action.
     * Moves the current photo to the selected album after confirmation.
     * Returns to album view after successful move.
     */
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

    /**
     * Handles the back button action.
     * Returns to the album view.
     */
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
        alert.setTitle("Info");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
