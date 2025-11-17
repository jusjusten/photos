/**
 * @author Justin
 */
package photos.model;

import java.io.*;
import java.util.*;
// import java.time.LocalDateTime;
// import java.time.ZoneId;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private List<Album> albums;
    // FIXED: use Map<String, String> because values are "single" or "multiple"
    private Map<String, String> tagTypes; // tag name -> "single" or "multiple"
    private transient Map<String, Photo> allPhotos; // filePath -> Photo (not serialized)
    
    // user-level tags
    private List<Tag> tags;

    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
        this.tagTypes = new HashMap<>();
        this.allPhotos = new HashMap<>();
        this.tags = new ArrayList<>();
        initializeDefaultTagTypes();
    }
    
    private void initializeDefaultTagTypes() {
        tagTypes.put("location", "single");
        tagTypes.put("person", "multiple");
        tagTypes.put("event", "single");
    }
    
    /**
     * Creates a new album for this user.
     * @param albumName the name of the album to create
     * @return true if album was created, false if it already exists
     */
    public boolean createAlbum(String albumName) {
        if (getAlbum(albumName) != null) {
            return false; 
        }
        Album newAlbum = new Album(albumName);
        albums.add(newAlbum);
        return true;
    }
    /**
     * Deletes an album by name.
     * @param albumName the name of the album to delete
     * @return true if album was deleted, false if not found
     */
    public boolean deleteAlbum(String albumName) {
        Album album = getAlbum(albumName);
        if (album != null) {
            albums.remove(album);
            return true;
        }
        return false;
    }
    
    /**
     * Renames an album.
     * @param oldName the current album name
     * @param newName the new album name
     * @return true if renamed, false if new name exists or old not found
     */
    public boolean renameAlbum(String oldName, String newName) {
        if (getAlbum(newName) != null) {
            // New name already exists
            return false; 
        }
        
        Album album = getAlbum(oldName);
        if (album != null) {
            album.setName(newName);
            return true;
        }
        return false;
    }
    /**
     * Gets an album by name (case-insensitive).
     * @param albumName the album name
     * @return the Album object, or null if not found
     */
    public Album getAlbum(String albumName) {
        for (Album album : albums) {
            if (album.getName().equalsIgnoreCase(albumName)) {
                return album;
            }
        }
        return null;
    }
    
    /**
     * Gets all albums for this user.
     * @return a copy of the albums list
     */
    public List<Album> getAlbums() {
        return new ArrayList<>(albums);
    }
    
    /**
     * Adds a photo to an album, creating a Photo object if needed.
     * @param photoFile the image file
     * @param albumName the album to add to
     * @return the Photo object if added, null if album not found or duplicate
     */
    public Photo addPhoto(File photoFile, String albumName) {
        Album album = getAlbum(albumName);
        if (album == null) {
            return null;
        }
        
        String filePath = photoFile.getAbsolutePath();
        Photo photo = allPhotos.get(filePath);
        
        if (photo == null) {
            photo = new Photo(photoFile);
            allPhotos.put(filePath, photo);
        }
        
        if (album.addPhoto(photo)) {
            return photo;
        }
        return null;
    }
    
    /**
     * Removes a photo from an album.
     * @param photo the photo to remove
     * @param albumName the album name
     * @return true if removed, false if album not found
     */
    public boolean removePhotoFromAlbum(Photo photo, String albumName) {
        Album album = getAlbum(albumName);
        if (album != null) {
            return album.removePhoto(photo);
        }
        return false;
    }
    
    /**
     * Gets all photos across all albums (cached).
     * @return a set of all Photo objects
     */
    public Set<Photo> getAllPhotos() {
        return new HashSet<>(allPhotos.values());
    }
    
    /**
     * Searches photos by date range (inclusive).
     * @param start the start date
     * @param end the end date
     * @return list of matching Photo objects
     */
    public List<Photo> searchByDateRange(Date start, Date end) {
        List<Photo> results = new ArrayList<>();
        for (Photo photo : getAllPhotos()) {
            Date photoDate = photo.getDateTaken();
            if (!photoDate.before(start) && !photoDate.after(end)) {
                results.add(photo);
            }
        }
        return results;
    }
    
    /**
     * Searches photos by tag criteria.
     * @param criteria the TagCriteria to match
     * @return list of matching Photo objects
     */
    public List<Photo> searchByTags(TagCriteria criteria) {
        List<Photo> results = new ArrayList<>();
        for (Photo photo : getAllPhotos()) {
            if (criteria.matches(photo)) {
                results.add(photo);
            }
        }
        return results;
    }
    
    /**
     * Adds a new tag type for this user.
     * @param tagName the tag name
     * @param isSingleValue true for single-value, false for multi-value
     * @return true if added, false if already exists
     */
    public boolean addTagType(String tagName, boolean isSingleValue) {
        if (tagName == null) return false;
        String key = tagName.toLowerCase();
        if (tagTypes.containsKey(key)) {
            return false;
        }
        tagTypes.put(key, isSingleValue ? "single" : "multiple");
        return true;
    }
    
    // FIXED: return Map<String, String> to match tagTypes type
    /**
     * Gets all tag types and their cardinality.
     * @return a copy of the tagTypes map
     */
    public Map<String, String> getTagTypes() {
        return new HashMap<>(tagTypes);
    }
    
    // --------------------------
    // user-level tag management
    // --------------------------
    /**
     * Adds a user-level tag.
     * @param tag the Tag to add
     * @return true if added, false if duplicate or null
     */
    public boolean addTag(Tag tag) {
        if (tag == null) return false;
        if (!tags.contains(tag)) {
            tags.add(tag);
            return true;
        }
        return false;
    }

    /**
     * Removes a user-level tag.
     * @param tag the Tag to remove
     * @return true if removed, false if not found or null
     */
    public boolean removeTag(Tag tag) {
        if (tag == null) return false;
        return tags.remove(tag);
    }

    /**
     * Gets all user-level tags.
     * @return a copy of the tags list
     */
    public List<Tag> getTags() {
        return new ArrayList<>(tags);
    }

    /**
     * Gets all tags with a specific name (case-insensitive).
     * @param tagName the tag name
     * @return list of matching Tag objects
     */
    public List<Tag> getTagsByName(String tagName) {
        List<Tag> result = new ArrayList<>();
        if (tagName == null) return result;
        for (Tag tag : tags) {
            if (tag.getName().equalsIgnoreCase(tagName)) {
                result.add(tag);
            }
        }
        return result;
    }

    /**
     * Removes all user-level tags.
     */
    public void clearTags() {
        tags.clear();
    }
    // --------------------------
    
    /**
     * Gets the username for this user.
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.allPhotos = new HashMap<>();
        if (albums != null) {
            for (Album album : albums) {
                if (album.getPhotos() != null) {
                    for (Photo photo : album.getPhotos()) {
                        allPhotos.put(photo.getFilePath(), photo);
                    }
                }
            }
        }
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        if (this.tagTypes == null) {
            this.tagTypes = new HashMap<>();
            initializeDefaultTagTypes();
        }
    }
    
    /**
     * Saves this user's data to disk.
     */
    public void saveUserData() {
        try {
            File dir = new File("data/users/");
            if (!dir.exists()) dir.mkdirs();
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(new File(dir, username + ".dat")))) {
                oos.writeObject(this);
            }
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }
    
    /**
     * Loads a user from disk by username.
     * @param username the username
     * @return the User object, or null if not found or error
     */
    public static User loadUserData(String username) {
        File file = new File("data/users/" + username + ".dat");
        if (!file.exists()) {
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            return (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading user data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Copies a photo from one album to another.
     * @param photo the photo to copy
     * @param fromAlbum the source album name
     * @param toAlbum the destination album name
     * @return true if copied, false if not found or duplicate
     */
    public boolean copyPhoto(Photo photo, String fromAlbum, String toAlbum) {
    Album sourceAlbum = getAlbum(fromAlbum);
    Album destAlbum = getAlbum(toAlbum);
    
    if (sourceAlbum == null || destAlbum == null) {
        return false;
    }
    
    if (!sourceAlbum.getPhotos().contains(photo)) {
        return false;
    }
    
    return destAlbum.addPhoto(photo);
}

    /**
     * Moves a photo from one album to another.
     * @param photo the photo to move
     * @param fromAlbum the source album name
     * @param toAlbum the destination album name
     * @return true if moved, false if not found or duplicate
     */
    public boolean movePhoto(Photo photo, String fromAlbum, String toAlbum) {
    if (copyPhoto(photo, fromAlbum, toAlbum)) {
        return removePhotoFromAlbum(photo, fromAlbum);
    }
    return false;
}

    /**
     * Creates a new album from a list of photos (e.g., search results).
     * @param albumName the name of the new album
     * @param photos the list of photos to add
     * @return true if album created, false if name exists
     */
    public boolean createAlbumFromSearch(String albumName, List<Photo> photos) {
    if (getAlbum(albumName) != null) {
        return false;
    }
    
    Album newAlbum = new Album(albumName);
    for (Photo photo : photos) {
        newAlbum.addPhoto(photo);
    }
    albums.add(newAlbum);
    return true;
}
    
}

