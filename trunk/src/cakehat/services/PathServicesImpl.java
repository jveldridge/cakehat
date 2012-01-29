package cakehat.services;

import cakehat.Allocator;
import cakehat.CakehatRunMode;
import cakehat.CakehatMain;
import cakehat.assignment.Part;
import cakehat.newdatabase.Group;
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
    public File getCakehatDir()
    {
        return new File(getCourseDir(), ".cakehat");
    }

    @Override
    public File getDatabaseFile()
    {
        return new File(new File(new File(
                getCakehatDir(),
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
    public File getGroupGMLFile(Part part, Group group)
    {
        return new File(new File(new File(new File(new File(new File(
                getCourseDir(),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear())),
                "gml"),
                Integer.toString(part.getGradableEvent().getAssignment().getId())),
                Integer.toString(part.getGradableEvent().getId())),
                Integer.toString(part.getId())),
                Integer.toString(group.getId()) + ".gml");
    }

    @Override
    public File getUserWorkspaceDir()
    {
        File parent = new File(getCakehatDir(), "workspaces");
        String userId = Integer.toString(Allocator.getUserUtilities().getUserId());
        File workspace;

        CakehatRunMode mode = CakehatMain.getRunMode();
        if(mode == CakehatRunMode.GRADER)
        {
            workspace = new File(parent, userId);
        }
        else if(mode == CakehatRunMode.ADMIN)
        {
            workspace = new File(parent, userId + "-admin");
        }
        else if(mode == CakehatRunMode.UNKNOWN && !CakehatMain.didStartNormally())
        {
            workspace = new File(parent, userId + "-test");
        }
        else
        {
            throw new IllegalStateException("Cannot provide path to user's  workspace directory due to unexpected " +
                    "run state.\n" +
                    "Run mode: " + mode + "\n" +
                    "Did start normally? " + CakehatMain.didStartNormally());
        }

        return workspace;
    }
    
    @Override
    public File getUserPartDir(Part part)
    {
        return new File(new File(new File(
                getUserWorkspaceDir(),
                Integer.toString(part.getGradableEvent().getAssignment().getId())),
                Integer.toString(part.getGradableEvent().getId())),
                Integer.toString(part.getId()));
    }
    
    @Override
    public File getUnarchiveHandinDir(Part part, Group group)
    {
        return new File(getUserPartDir(part), Integer.toString(group.getId()));
    }
    
    
    // DEPRECATED METHODS
    
    
    @Override
    public File getGroupGRDFile(cakehat.config.handin.Handin handin, cakehat.database.Group group)
    {
        return new File(new File(
                getUserWorkspaceDir(),
                handin.getAssignment().getName()),
                group.getName() + ".txt");
    }

    @Override
    public File getUserPartDir(cakehat.config.handin.DistributablePart part)
    {
        return new File(new File(
                getUserWorkspaceDir(),
                part.getAssignment().getName()),
                part.getName());
    }

    @Override
    @Deprecated
    public File getUnarchiveHandinDir(cakehat.config.handin.DistributablePart part, cakehat.database.Group group)
    {
        return new File(new File(new File(
                getUserWorkspaceDir(),
                part.getAssignment().getName()),
                part.getName()),
                group.getName());
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
    public File getGMLDir(cakehat.config.handin.DistributablePart part)
    {
        return new File(new File(new File(new File(
                getCakehatDir(),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear())),
                "rubrics"),
                part.getAssignment().getName()),
                part.getName());
    }

    @Override
    public File getGroupGMLFile(cakehat.config.handin.DistributablePart part, cakehat.database.Group group)
    {
        return new File(getGMLDir(part), group.getName() + ".gml");
    }
    
    @Override
    public File getHandinDir(cakehat.config.handin.Handin handin)
    {
        return new File(new File(new File(
                getCourseDir(),
                "handin"),
                handin.getAssignment().getName()),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear()));
    }
}