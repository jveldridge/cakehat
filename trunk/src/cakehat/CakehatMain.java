package cakehat;

import cakehat.newdatabase.DbTA;
import cakehat.services.ServicesException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import support.utils.SingleElementSet;
import support.utils.posix.NativeException;

/**
 * The entry point for the cakehat grading system.
 *
 * @author jak2
 */
public class CakehatMain
{
    private volatile static boolean _isDeveloperMode = false;
    private volatile static boolean _didStartNormally = false;
    private volatile static CakehatRunMode _runMode = CakehatRunMode.UNKNOWN;
    
    private volatile static boolean _hasSetRunMode = false;

    public static void main(String[] args)
    {
        _didStartNormally = true;

        CakehatUncaughtExceptionHandler.registerHandler();
        
        if(args.length == 0)
        {
            applyLookAndFeel();
            _isDeveloperMode = true;
            DeveloperModeView.launch(args);
        }
        else
        {       
            setRunMode(CakehatRunMode.getFromTerminalFlag(args[0]), args);
        }
    }
    
    /**
     * If the application was run in developer mode, meaning the developer was able to select the grader, admin, or
     * config view.
     *
     * @return
     */
    public static boolean isDeveloperMode()
    {
        return _isDeveloperMode;
    }

    /**
     * Whether or not this class's main method was run. This should be the case during normal operation, but will not be
     * the case during a test.
     * 
     * @return
     */
    public static boolean didStartNormally()
    {
        return _didStartNormally;
    }

    /**
     * The mode in which cakehat is running.
     *
     * @return
     */
    public static CakehatRunMode getRunMode()
    {
        return _runMode;
    }

    /**
     * Sets the mode that cakehat is running in. This method may only be called once.
     *
     * @param runMode
     * @param args
     */
    static synchronized void setRunMode(CakehatRunMode runMode, String[] args)
    {
        if(_hasSetRunMode)
        {
            throw new IllegalStateException("cakehat's run mode may only be set once");
        }
        _hasSetRunMode = true;
        _runMode = runMode;
        
        if(_runMode != CakehatRunMode.UNKNOWN)
        {
            try
            {
                if(validateUser(_runMode))
                {
                    if(_runMode.requiresLoadDataCache())
                    {
                        loadDataCache();
                    }

                    boolean isSSH = false;
                    if(_runMode.hasGUI())
                    {
                        isSSH = adjustIfRemote();
                    }

                    //Creating the ArrayList is necessary because the list created by Arrays.asList(...) is immutable
                    ArrayList<String> argList = new ArrayList<String>(Arrays.asList(args));
                    //If an argument was used to launch cakehat, remove it
                    if(!argList.isEmpty())
                    {
                        argList.remove(0); 
                    }

                    _runMode.run(argList, isSSH);
                }
            }
            catch(CakehatException e)
            {
                System.err.println("cakehat could not initialize properly; please try again.\n" +
                                   "If this problem persists, please contact the cakehat team.\n" +
                                   "Contact address: " + Allocator.getConstants().getCakehatEmailAddress() +"\n" +
                                   "Underlying cause:\n");
                e.printStackTrace(System.err);
            }
        }
        else
        {
            System.err.println("Invalid run mode: " + args[0] + "\n" +
                               "Valid modes: " + CakehatRunMode.getValidModes());
        }
    }
    
    /**
     * Confirms that the user can proceed to run cakehat. In some situations the user will be added to the database if
     * they are a member of the TA group.
     * 
     * @param runMode
     * @return
     * @throws CakehatException 
     */
    private static boolean validateUser(CakehatRunMode runMode) throws CakehatException
    {
        try
        {
            boolean canProceed = false;
            
            //Find the DbTA that corresponds to the user running cakehat
            Set<DbTA> tas = Allocator.getDatabaseV5().getTAs();
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
                List<String> taLogins = Allocator.getUserServices().getTALogins();
            
                if(taLogins.contains(userLogin))
                {
                    List<String> htaLogins = Allocator.getUserServices().getHTALogins(); 
                    
                    //If there are already TAs in the database, add the TA currently running cakehat
                    //Otherwise do not because that means cakehat is not set up yet
                    if(!tas.isEmpty())
                    {
                        String[] nameParts = Allocator.getUserUtilities().getUserLogin(userID).split(" ");
                        String firstName = nameParts[0];
                        String lastName = nameParts[nameParts.length - 1];
                        userTA = new DbTA(userID, userLogin, firstName, lastName, true, htaLogins.contains(userLogin));
                        
                        Allocator.getDatabaseV5().putTAs(SingleElementSet.of(userTA));
                        
                        canProceed = true;
                    }
                    else
                    {
                        if(runMode == CakehatRunMode.CONFIG)
                        {
                            if(htaLogins.contains(userLogin))
                            {
                                canProceed = true;
                            }
                            else
                            {
                                System.out.println("cakehat has not yet been set up.\n" +
                                                   "A HTA may set up cakehat by running: cakehat config\n" +
                                                   Allocator.getCourseInfo().getCourse() + " HTAs: " + htaLogins);
                            }
                        }
                        else
                        {
                            if(htaLogins.contains(userLogin))
                            {
                                System.out.println("cakehat has not yet been set up, please run: cakehat config");
                            }
                            else
                            {
                                System.out.println("cakehat has not yet been set up.\n" +
                                                   "A HTA may set up cakehat by running: cakehat config\n" +
                                                   Allocator.getCourseInfo().getCourse() + " HTAs: " + htaLogins);
                            }
                        }
                    }
                }
                else
                {
                    System.out.println("You are a not a member of " + Allocator.getCourseInfo().getTAGroup());
                }
            }
            //The TA is in the database
            else
            {
                //If the TA's login no longer matches the database - update it
                if(!userTA.getLogin().equals(userLogin))
                {
                    userTA.setLogin(userLogin);
                    Allocator.getDatabaseV5().putTAs(SingleElementSet.of(userTA));
                }
                
                //Determine if the user is allowed access to the run mode
                if(runMode.requiresAdminPrivileges())
                {
                    if(userTA.isAdmin())
                    {
                        canProceed = true;
                    }
                    else
                    {
                        System.out.println("This cakehat mode requires administrative privileges.\n" +
                                           "A HTA may grant you administrative privileges by running: cakehat config");
                    }
                }
                else
                {
                    canProceed = true;
                }
            }
            
            return canProceed;
        }
        catch(SQLException e)
        {
            throw new CakehatException("Could not retrieve TAs from database or insert/update TA into database", e);
        }
        catch(NativeException e)
        {
            throw new CakehatException("Could not retrieve TA related information with native system calls", e);
        }
    }
    
    /**
     * Loads and caches data from the database.
     * 
     * @throws CakehatException 
     */
    private static void loadDataCache() throws CakehatException
    {
        try
        {
            Allocator.getDataServicesV5().updateDataCache();
        }
        catch(ServicesException e)
        {
            throw new CakehatException("Could not load data to be cached.", e);
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
     * This method should only be called from main methods used for testing. Loads cached data into memory and applies
     * look and feel.
     */
    public static void initializeForTesting() throws CakehatException
    {
        loadDataCache();
        applyLookAndFeel();  
    }
}