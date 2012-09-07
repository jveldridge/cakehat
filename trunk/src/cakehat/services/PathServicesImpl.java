package cakehat.services;

import cakehat.CakehatSession;
import cakehat.assignment.Action;
import cakehat.assignment.Assignment;
import cakehat.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import java.io.File;
import org.joda.time.DateTime;

/**
 *
 * @author jak2
 */
public class PathServicesImpl implements PathServices
{   
    @Override
    public File getCourseDir()
    {
        return new File("/course", CakehatSession.getCourse());
    }
    
    @Override
    public File getTABinDir()
    {
        return new File(getCourseDir(), "tabin");
    }
    
    private File getCakehatCurrentYearDir()
    {
        return new File(new File(getCourseDir(), ".cakehat"),
                Integer.toString(new DateTime().getYear()));
    }

    @Override
    public File getDatabaseFile()
    {
        return new File(new File(
                getCakehatCurrentYearDir(),
                "database"),
                "database.db");
    }

    @Override
    public File getDatabaseBackupDir()
    {
        return new File(new File(
                getCakehatCurrentYearDir(),
                "database"),
                "backups");
    }
    
    @Override
    public File getTempDir()
    {
        return new File(new File(
                getCakehatCurrentYearDir(),
                "temp"),
                CakehatSession.getUserId() + "-" + CakehatSession.getRunMode().toString());
    }

    @Override
    public File getActionTempDir(Action action)
    {
        return new File(new File(new File(new File(
                getTempDir(),
                Integer.toString(action.getPart().getGradableEvent().getAssignment().getId())),
                Integer.toString(action.getPart().getGradableEvent().getId())),
                Integer.toString(action.getPart().getId())),
                Integer.toString(action.getId()));
    }

    @Override
    public File getActionTempDir(Action action, Group group)
    {
        return new File(
                getActionTempDir(action),
                group == null ? "nogroup" : Integer.toString(group.getId()));
    }

    @Override
    public File getUnarchiveHandinDir(Part part, Group group)
    {
        return new File(new File(new File(new File(new File(
                getCakehatCurrentYearDir(),
                "handin"),
                Integer.toString(part.getGradableEvent().getAssignment().getId())),
                Integer.toString(part.getGradableEvent().getId())),
                Integer.toString(part.getId())),
                Integer.toString(group.getId()));
    }
    
    @Override
    public File getGroupGMLFile(Part part, Group group)
    {
        return new File(new File(new File(new File(new File(
                getCakehatCurrentYearDir(),
                "gml"),
                Integer.toString(part.getGradableEvent().getAssignment().getId())),
                Integer.toString(part.getGradableEvent().getId())),
                Integer.toString(part.getId())),
                Integer.toString(group.getId()) + ".gml");
    }
    
    @Override
    public File getStudentGRDFile(Assignment asgn, Student student)
    {
        return new File(new File(
                getTempDir(),
                Integer.toString(asgn.getId())),
                student.getLogin() + ".txt");
    }
}