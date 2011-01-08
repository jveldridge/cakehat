package utils;

import java.io.File;
import java.io.FileFilter;

/**
 * FileFilter that only accepts files that have no file extension.
 *
 * @author jak2
 */
public class NoFileExtensionFilter implements FileFilter
{
    public boolean accept(File file)
    {
        boolean accept = file.isFile() && !file.getName().contains(".");

        return accept;
    }
}