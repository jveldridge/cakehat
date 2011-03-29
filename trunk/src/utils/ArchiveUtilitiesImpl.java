package utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;

public class ArchiveUtilitiesImpl implements ArchiveUtilities
{
    public FileFilter getSupportedFormatsFilter()
    {
        return new FileExtensionFilter("zip", "tar", "tgz", "tar.gz");
    }

    /**
     * Gets the appropriate stream depending the file extension of
     * <code>archivePath</code>.
     * <br><br>
     * Supported extensions: zip, tar, tgz, tar.gz. Unsupported extensions will
     * result in an extension being throw.
     *
     * @param archivePath
     * @return
     */
    private ArchiveInputStream getArchiveInputStream(File archive) throws ArchiveException
    {
        //Determine appropriate input stream and compression format
        InputStream is;
        String format;

        String lowerCaseSrcFile = archive.getAbsolutePath().toLowerCase();

        if(lowerCaseSrcFile.endsWith(".zip"))
        {
            try
            {
                is = new FileInputStream(archive);
                format = "zip";
            }
            catch(IOException e)
            {
                throw new ArchiveException("Unable to create input stream for: " +
                        archive.getAbsolutePath(), e);
            }
        }
        else if(lowerCaseSrcFile.endsWith(".tar"))
        {
            try
            {
                is = new FileInputStream(archive);
                format = "tar";
            }
            catch(IOException e)
            {
                throw new ArchiveException("Unable to create input stream for: " +
                        archive.getAbsolutePath(), e);
            }
        }
        else if(lowerCaseSrcFile.endsWith(".tgz") || lowerCaseSrcFile.endsWith(".tar.gz"))
        {
            try
            {
                is = new GZIPInputStream(new FileInputStream(archive));
                format = "tar";
            }
            catch(IOException e)
            {
                throw new ArchiveException("Unable to create input stream for: " +
                        archive.getAbsolutePath(), e);
            }
        }
        else
        {
            throw new ArchiveException("Unsupported file extension. " +
                    "Supported extensions are: zip, tar, tgz, & tar.gz");
        }

        return new ArchiveStreamFactory().createArchiveInputStream(format, is);
    }

    public Collection<ArchiveEntry> getArchiveContents(File archive) throws ArchiveException
    {
        ArrayList<ArchiveEntry> contents = new ArrayList<ArchiveEntry>();

        ArchiveInputStream in = getArchiveInputStream(archive);
        while(true)
        {
            ArchiveEntry entry;
            try
            {
                entry = in.getNextEntry();
            }
            catch(IOException e)
            {
                throw new ArchiveException("Unable to retrieve next archive entry for: " +
                        archive.getAbsolutePath(), e);
            }

            if(entry == null)
            {
                break;
            }

            contents.add(entry);
        }
        try
        {
            in.close();
        }
        catch(IOException e)
        {
            throw new ArchiveException("Unable to close input stream for archive: " +
                    archive.getAbsolutePath(), e);
        }

        return contents;
    }

    public void extractArchive(File archive, File dstDir, FileFilter filter) throws ArchiveException
    {
        ArchiveInputStream in = getArchiveInputStream(archive);
        while(true)
        {
            ArchiveEntry entry;
            try
            {
                entry = in.getNextEntry();
            }
            catch(IOException e)
            {
                throw new ArchiveException("Unable to retrieve next archive entry for: " +
                        archive.getAbsolutePath(), e);
            }

            if(entry == null)
            {
                break;
            }

            File file = new File(dstDir, entry.getName());
            if(filter.accept(file))
            {
                if(entry.isDirectory())
                {
                    if(!file.mkdirs())
                    {
                        throw new ArchiveException("Unable to make directory: " +
                                file.getAbsolutePath());
                    }
                }
                else
                {
                    try
                    {
                        File parentDir = file.getParentFile();
                        if(!parentDir.exists())
                        {
                            if(!parentDir.mkdirs())
                            {
                                throw new ArchiveException("Unable to make directory: " +
                                        parentDir.getAbsolutePath() + "\n" +
                                        "For file: " + file.getAbsolutePath());
                            }
                        }

                        OutputStream out = new FileOutputStream(file);
                        IOUtils.copy(in, out);
                        out.close();
                    }
                    catch(IOException e)
                    {
                        throw new ArchiveException("Unable to unarchive file to: \n" +
                                file.getAbsolutePath() + "\n" +
                                "For archive: \n" + archive.getAbsolutePath(), e);
                    }
                }
            }
        }

        try
        {
            in.close();
        }
        catch(IOException e)
        {
            throw new ArchiveException("Unable to close input stream for archive: " +
                    archive.getAbsolutePath(), e);
        }
    }
}