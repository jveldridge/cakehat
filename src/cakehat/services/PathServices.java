package cakehat.services;

import cakehat.CakehatSession;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.database.assignment.Action;
import java.io.File;

/**
 * Paths to files and directories used by cakehat.
 *
 * @author jak2
 */
public interface PathServices
{    
    /**
     * The course directory.
     * 
     * <pre>
     * {@code
     * /course/<course>/
     * }
     * </pre>
     * 
     * @return 
     */
    public File getCourseDir();
    
    /**
     * The course's TA bin directory. This directory is not managed by cakehat and is not guaranteed to exist.
     * 
     * <pre>
     * {@code
     * /course/<course>/tabin/
     * }
     * </pre>
     * 
     * @return 
     */
    public File getTABinDir();
    
    /**
     * The database file.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/database/database.db
     * }
     * </pre>
     *
     * @return
     */
    public File getDatabaseFile();

    /**
     * The directory that backups of the database are put in.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/database/backups/
     * }
     * </pre>
     *
     * @return
     */
    public File getDatabaseBackupDir();
    
    /**
     * A temporary directory that only exists during the user's cakehat session.
     * <br/><br/>
     * Run mode in the following path is defined as {@link CakehatSession#getRunMode()}:
     * <pre>
     * /course/<course>/.cakehat/<current year>/temp/<ta id>-<run mode>/
     * </pre>
     * 
     * @return 
     */
    public File getTempDir();
    
    /**
     * A temporary directory for the {@code action} that only exists during the user's cakehat session.
     * <br/><br/>
     * Run mode in the following path is defined as {@link CakehatSession#getRunMode()}:
     * <pre>
     * /course/<course>/.cakehat/<current year>/temp/<ta id>-<run mode>/<assignment id>/<gradable event id>/<part id>/<action id>/
     * </pre>
     * 
     * @param action
     * @return 
     */
    public File getActionTempDir(Action action);
    
    /**
     * A temporary directory for the {@code action} and {@code group} that only exists during the user's cakehat
     * session.
     * <br/><br/>
     * If {@code group} is not {@code null}:
     * <pre>
     * /course/<course>/.cakehat/<current year>/temp/<ta id>-<run mode>/<assignment id>/<gradable event id>/<part id>/<action id>/<group id>/
     * </pre>
     * If {@code group} is {@code null}:
     * <pre>
     * /course/<course>/.cakehat/<current year>/temp/<ta id>-<run mode>/<assignment id>/<gradable event id>/<part id>/<action id>/nogroup/
     * </pre>
     * Run mode in the above paths is defined as {@link CakehatSession#getRunMode()}.
     * 
     * @param action
     * @return 
     */
    public File getActionTempDir(Action action, Group group);
    
    /**
     * The directory the handin is unarchived into for a given part. Even if two parts belong to the same gradable
     * event, they will have different unarchive directories.
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/handin/<assignment id>/<gradable event id>/<part id>/<group id>/
     * }
     * </pre>
     */
    public File getUnarchiveHandinDir(Part part, Group group);
    
    /**
     * A group's GML file for a given Part.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/gml/<assignment id>/<gradable event id>/<part id>/<group id>.gml
     * }
     * </pre>
     *
     * @param part
     * @param group
     * @return
     */
    public File getGroupGMLFile(Part part, Group group);
    
    /**
     * The path to the file, inside the user's temporary workspace directory, that is the location of the student's
     * GRD file which will end in {@code .txt}.
     * <br/><br/>
     * Run mode in the following path is defined as {@link CakehatSession#getRunMode()}:
     * <pre>
     * {@code 
     * /course/<course>/.cakehat/<current year>/temp/<ta id>-<run mode>/<assignment id>/<student login>.txt
     * }
     * </pre>
     * 
     * @param asgn
     * @param student
     * @return 
     */
    public File getStudentGRDFile(Assignment asgn, Student student);
}