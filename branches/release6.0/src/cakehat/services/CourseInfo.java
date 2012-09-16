package cakehat.services;

/**
 * Constants used throughout the program that are based off of the course code, e.g. cs000.
 *
 * @author jak2
 */
public interface CourseInfo
{
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