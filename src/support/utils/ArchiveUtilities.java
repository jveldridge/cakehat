package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Set;
import javax.activation.DataSource;
import org.apache.commons.compress.archivers.ArchiveEntry;

/**
 * Utility methods for interacting with archive files. Supported archive formats are specified by
 * {@link ArchiveFormat}.
 * 
 * @author jak2
 */
public interface ArchiveUtilities
{
    public static enum ArchiveFormat
    {   
        ZIP("application/zip", "zip", ImmutableSet.of("zip")),
        TAR("application/tar", "tar", ImmutableSet.of("tar")),
        TAR_GZ("application/x-tar", "tar.gz", ImmutableSet.of("tar.gz", "tgz"));
        
        private final String _mimeType;
        private final String _defaultFileExtension;
        private final Set<String> _allFileExtensions;
        
        private ArchiveFormat(String mimeType, String defaultFileExtension, Set<String> allFileExtensions)
        {
            _mimeType = mimeType;
            _defaultFileExtension = defaultFileExtension;
            _allFileExtensions = allFileExtensions;
        }
        
        String getMimeType()
        {
            return _mimeType;
        }
        
        String getDefaultFileExtension()
        {
            return _defaultFileExtension;
        }
        
        Set<String> getAllFileExtensions()
        {
            return _allFileExtensions;
        }
        
        boolean matchesFormat(File file)
        {
            String normalizedName = file.getName().toLowerCase();
            
            boolean matches = false;
            for(String fileExtension : _allFileExtensions)
            {
                if(normalizedName.endsWith(fileExtension))
                {
                    matches = true;
                    break;
                }
            }
            
            return matches;
        }
    }
    
    /**
     * Returns a {@link FileFilter} which only accept files which are of a supported {@link ArchiveFormat}. Acceptance
     * is based on on file extensions.
     * 
     * @return
     */
    public FileFilter getArchiveFormatsFileFilter();
    
    /**
     * Returns a listing of the files and directories in the archive file without extracting the file. Determines the
     * archive format based on file extension.
     *
     * @param archive
     *
     * @return the entries in the archive
     */
    public Set<ArchiveEntry> getArchiveContents(File archive) throws IOException;

    /**
     * Extracts an archive file. Each file and directory to be created will be checked against the {@code filter}. Only
     * if it accepts will the file be unarchived. Returns a set of the files and directory created.  Determines the
     * archive format based on file extension.
     *
     * @param archive
     * @param dstDir
     * @param filter
     * @param groupOwner
     * @return
     * @throws ArchiveExtractionException
     */
    public Set<File> extractArchive(File archive, File dstDir, FileFilter filter, String groupOwner)
            throws ArchiveExtractionException;
    
    /**
     * Creates a read-only {@link DataSource} which is backed by an archived version of {@code src}. The format of
     * the archive is specified by {@code format}. Only the files and directories contained {@code src} that are
     * accepted by the {@code filter} are included in the archive.
     * 
     * @param format
     * @param src
     * @param filter
     * @return
     * @throws IOException 
     */
    public DataSource createArchiveDataSource(ArchiveFormat format, File src, FileFilter filter) throws IOException;
}