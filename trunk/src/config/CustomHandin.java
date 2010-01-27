package config;

import utils.BashConsole;

/**
 * Allows for a totally custom handin specification where an arbitratry command
 * will be sent to a hidden bash console.
 *
 * @author jak2
 */
class CustomHandin extends CodeHandin
{
    private static final String
    SPECIFY_SCRIPT = "specify-script", COMMAND="command";

    //Mode
    private static final LanguageSpecification.Mode
    SPECIFY_SCRIPT_MODE = new LanguageSpecification.Mode(SPECIFY_SCRIPT,
                            new LanguageSpecification.Property(COMMAND, true));

    //Language specification
    public static final LanguageSpecification SPECIFICATION =
    new LanguageSpecification("custom",
                             new LanguageSpecification.Mode[]{ SPECIFY_SCRIPT_MODE },
                             new LanguageSpecification.Mode[]{ SPECIFY_SCRIPT_MODE },
                             new LanguageSpecification.Mode[]{ SPECIFY_SCRIPT_MODE });

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
    public void run(String studentLogin)
    {
        BashConsole.writeThreaded(this.getRunProperty(COMMAND));
    }

    @Override
    public void runDemo()
    {
        BashConsole.writeThreaded(this.getDemoProperty(COMMAND));
    }

    @Override
    public void runTester(String studentLogin)
    {
        BashConsole.writeThreaded(this.getTesterProperty(COMMAND));
    }
}