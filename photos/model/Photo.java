package photos.model;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Represents a photo in the photo album application.
 * Each photo has a file path, caption, date taken, and a list of tags.
 * Photos can be displayed as thumbnails or full-size images.
 * The date taken is determined by the file's last modified date.
 * 
 * @author Keegan Tu
 */
public class Photo implements Serializable {
    /**
     * Serial version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Absolute file path to the photo on disk
     */
    private String filePath;
    
    /**
     * User-provided caption for the photo
     */
    private String caption;
    
    /**
     * Date the photo was taken (based on file modification date)
     */
    private Date dateTaken;
    
    /**
     * List of tags associated with this photo
     */
    private List<Tag> tags;
    
    /**
     * Thumbnail image (150x150), not serialized
     */
    private transient Object thumbnail;
    
    /**
     * Full-size image, not serialized
     */
    private transient Object fullImage;
    
    /**
     * Creates a new photo from a file.
     * Sets the date taken to the file's last modified date.
     * Initializes with an empty caption and no tags.
     * 
     * @param photoFile the image file to create the photo from
     */
    public Photo(File photoFile) {
        this.filePath = photoFile.getAbsolutePath();
        this.caption = "";
        this.tags = new ArrayList<>();
        this.dateTaken = getLastModifiedDate(photoFile);
        loadImages();
    }
    
    /**
     * Gets the last modified date of a file.
     * Removes milliseconds for simpler date comparisons.
     * 
     * @param file the file to get the date from
     * @return the last modified date without milliseconds
     */
    private Date getLastModifiedDate(File file) {
        long lastModified = file.lastModified();
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            new Date(lastModified).toInstant(), ZoneId.systemDefault());
        // no milliseconds so its easier
        dateTime = dateTime.withNano(0);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Loads thumbnail and full-size images using JavaFX.
     * Uses reflection to avoid compile-time dependency on JavaFX.
     * Falls back to storing file path if JavaFX is not available.
     */
    private void loadImages() {
        try {
            // Use reflection to load JavaFX Image class if available
            Class<?> imageClass = Class.forName("javafx.scene.image.Image");
            
            // thumbnail in smaller version
            String thumbnailUrl = "file:" + filePath;
            this.thumbnail = imageClass.getConstructor(String.class, double.class, double.class, boolean.class, boolean.class)
                .newInstance(thumbnailUrl, 150, 150, true, true);
            
            // load image in fulll view
            String fullImageUrl = "file:" + filePath;
            this.fullImage = imageClass.getConstructor(String.class).newInstance(fullImageUrl);
            
        } catch (Exception e) {
            // JavaFX not available, store file path instead
            System.err.println("JavaFX not available, storing file paths instead: " + e.getMessage());
            this.thumbnail = filePath;
            this.fullImage = filePath;
        }
    }
    
    /**
     * Adds a tag to this photo.
     * Prevents duplicate tags (same name and value).
     * 
     * @param tagName the name of the tag (e.g., "location")
     * @param tagValue the value of the tag (e.g., "New Brunswick")
     * @return true if the tag was added, false if it already exists
     */
    public boolean addTag(String tagName, String tagValue) {
        Tag newTag = new Tag(tagName, tagValue);
        if (tags.contains(newTag)) {
            return false;
        }
        tags.add(newTag);
        return true;
    }
    
    /**
     * Removes a tag from this photo.
     * 
     * @param tagName the name of the tag to remove
     * @param tagValue the value of the tag to remove
     * @return true if the tag was removed, false if it wasn't found
     */
    public boolean removeTag(String tagName, String tagValue) {
        Tag tagToRemove = new Tag(tagName, tagValue);
        return tags.remove(tagToRemove);
    }

    /**
     * Checks if this photo has a specific tag.
     * 
     * @param tagName the tag name to check
     * @param tagValue the tag value to check
     * @return true if the photo has this tag, false otherwise
     */
    public boolean hasTag(String tagName, String tagValue) {
        return tags.contains(new Tag(tagName, tagValue));
    }
    
    /**
     * Gets all tags for this photo.
     * 
     * @return a copy of the tags list
     */
    public List<Tag> getTags() {
        return new ArrayList<>(tags);
    }
    
    /**
     * Gets all tags with a specific tag name.
     * Useful for finding all values of a tag type (e.g., all "person" tags).
     * 
     * @param tagName the tag name to search for
     * @return list of tags matching the tag name
     */
    public List<Tag> getTagsByName(String tagName) {
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getName().equalsIgnoreCase(tagName)) {
                result.add(tag);
            }
        }
        return result;
    }
    
    /**
     * Sets the caption for this photo.
     * 
     * @param caption the caption text, or null for empty caption
     */
    public void setCaption(String caption) {
        this.caption = caption != null ? caption : "";
    }
    
    /**
     * Gets the caption for this photo.
     * 
     * @return the photo caption
     */
    public String getCaption() {
        return caption;
    }
    
    /**
     * Gets the date this photo was taken.
     * 
     * @return a copy of the date taken
     */
    public Date getDateTaken() {
        return new Date(dateTaken.getTime());
    }
    
    /**
     * Gets the thumbnail image for this photo.
     * Loads the image if not already loaded.
     * 
     * @return the thumbnail image object (150x150)
     */
    public Object getThumbnail() {
        if (thumbnail == null) {
            loadImages();
        }
        return thumbnail;
    }

    /**
     * Gets the full-size image for this photo.
     * Loads the image if not already loaded.
     * 
     * @return the full-size image object
     */
    public Object getFullImage() {
        if (fullImage == null) {
            loadImages();
        }
        return fullImage;
    }

    /**
     * Gets the absolute file path of this photo.
     * 
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Gets just the filename (without path) of this photo.
     * 
     * @return the filename
     */
    public String getFileName() {
        return new File(filePath).getName();
    }

    /**
     * Custom serialization method.
     * Uses default serialization (images are transient and not saved).
     * 
     * @param out the output stream
     * @throws IOException if serialization fails
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    /**
     * Custom deserialization method.
     * Reloads images after deserialization since they are transient.
     * 
     * @param in the input stream
     * @throws IOException if deserialization fails
     * @throws ClassNotFoundException if a class cannot be found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loadImages();
    }
    
    /**
     * Checks if this photo equals another object.
     * Photos are equal if they have the same file path.
     * 
     * @param obj the object to compare
     * @return true if the photos have the same file path
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Photo photo = (Photo) obj;
        return filePath.equals(photo.filePath);
    }
    
    /**
     * Generates a hash code for this photo.
     * Based on the file path.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return filePath.hashCode();
    }
    
    /**
     * Returns a string representation of this photo.
     * Includes filename and caption if present.
     * 
     * @return string in format "filename - caption" or just "filename"
     */
    @Override
    public String toString() {
        return getFileName() + (caption.isEmpty() ? "" : " - " + caption);
    }
}
