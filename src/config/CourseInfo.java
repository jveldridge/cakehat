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

    public Collection<Assignment> getAssignments()
    {
        return _config.getAssigments();
    }

    public Collection<Assignment> getHandinAssignments()
    {
        Vector<Assignment> asgns = new Vector<Assignment>();

        for(Assignment asgn : getAssignments())
        {
            if(asgn.hasHandinPart())
            {
                asgns.add(asgn);
            }
        }

        return asgns;
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