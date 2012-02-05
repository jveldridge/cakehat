package support.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import support.utils.posix.NativeException;

/**
 *
 * @author aunger
 */
public class UserUtilitiesTest {

    private UserUtilities _instance;

    public UserUtilitiesTest() {
        _instance = new UserUtilitiesImpl();
    }

    @Test
    public void testUserName() throws NativeException {
        // will fail if run on non department machine
        assertEquals("System Management", _instance.getUserName("system"));
    }

    @Test(expected=NativeException.class)
    public void testUserName_NonExistant() throws NativeException {
        _instance.getUserName("hellooooo"); // can't be more than 8 chars
    }

    @Test
    public void testIsLoginValid() {
        // will fail if run on non department machine
        assertTrue(_instance.isLoginValid("system"));
    }

    @Test
    public void testIsLoginValid_NonExistant() {
        assertFalse(_instance.isLoginValid("hellooooo")); // can't be more than 8 chars
    }
}
