package cakehat.config.handin.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * A FileFilter which requires that all FileFilters it is composed of accept
 * the File.
 *
 * @author jak2
 */
@Deprecated
public class AndFileFilter implements FileFilter
{
    private final Iterable<FileFilter> _filters;

    public AndFileFilter(FileFilter... filters)
    {
        this(Arrays.asList(filters));
    }

    public AndFileFilter(Iterable<FileFilter> filters)
    {
        _filters = filters;
    }

    public boolean accept(File file)
    {
        for(FileFilter filter : _filters)
        {
            if(!filter.accept(file))
            {
                return false;
            }
        }

        return true;
    }
}