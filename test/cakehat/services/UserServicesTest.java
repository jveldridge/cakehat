package cakehat.services;

import cakehat.services.UserServicesImpl;
import cakehat.Allocator;
import cakehat.config.TA;
import java.util.Arrays;
import org.junit.Test;
import support.utils.UserUtilities;

import cakehat.Allocator.SingletonAllocation;
import cakehat.config.ConfigurationInfo;
import java.util.List;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

public class UserServicesTest
{
    @Test
    public void isUserTA_test()
    {
        //Mocked User Utilities
        final UserUtilities userUtils = createMock(UserUtilities.class);
        expect(userUtils.getUserLogin()).andReturn("jak2");
        replay(userUtils);

        SingletonAllocation<UserUtilities> userUtilsAlloc =
            new SingletonAllocation<UserUtilities>()
            {
                public UserUtilities allocate() { return userUtils; };
            };

        //Mocked Configuration Info
        final ConfigurationInfo configurationInfo = createMock(ConfigurationInfo.class);

        TA ta = createMock(TA.class);
        expect(ta.getLogin()).andReturn("jak2");
        replay(ta);
        List<TA> tas = Arrays.asList(new TA[] { ta });

        expect(configurationInfo.getTAs()).andReturn(tas);
        replay(configurationInfo);

        SingletonAllocation<ConfigurationInfo> configurationInfoAlloc =
            new SingletonAllocation<ConfigurationInfo>()
            {
                public ConfigurationInfo allocate() { return configurationInfo; };
            };

        //Customizer the Allocator with mocked objects
        new Allocator.Customizer()
                .setConfigurationInfo(configurationInfoAlloc)
                .setUserUtils(userUtilsAlloc)
                .customize();

        //Assertion
        assertEquals(true, new UserServicesImpl().isUserTA());

        //Verify that mocked methods were called
        verify(userUtils);
        verify(ta);
        verify(configurationInfoAlloc);
    }
}