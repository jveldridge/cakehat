package cakehat.services;

import cakehat.Allocator;
import cakehat.config.TA;
import cakehat.Allocator.SingletonAllocation;
import cakehat.config.ConfigurationInfo;
import support.utils.UserUtilities;
import org.junit.Test;
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
        replay(ta);

        expect(configurationInfo.getTA("jak2")).andReturn(ta);
        replay(configurationInfo);

        SingletonAllocation<ConfigurationInfo> configurationInfoAlloc =
            new SingletonAllocation<ConfigurationInfo>()
            {
                public ConfigurationInfo allocate() { return configurationInfo; };
            };

        //Customize the Allocator with mocked objects
        new Allocator.Customizer()
                .setConfigurationInfo(configurationInfoAlloc)
                .setUserUtils(userUtilsAlloc)
                .customize();

        //Assertion
        assertEquals(true, new UserServicesImpl().isUserTA());

        //Verify that mocked methods were called
        verify(userUtils);
        verify(configurationInfo);
    }
}