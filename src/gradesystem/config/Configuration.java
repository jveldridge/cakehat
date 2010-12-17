package gradesystem.config;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;
import gradesystem.utils.Allocator;

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
    private SubmitOptions _submitOptions;

    /**
     * Checks the validity of the configuration values. This checks for
     * invalid or unreasonable values unrelated to parsing issues.
     *
     * Checks the validity of HANDIN's RUN, DEMO, and TESTER properties.
     *
     * Checks if EARLY, ONTIME, and LATE dates are reasonable. They are
     * not considered reasonable if the dates are not this year.
     *
     * Checks that Assignment names are unique, that for each Assignment each
     * of its Parts have unique names, and that all lab numbers are unique.
     *
     * Returns the validity and writers errors to the writer.
     *
     * @param writer to write errors to
     * @return validity
     */
    boolean checkValidity(StringWriter writer)
    {
        boolean valid = true;

        //Check validity of assignments
        for(Assignment asgn : this.getAssigments())
        {
            if(asgn.hasHandinPart())
            {
                HandinPart part = asgn.getHandinPart();

                //Check if the handin parts have reasonable dates
                valid &= part.getTimeInformation().areDatesReasonable(writer, part);

                if(part instanceof CodeHandin)
                {
                    CodeHandin code = (CodeHandin) part;

                    //Check if the RUN, DEMO, & TESTER properties are properly configured
                    valid &= code.checkValidity(writer);
                }
            }
        }

        valid &= this.checkUniqueAssignments(writer);

        //Check validity of TAs
        for (TA ta : this.getTAs()) {
            boolean isLoginValid = Allocator.getUserUtilities().isLoginValid(ta.getLogin());
            boolean isInTAGroup = Allocator.getUserUtilities().isInTAGroup(ta.getLogin());

            if (!isLoginValid) {
                writer.append(String.format("Login \"%s\" is not valid.\n", ta.getLogin()));
            }
            if (!isInTAGroup) {
                writer.append(String.format("Login \"%s\" is not in the TA group.\n", ta.getLogin()));
            }

            valid &= (isLoginValid && isInTAGroup);
        }

        return valid;
    }

    /**
     * Checks that Assignment names are unique, that for each Assignment its Part
     * names are unique, and that all lab numbers are unique.
     *
     * @param writer to write error messages to
     * @return
     */
    private boolean checkUniqueAssignments(StringWriter writer)
    {
        boolean valid = true;

        HashSet<String> asgnNames = new HashSet<String>();
        HashSet<Integer> labNumbers = new HashSet<Integer>();

        for(Assignment asgn : this.getAssigments())
        {
            //If name is not unique
            if(asgnNames.contains(asgn.getName()))
            {
                valid = false;
                writer.append(asgn.getName() + " is not a unique ASSIGNMENT name. \n");
            }
            asgnNames.add(asgn.getName());

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

    void setSubmitOptions(SubmitOptions options)
    {
        _submitOptions = options;
    }

    SubmitOptions getSubmitOptions()
    {
        return _submitOptions;
    }
}