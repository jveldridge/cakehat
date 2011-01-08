package gradesystem.handin;

import gradesystem.handin.file.AndFileFilter;
import java.io.FileFilter;
import java.util.HashSet;
import utils.FileExtensionFilter;
import utils.NoFileExtensionFilter;

/**
 * Functionality used commonly by actions. This class exists to prevent code
 * duplication.
 *
 * @author jak2
 */
class ActionUtilities
{

    /**
     * Creates a FileFilter that parses a string containing a comma-separated
     * list of file extensions. To represent a file that has no file extension,
     * an underscore is to be used.
     * <br/><br/>
     * Examples:
     * <br/>
     * <code>java</code>
     * <br/>
     * <code>c, cpp, h, _</code>
     *
     *
     * @param extensions
     * @return
     */
    public static FileFilter parseFileExtensions(String extensions)
    {
        //To allow for spaces not being used, remove spaces if they are present
        extensions = extensions.replace(" ", "");

        //Determine if there is an underscore in the extensions
        HashSet<String> extensionSet = new HashSet<String>();
        String[] extensionArray = extensions.split(",");
        for(String extension : extensionArray)
        {
            extensionSet.add(extension);
        }

        FileFilter filter;
        if(extensionSet.contains("_"))
        {
            extensionSet.remove("_");
            filter = new AndFileFilter(new NoFileExtensionFilter(),
                    new FileExtensionFilter(extensionSet));
        }
        else
        {
            filter = new FileExtensionFilter(extensionSet);
        }
        
        return filter;
    }
}