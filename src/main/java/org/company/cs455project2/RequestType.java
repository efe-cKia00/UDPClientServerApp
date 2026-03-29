package org.company.cs455project2;

public enum RequestType {
    CREATE_EVENT("Create a new fundraising event"),
    LIST_CURRENT("List current fundraising events"),
    LIST_PAST("List past fundraising events"),
    DONATE("Donate to a fundraising event"),
    INVALID("Invalid request type");
    
    private final String description;
    
    RequestType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Converts a string to RequestType enum.
     * Useful for parsing incoming requests.
     * 
     * @param value the string value to convert
     * @return the corresponding RequestType, or INVALID if not found
     */
    public static RequestType fromString(String value) {
        try {
            return RequestType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INVALID;
        }
    }
}
