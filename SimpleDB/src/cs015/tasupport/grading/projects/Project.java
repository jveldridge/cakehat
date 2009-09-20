package cs015.tasupport.grading.projects;

import java.util.Collection;
import java.util.HashMap;
import java.io.File;
import cs015.tasupport.utils.Utils;
import cs015.tasupport.grading.Constants;
import cs015.tasupport.grading.config.Assignment;
import cs015.tasupport.grading.config.AssignmentType;

public class Project {

    private final Collection<File> HAND_INS;
    private Assignment _asgn;
    private static HashMap<Assignment, Project> Projects = new HashMap<Assignment, Project>();

    public static Project getInstance(Assignment asgn) {
        if (asgn.Type != AssignmentType.PROJECT) {
            throw new Error("Cannot create a project for " + asgn.Name + ", as it is a " +
                    asgn.Type + ", but must be a " + AssignmentType.PROJECT);
        }

        //Check if the project has been created yet, if so then return it
        Project prj;
        if (Projects.containsKey(asgn)) {
            prj = Projects.get(asgn);
        } else {
            prj = new Project(asgn);
            Projects.put(asgn, prj);
        }

        return prj;
    }

    private Project(Assignment asgn) {
        _asgn = asgn;
        HAND_INS = initializeHandins();
    }

    Assignment getAssignmentInfo() {
        return _asgn;
    }

    public String getName() {
        return _asgn.Name;
    }

    private String getHandinPath() {
        String path = Constants.HANDIN_DIR + getName() + "/" + cs015.tasupport.utils.Utils.getCurrentYear() + "/";
        return path;
    }

    private Collection<File> initializeHandins() {
        return Utils.getFiles(getHandinPath(), "tar");
    }

    Collection<File> getHandins() {
        return HAND_INS;
    }

    File getHandin(String studentLogin) {
        for (File handin : getHandins()) {
            if (handin.getName().equals(studentLogin + ".tar")) {
                return handin;
            }
        }

        return null;
    }
}
