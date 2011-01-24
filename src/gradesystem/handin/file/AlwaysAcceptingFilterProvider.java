package gradesystem.handin.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * A FilterProvider that generates a FileFilter which always accepts.
 * Intended to be used when a DistributablePart does not specify any
 * restrictions on what files belong to the DistributablePart.
 *
 * @author jak2
 */
public class AlwaysAcceptingFilterProvider implements FilterProvider
{
    public FileFilter getFileFilter(File unarchivedDir)
    {
        return new AlwaysAcceptingFileFilter();
    }

    public boolean areFilteredFilesPresent(Collection<ArchiveEntry> archiveContents, StringBuffer buffer)
    {
        return true;
    }
}