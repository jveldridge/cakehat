package utils;

import rubric.TimeStatus;
import java.util.Collection;
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
        String compileDir = getStudentCodeDirectory(studentLogin);
        Allocator.getGeneralUtilities().makeDirectory(compileDir);

        //untar student handin
        Allocator.getGeneralUtilities().untar(this.getHandin(studentLogin).getAbsolutePath(), compileDir);
    }
    
    /**
     * Runs the handin of this project turned by the student login.
     * Do not call this method until untar has been run on it.
     *
     * @param studentLogin
     */
    public abstract void run(String studentLogin);

    /**
     * Whether or not this project has a tester.
     *
     * @return tester's existance
     */
    public abstract boolean hasTester();

    /**
     * Runs the tester for this project on the student's handin.
     *
     * @param studentLogin
     */
    public abstract void runTester(String studentLogin);

    /**
     * Runs a demo version of this project.
     */
    public abstract void runDemo();

    /**
     * Prints a student's code for this assignment.
     *
     * @param studentLogin
     */
    public abstract void print(String studentLogin, String printer);

    /**
     * Prints the students' code for this assignment.
     *
     * @param studentLogins
     */
    public abstract void print(Iterable<String> studentLogins, String printer);


    /**
     * @date 12/06/2009
     * @param studentLogin
     * @return path to the TA grading directory containing directories for all packages of a student's code for a particular project
     *          (i.e., the project and gfx, if applicable)
     *          currently, /course/cs015/grading/.<talogin>/<projectname>/<studentLogin>/
     * @author jeldridg
     */
    protected String getStudentCodeDirectory(String studentLogin) {
        return Allocator.getGeneralUtilities().getUserGradingDirectory() + this.getName() + "/" + studentLogin + "/";
    }
}