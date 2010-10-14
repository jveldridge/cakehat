/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hdrosen
 */
public class UserUtilitiesTest {

    private UserUtilities _instance;

    public UserUtilitiesTest() {
        _instance = new UserUtilities();
    }

//This tests the getUserName method in the UserUtilities class.
    @Test
    public void testUserName() {
        assertEquals("Ashley Rose Tuccero", _instance.getUserName("ashley"));
        assertEquals("UNKNOWN_LOGIN", _instance.getUserName("hello"));

    }

}