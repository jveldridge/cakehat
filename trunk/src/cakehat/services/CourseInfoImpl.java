package cakehat.services;

import cakehat.CakehatSession;
import com.google.common.annotations.VisibleForTesting;
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

    private final String _course = determineCourse();
    private final String _studentGroup = _course + "student";
    private final String _taGroup = _course + "ta";
    private final String _htaGroup = _course + "hta";
    
    private static String determineCourse()
    {
        //Get the location of where this code is running
        String codeLocation = CourseInfoImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        String course = getCourseFromPath(codeLocation);
        if(course == null)
        {
            if(CakehatSession.isDeveloperMode() || !CakehatSession.didStartNormally())
            {
                System.out.println("Using hard-coded test value for course: " + TESTING_COURSE);
                course = TESTING_COURSE;
            }
            else
            {
                throw new IllegalStateException("cakehat is unable to determine which course it is running for\n" +
                        "cakehat location: " + codeLocation);
            }
        }
        
        return course;
    }
    
    @VisibleForTesting
    static String getCourseFromPath(String path)
    {
        //Matches any characters followed by "/course/cs" and grabs "cs" and the 3 digits that follow with or without a
        //trailing letter
        Pattern regex = Pattern.compile(".*?/course/(cs[0-9]{3}[a-z]?)/",
                Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher regexMatcher = regex.matcher(path);
        
        //If the the pattern was found, meaning cakehat is running inside a course directory, extract the course from
        //the path
        String course = null;
        if(regexMatcher.find())
        {
            course = regexMatcher.group(1);
        }
        
        return course;
    }
    
    @Override
    public String getCourse()
    {
        return _course;
    }

    @Override
    public String getStudentGroup()
    {
        return _studentGroup;
    }

    @Override
    public String getTAGroup()
    {
        return _taGroup;
    }

    @Override
    public String getHTAGroup()
    {
        return _htaGroup;
    }
}