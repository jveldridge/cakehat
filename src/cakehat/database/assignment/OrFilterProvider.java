package cakehat.database.assignment;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.compress.archivers.ArchiveEntry;
import support.utils.OrFileFilter;

/**
 * Supplies a FileFilter that logically ORs together all of the FileFilters created by the FilterProviders passed in to
 * the constructor.
 *
 * @author jak2
 */
class OrFilterProvider implements FilterProvider
{
    private final Iterable<FilterProvider> _providers;

    public OrFilterProvider(Iterable<FilterProvider> providers)
    {
        _providers = ImmutableSet.copyOf(providers);
    }

    @Override
    public FileFilter getFileFilter(File unarchivedDir)
    {
        // Get all of the filters
        ArrayList<FileFilter> filters = new ArrayList<FileFilter>();
        for(FilterProvider provider : _providers)
        {
            filters.add(provider.getFileFilter(unarchivedDir));
        }
        
        return new OrFileFilter(filters);
    }

    @Override
    public boolean areFilteredFilesPresent(Collection<ArchiveEntry> archiveContents, StringBuilder builder)
    {
        boolean present = true;

        for(FilterProvider provider : _providers)
        {
            present &= provider.areFilteredFilesPresent(archiveContents, builder);
        }

        return present;
    }
}