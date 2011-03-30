package cakehat;

/**
 * The mode that cakehat is operating under. cakehat will be in the
 * <code>UNKNOWN</code> mode if cakehat code did not start by running
 * {@link CakehatMain} or a view has yet to be selected via
 * {@link DeveloperModeView}.
 *
 * @author jak2
 */
public enum CakehatRunMode
{
    FRONTEND("frontend"), ADMIN("admin"), LAB("lab"), UNKNOWN(null);

    private final String _terminalFlag;

    private CakehatRunMode(String terminalFlag)
    {
        _terminalFlag = terminalFlag;
    }

    String getTerminalFlag()
    {
        return _terminalFlag;
    }
}