package cakehat.logging;

import java.util.List;

/**
 *
 * @author jak2
 */
interface ThrowableReporter
{
    /**
     * Displays information about a throwable and returns {@code true}, or takes actions with no side effects and
     * returns {@code false}.
     * 
     * @param causalStack
     * @param msg
     * @param useGUI
     * @return whether the throwable was reported by this reporter
     */
    public boolean report(List<? extends Throwable> causalStack, String msg, boolean useGUI);
}