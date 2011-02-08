package gradesystem.services;

import gradesystem.config.Assignment;
import gradesystem.config.TA;
import gradesystem.database.Group;
import gradesystem.database.HandinStatus;
import gradesystem.handin.Handin;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

/**
 * This class provides grading specific utility functions.
 *
 * @author jeldridg
 * @author jak2
 */
public interface GradingServices
{
    /**
     * Makes the user's workspace as specified by {@link PathServices#getUserWorkspace()}.
     *
     * @throws ServicesException if unable to create directory
     */
    public void makeUserWorkspace() throws ServicesException;

    /**
     * Removes the user's workspace as specified by {@link PathServices#getUserWorkspace()}.
     *
     * @throws ServicesException if unable to remove directory
     */
    public void removeUserWorkspace() throws ServicesException;


    public void printGRDFiles(Handin part, Iterable<Group> groups) throws ServicesException;

    /**
     * Opens a new EmailView so that user TA can inform students that their assignment
     * has been graded.  Default settings:
     *  FROM:    user TA
     *  TO:      user TA
     *  CC:      grades TA & grades HTA
     *  BCC:     students the user TA is assigned to grade for this assignment, as selected
     *  SUBJECT: "[<course code>] <project> Graded"
     *  MESSAGE: "<project> has been graded and is available for pickup in the handback bin."
     *
     * @param project
     * @param students
     */
    public void notifyStudents(Handin handin, Collection<Group> groups, boolean emailRubrics);

    /**
     * Prompts the user to a select a printer.
     *
     * @return printer selected
     */
    public String getPrinter();

    /**
     * Print dialogue for selecting printer.  Message passed in will be displayed as instructions to the user
     * 
     * @param message
     * @return the name of the printer selected
     */
    public String getPrinter(String message);

    /**
     * Returns whether or not it is OK to distribute the given group to the given
     * TA.  It is always OK to distribute the group if no member is on the TA's blacklist.
     * 
     * If a group member is on the TA's blacklist, a dialog will be shown asking
     * the user whether or not to continue.  If the user selects the continue option,
     * this method returns true; otherwise, it will return false.
     * 
     * @param group
     * @param ta
     * @return true if it is OK to distribute the group's handin to the TA
     */
    public boolean isOkToDistribute(Group group, TA ta) throws ServicesException;

    /**
     * Returns whether or not some member of the given student's group
     * is on any of the given TAs' blacklists.
     *
     * @param studentLogin
     * @param tas
     * @return true if a group member is on the TA's blacklist; false otherwise
     */
    public boolean groupMemberOnTAsBlacklist(Group group, Map<TA, Collection<String>> blacklists) throws ServicesException;


    /**
     * Return value maps a handin name to the Group corresponding to that handin
     * for the given assignment.  Will not try to determine a group for handins in
     * handinsToIgnore.
     * 
     * @param asgn
     * @param handinsToIgnore
     * @return
     * @throws ServicesException
     */
    public Map<String, Group> getGroupsForHandins(Assignment asgn,
                                                  Collection<String> handinsToIgnore) throws ServicesException;

    /**
     * Return value maps a student's login to the Group object for that student on
     * the given Assignment for each enabled student.  If the assignment is not a
     * group assignment, this method guarantees that a group of one will have been
     * created in the database for each enabled student and that the corresponding
     * Group object will be in the returned map.
     *
     * Returns null if the configuration file indicates that the given Assignment
     * has groups but no groups have been entered into the database.
     *
     * @param asgn
     * @return
     * @throws ServicesException
     */
    public Map<String, Group> getGroupsForStudents(Assignment asgn) throws ServicesException;

    /**
     * Checks that all handins for the given Assignment belong to a valid student or Group.
     *
     * If the Assignment is a group assignment, this method checks that the name of each handin
     * is either the name of some group or the login of a member of some group.  If this is
     * not the case for any handin, a message listing the problematic handins will be shown
     * to the user, and null will be returned to indicate that distribution may not continue.
     *
     * If the Assignment is not a group assignment, this method checks that the name of each
     * handin is the login of a student who is in the database and enabled.  If this is not the
     * case for any handin, presents the user with an appopriate warning dialog through which students
     * corresponding to these handins can either be either added/enabled them or ignored.
     *
     * @param asgn
     * @return what are the remaining bad logins (null if the user clicked Cancel or Group resolution failed)
     */
    public Collection<String> resolveMissingStudents(Assignment asgn) throws ServicesException;

    public HandinStatus getHandinStatus(Handin handin, Group group,
                                    Calendar extension, int minsLeniency) throws ServicesException;

    public Map<Group, HandinStatus> getHandinStatuses(Handin handin, Collection<Group> groups,
                                                  Map<Group, Calendar> extensions, int minsLeniency) throws ServicesException;


    
}