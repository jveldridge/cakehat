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
public class GeneralUtilitiesTest {

    private GeneralUtilities _instance;

    public GeneralUtilitiesTest() {
        _instance = new GeneralUtilities();
    }

//This tests the getUserName method in the GeneralUtilities class.
    @Test
    public void testUserName() {
        assertEquals("Ashley Rose Tuccero", _instance.getUserName("ashley"));
        assertEquals("UNKNOWN_LOGIN", _instance.getUserName("hello"));

    }

}