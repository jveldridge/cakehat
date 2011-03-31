package cakehat.rubric;

import cakehat.config.LatePolicy;

/**
 * Represents the status of a turned in assignment. Not all of these are
 * applicable given the LatePolicy.
 * For LatePolicy.NO_LATE: ON_TIME, NC_LATE
 * For LatePolicy.DAILY_DEDUCTION: ON_TIME, LATE
 * For LatePolicy.MULTIPLE_DEADLINES: EARLY, ON_TIME, LATE, NC_LATE
 *
 * @author spoletto
 * @author jak2
 */
public enum TimeStatus
{
    EARLY, ON_TIME, LATE, NC_LATE;

    /**
     * For LatePolicy.NO_LATE: ON_TIME, NC_LATE
     * For LatePolicy.DAILY_DEDUCTION: ON_TIME, LATE
     * For LatePolicy.MULTIPLE_DEADLINES: EARLY, ON_TIME, LATE, NC_LATE
     *
     * @param policy
     * @return
     */
    TimeStatus[] getAvailableStatuses(LatePolicy policy)
    {
        if(policy == LatePolicy.NO_LATE)
        {
            return new TimeStatus[] { ON_TIME, NC_LATE };
        }
        else if(policy == LatePolicy.MULTIPLE_DEADLINES)
        {
            return new TimeStatus[] { EARLY, ON_TIME, LATE, NC_LATE };
        }
        else if(policy == LatePolicy.DAILY_DEDUCTION)
        {
            return new TimeStatus[] { ON_TIME, LATE };
        }

        return new TimeStatus[0];
    }

    /**
     * Returns a nicely formatted String representation.
     * Only use this for display or printing purposes, never for storing in a
     * database or XML.
     *
     * @return
     */
    String getPrettyPrintName()
    {
        switch(this)
        {
            case ON_TIME:
                return "On Time";
            case LATE:
                return "Late";
            case EARLY:
                return "Early";
            case NC_LATE:
                return "NC Late";
            default:
                return "Invalid Time Status";
        }
    }

    /**
     * Returns the TimeStatus corresponding to the string passed in.
     *
     * @param statusString
     * @return
     */
    static TimeStatus getStatus(String statusString)
    {
        for(TimeStatus status : values())
        {
            if(statusString.toUpperCase().equals(status.toString()))
            {
                return status;
            }
        }

        throw new Error("Invalid status string: " + statusString);
    }
}