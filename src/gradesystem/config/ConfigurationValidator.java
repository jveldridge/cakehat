package gradesystem.config;

import com.google.common.collect.Multimap;
import gradesystem.Allocator;
import gradesystem.handin.Handin;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import utils.system.NativeException;

/**
 * Performs validation of configuration data. In order for the configuration to
 * be built it must be valid XML, but that does not mean that all of the
 * information in it is valid. This class checks that the data contained is
 * accurate and/or reasonable.
 *
 * @author jak2
 */
class ConfigurationValidator
{
    private final Configuration _config;

    ConfigurationValidator(Configuration config)
    {
        _config = config;
    }

    /**
     * Checks for situations in the configuration that are problematic but will
     * not prevent cakehat from operating.
     *
     * @param writer to write warnings to
     * @return <code>true</code> if no warnings
     */
    boolean checkForWarnings(StringWriter writer)
    {
        boolean valid = true;

        valid &= this.checkTAForWarnings(writer);
        valid &= this.checkAssignmentForWarnings(writer);

        return valid;
    }

    /**
     * Checks that all of the TA logins are valid logins and if they are, that
     * they belong to the course's TA group.
     * 
     * @param writer
     * @return
     */
    private boolean checkTAForWarnings(StringWriter writer)
    {
        boolean valid = true;

        for(TA ta : _config.getTAs())
        {
            if(Allocator.getUserUtilities().isLoginValid(ta.getLogin()))
            {
                try
                {
                    if(!Allocator.getUserServices().isInTAGroup(ta.getLogin()))
                    {
                        writer.append(String.format("Login \"%s\" is not in the TA group.\n", ta.getLogin()));

                        valid = false;
                    }
                }
                catch(NativeException e)
                {
                    writer.append("Members of TA group could not be retrieved. (NativeException)\n");
                }
            }
            else
            {
                writer.append(String.format("Login \"%s\" is not valid.\n", ta.getLogin()));

                valid = false;
            }
        }

        return valid;
    }

    /**
     * Checks if EARLY, ONTIME, and LATE dates are reasonable for each
     * Assignment's handin. They are not considered reasonable if the dates are
     * not this year.
     *
     * @param writer to write error messages to
     * @return
     */
    private boolean checkAssignmentForWarnings(StringWriter writer)
    {
        boolean valid = true;

        for(Assignment asgn : _config.getAssignments())
        {
            //Check if the Handin's information has reasonable dates
            if(asgn.hasHandin())
            {
                Handin handin = asgn.getHandin();
                valid &= areDatesReasonable(writer, handin);
            }
        }

        return valid;
    }

    /**
     * Determines if the dates are reasonable. Reasonable is determined
     * as being in the same calendar year.
     *
     * @param writer to write error messages to
     * @param handin
     * @return whether dates are reasonable
     */
    private boolean areDatesReasonable(StringWriter writer, Handin handin)
    {
        TimeInformation timeInfo = handin.getTimeInformation();

        String msgBeginning = handin.getAssignment().getName() + " HANDIN's";

        Calendar thisYear = GregorianCalendar.getInstance();
        thisYear.set(Calendar.YEAR, Allocator.getCalendarUtilities().getCurrentYear());
        thisYear.set(Calendar.MONTH, 0);
        thisYear.set(Calendar.DAY_OF_MONTH, 1);
        thisYear.set(Calendar.HOUR_OF_DAY, 0);
        thisYear.set(Calendar.MINUTE, 0);
        thisYear.set(Calendar.SECOND, 0);
        thisYear.set(Calendar.MILLISECOND, 0);

        boolean valid = true;

        if(timeInfo.getEarlyDate() != null && timeInfo.getEarlyDate().before(thisYear))
        {
            valid = false;

            writer.append(msgBeginning + " EARLY date is likely incorrect." +
                          " Date specified: " +
                          Allocator.getCalendarUtilities().getCalendarAsString(timeInfo.getEarlyDate()) + "\n");
        }
        if(timeInfo.getOntimeDate() != null && timeInfo.getOntimeDate().before(thisYear))
        {
            valid = false;

            writer.append(msgBeginning + " ONTIME date is likely incorrect." +
                          " Date specified: " +
                          Allocator.getCalendarUtilities().getCalendarAsString(timeInfo.getOntimeDate()) + "\n");
        }
        if(timeInfo.getLateDate() != null && timeInfo.getLateDate().before(thisYear))
        {
            valid = false;

            writer.append(msgBeginning + " LATE date is likely incorrect." +
                          " Date specified: " +
                          Allocator.getCalendarUtilities().getCalendarAsString(timeInfo.getLateDate()) + "\n");
        }

        return valid;
    }

    /**
     * Checks for situations in the configuration that will prevent cakehat from
     * operating properly. If this method returns <code>false</code> cakehat
     * should not be allowed to run, except if it is to fix the configuration.
     *
     * @param writer to write errors to
     * @return <code>true</code> if no errors
     */
    boolean checkForErrors(StringWriter writer)
    {
        boolean valid = true;
        
        valid &= checkSubmitOptionsForErrors(writer);
        valid &= checkAssignmentForErrors(writer);
        
        return valid;
    }

    /**
     * Checks that Assignment names and numbers are unique, that for each
     * Assignment its Part names are unique, tgat Parts with the same number
     * have the same points, and that all lab numbers are unique.
     *
     * @param writer
     * @return
     */
    private boolean checkAssignmentForErrors(StringWriter writer)
    {
        boolean valid = true;

        HashSet<String> asgnNames = new HashSet<String>();
        HashSet<Integer> asgnNumbers = new HashSet<Integer>();
        HashSet<Integer> labNumbers = new HashSet<Integer>();

        for(Assignment asgn : _config.getAssignments())
        {

            //If Assignment name is not unique
            if(asgnNames.contains(asgn.getName()))
            {
                valid = false;
                writer.append(asgn.getName() + " is not a unique ASSIGNMENT name.\n");
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
                                  " is not a unique PART name.\n");
                }
                partNames.add(part.getName());
            }

            //Check that each lab number is unique
            for(LabPart lab : asgn.getLabParts())
            {
                if(labNumbers.contains(lab.getLabNumber()))
                {
                    valid = false;
                    writer.append(asgn.getName() + "'s " + lab.getName() +
                                  " does not have a unique LAB-NUMBER: " +
                                  lab.getLabNumber() + ".\n");
                }
                labNumbers.add(lab.getLabNumber());
            }

            //Check that for mutually exclusive parts (those with the same
            //number) for a given assignment have the same point value
            Multimap<Integer, Part> partsMap = asgn.getPartsMultimap();
            for(int partNum : partsMap.keySet())
            {
                HashSet<Integer> points = new HashSet<Integer>();

                Collection<Part> parts = partsMap.get(partNum);
                for(Part part : parts)
                {
                    points.add(part.getPoints());
                }

                //If there are multiple point values for mutually exclusive parts
                if(points.size() != 1)
                {
                    valid = false;

                    writer.append("ASSIGNMENT " + asgn.getName() + " has " +
                            "differing POINTS values for PARTs: " + parts + "\n" +
                            "PARTs with the same NUMBER must have the same POINTS values.\n");
                }
            }
        }

        return valid;
    }

    /**
     * Checks that if NOTIFY is set to FALSE then EMAIL-GRD is not TRUE.
     *
     * @param writer
     * @return
     */
    private boolean checkSubmitOptionsForErrors(StringWriter writer)
    {
        boolean valid = true;

        SubmitOptions options = _config.getSubmitOptions();

        if(!options.isNotifyDefaultEnabled() && options.isEmailGrdDefaultEnabled())
        {
            valid = false;
            writer.append("TRUE value for EMAIL-GRD requires a TRUE value for " +
                    "NOTIFY.\n");
        }

        return valid;
    }
}