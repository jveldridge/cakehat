package cakehat;

import cakehat.CakehatSession.CakehatSessionProvider;
import cakehat.CakehatSession.ConnectionType;

/**
 *
 * @author jak2
 */
public class TestCakehatSessionProvider implements CakehatSessionProvider
{
    private final String _course;
    private final boolean _didStartNormally;
    private final boolean _isDeveloperMode;
    private final CakehatRunMode _runMode;
    private final int _userId;
    private final ConnectionType _connectionType;
    
    public TestCakehatSessionProvider(String course, boolean didStartNormally, boolean isDeveloperMode,
            CakehatRunMode runMode, int userId, ConnectionType connectionType)
    {
        _course = course;
        _didStartNormally = didStartNormally;
        _isDeveloperMode = isDeveloperMode;
        _runMode = runMode;
        _userId = userId;
        _connectionType = connectionType;
    }
    
    public TestCakehatSessionProvider(int userId)
    {
        this("cs000", false, false, CakehatRunMode.UNKNOWN, userId, ConnectionType.UNKNOWN);
    }
    
    public TestCakehatSessionProvider(String course, int userId)
    {
        this(course, false, false, CakehatRunMode.UNKNOWN, userId, ConnectionType.UNKNOWN);
    }
    
    @Override
    public String getCourse()
    {
        return _course;
    }

    @Override
    public boolean didStartNormally()
    {
        return _didStartNormally;
    }

    @Override
    public boolean isDeveloperMode()
    {
        return _isDeveloperMode;
    }

    @Override
    public CakehatRunMode getRunMode()
    {
        return _runMode;
    }

    @Override
    public int getUserId()
    {
        return _userId;
    }

    @Override
    public ConnectionType getUserConnectionType()
    {
        return _connectionType;
    }
}