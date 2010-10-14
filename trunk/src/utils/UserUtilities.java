package utils;

import config.TA;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Utilities that are login-realated.
 */
public class UserUtilities {

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

            String result = output.iterator().next();
            String[] logins = result.split(" ");

            _groupLogins.put(group, Arrays.asList(logins));
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
    public boolean isUserHTA(){
        String userLogin = Allocator.getUserUtilities().getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getHTAs()){
            if(ta.getLogin().equals(userLogin)){
                return true;
            }
        }
        return false;
    }

}
