package utils;

import org.junit.Test;
import static org.junit.Assert.*;
import utils.system.NativeException;

/**
 *
 * @author hdrosen
 */
public class UserUtilitiesTest {

    private UserUtilities _instance;

    public UserUtilitiesTest() {
        _instance = new UserUtilitiesImpl();
    }

//This tests the getUserName method in the UserUtilities class.
    @Test
    public void testUserName() {
        boolean error = false;

        try {
            assertEquals("Ashley Rose Tuccero", _instance.getUserName("ashley"));
            assertEquals("UNKNOWN_LOGIN", _instance.getUserName("hello"));
        } catch (NativeException ex) {
            error = true;
        }

        assertFalse(error);
    }

}