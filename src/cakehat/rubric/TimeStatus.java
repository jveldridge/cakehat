package cakehat.rubric;

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
@Deprecated
public enum TimeStatus
{
    EARLY("Early"), ON_TIME("On Time"), LATE("Late"), NC_LATE("NC Late");
    
    private String _prettyPrintName;
    
    TimeStatus(String prettyPrintName) {
        _prettyPrintName = prettyPrintName;
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
        return _prettyPrintName;
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