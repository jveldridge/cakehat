package gradesystem.config;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;
import gradesystem.Allocator;
import gradesystem.handin.DistributablePart;
import gradesystem.handin.Handin;
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
        throw new RuntimeException("Deprecated - see DistributablePart");
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