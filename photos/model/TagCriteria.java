package photos.model;

import java.io.Serializable;
import java.util.List;

public class TagCriteria implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum SearchType {
        SINGLE_TAG,      // One tag
        CONJUNCTIVE,     // AND
        DISJUNCTIVE      // OR
    }

    private SearchType searchType;
    private Tag tag1;
    private Tag tag2;

    // Single tag constructor
    public TagCriteria(String tagName, String tagValue) {
        this.searchType = SearchType.SINGLE_TAG;
        this.tag1 = new Tag(tagName, tagValue);
    }

    // Two tag constructor
    public TagCriteria(String tagName1, String tagValue1, 
                      String tagName2, String tagValue2, 
                      boolean isConjunctive) {
        this.searchType = isConjunctive ? SearchType.CONJUNCTIVE : SearchType.DISJUNCTIVE;
        this.tag1 = new Tag(tagName1, tagValue1);
        this.tag2 = new Tag(tagName2, tagValue2);
    }

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

    public SearchType getSearchType() {
        return searchType;
    }

    public Tag getTag1() {
        return tag1;
    }
    
    public Tag getTag2() {
        return tag2;
    }
}
