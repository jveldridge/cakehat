package cakehat;

import cakehat.labcheckoff.CheckoffCLI;
import cakehat.views.admin.AdminView;
import cakehat.views.config.ConfigManagerView;
import cakehat.views.grader.GraderView;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;

/**
 * The mode that cakehat is operating under. cakehat will be in the {@link CakehatRunMode#UNKNOWN} mode if cakehat code
 * did not start by running {@link CakehatMain} or a mode has yet to be selected via {@link DeveloperModeView}.
 *
 * @author jak2
 */
public enum CakehatRunMode
{
    GRADER("grader", true,  true,  false),
    ADMIN("admin",   true,  true,  true),
    CONFIG("config", false, true,  true),
    LAB("lab",       true,  false, false),
    UNKNOWN(null,    false, false, false);

    private final String _terminalFlag;
    private final boolean _loadDataCache;
    private final boolean _hasGUI;
    private final boolean _requiresAdmin;

    private CakehatRunMode(String terminalFlag, boolean loadDataCache, boolean hasGUI, boolean requiresAdmin)
    {
        _terminalFlag = terminalFlag;
        _hasGUI = hasGUI;
        _requiresAdmin = requiresAdmin;
        _loadDataCache = loadDataCache;
    }
    
    boolean requiresLoadDataCache()
    {
        return _loadDataCache;
    }
    
    boolean hasGUI()
    {
        return _hasGUI;
    }
    
    boolean requiresAdminPrivileges()
    {
        return _requiresAdmin;
    }
    
    static Set<CakehatRunMode> getValidModes()
    {
        ImmutableSet.Builder<CakehatRunMode> validModes = ImmutableSet.builder();
        for(CakehatRunMode mode : values())
        {
            if(mode._terminalFlag != null)
            {
                validModes.add(mode);
            }
        }
        
        return validModes.build();
    }
    
    static CakehatRunMode getFromTerminalFlag(String terminalFlag)
    {
        CakehatRunMode matchingMode = UNKNOWN;
        
        for(CakehatRunMode mode : values())
        {
            if(mode._terminalFlag != null && mode._terminalFlag.equalsIgnoreCase(terminalFlag))
            {
                matchingMode = mode;
                break;
            }
        }
        
        return matchingMode;
    }
    
    void run(List<String> args, boolean isSSH)
    {
        if(this == GRADER)
        {
            GraderView.launch(isSSH);
        }
        else if(this == ADMIN)
        {
            AdminView.launch(isSSH);
        }
        else if(this == CONFIG)
        {
            ConfigManagerView.launch(isSSH);
        }
        else if(this == LAB)
        {
            CheckoffCLI.performCheckoff(args);
        }
        else if(this == UNKNOWN)
        {
            throw new IllegalStateException("Unknown mode - cannot run");
        }
    }
}