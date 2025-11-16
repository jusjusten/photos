package photos.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import photos.model.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SearchViewController {

    @FXML
    private RadioButton dateRangeRadio;
    
    @FXML
    private RadioButton singleTagRadio;
    
    @FXML
    private RadioButton twoTagsAndRadio;
    
    @FXML
    private RadioButton twoTagsOrRadio;
    
    @FXML
    private ToggleGroup searchTypeGroup;
    
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private VBox dateSearchBox;
    
    @FXML
    private TextField tagName1Field;
    
    @FXML
    private TextField tagValue1Field;
    
    @FXML
    private TextField tagName2Field;
    
    @FXML
    private TextField tagValue2Field;
    
    @FXML
    private VBox tagSearchBox;
    
    @FXML
    private VBox tag2Box;
    
    @FXML
    private TilePane resultsPane;
    
    @FXML
    private Label resultsLabel;
    
    @FXML
    private TextField newAlbumNameField;
    
    @FXML
    private Button createAlbumButton;
    
    private DataManager dataManager;
    private Stage primaryStage;
    private User currentUser;
    private List<Photo> searchResults;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    @FXML
    public void initialize() {
        dataManager = DataManager.getInstance();
        searchResults = null;
        
        // Set default selection
        dateRangeRadio.setSelected(true);
        updateSearchFields();
        
        // Add listeners for radio buttons
        searchTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateSearchFields();
        });
        
        createAlbumButton.setDisable(true);
    }

    private void updateSearchFields() {
        RadioButton selected = (RadioButton) searchTypeGroup.getSelectedToggle();
        
        if (selected == dateRangeRadio) {
            dateSearchBox.setVisible(true);
            dateSearchBox.setManaged(true);
            tagSearchBox.setVisible(false);
            tagSearchBox.setManaged(false);
        } else {
            dateSearchBox.setVisible(false);
            dateSearchBox.setManaged(false);
            tagSearchBox.setVisible(true);
            tagSearchBox.setManaged(true);
            
            if (selected == singleTagRadio) {
                tag2Box.setVisible(false);
                tag2Box.setManaged(false);
            } else {
                tag2Box.setVisible(true);
                tag2Box.setManaged(true);
            }
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleSearch() {
        RadioButton selected = (RadioButton) searchTypeGroup.getSelectedToggle();
        
        try {
            if (selected == dateRangeRadio) {
                searchByDateRange();
            } else if (selected == singleTagRadio) {
                searchBySingleTag();
            } else if (selected == twoTagsAndRadio) {
                searchByTwoTags(true); // AND
            } else if (selected == twoTagsOrRadio) {
                searchByTwoTags(false); // OR
            }
        } catch (Exception e) {
            showError("Search error: " + e.getMessage());
        }
    }
    
    private void searchByDateRange() {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Please select both start and end dates");
            return;
        }
        
        Date startDate = java.sql.Date.valueOf(startDatePicker.getValue());
        Date endDate = java.sql.Date.valueOf(endDatePicker.getValue());
        
        if (startDate.after(endDate)) {
            showError("Start date must be before or equal to end date");
            return;
        }
        
        searchResults = currentUser.searchByDateRange(startDate, endDate);
        displayResults();
    }
    
    private void searchBySingleTag() {
        String tagName = tagName1Field.getText().trim();
        String tagValue = tagValue1Field.getText().trim();
        
        if (tagName.isEmpty() || tagValue.isEmpty()) {
            showError("Please enter both tag name and value");
            return;
        }
        
        TagCriteria criteria = new TagCriteria(tagName, tagValue);
        searchResults = currentUser.searchByTags(criteria);
        displayResults();
    }
    
    private void searchByTwoTags(boolean isConjunctive) {
        String tagName1 = tagName1Field.getText().trim();
        String tagValue1 = tagValue1Field.getText().trim();
        String tagName2 = tagName2Field.getText().trim();
        String tagValue2 = tagValue2Field.getText().trim();
        
        if (tagName1.isEmpty() || tagValue1.isEmpty() || 
            tagName2.isEmpty() || tagValue2.isEmpty()) {
            showError("Please enter both tag names and values");
            return;
        }
        
        TagCriteria criteria = new TagCriteria(
            tagName1, tagValue1,
            tagName2, tagValue2,
            isConjunctive
        );
        
        searchResults = currentUser.searchByTags(criteria);
        displayResults();
    }
    
    private void displayResults() {
        resultsPane.getChildren().clear();
        
        if (searchResults == null || searchResults.isEmpty()) {
            resultsLabel.setText("No photos found");
            createAlbumButton.setDisable(true);
            return;
        }
        
        resultsLabel.setText("Found " + searchResults.size() + " photo(s)");
        createAlbumButton.setDisable(false);
        
        for (Photo photo : searchResults) {
            addPhotoToResults(photo);
        }
    }
    
    private void addPhotoToResults(Photo photo) {
        try {
            VBox photoBox = new VBox(5);
            photoBox.setAlignment(Pos.CENTER);
            photoBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 5;");
            
            Image image = new Image("file:" + photo.getFilePath(), 120, 120, true, true);
            ImageView imageView = new ImageView(image);
            
            Label nameLabel = new Label(photo.getFileName());
            nameLabel.setMaxWidth(120);
            nameLabel.setWrapText(true);
            nameLabel.setStyle("-fx-font-size: 10;");
            
            Label dateLabel = new Label(dateFormat.format(photo.getDateTaken()));
            dateLabel.setStyle("-fx-font-size: 9;");
            
            photoBox.getChildren().addAll(imageView, nameLabel, dateLabel);
            resultsPane.getChildren().add(photoBox);
            
        } catch (Exception e) {
            System.err.println("Error loading photo thumbnail: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateAlbumFromResults() {
        String albumName = newAlbumNameField.getText().trim();
        
        if (albumName.isEmpty()) {
            showError("Please enter an album name");
            return;
        }
        
        if (currentUser.getAlbum(albumName) != null) {
            showError("An album with that name already exists");
            return;
        }
        
        if (searchResults == null || searchResults.isEmpty()) {
            showError("No search results to create album from");
            return;
        }
        
        boolean created = currentUser.createAlbumFromSearch(albumName, searchResults);
        
        if (created) {
            dataManager.saveCurrentUser();
            showInfo("Album '" + albumName + "' created with " + searchResults.size() + " photos");
            newAlbumNameField.clear();
        } else {
            showError("Failed to create album");
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