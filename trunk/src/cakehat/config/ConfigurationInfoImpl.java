package cakehat.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import cakehat.config.handin.DistributablePart;
import cakehat.views.shared.ErrorView;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Information that comes from the configuration file or is built directly on
 * top of it. All of the data it returns is immutable.
 *
 * @author jak2
 */
public class ConfigurationInfoImpl implements ConfigurationInfo
{
    private Configuration _config;

    public ConfigurationInfoImpl()
    {
        try
        {
            _config = ConfigurationParser.parse();
        }
        catch (ConfigurationException ex)
        {
            System.err.println("cakehat was unable to parse the configuration file.");
            System.err.println("Please fix the issue specified by the exception and " +
                    "then relaunch cakehat.\n");
            ex.printStackTrace();

            System.exit(-1);
        }
        
        //Check validity
        ConfigurationValidator validator = new ConfigurationValidator(_config);

        //Check for errors
        StringWriter errorWriter = new StringWriter();
        if(!validator.checkForErrors(errorWriter))
        {
            String msg = "The following are configuration errors.\n" +
                    "cakehat cannot run until these issues are resolved.\n\n" +
                    "Errors:\n" +
                    errorWriter.toString();

            JOptionPane.showMessageDialog(null, msg,
                    "Configuration Errors", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        //Check for warnings
        StringWriter warningWriter = new StringWriter();
        if(!validator.checkForWarnings(warningWriter))
        {
            String msg = "The following are configuration warnings.\n" +
                    "cakehat will run, but problems may arise.\n\n" +
                    "Warnings:\n" +
                    warningWriter.toString();

            JOptionPane.showMessageDialog(null, msg,
                    "Configuration Warnings", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**************************************************************************\
    |*                    Directly from configuration data                    *|
    \**************************************************************************/

    public int getMinutesOfLeniency()
    {
        return _config.getLeniency();
    }

    public SubmitOptions getSubmitOptions()
    {
        return _config.getSubmitOptions();
    }

    public EmailAccount getEmailAccount()
    {
        return _config.getEmailAccount();
    }

    public List<Assignment> getAssignments()
    {
        return _config.getAssignments();
    }

    public List<TA> getTAs()
    {
        return _config.getTAs();
    }

    public List<String> getNotifyAddresses()
    {
        return _config.getNotifyAddresses();
    }

    /**************************************************************************\
    |*                     Built from configuration data                      *|
    \**************************************************************************/

    private List<TA> _defaultGraders;
    public List<TA> getDefaultGraders()
    {
        if(_defaultGraders == null)
        {
            ImmutableList.Builder<TA> builder = ImmutableList.builder();

            for(TA ta : getTAs())
            {
                if(ta.isDefaultGrader())
                {
                    builder.add(ta);
                }
            }

            _defaultGraders = builder.build();
        }

        return _defaultGraders;
    }

    private List<TA> _nonDefaultGraders;
    public List<TA> getNonDefaultGraders()
    {
        if (_nonDefaultGraders == null)
        {
            ImmutableList.Builder<TA> builder = ImmutableList.builder();

            for (TA ta : getTAs())
            {
                if (!ta.isDefaultGrader())
                {
                    builder.add(ta);
                }
            }

            _nonDefaultGraders = builder.build();
        }

        return _nonDefaultGraders;
    }

    private List<TA> _admins;
    public List<TA> getAdmins()
    {
        if(_admins == null)
        {
            ImmutableList.Builder<TA> builder = ImmutableList.builder();

            for(TA ta : getTAs())
            {
                if(ta.isAdmin())
                {
                    builder.add(ta);
                }
            }

            _admins = builder.build();
        }

        return _admins;
    }

    private Map<String, TA> _taMap;
    public TA getTA(String taLogin)
    {
        if(_taMap == null)
        {
            ImmutableMap.Builder<String, TA> builder = ImmutableMap.builder();

            for(TA ta : getTAs())
            {
                builder.put(ta.getLogin(), ta);
            }

            _taMap = builder.build();
        }

        return _taMap.get(taLogin);
    }

    private Map<String, DistributablePart> _distributablePartMap;
    public DistributablePart getDistributablePart(String partID)
    {
        if(_distributablePartMap == null)
        {
            ImmutableMap.Builder<String, DistributablePart> builder = ImmutableMap.builder();
            for(Assignment asgn : getHandinAssignments())
            {
                for(DistributablePart part : asgn.getDistributableParts())
                {
                    builder.put(part.getDBID(), part);
                }
            }

            _distributablePartMap = builder.build();
        }

        return _distributablePartMap.get(partID);
    }

    private List<Assignment> _handinAssignments;
    public List<Assignment> getHandinAssignments()
    {
        if(_handinAssignments == null)
        {
            ImmutableList.Builder<Assignment> builder = ImmutableList.builder();

            for(Assignment asgn : getAssignments())
            {
                if(asgn.hasHandin())
                {
                    builder.add(asgn);
                }
            }

            _handinAssignments = builder.build();
        }

        return _handinAssignments;
    }

    private List<Assignment> _nonHandinAssignments;
    public List<Assignment> getNonHandinAssignments()
    {
        if(_nonHandinAssignments == null)
        {
            ImmutableList.Builder<Assignment> builder = ImmutableList.builder();

            for(Assignment asgn : getAssignments())
            {
                if(asgn.hasNonHandinParts())
                {
                    builder.add(asgn);
                }
            }

            _nonHandinAssignments = builder.build();
        }

        return _nonHandinAssignments;
    }

    private List<Assignment> _labAssignments;
    public List<Assignment> getLabAssignments()
    {
        if(_labAssignments == null)
        {
            ImmutableList.Builder<Assignment> builder = ImmutableList.builder();

            for(Assignment asgn : getAssignments())
            {
                if(asgn.hasLabParts())
                {
                    builder.add(asgn);
                }
            }

            _labAssignments = builder.build();
        }

        return _labAssignments;
    }
}