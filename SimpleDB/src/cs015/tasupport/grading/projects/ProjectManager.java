package cs015.tasupport.grading.projects;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Vector;

import cs015.tasupport.grading.rubric.TimeStatus;
import cs015.tasupport.utils.Utils;
import cs015.tasupport.grading.Constants;
import cs015.tasupport.grading.config.Assignment;
import cs015.tasupport.grading.config.ConfigurationManager;

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
     * Gets project from project name string.  @TODO: CHANGE THIS.
     * @param projectName
     * @return
     */
    public static Project getProjectFromString(String projectName) {
        for (Assignment a : ConfigurationManager.getAssignments()) {
            if (a.Name.equalsIgnoreCase(projectName)) {
                return Project.getInstance(a);
            }
        }
        return null;
    //Throw error here maybe
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
        Calendar early = prj.getAssignmentInfo().Early;
        Calendar ontime = prj.getAssignmentInfo().Ontime;
        Calendar late = prj.getAssignmentInfo().Late;

        if (early != null && Utils.isBeforeDeadline(handinTime, early, minutesOfLeniency)) {
            return TimeStatus.EARLY;
        }

        if (ontime != null && Utils.isBeforeDeadline(handinTime, ontime, minutesOfLeniency)) {
            return TimeStatus.ON_TIME;
        }

        if (late != null && Utils.isBeforeDeadline(handinTime, late, minutesOfLeniency)) {
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
        //Create an empty folder for grading compiled student code
        String compileDir = getStudentSpecificDirectory(prj, studentLogin);
        Utils.makeDirectory(compileDir);

        //untar student handin
        Utils.untar(prj.getHandin(studentLogin).getAbsolutePath(), compileDir);
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
        Utils.execute(compileDir, prj.getName() + ".App");
    }

    private static String getStudentSpecificDirectory(Project prj, String studentLogin) {
        return getCodeDirectory(prj) + studentLogin + "/";
    }

    private static String getCodeDirectory(Project prj) {
        return getUserGradingDirectory() + prj.getName() + "/" + Constants.CODE_DIR;
    }

    private static String getUserGradingDirectory() {
        return Constants.GRADER_PATH + Utils.getUserLogin() + "/";
    }
}