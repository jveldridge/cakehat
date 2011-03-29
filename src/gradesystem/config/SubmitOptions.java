package gradesystem.config;

/**
 * Representation of the following XML in the configuration file:
 * <pre>
 * {@code
 * <SUBMIT-OPTIONS SUBMIT="TRUE" NOTIFY="TRUE" EMAIL-GRD="FALSE" PRINT-GRD="TRUE"/>
 * }
 * </pre>
 * This code is used by the {@link gradesystem.views.frontend.SubmitDialog} for
 * determining the default state of the checkboxes for:
 * <ul>
 * <li>Submit grades</li>
 * <li>Notify students</li>
 * <li>Email GRD files</li>
 * <li>Print GRD files</li>
 * </ul>
 *
 * @author jak2
 */
public class SubmitOptions
{
    /**
     * The default value for the submit check box if not specified in the
     * configuration file.
     */
    public static final boolean SUBMIT_DEFAULT = true;

    /**
     * The default value for the notify check box if not specified in the
     * configuration file.
     */
    public static final boolean NOTIFY_DEFAULT = true;

    /**
     * The default value for the email check box if not specified in the
     * configuration file.
     */
    public static final boolean EMAIL_GRD_DEFAULT = true;

    /**
     * The default value for the print check box if not specified in the
     * configuration file.
     */
    public static final boolean PRINT_GRD_DEFAULT = false;

    
    private final boolean _submit, _notify, _emailGrd, _printGrd;

    SubmitOptions(boolean submit, boolean notify, boolean emailGrd, boolean printGrd)
    {
        _submit= submit;
        _notify = notify;
        _emailGrd = emailGrd;
        _printGrd = printGrd;
    }

    public boolean isSubmitDefaultEnabled()
    {
        return _submit;
    }

    public boolean isNotifyDefaultEnabled()
    {
        return _notify;
    }

    public boolean isEmailGrdDefaultEnabled()
    {
        return _emailGrd;
    }

    public boolean isPrintGrdDefaultEnabled()
    {
        return _printGrd;
    }
}