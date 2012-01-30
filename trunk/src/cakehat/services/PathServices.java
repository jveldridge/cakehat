package cakehat.services;

import cakehat.assignment.Part;
import cakehat.database.Group;
import java.io.File;

/**
 * Paths to files and directories used by cakehat.
 *
 * @author jak2
 */
public interface PathServices
{
    /**
     * Course directory.
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
     * The directory where cakehat exists.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/
     * }
     * </pre>
     *
     * @return
     */
    public File getCakehatDir();
    
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
     * The temporary directory that the user uses while running cakehat.
     * <br/><br/>
     * If cakehat is running from the grader view:
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta id>/
     * }
     * </pre>
     * 
     * If cakehat is running from the admin view:
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta id>-admin/
     * }
     * </pre>
     * 
     * If cakehat's normal main class was not run and cakehat is in an unknown
     * mode, which should only occur during a test:
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta id>-test/
     * }
     * </pre>
     *
     * Any other state (such as during lab checkoff) will result in a runtime exception being thrown. If cakehat is not
     * using the grader, admin, or testing then the user workspace directory should not need be needed.
     * <br/><br/>
     * All other path methods that reference {@code <ta id>} in their path build from this path and so follow the same
     * pattern.
     *
     * @return
     */
    public File getUserWorkspaceDir();
    
    /**
     * The path to the directory, inside the user's temporary workspace directory, which contains unarchived digital
     * handins.
     * 
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta id>/<assignment id>/<gradable event id>/<part id>/
     * }
     * </pre>
     * 
     * @param part
     * @return 
     */
    public File getUserPartDir(Part part);
    
    /**
     * The directory the handin is unarchived into for a given part. Even if two parts belong to the same part block,
     * they will have different unarchive directories.
     * 
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta id>/<assignment id>/<gradable event id>/<part id>/<group id>
     * }
     * </pre>
     */
    public File getUnarchiveHandinDir(Part part, Group group);

}