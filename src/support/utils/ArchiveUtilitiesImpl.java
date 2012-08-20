package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import support.utils.posix.FilePermission;

public class ArchiveUtilitiesImpl implements ArchiveUtilities
{
    private final FileSystemUtilities _fileSystemUtils;
    
    public ArchiveUtilitiesImpl(FileSystemUtilities fileSystemUtilities)
    {
        _fileSystemUtils = fileSystemUtilities;
    }
    
    @Override
    public FileFilter getSupportedFormatsFilter()
    {
        return new FileExtensionFilter("zip", "tar", "tgz", "tar.gz");
    }

    /**
     * Gets the appropriate stream depending the file extension of {@code archive}.
     * <br><br>
     * Supported extensions: zip, tar, tgz, tar.gz. Unsupported extensions will result in an exception being throw.
     *
     * @param archive
     * @return
     */
    private ArchiveInputStream getArchiveInputStream(File archive) throws IOException
    {
        ArchiveInputStream archiveStream;
        
        String lowerCaseSrcFile = archive.getAbsolutePath().toLowerCase();
        if(lowerCaseSrcFile.endsWith(".zip"))
        {
            archiveStream = new ZipArchiveInputStream(new FileInputStream(archive));
        }
        else if(lowerCaseSrcFile.endsWith(".tar"))
        {
            archiveStream = new TarArchiveInputStream(new FileInputStream(archive));
        }
        else if(lowerCaseSrcFile.endsWith(".tgz") || lowerCaseSrcFile.endsWith(".tar.gz"))
        {
            archiveStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(archive)));
        }
        else
        {
            throw new IOException("Unsupported file extension. Supported extensions are: zip, tar, tgz, & tar.gz");
        }

        return archiveStream;
    }

    @Override
    public Set<ArchiveEntry> getArchiveContents(File archive) throws IOException
    {
        ImmutableSet.Builder<ArchiveEntry> contents = ImmutableSet.builder();

        ArchiveInputStream in = getArchiveInputStream(archive);
        while(true)
        {
            ArchiveEntry entry = in.getNextEntry();
            
            if(entry == null)
            {
                break;
            }

            contents.add(entry);
        }
        in.close();

        return contents.build();
    }

    @Override
    public Set<File> extractArchive(File archive, File dstDir, FileFilter filter, String groupOwner)
            throws ArchiveExtractionException
    {
        ImmutableSet.Builder<File> filesCreated = ImmutableSet.builder();
        
        try
        {
            ArchiveInputStream in = getArchiveInputStream(archive);
            while(true)
            {
                ArchiveEntry entry = in.getNextEntry();

                if(entry == null)
                {
                    break;
                }

                File file = new File(dstDir, entry.getName());
                if(filter.accept(file))
                {
                    if(entry.isDirectory())
                    {
                        //Create directory (and parent directories) as necessary
                        filesCreated.addAll(_fileSystemUtils.makeDirectory(file, groupOwner));
                    }
                    else
                    {
                        //Create parent directories of file if necessary
                        filesCreated.addAll(_fileSystemUtils.makeDirectory(file.getParentFile(), groupOwner));

                        //Create file
                        OutputStream out = new FileOutputStream(file);
                        IOUtils.copy(in, out);
                        filesCreated.add(file);
                        out.close();
                        
                        //Set permissions
                        _fileSystemUtils.changeGroup(file, groupOwner, false);
                        Set<FilePermission> permissions = new HashSet<FilePermission>();
                        permissions.add(FilePermission.OWNER_READ);
                        permissions.add(FilePermission.OWNER_WRITE);
                        permissions.add(FilePermission.GROUP_READ);
                        permissions.add(FilePermission.GROUP_WRITE);
                        if(file.canExecute())
                        {
                            permissions.add(FilePermission.OWNER_EXECUTE);
                            permissions.add(FilePermission.GROUP_EXECUTE);
                        }
                        _fileSystemUtils.chmod(file, false, permissions);
                    }
                }
            }
            in.close();
        }
        catch(IOException e)
        {
            try
            {
                _fileSystemUtils.deleteFiles(filesCreated.build());
                
                throw new ArchiveExtractionException(false, ImmutableSet.<File>of(),
                        "Unable to extract archive. The files and/or directories created in the extraction have " +
                        "been deleted.\n" +
                        "Archive: " + archive.getAbsolutePath() + "\n" +
                        "Destination Directory: " + dstDir.getAbsolutePath() + "\n" +
                        "Group Owner: " + groupOwner, e);
            }
            catch(FileDeletingException ex)
            {
                throw new ArchiveExtractionException(false, ex.getFilesNotDeleted(),
                    "Unable to extract archive. Unable to delete partially extracted files and/or directories.\n" +
                    "Archive: " + archive.getAbsolutePath() + "\n" +
                    "Destination Directory: " + dstDir.getAbsolutePath() + "\n" +
                    "Group Owner: " + groupOwner, e);
            }
        }
        
        return filesCreated.build();
    }
}