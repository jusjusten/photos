package photos.model;

import java.io.*;
import java.util.*;

/**
 * Singleton class that manages all data persistence and user sessions.
 * Handles loading and saving of user data, admin data, and manages the current logged-in user.
 * Ensures data directories exist and handles serialization/deserialization of all data.
 * 
 * @author Keegan Tu
 */
public class DataManager {
    /**
     * Central data manager for application (singleton)
     *
     * @author Justin
     */
    private static final String DATA_DIR = "data";
    
    /**
     * Directory path for storing user-specific data files
     */
    private static final String USERS_DIR = DATA_DIR + "/users";
    
    /**
     * File path for storing admin configuration data
     */
    private static final String ADMIN_FILE = DATA_DIR + "/admin.dat";
    
    /**
     * Singleton instance of DataManager
     */
    private static DataManager instance;
    
    /**
     * Admin object managing user accounts
     */
    private Admin admin;
    
    /**
     * Currently logged-in user (null if admin is logged in)
     */
    private User currentUser;
    
    /**
     * List of all users in the system
     */
    private List<User> users;
    
    /**
     * Private constructor to enforce singleton pattern.
     * Initializes data directories, loads admin data, and loads all users.
     */
    private DataManager() {
        initializeDataDirectories();
        users = new ArrayList<>();
        loadAdmin();
        loadAllUsers();
    }
    
    /**
     * Gets the singleton instance of DataManager.
     * Creates a new instance if one doesn't exist.
     * 
     * @return the singleton DataManager instance
     */
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
    /**
     * Initializes the data directory structure.
     * Creates data and users directories if they don't exist.
     */
    private void initializeDataDirectories() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdir();
        }
        
        File usersDir = new File(USERS_DIR);
        if (!usersDir.exists()) {
            usersDir.mkdir();
        }
    }
    
    /**
     * Loads admin data from disk.
     * If no admin file exists, creates a new admin and initializes the stock user.
     */
    private void loadAdmin() {
        File adminFile = new File(ADMIN_FILE);
        
        if (adminFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(adminFile))) {
                admin = (Admin) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading admin data: " + e.getMessage());
                admin = new Admin();
            }
        } else {
            // First time running, create new admin
            admin = new Admin();
            saveAdmin();
            initializeStockUser();
        }
    }
    
    /**
     * Loads all users from disk.
     * Reads all .dat files in the users directory and deserializes them.
     * Ensures admin user exists in the list and stock user has photos loaded.
     */
    private void loadAllUsers() {
        users = new ArrayList<>();
        File usersDir = new File(USERS_DIR);
        
        if (usersDir.exists() && usersDir.isDirectory()) {
            File[] userFiles = usersDir.listFiles((dir, name) -> name.endsWith(".dat"));
            
            if (userFiles != null) {
                for (File userFile : userFiles) {
                    String username = userFile.getName().replace(".dat", "");
                    User user = User.loadUserData(username);
                    if (user != null) {
                        users.add(user);
                        
                        // Ensure stock user has its album and photos loaded
                        if (username.equalsIgnoreCase(Admin.getStockUsername())) {
                            ensureStockUserPhotos(user);
                        }
                    }
                }
            }
        }
        
        // Always ensure admin user exists in the list
        if (getUserByUsername("admin") == null) {
            User adminUser = new User("admin");
            users.add(adminUser);
        }
    }
    
    /**
     * Ensures the stock user has the stock album and photos loaded.
     * Creates the stock album if it doesn't exist and populates it with photos.
     * 
     * @param stockUser the stock user to configure
     */
    private void ensureStockUserPhotos(User stockUser) {
        // Create stock album if it doesn't exist
        if (stockUser.getAlbum("stock") == null) {
            stockUser.createAlbum("stock");
        }
        
        // Load photos from stock_photos directory
        File stockPhotoDir = new File("stock_photos");
        if (stockPhotoDir.exists() && stockPhotoDir.isDirectory()) {
            File[] photoFiles = stockPhotoDir.listFiles();
            if (photoFiles != null) {
                for (File photoFile : photoFiles) {
                    if (isImageFile(photoFile)) {
                        stockUser.addPhoto(photoFile, "stock");
                    }
                }
            }
        }
        
        // Save the updated stock user
        stockUser.saveUserData();
    }
    
    /**
     * Gets a user by their username.
     * 
     * @param username the username to search for
     * @return the User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Gets a list of all users in the system.
     * 
     * @return a copy of the users list
     */
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }
    
    /**
     * Adds a new user to the system.
     * Saves the user data immediately if the user doesn't already exist.
     * 
     * @param user the user to add
     */
    public void addUser(User user) {
        if (user != null && getUserByUsername(user.getUsername()) == null) {
            users.add(user);
            user.saveUserData();
        }
    }
    
    /**
     * Deletes a user by username.
     * Removes the user from the list and deletes their data file.
     * Cannot delete the admin user.
     * 
     * @param username the username of the user to delete
     */
    public void deleteUser(String username) {
        if (username.equals("admin")) {
            return; // Cannot delete admin
        }
        
        User userToDelete = getUserByUsername(username);
        if (userToDelete != null) {
            users.remove(userToDelete);
            
            // Delete user data file
            File userFile = new File(USERS_DIR + "/" + username + ".dat");
            if (userFile.exists()) {
                userFile.delete();
            }
        }
    }
    
    /**
     * Saves all users' data to disk.
     * Iterates through all users and saves each one.
     */
    public void saveUsers() {
        for (User user : users) {
            user.saveUserData();
        }
    }
    
    /**
     * Saves all data including users and admin.
     * Convenience method that calls saveUsers() and saveAdmin().
     */
    public void saveData() {
        saveUsers();
        saveAdmin();
    }
    
    /**
     * Saves admin data to disk.
     * Serializes the admin object to the admin data file.
     */
    public void saveAdmin() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(ADMIN_FILE))) {
            oos.writeObject(admin);
        } catch (IOException e) {
            System.err.println("Error saving admin data: " + e.getMessage());
        }
    }
    
    /**
     * Initializes the stock user with pre-loaded photos.
     * Creates a stock user with a stock album for testing purposes.
     * This user is created on first run of the application.
     * Loads photos from the stock_photos directory if it exists.
     */
    private void initializeStockUser() {
        User stockUser = new User(Admin.getStockUsername());
        
        // Create stock album
        stockUser.createAlbum("stock");
        
        // Load stock photos from stock_photos directory
        File stockPhotoDir = new File("stock_photos");
        if (stockPhotoDir.exists() && stockPhotoDir.isDirectory()) {
            File[] photoFiles = stockPhotoDir.listFiles();
            if (photoFiles != null) {
                for (File photoFile : photoFiles) {
                    if (isImageFile(photoFile)) {
                        stockUser.addPhoto(photoFile, "stock");
                    }
                }
            }
        }
        
        stockUser.saveUserData();
        users.add(stockUser);
    }
    
    /**
     * Helper method to check if a file is an image file.
     * Supports common image formats: jpg, jpeg, png, gif, bmp.
     * 
     * @param file the file to check
     * @return true if the file is a recognized image format, false otherwise
     */
    private boolean isImageFile(File file) {
        if (!file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".gif") || 
               name.endsWith(".bmp");
    }
    
    /**
     * Logs in a user by username.
     * Sets the current user if login is successful.
     * Admin login sets currentUser to null.
     * 
     * @param username the username to log in
     * @return true if login successful, false if user doesn't exist
     */
    public boolean login(String username) {
        // Admin login
        if (username.equalsIgnoreCase(Admin.getAdminUsername())) {
            currentUser = null; // Admin doesn't have a User object
            return true;
        }
        
        // Check if user exists in admin list
        if (!admin.userExists(username)) {
            return false;
        }
        
        // Load user from disk
        User user = User.loadUserData(username);
        if (user == null) {
            // User exists in admin list but no data file - create new
            user = new User(username);
            user.saveUserData();
        }
        
        currentUser = user;
        return true;
    }
    
    /**
     * Logs out the current user.
     * Saves the current user's data before clearing the session.
     */
    public void logout() {
        if (currentUser != null) {
            currentUser.saveUserData();
            currentUser = null;
        }
    }
    
    /**
     * Checks if admin is currently logged in.
     * Admin is logged in when currentUser is null.
     * 
     * @return true if admin is logged in, false otherwise
     */
    public boolean isAdminLoggedIn() {
        return currentUser == null;
    }
    
    /**
     * Gets the currently logged-in user.
     * 
     * @return the current User object, or null if admin is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Gets the admin object.
     * 
     * @return the Admin instance
     */
    public Admin getAdmin() {
        return admin;
    }
    
    /**
     * Saves the current user's data to disk.
     * Does nothing if no user is logged in or if admin is logged in.
     */
    public void saveCurrentUser() {
        if (currentUser != null) {
            currentUser.saveUserData();
        }
    }
}
