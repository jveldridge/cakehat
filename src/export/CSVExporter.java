package export;

import config.Assignment;
import config.HandinPart;
import config.Part;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executors;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import utils.Allocator;
import utils.ErrorView;

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

    public void export()
    {
        JFileChooser chooser = new JFileChooser(Allocator.getCourseInfo().getCourseDir());

        FileNameExtensionFilter filter = new FileNameExtensionFilter("Comma Seperated Values", "csv");
        chooser.setFileFilter(filter);

        File defaultFile = new File(Allocator.getCourseInfo().getCourseDir() + "/" +
                                    Allocator.getCourseInfo().getCourse() + "_grades_" +
                                    Allocator.getCalendarUtilities().getCurrentYear() +
                                    ".csv");
        chooser.setSelectedFile(defaultFile);


        int returnVal = chooser.showSaveDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            this.export(chooser.getSelectedFile());
        }
    }

    public void export(File file)
    {
        _attemptCancel = false;

        ProgressView pView = new ProgressView(this.getNumberOfSteps(), this);
        ExportTask task = new ExportTask(file, pView);

        Executors.newSingleThreadExecutor().submit(task);
    }

    public void cancelExport()
    {
        _attemptCancel = true;
    }

    private int getNumberOfSteps()
    {
        int numParts = 0;

        for(Assignment asgn : Allocator.getCourseInfo().getAssignments())
        {
            for(Part part : asgn.getParts())
            {
                numParts++;
            }
        }

        int numStudents = Allocator.getDatabaseIO().getEnabledStudents().size();

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
            String line1 = ",,,", line2 = ",,,", line3 = ",,,";

            //Write out the assignments
            for(Assignment asgn : Allocator.getCourseInfo().getAssignments())
            {
                line1 += asgn.getName() + ",";

                //For each part, make room for the line below
                for(Part part : asgn.getParts())
                {
                    line1 += ",";
                    line2 += part.getName() + ",";
                    line3 += part.getPoints() + ",";

                    if(part instanceof HandinPart)
                    {
                        line1 += ",";
                        line2 += ",";
                        line3 += "Status,";
                    }
                }
                line2 += "Total,";
                line3 += asgn.getTotalPoints() + ",";
            }

            printer.println(line1);
            printer.println(line2);
            printer.println(line3);
        }

        private void writeStudentGrades(PrintWriter printer, ProgressView pView)
        {
            List<String> students = this.sortStudents();

            int currStep = 0;

            for(String student : students)
            {
                String[] studentParts = student.split(",");

                String login = studentParts[studentParts.length - 1];

                printer.append(student);
                printer.append(",");

                for(Assignment asgn : Allocator.getCourseInfo().getAssignments())
                {
                    //If there is no attempt to cancel
                    if(!_attemptCancel)
                    {
                        double total = 0;
                        for(Part part : asgn.getParts())
                        {
                            //If no exemption
                            if(Allocator.getDatabaseIO().getExemptionNote(login, part) == null)
                            {
                                double score = Allocator.getDatabaseIO().getStudentScore(login, part);

                                total += score;

                                printer.append(score + ",");
                            }
                            //If exemption
                            else
                            {
                                printer.append("EXEMPT" + ",");
                            }

                            if(part instanceof HandinPart)
                            {
                                String status = Allocator.getRubricManager().getTimeStatusDescriptor((HandinPart) part, login);
                                printer.append(status + ",");
                            }

                            pView.updateProgress(login, asgn, part, ++currStep);
                        }
                        printer.append(total + ",");

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

        /**
         * Returns the students sorted by last name in the format:
         *
         * <last name>,<beginning of name>,<student login>
         *
         * @return
         */
        private List<String> sortStudents()
        {
            Map<String, String> students = Allocator.getDatabaseIO().getEnabledStudents();

            List<String> descriptors = new Vector<String>();

            for(String studentLogin : students.keySet())
            {
                String name = students.get(studentLogin);
                String[] nameParts = name.split(" ");

                //Take the last part as the last name
                String lastName = nameParts[nameParts.length-1];

                //Rest of the name
                String beginName = "";
                for(int i = 0; i < nameParts.length - 1; i++)
                {
                    beginName += nameParts[i];
                    //If not the last word to be appended, add a space
                    if(i != nameParts.length - 2)
                    {
                        beginName += " ";
                    }
                }

                //Build string that is: <last name>, <beginning of name>, <student login>
                String descriptor = lastName + "," + beginName + "," + studentLogin;

                //Add descriptor
                descriptors.add(descriptor);
            }

            //Sort descriptors
            Collections.sort(descriptors);

            return descriptors;
        }
    }
}