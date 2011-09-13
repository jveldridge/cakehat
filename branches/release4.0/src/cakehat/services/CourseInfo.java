package cakehat.services;

/**
 * Constants used throughout the program that are based off of the course code,
 * e.g. cs000.
 *
 * @author jak2
 */
public interface CourseInfo
{
    /**
     * Retrieves the course code, e.g. cs000. This is done by examining the
     * location of the running code. If it is determined that the code is
     * running from the cakehat jar as it would be during normal operation, the
     * course code is extracted from the path. If the code is instead believed
     * to be running in development mode, a hard coded test value is used.
     *
     * @return
     */
    public String getCourse();

    /**
     * The course's student group.
     * <br/><br/>
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
     * <br/><br/>
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
     * <br/><br/>
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