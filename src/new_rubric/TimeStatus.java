package new_rubric;

import config.GradeUnits;
import config.HandinPart;
import config.LatePolicy;

public enum TimeStatus
{
    ON_TIME, LATE, EARLY, NC_LATE;

    public String getPrettyPrintName()
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

    public static TimeStatus getStatus(String statusString)
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

        // LatePolicy
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