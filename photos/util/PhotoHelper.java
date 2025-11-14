package photos.util;

import photos.model.Photo;
import javafx.scene.image.Image;

/**
 * Helper class for JavaFX image operations
 * @author Your Name
 */
public class PhotoHelper {
    
    /**
     * Gets JavaFX Image for thumbnail display
     * @param photo the photo object
     * @return JavaFX Image for thumbnail
     */
    public static Image getThumbnailImage(Photo photo) {
        try {
            return new Image("file:" + photo.getFilePath(), 150, 150, true, true);
        } catch (Exception e) {
            System.err.println("Error loading thumbnail: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets JavaFX Image for full display
     * @param photo the photo object
     * @return JavaFX Image for full display
     */
    public static Image getFullImage(Photo photo) {
        try {
            return new Image("file:" + photo.getFilePath());
        } catch (Exception e) {
            System.err.println("Error loading full image: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Checks if file is a supported image format
     * @param file the file to check
     * @return true if supported image format
     */
    public static boolean isSupportedImageFormat(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        
        String name = file.getName().toLowerCase();
        return name.endsWith(".bmp") || 
               name.endsWith(".gif") || 
               name.endsWith(".jpeg") || 
               name.endsWith(".jpg") || 
               name.endsWith(".png");
    }
}