package utils;

import config.TA;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Utilities that are login-realated.
 */
public class UserUtilities {

    public enum ValidityCheck {BYPASS, CHECK};

    private HashMap<String, String> _loginNames = new HashMap<String, String>();
    private HashMap<String, Collection<String>> _groupLogins = new HashMap<String, Collection<String>>();

   /**
     * Returns all members of a given group
     *
     * @param group
     * @return collection of all of the logins of a given group
     */
    public Collection<String> getMembers(String group) {
        if (!_groupLogins.containsKey(group)){
            Collection<String> output = BashConsole.write("members " + group);
            Iterator<String> outputIterator = output.iterator();

            if (outputIterator.hasNext()) {
                String result = outputIterator.next();
                String[] logins = result.split(" ");

                _groupLogins.put(group, Arrays.asList(logins));
            }
            else {
                _groupLogins.put(group, new LinkedList<String>());
            }
        }

        return _groupLogins.get(group);
    }

    /**
     * Returns the logins of all students in the class's student group.
     * Removes the test account login.
     *
     * @return
     */
    public Collection<String> getStudentLogins() {
        //Get list of members to the student group
        Collection<String> list = (List<String>) Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getStudentGroup());
        //Remove test account from list
        //list.remove(Allocator.getCourseInfo().getTestAccount());

        return list;
    }

    /**
     * Returns the user login.
     *
     * @return user login
     */
    public String getUserLogin() {
        return BashConsole.write("/usr/bin/whoami").iterator().next();
    }

    /**
     * Gets a user's name.
     *
     * @param login the user's login
     * @return user's name
     */
    public String getUserName(String login) {
        if (!_loginNames.containsKey(login)) {
            List<String> output = BashConsole.write("f " + login);

            String name = output.get(0).split("life: ")[1];
            if (name.equals("???")) {
                name = "UNKNOWN_LOGIN";
            }
            _loginNames.put(login, name);
        }
        return _loginNames.get(login);
    }

    /**
     * Check if a login is valid.
     *
     * @param login the user's login
     * @return true if the login is snoopable
     */
    public boolean isLoginValid(String login){
        return !this.getUserName(login).equals("UNKNOWN_LOGIN");
    }

        /**
     * Returns whether or not the current user is a TA for the course as
     * specified by the configuration file.
     *
     * @return whether user is a TA
     */
    public boolean isUserTA(){
        String userLogin = Allocator.getUserUtilities().getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getTAs()){
            if(ta.getLogin().equals(userLogin)){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the current user is an admin for the course as
     * specified by the configuration file.
     *
     * @return whether the user is an Admin
     */
    public boolean isUserAdmin(){
        String userLogin = Allocator.getUserUtilities().getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getTAs()){
            if(ta.getLogin().equals(userLogin) && ta.isAdmin()){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the current user is an HTA for the course as
     * specified by the configuration file.
     *
     * @return whether user is a HTA
     */
    public boolean isUserHTA() {
        String userLogin = Allocator.getUserUtilities().getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getHTAs()){
            if(ta.getLogin().equals(userLogin)){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the student with given login studentLogin is a
     * member of the course's student group.
     * 
     * @return true if the student with login studentLogin is a member of the
     *         course's student group; false otherwise
     */
    public boolean isInStudentGroup(String studentLogin) {
        return getMembers(Allocator.getCourseInfo().getCourse() + "student").contains(studentLogin);
    }

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login (i.e., is not snoopable)
     * or is not in the course's student group; the user will then have the option
     * of adding the student anyway or cancelling the operation.  The user's
     * first and last name will be set to the firstName and lastName parameters,
     * respectively.
     *
     * @param studentLogin
     * @param firstName
     * @param lastName
     * @param checkValidity parameter that indicates whether the student should be
     *                    added to the database without checking that the login is
     *                    valid and that the the student is in the course student
     *                    group.  This should be passed as BYPASS when both of
     *                    these conditions are known to be true (for example, when
     *                    adding all members of the course group)
     */
    public void addStudent(String studentLogin, String firstName,
            String lastName, ValidityCheck checkValidity) {

        if (checkValidity == ValidityCheck.CHECK) {
            boolean isLoginValid = this.isLoginValid(studentLogin);
            boolean isInStudentGroup = this.isInStudentGroup(studentLogin);

            String warningMessage = "";
            if (!isLoginValid) {
                warningMessage += String.format("The login %s is not a valid (snoopable) login\n",
                        studentLogin);
            }
            else if (!isInStudentGroup) {
                warningMessage += String.format("The login %s is not in the student group",
                        studentLogin);
            }

            if (!isLoginValid || !isInStudentGroup) {
                Object[] options = {"Proceed", "Cancel"};
                int shouldContinue = JOptionPane.showOptionDialog(null, warningMessage,
                        "Invalid Student Login",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);

                if (shouldContinue != JOptionPane.OK_OPTION) {
                    return;
                }
            }
        }

        Allocator.getDatabaseIO().addStudent(studentLogin, firstName, lastName);
    }

}
