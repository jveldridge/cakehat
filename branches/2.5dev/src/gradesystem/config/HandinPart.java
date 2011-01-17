package gradesystem.config;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.JOptionPane;
import gradesystem.Allocator;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
import gradesystem.services.ServicesException;
import org.apache.commons.compress.archivers.ArchiveException;
import gradesystem.views.shared.ErrorView;
import gradesystem.views.shared.TextViewerView;

/**
 *
 * @deprecated To be replaced with {@link Handin} and {@link DistributablePart}.
 * @author jak2
 */
public abstract class HandinPart extends Part
{
    private File _deductionsFile, _rubricFile;
    private TimeInformation _timeInfo;
    private Collection<File> _handins = null;
    private HashSet<String> _untarredStudents = new HashSet<String>();

    HandinPart(Assignment asgn, String name, int points)
    {
        super(asgn, name, Integer.MIN_VALUE, points);
    }

    // Time Info

    void setTimeInfo(TimeInformation info)
    {
        _timeInfo = info;
    }

    public TimeInformation getTimeInformation()
    {
        return _timeInfo;
    }

    //Deduction List

    void setDeductionList(String filePath)
    {
        _deductionsFile = new File(filePath);
    }

    public boolean hasDeductionList()
    {
        boolean exists = (_deductionsFile != null) && (_deductionsFile.exists());

        return exists;
    }

    public void viewDeductionList()
    {
        new TextViewerView(_deductionsFile, this.getAssignment().getName() + " Deductions List");
    }

    // Readme

    public boolean hasReadme(String studentLogin)
    {
        //Get path to the student's handin
        File handin = this.getHandin(studentLogin);

        if (handin == null) {
            new ErrorView(new Exception(), "The handin for the student: " + studentLogin + " could not be found. This could be because the file was moved or you don't have access to that file.");
            return false;
        }

        //Get contents of tar
        Collection<String> contents;
        try
        {
            contents = Allocator.getArchiveUtilities().getArchiveContents(handin);
        }
        catch (ArchiveException ex)
        {
            new ErrorView(ex, "Cannot determine if a readme exists - unable to get archive contents.");
            return false;
        }

        //For each entry (file and directory) in the tar
        for(String entry : contents)
        {
            //Extract the file name
            String filename = entry.substring(entry.lastIndexOf("/")+1);

            //See if the file name begins with README, regardless of case and doesn't end with ~
            if(filename.toUpperCase().startsWith("README") && !filename.endsWith("~"))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Views all readme files in the handin. Files that end in ~ will be ignored.
     * Files that have no file extension or end in txt  will be interpreted as
     * text files. Files that end in pdf will be interpreted as pdf files and
     * will be opened with KPDF. If a readme of another file extension is found,
     * tell the user.
     *
     * @param studentLogin
     */
    public void viewReadme(String studentLogin)
    {
        //Get all of the readmes
        Collection<File> readmes = this.getReadme(studentLogin);

        //For each readme
        for(File readme : readmes)
        {
            String name = readme.getName().toLowerCase();

            //Ignore ~ files if there are multiple READMEs
            if(name.endsWith("~") && readmes.size() > 1)
            {
                continue;
            }

            //If a text file
            if(!name.contains(".") || name.endsWith(".txt"))
            {
                new TextViewerView(readme, studentLogin +"'s Readme");
            }
            //If a PDF
            else if(readme.getAbsolutePath().toLowerCase().endsWith(".pdf"))
            {
                try
                {
                    Allocator.getExternalProcessesUtilities()
                            .executeAsynchronously("kpdf " + readme.getAbsolutePath());
                }
                catch(IOException e)
                {
                    new ErrorView(e, "Unable to open " + studentLogin + "'s readme: " + readme.getAbsolutePath());
                }
            }
            //Otherwise, we don't know what it is, tell the user
            else
            {
                JOptionPane.showConfirmDialog(null, "Encountered README that cannot be opened by cakehat. \n"
                                                    + readme.getAbsolutePath());
            }
        }
    }

    private Collection<File> getReadme(String studentLogin)
    {
        this.untar(studentLogin);

        return getFiles(this.getStudentHandinDirectory(studentLogin), "readme");
    }

    public Collection<File> getFiles(String dirPath, String filename) {
        Vector<File> files = new Vector<File>();

        File dir = new File(dirPath);
        if (dir == null || !dir.exists()) {
            return files;
        }
        for (String name : dir.list()) {
            File entry = new File(dir.getAbsolutePath() + "/" + name);
            //If it is a directory, recursively explore and add files
            if (entry.isDirectory()) {
                files.addAll(getFiles(entry.getAbsolutePath(), filename));
            }
            //Add if this entry is a file starts with readme (case insensitive)
            if (entry.isFile() && name.toUpperCase().startsWith(filename.toUpperCase())) {
                files.add(entry);
            }
        }

        return files;
    }

    //Rubric

    void setRubric(String filePath)
    {
        _rubricFile = new File(filePath);
    }

    public boolean hasRubric()
    {
        boolean exists = (_rubricFile != null) && (_rubricFile.exists());

        return exists;
    }

    public File getRubricFile()
    {
        return _rubricFile;
    }

    /**
     * It returns a student's handin if it exists otherwise it returns null.
     *
     * @param studentLogin
     * @return a student's handin for this assignment.
     */
    public File getHandin(String studentLogin)
    {
        for (File handin : this.getHandins())
        {
            if (handin.getName().equals(studentLogin + ".tar"))
            {
                return handin;
            }
            if (handin.getName().equals(studentLogin + ".tgz"))
            {
                return handin;
            }
        }

        return null;
    }

    /**
     * Returns the Files for each handin for this project. If this method has
     * not been called before it will load all of the handins.
     *
     * @return handins
     */
    private Collection<File> getHandins()
    {
        //If handins have not been requested yet, load them
        if(_handins == null)
        {
            _handins = new Vector<File>();

            _handins.addAll(Allocator.getFileSystemUtilities().getFiles(this.getHandinPath(), "tar"));
            _handins.addAll(Allocator.getFileSystemUtilities().getFiles(this.getHandinPath(), "tgz"));
        }
        return _handins;
    }

    /**
     * Helper method to generate handin path. Relies upon the handin directory
     * specified by constants, the name of this project, and the current year.
     *
     * @return handin path
     */
    private String getHandinPath()
    {
        String path = Allocator.getCourseInfo().getHandinDir()
                      + this.getAssignment().getName() + "/" + Allocator.getCalendarUtilities().getCurrentYear() + "/";
        return path;
    }

    /**
     * Checks whether or not a student login has a handin for this project.
     *
     * @param studentLogin
     * @return if student has handin for this project
     */
    public boolean hasHandin(String studentLogin)
    {
        return (this.getHandin(studentLogin) != null);
    }

    /**
     * Returns all student logins that have a handin
     *
     * @return all student logins that have a handin
     */
    public Collection<String> getHandinLogins()
    {
        Vector<String> logins = new Vector<String>();

        for (File handin : this.getHandins())
        {
            //Split at the . in the filename
            //So if handin is "jak2.tar", will add the "jak2" part
            logins.add(handin.getName().split("\\.")[0]);
        }
        return logins;
    }

    /**
     * Code directory is:
     *
     * /course/<course>/.cakehat/.<ta login>/<assignment name>/<student login>/
     *
     */
    protected String getStudentHandinDirectory(String studentLogin)
    {
        return Allocator.getGradingServices().getUserGradingDirectory() +
               this.getAssignment().getName() + "/" + studentLogin + "/";
    }

    /**
     * Untars a student's handin.
     *
     * @param studentLogin
     */
    protected void untar(String studentLogin)
    {
        if(!_untarredStudents.contains(studentLogin))
        {
            //Create an empty folder for grading compiled student code
            try
            {
                File compileDir = new File(this.getStudentHandinDirectory(studentLogin));
                Allocator.getFileSystemServices().makeDirectory(compileDir);

                //untar student handin
                File handin = this.getHandin(studentLogin);
                if(handin != null)
                {
                    try
                    {
                        Allocator.getArchiveUtilities().extractArchive(handin, compileDir);

                        //record that student's code has been untarred
                        _untarredStudents.add(studentLogin);
                    }
                    catch (ArchiveException ex)
                    {
                        new ErrorView(ex, "Unable to extract " + studentLogin + "'s handin.");
                    }
                }
            }
            catch(ServicesException e)
            {
                new ErrorView(e, "Unable to create directory to untar " + studentLogin + "'s handin into.");
            }
        }
    }

    public abstract boolean hasOpen();

    public abstract void openCode(String studentLogin);


    public abstract boolean hasPrint();

    public abstract void printCode(String studentLogin, String printer);

    public abstract void printCode(Iterable<String> studentLogins, String printer);
    

    public abstract boolean hasRun();

    public abstract void run(String studentLogin);


    public abstract boolean hasDemo();

    public abstract void runDemo();


    public abstract boolean hasTester();

    public abstract void runTester(String studentLogin);
}