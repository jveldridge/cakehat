package cakehat;

import cakehat.CakehatMain.TerminalOption;
import cakehat.views.admin.AdminView;
import cakehat.views.config.ConfigManagerView;
import cakehat.views.entergrade.EnterGradeCLI;
import cakehat.views.grader.GraderView;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The mode that cakehat is operating under. cakehat will be in the {@link CakehatRunMode#UNKNOWN} mode if cakehat code
 * did not start by running {@link CakehatMain} or a mode has yet to be selected via {@link ChooseModeView}.
 *
 * @author jak2
 */
public enum CakehatRunMode
{
    GRADER(     "grader",     true,  false, true,  false, true),
    ADMIN(      "admin",      true,  true,  true,  true,  true),
    CONFIG(     "config",     true,  true,  false, true,  false),
    ENTER_GRADE("enterGrade", false, false, false, false, true),
    UNKNOWN(    null,         false, false, false, false, false);

    public static final CakehatRunMode DEFAULT_RUN_MODE = GRADER;
    
    private final String _terminalValue;
    private final boolean _hasGUI;
    private final boolean _requiresAdmin;
    private final boolean _requiresTempDir;
    private final boolean _backupDatabaseOnShutdown;
    private final boolean _requiresConfiguredCakehat;

    private CakehatRunMode(String terminalValue, boolean hasGUI, boolean requiresAdmin, boolean requiresTempDir,
            boolean backupDatabaseOnShutdown, boolean requiresConfiguredCakehat)
    {
        _terminalValue = terminalValue;
        _hasGUI = hasGUI;
        _requiresAdmin = requiresAdmin;
        _requiresTempDir = requiresTempDir;
        _backupDatabaseOnShutdown = backupDatabaseOnShutdown;
        _requiresConfiguredCakehat = requiresConfiguredCakehat;
    }
    
    public String getTerminalValue()
    {
        return _terminalValue;
    }
    
    public boolean hasGUI()
    {
        return _hasGUI;
    }
    
    boolean requiresAdminPrivileges()
    {
        return _requiresAdmin;
    }
    
    boolean requiresTempDir()
    {
        return _requiresTempDir;
    }
    
    boolean backupDatabaseOnShutdown()
    {
        return _backupDatabaseOnShutdown;
    }
    
    boolean requiresConfiguredCakehat()
    {
        return _requiresConfiguredCakehat;
    }
    
    static Set<CakehatRunMode> getValidModes()
    {
        ImmutableSet.Builder<CakehatRunMode> validModes = ImmutableSet.builder();
        for(CakehatRunMode mode : values())
        {
            if(mode._terminalValue != null)
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
            if(mode._terminalValue != null && mode._terminalValue.equalsIgnoreCase(terminalFlag))
            {
                matchingMode = mode;
                break;
            }
        }
        
        return matchingMode;
    }
    
    void run(Map<TerminalOption, List<String>> parsedArgs, boolean isCakehatConfigured)
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
            ConfigManagerView.launch(isCakehatConfigured);
        }
        else if(this == ENTER_GRADE)
        {
            EnterGradeCLI.performEnterGrade(parsedArgs.get(TerminalOption.ENTER_GRADE_ARGS));
        }
        else if(this == UNKNOWN)
        {
            throw new IllegalStateException("Unknown mode - cannot run");
        }
    }
}