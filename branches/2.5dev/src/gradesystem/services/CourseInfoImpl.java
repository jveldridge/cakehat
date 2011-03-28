package gradesystem.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jak2
 */
public class CourseInfoImpl implements CourseInfo
{
    //For testing purposes, specifies which course this is being run for
    private final static String TESTING_COURSE = "cs000";

    private String _course = null;
    public String getCourse() {
        if (_course == null) {
            //Get the location of where this code is running
            String loc = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

            String course = this.getCourseFromPath(loc);
            if (course != null) {
                _course = course;
            }
            else {
                System.out.println("Using hard-coded test value for course: " + TESTING_COURSE);
                _course = TESTING_COURSE;
            }
        }

        return _course;
    }

    public String getStudentGroup()
    {
        return getCourse() + "student";
    }

    public String getTAGroup()
    {
        return getCourse() + "ta";
    }

    public String getHTAGroup()
    {
        return getCourse() + "hta";
    }

    protected String getCourseFromPath(String path) {
        /* matches: any characters followed by "/course/cs" and grabs "cs" and
         * the 3 digits that follow with or without a trailing letter */
        Pattern regex = Pattern.compile(".*?/course/(cs[0-9]{3}[a-z]?)/",
                Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher regexMatcher = regex.matcher(path);
        //If this is actually the jar we are running from
        if (regexMatcher.find()) {
            return regexMatcher.group(1);
        } else {
            return null;
        }
    }
}