package utils;

import frontend.grader.rubric.TimeStatus;
import java.util.Collection;
import java.util.HashMap;
import java.io.File;
import java.util.Calendar;
import java.util.Vector;

/**
 * Represents a code project that exists in the handin directory for a course.
 *
 * @author jak2 (Joshua Kaplan)
 */
public abstract class Project {

    //Handins for the project
    private Collection<File> _handins = null;
    //Name of project
    private String _name;

    /**
     * Protected constructor which initializes name of project and prevents
     * project from being constructed improperly.
     *
     * @param name
     */
    protected Project(String name) {
        _name = name;
    }

    /**
     * Returns this Project's name.
     *
     * @return name
     */
    public String getName() {
        return _name;
    }

    /**
     * Helper method to generate handin path. Relies upon the handin directory
     * specified by constants, the name of this project, and the current year.
     *
     * @return handin path
     */
    private String getHandinPath() {
        String path = Allocator.getConstants().getHandinDir()
                      + this.getName() + "/" + Allocator.getGeneralUtilities().getCurrentYear() + "/";
        return path;
    }

    /**
     * Only visible to other classes in this package. Returns the Files for each
     * handin for this project. If this method has not been called before it will
     * load all of the handins. If the name of this project was invalid, calling
     * this method will result in an empty Collection of Files being returned.
     *
     * @return handins
     */
    Collection<File> getHandins() {
        //If handins have not been requested yet, load them
        if(_handins == null){
            _handins = Allocator.getGeneralUtilities().getFiles(this.getHandinPath(), "tar");
        }
        return _handins;
    }

    /**
     * Checks whether or not a student login has a handin for this project.
     *
     * @param studentLogin
     * @return if student has handin for this project
     */
    public boolean containsStudent(String studentLogin){
        return (this.getHandin(studentLogin) != null);
    }

    /**
     * This method is package-private. It returns a student's handin if it exists
     * otherwise it returns null.
     *
     * @param studentLogin
     * @return a student's handin for this assignment.
     */
    File getHandin(String studentLogin) {
        for (File handin : this.getHandins()) {
            if (handin.getName().equals(studentLogin + ".tar")) {
                return handin;
            }
        }

        return null;
    }


    /// New functionality

    /**
     * Returns all student logins that handed in a project.
     *
     * @return all student logins for project passed in
     */
    public Collection<String> getHandinLogins() {
        Vector<String> logins = new Vector<String>();

        for (File handin : this.getHandins()) {
            //Split at the . in the filename
            //So if handin is "jak2.tar", will add the "jak2" part
            logins.add(handin.getName().split("\\.")[0]);
        }
        return logins;
    }

    /**
     * Returns the time status of a student project.
     *
     * @param studentLogin
     * @param minutesOfLeniency
     */
    public TimeStatus getTimeStatus(String studentLogin, int minutesOfLeniency) {

        Calendar handinTime = Allocator.getGeneralUtilities().getModifiedDate(this.getHandin(studentLogin));

        //TODO: Replace information out of the database instead
        Assignment asgn = null;
        for(Assignment a : ConfigurationManager.getAssignments()){
            if(a.Name.equalsIgnoreCase(this.getName())){
                asgn = a;
            }
        }
        if(asgn == null){
            throw new Error("No information in the configuration could be found for " + this.getName());
        }
        //END

        if (asgn.Early != null && Allocator.getGeneralUtilities().isBeforeDeadline(handinTime, asgn.Early, minutesOfLeniency)) {
            return TimeStatus.EARLY;
        }

        if (asgn.Ontime != null && Allocator.getGeneralUtilities().isBeforeDeadline(handinTime, asgn.Ontime, minutesOfLeniency)) {
            return TimeStatus.ON_TIME;
        }

        if (asgn.Late != null && Allocator.getGeneralUtilities().isBeforeDeadline(handinTime, asgn.Late, minutesOfLeniency)) {
            return TimeStatus.LATE;
        }

        return TimeStatus.NC_LATE;
    }

    /**
     * Creates a directory for the .code for this project.
     */
    public void createCodeDirectory() {
        Allocator.getGeneralUtilities().makeDirectory(getProjectCodeDirectory());
    }

    /**
     * Removes the directory created by createCodeDirectory()
     */
    public void removeCodeDirectory() {
        Allocator.getGeneralUtilities().removeDirectory(getProjectCodeDirectory());
    }

    /**
     * Untars all of the handins for the specified student logins.
     *
     * @param studentLogins
     */
    public void untar(Iterable<String> studentLogins) {
        for (String login : studentLogins) {
            untar(login);
        }
    }

    /**
     * Untars a student's handin.
     *
     * @param studentLogin
     */
    public void untar(String studentLogin) {
        //Check that the student actually has a handin
        if(!this.containsStudent(studentLogin)){
            System.err.println("Cannot untar " + studentLogin + "'s " + this.getName() + ". No handin found.");
            return;
        }

        //Create an empty folder for grading compiled student code
        String compileDir = getCodeStudentDirectory(studentLogin);
        Allocator.getGeneralUtilities().makeDirectory(compileDir);

        //untar student handin
        Allocator.getGeneralUtilities().untar(this.getHandin(studentLogin).getAbsolutePath(), compileDir);
    }

    /**
     * Deletes all compiled files in a student's project. It is safe to run
     * even if there are no compiled files in the project.
     *
     * @param studentLogin
     * @return success of deletion operation
     */
    public boolean deleteCompiledFiles(String studentLogin){
        String compileDir = getCodeStudentDirectory(studentLogin);
        return Allocator.getGeneralUtilities().deleteCompiledFiles(compileDir);
    }

    /**
     * Compiles a student's handin. If the language this handin is
     * written in has no form of compilation, then this method
     * does nothing.
     *
     * @param studentLogin
     */
    public abstract void compile(String studentLogin);

    /**
     * Runs the handin of this project turned by the student login.
     * Do not call this method until untar has been run on it.
     *
     * @param studentLogin
     */
    public abstract void run(String studentLogin);

    /**
     * Runs a demo version of this project.
     */
    public abstract void runDemo();

    /**
     * @date 12/06/2009
     * @param studentLogin
     * @return path to the TA grading directory for the project package for the given project and student
     *          currently, /course/cs015/grading/ta/2009/<talogin>/<projectname>/.code/<studentlogin>/<projectname>/
     * @author jeldridg
     */
    public String getStudentProjectDirectory(String studentLogin) {
        return getCodeStudentDirectory(studentLogin) + this.getName() + "/";
    }

    /**
     * TODO: Remove this method and any need for it. GFX should not be special cased.
     * 
     * @date 12/06/2009
     * @param studentLogin
     * @return path to the TA grading directory for GFX for the given project and student
     *          currently, /course/cs015/grading/ta/2009/<talogin>/<projectname>/.code/<studentlogin>/gfx/
     * @author jeldridg
     */
    public String getStudentGFXDirectory(String studentLogin) {
        return getCodeStudentDirectory(studentLogin) + "gfx/";
    }

    /**
     * @date 12/06/2009
     * @param studentLogin
     * @return path to the TA grading directory containing directories for all packages of a student's code for a particular project
     *          (i.e., the project and gfx, if applicable)
     *          currently, /course/cs015/grading/ta/2009/<talogin>/<projectname>/.code/<studentlogin>/
     * @author jeldridg
     */
    public String getCodeStudentDirectory(String studentLogin) {
        return getProjectCodeDirectory() + studentLogin + "/";
    }

    /**
     * @date 12/06/2009
     * @return path to the directory in which code of the students the user TA must grade will be stored
     *   currently, /course/cs015/grading/ta/2009/<talogin>/<projectname>/.code/
     * @author jeldridg
     */
    public String getProjectCodeDirectory() {
        return getUserProjectDirectory() + Allocator.getConstants().getCodeDir();
    }

    /**
     * @date 12/06/2009
     * @return path to a TA's grading directory for a particular project
     *          currently, /course/cs015/grading/ta/2009/<talogin>/<projectname>/
     * @author jeldridg
     */
    public String getUserProjectDirectory() {
        return Allocator.getGeneralUtilities().getUserGradingDirectory() + this.getName() + "/";
    }
}