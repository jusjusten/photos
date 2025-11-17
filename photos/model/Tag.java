/**
 * @author Justin
 */
package photos.model;

import java.io.Serializable;

public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String value;

    /**
     * Constructs a tag with a name and value.
     * @param name the tag name (stored lowercase)
     * @param value the tag value
     */
    public Tag(String name, String value) {
        this.name = name != null ? name.toLowerCase() : "";
        this.value = value != null ? value : "";
    }

    /**
     * Gets the tag name (always lowercase).
     * @return the tag name
     */
    public String getName() {
        return name;
    }
    

    /**
     * Gets the tag value.
     * @return the tag value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the tag value.
     * @param value the new tag value
     */
    public void setValue(String value) {
        this.value = value != null ? value : "";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tag tag = (Tag) obj;
        return name.equalsIgnoreCase(tag.name) && value.equalsIgnoreCase(tag.value);
    }
    
    @Override
    public int hashCode() {
        return (name.toLowerCase() + value.toLowerCase()).hashCode();
    }
    
    @Override
    public String toString() {
        return name + ": " + value;
    }
}