package config;

import java.util.Collection;
import java.util.Vector;

/**
 * Object representation of the XML config file. Only meant to be used by
 * ConfigurationParser and ConfigurationManager.
 *
 * @author jak2
 */
class Configuration
{
    private Vector<Assignment> _assignments = new Vector<Assignment>();
    private Vector<String> _notifyAddresses = new Vector<String>();
    private Vector<TA> _tas = new Vector<TA>();
    private EmailAccount _emailAccount;
    private String _course;
    private int _leniency;

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
        _notifyAddresses.add(address);
    }

    Collection<String> getNotifyAddresses()
    {
       return _notifyAddresses;
    }

    void addTA(TA ta)
    {
        _tas.add(ta);
    }

    Collection<TA> getTAs()
    {
        return _tas;
    }

    void setCourse(String course)
    {
        _course = course;
    }

    String getCourse()
    {
        return _course;
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
        _assignments.add(asgn);
    }

    Collection<Assignment> getAssigments()
    {
        return _assignments;
    }
}