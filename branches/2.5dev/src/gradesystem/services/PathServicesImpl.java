package gradesystem.services;

import gradesystem.Allocator;
import gradesystem.GradeSystemApp;
import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
import java.io.File;

/**
 *
 * @author jak2
 */
public class PathServicesImpl implements PathServices
{
    @Override
    public File getCourseDir()
    {
        return new File("/course", Allocator.getCourseInfo().getCourse());
    }
    
    @Override
    public File getHandinDir(Handin handin)
    {
        return new File(new File(new File(
                getCourseDir(),
                "handin"),
                handin.getAssignment().getName()),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear()));
    }

    @Override
    public File getCakehatDir()
    {
        return new File(getCourseDir(), ".cakehat");
    }

    @Override
    public File getConfigurationFile()
    {
        return new File(new File(new File(
                getCakehatDir(),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear())),
                "config"),
                "config.xml");
    }
    
    @Override
    public File getGMLDir(DistributablePart part)
    {
        return new File(new File(new File(new File(
                getCakehatDir(),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear())),
                "rubrics"),
                part.getAssignment().getName()),
                part.getName());
    }

    @Override
    public File getGroupGMLFile(DistributablePart part, Group group)
    {
        return new File(getGMLDir(part), group.getName() + ".gml");
    }

    @Override
    public File getDatabaseFile()
    {
        return new File(new File(new File(getCakehatDir(),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear())),
                "database"),
                "database.db");
    }

    @Override
    public File getDatabaseBackupDir()
    {
        return new File(new File(new File(
                getCakehatDir(),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear())),
                "database"),
                "backups");
    }

    @Override
    public File getUserWorkspaceDir()
    {
        File parent = new File(getCakehatDir(), "workspaces");

        File workspace;

        if(GradeSystemApp.isFrontend())
        {
            workspace = new File(parent,
                    Allocator.getUserUtilities().getUserLogin());
        }
        else if(GradeSystemApp.isBackend())
        {
            workspace = new File(parent,
                    Allocator.getUserUtilities().getUserLogin() + "-admin");
        }
        else
        {
            workspace = new File(parent,
                    Allocator.getUserUtilities().getUserLogin() + "-test");

            System.out.println("cakehat is neither in the frontend or backend, " +
                    "assuming cakehat is in test mode");
            System.out.println("Will use directory: " + workspace.getAbsolutePath());
        }

        return workspace;
    }

    @Override
    public File getUserPartDir(DistributablePart part)
    {
        return new File(new File(
                getUserWorkspaceDir(),
                part.getAssignment().getName()),
                part.getName());
    }

    @Override
    public File getUnarchiveHandinDir(DistributablePart part, Group group)
    {
        return new File(new File(new File(
                getUserWorkspaceDir(),
                part.getAssignment().getName()),
                part.getName()),
                group.getName());
    }

    @Override
    public File getGroupGRDFile(Handin handin, Group group)
    {
        return new File(new File(
                getUserWorkspaceDir(),
                handin.getAssignment().getName()),
                group.getName() + ".txt");
    }
}