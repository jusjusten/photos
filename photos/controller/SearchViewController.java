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

/**
 * Controller for the search view.
 * Allows users to search for photos by date range or tags.
 * Supports single tag search and two-tag searches with AND/OR logic.
 * Users can create new albums from search results.
 * 
 * @author Keegan Tu
 */
public class SearchViewController {

    /**
     * Radio button for date range search option
     */
    @FXML
    private RadioButton dateRangeRadio;
    
    /**
     * Radio button for single tag search option
     */
    @FXML
    private RadioButton singleTagRadio;
    
    /**
     * Radio button for two-tag AND search option
     */
    @FXML
    private RadioButton twoTagsAndRadio;
    
    /**
     * Radio button for two-tag OR search option
     */
    @FXML
    private RadioButton twoTagsOrRadio;
    
    /**
     * Toggle group for search type radio buttons
     */
    @FXML
    private ToggleGroup searchTypeGroup;
    
    /**
     * Date picker for search start date
     */
    @FXML
    private DatePicker startDatePicker;
    
    /**
     * Date picker for search end date
     */
    @FXML
    private DatePicker endDatePicker;
    
    /**
     * Container box for date search fields
     */
    @FXML
    private VBox dateSearchBox;
    
    /**
     * Text field for first tag name
     */
    @FXML
    private TextField tagName1Field;
    
    /**
     * Text field for first tag value
     */
    @FXML
    private TextField tagValue1Field;
    
    /**
     * Text field for second tag name
     */
    @FXML
    private TextField tagName2Field;
    
    /**
     * Text field for second tag value
     */
    @FXML
    private TextField tagValue2Field;
    
    /**
     * Container box for tag search fields
     */
    @FXML
    private VBox tagSearchBox;
    
    /**
     * Container box for second tag fields (shown only for two-tag searches)
     */
    @FXML
    private VBox tag2Box;
    
    /**
     * Tile pane for displaying search results as thumbnails
     */
    @FXML
    private TilePane resultsPane;
    
    /**
     * Label showing search results count
     */
    @FXML
    private Label resultsLabel;
    
    /**
     * Text field for entering new album name from search results
     */
    @FXML
    private TextField newAlbumNameField;
    
    /**
     * Button to create album from search results
     */
    @FXML
    private Button createAlbumButton;
    
    /**
     * Data manager instance for saving user data
     */
    private DataManager dataManager;
    
    /**
     * The primary stage for scene transitions
     */
    private Stage primaryStage;
    
    /**
     * The currently logged-in user
     */
    private User currentUser;
    
    /**
     * List of photos matching the current search criteria
     */
    private List<Photo> searchResults;
    
    /**
     * Date formatter for displaying photo dates
     */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Initializes the controller.
     * Sets up default search type, listeners, and disables create album button.
     */
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

    /**
     * Updates the visibility of search input fields based on selected search type.
     * Shows date fields for date range search, tag fields for tag searches.
     * Shows second tag fields only for two-tag searches.
     */
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

    /**
     * Sets the primary stage for this controller.
     * 
     * @param primaryStage the primary stage for scene transitions
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Sets the current user for searching their photos.
     * 
     * @param user the user whose photos will be searched
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Handles the search button action.
     * Determines which search type is selected and calls the appropriate search method.
     */
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
    
    /**
     * Searches for photos within a specified date range.
     * Validates that both dates are selected and start date is before end date.
     */
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
    
    /**
     * Searches for photos with a single tag.
     * Validates that both tag name and value are provided.
     */
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
    
    /**
     * Searches for photos with two tags using AND or OR logic.
     * Validates that both tag names and values are provided.
     * 
     * @param isConjunctive true for AND search (both tags required), false for OR search (either tag)
     */
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
    
    /**
     * Displays the search results as photo thumbnails.
     * Updates the results label with the count and enables/disables create album button.
     */
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
    
    /**
     * Adds a photo thumbnail to the results display.
     * Creates a visual box with the photo image, filename, and date.
     * 
     * @param photo the photo to add to the results
     */
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

    /**
     * Handles the create album from results button action.
     * Creates a new album containing all photos from the current search results.
     */
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
