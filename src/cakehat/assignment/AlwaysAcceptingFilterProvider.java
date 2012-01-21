package cakehat.assignment;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import org.apache.commons.compress.archivers.ArchiveEntry;
import support.utils.AlwaysAcceptingFileFilter;

/**
 * A FilterProvider that generates a FileFilter which always accepts. Intended to be used when a Part does not specify
 * any restrictions on what files belong to the Part.
 *
 * @author jak2
 */
class AlwaysAcceptingFilterProvider implements FilterProvider
{
    @Override
    public FileFilter getFileFilter(File unarchivedDir)
    {
        return new AlwaysAcceptingFileFilter();
    }

    @Override
    public boolean areFilteredFilesPresent(Collection<ArchiveEntry> archiveContents, StringBuilder builder)
    {
        return true;
    }
}