package cakehat.services;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the course id parsed by {@link CourseInfoImple}.
 *
 * @author aunger
 */
public class CourseInfoImplTest {

    /**
     * Verifies that the regex in getCourseFromPath is correct and only matches
     *  course titles that exist in the department.
     */
    @Test
    public void testGetCourseFromPath() {
        CourseInfoImpl cii = new CourseInfoImpl();

        String path = "/course/cs123/.cakehat/";
        String expected = "cs123";
        assertEquals(expected, cii.getCourseFromPath(path));

        path = "/course/cs195h/.cakehat/";
        expected = "cs195h";
        assertEquals(expected, cii.getCourseFromPath(path));

        path = "/gpfs/main/course/cs195h/.cakehat/";
        expected = "cs195h";
        assertEquals(expected, cii.getCourseFromPath(path));

        path = "/gpfs/main/course/cs195hh/.cakehat/";
        expected = null;
        assertEquals(expected, cii.getCourseFromPath(path));

        path = "/gpfs/main/course/cs19/.cakehat/";
        expected = null;
        assertEquals(expected, cii.getCourseFromPath(path));

        path = "/gpfs/main/course/cd195h/.cakehat/";
        expected = null;
        assertEquals(expected, cii.getCourseFromPath(path));

        path = "/gpfs/main/course/cs1945/.cakehat/";
        expected = null;
        assertEquals(expected, cii.getCourseFromPath(path));
    }
}