package cakehat.printing;

import cakehat.config.TA;
import cakehat.newdatabase.Group;
import cakehat.database.Student;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private List<File> _files = new ArrayList<File>();
    private TA _ta;
    private String _displayString;

    /**
     * A request of text files.
     *
     * @param file
     * @param ta
     * @param group
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    @Deprecated
    public PrintRequest(File file, TA ta, cakehat.database.Group group) throws FileNotFoundException
    {
        this(Arrays.asList(file), ta, group);
    }

    /**
     * A request of text files.
     *
     * @param files
     * @param ta
     * @param group
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    @Deprecated
    public PrintRequest(Iterable<File> files, TA ta, cakehat.database.Group group) throws FileNotFoundException
    {
        this(files);
        _ta = ta;
        if (group.size() == 1) {
            _displayString = "student: " + group.getName();
        }
        else {
            _displayString = "group: " + group.getName();
        }
    }
    
    /**
     * A request of text files.
     *
     * @param file
     * @param ta
     * @param group
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(File file, TA ta, Group group) throws FileNotFoundException
    {
        this(Arrays.asList(file), ta, group);
    }

    /**
     * A request of text files.
     *
     * @param files
     * @param ta
     * @param group
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(Iterable<File> files, TA ta, Group group) throws FileNotFoundException
    {
        this(files);
        _ta = ta;
        if(group.isGroupOfOne())
        {
            _displayString = "student: " + group.getName();
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
        this(Arrays.asList(file), ta, student);
    }

    /**
     * A request of text files.
     *
     * @param files
     * @param taLogin
     * @param student
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public PrintRequest(Iterable<File> files, TA ta, Student student) throws FileNotFoundException
    {
        this(files);
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
        this.addFiles(files);
    }

    /**
     * Adds a file to the request.
     *
     * @param file
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public void addFile(File file) throws FileNotFoundException
    {
        if(!file.exists())
        {
            throw new FileNotFoundException(file.getAbsolutePath() + " cannot be found");
        }
        else
        {
            _files.add(file);
        }
    }

    /**
     * Adds files to the request.
     *
     * @param files
     * @throws FileNotFoundException thrown if a file is passed in that does not exist
     */
    public void addFiles(Iterable<File> files) throws FileNotFoundException
    {
        for(File file : files)
        {
            this.addFile(file);
        }
    }

    public TA getTA()
    {
        return _ta;
    }

    public String getHeaderString()
    {
        return _displayString;
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