package cakehat.services;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.database.TA;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.printing.CITPrinter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * Creates and prints GRD files (plain text files with a .txt file extension) for each student in the given groups
     * for the assignment they all belong to. The output will be printed to the provided printer.
     * <br/><br/>
     * If the groups do not all belong to the assignment an exception will be thrown.
     * 
     * @param asgn
     * @param groups
     * @param printer
     * @throws ServicesException 
     */
    public void printGRDFiles(Assignment asgn, Set<Group> groups, CITPrinter printer) throws ServicesException;
    
    /**
     * Creates and emails GRD files (plain text files with a .txt file extension) for each student in the given groups
     * for the assignment they belong to.
     * <br/><br/>
     * If the groups do not all belong to the assignment an exception will be thrown.
     * 
     * @param asgn
     * @param groups
     * @throws ServicesException 
     */
    public void emailGRDFiles(Assignment asgn, Set<Group> groups) throws ServicesException;
    

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
     * Return value maps a handin name to the Group corresponding to that handin for the given gradable event and handin
     * names.  This code assumes that {@link #resolveUnexpectedHandins(GradableEvent)} has already been used to ensure
     * that all handin names in the given Set are in fact for a group valid for the Assignment.  If this is not the case,
     * a ServicesException will be thrown.
     * 
     * @param ge
     * @param handinNames
     * @return
     * @throws ServicesException
     */
    public Map<String, Group> getGroupsForHandins(GradableEvent ge,
                                                  Set<String> handinNames) throws ServicesException;

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
     * Checks that all handins for the given GradableEvent belong to a valid student or group.
     *
     * If the GradableEvent belongs to a group assignment, this method checks that the name of each handin
     * is either the name of some group or the login of a member of some group.  If this is
     * not the case for any handin, a message listing the problematic handins will be shown
     * to the user, and the names of the problematic handins will be returned.
     *
     * If the Gradable Event belongs to a non-group assignment, this method checks that the name of each
     * handin is the login of a student who is in the database and enabled.  If this is not the
     * case for any handin, presents the user with an appropriate warning dialog through which students
     * corresponding to these handins can either be added/enabled or ignored.
     *
     * @param asgn
     * @return what are the remaining bad logins (null if the user clicked Cancel)
     */
    public Set<String> resolveUnexpectedHandins(GradableEvent ge) throws ServicesException;
    
    /**
     * Builds a mapping from assignment to student to score for a given assignment. A given mapping will never be null;
     * if no score exists for a student, the score will be set to 0. This is the raw score from the database and does
     * not take into account handin bonus or penalty - see bug 309.
     * 
     * @param asgns assignments to include in the map
     * @param studentsToInclude students to include in the map
     * @return
     * @throws ServicesException 
     */
    public Map<Assignment, Map<Student, Double>> getScores(Collection<Assignment> asgns,
            Collection<Student> studentsToInclude) throws ServicesException;
}