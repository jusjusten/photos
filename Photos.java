package photos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import photos.model.DataManager;

/**
 * Main application class for Photos
 */
public class Photos extends Application {

    private Stage primaryStage;
    private DataManager dataManager;

    /**
     * Main method
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start method - initializes the application
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Photos Application");
        
        // Initialize data manager
        dataManager = DataManager.getInstance();
        dataManager.loadData();
        
        // Show login screen
        showLoginView();
    }

    /**
     * Shows the login view
     */
    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/LoginView.fxml"));
            Parent root = loader.load();
            
            LoginViewController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setMainApp(this);
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading login view: " + e.getMessage());
        }
    }

    /**
     * Shows the admin view
     */
    public void showAdminView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/AdminView.fxml"));
            Parent root = loader.load();
            
            AdminViewController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setMainApp(this);
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading admin view: " + e.getMessage());
        }
    }

    /**
     * Shows the user dashboard view
     */
    public void showUserDashboard(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/UserDashboardView.fxml"));
            Parent root = loader.load();
            
            UserDashboardViewController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            controller.setMainApp(this);
            controller.setCurrentUser(dataManager.getUser(username));
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading user dashboard: " + e.getMessage());
        }
    }

    /**
     * Stop method - saves data when application closes
     */
    @Override
    public void stop() {
        if (dataManager != null) {
            dataManager.saveData();
        }
    }

    /**
     * Gets the data manager instance
     */
    public DataManager getDataManager() {
        return dataManager;
    }
}