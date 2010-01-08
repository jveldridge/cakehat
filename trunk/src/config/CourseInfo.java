package config;

import java.util.Collection;
import java.util.Vector;
import utils.Allocator;
import utils.ErrorView;

/**
 * Constants used throughout the program. This information is based off of the
 * data specified in the configuration file.
 *
 * @author jak2
 */
public class CourseInfo
{
    //Just in case this class is created multiple times, only parse once
    private static Configuration _config = null;

    /**
     * Don't directly create this class, access it via util.Allocator
     */
    public CourseInfo()
    {
        if(_config == null)
        {
            try
            {
                _config = ConfigurationParser.parse();
            }
            catch (ConfigurationException ex)
            {
                new ErrorView(ex);
            }
        }
    }

    //           Directly from configuration

    /**
     * Returns a collection of all assignments.
     *
     * @return
     */
    public Collection<Assignment> getAssignments()
    {
        return _config.getAssigments();
    }

    public EmailAccount getEmailAccount()
    {
        return _config.getEmailAccount();
    }

    public Collection<String> getNotifyAddresses()
    {
        return _config.getNotifyAddresses();
    }

    public String getCourse()
    {
        return _config.getCourse();
    }

    public int getMinutuesOfLeniency()
    {
        return _config.getLeniency();
    }

    //             Built from configuration data

    private Collection<Assignment> _handinAssignments = null;
    /**
     * Returns a collection of all assignments that have a handin part.
     * 
     * @return
     */
    public Collection<Assignment> getHandinAssignments()
    {
        if(_handinAssignments == null)
        {
            _handinAssignments = new Vector<Assignment>();

            for(Assignment asgn : getAssignments())
            {
                if(asgn.hasHandinPart())
                {
                    _handinAssignments.add(asgn);
                }
            }
        }


        return _handinAssignments;
    }

    private Collection<Assignment> _nonHandinAssignments = null;
    /**
     * Returns a collection of all assignment that have a nonhandin part.
     *
     * @return
     */
    public Collection<Assignment> getNonHandinAssignments()
    {
        if(_nonHandinAssignments == null)
        {
            _nonHandinAssignments = new Vector<Assignment>();

            for(Assignment asgn : getAssignments())
            {
                if(asgn.hasNonHandinParts())
                {
                    _nonHandinAssignments.add(asgn);
                }
            }
        }


        return _nonHandinAssignments;
    }

    private Collection<Assignment> _labAssignments = null;
    /**
     * Returns a collection of all assignments that have a lab part.
     *
     * @return
     */
    public Collection<Assignment> getLabAssignments()
    {
        if(_labAssignments == null)
        {
            _labAssignments = new Vector<Assignment>();

            for(Assignment asgn : getAssignments())
            {
                if(asgn.hasLabParts())
                {
                    _labAssignments.add(asgn);
                }
            }
        }


        return _labAssignments;
    }

    public String getTestAccount(){
        return getCourse() + "000";
    }

    public String getStudentGroup(){
        return getCourse() + "student";
    }

    public String getCourseDir(){
        return "/course/" + getCourse() + "/";
    }

    public String getHandinDir(){
        return getCourseDir() + "handin/";
    }

    //TODO: Switch to cakehat
      public String getGradingDir(){
        return getCourseDir() + "grading/";
    }

    public String getLabsDir(){
        return getGradingDir() + "labs/";
    }

    //TODO: have directory just for database and config file
    //TODO: Change back to config.xml
    public String getConfigFilePath(){
        return getGradingDir() + Allocator.getGeneralUtilities().getCurrentYear() + "/" + "config_new_test.xml";
    }

    public String getRubricDirectoryPath() {
        return getGradingDir() + "rubrics/" + Allocator.getGeneralUtilities().getCurrentYear() + "/";
    }

    //TODO: have directory just for database and config file
    public String getDatabaseFilePath(){
        return getGradingDir() + "bin/" + Allocator.getGeneralUtilities().getCurrentYear() + "/cs015Database.db";
    }

    //TODO: have directory just for database and config file
    public String getDatabaseBackupDir(){
        return getGradingDir() + "bin/" + Allocator.getGeneralUtilities().getCurrentYear() + "/bak/";
    }

    // E-mail

    public String getEmailDomain(){
        return "cs.brown.edu";
    }

    public String getEmailHost(){
        return "smtps.cs.brown.edu";
    }

    public String getEmailPort(){
        return "465";
    }
}