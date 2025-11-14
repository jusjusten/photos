package photos.model;

import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.ZoneId;


public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String filePath;
    private String caption;
    private Date dateTaken;
    private List<Tag> tags;
    private transient Object thumbnail; // Will store Image when available
    private transient Object fullImage; // Will store Image when available
    
    public Photo(File photoFile) {
        this.filePath = photoFile.getAbsolutePath();
        this.caption = "";
        this.tags = new ArrayList<>();
        this.dateTaken = getLastModifiedDate(photoFile);
        loadImages();
    }
    
    private Date getLastModifiedDate(File file) {
        long lastModified = file.lastModified();
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            new Date(lastModified).toInstant(), ZoneId.systemDefault());
        // no milliseconds so its easier
        dateTime = dateTime.withNano(0);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
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
    public boolean addTag(String tagName, String tagValue) {
        Tag newTag = new Tag(tagName, tagValue);
        if (tags.contains(newTag)) {
            return false;
        }
        tags.add(newTag);
        return true;
    }
    public boolean removeTag(String tagName, String tagValue) {
        Tag tagToRemove = new Tag(tagName, tagValue);
        return tags.remove(tagToRemove);
    }

    public boolean hasTag(String tagName, String tagValue) {
        return tags.contains(new Tag(tagName, tagValue));
    }
    
    public List<Tag> getTags() {
        return new ArrayList<>(tags);
    }
    public List<Tag> getTagsByName(String tagName) {
        List<Tag> result = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getName().equalsIgnoreCase(tagName)) {
                result.add(tag);
            }
        }
        return result;
    }
    

    public void setCaption(String caption) {
        this.caption = caption != null ? caption : "";
    }
    

    public String getCaption() {
        return caption;
    }
    public Date getDateTaken() {
        return new Date(dateTaken.getTime());
    }
    

    public Object getThumbnail() {
        if (thumbnail == null) {
            loadImages();
        }
        return thumbnail;
    }

    public Object getFullImage() {
        if (fullImage == null) {
            loadImages();
        }
        return fullImage;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return new File(filePath).getName();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        loadImages();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Photo photo = (Photo) obj;
        return filePath.equals(photo.filePath);
    }
    
    @Override
    public int hashCode() {
        return filePath.hashCode();
    }
    
    @Override
    public String toString() {
        return getFileName() + (caption.isEmpty() ? "" : " - " + caption);
    }
}