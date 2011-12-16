package cakehat.assignment;

import org.joda.time.DateTime;
import cakehat.assignment.DeadlineInfo.TimeStatus;
import cakehat.assignment.DeadlineInfo.DeadlineResolution;
import org.joda.time.Period;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link DeadlineInfo}'s
 * {@link DeadlineInfo#apply(org.joda.time.DateTime, org.joda.time.DateTime, java.lang.Boolean)} method.
 *
 * @author jak2
 */
public class DeadlineInfoTest
{
    private final DateTime EARLY_DATE = new DateTime(2011, 12, 5, 23, 59, 59); //December 5, 2011 at 23:59:59
    private final DateTime ON_TIME_DATE = new DateTime(2011, 12, 7, 23, 59, 59); //December 7, 2011 at 23:59:59
    private final DateTime LATE_DATE = new DateTime(2011, 12, 9, 23, 59, 59); //December 9, 2011 at 23:59:59
    
    private final DateTime EXTENSION_DATE = new DateTime(2011, 12, 13, 23, 59, 59); //December 13, 2011 at 23:59:59
    
    //Not a direct input to any DeadlineInfo method - to aid testing
    private final Period EXTENSION_SHIFT = new Period(ON_TIME_DATE, EXTENSION_DATE); 
    
    private final Period LATE_PERIOD = new Period(0, 0, 0, 1, 0, 0, 0, 0); //1 day
    
    private final double EARLY_BONUS = 5D;
    private final double LATE_PENALTY = -10D;
    private final double EARNED = 89D;
    
    /******************************************************************************************************************\
    |*                                                 No Deadlines                                                   *|
    \******************************************************************************************************************/
    
    @Test
    public void testNoDeadline_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newNoDeadlineInfo();
        
        DeadlineResolution resolution = info.apply(null, null, null);
        assertEquals(resolution.getTimeStatus(), TimeStatus.ON_TIME);
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testNoDeadline_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newNoDeadlineInfo();
        
        DeadlineResolution resolution = info.apply(null, EXTENSION_DATE, false);
        assertEquals(resolution.getTimeStatus(), TimeStatus.ON_TIME);
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testNoDeadline_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newNoDeadlineInfo();
        
        DeadlineResolution resolution = info.apply(null, EXTENSION_DATE, true);
        assertEquals(resolution.getTimeStatus(), TimeStatus.ON_TIME);
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    /******************************************************************************************************************\
    |*                                      Fixed Deadlines - No Extension                                            *|
    \******************************************************************************************************************/
    
    //Ontime specified
    
    @Test
    public void testFixedDeadline_OntimeSpecified_BeforeOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.minusHours(1), null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeSpecified_OnOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE, null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeSpecified_AfterOntime_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Early, Ontime specified
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_BeforeEarlyHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.minusHours(1), null, null);
        assertEquals(TimeStatus.EARLY, resolution.getTimeStatus());
        assertEquals(EARLY_BONUS, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_OnEarlyHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE, null, null);
        assertEquals(TimeStatus.EARLY, resolution.getTimeStatus());
        assertEquals(EARLY_BONUS, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_AfterEarlyBeforeOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_OnOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE, null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_AfterOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Ontime, Late specified
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_BeforeOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.minusHours(1), null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_OnOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE, null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_AfterOntimeBeforeLateHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_OnLateHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(LATE_DATE, null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_AfterLateHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Early, Ontime, & Late specified
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_BeforeEarlyHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.minusHours(1), null, null);
        assertEquals(TimeStatus.EARLY, resolution.getTimeStatus());
        assertEquals(EARLY_BONUS, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_OnEarlyHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE, null, null);
        assertEquals(TimeStatus.EARLY, resolution.getTimeStatus());
        assertEquals(EARLY_BONUS, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_AfterEarlyBeforeOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_OnOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE, null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_AfterOntimeBeforeLateHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_OnLateHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(LATE_DATE, null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_AfterLateHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    /******************************************************************************************************************\
    |*                                    Fixed Deadlines - Extension, No Shift                                       *|
    \******************************************************************************************************************/
    
    //Ontime specified
    
    @Test
    public void testFixedDeadline_OntimeSpecified_BeforeExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.minusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeSpecified_OnExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE, EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED),0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeSpecified_AfterExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    
    //Early & Ontime specified
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_BeforeExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.minusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_OnExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE, EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED),0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_AfterExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Early, Ontime, & Late specified
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_BeforeExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.minusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_OnExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE, EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED),0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_AfterExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Early, Ontime, & Late specified
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_BeforeExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.minusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_OnExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE, EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED),0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_AfterExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-20D, resolution.getPenaltyOrBonus(20D), 0D);
    }
        
    /******************************************************************************************************************\
    |*                                      Fixed Deadlines - Extension, Shift                                        *|
    \******************************************************************************************************************/
    
    //Ontime specified
    
    @Test
    public void testFixedDeadline_OntimeSpecified_BeforeOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).minusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeSpecified_OnOntimeHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeSpecified_AfterOntime_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Early, Ontime specified
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_BeforeEarlyExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.plus(EXTENSION_SHIFT).minusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.EARLY, resolution.getTimeStatus());
        assertEquals(EARLY_BONUS, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_OnEarlyExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.EARLY, resolution.getTimeStatus());
        assertEquals(EARLY_BONUS, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_AfterEarlyExtensionBeforeOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_OnOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeSpecified_AfterOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Ontime, Late specified
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_BeforeOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).minusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_OnOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_AfterOntimeExtensionBeforeLateExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_OnLateExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_OntimeLateSpecified_AfterLateExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(null, null, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Early, Ontime, & Late specified
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_BeforeEarlyExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.plus(EXTENSION_SHIFT).minusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.EARLY, resolution.getTimeStatus());
        assertEquals(EARLY_BONUS, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_OnEarlyExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.EARLY, resolution.getTimeStatus());
        assertEquals(EARLY_BONUS, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_AfterEarlyExtensionBeforeOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(EARLY_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_OnOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_AfterOntimeExtensionBeforeLateExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_OnLateExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testFixedDeadline_EarlyOntimeLateSpecified_AfterLateHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newFixedDeadlineInfo(EARLY_DATE, EARLY_BONUS, ON_TIME_DATE, LATE_DATE, LATE_PENALTY);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    /******************************************************************************************************************\
    |*                                   Variable Deadlines - No Extension                                            *|
    \******************************************************************************************************************/
    
    //Ontime specified
    
    @Test
    public void testVariableDeadline_OntimeSpecified_BeforeOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.minusHours(1), null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeSpecified_OnOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE, null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeSpecified_AfterOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Ontime specified, Late specified - late date not specified
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_BeforeOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.minusHours(1), null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_OnOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE, null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(resolution.getPenaltyOrBonus(EARNED), 0D, 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterOntimeLessThan1PeriodHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterOntimeExactly1PeriodHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(LATE_PERIOD), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterOntimeBetween1And2PeriodsHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(LATE_PERIOD).plusHours(1), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterOntimeExactly2PeriodsHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(LATE_PERIOD).plus(LATE_PERIOD), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Ontime specified, Late specified - late date specified
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_BeforeOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.minusHours(1), null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_OnOntimeHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE, null, null);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(resolution.getPenaltyOrBonus(EARNED), 0D, 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterOntimeLessThan1PeriodHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterOntimeExactly1PeriodHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(LATE_PERIOD), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterOntimeBetween1And2PeriodsHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(LATE_PERIOD).plusHours(1), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterOntimeExactly2PeriodsHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(LATE_PERIOD).plus(LATE_PERIOD), null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_OnLateHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(LATE_DATE, null, null);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterLateHandin_NoExtension()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plusHours(1), null, null);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    /******************************************************************************************************************\
    |*                                Variable Deadlines - Extension, No Shift                                        *|
    \******************************************************************************************************************/
        
    //Ontime specified
    
    @Test
    public void testVariableDeadline_OntimeSpecified_BeforeExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.minusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeSpecified_OnExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE, EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeSpecified_AfterExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    
    //Ontime specified, Late specified - late date not specified
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_BeforeExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.minusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_OnExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE, EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(resolution.getPenaltyOrBonus(EARNED), 0D, 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterExtensionLessThan1PeriodHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterExtensionExactly1PeriodHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plus(LATE_PERIOD), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterExtensionBetween1And2PeriodsHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plus(LATE_PERIOD).plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterExtensionExactly2PeriodsHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plus(LATE_PERIOD).plus(LATE_PERIOD), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    
    //Ontime specified, Late specified - late date specified
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_BeforeExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.minusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_OnExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE, EXTENSION_DATE, false);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(resolution.getPenaltyOrBonus(EARNED), 0D, 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterExtensionLessThan1PeriodHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterExtensionExactly1PeriodHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plus(LATE_PERIOD), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterExtensionBetween1And2PeriodsHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plus(LATE_PERIOD).plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterExtensionExactly2PeriodsHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(EXTENSION_DATE.plus(LATE_PERIOD).plus(LATE_PERIOD), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Note: the correct behavior is the late date is NOT shifted or preserved, so there is no late date/extension, but
    //      if it were to be shifted then it would be to the date used in this test
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_OnLateExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
    }
    
    //Note: the correct behavior is the late date is NOT shifted or preserved, so there is no late date/extension, but
    //      if it were to be shifted then it would be to the date used in this test
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterLateExtensionHandin_Extension_NoShift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, false);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
    }
    
    /******************************************************************************************************************\
    |*                                  Variable Deadlines - Extension, Shift                                         *|
    \******************************************************************************************************************/
        
    //Ontime specified
    
    @Test
    public void testVariableDeadline_OntimeSpecified_BeforeOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).minusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeSpecified_OnOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeSpecified_AfterOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, null, null);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Ontime specified, Late specified - late date not specified
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_BeforeOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).minusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_OnExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(resolution.getPenaltyOrBonus(EARNED), 0D, 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterOntimeExtensionLessThan1PeriodHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterOntimeExtensionExactly1PeriodHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plus(LATE_PERIOD), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterOntimeExtensionBetween1And2PeriodsHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plus(LATE_PERIOD).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodSpecified_AfterOntimeExtensionExactly2PeriodsHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, null, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plus(LATE_PERIOD).plus(LATE_PERIOD), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    //Ontime specified, Late specified - late date specified
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_BeforeOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).minusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(0D, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_OnOntimeExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.ON_TIME, resolution.getTimeStatus());
        assertEquals(resolution.getPenaltyOrBonus(EARNED), 0D, 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterOntimeExtensionLessThan1PeriodHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterOntimeExtensionExactly1PeriodHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plus(LATE_PERIOD), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterOntimeExtensionBetween1And2PeriodsHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plus(LATE_PERIOD).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterOntimeExtensionExactly2PeriodsHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(ON_TIME_DATE.plus(EXTENSION_SHIFT).plus(LATE_PERIOD).plus(LATE_PERIOD), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
        assertEquals(2*LATE_PENALTY, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_OnLateExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plus(EXTENSION_SHIFT), EXTENSION_DATE, true);
        assertEquals(TimeStatus.LATE, resolution.getTimeStatus());
    }
    
    @Test
    public void testVariableDeadline_OntimeLatePeriodAndDateSpecified_AfterLateExtensionHandin_Extension_Shift()
    {
        DeadlineInfo info = DeadlineInfo.newVariableDeadlineInfo(ON_TIME_DATE, LATE_DATE, LATE_PENALTY, LATE_PERIOD);
        
        DeadlineResolution resolution = info.apply(LATE_DATE.plus(EXTENSION_SHIFT).plusHours(1), EXTENSION_DATE, true);
        assertEquals(TimeStatus.NC_LATE, resolution.getTimeStatus());
        assertEquals(-EARNED, resolution.getPenaltyOrBonus(EARNED), 0D);
    }
}