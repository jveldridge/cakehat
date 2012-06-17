package cakehat;

import support.testutils.TestUtilities;

/**
 *
 * @author jak2
 */
public class CakehatSession
{
    public static enum ConnectionType
    {
        LOCAL, REMOTE, UNKNOWN;
    }
    
    private CakehatSession() { }
    
    private static volatile CakehatSessionProvider _provider = new DidNotStartNormallyContextProvider();
    
    /**
     * Whether cakehat was started normally, meaning {@link CakehatMain#main(java.lang.String[])} was called.
     * 
     * @return 
     */
    public static boolean didStartNormally()
    {
        return _provider.didStartNormally();
    }

    /**
     * Whether cakehat was started in developer mode.
     * 
     * @return 
     */
    public static boolean isDeveloperMode()
    {
        return _provider.isDeveloperMode();
    }

    /**
     * The mode cakehat is running in. Will not be {@code null}, but may be {@link CakehatRunMode#UNKNOWN}.
     * 
     * @return 
     */
    public static CakehatRunMode getRunMode()
    {
        return _provider.getRunMode();
    }

    /**
     * The unique identifier for the user running cakehat. This is both their POSIX user id on the operating system
     * and the primary key of their entry in the TA database.
     * 
     * @return 
     */
    public static int getUserId()
    {
        return _provider.getUserId();
    }

    /**
     * Indicates whether the user is remotely connected (ssh). Will not be {@code null}. but may be
     * {@link ConnectionType#UNKNOWN} if a failure occurs in determining the information.
     * 
     * @return 
     */
    public static ConnectionType getUserConnectionType()
    {
        return _provider.getUserConnectionType();
    }
    
    /**
     * Sets the cakehat session provider. This should only be called from {@link CakehatMain} during initialization.
     * 
     * @param context 
     */
    static void setSessionProvider(CakehatSessionProvider provider)
    {
        _provider = provider;
    }
    
    /**
     * Sets the provider.
     * 
     * @param provider 
     */
    public static void setSessionProviderForTesting(CakehatSessionProvider provider)
    {
        TestUtilities.checkJUnitRunning();
        
        _provider = provider;
    }
    
    /**
     * Serves as a delegate for the methods defined in {@code CakehatSession}.
     */
    public static interface CakehatSessionProvider
    {
        public boolean didStartNormally();

        public boolean isDeveloperMode();

        public CakehatRunMode getRunMode();

        public int getUserId();

        public ConnectionType getUserConnectionType();
    }
    
    /**
     * The session provider used when cakehat was not started via {@link CakehatMain#main(java.lang.String[])}. This
     * will be the case during automation testing and when manual testing by running other mainlines. 
     */
    private static final class DidNotStartNormallyContextProvider implements CakehatSessionProvider
    {
        @Override
        public boolean didStartNormally()
        {
            return false;
        }

        @Override
        public boolean isDeveloperMode()
        {
            return false;
        }

        @Override
        public CakehatRunMode getRunMode()
        {
            return CakehatRunMode.UNKNOWN;
        }

        @Override
        public int getUserId()
        {
            return Allocator.getUserUtilities().getUserId();
        }

        @Override
        public ConnectionType getUserConnectionType()
        {
            return ConnectionType.UNKNOWN;
        }
    }
}