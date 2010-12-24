package gradesystem.config;

import gradesystem.Allocator;
import gradesystem.views.shared.ErrorView;
import java.io.IOException;

/**
 * Allows for a totally custom handin specification where an arbitratry command
 * will be sent to a hidden bash console.
 *
 * @author jak2
 */
class CustomHandin extends CodeHandin
{
    private static final String
    SPECIFY_SCRIPT = "specify-script",
    SPECIFY_USER_SCRIPT = "specify-user-script", 
    COMMAND="command";

    //this must use regex syntax to work correctly in String.replaceAll(...)
    private static final String STUDENT_LOGIN_TAG = "\\^student_login\\^";

    //Mode
    private static final LanguageSpecification.Mode
    SPECIFY_SCRIPT_MODE = new LanguageSpecification.Mode(SPECIFY_SCRIPT,
                              new LanguageSpecification.Property(COMMAND, true)),
    //In this mode any occurence of ^student_login^ in the command will be replaced
    //by the applicable student login that run or tester is being called for
    SPECIFY_USER_SCRIPT_MODE = new LanguageSpecification.Mode(SPECIFY_USER_SCRIPT,
                                   new LanguageSpecification.Property(COMMAND, true));

    //Language specification
    public static final LanguageSpecification SPECIFICATION =
    new LanguageSpecification("custom",
                             new LanguageSpecification.Mode[]{ SPECIFY_SCRIPT_MODE, SPECIFY_USER_SCRIPT_MODE },
                             new LanguageSpecification.Mode[]{ SPECIFY_SCRIPT_MODE },
                             new LanguageSpecification.Mode[]{ SPECIFY_SCRIPT_MODE, SPECIFY_USER_SCRIPT_MODE });

    CustomHandin(Assignment asgn, String name, int points)
    {
        super(asgn,name,points);
    }

    @Override
    protected String[] getSourceFileTypes()
    {
        return new String[0];
    }

    @Override
    public boolean hasOpen()
    {
        return false;
    }

    @Override
    public boolean hasPrint()
    {
        return false;
    }

    @Override
    public void run(String studentLogin)
    {
        this.untar(studentLogin);

        if(_runMode.equalsIgnoreCase(SPECIFY_SCRIPT))
        {
            try
            {
                Allocator.getExternalProcessesUtilities().executeAsynchronously(this.getRunProperty(COMMAND));
            }
            catch(IOException e)
            {
                new ErrorView(e);
            }
        }
        else if(_runMode.equalsIgnoreCase(SPECIFY_USER_SCRIPT))
        {
            try
            {
                Allocator.getExternalProcessesUtilities().executeAsynchronously(this.getRunProperty(COMMAND).replaceAll(STUDENT_LOGIN_TAG, studentLogin));
            }
            catch(IOException e)
            {
                new ErrorView(e);
            }
        }
    }

    @Override
    public void runDemo()
    {
        if(_demoMode.equalsIgnoreCase(SPECIFY_SCRIPT))
        {
            try
            {
                Allocator.getExternalProcessesUtilities().executeAsynchronously(this.getDemoProperty(COMMAND));
            }
            catch(IOException e)
            {
                new ErrorView(e);
            }
        }
    }

    @Override
    public void runTester(String studentLogin)
    {
        this.untar(studentLogin);

        if(_testerMode.equalsIgnoreCase(SPECIFY_SCRIPT))
        {
            try
            {
                Allocator.getExternalProcessesUtilities().executeAsynchronously(this.getTesterProperty(COMMAND));
            }
            catch(IOException e)
            {
                new ErrorView(e);
            }
        }
        else if(_testerMode.equalsIgnoreCase(SPECIFY_USER_SCRIPT))
        {
            try
            {
                Allocator.getExternalProcessesUtilities().executeAsynchronously(this.getTesterProperty(COMMAND).replaceAll(STUDENT_LOGIN_TAG, studentLogin));
            }
            catch(IOException e)
            {
                new ErrorView(e);
            }
        }
    }
}