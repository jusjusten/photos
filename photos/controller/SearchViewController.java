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
                tag2Box.
