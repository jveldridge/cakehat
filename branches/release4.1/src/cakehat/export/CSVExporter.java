package cakehat.export;

import cakehat.config.Assignment;
import cakehat.config.Part;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.config.handin.DistributablePart;
import cakehat.database.HandinStatus;
import cakehat.database.Student;
import cakehat.services.ServicesException;
import java.util.ArrayList;
import cakehat.views.shared.ErrorView;

/**
 * Exports grades and handin status (on time, late, etc. and number of days
 * late if applicable). Pulls data from the database, config file, and rubrics.
 *
 * Don't create this class directly, access it through the Allocator.
 *
 * @author jak2
 */
public class CSVExporter implements Exporter
{
    private boolean _attemptCancel;

    @Override
    public void export() throws ExportException
    {
        JFileChooser chooser = new JFileChooser(Allocator.getPathServices().getCourseDir());

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Seperated Values", "csv");
        chooser.setFileFilter(filter);

        File defaultFile = new File(Allocator.getPathServices().getCourseDir(),
                Allocator.getCourseInfo().getCourse() + "_grades_" +
                Allocator.getCalendarUtilities().getCurrentYear() + ".csv");
        chooser.setSelectedFile(defaultFile);


        int returnVal = chooser.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            this.export(chooser.getSelectedFile());
        }
    }

    @Override
    public void export(File file) throws ExportException
    {
        _attemptCancel = false;

        ProgressView pView = new ProgressView(this.getNumberOfSteps(), this);
        ExportTask task = new ExportTask(file, pView);

        Executors.newSingleThreadExecutor().submit(task);
    }

    @Override
    public void cancelExport()
    {
        _attemptCancel = true;
    }

    private int getNumberOfSteps() throws ExportException
    {
        int numParts = 0;

        for(Assignment asgn : Allocator.getConfigurationInfo().getAssignments())
        {
            numParts += asgn.getParts().size();
        }

        int numStudents = Allocator.getDataServices().getEnabledStudents().size();
        return numParts * numStudents;
    }

    private class ExportTask implements Runnable
    {
        private File _exportFile;
        private ProgressView _pView;

        public ExportTask(File exportFile, ProgressView view)
        {
            _exportFile = exportFile;
            _pView = view;
        }

        public void run()
        {
            PrintWriter printer = null;
            try
            {
                printer = new PrintWriter(_exportFile);

                this.writeAssignmentHeaders(printer);

                printer.println();
                printer.println();

                this.writeStudentGrades(printer, _pView);
                _pView.notifyComplete();
            }
            catch(Exception e)
            {
                new ErrorView(e);
            }
            finally
            {
                if(printer != null)
                {
                    printer.close();
                }
            }
        }

        private void writeAssignmentHeaders(PrintWriter printer)
        {
            //line1 = assignment names
            //line2 = part names
            //line3 = part out of points
            //need to leave three columns empty in front for student last name, first name, and login
            String line1 = ",,,", line2 = ",,,", line3 = ",,,";

            //Write out the assignments
            for(Assignment asgn : Allocator.getConfigurationInfo().getAssignments())
            {
                line1 += asgn.getName() + ",";

                //For each part, make room for the line below
                for(Part part : asgn.getParts())
                {
                    line1 += ",";
                    line2 += part.getName() + ",";
                    line3 += part.getPoints() + ",";

                    if(part instanceof DistributablePart)
                    {
                        line1 += ",";
                        line2 += ",";
                        line3 += "Status,Days Late,";
                    }
                }
                line2 += "Total,";
                line3 += asgn.getTotalPoints() + ",";
            }

            printer.println(line1);
            printer.println(line2);
            printer.println(line3);
        }

        private void writeStudentGrades(PrintWriter printer, ProgressView pView) throws ExportException
        {
            List<Student> students = new ArrayList(Allocator.getDataServices().getEnabledStudents());
            Collections.sort(students, Student.NAME_COMPARATOR);

            int currStep = 0;

            for(Student student : students)
            {
                printer.append(student.getLastName() + ",");
                printer.append(student.getFirstName() + ",");
                printer.append(student.getLogin() + ",");

                for(Assignment asgn : Allocator.getConfigurationInfo().getAssignments())
                {
                    //If there is no attempt to cancel
                    if(!_attemptCancel)
                    {
                        try {
                            Group studentsGroup = Allocator.getDataServices().getGroup(asgn, student);
                            double total = 0;

                            for(Part part : asgn.getParts())
                            {
                                if (studentsGroup == null) {
                                    printer.append("0 (No grade recorded),");
                                    if (part instanceof DistributablePart) {
                                       printer.append("(unknown handin status),");
                                    }
                                    pView.updateProgress(student, asgn, part, ++currStep);
                                    continue;
                                }

                                //If no exemption
                                if (Allocator.getDataServices().getExemptionNote(studentsGroup, part) == null) {
                                    Double score = Allocator.getDataServices().getScore(studentsGroup, part);
                                    
                                    if (score != null) {
                                        total += score;
                                        printer.append(score + ",");
                                    }
                                    else {
                                        printer.append("0 (No grade recorded),");
                                    }
                                } else {
                                    printer.append("EXEMPT" + ",");
                                }
                                
                                if (part instanceof DistributablePart) {
                                    HandinStatus handinStatus = Allocator.getDataServices().getHandinStatus(studentsGroup);
                                    if (handinStatus != null) {
                                        printer.append(handinStatus.getTimeStatus() + ",");
                                        printer.append(handinStatus.getDaysLate() + ",");
                                    }
                                    else {
                                        printer.append("(unknown handin status),");
                                        printer.append("(unknown days late),");
                                    }
                                }
                                pView.updateProgress(student, asgn, part, ++currStep);
                            
                            }
                            printer.append(total + ",");
                        } catch (ServicesException ex) {
                            _exportFile.delete();
                            throw new ExportException("Export failed; grades data could not be retrieved " +
                                                      "from the database.", ex);
                        }
                    }
                    //If attempting to cancel, delete the file created
                    else
                    {
                        _exportFile.delete();
                        pView.notifyCancel();
                        return;
                    }
                }

                printer.println();
            }

        }
    }

}