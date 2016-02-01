package org.fullmetalfalcons.androidscouting;

/**
 * This Enum holds the list of types for elements and contains methods that return the appropriate element based on
 * a string
 *
 * Created by Dan on 1/11/2016.
 */
public enum ElementType {

    SEGMENTED_CONTROL("segmented_control"),
    TEXTFIELD("textfield"),
    STEPPER("stepper"),
    LABEL("label"),
    SWITCH("switch"),
    SPACE("space"),
    SLIDER("slider");

    private final String key;

    /**
     * Private constructor for ElementType
     *
     * @param key Required so strings can be matched to ElementTypes
     */
    ElementType(String key){
        this.key = key;
    }

    /**
     * Parses a string and returns the appropriate ElementType
     *
     * @param input String to be parsed
     * @return The ElementType that matches input or null if none match
     */
    public static ElementType getElement(String input){
        for (ElementType e: values()){
            if (input.trim().equalsIgnoreCase(e.getKey())){
                return e;
            }
        }
        return null;
    }

    /**
     * Simple getter - only used in ElementType class
     *
     * @return key of the current ElementType
     */
    private String getKey(){
        return key;
    }
}
