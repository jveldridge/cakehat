package cakehat.services;

import cakehat.config.Assignment;
import cakehat.config.ConfigurationInfo;
import cakehat.config.TA;
import cakehat.database.Group;
import cakehat.config.handin.Handin;
import cakehat.database.Student;
import cakehat.printing.CITPrinter;
import java.util.Collection;
import java.util.List;
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
     * Makes the user's workspace as specified by
     * {@link PathServices#getUserWorkspace()}. If the workspace already exists,
     * it will attempt to delete it, but if it does not succeed at this it will
     * fail silently.
     * 
     * An attempt will be made to remove the user's workspace upon JVM shutdown;
     * however, this may fail silently if there are issues with NFS.
     *
     * @throws ServicesException if unable to create directory
     */
    public void makeUserWorkspace() throws ServicesException;
    
    /**
     * Attempts to make a backup of the database.  The backup will be created in
     * the directory returned by {@link PathServices#getDatabaseBackupDir()}.
     * It will be named "csXXXdb_bk_<date>".
     * 
     * @throws ServicesException 
     */
    public void makeDatabaseBackup() throws ServicesException;

    /**
     * Returns an immutable list of the printers in the CIT that the user is
     * allowed to print to.
     *
     * @return
     */
    public List<CITPrinter> getAllowedPrinters();

    /**
     * Returns the default CIT printer to be used. This printer corresponds to
     * the printer available on the floor 3, the floor the TA Lab is on.
     *
     * @return
     */
    public CITPrinter getDefaultPrinter();

    /**
     * Prints GRD files for the specified groups and handin. This method does
     * <strong>not</strong> generate GRD files, if any GRD files are missing
     * then an exception will be thrown.
     *
     * @param handin
     * @param groups
     * @param printer
     * @throws ServicesException
     */
    public void printGRDFiles(Handin handin, Iterable<Group> groups, CITPrinter printer) throws ServicesException;

    /**
     * Opens a new EmailView so that user TA can inform students that their assignment
     * has been graded.  Default settings:
     *  FROM:    user TA
     *  TO:      user TA
     *  CC:      notify addresses as specified by {@link ConfigurationInfo#getNotifyAddresses()}
     *  BCC:     students the user TA is assigned to grade for this assignment, as selected
     *  SUBJECT: "[<course code>] <Assignment> Graded"
     *  MESSAGE: "<Assignment> has been graded."
     *
     * @param handin
     * @param groups
     * @param emailRubrics
     */
    public void notifyStudents(Handin handin, Collection<Group> groups, boolean emailRubrics);

    /**
     * Prompts the user to a select a printer.
     *
     * @return printer selected
     */
    public CITPrinter getPrinter();

    /**
     * Print dialogue for selecting printer.  Message passed in will be displayed as instructions to the user
     * 
     * @param message
     * @return printer selected
     */
    public CITPrinter getPrinter(String message);

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
     * Returns whether or not some member of the given group
     * is on any of the given TAs' blacklists.
     *
     * @param group
     * @param blacklists
     * @return true if a group member is on the TA's blacklist; false otherwise
     */
    public boolean isSomeGroupMemberBlacklisted(Group group, Map<TA, Collection<Student>> blacklists) throws ServicesException;


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
     * Return value maps a Student object to the Group object for that student on
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
    public Map<Student, Group> getGroupsForStudents(Assignment asgn) throws ServicesException;

    /**
     * Checks that all handins for the given Assignment belong to a valid student or Group.
     *
     * If the Assignment is a group assignment, this method checks that the name of each handin
     * is either the name of some group or the login of a member of some group.  If this is
     * not the case for any handin, a message listing the problematic handins will be shown
     * to the user, and the names of the problematic handins will be returned.
     *
     * If the Assignment is not a group assignment, this method checks that the name of each
     * handin is the login of a student who is in the database and enabled.  If this is not the
     * case for any handin, presents the user with an appopriate warning dialog through which students
     * corresponding to these handins can either be either added/enabled them or ignored.
     *
     * @param asgn
     * @return what are the remaining bad logins (null if the user clicked Cancel)
     */
    public Collection<String> resolveUnexpectedHandins(Assignment asgn) throws ServicesException;

    /**
     * For each Group in the given Collection, calculates the handin status for that
     * Group for the given Handin and stores the handin status in the database.  If
     * overwrite is true, any existing handin status will be overwritten.  If false,
     * handin statuses will only be stored for groups that do not already have handin
     * statuses.
     *
     * @param handin
     * @param groups
     * @param minsLeniency
     * @param overwrite
     * @throws ServicesException
     */
    public void storeHandinStatuses(Handin handin, Collection<Group> groups,
                                    int minsLeniency, boolean overwrite) throws ServicesException;
}