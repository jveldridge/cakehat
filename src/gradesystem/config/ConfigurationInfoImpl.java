package gradesystem.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import gradesystem.handin.DistributablePart;
import gradesystem.views.shared.ErrorView;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 * Information that comes from the configuration file or is built directly on
 * top of it. All of the data it return is immutable.
 *
 * @author jak2
 */
public class ConfigurationInfoImpl implements ConfigurationInfo
{
    //Just in case this class is created multiple times, only parse once
    private static Configuration _config = null;

    /**
     * Don't directly create this class, access it via Allocator
     */
    public ConfigurationInfoImpl()
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
                    JOptionPane.showMessageDialog(null, writer.toString(),
                            "Configuration Issues", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (ConfigurationException ex)
            {
                new ErrorView(ex);
            }
        }
    }

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