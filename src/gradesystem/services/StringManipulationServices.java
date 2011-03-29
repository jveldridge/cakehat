package gradesystem.services;

import gradesystem.config.Assignment;
import gradesystem.database.Group;
import java.util.Collection;

/**
 * Services relating to modifying Strings based on whether an Assignment does
 * or does not have Groups.  The given Strings are likely to be used in UI
 * elements to hide from users the underlying implementation of Assignments
 * without Groups as Groups of one.
 *
 * @author jeldridg
 */
public interface StringManipulationServices {
    
    public static final String BE_TAG = "%BE%";
    public static final String UNIT_TAG = "%UNIT%";
    public static final String CAP_UNIT_TAG = "%CAP_UNIT%";
    public static final String NUM_TAG = "%NUM%";

    /**
     * Returns a new string that has predefined tags in the given text string
     * replaced based on the given Assignment and Collection of Groups.
     *
     * The following tags are recognized:
     * -%BE%       (replaced by "is" or "are")
     * -%UNIT%     (replaced by "student", "students", "group", or "groups")
     * -%CAP_UNIT% (identical with %UNIT%, but with the first character capitalized)
     * -%NUM%      (replaced by the number of groups)
     *
     * @param text
     * @param asgn
     * @param groups
     * @return
     */
    public String localizeText(String text, Assignment asgn, Collection<Group> groups);
    
}
