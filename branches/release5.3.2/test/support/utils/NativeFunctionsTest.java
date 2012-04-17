package support.utils;

import java.io.File;
import java.util.List;
import support.utils.posix.NativeException;
import support.utils.posix.NativeFunctions;

/**
 * This class contains tests for NativeFunctions.java. To run the tests,
 * run the mainline. The strings defined at the beginning of the mainline can be changed based on where
 * the test is being run. Some things must be verified by running the corresponding
 * linux commands from the shell (change group, chmod, and get members of a group),
 * while others (ssh detection, current user, is user login valid, get real name) 
 * can be verified by what is printed out. There are comments in the mainline with instructions
 * for how to do this.
 * 
 * 
 * @author hdrosen
 */
public class NativeFunctionsTest {
    
    private NativeFunctions _instance = new NativeFunctions();
    
    /**
     * Test this on a file that exists, a directory that exists,
     * a file that does not exist and a directory that does not exist.
     * Prints an error message if the file/directory does not exist.
     * 
     * @param filePath
     * @return 
     */
    public void testChmod(String filePath, int mode) {
        File file = new File(filePath);
        try {
            _instance.chmod(file, mode);
        }
        catch(NativeException e) {
            System.out.println("Error occurred while trying to chmod " + filePath + ". "
                    + "File or directory does not exist or permissions cannot be changed.\n");
        }
    }
    
    //550
    /**
     * Test this on a file that does not exist, a group that does not exist, 
     * a directory that does not exist, a group + directory that do exist, and
     * a group + file that do exist.
     * 
     * @param filePath
     * @param group 
     */
    public void testChangeGroup(String filePath, String group) {
        File file = new File(filePath);
        try {
            _instance.changeGroup(file, group);
        }
        catch(NativeException e) {
            System.out.println("Error occurred while trying to change group on " + filePath + "."
                    + "File or directory does not exist or group (" + group + ") does not exist.\n");
        }
    }
    
    /**
     * Test this on a real group and a fake group.
     * Returns a string with the list of group members if the group is valid,
     * and an error message if the group is not valid.
     * 
     * @param group
     * @throws NativeException 
     */
    public String testGetGroupMembers(String group) {
        try {
            List<String> members = _instance.getGroupMembers(group);
            String toreturn = "Members of " + group + " are:";
            for (String s: members) {
                toreturn += " " + s;
            }
            toreturn += ".\n";
            return toreturn;
        }
        catch(NativeException e) {
            return "The group members for " + group + " could not be retrieved because " 
                    + group + " is not a valid group.\n";
        }
    }
    
    /**
     * Test this on a real login and a fake login.
     * Returns the real name if login exists and an error message
     * if it does not.
     * 
     * @param login
     * @throws NativeException 
     */
    public String testGetRealName(String login) {
        try {
            return "The real name for " + login + " is " + _instance.getRealName(login) + ".\n";
        }
        catch(NativeException e) {
            return "Error trying to get real name because " + login + " is not a valid login.\n";
        }
    }
    
    /**
     * Test this on a real login and on a fake login.
     * Returns true if login exists and false if not.
     * 
     * @param login 
     */
    public boolean testIsLogin(String login) {
        return _instance.isLogin(login);
    }
    
    /**
     * Make sure that the string returned is the login of the person running
     * this test.
     * 
     * @return 
     */
    public String testGetUserLogin() {
        return _instance.getUserLogin();
    }
    
    /**
     * Test this over ssh and not over ssh.
     * Should return true if over ssh, and false if not.
     * 
     * @return
     * @throws NativeException 
     */
    public boolean testIsUserRemotelyConnected() throws NativeException {
        return _instance.isUserRemotelyConnected();
    }
    
    public static void main(String[] args) {
        NativeFunctionsTest test = new NativeFunctionsTest();
        
        String realLogin = "hdrosen";
        String fakeLogin = "as;dlkfj";
        String realGroup = "cs000ta";
        String fakeGroup = "fakeGroup";
        
        String realFile = "/course/cs000/testDir/testFile.txt";
        String realDirectory = "/course/cs000/testDir";
        
        String fakeFile = "/home/users/hdrosen/fakefile.java";
        String fakeDirectory = "/home/users/fakeDirectory";
        
        // Test chmod on a real file. Manually verify that the permissions on the file
        // are correct.
        test.testChmod(realFile, 0770);
        
        // Test chmod on a real directory. Manually verify that the permissions on the 
        // directory are correct.
        test.testChmod(realDirectory, 0770);
        
        // Test chmod on a fake file. Make sure that error message prints out.
        test.testChmod(fakeFile, 0777);
        
        // Test chmod on a fake directory.  Make sure that error message prints out.
        test.testChmod(fakeDirectory, 0777);
        
        // Test chgrp on a fake file and real group. Make sure that the error message 
        // prints out.
        test.testChangeGroup(fakeFile, realGroup);
        
        // Test chgrp on a real file and fake group. Make sure that the error message
        // prints out.
        test.testChangeGroup(realFile, fakeGroup);
        
        // Test chgrp on a fake directory and real group. Make sure that the error message
        // prints out.
        test.testChangeGroup(fakeDirectory, realGroup);
        
        // Test chgrp on a real file and real group.
        test.testChangeGroup(realFile, realGroup);
        
        // Test chgrp on a real directory and real group.
        test.testChangeGroup(realDirectory, realGroup);
        
        // Test getting the members of a group on a real group and verify the members by manually
        // running members {realGroup}.
        System.out.println(test.testGetGroupMembers(realGroup));
        
        // Test getting the members of a group on a fake group and verify that an exception was thrown.
        System.out.println(test.testGetGroupMembers(fakeGroup));
        
        // Test getting the real name on a real login and verify the name printed is correct by manually
        // running finger {realLogin}.
        System.out.println(test.testGetRealName(realLogin));
        
        // Test on a fake login and verify that an exception was thrown.
        System.out.println(test.testGetRealName(fakeLogin));
        
        // Test on a real login and make sure it prints out true.
        System.out.println(realLogin + " is a login: " + test.testIsLogin(realLogin) + ".\n");
        
        // Test on a fake login and make sure it prints out false.
        System.out.println(fakeLogin + " is a login: " + test.testIsLogin(fakeLogin) + ".\n");
        
        // Make sure this prints out your login.
        System.out.println("Current user login is " + test.testGetUserLogin() + ".\n");
        
        // Test over ssh and not over ssh.
        // Make sure this prints true if you are over ssh, false if you are not.
        try {
            System.out.println("User is remotely connected: " + test.testIsUserRemotelyConnected() + ".\n");
        }
        catch(NativeException e) {
            System.out.println("An error occurred while trying to detect if the user is remotely connected.");
        }
    }
}