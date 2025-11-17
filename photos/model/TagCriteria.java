package photos.model;

import java.io.Serializable;
import java.util.List;

/**
 * Criteria for searching photos by tags.
 * Supports three search types: single tag, conjunctive (AND), and disjunctive (OR).
 * Encapsulates the search logic for matching photos against tag criteria.
 * 
 * @author Keegan Tu
 */
public class TagCriteria implements Serializable {
    /**
     * Serial version UID for serialization
     */
    private static final long serialVersionUID = 1L;
    /**
     * Criteria used for tag-based search of photos.
     *
     * @author Justin
     */
    
    /**
     * Enumeration of search types.
     * SINGLE_TAG searches for one tag.
     * CONJUNCTIVE requires both tags (AND).
     * DISJUNCTIVE requires either tag (OR).
     */
    public enum SearchType {
        /** Search for a single tag */
        SINGLE_TAG,
        /** Search for both tags (AND) */
        CONJUNCTIVE,
        /** Search for either tag (OR) */
        DISJUNCTIVE
    }
    
    /**
     * The type of search being performed
     */
    private SearchType searchType;
    
    /**
     * The first tag to search for
     */
    private Tag tag1;
    
    /**
     * The second tag to search for (only used for CONJUNCTIVE and DISJUNCTIVE)
     */
    private Tag tag2;
    
    /**
     * Creates a single tag search criteria.
     * 
     * @param tagName the tag name to search for
     * @param tagValue the tag value to search for
     */
    public TagCriteria(String tagName, String tagValue) {
        this.searchType = SearchType.SINGLE_TAG;
        this.tag1 = new Tag(tagName, tagValue);
    }
    
    /**
     * Creates a two-tag search criteria with AND or OR logic.
     * 
     * @param tagName1 the first tag name
     * @param tagValue1 the first tag value
     * @param tagName2 the second tag name
     * @param tagValue2 the second tag value
     * @param isConjunctive true for AND search (both required), false for OR search (either)
     */
    public TagCriteria(String tagName1, String tagValue1, 
                      String tagName2, String tagValue2, 
                      boolean isConjunctive) {
        this.searchType = isConjunctive ? SearchType.CONJUNCTIVE : SearchType.DISJUNCTIVE;
        this.tag1 = new Tag(tagName1, tagValue1);
        this.tag2 = new Tag(tagName2, tagValue2);
    }
    
    /**
     * Checks if a photo matches this search criteria.
     * For SINGLE_TAG: photo must have tag1.
     * For CONJUNCTIVE: photo must have both tag1 and tag2.
     * For DISJUNCTIVE: photo must have either tag1 or tag2.
     * 
     * @param photo the photo to check
     * @return true if the photo matches the criteria, false otherwise
     */
    public boolean matches(Photo photo) {
        List<Tag> photoTags = photo.getTags();
        
        switch (searchType) {
            case SINGLE_TAG:
                return photoTags.contains(tag1);
                
            case CONJUNCTIVE:
                return photoTags.contains(tag1) && photoTags.contains(tag2);
                
            case DISJUNCTIVE:
                return photoTags.contains(tag1) || photoTags.contains(tag2);
                
            default:
                return false;
        }
    }
    
    /**
     * Gets the search type.
     * 
     * @return the SearchType enum value
     */
    public SearchType getSearchType() {
        return searchType;
    }
    
    /**
     * Gets the first tag.
     * 
     * @return the first tag in the criteria
     */
    public Tag getTag1() {
        return tag1;
    }
    
    /**
     * Gets the second tag.
     * Only relevant for CONJUNCTIVE and DISJUNCTIVE searches.
     * 
     * @return the second tag, or null for SINGLE_TAG searches
     */
    public Tag getTag2() {
        return tag2;
    }
}
