package cakehat.services;

import cakehat.Allocator;
import cakehat.database.DeadlineInfo;
import cakehat.database.DeadlineInfo.DeadlineResolution;
import cakehat.database.Extension;
import cakehat.database.Group;
import cakehat.database.PartGrade;
import cakehat.database.Student;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;
import support.utils.LongRunningTask;

/**
 * Writes out in CSV format grades for all students for all assignments, gradable events, and parts including deadline
 * resolutions. Enabled students are written out first, followed by disabled students. Students are sorted
 * alphabetically by last name. Parts for which a student has no submitted grade or has no group (and therefore also no
 * submitted grade) are recorded as a {@code 0}.
 * <br/><br/>
 * CSV stands for Comma-separated values and is a poorly defined "standard" understood by spreadsheet applications such
 * as Microsoft Excel and OpenOffice Calc.
 * <br/><br/>
 * Execution of the task occurs on an internally managed thread which exists only for the life time of the task.
 * <br/><br/>
 * The format of the file is the following:
 * <pre>
 * {@code
 * |      |     |     | |Asgn1 |      |               |             |      |               |             | |Asgn2 |
 * |      |     |     | |EventA|      |               |             |EventB|               |             | |EventA|
 * |      |     |     | |Part1A|Part2A|Deadline Status|Penalty/Bonus|Part1B|Deadline Status|Penalty/Bonus| |Part1A|
 * |      |     |     | |      |      |               |             |      |               |             | |      |
 * |Out Of|     |     | |25    |50    |               |             |20    |               |             | |100   | ...
 * |      |     |     | |      |      |               |             |      |               |             | |      |
 * |Last  |First|Login| |      |      |               |             |      |               |             | |      |
 * |      |     |     | |      |      |               |             |      |               |             | |      |
 * |Kaplan|Josh |jak2 | |21.25 |34.5  |On Time        |0            |5     |NC Late        |-5           | |71.2  |
 * 
 * ... and so on
 * }
 * </pre>
 * 
 * @author jak2
 */     
public class CSVExportTask extends LongRunningTask
{   
    private final File _destination;
    
    public CSVExportTask(File destination)
    {
        _destination = destination;
    }
    
    @Override
    protected void startTask()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    export();
                }
                catch(RuntimeException e)
                {
                    notifyTaskFailed(e, "Unexpected runtime exception encountered while generating CSV file");
                }
                catch(ServicesException e)
                {
                    notifyTaskFailed(e, "Unable to retrieve needed information for CSV generation");
                }
                catch(IOException e)
                {
                    notifyTaskFailed(e, "Unable to retrieve needed information for CSV generation");
                }
                //This isn't an exception - it is used to signal that the export was cancelled
                catch(Cancellation cancellation)
                {
                    notifyTaskCanceled();
                }
            }
        }.start();
    }
    
    @Override
    protected void cancelTask() { }
    
    private void checkAttemptCancel(PrintWriter writer) throws Cancellation
    {
        if(isCancelAttempted())
        {
            writer.close();
            _destination.delete();
            throw new Cancellation();
        }
    }
    
    /**
     * Used to break out of code and signal cancellation.
     */
    private static class Cancellation extends Throwable { }
    
    private void export() throws ServicesException, IOException, Cancellation
    {
        //Initial setup
        PrintWriter writer = new PrintWriter(_destination);
        checkAttemptCancel(writer);
        
        //Load student data
        List<Student> enabledStudents = new ArrayList<Student>(Allocator.getDataServices().getEnabledStudents());
        Collections.sort(enabledStudents, Student.NAME_COMPARATOR);
        checkAttemptCancel(writer);
        
        List<Student> disabledStudents = new ArrayList<Student>(Allocator.getDataServices().getStudents());
        disabledStudents.removeAll(enabledStudents);
        Collections.sort(disabledStudents, Student.NAME_COMPARATOR);
        checkAttemptCancel(writer);
        
        //Number of steps is:
        // - 1 for the header rows
        // - 1 for each asssignment grade data is loaded for
        // - 1 for each student row that will be written
        // (These are not ideal steps as retrieving data from the database takes much longer than writing a student row)
        notifyTaskDetermined(1 + Allocator.getDataServices().getAssignments().size() +
                             enabledStudents.size() + disabledStudents.size());
        checkAttemptCancel(writer);
        
        //Write header rows
        writeHeaderRows(writer);
        
        //Load grade data
        GradeData data = new GradeData(writer);
        
        //Write student rows
        writeStudentRows(writer, enabledStudents, disabledStudents, data);
        
        //Close writer (which closes the file) then notify task completion
        writer.close();
        notifyTaskCompleted();
    }
    
    private void writeHeaderRows(PrintWriter writer) throws Cancellation
    {
        notifyTaskStepStarted("Writing header rows");
        
        ArrayList<String> row1 = new ArrayList<String>();
        ArrayList<String> row2 = new ArrayList<String>();
        ArrayList<String> row3 = new ArrayList<String>();
        ArrayList<String> row5 = new ArrayList<String>();
        ArrayList<String> row7 = new ArrayList<String>();
        
        //Setup first four columns
        for(int i = 0; i < 4; i++)
        {
            row1.add(null);
            row2.add(null);
            row3.add(null);
        }
        
        row5.add("Out Of");
        row5.add(null);
        row5.add(null);
        row5.add(null);
        
        row7.add("Last");
        row7.add("First");
        row7.add("Login");
        row7.add(null);
        
        //Write columns for assignments, events, and parts
        List<Assignment> assignments = Allocator.getDataServices().getAssignments();
        for(int asgnIndex = 0; asgnIndex < assignments.size(); asgnIndex++)
        {
            Assignment asgn = assignments.get(asgnIndex);
            row1.add(asgn.getName());
            
            //Edge case - assignment has no gradable events
            if(asgn.getGradableEvents().isEmpty())
            {
                row2.add(null);
                row3.add(null);
                row5.add(null);
            }
            else
            {
                for(int geIndex = 0; geIndex < asgn.getGradableEvents().size(); geIndex++)
                {
                    GradableEvent ge = asgn.getGradableEvents().get(geIndex);
                    row2.add(ge.getName());
                    
                    //Edge case - gradable event has no parts
                    if(ge.getParts().isEmpty())
                    {
                        //If not the first gradable event for the assignment, write a blank entry in the assignment row
                        if(geIndex != 0)
                        {
                            row1.add(null);
                        }
                        
                        row3.add(null);
                        row5.add(null);
                    }
                    else
                    {
                        for(int partIndex = 0; partIndex < ge.getParts().size(); partIndex++)
                        {
                            Part part = ge.getParts().get(partIndex);
                            row3.add(part.getName());
                            row5.add(Double.toString(part.getOutOf()));
                            
                            //If not the first part of the first gradable event for the assignment,
                            //write a blank entry in the assignment row
                            if(!(geIndex == 0 && partIndex == 0))
                            {
                                row1.add(null);
                            }
                            
                            //If not the first part for the gradable event, write a blank entry in the gradable event row
                            if(partIndex != 0)
                            {
                                row2.add(null);
                            }
                        }
                    }
                    
                    //Deadline
                    row1.add(null);
                    row2.add(null);
                    row3.add("Deadline Status");
                    row5.add(null);
                    
                    row1.add(null);
                    row2.add(null);
                    row3.add("Penalty/Bonus");
                    row5.add(null);
                }
            }
            
            //If not the last assignment, write a blank entry for first 5 rows
            if(asgnIndex != assignments.size() - 1)
            {
                row1.add(null);
                row2.add(null);
                row3.add(null);
                row5.add(null);
            }
        }
        
        //Write out the rows
        writeRow(writer, row1);
        writeRow(writer, row2);
        writeRow(writer, row3);
        writeRow(writer);
        writeRow(writer, row5);
        writeRow(writer);
        writeRow(writer, row7);
        writeRow(writer);
        
        //Notify step completion
        notifyTaskStepCompleted();
        checkAttemptCancel(writer);
    }
    
    private void writeStudentRows(PrintWriter writer, List<Student> enabledStudents, List<Student> disabledStudents,
            GradeData data) throws Cancellation
    {
        for(Student student : enabledStudents)
        {
            writeStudentRow(writer, data, student);
        }
        for(int i = 0; i < 3; i++)
        {
            writeRow(writer);
        }
        for(Student student : disabledStudents)
        {
            writeStudentRow(writer, data, student);
        }
    }
    
    private void writeStudentRow(PrintWriter writer, GradeData data, Student student) throws Cancellation
    {
        notifyTaskStepStarted("Writing " + student.getName());
            
        ArrayList<String> row = new ArrayList<String>();
        
        row.add(student.getLastName());
        row.add(student.getFirstName());
        row.add(student.getLogin());
        row.add(null);
        
        List<Assignment> assignments = Allocator.getDataServices().getAssignments();
        for(int asgnIndex = 0; asgnIndex < assignments.size(); asgnIndex++)
        {
            Assignment asgn = assignments.get(asgnIndex);
            Group group = data._groups.get(asgn).get(student);  
            
            //Edge case - assignment has no gradable events
            if(asgn.getGradableEvents().isEmpty())
            {
                row.add(null);
            }
            else
            {
                for(GradableEvent ge : asgn.getGradableEvents())
                {
                    double geTotalEarned = 0;
                    
                    //Edge case - gradable event has no parts
                    if(ge.getParts().isEmpty())
                    {
                        row.add(null);
                    }
                    else
                    {
                        for(Part part : ge.getParts())
                        {
                            double earned = 0;
                            if(group != null)
                            {
                                PartGrade grade = data._partGrades.get(part).get(group);
                                if(grade != null && grade.isSubmitted() && grade.getEarned() != null)
                                {
                                    earned = grade.getEarned();
                                }
                            }
                            row.add(Double.toString(earned));
                            
                            geTotalEarned += earned;
                        }
                    }
                    
                    //Deadline
                    DeadlineResolution resolution = data._deadlines.get(ge).apply(
                            data._occurrenceDates.get(ge).get(group),
                            data._extensions.get(ge).get(group));
                    row.add(resolution.getTimeStatus().toString());
                    row.add(Double.toString(resolution.getPenaltyOrBonus(geTotalEarned)));
                }
            }
            
            //If not the last assignment, write a blank entry
            if(asgnIndex != assignments.size() - 1)
            {
                row.add(null);
            }
        }
        
        writeRow(writer, row);
        notifyTaskStepCompleted();
        checkAttemptCancel(writer);
    }
    
    //Helper CSV writing methods
    
    private static void writeRow(PrintWriter writer)
    {
        writer.println();
    }
    
    private static void writeRow(PrintWriter writer, List<String> entries)
    {
        StringBuilder row = new StringBuilder();
        for(int i = 0; i < entries.size(); i++)
        {
            //Write the entry if not null
            String entry = entries.get(i);
            if(entry != null)
            {
                row.append(escape(entry));
            }
            
            //Write the separating comma if not the last entry
            if(i != entries.size() - 1)
            {
                row.append(',');
            }
        }
        writer.println(row.toString());
    }
    
    private static String escape(String text)
    {
        //Replaces all " with ""
        text = text.replace("\"", "\"\"");
        
        //Place all text inside of " marks
        text = '"' + text + '"';
        
        return text;
    }
    
    private final class GradeData
    {
        private final Map<Assignment, Map<Student, Group>> _groups = new HashMap<Assignment, Map<Student, Group>>();
        private final Map<Part, Map<Group, PartGrade>> _partGrades = new HashMap<Part, Map<Group, PartGrade>>();
        private final Map<GradableEvent, DeadlineInfo> _deadlines = new HashMap<GradableEvent, DeadlineInfo>();
        private final Map<GradableEvent, Map<Group, DateTime>> _occurrenceDates = new HashMap<GradableEvent, Map<Group, DateTime>>();
        private final Map<GradableEvent, Map<Group, Extension>> _extensions = new HashMap<GradableEvent, Map<Group, Extension>>();
        
        private GradeData(PrintWriter writer) throws ServicesException, IOException, Cancellation
        {   
            for(Assignment asgn : Allocator.getDataServices().getAssignments())
            {
                notifyTaskStepStarted("Retrieving data for " + asgn.getName());
                Set<Group> groups = Allocator.getDataServices().getGroups(asgn);

                //Students to groups
                Map<Student, Group> studentToGroup = new HashMap<Student, Group>();
                for(Group group : groups)
                {
                    for(Student student : group)
                    {
                        studentToGroup.put(student, group);
                    }
                }
                _groups.put(asgn, studentToGroup);

                for(GradableEvent ge : asgn)
                {
                    //Deadlines
                    _deadlines.put(ge, Allocator.getDataServices().getDeadlineInfo(ge));

                    //Occurrence dates
                    _occurrenceDates.put(ge, Allocator.getGradingServices().getOccurrenceDates(ge, groups));

                    //Extensions
                    _extensions.put(ge, Allocator.getDataServices().getExtensions(ge, groups));

                    //Part grades
                    for(Part part : ge)
                    {
                        _partGrades.put(part, Allocator.getDataServices().getEarned(groups, part));
                    }
                }
                
                //Done retrieving data for this assignment
                notifyTaskStepCompleted();
                checkAttemptCancel(writer);
            }
        }
    }
}