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
import org.joda.time.DateTime;

/**
 * This class provides grading specific utility functions.
 *
 * @author jeldridg
 * @author jak2
 */
public interface GradingServices
{
    /**
     * Gets a mapping of group to the occurrence date. This method handles the logic of determining whether the date is
     * coming from the database (previously manually entered by a TA) or from the file system in the case of a digital
     * handin.
     * 
     * @param ge
     * @param groups
     * @return
     * @throws ServicesException 
     */
    public Map<Group, DateTime> getOccurrenceDates(GradableEvent ge, Set<Group> groups) throws ServicesException;

    /**
     * Returns an immutable list of the printers in the CIT that the user is allowed to print to.
     *
     * @return
     */
    public List<CITPrinter> getAllowedPrinters();

    /**
     * Returns the default CIT printer to be used. This printer corresponds to the printer available on the floor 3, the
     * floor the TA Lab is on.
     *
     * @return
     */
    public CITPrinter getDefaultPrinter();

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
    
    public Map<Student, String> generateGRD(Assignment asgn, Set<Student> students) throws ServicesException;

    /**
     * Returns whether or not it is OK to distribute the given group to the given TA. It is always OK to distribute the
     * group if no member is on the TA's blacklist, or if the given TA is null (meaning that the group is being
     * unassigned from another TA).
     * <br/><br/>
     * If a group member is on the TA's blacklist, a dialog will be shown asking the user whether or not to continue. If
     * the user selects the continue option, this method returns true; otherwise, it will return false.
     * 
     * @param group
     * @param ta
     * @return true if it is OK to distribute the group's handin to the TA
     */
    public boolean isOkToDistribute(Group group, TA ta) throws ServicesException;

    /**
     * Returns whether or not some member of the given group is on any of the given TAs' blacklists.
     *
     * @param group
     * @param blacklists
     * @return true if a group member is on the TA's blacklist; false otherwise
     */
    public boolean isSomeGroupMemberBlacklisted(Group group, Map<TA, Collection<Student>> blacklists) throws ServicesException;

    /**
     * Return value maps a handin name to the Group corresponding to that handin for the given gradable event and handin
     * names.  Will not attempt to determine a group for handins in handinsToIgnore.  This code assumes that
     * {@link #resolveUnexpectedHandins(GradableEvent)} has already been used to ensure that all handin names not in the
     * the given Set of handins to ignore are in fact for a group valid for the Assignment.  If this is not the case,
     * a ServicesException will be thrown.
     * 
     * @param ge
     * @param handinNames
     * @return
     * @throws ServicesException
     */
    public Map<String, Group> getGroupsForHandins(GradableEvent ge,
                                                  Set<String> handinsToIgnore) throws ServicesException;

    /**
     * Checks that all handins for the given GradableEvent belong to a valid student or group.
     * <br/><br/>
     * If the GradableEvent belongs to a group assignment, this method checks that the name of each handin is either the
     * name of some group or the login of a member of some group. If this is not the case for any handin, a message
     * listing the problematic handins will be shown to the user, and the names of the problematic handins will be
     * returned.
     * <br/><br/>
     * If the Gradable Event belongs to a non-group assignment, this method checks that the name of each handin is the
     * login of a student who is in the database and enabled. If this is not the case for any handin, presents the user
     * with an appropriate warning dialog through which students corresponding to these handins can either be
     * added/enabled or ignored.
     *
     * @param asgn
     * @return what are the remaining bad logins (null if the user clicked Cancel)
     */
    public Set<String> resolveUnexpectedHandins(GradableEvent ge) throws ServicesException;
}