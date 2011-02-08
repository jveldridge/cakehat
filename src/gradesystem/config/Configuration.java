package gradesystem.config;

import com.google.common.collect.ImmutableList;
import java.io.StringWriter;
import java.util.HashSet;
import gradesystem.Allocator;
import gradesystem.handin.Handin;
import java.util.List;
import utils.system.NativeException;

/**
 * Object representation of the XML config file. Only meant to be used by
 * {@link ConfigurationParser} and {@link ConfigurationInfoImpl}.
 *
 * @author jak2
 */
class Configuration
{
    private final ImmutableList.Builder<Assignment> _assignmentsBuilder = ImmutableList.builder();
    private final ImmutableList.Builder<String> _notifyAddressesBuilder = ImmutableList.builder();
    private final ImmutableList.Builder<TA> _tasBuilder = ImmutableList.builder();
    private EmailAccount _emailAccount;
    private int _leniency;
    private SubmitOptions _submitOptions;

    /**
     * Checks the validity of the configuration values. This checks for
     * invalid or unreasonable values unrelated to parsing issues.
     *
     * Returns the validity and writes errors to the writer.
     *
     * @param writer to write errors to
     * @return validity
     */
    boolean checkValidity(StringWriter writer)
    {
        boolean valid = true;

        valid &= this.checkAssignmentValidity(writer);
        valid &= this.checkTAValidity(writer);
        //TODO: Check validity of email account information

        return valid;
    }

    private boolean checkTAValidity(StringWriter writer)
    {
        boolean valid = true;

        for(TA ta : this.getTAs())
        {
            boolean isLoginValid = Allocator.getUserUtilities().isLoginValid(ta.getLogin());
            boolean isInTAGroup = false;

            try
            {
                isInTAGroup = Allocator.getUserServices().isInTAGroup(ta.getLogin());
                if(!isInTAGroup)
                {
                    writer.append(String.format("Login \"%s\" is not in the TA group.\n", ta.getLogin()));
                }
            }
            catch(NativeException e)
            {
                writer.append("Members of TA group could not be retrieved. (NativeException)\n");
            }

            if(!isLoginValid)
            {
                writer.append(String.format("Login \"%s\" is not valid.\n", ta.getLogin()));
            }

            valid &= (isLoginValid && isInTAGroup);
        }

        return valid;
    }

    /**
     * Checks that Assignment names and numbers are unique, that for each
     * Assignment its Part names are unique, and that all lab numbers are
     * unique.
     *
     * Checks if EARLY, ONTIME, and LATE dates are reasonable for each
     * Assignment's handin. They are not considered reasonable if the dates are
     * not this year.
     *
     * @param writer to write error messages to
     * @return
     */
    private boolean checkAssignmentValidity(StringWriter writer)
    {
        boolean valid = true;

        HashSet<String> asgnNames = new HashSet<String>();
        HashSet<Integer> asgnNumbers = new HashSet<Integer>();
        HashSet<Integer> labNumbers = new HashSet<Integer>();

        for(Assignment asgn : this.getAssignments())
        {
            //Check if the Handin's information has reasonable dates
            if(asgn.hasHandin())
            {
                Handin handin = asgn.getHandin();
                valid &= handin.getTimeInformation().areDatesReasonable(writer, handin);
            }

            //If Assignment name is not unique
            if(asgnNames.contains(asgn.getName()))
            {
                valid = false;
                writer.append(asgn.getName() + " is not a unique ASSIGNMENT name. \n");
            }
            asgnNames.add(asgn.getName());

            //If Assignment number is not unique
            if(asgnNumbers.contains(asgn.getNumber()))
            {
                valid = false;
                writer.append(asgn.getName() + " does not have a unique " +
                        "ASSIGNMENT number: " + asgn.getNumber() + ".\n");
            }
            asgnNumbers.add(asgn.getNumber());

            //Check each part name is unique
            HashSet<String> partNames = new HashSet<String>();
            for(Part part : asgn.getParts())
            {
                if(partNames.contains(part.getName()))
                {
                    valid = false;
                    writer.append(asgn.getName() + "'s " + part.getName() +
                                  " is not a unique PART name. \n");
                }
                partNames.add(part.getName());

                //If this is lab part, check that it has a unique lab number
                if(part instanceof LabPart)
                {
                    LabPart labPart = (LabPart) part;

                    if(labNumbers.contains(labPart.getLabNumber()))
                    {
                        valid = false;
                        writer.append(asgn.getName() + " - " + labPart.getName() +
                                      "'s LAB-NUMBER " + labPart.getLabNumber() +
                                      " is not unique. \n");
                    }
                    labNumbers.add(labPart.getLabNumber());
                }
            }
        }

        return valid;
    }

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