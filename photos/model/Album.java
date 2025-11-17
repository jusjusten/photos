/**
 * @author Justin
 */
package photos.model;

import java.io.*;
import java.util.*;

/**
 * Represents a photo album containing multiple photos.
 * Each album has a unique name and tracks the date range of its photos.
 * Albums maintain references to Photo objects, allowing photos to exist in multiple albums.
 * 
 * @author Keegan Tu
 */
public class Album implements Serializable {
    /**
     * Serial version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * The name of this album
     */
    private String name;
    
    /**
     * List of photos in this album (references, not copies)
     */
    private List<Photo> photos;
    
    /**
     * Earliest photo date in this album
     */
    private Date startDate;
    
    /**
     * Latest photo date in this album
     */
    private Date endDate;
    
    /**
     * Creates a new album with the specified name.
     * The album is initially empty with no date range.
     * 
     * @param name the name of the album
     */
    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
        this.startDate = null;
        this.endDate = null;
    }
    
    /**
     * Adds a photo to this album.
     * Duplicate photos (same file path) are not allowed.
     * Updates the album's date range after adding.
     * 
     * @param photo the photo to add
     * @return true if the photo was added, false if it already exists in the album
     */
    public boolean addPhoto(Photo photo) {
        if (photos.contains(photo)) {
            return false;
        }
        photos.add(photo);
        updateDateRange();
        return true;
    }
    
    /**
     * Removes a photo from this album.
     * Updates the album's date range after removal.
     * 
     * @param photo the photo to remove
     * @return true if the photo was removed, false if it wasn't in the album
     */
    public boolean removePhoto(Photo photo) {
        boolean removed = photos.remove(photo);
        if (removed) {
            updateDateRange();
        }
        return removed;
    }
    
    /**
     * Checks if this album contains a specific photo.
     * 
     * @param photo the photo to check for
     * @return true if the album contains the photo, false otherwise
     */
    public boolean containsPhoto(Photo photo) {
        return photos.contains(photo);
    }
    
    /**
     * Gets a photo at a specific index.
     * 
     * @param index the index of the photo (0-based)
     * @return the Photo at the index, or null if index is out of bounds
     */
    public Photo getPhoto(int index) {
        if (index >= 0 && index < photos.size()) {
            return photos.get(index);
        }
        return null;
    }
    
    /**
     * Gets all photos in this album.
     * 
     * @return a copy of the photos list
     */
    public List<Photo> getPhotos() {
        return new ArrayList<>(photos);
    }
    
    /**
     * Gets the number of photos in this album.
     * 
     * @return the photo count
     */
    public int getPhotoCount() {
        return photos.size();
    }
    
    /**
     * Updates the date range based on all photos in the album.
     * Sets startDate to the earliest photo date and endDate to the latest.
     * Sets both to null if the album is empty.
     */
    public void updateDateRange() {
        if (photos.isEmpty()) {
            startDate = null;
            endDate = null;
            return;
        }
        
        startDate = photos.get(0).getDateTaken();
        endDate = photos.get(0).getDateTaken();
        
        for (Photo photo : photos) {
            Date photoDate = photo.getDateTaken();
            if (photoDate.before(startDate)) {
                startDate = photoDate;
            }
            if (photoDate.after(endDate)) {
                endDate = photoDate;
            }
        }
    }
    
    /**
     * Sets the name of this album.
     * 
     * @param name the new album name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the name of this album.
     * 
     * @return the album name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the earliest photo date in this album.
     * 
     * @return a copy of the start date, or null if album is empty
     */
    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }
    
    /**
     * Gets the latest photo date in this album.
     * 
     * @return a copy of the end date, or null if album is empty
     */
    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }
    
    /**
     * Gets a string representation of the date range.
     * 
     * @return "No photos" if empty, single date if all photos same date,
     *         or "startDate to endDate" for a range
     */
    public String getDateRangeString() {
        if (startDate == null || endDate == null) {
            return "No photos";
        }
        
        if (startDate.equals(endDate)) {
            return startDate.toString();
        } else {
            return startDate.toString() + " to " + endDate.toString();
        }
    }
    
    /**
     * Checks if this album equals another object.
     * Albums are equal if they have the same name (case-insensitive).
     * 
     * @param obj the object to compare
     * @return true if the albums have the same name
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Album album = (Album) obj;
        return name.equalsIgnoreCase(album.name);
    }
    
    /**
     * Generates a hash code for this album.
     * Based on the lowercase album name.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
    
    /**
     * Returns a string representation of this album.
     * 
     * @return string in format "name (X photos)"
     */
    @Override
    public String toString() {
        return name + " (" + getPhotoCount() + " photos)";
    }
}
