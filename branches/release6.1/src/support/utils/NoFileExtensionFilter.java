package support.utils;

import java.io.File;
import java.io.FileFilter;

/**
 * FileFilter that only accepts files that have no file extension.
 *
 * @author jak2
 */
public class NoFileExtensionFilter implements FileFilter
{
    @Override
    public boolean accept(File file)
    {
        return file.isFile() && !file.getName().contains(".");
    }
}