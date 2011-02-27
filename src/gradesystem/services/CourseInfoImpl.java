package gradesystem.services;

/**
 * 
 * @author jak2
 */
public class CourseInfoImpl implements CourseInfo
{
    //For testing purposes, specifies which course this is being run for
    private final static String TESTING_COURSE = "cs000";

    String _course = null;
    public String getCourse()
    {
        if(_course == null)
        {
            //Get the location of where this code is running
            String loc = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();

            //If this is actually the jar we are running from
            if(loc.endsWith("jar") && loc.startsWith("/course/cs"))
            {
                String course = loc.replace("/course/", "");
                course = course.substring(0, course.indexOf("/"));

                return course;
            }
            else
            {
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
}