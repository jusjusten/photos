package photos.model;

import java.io.*;
import java.util.*;

public class DataManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_DIR = DATA_DIR + "/users";
    private static final String ADMIN_FILE = DATA_DIR + "/admin.dat";
    
    private static DataManager instance;
    private Admin admin;
    private User currentUser;
    private List<User> users; // Added to track all users
    
    private DataManager() {
        initializeDataDirectories();
        loadAdmin();
        loadAllUsers(); // Load all users on startup
    }
    
    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }
    
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
     * Load all users from disk
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
     * Get user by username (added for controllers)
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
     * Get all users (added for controllers)
     */
    public List<User> getUsers() {
        return new ArrayList<>(users);
    }
    
    /**
     * Add a new user (added for controllers)
     */
    public void addUser(User user) {
        if (user != null && getUserByUsername(user.getUsername()) == null) {
            users.add(user);
            user.saveUserData(); // Save immediately
        }
    }
    
    /**
     * Delete user by username (added for controllers)
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
     * Save all users data (added for controllers)
     */
    public void saveUsers() {
        for (User user : users) {
            user.saveUserData();
        }
    }
    
    /**
     * Save all data (added for controllers - alias for saveUsers)
     */
    public void saveData() {
        saveUsers();
        saveAdmin();
    }
    
    public void saveAdmin() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(ADMIN_FILE))) {
            oos.writeObject(admin);
        } catch (IOException e) {
            System.err.println("Error saving admin data: " + e.getMessage());
        }
    }
    
    private void initializeStockUser() {
        User stockUser = new User(Admin.getStockUsername());
        
        // Create stock album
        stockUser.createAlbum("stock");
        
        // TODO: Add your stock photos here
        // Example:
        // File stockPhotoDir = new File("stock_photos");
        // if (stockPhotoDir.exists() && stockPhotoDir.isDirectory()) {
        //     for (File photoFile : stockPhotoDir.listFiles()) {
        //         if (isImageFile(photoFile)) {
        //             stockUser.addPhoto(photoFile, "stock");
        //         }
        //     }
        // }
        
        stockUser.saveUserData();
        users.add(stockUser); // Add to users list
    }
    
    public boolean login(String username) {
        // Admin login
        if (username.equalsIgnoreCase(Admin.getAdminUsername())) {
            currentUser = null; // Admin doesn't have a User object
            return true;
        }
        
        // Check if user exists
        User user = getUserByUsername(username);
        if (user == null && !username.equalsIgnoreCase(Admin.getStockUsername())) {
            return false;
        }
        
        if (user == null && username.equalsIgnoreCase(Admin.getStockUsername())) {
            // Stock user should exist, but if not, create it
            user = new User(Admin.getStockUsername());
            user.createAlbum("stock");
            user.saveUserData();
            users.add(user);
        }
        
        currentUser = user;
        return true;
    }
    
    public void logout() {
        if (currentUser != null) {
            currentUser.saveUserData();
            currentUser = null;
        }
    }
    
    public boolean isAdminLoggedIn() {
        return currentUser == null; // Admin has no User object
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public Admin getAdmin() {
        return admin;
    }
    
    public void saveCurrentUser() {
        if (currentUser != null) {
            currentUser.saveUserData();
        }
    }
    
    private boolean isImageFile(File file) {
        if (!file.isFile()) {
            return false;
        }
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || 
               name.endsWith(".png") || name.endsWith(".gif") || 
               name.endsWith(".bmp");
    }
}