package gradesystem.services;

import gradesystem.Allocator;
import gradesystem.config.CourseInfo;
import gradesystem.config.TA;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import utils.UserUtilities;

import gradesystem.Allocator.SingletonAllocation;

import static org.junit.Assert.*;
import org.easymock.EasyMock;

//import static org.easymock.EasyMock.*;
//Normally would be imported like this (as is done for junit),
//but to make it clearer for this demo what EasyMock is being
//used for, it has not been.

public class UserServicesTest
{
    /**
     * See more on EasyMock here:
     * http://easymock.org/EasyMock3_0_Documentation.html
     */


    @Test
    public void isUserTA_test()
    {
        //Mocked User Utilities
        final UserUtilities userUtils = EasyMock.createMock(UserUtilities.class);
        EasyMock.expect(userUtils.getUserLogin()).andReturn("jak2");
        EasyMock.replay(userUtils);

        SingletonAllocation<UserUtilities> userUtilsAlloc =
            new SingletonAllocation<UserUtilities>()
            {
                public UserUtilities allocate() { return userUtils; };
            };

        //Mocked Course Info
        final CourseInfo courseInfo = EasyMock.createMock(CourseInfo.class);

        TA ta = EasyMock.createMock(TA.class);
        EasyMock.expect(ta.getLogin()).andReturn("jak2");
        EasyMock.replay(ta);
        Collection<TA> tas = Arrays.asList(new TA[] { ta });

        EasyMock.expect(courseInfo.getTAs()).andReturn(tas);
        EasyMock.replay(courseInfo);

        SingletonAllocation<CourseInfo> courseInfoAlloc =
            new SingletonAllocation<CourseInfo>()
            {
                public CourseInfo allocate() { return courseInfo; };
            };

        //Customizer the Allocator with mocked objects
        new Allocator.Customizer()
                .setCourseInfo(courseInfoAlloc)
                .setUserUtils(userUtilsAlloc)
                .customize();

        //Assertion
        assertEquals(true, new UserServicesImpl().isUserTA());

        //Verify that mocked methods were called
        EasyMock.verify(userUtils);
        EasyMock.verify(ta);
        EasyMock.verify(courseInfo);
    }
}