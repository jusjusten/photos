import photos.model.*;
import java.io.File;


public class TestApp {
    public static void main(String[] args) {

        System.out.println("=== Testing User, Albums, Photos, and Tags ===");

        // Create a user
        User user = new User("justin");
        System.out.println("Created user: " + user.getUsername());

        // Create albums
        user.createAlbum("Vacation");
        user.createAlbum("Family");
        System.out.println("Albums: " + user.getAlbums());

        // Add a photo
        File f = new File("test.jpg");
        try {
            f.createNewFile(); // ensures a dummy file exists
        } catch (Exception e) {}

        Photo p = user.addPhoto(f, "Vacation");
        System.out.println("Added photo: " + (p != null ? p.getFilePath() : "FAILED"));

        // Add photo tags
        if (p != null) {
            p.addTag("location", "Hawaii");
            p.addTag("person", "Mom");
            System.out.println("Photo tags: " + p.getTags());
        }

        // Search by tag
        TagCriteria tc = new TagCriteria("location", "Hawaii");
        System.out.println("Search results: " + user.searchByTags(tc));

        // Test user-level tags
        user.addTag(new Tag("favorite", "beaches"));
        System.out.println("User tags: " + user.getTags());

        // Save & reload user
        user.saveUserData();
        User loaded = User.loadUserData("justin");
        System.out.println("Loaded user: " + loaded.getUsername());
    }
}
