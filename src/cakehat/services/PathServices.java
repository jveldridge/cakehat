package cakehat.services;

import cakehat.assignment.Part;
import cakehat.newdatabase.Group;
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

    
    // DEPRECATED METHODS
    

    /**
     * The directory containing all of the handins for a handin. This directory
     * does not belong to cakehat.
     *
     * <pre>
     * {@code
     * /course/<course>/handin/<asssignment name>/<current year>/
     * }
     * </pre>
     *
     * @param handin
     * @return
     */
    @Deprecated
    public File getHandinDir(cakehat.config.handin.Handin handin);
    
    /**
     * A group's GRD file for a given Handin.
     * 
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta login>/<assignment name>/<group name>.txt
     * }
     * </pre>
     *
     * @param handin
     * @param group
     * @return
     */
    @Deprecated
    public File getGroupGRDFile(cakehat.config.handin.Handin handin, cakehat.database.Group group);
    
    /**
     * Configuration file.
     *
     * <pre>
     * {@code
     * /course/<course code>/.cakehat/<current year>/config/config.xml
     * }
     * </pre>
     *
     * @return
     */
    @Deprecated
    public File getConfigurationFile();

    /**
     * The directory containing the GML files for a given DistributablePart.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/rubrics/<assignment name>/<distributablepart name>/
     * }
     * </pre>
     *
     * @param part
     * @return
     */
    @Deprecated
    public File getGMLDir(cakehat.config.handin.DistributablePart part);

    /**
     * A group's GML file for a given DistributablePart.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/rubrics/<assignment name>/<distributable part name>/<group name>.gml
     * }
     * </pre>
     *
     * @param part
     * @param group
     * @return
     */
    @Deprecated
    public File getGroupGMLFile(cakehat.config.handin.DistributablePart part, cakehat.database.Group group);
    
    /**
     * The path to the directory, inside the user's temporary workspace
     * directory, which contains unarchived handins and converted GRD files
     * belonging to the <code>part</code>.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta login>/<assignment name>/<distributable part name>/
     * }
     * </pre>
     *
     * @param part
     * @return
     */
    @Deprecated
    public File getUserPartDir(cakehat.config.handin.DistributablePart part);

    /**
     * The directory the handin is unarchived into for a given distributable
     * part. Even if two distributable parts have the same handin, they have
     * different unarchive directories.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta login>/<assignment name>/<distributable part name>/<group name>/
     * }
     * </pre>
     *
     * @param part
     * @param group
     * @return
     */
    @Deprecated
    public File getUnarchiveHandinDir(cakehat.config.handin.DistributablePart part, cakehat.database.Group group);
}