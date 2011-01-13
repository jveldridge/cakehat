package gradesystem.config;

/**
 * Representation of the following xml:
 * <SUBMIT-OPTIONS SUBMIT="TRUE" NOTIFY="TRUE" EMAIL-GRD="FALSE" PRINT-GRD="TRUE"/>
 *
 * This code is used by the SubmitDialog for determining the default state of
 * the checkboxes for:
 *  - Submit grades
 *  - Notify students
 *  - Email GRD files
 *  - Print GRD files
 *
 * @author jak2
 */
public class SubmitOptions
{
    private final boolean _submit, _notify, _emailGrd, _printGrd;

    /**
     * Default with values:
     *
     * SUBMIT    - TRUE
     * NOTIFY    - TRUE
     * EMAIL-GRD - TRUE
     * PRINT-GRD - FALSE
     */
    SubmitOptions()
    {
        this(true, true, true, false);
    }

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