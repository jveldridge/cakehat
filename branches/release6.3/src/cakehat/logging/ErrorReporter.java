package cakehat.logging;

import cakehat.CakehatSession;
import com.google.common.collect.ImmutableList;
import java.awt.GraphicsEnvironment;
import java.util.List;
import support.ui.ProgressDialog;

/**
 * Manages displaying and reporting of cakehat errors.
 *
 * @author jak2
 */
public class ErrorReporter
{
    private ErrorReporter() { }
    
    /**
     * An ordered list of reporters for throwables received by this class. Each reporter in order is given the
     * opportunity to handle the reported issue.
     */
    private static final ImmutableList<? extends ThrowableReporter> REPORTERS = ImmutableList.of(
            new FilePermissionExceptionReporter(),
            new DefaultThrowableReporter());
    
    /**
     * Initializes the error reporter. This should be called during cakehat initialization.
     */
    public static void initialize()
    {
        CakehatUncaughtExceptionHandler.registerHandler();
    }
    
    /**
     * Returns an exception reporter that is backed by this class.
     * 
     * @return exception reporter
     */
    public static ProgressDialog.ExceptionReporter getExceptionReporter()
    {
        ProgressDialog.ExceptionReporter reporter = new ProgressDialog.ExceptionReporter()
        {
            @Override
            public void report(String message, Exception exception)
            {
                ErrorReporter.report(message, exception);
            }
        };
        
        return reporter;
    }
    
    /**
     * Reports the {@code error} along with an associated {@code message}. The way the error is reported to the user
     * will differ depending on the graphical environment they are running in.
     * 
     * @param message may be {@code null}
     * @param error may be {@code null}
     */
    public static void report(String message, Throwable error)
    {
        printThrowableIfDeveloper(error);
        
        List<Throwable> causalStack = buildCausalStack(error);
        boolean useGUI = CakehatSession.getRunMode().hasGUI() && !GraphicsEnvironment.isHeadless();
        
        //Iterate through the reporters, stopping after the first one returns true which means it reported the issue
        for(ThrowableReporter reporter : REPORTERS)
        {
            if(reporter.report(causalStack, message, useGUI))
            {
                break;
            }
        }
    }
    
    /**
     * Reports an error described by {@code message}. The way the error is reported to the user will differ depending on
     * the graphical environment they are running in.
     * 
     * @param message may be {@code null}
     */
    public static void report(String message)
    {
        report(message, null);
    }
    
    /**
     * Reports an {@code error}. The way the error is reported to the user will differ depending on the graphical
     * environment they are running in.
     * 
     * @param error may be {@code null}
     */
    public static void report(Throwable error)
    {
        report(null, error);
    }
    
    /**
     * Reports an error that is an uncaught exception that is not otherwise handled by cakehat.
     * 
     * @param uncaughtException 
     */
    public static void reportUncaughtException(Throwable uncaughtException)
    {
        report("An unhandled exception has occurred. Please report this.", uncaughtException);
    }
    
    /**
     * Builds a list ordered from {@code throwable} to its originating cause retrieved by successively calling
     * {@link Throwable#getCause()}.
     * 
     * @param throwable
     * @return 
     */
    private static List<Throwable> buildCausalStack(Throwable throwable)
    {
        ImmutableList.Builder<Throwable> stack = ImmutableList.builder();
        for(Throwable curr = throwable; curr != null; curr = curr.getCause())
        {
            stack.add(curr);
        }
        
        return stack.build();
    }
    
    private static void printThrowableIfDeveloper(Throwable throwable)
    {
        //If cakehat is running in developer mode or cakehat is not running normally, then print the stack trace to aid
        //debugging (most IDEs allow for clicking inside the stack trace to navigate to the corresponding file and line)
        if(CakehatSession.isDeveloperMode() || !CakehatSession.didStartNormally())
        {
            System.err.println("Throwable encountered. During normal operation cakehat will not print the stack " +
                    "trace to the terminal.");
            throwable.printStackTrace(System.err);
            System.err.println("");
        }
    }
}