package cakehat.services;

/**
 * Constants used throughout the program that are based off of the course code, e.g. cs000.
 *
 * @author jak2
 */
public interface CourseInfo
{
    /**
     * Retrieves the course code, e.g. cs000. This is done by examining the location of the running code. If it is
     * determined that the code is from a course directory as it would be during normal operation, the course code is
     * extracted from the path. If the code is instead running in development mode or from a test, a hard coded test
     * value is used.
     *
     * @return
     */
    public String getCourse();

    /**
     * The course's student group.
     * 
     * <pre>
     * {@code
     * <course>student
     * }
     * </pre>
     *
     * @return
     */
    public String getStudentGroup();

    /**
     * The course's TA group.
     * 
     * <pre>
     * {@code
     * <course>ta
     * }
     * </pre>
     *
     * @return
     */
    public String getTAGroup();

    /**
     * The course's HTA group.
     * 
     * <pre>
     * {@code
     * <course>hta
     * }
     * </pre>
     *
     * @return
     */
    public String getHTAGroup();
}