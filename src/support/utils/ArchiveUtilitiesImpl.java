package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.activation.DataSource;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
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
    public FileFilter getArchiveFormatsFileFilter()
    {
        ImmutableSet.Builder<String> fileExtensions = ImmutableSet.builder();
        for(ArchiveFormat format : ArchiveFormat.values())
        {
            fileExtensions.addAll(format.getAllFileExtensions());
        }
        
        return new FileExtensionFilter(fileExtensions.build());
    }
    
    /******************************************************************************************************************\
    |*                                                   Extraction                                                   *|
    \******************************************************************************************************************/

    @Override
    public Set<ArchiveEntry> getArchiveContents(File archive) throws IOException
    {
        ImmutableSet.Builder<ArchiveEntry> contents = ImmutableSet.builder();
        
        ArchiveInputStream in = getArchiveInputStream(archive);
        for(ArchiveEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry())
        {
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
            for(ArchiveEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry())
            {
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

    private ArchiveInputStream getArchiveInputStream(File archive) throws IOException
    {
        ArchiveInputStream archiveStream;
        if(ArchiveFormat.ZIP.matchesFormat(archive))
        {
            archiveStream = new ZipArchiveInputStream(new FileInputStream(archive));
        }
        else if(ArchiveFormat.TAR.matchesFormat(archive))
        {
            archiveStream = new TarArchiveInputStream(new FileInputStream(archive));
        }
        else if(ArchiveFormat.TAR_GZ.matchesFormat(archive))
        {
            archiveStream = new TarArchiveInputStream(new GZIPInputStream(new FileInputStream(archive)));
        }
        else
        {
            throw new IOException("Unsupported file extension. Archive: " + archive.getAbsolutePath());
        }

        return archiveStream;
    }
    
    /******************************************************************************************************************\
    |*                                                  Creation                                                      *|
    \******************************************************************************************************************/
    
    @Override
    public DataSource createArchiveDataSource(String archiveName, ArchiveFormat format, File src, FileFilter filter)
            throws IOException
    {
        String name = archiveName + "." + format.getDefaultFileExtension();
        
        return new ByteArrayDataSource(name, format.getMimeType(),
                createArchiveAsByteArray(archiveName, format, src, filter));
    }
    
    private byte[] createArchiveAsByteArray(String rootEntryName, ArchiveFormat format, File src, FileFilter filter)
            throws IOException
    {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        
        ArchiveOutputStream archiveStream = getArchiveOutputStream(format, byteStream);
        try
        {
            for(File file : _fileSystemUtils.getFiles(src, filter))
            {
                writeToArchiveOutputStream(format, archiveStream, file, src, rootEntryName);
            }
        }
        finally
        {
            archiveStream.close();
        }
        
        return byteStream.toByteArray();
    }
    
    private ArchiveOutputStream getArchiveOutputStream(ArchiveFormat format, OutputStream backingStream)
            throws IOException
    {
        ArchiveOutputStream archiveStream;
        if(format == ArchiveFormat.ZIP)
        {
            archiveStream = new ZipArchiveOutputStream(backingStream);
        }
        else if(format == ArchiveFormat.TAR)
        {
            archiveStream = new TarArchiveOutputStream(backingStream);
        }
        else if(format == ArchiveFormat.TAR_GZ)
        {
            archiveStream = new TarArchiveOutputStream(new GZIPOutputStream(backingStream)); 
        }
        else
        {
            throw new IOException("Unsupported archive format: " + format);
        }
        
        return archiveStream;
    }
    
    private void writeToArchiveOutputStream(ArchiveFormat format, ArchiveOutputStream archiveStream, File src,
            File rootSrc, String rootEntryName) throws IOException
    {
        String entryName;
        if(src.equals(rootSrc))
        {
            entryName = rootEntryName;
        }
        else
        {
            entryName = rootEntryName + "/" + src.getAbsolutePath().replaceFirst(rootSrc.getAbsolutePath(), "");
        }
        
        ArchiveEntry entry;
        if(format == ArchiveFormat.ZIP)
        {
            entry = new ZipArchiveEntry(src, entryName);
        }
        else if(format == ArchiveFormat.TAR || format == ArchiveFormat.TAR_GZ)
        {
            entry = new TarArchiveEntry(src, entryName);
        }
        else
        {
            throw new IllegalArgumentException("Unsupported archive format: " + format);
        }
        
        //Write archive entry info
        archiveStream.putArchiveEntry(entry);
 
        //If a file - write file contents to the archive stream
        if(src.isFile())
        {
            FileInputStream in = new FileInputStream(src);
            IOUtils.copy(in, archiveStream);
            in.close();
        }
        
        //Close the archive entry
        archiveStream.closeArchiveEntry();
    }
}