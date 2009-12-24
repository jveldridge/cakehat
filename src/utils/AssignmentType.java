package utils;

/**
 * Supports the various types of assignments a course may have:
 *      PROJECT, LAB, HOMEWORK, and FINAL
 *
 * FINAL is treated as a special kind of PROJECT.
 *
 * @author jak2 (Joshua Kaplan)
 */
public enum AssignmentType {
    
    FINAL, PROJECT, LAB, HOMEWORK;

    /**
     * Returns the AssignmentType corresponding to the string passed in.
     *
     * @param typeString
     * @return corresponding AssignmentType
     */
    public static AssignmentType getInstance(String typeString) {
        //Look for corresponding AssignmentType enum
        for(AssignmentType type : values()) {
            if(typeString.toUpperCase().equals(type.toString())) {
                return type;
            }
        }

        //TODO: Consider throwing a AssignmentTypeNotFound exception
        //If not found, print error and return null
        System.err.println("Invalid input: " + typeString +
                            ", valid options are " + java.util.Arrays.toString(values()));
        return null;
    }
}