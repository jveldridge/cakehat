package support.utils;

import support.utils.CalendarUtilities;
import support.utils.CalendarUtilitiesImpl;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author hdrosen
 */
public class CalendarUtilitiesTest {

    private CalendarUtilities _instance;
    private GregorianCalendar _cal;

    public CalendarUtilitiesTest() {
        _instance = new CalendarUtilitiesImpl();
        _cal = new GregorianCalendar();
        //This sets the date and time of the calendar so that it is 3:45:32 on 10/31/2010.
        _cal.set(2010, 9, 31, 3, 45, 32);
    }

     @Test
    public void testGetCurrentYear() {
        assertEquals(Calendar.getInstance().get(Calendar.YEAR), _instance.getCurrentYear());
    }

    @Test
    public void testGetDateAsString() {
        assertEquals("", _instance.getDateAsString(null));
        assertEquals("2010-10-31", _instance.getDateAsString(_cal));
    }

    @Test
    public void testGetTimeAsString() {
        assertEquals("", _instance.getTimeAsString(null));
        assertEquals("03:45:32", _instance.getTimeAsString(_cal));
    }

    @Test
    public void testGetCalendarAsString() {
        assertEquals("", _instance.getCalendarAsString(null));
        assertEquals("2010-10-31 03:45:32", _instance.getCalendarAsString(_cal));
    }

    @Test
    public void testGetCalendarAsHandinTime() {
        assertEquals("", _instance.getCalendarAsHandinTime(null));
        assertEquals("3:45 10-31-2010", _instance.getCalendarAsHandinTime(_cal));
    }


}