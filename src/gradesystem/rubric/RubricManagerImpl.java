package gradesystem.rubric;

import gradesystem.services.ServicesException;
import gradesystem.views.backend.assignmentdist.DistributionRequester;
import gradesystem.config.HandinPart;
import gradesystem.config.LatePolicy;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import gradesystem.rubric.Rubric.Person;
import gradesystem.Allocator;
import gradesystem.config.TA;
import gradesystem.views.shared.ErrorView;

public class RubricManagerImpl implements RubricManager
{
    public void view(HandinPart part, String studentLogin)
    {
        view(part, studentLogin, false);
    }

    private HashMap<String, GradingVisualizer> _graders = new HashMap<String, GradingVisualizer>();
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
                    @Override
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

    public String getStudentRubricPath(HandinPart part, String studentLogin)
    {
        return Allocator.getCourseInfo().getRubricDir() + part.getAssignment().getName() + "/" + studentLogin + ".gml";
    }

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
        return new File(getStudentRubricPath(part, studentLogin)).exists();
    }

    public Map<String, Double> getHandinTotals(HandinPart part, Iterable<String> studentLogins)
    {
        HashMap<String, Double> totals = new HashMap<String, Double>();

        for(String studentLogin : studentLogins)
        {
            totals.put(studentLogin, getHandinTotal(part, studentLogin));
        }

        return totals;
    }

    public double getHandinTotal(HandinPart part, String studentLogin)
    {
        try
        {
            // get template rubric
            Rubric rubric = RubricGMLParser.parse(getStudentRubricPath(part, studentLogin), part);

            return rubric.getTotalHandinScore();
        }
        catch(RubricException e)
        {
            new ErrorView(e);
        }

        return 0;
    }

    public double getRubricTotal(HandinPart part, String studentLogin)
    {
        try
        {
            // get template rubric
            Rubric rubric = RubricGMLParser.parse(getStudentRubricPath(part, studentLogin), part);

            return rubric.getTotalRubricScore();
        }
        catch(RubricException e)
        {
            new ErrorView(e);
        }

        return 0;
    }

    public Map<String, Double> getRubricTotals(HandinPart part, Iterable<String> studentLogins)
    {
        HashMap<String, Double> totals = new HashMap<String, Double>();

        for(String studentLogin : studentLogins)
        {
            totals.put(studentLogin, getRubricTotal(part, studentLogin));
        }

        return totals;
    }

    public void distributeRubrics(HandinPart part,
                                  Map<TA, Collection<String>> distribution,
                                  int minutesOfLeniency,
                                  DistributionRequester requester) throws RubricException
    {
        Map<String, String> students;
        try {
            students = Allocator.getDatabaseIO().getAllStudents();
        } catch (SQLException ex) {
            throw new RubricException("Rubrics could not be distributed for part " + part + " " +
                                      "because the student list could not be read from the database.", ex);
        }

        Map<String, Calendar> extensions;
        try {
            extensions = Allocator.getGradingServices().getExtensions(part, students.keySet());
        } catch (ServicesException ex) {
            throw new RubricException("Rubrics could not be distributed for part " + part + " " +
                                      "because extnsions data could not be read from the database.", ex);
        }

        int numToDistribute = 0;
        for (TA ta : distribution.keySet()) {
            numToDistribute += distribution.get(ta).size();
        }

        int numDistributedSoFar = 0;
        double fractionDone = 0.0;

        try
        {
            //get template rubric
            Rubric rubric = RubricGMLParser.parse(part.getRubricFile().getAbsolutePath(), part);

            //for each TA
            for (TA ta : distribution.keySet())
            {
                //for each student
                for (String studentLogin : distribution.get(ta))
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

                    numDistributedSoFar++;
                    fractionDone = (double) numDistributedSoFar / (double) numToDistribute;
                    requester.updatePercentDone((int) (fractionDone * 100));
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
        Calendar handinTime = Allocator.getFileSystemUtilities().getModifiedDate(part.getHandin(studentLogin));
        Calendar onTime = part.getTimeInformation().getOntimeDate();
        //if there is an extension, use that date
        if(extensions.containsKey(studentLogin) && extensions.get(studentLogin) != null)
        {
            onTime = extensions.get(studentLogin);
        }

        return Allocator.getCalendarUtilities().daysAfterDeadline(handinTime, onTime, minutesOfLeniency);
    }

    private TimeStatus getTimeStatus(HandinPart part, String studentLogin, Map<String, Calendar> extensions, int minutesOfLeniency)
    {
        Calendar handinTime = Allocator.getFileSystemUtilities().getModifiedDate(part.getHandin(studentLogin));

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
            if(Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency))
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
            if(Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency))
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
            if(Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, earlyTime, minutesOfLeniency))
            {
                return TimeStatus.EARLY;
            }
            // If before ontime deadline
            else if(Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, onTime, minutesOfLeniency))
            {
                return TimeStatus.ON_TIME;
            }
            // If before late deadline
            else if(Allocator.getCalendarUtilities().isBeforeDeadline(handinTime, lateTime, minutesOfLeniency))
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

    public void convertToGRD(HandinPart part, String studentLogin)
    {
        String xmlPath = getStudentRubricPath(part, studentLogin);
        String grdPath = Allocator.getGradingServices().getStudentGRDPath(part, studentLogin);

        try
        {
            //Get rubric
            Rubric rubric = RubricGMLParser.parse(xmlPath, part);

            //Write to grd
            RubricGRDWriter.write(rubric, grdPath);
        }
        catch(RubricException e)
        {
            //delete the incomplete GRD file, if it was written
            File grdFile = new File(grdPath);
            if (grdFile.exists()) {
                grdFile.delete();
            }

            //display the error to the user
            new ErrorView(e, "The rubric ould not be written.  " +
                             "No GRD file has been created.");
        }
    }

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