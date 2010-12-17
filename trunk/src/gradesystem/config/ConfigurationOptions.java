package gradesystem.config;

import java.util.Collection;
import java.util.Vector;

/**
 * Provides information on what are valid configuration options for each
 * portion of the configuration.
 *
 * TODO: Eventually hook this up to the configuration builder code when it is
 *       written.
 *
 * @author jak2
 */
public class ConfigurationOptions
{
    private static Collection<LanguageSpecification> SPECIFICATIONS = new Vector<LanguageSpecification>();
    static void registerLanguageSpecification(LanguageSpecification spec)
    {
        SPECIFICATIONS.add(spec);
    }

    public Collection<LanguageSpecification> getLanguageSpecifications()
    {
        return SPECIFICATIONS;
    }
}