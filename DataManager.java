//package photos.model;

import java.io.*;
import java.util.List;

public class DataManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_DIR = DATA_DIR + "/users";
    private static final String ADMIN_FILE = DATA_DIR + "/admin.dat";
    
    private static DataManager instance;
    private Admin admin;
    private User currentUser;
    
    private DataManager() {
        initializeDataDirectories();
        loadAdmin();
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
    }
    
    public boolean login(String username) {
        // Admin login
        if (username.equalsIgnoreCase(Admin.getAdminUsername())) {
            currentUser = null; // Admin doesn't have a User object
            return true;
        }
        
        // Check if user exists
        if (!admin.userExists(username) && 
            !username.equalsIgnoreCase(Admin.getStockUsername())) {
            return false;
        }
        
        // Load user data
        User user = User.loadUserData(username);
        
        if (user == null) {
            // User exists but no data file, create new
            user = new User(username);
            user.saveUserData();
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
