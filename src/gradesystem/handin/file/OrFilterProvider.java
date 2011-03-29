package gradesystem.handin.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * Supplies a FileFilter that logically ORs together all of the FileFilters
 * created by the FilterProviders passed in to the constructor.
 *
 * @author jak2
 */
public class OrFilterProvider implements FilterProvider
{
    private final Iterable<FilterProvider> _providers;

    public OrFilterProvider(FilterProvider... providers)
    {
        this(Arrays.asList(providers));
    }

    public OrFilterProvider(Iterable<FilterProvider> providers)
    {
        _providers = providers;
    }

    public FileFilter getFileFilter(File unarchivedDir)
    {
        // Get all of the filters
        final ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
        for(FilterProvider provider : _providers)
        {
            filters.add(provider.getFileFilter(unarchivedDir));
        }

        //Create a filter that logically ORs them all together
        FileFilter orFileFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                for(FileFilter filter : filters)
                {
                    if(filter.accept(file))
                    {
                        return true;
                    }
                }

                return false;
            }
        };

        return orFileFilter;
    }

    public boolean areFilteredFilesPresent(Collection<ArchiveEntry> archiveContents, StringBuffer buffer)
    {
        boolean present = true;

        for(FilterProvider provider : _providers)
        {
            present &= provider.areFilteredFilesPresent(archiveContents, buffer);
        }

        return present;
    }
}