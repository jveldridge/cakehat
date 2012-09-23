package cakehat.assignment;

import cakehat.database.Extension;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

/**
 * The deadline information for a {@link GradableEvent}.
 *
 * @author jak2
 */
public class DeadlineInfo
{
    public static enum Type
    {
        NONE, FIXED, VARIABLE;
    }
    
    private final Type _type;
    private final DateTime _earlyDate, _onTimeDate, _lateDate;
    private final Double _earlyPoints, _latePoints;
    private final Period _latePeriod;
    
    private DeadlineInfo(Type type,
            DateTime earlyDate, Double earlyPoints,
            DateTime onTimeDate,
            DateTime lateDate, Double latePoints, Period latePeriod)
    {
        _type = type;
        
        _earlyDate = earlyDate;
        _earlyPoints = earlyPoints;
        
        _onTimeDate = onTimeDate;
        
        _lateDate = lateDate;
        _latePoints = latePoints;
        _latePeriod = latePeriod;
    }
    
    /**
     * Constructs a new {@code DeadlineInfo} such that any provided occurrence date will result in a
     * {@link DeadlineResolution} with a {@link TimeStatus} of {@link TimeStatus#ON_TIME}.
     * 
     * @return
     */
    static DeadlineInfo newNoDeadlineInfo()
    {
        return new DeadlineInfo(Type.NONE, null, null, null, null, null, null);
    }
    
    /**
     * Constructs a new {@code DeadlineInfo} with a late penalty that varies based on the amount of time the occurrence
     * date is beyond the on time date.
     * <br/><br/>
     * Occurrence dates before {@code onTimeDate} are given a {@link TimeStatus} of {@code ON_TIME} and a penalty or
     * bonus of {@code 0}.
     * <br/><br/>
     * If {@code lateDate} is not specified, occurrence dates after {@code onTimeDate} are given a {@link TimeStatus}
     * of {@code LATE}. If {@code lateDate} is specified, occurrence dates after {@code onTimeDate}, but before or on
     * {@code lateDate} are given a {@link TimeStatus} of {@code LATE}. If {@code lateDate} is specified, occurrence
     * dates after {@code lateDate} are given a {@link TimeStatus} of {@code NC_LATE} and a penalty or bonus of the
     * negation of the gradable event total earned. If an occurrence date results in a {@link TimeStatus} of
     * {@code LATE} then its penalty or bonus adds {@code latePoints} for each increment of {@code latePeriod} that has
     * passed since {@code onTimeDate} such that an amount less than {@code latePeriod} is 1 increment.
     * 
     * @param onTimeDate may not be {@code null}
     * @param lateDate may be {@code null}
     * @param latePoints may not be {@code null} if {@code latePeriod} is not {@code null}
     * @param latePeriod may not be {@code null} if {@code latePoints} is not {@code null}
     * @return 
     */
    static DeadlineInfo newVariableDeadlineInfo(DateTime onTimeDate,
            DateTime lateDate, Double latePoints, Period latePeriod)
    {
        //Perform validation
        if(onTimeDate == null)
        {
            throw new NullPointerException("onTimeDate may not be null");
        }
        if(latePoints != null && latePeriod == null)
        {   
            throw new NullPointerException("latePeriod may not be null when latePoints is specified");
        }
        if(latePeriod != null && latePoints == null)
        {
            throw new NullPointerException("latePoints may not be null when latePeriod is specified");
        }
        if(lateDate != null && latePoints == null)
        {
            throw new NullPointerException("latePoints may not be null when lateDate is specified");
        }
        if(lateDate != null && latePeriod == null)
        {
            throw new NullPointerException("latePeriod may not be null when lateDate is specified");
        }
        
        return new DeadlineInfo(Type.VARIABLE, null, null, onTimeDate, lateDate, latePoints, latePeriod);
    }
    
    /**
     * Constructs a new {@code DeadlineInfo} with the option for an early date and late date with associated point
     * values.
     * <br/><br/>
     * If {@code earlyDate} is specified, occurrence dates before or on are given a {@link TimeStatus} of {@code EARLY}
     * and a penalty or bonus of {@code earlyPoints}. If {@code earlyDate} is specified, occurrence dates after
     * {@code earlyDate} but before or on {@code onTimeDate} are given a {@link TimeStatus} of {@code ON_TIME} and a
     * penalty or bonus of {@code 0}.
     * <br/><br/>
     * If {@code earlyDate} is not specified, occurrence dates before on or {@code onTimeDate} are given a
     * {@link TimeStatus} of {@code ON_TIME} and a penalty or bonus of {@code 0}. If {@code lateDate} is not specified,
     * occurrence dates after {@code onTimeDate} are given a {@link TimeStatus} of {@code NC_LATE} and a penalty or
     * bonus of the negation of the gradable event total earned.
     * <br/><br/>
     * If {@code lateDate} is specified, occurrence dates after {@code onTimeDate} but before on {@code lateDate} are
     * given a {@link TimeStatus} of {@code LATE} and a penalty or bonus of {@code latePoints}. If {@code lateDate} is
     * specified, occurrence dates after {@code lateDate} are given a {@link TimeStatus} of {@code NC_LATE} and a
     * penalty or bonus of the negation of the gradable event total earned.
     * 
     * @param earlyDate may be null
     * @param earlyPoints may not be {@code null} if {@code earlyDate} is not {@code null}
     * @param onTimeDate may not be {@code null}
     * @param lateDate may be null
     * @param latePoints may not be {@code null} if {@code lateDate} is not {@code null}
     * @return 
     */
    static DeadlineInfo newFixedDeadlineInfo(DateTime earlyDate, Double earlyPoints,
            DateTime onTimeDate,
            DateTime lateDate, Double latePoints)
    {
        //Perform validation
        if(onTimeDate == null)
        {
            throw new NullPointerException("onTimeDate may not be null");
        }
        if(earlyDate != null && earlyPoints == null)
        {
            throw new NullPointerException("earlyPoints may not be null when earlyDate is specified");
        }
        if(lateDate != null && latePoints == null)
        {
            throw new NullPointerException("latePoints may not be null when lateDate is specified");
        }
        
        return new DeadlineInfo(Type.FIXED, earlyDate, earlyPoints, onTimeDate, lateDate, latePoints, null);
    }
    
    public Type getType()
    {
        return _type;
    }
    
    public DateTime getEarlyDate()
    {
        return _earlyDate;
    }
    
    public DateTime getOnTimeDate()
    {
        return _onTimeDate;
    }
    
    public DateTime getLateDate()
    {
        return _lateDate;
    }
    
    public Double getEarlyPoints()
    {
        return _earlyPoints;
    }
    
    public Double getLatePoints()
    {
        return _latePoints;
    }
    
    public Period getLatePeriod()
    {
        return _latePeriod;
    }
    
    /**
     * Determines the effect of this deadline info for {@code occurrenceDate}. An {@code extension} may be provided. A
     * convenience method for {@link #apply(org.joda.time.DateTime, org.joda.time.DateTime, java.lang.Boolean)}.
     * 
     * @param occurrenceDate may be {@code null}, if {@code null} then a {@link DeadlineResolution} with a time status
     * of {@link TimeStatus#UNKNOWN} with a penalty/bonus of 0
     * @param extension may be {@code null}
     * @return 
     */
    public DeadlineResolution apply(DateTime occurrenceDate, Extension extension)
    {
        return apply(occurrenceDate,
                     extension == null ? null : extension.getNewOnTime(),
                     extension == null ? null : extension.getShiftDates());
    }
    
    /**
     * Determines the effect of this deadline info for {@code occurrenceDate}. An extension may be provided, and this
     * extension may shift any early or late dates that belong to this deadline.
     * 
     * @param occurrenceDate may be {@code null}, if {@code null} then a {@link DeadlineResolution} with a time status
     * of {@link TimeStatus#UNKNOWN} and a penalty/bonus of 0
     * @param onTimeExtension may be {@code null}
     * @param shiftDates will be ignored if {@code onTimeExtension} is {@code null}
     * @return 
     */
    public DeadlineResolution apply(DateTime occurrenceDate, DateTime onTimeExtension, Boolean shiftDates)
    {
        DeadlineResolution effect;
        if(occurrenceDate == null)
        {
            effect = new DeadlineResolution(TimeStatus.UNKNOWN, 0);
        }
        else if(_type == Type.NONE)
        {
            effect = new DeadlineResolution(TimeStatus.ON_TIME, 0);
        }
        else
        {
            //Determe the early, ontime, and late dates to work with
            DateTime earlyDate, ontimeDate, lateDate;
            if(onTimeExtension != null)
            {
                if(shiftDates)
                {
                    Duration shiftBy = new Duration(_onTimeDate, onTimeExtension);

                    earlyDate = (_earlyDate == null) ? null : _earlyDate.plus(shiftBy);
                    ontimeDate = onTimeExtension;
                    lateDate = (_lateDate == null) ? null : _lateDate.plus(shiftBy);
                }
                else
                {
                    earlyDate = null;
                    ontimeDate = onTimeExtension;
                    lateDate = null;
                }
            }
            else
            {
                earlyDate = _earlyDate;
                ontimeDate = _onTimeDate;
                lateDate = _lateDate;
            }

            if(_type == Type.FIXED)
            {
                effect = applyFixed(occurrenceDate, earlyDate, ontimeDate, lateDate);
            }
            else if(_type == Type.VARIABLE)
            {
                effect = applyVariable(occurrenceDate, ontimeDate, lateDate);
            }
            else
            {
                throw new IllegalStateException("Unknown " + Type.class.getName() + ": " + _type);
            }
        }
        
        return effect;
    }
    
    private DeadlineResolution applyFixed(DateTime occurrenceDate, DateTime earlyDate, DateTime onTimeDate, DateTime lateDate)
    {
        DeadlineResolution effect;
        if(earlyDate != null && (occurrenceDate.isBefore(earlyDate) || occurrenceDate.isEqual(earlyDate)))
        {
            effect = new DeadlineResolution(TimeStatus.EARLY, _earlyPoints);
        }
        else if(occurrenceDate.isBefore(onTimeDate) || occurrenceDate.isEqual(onTimeDate))
        {
            effect = new DeadlineResolution(TimeStatus.ON_TIME, 0);
        }
        else if(lateDate == null && occurrenceDate.isAfter(onTimeDate))
        {
            effect = new DeadlineResolution(TimeStatus.NC_LATE, Double.NaN);
        }
        else if(lateDate != null && (occurrenceDate.isBefore(lateDate) || occurrenceDate.isEqual(lateDate)))
        {
            effect = new DeadlineResolution(TimeStatus.LATE, _latePoints);
        }
        else if(lateDate != null && occurrenceDate.isAfter(lateDate))
        {
            effect = new DeadlineResolution(TimeStatus.NC_LATE, Double.NaN);
        }
        else
        {
            throw new IllegalStateException("Unexpected situation for FIXED Deadlines\n" +
                    "Original Early: " + _earlyDate + "\n" +
                    "Original On Time: " + _onTimeDate + "\n" +
                    "Original Late: " + _lateDate + "\n" + 
                    "Occurrence Date: " + occurrenceDate + "\n" +
                    "Adjusted Early: " + earlyDate + "\n" +
                    "Adjusted On Time: " + onTimeDate + "\n" + 
                    "Adjusted Late: " + lateDate);
        }
        
        return effect;
    }
    
    private DeadlineResolution applyVariable(DateTime occurrenceDate, DateTime onTimeDate, DateTime lateDate)
    {
        DeadlineResolution effect;
        if(occurrenceDate.isBefore(onTimeDate) || occurrenceDate.equals(onTimeDate))
        {
            effect = new DeadlineResolution(TimeStatus.ON_TIME, 0);
        }
        //If no late penalty exists, then after the on time date occurence dates are NC Late
        else if(_latePeriod == null && occurrenceDate.isAfter(onTimeDate))
        {
            effect = new DeadlineResolution(TimeStatus.NC_LATE, Double.NaN);
        }
        //If there is a date after which ocurrence dates become NC Late
        else if(lateDate != null && occurrenceDate.isAfter(lateDate))
        {
            effect = new DeadlineResolution(TimeStatus.NC_LATE, Double.NaN);
        }
        else
        {   
            long amountLate = new Duration(onTimeDate, occurrenceDate).getMillis();
            long latePeriod = _latePeriod.toStandardDuration().getMillis();
            //Units late = ceiling(amount late / late period)
            //For example:
            // - if the amount late is 1 second and the late period is 5 minutes,
            //   then there is 1 late unit
            // - if the amount late is 5 minutes and the late period is 5 minutes,
            //   then there is 1 late unit
            // - if the amount later is 5 minutes 1 second and the late period is 5 minutes,
            //   then there are 2 late units
            long unitsLate = amountLate / latePeriod + (amountLate % latePeriod == 0 ? 0 : 1);
            double pointsEffect = unitsLate * _latePoints;

            effect = new DeadlineResolution(TimeStatus.LATE, pointsEffect);
        }
        
        return effect;
    }
    
    public static enum TimeStatus
    {
        EARLY("Early"), ON_TIME("On Time"), LATE("Late"), NC_LATE("NC Late"), UNKNOWN("Unknown");

        private final String _prettyPrintName;

        private TimeStatus(String prettyPrintName)
        {
            _prettyPrintName = prettyPrintName;
        }

        @Override
        public String toString()
        {   
            return _prettyPrintName;
        }
    }
    
    /**
     * The effect of a DeadlineInfo instance for a given occurrence date, potentially adjusted by an extension.
     */
    public static class DeadlineResolution
    {  
        private final TimeStatus _status;
        private final double _penaltyOrBonus;
        
        private DeadlineResolution(TimeStatus status, double penaltyOrBonus)
        {
            _status = status;
            _penaltyOrBonus = penaltyOrBonus;
        }
        
        public TimeStatus getTimeStatus()
        {
            return _status;
        }
        
        /**
         * Returns the penalty or bonus for the gradable event. When {@link #getTimeStatus()} returns {@code NC_LATE}
         * this method will return {@code -unadjustedGradableEventTotalEarned}.
         * 
         * @param unadjustedGradableEventTotalEarned the total amount of points earned by a group for the gradable event
         * the deadlines this effect was calculated relative to
         * @return the penalty or bonus the group received
         */
        public double getPenaltyOrBonus(Double unadjustedGradableEventTotalEarned)
        {
            double penaltyOrBonus = _penaltyOrBonus;
            if(_status == TimeStatus.NC_LATE)
            {
                if(unadjustedGradableEventTotalEarned == null)
                {
                    penaltyOrBonus = 0;
                }
                else
                {
                    penaltyOrBonus = -unadjustedGradableEventTotalEarned;
                }
            }
            
            return penaltyOrBonus;
        }
        
        /**
         * A string representation of this object for debugging purposes; not intended for display to users.
         * 
         * @return 
         */
        @Override
        public String toString()
        {
            return "[TimeStatus=" + _status + ", PenaltyOrBonus=" + _penaltyOrBonus + "]";
        }
    }
}