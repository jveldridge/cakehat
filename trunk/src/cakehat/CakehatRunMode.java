package cakehat;

import cakehat.views.admin.AdminView;
import cakehat.views.config.ConfigManagerView;
import cakehat.views.entergrade.EnterGradeCLI;
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
    GRADER(     "grader",     true,  false, true,  false),
    ADMIN(      "admin",      true,  true,  true,  true),
    CONFIG(     "config",     true,  true,  false, true),
    ENTER_GRADE("enterGrade", false, false, false, false),
    UNKNOWN(    null,         false, false, false, false);

    private final String _terminalFlag;
    private final boolean _hasGUI;
    private final boolean _requiresAdmin;
    private final boolean _requiresWorkspaceDir;
    private final boolean _backupDatabaseOnShutdown;

    private CakehatRunMode(String terminalFlag, boolean hasGUI, boolean requiresAdmin, boolean requiresWorkspaceDir,
            boolean backupDatabaseOnShutdown)
    {
        _terminalFlag = terminalFlag;
        _hasGUI = hasGUI;
        _requiresAdmin = requiresAdmin;
        _requiresWorkspaceDir = requiresWorkspaceDir;
        _backupDatabaseOnShutdown = backupDatabaseOnShutdown;
    }
    
    public boolean hasGUI()
    {
        return _hasGUI;
    }
    
    boolean requiresAdminPrivileges()
    {
        return _requiresAdmin;
    }
    
    boolean requiresWorkspaceDir()
    {
        return _requiresWorkspaceDir;
    }
    
    boolean backupDatabaseOnShutdown()
    {
        return _backupDatabaseOnShutdown;
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
    
    void run(List<String> args)
    {
        if(this == GRADER)
        {
            GraderView.launch();
        }
        else if(this == ADMIN)
        {
            AdminView.launch();
        }
        else if(this == CONFIG)
        {
            ConfigManagerView.launch();
        }
        else if(this == ENTER_GRADE)
        {
            EnterGradeCLI.performEnterGrade(args);
        }
        else if(this == UNKNOWN)
        {
            throw new IllegalStateException("Unknown mode - cannot run");
        }
    }
}