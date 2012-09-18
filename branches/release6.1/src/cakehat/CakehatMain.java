package cakehat;

import cakehat.CakehatSession.CakehatSessionProvider;
import cakehat.CakehatSession.ConnectionType;
import cakehat.database.DbTA;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import support.utils.posix.NativeException;

/**
 * The entry point for the cakehat grading system.
 *
 * @author jak2
 */
public class CakehatMain
{
    public static enum TerminalOption
    {
        COURSE("-c", "--course", true, 1, 1),
        RUN_MODE("-m", "--mode", false, 1, 1),
        ENTER_GRADE_ARGS("-g", "--gradeargs", false, 0, Integer.MAX_VALUE),
        DEVELOPER_MODE("-d", "--developer", false, 0, 0);
        
        private final String _shortOption, _longOption;
        private final boolean _required;
        private final int _minValues, _maxValues;
        
        private TerminalOption(String shortOption, String longOption, boolean required, int minValues, int maxValues)
        {
            _shortOption = shortOption;
            _longOption = longOption;
            _required = required;
            _minValues = minValues;
            _maxValues = maxValues;
        }
        
        /**
         * Parses the {@code terminalArg} returning a matching {@code TerminalOption} if one exists and {@code null}
         * otherwise.
         * 
         * @param terminalArg
         * @return 
         */
        static TerminalOption parse(String terminalArg)
        {
            TerminalOption matchingOption = null;
            for(TerminalOption option : values())
            {
                if(option.getShortOption().equals(terminalArg) || option.getLongOption().equals(terminalArg))
                {
                    matchingOption = option;
                    break;
                }
            }
            
            return matchingOption;
        }
        
        public String getShortOption()
        {
            return _shortOption;
        }
        
        public String getLongOption()
        {
            return _longOption;
        }
        
        boolean isRequired()
        {
            return _required;
        }
        
        int getMinValues()
        {
            return _minValues;
        }
        
        int getMaxValues()
        {
            return _maxValues;
        }
    }
    
    /**
     * Provider of core information on the course, user, environment, and how cakehat was initialized.
     */
    private volatile static CakehatMainSessionProvider _sessionProvider;
    
    /**
     * Ensures that {@link #setRunMode(cakehat.CakehatRunMode, java.util.Map)} is never called more than once.
     */
    private static final AtomicBoolean _hasSetRunMode = new AtomicBoolean(false);
    
    /**
     * cakehat's main line. Parses the terminal arguments and then proceeds with initialization if the required
     * arguments are present.
     * 
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args)
    {
        //This is purposely not a Guava multimap because their implementations do not allow for mapping a key to an
        //empty list. This functionality is needed for options which are specified, but have no associated values.
        Map<TerminalOption, List<String>> parsedArgs = new EnumMap<TerminalOption, List<String>>(TerminalOption.class);
        
        boolean optionsValid = true;
        
        //Parse arguments
        TerminalOption currOption = null;
        for(String arg : args)
        {
            TerminalOption option = TerminalOption.parse(arg);
            if(option != null)
            {
                parsedArgs.put(option, new ArrayList<String>());
                currOption = option;
            }
            else if(currOption == null)
            {
                System.out.println("invalid option: " + arg);
                optionsValid = false;
            }
            else
            {
                parsedArgs.get(currOption).add(arg);
            }
        }
        
        //Validate arguments
        for(TerminalOption option : TerminalOption.values())
        {
            if(option.isRequired() && !parsedArgs.containsKey(option))
            {
                System.out.println("required option not provided: " + option.getShortOption());
                optionsValid = false;
            }
            else if(parsedArgs.containsKey(option))
            {
                List<String> values = parsedArgs.get(option);
                if(values.size() < option.getMinValues())
                {
                    System.out.println(option.getShortOption() + " requires at least " +  option.getMinValues() +
                            " value(s)");
                    optionsValid = false;
                }
                else if(values.size() > option.getMaxValues())
                {
                    System.out.println(option.getShortOption() + " supports at most " +  option.getMaxValues() +
                            " value(s)");
                    optionsValid = false;
                }
            }
        }
        
        if(optionsValid)
        {
            init(parsedArgs);
        }
    }
    
    private static void init(Map<TerminalOption, List<String>> parsedArgs)
    {   
        //Setup the cakehat session provider
        boolean isDeveloperMode = parsedArgs.containsKey(TerminalOption.DEVELOPER_MODE);
        String course = parsedArgs.get(TerminalOption.COURSE).get(0);
        _sessionProvider = new CakehatMainSessionProvider(course, isDeveloperMode);
        CakehatSession.setSessionProvider(_sessionProvider);
        
        ErrorReporter.initialize();
        
        //Causes the allocator to create an instance of itself when cakehat is running on a single thread ensuring that
        //only one of it will ever be created (stores itself in a volatile variable)
        Allocator.getInstance();
        
        //Retrieve the parsed run mode; there may not be one
        String runModeArg = parsedArgs.containsKey(TerminalOption.RUN_MODE) ?
                parsedArgs.get(TerminalOption.RUN_MODE).get(0) : null;
        if(runModeArg == null)
        {
            //When running in developer mode, if no run mode was specified then launch UI to let the user choose
            if(isDeveloperMode)
            {
                applyLookAndFeel();
                ChooseModeView.launch(parsedArgs);
            }
            //If not in developer mode, then launch use default run mode
            else
            {
                setRunMode(CakehatRunMode.DEFAULT_RUN_MODE, parsedArgs);
            }
        }
        else
        {
            CakehatRunMode runMode = CakehatRunMode.getFromTerminalFlag(runModeArg);
            if(runMode == null)
            {
                System.out.println("Invalid run mode: " + runModeArg + "\n" +
                        "Valid run modes: " + CakehatRunMode.getValidModes());
            }
            else
            {
                setRunMode(runMode, parsedArgs);
            }
        }
    }
    
    /**
     * Sets the mode that cakehat is running in. This method may only be called once.
     *
     * @param runMode
     * @param parsedArgs
     */
    static void setRunMode(CakehatRunMode runMode, Map<TerminalOption, List<String>> parsedArgs)
    {
        if(_hasSetRunMode.getAndSet(true))
        {
            throw new IllegalStateException("cakehat's run mode may only be set once");
        }

        _sessionProvider.setRunMode(runMode);
        
        try
        {
            String course = parsedArgs.get(TerminalOption.COURSE).get(0);
            
            if(isUserTA())
            {
                boolean isCakehatConfigured = isCakehatConfigured();
                if(runMode.requiresConfiguredCakehat() && !isCakehatConfigured)
                {
                    System.out.println("cakehat has not been configured for your course");
                    if(isUserHTA())
                    {
                        System.out.println("You can configure cakehat for your course by running:");
                    }
                    else
                    {
                        System.out.println("A HTA for your course can configure cakehat by running:");
                    }
                    System.out.println("cakehat " + TerminalOption.COURSE.getShortOption() + " " + course +
                            " " + TerminalOption.RUN_MODE.getShortOption() + " " +
                            CakehatRunMode.CONFIG.getTerminalValue());
                }
                else
                {
                    boolean hasPermissionToRun;
                    if(isCakehatConfigured)
                    {
                        DbTA ta = initializeTA();
                        hasPermissionToRun = runMode.requiresAdminPrivileges() ? ta.isAdmin() : true;
                        
                        if(!hasPermissionToRun)
                        {
                            System.out.println("This cakehat mode requires administrative privileges");
                        }
                    }
                    else
                    {
                        hasPermissionToRun = runMode.requiresAdminPrivileges() ? isUserHTA() : true;
                        
                        if(!hasPermissionToRun)
                        {
                            System.out.println("Until cakehat is configured, only a HTA can run this mode");
                        }
                    }
                    
                    if(hasPermissionToRun)
                    {
                        if(runMode.hasGUI())
                        {
                            applyLookAndFeel();
                            if(adjustIfRemote())
                            {
                                _sessionProvider.setUserConnectionType(ConnectionType.REMOTE);
                            }
                            else
                            {
                                _sessionProvider.setUserConnectionType(ConnectionType.LOCAL);
                            }
                        }

                        if(runMode.requiresTempDir())
                        {
                            try
                            {
                                Allocator.getFileSystemServices().makeTempDir();
                            }
                            catch(ServicesException e)
                            {
                                throw new CakehatException("Unable to create user temporary directory", e);
                            }
                        }

                        if(runMode.backupDatabaseOnShutdown())
                        {
                            Runtime.getRuntime().addShutdownHook(new Thread()
                            {
                                public void run()
                                {
                                    try
                                    {
                                        Allocator.getFileSystemServices().makeDatabaseBackup();
                                    }
                                    catch(ServicesException e)
                                    {
                                        //Print the failure to the terminal - there's no opportunity during a shutdown
                                        //hook to bring up the ErrorReporter
                                        System.err.println("Unable to backup database");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                        runMode.run(parsedArgs, isCakehatConfigured);
                    }
                }
            }
            else
            {
                System.out.println("You are not a member of the course's TA group\n" +
                        "Course: " + course);
            }
        }
        catch(CakehatException e)
        {
            System.err.println("cakehat could not initialize properly; please try again.\n" +
                               "If this problem persists, please contact the cakehat team.\n" +
                               "Underlying cause:\n");
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * Determines if the user is a member of the TA group.
     * 
     * @return
     * @throws CakehatException 
     */
    private static boolean isUserTA() throws CakehatException
    {
        try
        {
            return Allocator.getUserServices().getTALogins().contains(Allocator.getUserUtilities().getUserLogin());
        }
        catch(NativeException e)
        {
            throw new CakehatException("Unable to determine if user is a member of the course's TA group", e);
        }
    }
    
    /**
     * Determines if the user is a member of the HTA group.
     * 
     * @return
     * @throws CakehatException 
     */
    private static boolean isUserHTA() throws CakehatException
    {
        try
        {
            return Allocator.getUserServices().getHTALogins().contains(Allocator.getUserUtilities().getUserLogin());
        }
        catch(NativeException e)
        {
            throw new CakehatException("Unable to determine if user is a member of the course's HTA group", e);
        }
    }

    /**
     * Determines if cakehat has been configured for this course and year. cakehat is considered to be configured if the
     * database exists and contains at least one TA in it.
     * @return 
     */
    private static boolean isCakehatConfigured() throws CakehatException
    {
        boolean configured = false;
        if(Allocator.getPathServices().getDatabaseFile().exists())
        {
            try
            {
                configured = !Allocator.getDatabase().getTAs().isEmpty();
            }
            catch(SQLException e)
            {
                throw new CakehatException("Unable to access cakehat database", e);
            }
        }
        
        return configured;
    }
    
    /**
     * Gets the TA object. If the TA is not in the database, they will be added. If the TA's login in the database no
     * longer matches their login then the login will be updated in the database.
     * 
     * @return
     * @throws CakehatException 
     */
    private static DbTA initializeTA() throws CakehatException
    {
        try
        {   
            //Find the DbTA that corresponds to the user running cakehat
            Set<DbTA> tas = Allocator.getDatabase().getTAs();
            DbTA userTA = null;
            int userID = Allocator.getUserUtilities().getUserId();
            for(DbTA ta : tas)
            {
                if(ta.getId() == userID)
                {
                    userTA = ta;
                    break;
                }
            }
            
            String userLogin = Allocator.getUserUtilities().getUserLogin();
            
            //If there is no matching record in the database
            if(userTA == null)
            {
                //Add the TA to the database
                String[] nameParts = Allocator.getUserUtilities().getUserLogin(userID).split(" ");
                String firstName = nameParts[0];
                String lastName = nameParts[nameParts.length - 1];
                userTA = new DbTA(userID, userLogin, firstName, lastName, true, isUserHTA());
                Allocator.getDatabase().putTAs(ImmutableSet.of(userTA));
            }
            //If the TA is in the database, but the TA's login no longer matches the database
            else if(!userTA.getLogin().equals(userLogin))
            {
                //Update the TA's login in the database
                userTA.setLogin(userLogin);
                Allocator.getDatabase().putTAs(ImmutableSet.of(userTA));
            }
            
            return userTA;
        }
        catch(SQLException e)
        {
            throw new CakehatException("Could not retrieve TAs from database or insert/update TA into database.", e);
        }
        catch(NativeException e)
        {
            throw new CakehatException("Could not retrieve TA related information with native system calls.", e);
        }
    }
    
    /**
     * If the user is running over SSH some UI properties will be changed to improve performance.
     */
    private static boolean adjustIfRemote()
    {
        boolean isSSH = false;
        try
        {
            isSSH = Allocator.getUserUtilities().isUserRemotelyConnected();
        }
        catch(NativeException e)
        {
            System.err.println("Unable to determine if you are remotely connected.\n" +
                               "cakehat will run as if you were running locally.\n" +
                               "Underlying cause:\n");
            e.printStackTrace(System.err);
        }
        
        // Turn off anti-aliasing if running cakehat remotely (ssh)
        if(isSSH)
        {
            System.setProperty("awt.useSystemAAFontSettings", "false");
            System.setProperty("swing.aatext", "false");
        }
        
        return isSSH;
    }

    /**
     * Applies the look and feel that cakehat uses, which may differ from the default look and feel used by a given
     * operating system or Linux windowing toolkit.
     */
    private static void applyLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        }
        // Depending on the windowing toolkit the user has, this call may fail but cakehat most likely will still appear
        // similar enough to what is intended to be functional
        catch(Exception e)
        {
            System.err.println("cakehat could not set its default appearance.\n" +
                               "Some interfaces may not appear as intended.\n" +
                               "Underlying cause:\n");
            e.printStackTrace(System.err);
        }
    }

    /**
     * This method should only be called from main methods used for testing.
     */
    public static void initializeForTesting() throws CakehatException
    {
        applyLookAndFeel();  
    }
    
    private static class CakehatMainSessionProvider implements CakehatSessionProvider
    {
        private final String _course;
        private final boolean _isDeveloperMode;
        private volatile CakehatRunMode _runMode = CakehatRunMode.UNKNOWN;
        private volatile CakehatSession.ConnectionType _connectionType = CakehatSession.ConnectionType.UNKNOWN;
        private final int USER_ID = Allocator.getUserUtilities().getUserId();
        
        private CakehatMainSessionProvider(String course, boolean isDeveloperMode)
        {
            _course = course;
            _isDeveloperMode = isDeveloperMode;
        }
        
        @Override
        public String getCourse()
        {
            return _course;
        }
        
        @Override
        public boolean didStartNormally()
        {
            return true;
        }
        
        @Override
        public boolean isDeveloperMode()
        {
            return _isDeveloperMode;
        }
        
        private void setRunMode(CakehatRunMode runMode)
        {
            _runMode = runMode;
        }

        @Override
        public CakehatRunMode getRunMode()
        {
            return _runMode;
        }

        @Override
        public int getUserId()
        {
            return USER_ID;
        }

        private void setUserConnectionType(ConnectionType connectionType)
        {
            _connectionType = connectionType;
        }
        
        @Override
        public ConnectionType getUserConnectionType()
        {
            return _connectionType;
        }
    }
}