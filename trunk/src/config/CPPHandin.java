package config;

/**
 *
 * @author <author-here>
 */
class CPPHandin extends CodeHandin
{
    //TODO: Fill in my properties
    public static final LanguageSpecification SPECIFICATION =
            new LanguageSpecification("C++", null, null, null);

    CPPHandin(Assignment asgn, String name, int points)
    {
        super(asgn,name,points);
    }

    @Override
    protected String[] getSourceFileTypes()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void openCode(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void run(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasDemo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void runDemo()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasTester()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void runTester(String studentLogin)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}