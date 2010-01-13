package config;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;
import utils.Allocator;
import utils.FileViewerView;

/**
 *
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
        super(asgn, name, points);
    }

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
        FileViewerView fv = new FileViewerView(_deductionsFile);
        fv.setTitle(this.getAssignment().getName() + " Deductions List");
    }

    // Readme

    public boolean hasReadme(String studentLogin)
    {
        return !getReadme(studentLogin).isEmpty();
    }

    public void viewReadme(String studentLogin)
    {
        //View the first readme that was found
        FileViewerView fv = new FileViewerView(this.getReadme(studentLogin).iterator().next());
        fv.setTitle(studentLogin +"'s Readme");
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
            _handins = Allocator.getGeneralUtilities().getFiles(this.getHandinPath(), "tar");
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
                      + this.getAssignment().getName() + "/" + Allocator.getGeneralUtilities().getCurrentYear() + "/";
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
     * /course/<course>/cakehat/.<ta login>/<assignment name>/<student login>/
     *
     */
    protected String getStudentHandinDirectory(String studentLogin)
    {
        return Allocator.getGradingUtilities().getUserGradingDirectory() +
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
            String compileDir = this.getStudentHandinDirectory(studentLogin);
            Allocator.getGeneralUtilities().makeDirectory(compileDir);

            //untar student handin
            File handin = this.getHandin(studentLogin);
            if(handin != null)
            {
                Allocator.getGeneralUtilities().untar(handin.getAbsolutePath(), compileDir);
            }

            //record that student's code has been untarred
            _untarredStudents.add(studentLogin);
        }
    }

    public abstract boolean hasCode();

    public abstract void openCode(String studentLogin);

    public abstract void printCode(String studentLogin, String printer);

    public abstract void printCode(Iterable<String> studentLogins, String printer);

    public abstract void run(String studentLogin);

    public abstract boolean hasDemo();

    public abstract void runDemo();

    public abstract boolean hasTester();

    public abstract void runTester(String studentLogin);
}