package testutils;

import java.util.Collection;

/**
 * Methods used to determine testing situation.
 *
 * @author jak2
 */
public class TestUtilities
{
    /**
     * Returns if JUnit exists in any of the stack frames for any thread.
     */
    public static boolean isJUnitRunning()
    {
        boolean junitRunning = false;

        Collection<StackTraceElement[]> traces = Thread.getAllStackTraces().values();
        for(StackTraceElement[] trace : traces)
        {
            if(isJUnitInStackTrace(trace))
            {
                junitRunning = true;
                break;
            }
        }

        return junitRunning;
    }

    private static boolean isJUnitInStackTrace(StackTraceElement[] stackTrace)
    {
        boolean junitRunning = false;

        for(StackTraceElement elem : stackTrace)
        {
            if (elem.getClassName().toLowerCase().contains("junit"))
            {
                junitRunning = true;
                break;
            }
        }

        return junitRunning;
    }

    /**
     * If JUnit is running, nothing will happen. If JUnit is not running, an
     * exception will be thrown.
     */
    public static void checkJUnitRunning()
    {
        if(!isJUnitRunning())
        {
            throw new IllegalStateException("This method may only be called from a JUnit test");
        }
    }
}