package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileFilter;

/**
 * A FileFilter which accepts files with certain extensions. Hidden files are ignored. File extensions should just be
 * the extension, ex. "java" not ".java"
 *
 * @author jak2
 */
public class FileExtensionFilter implements FileFilter
{
    private final Iterable<String> _extensions;

    public FileExtensionFilter(String... extensions)
    {
        _extensions = ImmutableSet.copyOf(extensions);
    }

    public FileExtensionFilter(Iterable<String> extensions)
    {
        _extensions = ImmutableSet.copyOf(extensions);
    }

    @Override
    public boolean accept(File file)
    {
        if(!file.exists() || !file.isFile() || file.isHidden())
        {
            return false;
        }

        String name = file.getName().toLowerCase();
        for(String acceptableExtension : _extensions)
        {
            if(name.endsWith("." + acceptableExtension.toLowerCase()))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString()
    {
        String description = "FileExtensionFilter";

        for(String extension : _extensions)
        {
            description += " " + extension;
        }

        return description;
    }
}