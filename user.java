// need a TagCriteria.java, Admin.java, DataManager.java, 
// package photos.model;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private List<Album> albums;
    private Map<String, Set<String>> tagTypes; // tag name -> "single" or "multiple"
    private transient Map<String, Photo> allPhotos; // filePath -> Photo (not serialized)
    

    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
        this.tagTypes = new HashMap<>();
        this.allPhotos = new HashMap<>();
        initializeDefaultTagTypes();
        
    }
    
    private void initializeDefaultTagTypes() {
        tagTypes.put("location", "single");
        tagTypes.put("person", "multiple");
        tagTypes.put("event", "single");


    }
    
    public boolean createAlbum(String albumName) {
        if (getAlbum(albumName) != null) {
            return false; 
        }
        Album newAlbum = new Album(albumName);
        albums.add(newAlbum);
        return true;
    }
    public boolean deleteAlbum(String albumName) {
        Album album = getAlbum(albumName);
        if (album != null) {
            albums.remove(album);
            return true;

        }
        return false;

    }
    
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
    public Album getAlbum(String albumName) {
        for (Album album : albums) {
            if (album.getName().equalsIgnoreCase(albumName)) {
                return album;
            }

        }
        return null;
    }
    

    public List<Album> getAlbums() {
        return new ArrayList<>(albums);
    }
    

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
    

    public boolean removePhotoFromAlbum(Photo photo, String albumName) {
        Album album = getAlbum(albumName);
        if (album != null) {
            return album.removePhoto(photo);

        }
        return false;
    }
    

    public Set<Photo> getAllPhotos() {
        return new HashSet<>(allPhotos.values());
    }
    

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
    

    public List<Photo> searchByTags(TagCriteria criteria) {
        List<Photo> results = new ArrayList<>();
        for (Photo photo : getAllPhotos()) {
            if (criteria.matches(photo)) {
                results.add(photo);
            }
        }
        return results;
    }
    

    public boolean addTagType(String tagName, boolean isSingleValue) {
        if (tagTypes.containsKey(tagName.toLowerCase())) {
            return false;
        }
        tagTypes.put(tagName.toLowerCase(), isSingleValue ? "single" : "multiple");
        return true;
    }
    

    public Map<String, String> getTagTypes() {
        return new HashMap<>(tagTypes);
    }
    

    public String getUsername() {
        return username;

    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.allPhotos = new HashMap<>();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                allPhotos.put(photo.getFilePath(), photo);
            }
        }
    }
    

    public void saveUserData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("data/users/" + username + ".dat"))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }
    
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
}