/**This class contains methods that test the methods in the GeneralUtilities class.
 *
 * @author hdrosen
 */

package gradesystem.utils;

import gradesystem.utils.GeneralUtilities;
import org.junit.Test;
import static org.junit.Assert.*;

public class GeneralUtilitiesTest {

    private GeneralUtilities _instance;

    public GeneralUtilitiesTest() {
        _instance = new GeneralUtilities();
    }

    
    @Test
    public void testDoubleToString() {
        assertEquals("3.35", _instance.doubleToString(3.34999));
        assertEquals("1.25", _instance.doubleToString(1.25));
        assertEquals("1.0", _instance.doubleToString(1));
        assertEquals("-1.25", _instance.doubleToString(-1.25));
    }

    @Test
    public void testRound() {
        assertEquals(3.3, _instance.round(3.25, 1), 0);
        assertEquals(3.3, _instance.round(3.32, 1), 0);
        assertEquals(3.32, _instance.round(3.32, 2), 0);
        assertEquals(19, _instance.round(18.95432, 0), 0);
    }

}