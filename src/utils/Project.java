package utils;

import java.util.Collection;
import java.util.HashMap;
import java.io.File;

/**
 * Represents a code project that exists in the handin directory for a course.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class Project {

    //Handins for the project
    private Collection<File> _handins = null;
    //All projects that have been loaded so far, to avoid loading a project more than once
    private static HashMap<String, Project> PROJECTS = new HashMap<String, Project>();
    //Name of project
    private String _name;

    //TODO: Consider having this method throw a ProjectNotFoundException instead of returning null if a project isn't found
    /**
     * Only way to get an instance of Project - there is no public constructor.
     * If a name has been passed in that does not have a directory in the course's
     * handin directory then an error will be printed and null will be returned.
     *
     * @param name
     * @return
     */
    public static Project getInstance(String name) {
        Project prj = null;
        //Check if the project has been created yet
        if (PROJECTS.containsKey(name)) {
            prj = PROJECTS.get(name);
        }
        //Otherwise create the project and store it
        else {
            //Build path to handin directory
            String prjPath = Constants.HANDIN_DIR + name + "/";
            //Check if directory exists
            if(new File(prjPath).exists()) {
                prj = new Project(name);
                PROJECTS.put(name, prj);
            }
            //Otherwise print an error and null will be returned
            else {
                System.err.println("Cannot create project for " + name);
                System.err.println(prjPath + " does not exist.");
            }
        }

        return prj;
    }

    /**
     * Private constructor which initializes name of project and prevents
     * project from being directly constructed.
     *
     * @param name
     */
    private Project(String name) {
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
     * specified by Constants, the name of this project, and the current year.
     *
     * @return handin path
     */
    private String getHandinPath() {
        String path = Constants.HANDIN_DIR + this.getName() + "/" + Utils.getCurrentYear() + "/";
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
            _handins = Utils.getFiles(this.getHandinPath(), "tar");
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
}