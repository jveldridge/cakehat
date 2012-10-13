package cakehat.services;

import cakehat.CakehatSession;

/**
 *
 * @author jak2
 */
public class CourseInfoImpl implements CourseInfo
{
    private final String _studentGroup = CakehatSession.getCourse() + "student";
    private final String _taGroup = CakehatSession.getCourse() + "ta";
    private final String _htaGroup = CakehatSession.getCourse() + "hta";

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