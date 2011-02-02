package gradesystem.config;

import gradesystem.handin.DistributablePart;
import gradesystem.views.shared.ErrorView;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;

/**
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
                    JOptionPane.showMessageDialog(null, writer.toString(), "Configuration Issues", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (ConfigurationException ex)
            {
                new ErrorView(ex);
            }
        }
    }


    public Collection<Assignment> getAssignments()
    {
        return _config.getAssignments();
    }

    public EmailAccount getEmailAccount()
    {
        return _config.getEmailAccount();
    }

    public Collection<String> getNotifyAddresses()
    {
        return _config.getNotifyAddresses();
    }

    public int getMinutesOfLeniency()
    {
        return _config.getLeniency();
    }

    public SubmitOptions getSubmitOptions()
    {
        return _config.getSubmitOptions();
    }

    public Collection<TA> getTAs()
    {
        return _config.getTAs();
    }

        //             Built from configuration data

    private Collection<TA> _defaultGraders = null;
    public Collection<TA> getDefaultGraders()
    {
        if(_defaultGraders == null)
        {
            _defaultGraders = new ArrayList<TA>();

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
    public Collection<TA> getNonDefaultGraders()
    {
        if (_nonDefaultGraders == null)
        {
            _nonDefaultGraders = new ArrayList<TA>();

            for (TA ta : getTAs())
            {
                if (!ta.isDefaultGrader())
                {
                    _nonDefaultGraders.add(ta);
                }
            }
        }

        return _nonDefaultGraders;
    }

    private Collection<TA> _admins = null;
    public Collection<TA> getAdmins()
    {
        if(_admins == null)
        {
            _admins = new ArrayList<TA>();

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

    private Map<String, TA> _taMap = null;
    public TA getTA(String taLogin)
    {
        if(_taMap == null)
        {
            _taMap = new HashMap<String, TA>();

            for(TA ta : getTAs())
            {
                _taMap.put(ta.getLogin(), ta);
            }
        }

        return _taMap.get(taLogin);
    }

    private Map<String, DistributablePart> _distributablePartMap = null;
    public DistributablePart getDistributablePart(String partID)
    {
        if(_distributablePartMap == null)
        {
            _distributablePartMap = new HashMap<String, DistributablePart>();
            for(Assignment asgn : getHandinAssignments())
            {
                for(DistributablePart part : asgn.getDistributableParts())
                {
                    _distributablePartMap.put(part.getDBID(), part);
                }
            }
        }

        return _distributablePartMap.get(partID);
    }

    private Collection<Assignment> _handinAssignments = null;
    public Collection<Assignment> getHandinAssignments()
    {
        if(_handinAssignments == null)
        {
            _handinAssignments = new ArrayList<Assignment>();

            for(Assignment asgn : getAssignments())
            {
                if(asgn.hasHandin())
                {
                    _handinAssignments.add(asgn);
                }
            }
        }

        return _handinAssignments;
    }

    private Collection<Assignment> _nonHandinAssignments = null;
    public Collection<Assignment> getNonHandinAssignments()
    {
        if(_nonHandinAssignments == null)
        {
            _nonHandinAssignments = new ArrayList<Assignment>();

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
    public Collection<Assignment> getLabAssignments()
    {
        if(_labAssignments == null)
        {
            _labAssignments = new ArrayList<Assignment>();

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
}