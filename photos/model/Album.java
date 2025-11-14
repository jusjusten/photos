package photos.model;

import java.io.*;
import java.util.*;


public class Album implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private List<Photo> photos;
    private Date startDate;
    private Date endDate;
    

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
        this.startDate = null;
        this.endDate = null;
    }
    

    public boolean addPhoto(Photo photo) {
        if (photos.contains(photo)) {
            return false;
        }
        photos.add(photo);
        updateDateRange();
        return true;
    }
    

    public boolean removePhoto(Photo photo) {
        boolean removed = photos.remove(photo);
        if (removed) {
            updateDateRange();
        }
        return removed;
    }
    

    public boolean containsPhoto(Photo photo) {
        return photos.contains(photo);
    }
    

    public Photo getPhoto(int index) {
        if (index >= 0 && index < photos.size()) {
            return photos.get(index);
        }
        return null;
    }
    

    public List<Photo> getPhotos() {
        return new ArrayList<>(photos);
    }

    public int getPhotoCount() {
        return photos.size();
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }

    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }

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
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Album album = (Album) obj;
        return name.equalsIgnoreCase(album.name);
    }
    
    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
    
    @Override
    public String toString() {
        return name + " (" + getPhotoCount() + " photos)";
    }
}