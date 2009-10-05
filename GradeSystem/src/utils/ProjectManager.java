package utils;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Vector;

import frontend.grader.rubric.TimeStatus;
import utils.Utils;
import utils.Constants;

public class ProjectManager {

    /**
     * Returns all student logins that handed in a project.
     *
     * @param prj the project
     * @return all student logins for project passed in
     */
    public static Collection<String> getHandinLogins(Project prj) {
        Vector<String> logins = new Vector<String>();
        for (File handin : prj.getHandins()) {
            logins.add(handin.getName().split("\\.")[0]);
        }
        return logins;
    }

    /**
     * Returns the time status of a student project.
     *
     * @param studentLogin
     * @param prj
     * @param minutesOfLeniency
     */
    public static TimeStatus getTimeStatus(String studentLogin, Project prj, int minutesOfLeniency) {
           
        Calendar handinTime = Utils.getModifiedDate(prj.getHandin(studentLogin));

        //TODO: Replace this with get the information out of the database instead
        Assignment asgn = null;
        for(Assignment a : ConfigurationManager.getAssignments()){
            if(a.Name.equalsIgnoreCase(prj.getName())){
                asgn = a;
            }
        }
        if(asgn == null){
            throw new Error("No information in the configuration could be found for " + prj.getName());
        }
        //END

        if (asgn.Early != null && Utils.isBeforeDeadline(handinTime, asgn.Early, minutesOfLeniency)) {
            return TimeStatus.EARLY;
        }

        if (asgn.Ontime != null && Utils.isBeforeDeadline(handinTime, asgn.Ontime, minutesOfLeniency)) {
            return TimeStatus.ON_TIME;
        }

        if (asgn.Late != null && Utils.isBeforeDeadline(handinTime, asgn.Late, minutesOfLeniency)) {
            return TimeStatus.LATE;
        }

        return TimeStatus.NC_LATE;
    }

    /**
     * Creates a directory for the code for this project.
     *
     * @param prj
     */
    public static void createCodeDirectory(Project prj) {
        Utils.makeDirectory(getCodeDirectory(prj));
    }

    /**
     * Removes the directory created by createCodeDirectory(...)
     *
     * @param prj
     */
    public static void removeCodeDirectory(Project prj) {
        Utils.removeDirectory(getCodeDirectory(prj));
    }

    /**
     * Untars all of the handins for the specified project and
     * student logins.
     *
     * @param prj
     * @param studentLogins
     */
    public static void untar(Project prj, Iterable<String> studentLogins) {
        for (String login : studentLogins) {
            untar(prj, login);
        }
    }

    /**
     * Untars a student's handin for a given project.
     *
     * @param prj
     * @param studentLogin
     */
    private static void untar(Project prj, String studentLogin) {
        //Check that the student actually has a handin
        if(prj.getHandin(studentLogin) == null){
            return;
        }

        //Create an empty folder for grading compiled student code
        String compileDir = getStudentSpecificDirectory(prj, studentLogin);
        Utils.makeDirectory(compileDir);

        //untar student handin
        Utils.untar(prj.getHandin(studentLogin).getAbsolutePath(), compileDir);
    }

    /**
     * Deletes all .class files in a student's project. It is safe to run
     * even if there are no .class files in the project.
     *
     * @param prj
     * @param studentLogin
     * @return success of deletion operation
     */
    public static boolean deleteClassFiles(Project prj, String studentLogin){
        String compileDir = getStudentSpecificDirectory(prj, studentLogin);
        return Utils.deleteClassFiles(compileDir);
    }

    /**
     * Compiles a student project. Do not run until untar has been
     * run on it.
     *
     * @param prj
     * @param studentLogin
     */
    public static void compile(Project prj, String studentLogin) {
        String compileDir = getStudentSpecificDirectory(prj, studentLogin);
        Utils.compile(compileDir);
    }

    /**
     * Executes a student project. Do not run until after compile(...)
     * has been run on it.
     *
     * @param prj
     * @param studentLogin
     */
    public static void execute(Project prj, String studentLogin) {
        String compileDir = getStudentSpecificDirectory(prj, studentLogin);
        //Utils.execute(compileDir, prj.getName() + ".App"); //This executes without a terminal
        Utils.executeInVisibleTerminal(compileDir, prj.getName() + ".App", studentLogin + "'s " + prj.getName());
    }

    public static String getStudentSpecificDirectory(Project prj, String studentLogin) {
        return getCodeDirectory(prj) + studentLogin + "/";
    }

    public static String getCodeDirectory(Project prj) {
        return getUserGradingDirectory() + prj.getName() + "/" + Constants.CODE_DIR;
    }

    public static String getUserGradingDirectory() {
        return Constants.GRADER_PATH + Utils.getUserLogin() + "/";
    }
}