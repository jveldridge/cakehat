package rubric;

import config.HandinPart;
import config.LatePolicy;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import rubric.Rubric.Person;
import utils.Allocator;
import utils.ErrorView;

/**
 * Don't directly create this class, access it via the Allocator.
 * All rubric related functionality goes through this class.
 *
 * @author spoletto
 * @author jak2
 */
public class RubricMananger
{
    /**
     * View the rubric for a student for a given handin part. If it is already
     * open it will be brought to front and centered on screen.
     *
     *
     * @param part
     * @param studentLogin
     */
    public void view(HandinPart part, String studentLogin)
    {
        view(part, studentLogin, false);
    }

    private HashMap<String, GradingVisualizer> _graders = new HashMap<String, GradingVisualizer>();
    /**
     * View the rubric for a student for a given handin part. If it is already
     * open it will be brought to front and centered on screen.
     *
     * @param part
     * @param studentLogin
     * @param isAdmin if true then on save the rubric's handin score will be written
     *                to the database
     */
    public void view(HandinPart part, String studentLogin, boolean isAdmin)
    {
        String GMLFilePath = getStudentRubricPath(part, studentLogin);

        //Determine if it has been opened
        final String graderViewName = part.getAssignment().getName() + "::" + studentLogin;
        //If it hasn't been opened
        if(!_graders.containsKey(graderViewName))
        {
            try
            {
                Rubric rubric = RubricGMLParser.parse(GMLFilePath, part);
                GradingVisualizer visualizer = new GradingVisualizer(rubric, isAdmin);
                visualizer.addWindowListener(new WindowAdapter()
                {
                    public void windowClosed(WindowEvent e)
                    {
                        _graders.remove(graderViewName);
                    }
                });

                _graders.put(graderViewName, visualizer);
            }
            catch (RubricException ex)
            {
                new ErrorView(ex);
            }
        }
        //If it has, bring it to front and center it on screen
        else
        {
            GradingVisualizer visualizer = _graders.get(graderViewName);
            visualizer.toFront();
            visualizer.setLocationRelativeTo(null);
        }

    }

    /**
     * @date 01/08/2010
     * @return path to student's rubric for a particular project
     *          Note: this is independent of the TA who graded the student
     *         currently, /course/<course>/cakehat/<year>/rubrics/<assignmentName>/<studentLogin>.gml
     */
    protected String getStudentRubricPath(HandinPart part, String studentLogin) {
        return Allocator.getCourseInfo().getRubricDir() + part.getAssignment().getName() + "/" + studentLogin + ".gml";
    }

    /**
     * Views an assignment's template rubric.
     *
     * @param rubric
     */
    public void viewTemplate(HandinPart part)
    {
        try
        {
            Rubric rubric = RubricGMLParser.parse(part.getRubricFile().getAbsolutePath(), part);
            new TemplateVisualizer(part.getAssignment().getName(), rubric);
        }
        catch (RubricException ex)
        {
            new ErrorView(ex);
        }
    }

    public boolean hasRubric(HandinPart part, String studentLogin)
    {
        return new File(getRubricPath(part, studentLogin)).exists();
    }

    private String getRubricPath(HandinPart part, String studentLogin)
    {
        return Allocator.getCourseInfo().getRubricDir() + 
               part.getAssignment().getName() + "/" + studentLogin + ".gml";
    }

    /**
     * Get the scores for the handin part. Includes any late penalties that
     * were applied.
     *
     * @param studentLogin
     * @return map from student logins to score
     */
    public Map<String, Double> getHandinTotals(HandinPart part, Iterable<String> studentLogins)
    {
        HashMap<String, Double> totals = new HashMap<String, Double>();

        for(String studentLogin : studentLogins)
        {
            totals.put(studentLogin, getHandinTotal(part, studentLogin));
        }

        return totals;
    }

    /**
     * Get the score for the handin part. Includes any late penalties that
     * were applied.
     *
     * @param studentLogin
     * @return
     */
    public double getHandinTotal(HandinPart part, String studentLogin)
    {
        try
        {
            // get template rubric
            Rubric rubric = RubricGMLParser.parse(getRubricPath(part, studentLogin), part);

            return rubric.getTotalHandinScore();
        }
        catch(RubricException e)
        {
            new ErrorView(e);
        }

        return 0;
    }

    /**
     * Get the score for the entire rubric. Includes any late penalties that
     * were applied.
     *
     * @param studentLogin
     * @return
     */
    public double getRubricTotal(HandinPart part, String studentLogin)
    {
        try
        {
            // get template rubric
            Rubric rubric = RubricGMLParser.parse(getRubricPath(part, studentLogin), part);

            return rubric.getTotalRubricScore();
        }
        catch(RubricException e)
        {
            new ErrorView(e);
        }

        return 0;
    }

    /**
     * Get the scores for the entire rubric. Includes any late penalties that
     * were applied.
     *
     * @param studentLogin
     * @return map from student logins to score
     */
    public Map<String, Double> getRubricTotals(HandinPart part, Iterable<String> studentLogins)
    {
        HashMap<String, Double> totals = new HashMap<String, Double>();

        for(String studentLogin : studentLogins)
        {
            totals.put(studentLogin, getRubricTotal(part, studentLogin));
        }

        return totals;
    }

    /**
     * Distributes the rubric for the HandinPart part, mapping TA logins (as
     * strings) to Collections of Strings of studentLogins that TA is assigned
     * to grade. When determining handin time status, takes into account the
     * specified minutes of leniency to apply to deadlines. Also takes into
     * account extensions that have been recorded in the database. If an
     * extension is granted and the policy is MULTIPLE_DEADLINES it is treated
     * as if the policy is NO_LATE using the extension date.
     *
     * @param assignmentName
     * @param distribution
     * @param minutesOfLeniency
     * @return
     */
    public void distributeRubrics(HandinPart part, Map<String,Collection<String>> distribution, int minutesOfLeniency)
    {
        Map<String, String> students = Allocator.getDatabaseIO().getAllStudents();
        Map<String, String> tas = Allocator.getDatabaseIO().getAllTAs();
        Map<String, Calendar> extensions = getExtensions(part, students.keySet());

        try
        {
            //get template rubric
            Rubric rubric = RubricGMLParser.parse(part.getRubricFile().getAbsolutePath(), part);
            
            //for each TA
            for (String taLogin : distribution.keySet())
            {
                Person grader = new Person(tas.get(taLogin), taLogin);
                rubric.setGrader(grader);

                //for each student
                for (String studentLogin : distribution.get(taLogin))
                {
                    //student login and name
                    Person student = new Person(students.get(studentLogin), studentLogin);
                    rubric.setStudent(student);
                    //time status
                    rubric.setStatus(getTimeStatus(part, studentLogin, extensions, minutesOfLeniency));
                    rubric.setDaysLate(0);
                    if (rubric.getStatus() == TimeStatus.LATE && rubric.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
                    {
                        rubric.setDaysLate(getDaysLate(part, studentLogin, extensions, minutesOfLeniency));
                    }
                    //write file
                    String gmlPath = getStudentRubricPath(part, studentLogin);
                    RubricGMLWriter.write(rubric, gmlPath);
                }
            }
        }
        catch (RubricException ex)
        {
            new ErrorView(ex);
        }
    }

    private int getDaysLate(HandinPart part, String studentLogin, Map<String, Calendar> extensions, int minutesOfLeniency)
    {
        Calendar handinTime = Allocator.getGeneralUtilities().getModifiedDate(part.getHandin(studentLogin));
        Calendar onTime = part.getTimeInformation().getOntimeDate();
        //if there is an extension, use that date
        if(extensions.containsKey(studentLogin))
        {
            onTime = extensions.get(studentLogin);
        }

        return Allocator.getGeneralUtilities().daysAfterDeadline(handinTime, onTime, minutesOfLeniency);
    }

    /**
     * Gets the extensions for each student in studentLogins. Takes into account
     * the group the student is in; using the latest date of all group members.
     * If there is no extension for any member of the group, null is assigned.
     *
     * @param part
     * @param studentLogins
     * @return
     */
    private Map<String, Calendar> getExtensions(HandinPart part, Iterable<String> studentLogins)
    {
        //Info from the database
        Map<String, Calendar> individualExtensions = Allocator.getDatabaseIO().getExtensions(part);
        Map<String, Collection<String>> groups = Allocator.getDatabaseIO().getGroups(part);

        //Extensions for each student, taking into account the extensions that
        //apply for each member of the group
        Map<String, Calendar> groupExtensions = new HashMap<String, Calendar>();

        for(String studentLogin : studentLogins)
        {
            groupExtensions.put(studentLogin,
                                getExtensionCalendar(studentLogin, individualExtensions, groups));
        }

        return groupExtensions;
    }

    /**
     * Gets the the extension calendar for this student. Gets all calendars for
     * each member of the group and then returns the latest one. If there is no
     * applicable extension calendar, then null is returned.
     *
     * @param studentLogin
     * @param extensions
     * @param groups
     * @return
     */
    private Calendar getExtensionCalendar(String studentLogin, Map<String, Calendar> extensions, Map<String, Collection<String>> groups)
    {
        Collection<String> groupLogins = groups.get(studentLogin);
        
        Calendar latestCal = null;
        
        for(String login : groupLogins)
        {
            Calendar cal = extensions.get(login);
            
            //If no calendar so far, set this one
            if(latestCal == null)
            {
                latestCal = cal;
            }            
            //Else if this student has a calendar and is after the latest so far
            else if(cal != null && cal.after(latestCal))
            {
                latestCal = cal;
            }
        }
        
        return latestCal;
    }

    private TimeStatus getTimeStatus(HandinPart part, String studentLogin, Map<String, Calendar> extensions, int minutesOfLeniency)
    {
        Calendar handinTime = Allocator.getGeneralUtilities().getModifiedDate(part.getHandin(studentLogin));
        
        Calendar extensionTime = null;
        if(extensions.containsKey(studentLogin))
        {
            extensionTime = extensions.get(studentLogin);
        }

        //If the policy is N0_LATE, or MULTIPLE_DEADLINES with an extension
        if( (part.getTimeInformation().getLatePolicy() == LatePolicy.NO_LATE) ||
            (extensionTime != null && part.getTimeInformation().getLatePolicy() == LatePolicy.MULTIPLE_DEADLINES)   )
        {
            Calendar onTime = part.getTimeInformation().getOntimeDate();
            if(extensionTime != null)
            {
                onTime = extensionTime;
            }

            //If before deadline
            if(Allocator.getGeneralUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency))
            {
                return TimeStatus.ON_TIME;
            }
            else
            {
                return TimeStatus.NC_LATE;
            }
        }
        else if(part.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
        {
            Calendar onTime = part.getTimeInformation().getOntimeDate();
            if(extensionTime != null)
            {
                onTime = extensionTime;
            }

            //If before deadline
            if(Allocator.getGeneralUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency))
            {
                return TimeStatus.ON_TIME;
            }
            else
            {
                return TimeStatus.LATE;
            }
        }
        else if(part.getTimeInformation().getLatePolicy() == LatePolicy.MULTIPLE_DEADLINES)
        {
            Calendar earlyTime = part.getTimeInformation().getEarlyDate();
            Calendar onTime = part.getTimeInformation().getOntimeDate();
            Calendar lateTime = part.getTimeInformation().getLateDate();

            // If before early deadline
            if(Allocator.getGeneralUtilities().isBeforeDeadline(handinTime, earlyTime, minutesOfLeniency))
            {
                return TimeStatus.EARLY;
            }
            // If before ontime deadline
            else if(Allocator.getGeneralUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency))
            {
                return TimeStatus.ON_TIME;
            }
            // If before late deadline
            else if(Allocator.getGeneralUtilities().isBeforeDeadline(handinTime, lateTime, minutesOfLeniency))
            {
                return TimeStatus.LATE;
            }
            // If after late deadline
            else
            {
                return TimeStatus.NC_LATE;
            }
        }

        return null;
    }

    /**
     * Reads out the time status information from a student's rubric.
     * If the student's rubric cannot be parsed for any reason null will be
     * returned.
     *
     * @param part
     * @param studentLogin
     */
     public TimeStatus getTimeStatus(HandinPart part, String studentLogin)
     {
         try
         {
            //Get rubric
            String xmlPath = getStudentRubricPath(part, studentLogin);
            Rubric rubric = RubricGMLParser.parse(xmlPath, part);
            
            return rubric.getStatus();
            
         }
         catch(RubricException e)
         {
             new ErrorView(e);
         }
         
         return null;
     }

    /**
     * Reads out the time status information from a student's rubric. If the
     * late policy is DAILY_DEDUCTION and the assignment is LATE, then the
     * number of days late will be included in the returned descriptor.
     *
     * @param part
     * @param studentLogin
     */
     public String getTimeStatusDescriptor(HandinPart part, String studentLogin)
     {
         try
         {
            //Get rubric
            String xmlPath = getStudentRubricPath(part, studentLogin);
            Rubric rubric = RubricGMLParser.parse(xmlPath, part);

            TimeStatus status = rubric.getStatus();

            String descriptor = status.getPrettyPrintName();

            if(part.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION &&
               status == TimeStatus.LATE)
            {
                descriptor += " " + rubric.getDaysLate();

                if(rubric.getDaysLate() == 1)
                {
                    descriptor += " day late";
                }
                else
                {
                    descriptor += " days late";
                }
            }

            return descriptor;

         }
         catch(RubricException e) { }

         return "not available";
     }

     /**
      * Sets the student's rubric to the given time status and days late. If days late is not
      * applicable pass in 0.
      *
      * @param part
      * @param studentLogin
      * @param status
      * @param daysLate
      */
     public void setTimeStatus(HandinPart part, String studentLogin, TimeStatus status, int daysLate)
     {
         try
         {
            //Get rubric
            String xmlPath = getStudentRubricPath(part, studentLogin);
            Rubric rubric = RubricGMLParser.parse(xmlPath, part);

            //Change status and days late
            rubric.setStatus(status);
            rubric.setDaysLate(daysLate);

            //Write changes
            RubricGMLWriter.write(rubric, xmlPath);
         }
         catch(RubricException e)
         {
             new ErrorView(e);
         }
     }

    /**
     * Changes the grader of the rubric. Only makes changes in the rubric itself,
     * this has no effect on any information in the database.
     *
     * @param part
     * @param studentLogin
     * @param newGraderLogin
     */
    public void reassignRubric(HandinPart part, String studentLogin, String newGraderLogin)
    {
        try
        {
            //Get rubric
            String xmlPath = getStudentRubricPath(part, studentLogin);
            Rubric rubric = RubricGMLParser.parse(xmlPath, part);

            //Change grader
            String graderName = Allocator.getDatabaseIO().getAllTAs().get(newGraderLogin);
            rubric.setGrader(graderName, newGraderLogin);

            //Write rubric
            RubricGMLWriter.write(rubric, xmlPath);
        }
        catch(RubricException e)
        {
            new ErrorView(e);
        }
    }

    /**
     * Converts a rubric to a GRD file.
     *
     * @param part
     * @param studentLogin
     */
    public void convertToGRD(HandinPart part, String studentLogin)
    {
        try
        {
            //Get rubric
            String xmlPath = getStudentRubricPath(part, studentLogin);
            Rubric rubric = RubricGMLParser.parse(xmlPath, part);

            //Write to grd
            String grdPath = Allocator.getGradingUtilities().getStudentGRDPath(part, studentLogin);
            RubricGRDWriter.write(rubric, grdPath);
        }
        catch(RubricException e)
        {
            new ErrorView(e);
        }
    }

    /**
     * Converts rubrics to GRD files.
     *
     * @param part
     * @param studentLogins
     */
    public void convertToGRD(HandinPart part, Iterable<String> studentLogins)
    {
        for(String studentLogin : studentLogins)
        {
            convertToGRD(part, studentLogin);
        }
    }

    public void convertToGRD(Map<HandinPart, Iterable<String>> toConvert)
    {
        for(HandinPart part : toConvert.keySet())
        {
            for(String studentLogin : toConvert.get(part))
            {
                convertToGRD(part, studentLogin);
            }
        }
    }
}