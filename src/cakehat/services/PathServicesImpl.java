package cakehat.services;

import cakehat.Allocator;
import cakehat.CakehatRunMode;
import cakehat.CakehatMain;
import cakehat.database.Group;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.Handin;
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

        CakehatRunMode mode = CakehatMain.getRunMode();
        if(mode == CakehatRunMode.GRADER)
        {
            workspace = new File(parent,
                    Allocator.getUserUtilities().getUserLogin());
        }
        else if(mode == CakehatRunMode.ADMIN)
        {
            workspace = new File(parent,
                    Allocator.getUserUtilities().getUserLogin() + "-admin");
        }
        else if(mode == CakehatRunMode.UNKNOWN && !CakehatMain.didStartNormally())
        {
            workspace = new File(parent,
                    Allocator.getUserUtilities().getUserLogin() + "-test");
        }
        else
        {
            throw new IllegalStateException("Cannot provide path to user's " +
                    "workspace directory due to unexpected run state.\n" +
                    "Run mode: "+ mode + "\n" +
                    "Did start normally? " + CakehatMain.didStartNormally());
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
    @Deprecated
    public File getUnarchiveHandinDir(DistributablePart part, Group group)
    {
        return new File(new File(new File(
                getUserWorkspaceDir(),
                part.getAssignment().getName()),
                part.getName()),
                group.getName());
    }
    
    @Override
    public File getUnarchiveHandinDir(cakehat.assignment.Part part, Group group)
    {
        return new File(new File(new File(new File(
                getUserWorkspaceDir(),
                Integer.toString(part.getGradableEvent().getAssignment().getID())),
                Integer.toString(part.getGradableEvent().getID())),
                Integer.toString(part.getID())),
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