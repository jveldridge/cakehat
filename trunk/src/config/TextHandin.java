package config;

/**
 * Support for opening text files. If no RUN mode is specified then files with
 * extension 'txt' will be opened.
 *
 * @author jak2
 */
public class TextHandin extends CodeHandin
{
    //Configuration tags
    private static final String SPECIFY_TYPES="specify-types", FILE_TYPES="file-types";

    //Run mode
    private static final LanguageSpecification.Mode
    RUN_SPECIFY_TYPES_MODE = new LanguageSpecification.Mode(SPECIFY_TYPES,
                            new LanguageSpecification.Property(FILE_TYPES, true));

    //The specification of how this handin can be configured
    public static final LanguageSpecification SPECIFICATION =
    new LanguageSpecification("text",
                              new LanguageSpecification.Mode[]{ RUN_SPECIFY_TYPES_MODE },
                              null,
                              null);


    TextHandin(Assignment asgn, String name, int points)
    {
        super(asgn,name,points);
    }

    private static final String[] _defaultFileTypes = { "txt" };

    @Override
    protected String[] getSourceFileTypes()
    {
        if(_runMode != null && _runMode.equals(SPECIFY_TYPES))
        {
            return this.getRunProperty(FILE_TYPES).split(",");
        }
        else
        {
            return _defaultFileTypes;
        }
    }

    @Override
    public boolean hasRun()
    {
        return false;
    }

    @Override
    public void run(String studentLogin) { }

    @Override
    public boolean hasDemo()
    {
        return false;
    }

    @Override
    public void runDemo() { }

    @Override
    public boolean hasTester()
    {
        return false;
    }

    @Override
    public void runTester(String studentLogin) { }
}