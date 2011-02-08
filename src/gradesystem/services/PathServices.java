package gradesystem.services;

import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
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
    public File getHandinDir(Handin handin);

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
    public File getGMLDir(DistributablePart part);

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
    public File getGroupGMLFile(DistributablePart part, Group group);

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
     * The temporary directory that the user uses while running cakehat.
     * <br/><br/>
     * If cakehat is running from the frontend:
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta login>/
     * }
     * </pre>
     * 
     * If cakehat is running from the backend:
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta login>-admin/
     * }
     * </pre>
     * 
     * If cakehat is being run from neither the backend nor frontend, which
     * should only occur during a test:
     * <pre>
     * {@code
     * /course/<course>/.cakehat/workspaces/<ta login>-test/
     * }
     * </pre>
     *
     * All other path methods that reference {@code <ta login>} in their path
     * build from this path and so follow the same pattern.
     *
     * @return
     */
    public File getUserWorkspaceDir();

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
    public File getUserPartDir(DistributablePart part);

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
    public File getUnarchiveHandinDir(DistributablePart part, Group group);

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
    public File getGroupGRDFile(Handin handin, Group group);
}