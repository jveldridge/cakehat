package cakehat.config;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Object representation of the XML config file. Only meant to be used within
 * the config package.
 *
 * @author jak2
 */
@Deprecated
class Configuration
{
    private final ImmutableList.Builder<Assignment> _assignmentsBuilder = ImmutableList.builder();
    private final ImmutableList.Builder<String> _notifyAddressesBuilder = ImmutableList.builder();
    private final ImmutableList.Builder<TA> _tasBuilder = ImmutableList.builder();
    private EmailAccount _emailAccount;
    private int _leniency;
    private SubmitOptions _submitOptions;

    void setEmailAccount(EmailAccount account)
    {
        _emailAccount = account;
    }

    EmailAccount getEmailAccount()
    {
        return _emailAccount;
    }

    void addNotifyAddress(String address)
    {
        _notifyAddressesBuilder.add(address);
    }

    private List<String> _notifyAddresses;
    List<String> getNotifyAddresses()
    {
        if(_notifyAddresses == null)
        {
            _notifyAddresses = _notifyAddressesBuilder.build();
        }
        
        return _notifyAddresses;
    }

    void addTA(TA ta)
    {
        _tasBuilder.add(ta);
    }

    private List<TA> _tas;
    List<TA> getTAs()
    {
        if(_tas == null)
        {
            _tas = _tasBuilder.build();
        }

        return _tas;
    }

    void setLeniency(int minutes)
    {
        _leniency = minutes;
    }

    int getLeniency()
    {
        return _leniency;
    }

    void addAssignment(Assignment asgn)
    {
        _assignmentsBuilder.add(asgn);
    }

    private List<Assignment> _assignments;
    List<Assignment> getAssignments()
    {
        if(_assignments == null)
        {
            _assignments = _assignmentsBuilder.build();
        }

        return _assignments;
    }

    void setSubmitOptions(SubmitOptions options)
    {
        _submitOptions = options;
    }

    SubmitOptions getSubmitOptions()
    {
        return _submitOptions;
    }
}