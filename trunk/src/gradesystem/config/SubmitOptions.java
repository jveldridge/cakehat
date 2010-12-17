package gradesystem.config;

/**
 * Representation of the following xml:
 * <SUBMIT-OPTIONS SUBMIT="TRUE" NOTIFY="TRUE" EMAIL-GRD="FALSE" PRINT-GRD="TRUE"/>
 *
 * Default values:
 * SUBMIT    - TRUE
 * NOTIFY    - TRUE
 * EMAIL-GRD - TRUE
 * PRINT-GRD - FALSE
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

    private SubmitOptions(boolean submit, boolean notify, boolean emailGrd, boolean printGrd)
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

    public static class Builder implements gradesystem.components.Builder<SubmitOptions>
    {
        //Defaults
        private boolean _submit = true;
        private boolean _notify = true;
        private boolean _emailGrd = true;
        private boolean _printGrd = false;

        public SubmitOptions build()
        {
            return new SubmitOptions(_submit, _notify, _emailGrd, _printGrd);
        }

        public Builder setSubmit(boolean submit)
        {
            _submit = submit;
            return this;
        }

        public Builder setNotify(boolean notify)
        {
            _notify = notify;
            return this;
        }

        public Builder setEmailGrd(boolean emailGrd)
        {
            _emailGrd = emailGrd;
            return this;
        }

        public Builder setPrintGrd(boolean printGrd)
        {
            _printGrd = printGrd;
            return this;
        }
    }
}