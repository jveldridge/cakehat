package cakehat.printing;

import cakehat.database.TA;
import cakehat.database.Group;
import cakehat.database.Student;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Set;

/**
 * A request to be submitted to a Printer. Each file added to the request should be a plain text file.
 * <br/><br/>
 * Can have an associated TA and Student/Group that may or may not be used by the printer that the PrintRequest is
 * submitted to.
 *
 * @author jak2
 */
public class PrintRequest
{
    private final Set<File> _files;;
    private TA _ta;
    private String _displayString;
    private File _parentPathToHide;

    /**
     * A request of text files.
     *
     * @param files
     * @param ta
     * @param parentPathToHide
     * @param group
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(Iterable<File> files, File parentPathToHide, TA ta, Group group) throws FileNotFoundException
    {
        _files = this.validateFiles(files);
        
        _ta = ta;
        _parentPathToHide = parentPathToHide;
        if(group.isGroupOfOne())
        {
            _displayString = "student: " + group.getOnlyMember().getLogin();
        }
        else
        {
            _displayString = "group: " + group.getName();
        }
    }

    /**
     * A request of text files.
     *
     * @param file
     * @param ta
     * @param student
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(File file, TA ta, Student student) throws FileNotFoundException
    {
        _files = this.validateFiles(Arrays.asList(file));
        
        _ta = ta;
        _displayString = "student: " + student.getLogin();
    }
    
    /**
     * A request of text files.
     *
     * @param files
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(Iterable<File> files) throws FileNotFoundException
    {
        _files = this.validateFiles(files);
    }
    
    private Set<File> validateFiles(Iterable<File> files) throws FileNotFoundException
    {
        ImmutableSet.Builder<File> validatedFiles = ImmutableSet.builder();
        for(File file : files)
        {
            if(!file.exists())
            {
                throw new FileNotFoundException(file.getAbsolutePath() + " cannot be found");
            }
            else
            {
                validatedFiles.add(file);
            }
        }
        
        return validatedFiles.build();
    }

    TA getTA()
    {
        return _ta;
    }

    String getHeaderString()
    {
        return _displayString;
    }
    
    File getParentPathToHide()
    {
        return _parentPathToHide;
    }

    /**
     * The files in this PrintRequest.
     *
     * @return
     */
    Iterable<File> getFiles()
    {
        return _files;
    }
}