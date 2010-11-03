package rubric;

import config.GradeUnits;
import config.HandinPart;
import config.LatePolicy;

/**
 * Represents the status of a turned in assignment. Not all of these are
 * applicable given the LatePolicy.
 * For LatePolicy.NO_LATE: ON_TIME, NC_LATE
 * For LatePolicy.DAILY_DEDUCTION: ON_TIME, LATE
 * For MULTIPLE_DEADLINES: EARLY, ON_TIME, LATE, NC_LATE
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
     * For MULTIPLE_DEADLINES: EARLY, ON_TIME, LATE, NC_LATE
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

    /**
     * Calculates the appropriate deduction based on which TimeStatus value this
     * is, what the LatePolicy governing this assignment is, and whether the
     * LatePolicy applies to the entire rubric (AFFECT-ALL="TRUE") or just the
     * handin parts of the rubric (AFFECT-ALL="FALSE").
     *
     * If NC_LATE, then all points will be deducted. (Whether that will be all
     * of the points in the assignment or all the points in the handin depends
     * on the AFFECT-ALL value.)
     *
     * @param part
     * @param rubric
     * @return
     */
    double getDeduction(HandinPart part, Rubric rubric)
    {
        double outOf = 0;
        double points = 0;
        // if it affects the entire assignment
        if(part.getTimeInformation().affectsAll())
        {
            outOf = rubric.getTotalRubricOutOf();
            points = rubric.getTotalRubricPoints();
        }
        // if it affects just the handin part
        else
        {
            outOf = rubric.getTotalHandinOutOf();
            points = rubric.getTotalHandinPoints();
        }

        // If NC Late, negate all points
        if(this == NC_LATE)
        {
            return -points;
        }

        LatePolicy policy = part.getTimeInformation().getLatePolicy();
        GradeUnits units = part.getTimeInformation().getGradeUnits();

        if(policy == LatePolicy.DAILY_DEDUCTION)
        {
            if(this == LATE)
            {
                double dailyDeduction = 0;
                if(units == GradeUnits.PERCENTAGE)
                {
                    double percent = part.getTimeInformation().getOntimeValue() / 100.0;
                    dailyDeduction = outOf * percent;
                }
                else if(units == GradeUnits.POINTS)
                {
                    dailyDeduction = part.getTimeInformation().getOntimeValue();
                }

                return dailyDeduction * rubric.getDaysLate();
            }
        }
        else if(policy == LatePolicy.MULTIPLE_DEADLINES)
        {
            // get appropriate value
            double value = 0;
            if(this == EARLY)
            {
                value = part.getTimeInformation().getEarlyValue();
            }
            else if(this == ON_TIME)
            {
                value = part.getTimeInformation().getOntimeValue();
            }
            else if(this == LATE)
            {
                value = part.getTimeInformation().getLateValue();
            }

            // deduction based on grade units
            if(units == GradeUnits.PERCENTAGE)
            {
               return outOf * (value / 100.0);
            }
            else if(units == GradeUnits.POINTS)
            {
                return value;
            }
        }

        return 0;
    }

}