package photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Admin implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final String ADMIN_USERNAME = "admin";
    private static final String STOCK_USERNAME = "stock";
    
    private List<String> usernames;
    
    public Admin() {
        this.usernames = new ArrayList<>();
        // Stock user is always present
        if (!usernames.contains(STOCK_USERNAME)) {
            usernames.add(STOCK_USERNAME);
        }
    }
    
    // Creates a new user and returns true if successful and false if user already exists
    public boolean createUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmedUsername = username.trim();
        
        // Check for duplicates
        for (String existingUser : usernames) {
            if (existingUser.equalsIgnoreCase(trimmedUsername)) {
                return false;
            }
        }
        
        // Don't allow creating admin user
        if (trimmedUsername.equalsIgnoreCase(ADMIN_USERNAME)) {
            return false;
        }
        
        usernames.add(trimmedUsername);
        
        // Create the actual User object and save it
        User newUser = new User(trimmedUsername);
        newUser.saveUserData();
        
        return true;
    }
    
    // Deletes a user and returns true if successful and false if user does not exist or unable to delete
    public boolean deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        // Cannot delete stock user
        if (username.equalsIgnoreCase(STOCK_USERNAME)) {
            return false;
        }
        
        // Cannot delete admin
        if (username.equalsIgnoreCase(ADMIN_USERNAME)) {
            return false;
        }
        
        // Find and remove user
        boolean removed = usernames.removeIf(u -> u.equalsIgnoreCase(username));
        
        if (removed) {
            // Delete the user's data file
            java.io.File userFile = new java.io.File("data/users/" + username + ".dat");
            userFile.delete();
        }
        
        return removed;
    }
    
    // List all usernames
    public List<String> listUsers() {
        // Return copy to prevent external modification
        return new ArrayList<>(usernames);
    }
    
    // returns true if user exists, false otherwise
    public boolean userExists(String username) {
        for (String existingUser : usernames) {
            if (existingUser.equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get the admin username constant
     * @return "admin"
     */
    public static String getAdminUsername() {
        return ADMIN_USERNAME;
    }
    
    /**
     * Get the stock username constant
     * @return "stock"
     */
    public static String getStockUsername() {
        return STOCK_USERNAME;
    }
}
