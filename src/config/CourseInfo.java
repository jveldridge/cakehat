package config;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.JOptionPane;
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

                //Check validity
                StringWriter writer = new StringWriter();
                //If invalid display message
                if(!_config.checkValidity(writer))
                {
                    JOptionPane.showMessageDialog(null, writer.toString(), "Configuration Issues", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (ConfigurationException ex)
            {
                new ErrorView(ex);
            }
        }
    }

    /**
     * Returns a collection of all assignments.
     *
     * @return
     */
    public Collection<Assignment> getAssignments()
    {
        return _config.getAssigments();
    }

    /**
     * Returns the email account that emails can be sent from.
     * 
     * @return
     */
    public EmailAccount getEmailAccount()
    {
        return _config.getEmailAccount();
    }

    /**
     * Returns the addresses that should be notified of actions such as grade
     * submission.
     *
     * @return
     */
    public Collection<String> getNotifyAddresses()
    {
        return _config.getNotifyAddresses();
    }

    /**
     * Returns the course name (that matches its course directory), e.g. cs015.
     *
     * @return
     */
    public String getCourse()
    {
        return _config.getCourse();
    }

    /**
     * Minutes of leniency when determing if an assignment meets a deadline.
     *
     * @return
     */
    public int getMinutesOfLeniency()
    {
        return _config.getLeniency();
    }

    /**
     * Default options for how to submit a graded assignment.
     *
     * @return
     */
    public SubmitOptions getSubmitOptions()
    {
        return _config.getSubmitOptions();
    }

    /**
     * Returns a collection of all of the TAs.
     * @return
     */
    public Collection<TA> getTAs()
    {
        return _config.getTAs();
    }

    //             Built from configuration data

    private Collection<TA> _utas = null;
    /**
     * Returns a collection of TAs that are not HTAs.
     * @return
     */
    public Collection<TA> getUTAs()
    {
        if(_utas == null)
        {
            _utas = new Vector<TA>();

            for(TA ta : getTAs())
            {
                if(!ta.isHTA())
                {
                    _utas.add(ta);
                }
            }
        }

        return _utas;
    }

    private Collection<TA> _htas = null;
    /**
     * Returns a collection of HTAs.
     * @return
     */
    public Collection<TA> getHTAs()
    {
        if(_htas == null)
        {
            _htas = new Vector<TA>();

            for(TA ta : getTAs())
            {
                if(ta.isHTA())
                {
                    _htas.add(ta);
                }
            }
        }

        return _htas;
    }

    private Collection<TA> _defaultGraders = null;
    /**
     * Returns a collection of TAs that are default graders.
     * @return
     */
    public Collection<TA> getDefaultGraders()
    {
        if(_defaultGraders == null)
        {
            _defaultGraders = new Vector<TA>();

            for(TA ta : getTAs())
            {
                if(ta.isDefaultGrader())
                {
                    _defaultGraders.add(ta);
                }
            }
        }

        return _defaultGraders;
    }

    private Collection<TA> _nonDefaultGraders = null;
    /**
     * Returns a collection of TAs who are not default graders.
     * @return
     */
    public Collection<TA> getNonDefaultGraders() {
        if (_nonDefaultGraders == null) {
            _nonDefaultGraders = new Vector<TA>();

            for (TA ta : getTAs()) {
                if (!ta.isDefaultGrader()) {
                    _nonDefaultGraders.add(ta);
                }
            }
        }

        return _nonDefaultGraders;
    }

    private Collection<TA> _admins = null;
    /**
     * Returns a collection of TAs that are default graders.
     * @return
     */
    public Collection<TA> getAdmins()
    {
        if(_admins == null)
        {
            _admins = new Vector<TA>();

            for(TA ta : getTAs())
            {
                if(ta.isAdmin())
                {
                    _admins.add(ta);
                }
            }
        }

        return _admins;
    }

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

    /**
     * The course's test account.
     *
     * <course>000
     *
     * @return
     */
    public String getTestAccount()
    {
        return getCourse() + "000";
    }

    /**
     * The course's student group.
     *
     * <course>student
     *
     * @return
     */
    public String getStudentGroup()
    {
        return getCourse() + "student";
    }

    /**
     * Course directory.
     *
     * /course/<course>/
     *
     * @return
     */
    public String getCourseDir()
    {
        return "/course/" + getCourse() + "/";
    }

    /**
     * Handin directory.
     *
     * /course/<course>/handin/
     *
     * @return
     */
    public String getHandinDir()
    {
        return getCourseDir() + "handin/";
    }

    /**
     * The directory where cakehat exists.
     *
     * /course/<course>/.cakehat/
     *
     * @return
     */
    public String getGradingDir()
    {
        return getCourseDir() + ".cakehat/";
    }

    /**
     * The directory where the lab check off data is stored.
     *
     * /course/<course>/.cakehat/<current year>/labs/
     *
     * @return
     */
    public String getLabsDir()
    {
        return getGradingDir() + Allocator.getGeneralUtilities().getCurrentYear() + "/labs/";
    }

    /**
     * The top level directory that stores all of the GML rubric files.
     *
     * /course/<course>/.cakehat/<current year>/rubrics/
     *
     * @return
     */
    public String getRubricDir()
    {
        return getGradingDir() + Allocator.getGeneralUtilities().getCurrentYear() + "/rubrics/";
    }

    /**
     * The path to the database file.
     *
     * /course/<course>/.cakehat/<current year>/database/database.db
     *
     * @return
     */
    public String getDatabaseFilePath()
    {
        return getGradingDir() + Allocator.getGeneralUtilities().getCurrentYear() + "/database/database.db";
    }

    /**
     * The path to the test database file.
     *
     * /course/<course>/.cakehat/<current year>/database/test_database.db
     *
     * @return
     */
    public String getTestDatabaseFilePath()
    {
        return getGradingDir() + Allocator.getGeneralUtilities().getCurrentYear() + "/database/test_database.db";
    }

    /**
     * The path to the test database format file.
     *
     * /course/<course>/.cakehat/<current year>/database/test_data.sql
     *
     * @return
     */
    public String getTestDataFilePath()
    {
        return getGradingDir() + Allocator.getGeneralUtilities().getCurrentYear() + "/database/test_data.sql";
    }

    /**
     * The directory that backups of the database are put in.
     *
     * /course/<course>/.cakehat/<current year>/database/backups/
     *
     * @return
     */
    public String getDatabaseBackupDir()
    {
        return getGradingDir() + Allocator.getGeneralUtilities().getCurrentYear() + "/database/backups/";
    }

    /**
     * The email domain of student and TA logins: cs.brown.edu
     *
     * (Technically this isn't course info, but this is a convenient place to
     * put it.)
     *
     * @return
     */
    public String getEmailDomain()
    {
        return "cs.brown.edu";
    }

    /**
     * This is the cakehat email address.
     *
     * (Technically this isn't course info, but this is a convenient place to
     * put it.)
     */
    public String getCakehatEmailAddress()
    {
        return "cakehat@cs.brown.edu";
    }

    private Set<Integer> _asgnsWithChoices = null;
    /**
     * Returns a set of the assignments where there is a choice of which one students can do. (cs15 fnl prjs)
     * @return Set of assignment numbers
     */
    public Set<Integer> getAssignmentsWithChoices() {
        if (_asgnsWithChoices == null) {
            _asgnsWithChoices = new HashSet<Integer>();
            Set<Integer> asgnNumbersSeenAlready = new HashSet<Integer>();
            for (Assignment a : _config.getAssigments()) {
                if (asgnNumbersSeenAlready.contains(a.getNumber())) { //if we've seen an assignment already and we're seeing it again
                    _asgnsWithChoices.add(a.getNumber());
                } else { //else we've never seen it before
                    asgnNumbersSeenAlready.add(a.getNumber());
                }
            }
        }
        return _asgnsWithChoices;
    }
}
