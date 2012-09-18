package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileFilter;

/**
 * A FileFilter which requires that all FileFilters it is composed of accept the File.
 *
 * @author jak2
 */
public class AndFileFilter implements FileFilter
{
    private final Iterable<FileFilter> _filters;

    public AndFileFilter(FileFilter... filters)
    {
        _filters = ImmutableSet.copyOf(filters);
    }

    public AndFileFilter(Iterable<FileFilter> filters)
    {
        _filters = ImmutableSet.copyOf(filters);
    }

    @Override
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