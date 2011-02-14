package gradesystem;

import org.jdesktop.application.SingleFrameApplication;

/**
 * The old main class of the application. Still needed because all of the GUI
 * builder classes reference this class and there appears to be no easy way
 * to remove that dependency.
 */
@Deprecated
public class GradeSystemApp extends SingleFrameApplication
{
    protected void startup()
    {
        throw new UnsupportedOperationException("This method should never be" +
                "called. If it is being called that means that making the new" +
                "main class be a non-GUIBuilder class has failed in some way.");
    }
}