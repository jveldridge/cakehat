package gradesystem.config;

import java.util.Collection;
import java.util.Set;
import gradesystem.Allocator;
import gradesystem.handin.DistributablePart;
import java.io.File;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Constants used throughout the program. This information is based off of the
 * data specified in the configuration file.
 *
 * @author jak2
 */
public class CourseInfoImpl implements CourseInfo
{
    //For testing purposes, specifies which course this is being run for
    private final static String TESTING_COURSE = "cs000";

    String _course = null;
    public String getCourse()
    {
        if(_course == null)
        {
            //Get the location of where this code is running
            String loc = ConfigurationParserHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();

            //If this is actually the jar we are running from
            if(loc.endsWith("jar") && loc.startsWith("/course/cs"))
            {
                String course = loc.replace("/course/", "");
                course = course.substring(0, course.indexOf("/"));

                return course;
            }
            else
            {
                System.out.println("Using hard-coded test value for course: " + TESTING_COURSE);

                _course = TESTING_COURSE;
            }
        }

        return _course;
    }
    
    public File getConfigurationFile()
    {
        File configFile = new File(new File(new File(new File(new File(new File
                ("/course"),
                getCourse()),
                "/.cakehat"),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear())),
                "/config"),
                "config.xml");

        return configFile;
    }

    public String getTestAccount()
    {
        return getCourse() + "000";
    }

    public String getStudentGroup()
    {
        return getCourse() + "student";
    }

    public String getTAGroup()
    {
        return getCourse() + "ta";
    }

    public String getCourseDir()
    {
        return "/course/" + getCourse() + "/";
    }

    public String getHandinDir()
    {
        return getCourseDir() + "handin/";
    }

    public String getGradingDir()
    {
        return getCourseDir() + ".cakehat/";
    }

    public String getRubricDir()
    {
        return getGradingDir() + Allocator.getCalendarUtilities().getCurrentYear() + "/rubrics/";
    }

    public String getDatabaseFilePath()
    {
        return getGradingDir() + Allocator.getCalendarUtilities().getCurrentYear() + "/database/database.db";
    }

    public String getDatabaseBackupDir()
    {
        return getGradingDir() + Allocator.getCalendarUtilities().getCurrentYear() + "/database/backups/";
    }

    public String getEmailDomain()
    {
        return "cs.brown.edu";
    }

    public String getCakehatEmailAddress()
    {
        return "cakehat@cs.brown.edu";
    }





    // These methods are deprecated and remain to avoid causing massive merge
    // issues.

    public Collection<Assignment> getAssignments()
    {
        return Allocator.getConfigurationInfo().getAssignments();
    }

    public EmailAccount getEmailAccount()
    {
        return Allocator.getConfigurationInfo().getEmailAccount();
    }

    public Collection<String> getNotifyAddresses()
    {
        return Allocator.getConfigurationInfo().getNotifyAddresses();
    }

    public int getMinutesOfLeniency()
    {
        return Allocator.getConfigurationInfo().getMinutesOfLeniency();
    }

    public SubmitOptions getSubmitOptions()
    {
        return Allocator.getConfigurationInfo().getSubmitOptions();
    }

    public Collection<TA> getTAs()
    {
        return Allocator.getConfigurationInfo().getTAs();
    }

    public Collection<TA> getDefaultGraders()
    {
        return Allocator.getConfigurationInfo().getDefaultGraders();
    }

    public Collection<TA> getNonDefaultGraders()
    {
        return Allocator.getConfigurationInfo().getNonDefaultGraders();
    }

    public Collection<TA> getAdmins()
    {
        return Allocator.getConfigurationInfo().getAdmins();
    }

    public TA getTA(String taLogin)
    {
        return Allocator.getConfigurationInfo().getTA(taLogin);
    }

    public DistributablePart getDistributablePart(String partID)
    {
        return Allocator.getConfigurationInfo().getDistributablePart(partID);
    }

    public Collection<Assignment> getHandinAssignments()
    {
        return Allocator.getConfigurationInfo().getHandinAssignments();
    }

    public Collection<Assignment> getNonHandinAssignments()
    {
        return Allocator.getConfigurationInfo().getNonHandinAssignments();
    }

    public Collection<Assignment> getLabAssignments()
    {
        return Allocator.getConfigurationInfo().getLabAssignments();
    }

    public Set<Integer> getAssignmentsWithChoices()
    {
        throw new NotImplementedException();
    }
}