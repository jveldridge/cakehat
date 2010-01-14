package rubric;

import config.HandinPart;
import config.LatePolicy;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
 * @author jak2
 */
public class RubricMananger
{

    private HashMap<String, GradingVisualizer> _graders = new HashMap<String, GradingVisualizer>();
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
        String XMLFilePath = Allocator.getGradingUtilities().getStudentRubricPath(part.getAssignment().getName(), studentLogin);

        //Determine if it has been opened
        final String graderViewName = part.getAssignment().getName() + "::" + studentLogin;
        //If it hasn't been opened
        if(!_graders.containsKey(graderViewName))
        {
            try
            {
            Rubric rubric = RubricGMLParser.parse(XMLFilePath, part);
            GradingVisualizer visualizer = new GradingVisualizer(rubric, XMLFilePath);
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
            Rubric rubric = RubricGMLParser.parse(part.getRubricFile().getAbsolutePath(), part);

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
            Rubric rubric = RubricGMLParser.parse(part.getRubricFile().getAbsolutePath(), part);

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
     * specified minutes of leniency to apply to deadlines.
     *
     * @param assignmentName
     * @param distribution
     * @param minutesOfLeniency
     * @return
     */
    public void distributeRubrics(HandinPart part, Map<String,Collection<String>> distribution, int minutesOfLeniency)
    {
        try
        {
            // get template rubric
            Rubric rubric = RubricGMLParser.parse(part.getRubricFile().getAbsolutePath(), part);
            
            //For each TA
            for (String taLogin : distribution.keySet())
            {
                //TODO: Get grader name from database
                Person grader = new Person(taLogin, Allocator.getGeneralUtilities().getUserName(taLogin));
                rubric.setGrader(grader);
                //For each student
                for (String studentLogin : distribution.get(taLogin))
                {
                    //TODO: Get student name from database
                    Person student = new Person(studentLogin, Allocator.getGeneralUtilities().getUserName(studentLogin));
                    rubric.setStudent(student);
                    // time status
                    rubric.setStatus(getTimeStatus(part, studentLogin, minutesOfLeniency));
                    rubric.setDaysLate(0);
                    if (rubric.getStatus() == TimeStatus.LATE && rubric.getTimeInformation().getLatePolicy() == LatePolicy.DAILY_DEDUCTION)
                    {
                        rubric.setDaysLate(getDaysLate(part, studentLogin, minutesOfLeniency));
                    }
                    String xmlPath = Allocator.getGradingUtilities().getStudentRubricPath(part.getAssignment().getName(), studentLogin);
                    RubricGMLWriter.write(rubric, xmlPath);
                }
            }
        }
        catch (RubricException ex)
        {
            new ErrorView(ex);
        }
    }

    private int getDaysLate(HandinPart part, String studentLogin, int minutesOfLeniency)
    {
        Calendar handinTime = Allocator.getGeneralUtilities().getModifiedDate(part.getHandin(studentLogin));
        Calendar onTime = part.getTimeInformation().getOntimeDate();

        return Allocator.getGeneralUtilities().daysAfterDeadline(handinTime, onTime, minutesOfLeniency);
    }

    private TimeStatus getTimeStatus(HandinPart part, String studentLogin, int minutesOfLeniency)
    {
        Calendar handinTime = Allocator.getGeneralUtilities().getModifiedDate(part.getHandin(studentLogin));

        if(part.getTimeInformation().getLatePolicy() == LatePolicy.NO_LATE)
        {
            Calendar onTime = part.getTimeInformation().getOntimeDate();

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
            String xmlPath = Allocator.getGradingUtilities().getStudentRubricPath(part.getAssignment().getName(), studentLogin);
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
            String xmlPath = Allocator.getGradingUtilities().getStudentRubricPath(part.getAssignment().getName(), studentLogin);
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
            String xmlPath = Allocator.getGradingUtilities().getStudentRubricPath(part.getAssignment().getName(), studentLogin);
            Rubric rubric = RubricGMLParser.parse(xmlPath, part);

            //Change grader
            //TODO: Get grader login from database
            rubric.setGrader(Allocator.getGeneralUtilities().getUserName(newGraderLogin), newGraderLogin);

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
            String xmlPath = Allocator.getGradingUtilities().getStudentRubricPath(part.getAssignment().getName(), studentLogin);
            Rubric rubric = RubricGMLParser.parse(xmlPath, part);

            //Write to grd
            String grdPath = Allocator.getGradingUtilities().getStudentGRDPath(part.getAssignment().getName(), studentLogin);
            RubricGMLWriter.write(rubric, grdPath);
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
}