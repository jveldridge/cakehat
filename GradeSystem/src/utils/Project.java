package utils;

import java.util.Collection;
import java.util.HashMap;
import java.io.File;
import utils.Utils;
import utils.Constants;

public class Project {

    private final Collection<File> HAND_INS;
    private static HashMap<String, Project> Projects = new HashMap<String, Project>();
    private String _name;

    public static Project getInstance(String name) {

        //Check if the project has been created yet, if so then return it
        Project prj;
        if (Projects.containsKey(name)) {
            prj = Projects.get(name);
        } else {
            prj = new Project(name);
            Projects.put(name, prj);
        }

        return prj;
    }

    private Project(String name) {
        _name = name;
        HAND_INS = initializeHandins();
    }


    public String getName() {
        return _name;
    }

    private String getHandinPath() {
        String path = Constants.HANDIN_DIR + getName() + "/" + Utils.getCurrentYear() + "/";
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
