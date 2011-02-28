package gradesystem;

import gradesystem.views.shared.ErrorView;
import java.awt.GraphicsEnvironment;

/**
 * Handles exceptions that were not handled elsewhere. Displays the exceptions
 * in an {@link ErrorView}. Do not directly instantiate this class, instead
 * call {@link #registerHandler()} to setup this handler.
 * <br/><br/>
 * Note: The constructor for this class <strong>must</strong> be public and take
 * zero arguments. AWT will use reflection to construct the class specified by
 * the AWT exception handler property. See the {@link #registerHandler()} for
 * details.
 *
 * @author jak2
 */
class CakehatUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
    /**
     * Registers handlers such that when an unhandled exception occurs an
     * {@link ErrorView} is displayed so that the issue can be reported.
     */
    static void registerHandler()
    {
        //Setup handler with normal threads
        Thread.setDefaultUncaughtExceptionHandler(new CakehatUncaughtExceptionHandler());

        /* Set up handler with AWT
         *
         * This is necessary due to a bug in Java that is present in Java SE 6
         * and earlier, which hopefully will be resolved in future versions.
         * When the EventDispatch thread is blocking, which is what occurs when
         * a modal dialog is on screen, exceptions are not properly handled. To
         * get around this Sun introduced a hacky work-around which involves
         * setting a property and having its value be the name of a class which
         * satisfies certain properties. The details have been copied from the
         * javadoc method comment of java.awt.EventDispatchThread's
         * handleException(Throwable) which is a private method in a package
         * private class:
         *
         * Handles an exception thrown in the event-dispatch thread.
         *
         * If the system property "sun.awt.exception.handler" is defined, then
         * when this method is invoked it will attempt to do the following:
         *
         *  - Load the class named by the value of that property, using the
         *    current thread's context class loader,
         *  - Instantiate that class using its zero-argument constructor,
         *    Find the resulting handler object's public void handle
         *    method, which should take a single argument of type Throwable, and
         *    Invoke the handler's handle method, passing it the
         *    thrown argument that was passed to this method.
         *
         * [Non-relevant portion elided]
         *
         * Note: This method is a temporary hack to work around the absence of a
         * real API that provides the ability to replace the event-dispatch
         * thread.  The magic "sun.awt.exception.handler" property will be
         * removed in a future release.
         */
        System.setProperty("sun.awt.exception.handler",
                CakehatUncaughtExceptionHandler.class.getName());
    }

    /**
     * Called when a normal thread encounters an unhandled exception.
     *
     * @param thread
     * @param throwable
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable)
    {
        this.displayException(throwable);
    }

    /**
     * Called when an exception is encountered on a modal dialog.
     * 
     * @param throwable
     */
    public void handle(Throwable throwable)
    {
        this.displayException(throwable);
    }

    /**
     * Display the exception to the user.
     *
     * @param throwable
     */
    private void displayException(Throwable throwable)
    {
        //If the user cannot see graphical elements
        //This situation can arise when the user has run 'su' while checking off
        //a student for a lab
        if(GraphicsEnvironment.isHeadless())
        {
            System.err.println("Unhandled exception:");
            throwable.printStackTrace();
        }
        else
        {
            new ErrorView(throwable, "An unhandled exception has occurred. " +
                "Please report this.");
        }
    }
}