package cakehat.database.assignment;

import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import support.utils.OrFileFilter;
import support.utils.FileExtensionFilter;
import support.utils.NoFileExtensionFilter;

/**
 * Functionality used commonly by actions. This class exists to prevent code duplication.
 *
 * @author jak2
 */
class ActionUtilities
{
    /**
     * Creates a FileFilter that parses a string containing a comma-separated list of file extensions. To represent a
     * file that has no file extension, an underscore {@code _} is to be used.
     * <br/><br/>
     * Examples:
     * <br/>
     * {@code java}
     * <br/>
     * {@code c, cpp, h, _}
     *
     * @param extensions
     * @return file filter
     */
    public static FileFilter parseFileExtensions(String extensions)
    {
        //To allow for spaces not being used, remove spaces if they are present
        extensions = extensions.replace(" ", "");

        //Build a set of the extensions supplied
        HashSet<String> extensionSet = new HashSet<String>(Arrays.asList(extensions.split(",")));

        //If underscore is used, accept files without file extensions
        FileFilter filter;
        if(extensionSet.contains("_"))
        {
            extensionSet.remove("_");
            filter = new OrFileFilter(new NoFileExtensionFilter(), new FileExtensionFilter(extensionSet));
        }
        else
        {
            filter = new FileExtensionFilter(extensionSet);
        }
        
        return filter;
    }
}