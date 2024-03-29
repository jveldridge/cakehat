package cakehat.assignment;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * FilterProviders generate FileFilters which respect to where the handin was unarchived into. They can determine
 * whether the files or directories they accept are present.
 *
 * @author jak2
 */
interface FilterProvider
{
    /**
     * Provides a FileFilter which takes into account where the handin has been unarchived into.
     *
     * @param unarchivedDir the directory the handin has been unarchived into
     * @return
     */
    public FileFilter getFileFilter(File unarchivedDir);

     /**
     * Determines whether all of the files or directories this filter is supposed to include are actually present. If
     * they are not, {@code false} is returned and what is not present is added to the {@code builder}.
     *
     * @param archiveContents
     * @param builder
     * @return
     */
    public boolean areFilteredFilesPresent(Collection<ArchiveEntry> archiveContents, StringBuilder builder);
}