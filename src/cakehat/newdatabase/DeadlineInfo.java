package cakehat.newdatabase;

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
    private final DateTime _earlyDate, _ontimeDate, _lateDate;
    private final Double _earlyPoints, _latePoints;
    private final Period _latePeriod;
    
    private DeadlineInfo(Type type,
            DateTime earlyDate, Double earlyPoints,
            DateTime ontimeDate,
            DateTime lateDate, Double latePoints, Period latePeriod)
    {
        _type = type;
        
        _earlyDate = earlyDate;
        _earlyPoints = earlyPoints;
        
        _ontimeDate = ontimeDate;
        
        _lateDate = lateDate;
        _latePoints = latePoints;
        _latePeriod = latePeriod;
    }
    
    /**
     * Constructs a new {@code DeadlineInfo} that when
     * {@link #apply(org.joda.time.DateTime, org.joda.time.DateTime, java.lang.Boolean)} is called returns a
     * {@link DeadlineResolution} with a {@link TimeStatus} of {@code ON_TIME} and a penalty or bonus of {@code 0}.
     * 
     * @return
     */
    static DeadlineInfo newNoDeadlineInfo()
    {
        return new DeadlineInfo(Type.NONE, null, null, null, null, null, null);
    }
    
    /**
     * Constructs a new {@code DeadlineInfo} with a late penalty that varies based on the amount of time the handin
     * time is beyond the ontime date.
     * <br/><br/>
     * Handins turned in before {@code ontimeDate} are given a {@link TimeStatus} of {@code ON_TIME} and a penalty or
     * bonus of {@code 0}.
     * <br/><br/>
     * If {@code lateDate} is not specified, handins turned in after {@code ontimeDate} are given a {@link TimeStatus}
     * of {@code LATE}. If {@code lateDate} is specified, handins turned in after {@code ontimeDate}, but before or on
     * {@code lateDate} are given a {@link TimeStatus} of {@code LATE}. If {@code lateDate} is specified, handins turned
     * in after {@code lateDate} are given a {@link TimeStatus} of {@code NC_LATE} and a penalty or bonus of the
     * negation of the gradable event total earned. If a handin is given a {@link TimeStatus} of {@code LATE} then its
     * penalty or bonus is adds {@code latePoints} for each increment {@code latePeriod} that has passed since
     * {@code ontimeDate} such that an amount less than {@code Period} is 1 increment.
     * 
     * @param ontimeDate may not be {@code null}
     * @param lateDate may be {@code null}
     * @param latePoints may not be {@code null} if {@code latePeriod} is not {@code null}
     * @param latePeriod may not be {@code null} if {@code latePoints} is not {@code null}
     * @return 
     */
    static DeadlineInfo newVariableDeadlineInfo(DateTime ontimeDate,
            DateTime lateDate, Double latePoints, Period latePeriod)
    {
        //Perform validation
        if(ontimeDate == null)
        {
            throw new NullPointerException("ontimeDate may not be null");
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
        
        return new DeadlineInfo(Type.VARIABLE, null, null, ontimeDate, lateDate, latePoints, latePeriod);
    }
    
    /**
     * Constructs a new {@code DeadlineInfo} with the option for an early date and late date with associated point
     * values.
     * <br/><br/>
     * If {@code earlyDate} is specified, handins turned in before or on are given a {@link TimeStatus} of {@code EARLY}
     * and a penalty or bonus of {@code earlyPoints}. If {@code earlyDate} is specified, handins turned in after
     * {@code earlyDate} but before or on {@code ontimeDate} are given a {@link TimeStatus} of {@code ON_TIME} and a
     * penalty or bonus of {@code 0}.
     * <br/><br/>
     * If {@code earlyDate} is not specified, handins turned in before on or {@code ontimeDate} are given a
     * {@link TimeStatus} of {@code ON_TIME} and a penalty or bonus of {@code 0}. If {@code lateDate} is not specified,
     * handins turned in after {@code ontimeDate} are given a {@link TimeStatus} of {@code NC_LATE} and a penalty or
     * bonus of the negation of the gradable event total earned.
     * <br/><br/>
     * If {@code lateDate} is specified, handins turned in after {@code ontimeDate} but before on {@code lateDate} are
     * given a {@link TimeStatus} of {@code LATE} and a penalty or bonus of {@code latePoints}. If {@code lateDate} is
     * specified, handins turned in after {@code lateDate} are given a {@link TimeStatus} of {@code NC_LATE} and a
     * penalty or bonus of the negation of the gradable event total earned.
     * 
     * @param earlyDate may be null
     * @param earlyPoints may not be {@code null} if {@code earlyDate} is not {@code null}
     * @param ontimeDate may not be {@code null}
     * @param lateDate may be null
     * @param latePoints may not be {@code null} if {@code lateDate} is not {@code null}
     * @return 
     */
    static DeadlineInfo newFixedDeadlineInfo(DateTime earlyDate, Double earlyPoints,
            DateTime ontimeDate,
            DateTime lateDate, Double latePoints)
    {
        //Perform validation
        if(ontimeDate == null)
        {
            throw new NullPointerException("ontimeDate may not be null");
        }
        if(earlyDate != null && earlyPoints == null)
        {
            throw new NullPointerException("earlyPoints may not be null when earlyDate is specified");
        }
        if(lateDate != null && latePoints == null)
        {
            throw new NullPointerException("latePoints may not be null when lateDate is specified");
        }
        
        return new DeadlineInfo(Type.FIXED, earlyDate, earlyPoints, ontimeDate, lateDate, latePoints, null);
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
        return _ontimeDate;
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
     * Determines the effect of this deadline info to the {@code handinTime}. An extension may be provided, and this
     * extension may shift any early or late dates that belong to this deadline.
     * 
     * @param handinTime may be {@code null}, if {@code null} then a {@link DeadlineResolution} with a time status of
     * {@link TimeStatus#NOT_RECEIVED} with a penalty/bonus of 0
     * @param ontimeExtension may be {@code null}
     * @param shiftDates will be ignored if {@code ontimeExtension} is {@code null}
     * @return 
     */
    public DeadlineResolution apply(DateTime handinTime, DateTime ontimeExtension, Boolean shiftDates)
    {
        DeadlineResolution effect;
        if(handinTime == null)
        {
            effect = new DeadlineResolution(TimeStatus.NOT_RECEIVED, 0);
        }
        else if(_type == Type.NONE)
        {
            effect = new DeadlineResolution(TimeStatus.ON_TIME, 0);
        }
        else
        {
            //Determe the early, ontime, and late dates to work with
            DateTime earlyDate, ontimeDate, lateDate;
            if(ontimeExtension != null)
            {
                if(shiftDates)
                {
                    Duration shiftBy = new Duration(_ontimeDate, ontimeExtension);

                    earlyDate = (_earlyDate == null) ? null : _earlyDate.plus(shiftBy);
                    ontimeDate = ontimeExtension;
                    lateDate = (_lateDate == null) ? null : _lateDate.plus(shiftBy);
                }
                else
                {
                    earlyDate = null;
                    ontimeDate = ontimeExtension;
                    lateDate = null;
                }
            }
            else
            {
                earlyDate = _earlyDate;
                ontimeDate = _ontimeDate;
                lateDate = _lateDate;
            }

            if(_type == Type.FIXED)
            {
                effect = applyFixed(handinTime, earlyDate, ontimeDate, lateDate);
            }
            else if(_type == Type.VARIABLE)
            {
                effect = applyVariable(handinTime, ontimeDate, lateDate);
            }
            else
            {
                throw new IllegalStateException("Unknown " + Type.class.getName() + ": " + _type);
            }
        }
        
        return effect;
    }
    
    private DeadlineResolution applyFixed(DateTime handinTime, DateTime earlyDate, DateTime ontimeDate, DateTime lateDate)
    {
        DeadlineResolution effect;
        if(earlyDate != null && (handinTime.isBefore(earlyDate) || handinTime.isEqual(earlyDate)))
        {
            effect = new DeadlineResolution(TimeStatus.EARLY, _earlyPoints);
        }
        else if(handinTime.isBefore(ontimeDate) || handinTime.isEqual(ontimeDate))
        {
            effect = new DeadlineResolution(TimeStatus.ON_TIME, 0);
        }
        else if(lateDate == null && handinTime.isAfter(ontimeDate))
        {
            effect = new DeadlineResolution(TimeStatus.NC_LATE, Double.NaN);
        }
        else if(lateDate != null && (handinTime.isBefore(lateDate) || handinTime.isEqual(lateDate)))
        {
            effect = new DeadlineResolution(TimeStatus.LATE, _latePoints);
        }
        else if(lateDate != null && handinTime.isAfter(lateDate))
        {
            effect = new DeadlineResolution(TimeStatus.NC_LATE, Double.NaN);
        }
        else
        {
            throw new IllegalStateException("Unexpected situation for FIXED Deadlines\n" +
                    "Original Early: " + _earlyDate + "\n" +
                    "Original On Time: " + _ontimeDate + "\n" +
                    "Original Late: " + _lateDate + "\n" + 
                    "Handin: " + handinTime + "\n" +
                    "Adjusted Early: " + earlyDate + "\n" +
                    "Adjusted On Time: " + ontimeDate + "\n" + 
                    "Adjusted Late: " + lateDate);
        }
        
        return effect;
    }
    
    private DeadlineResolution applyVariable(DateTime handinTime, DateTime ontimeDate, DateTime lateDate)
    {
        DeadlineResolution effect;
        if(handinTime.isBefore(ontimeDate) || handinTime.equals(ontimeDate))
        {
            effect = new DeadlineResolution(TimeStatus.ON_TIME, 0);
        }
        //If no late penalty exists, then after the on time date handins are NC Late
        else if(_latePeriod == null && handinTime.isAfter(ontimeDate))
        {
            effect = new DeadlineResolution(TimeStatus.NC_LATE, Double.NaN);
        }
        //If there is a date after which handins become NC Late
        else if(lateDate != null && handinTime.isAfter(lateDate))
        {
            effect = new DeadlineResolution(TimeStatus.NC_LATE, Double.NaN);
        }
        else
        {   
            long amountLate = new Duration(ontimeDate, handinTime).getMillis();
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
        EARLY("Early"), ON_TIME("On Time"), LATE("Late"), NC_LATE("NC Late"), NOT_RECEIVED("Not Received");

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
     * The effect of a DeadlineInfo instance for a given digital handin time, potentially adjusted by an extension.
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
        public double getPenaltyOrBonus(double unadjustedGradableEventTotalEarned)
        {
            double penaltyOrBonus = _penaltyOrBonus;
            if(_status == TimeStatus.NC_LATE)
            {
                penaltyOrBonus = -unadjustedGradableEventTotalEarned;
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