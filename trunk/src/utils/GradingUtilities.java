package utils;

import config.TA;
import utils.printing.PrintRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This class provides grading specific utility functions.
 *
 * @author jeldridg
 * @author jak2
 */
public class GradingUtilities {

    /**
     * Returns whether or not the current user is a TA for the course as
     * specified by the configuration file.
     * @return
     */
    public boolean isUserTA(){
        String userLogin = Allocator.getGeneralUtilities().getUserLogin();
        
        for(TA ta : Allocator.getCourseInfo().getTAs()){
            if(ta.getLogin().equals(userLogin)){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not the current user is an admin for the course as
     * specified by the configuration file.
     * @return
     */
    public boolean isUserAdmin(){
        String userLogin = Allocator.getGeneralUtilities().getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getTAs()){
            if(ta.getLogin().equals(userLogin) && ta.isAdmin()){
                return true;
            }
        }
        return false;
    }


    public void makeUserGradingDirectory() {
        Allocator.getGeneralUtilities().makeDirectory(this.getUserGradingDirectory());
    }

    public void removeUserGradingDirectory() {
        Allocator.getGeneralUtilities().removeDirectory(this.getUserGradingDirectory());
    }

    /**
     * @date 01/08/2009
     * @return path to a TA's temporary grading directory.
     *         currently, /course/<course>/<cakehat>/.<talogin>/
     *         this directory is erased when the user closes the grader
     */
    public String getUserGradingDirectory() {
        return Allocator.getCourseInfo().getGradingDir() + "." + Allocator.getGeneralUtilities().getUserLogin() + "/";
    }

    /**
     * @date 01/08/2010
     * @return path to student's rubric for a particular project
     *          Note: this is independent of the TA who graded the student
     *         currently, /course/<course>/cakehat/<year>/rubrics/<assignmentName>/<studentLogin>.gml
     */
    public String getStudentRubricPath(String assignmentName, String studentLogin) {
        return Allocator.getCourseInfo().getRubricDir() + assignmentName + "/" + studentLogin + ".gml";
    }

    public String getStudentGRDPath(String assignmentName, String studentLogin) {
        return this.getUserGradingDirectory() + assignmentName + "/" + studentLogin + ".grd";
    }

    /**
     * Opens a new EmailView so that user TA can inform students that their assignment
     * has been graded.  Default settings:
     *  FROM:    user TA
     *  TO:      user TA
     *  CC:      grades TA & grades HTA
     *  BCC:     students the user TA is assigned to grade for this assignment, as selected
     *  SUBJECT: "[<course code>] <project> Graded"
     *  MESSAGE: "<project> has been graded and is available for pickup in the handback bin."
     *
     * @param project
     * @param students
     */
    public void notifyStudents(String project, Vector<String> students, boolean emailRubrics) {
        
        Map<String,String> attachments = null;
        if (emailRubrics) {
            attachments = new HashMap<String,String>();
            for (String student : students) {
                attachments.put(student, Allocator.getGradingUtilities().getStudentGRDPath(project, student));
            }
        }
        
        for (int i = 0; i < students.size(); i++) {
            students.setElementAt(students.get(i)+"@"+Allocator.getCourseInfo().getEmailDomain(), i);  //login -> email
        }

        new EmailView(students, Allocator.getCourseInfo().getNotifyAddresses(), 
                        "[" + Allocator.getCourseInfo().getCourse() + "] " + project + " Graded",
                         project + " has been graded and is available for pickup in the handback bin.", attachments);
                
    }

    /**
     * Prints .GRD files for each student the user TA has graded
     * Calls getPrinter(...) and then prints using lpr
     *
     * @param assignment assignment for which .GRD files should be printed
     */
    public void printGRDFiles(Iterable<String> studentLogins, String assignment) {
        String printer = this.getPrinter("Select printer to print .GRD files");

        String taLogin = Allocator.getGeneralUtilities().getUserLogin();
        Vector<PrintRequest> requests = new Vector<PrintRequest>();
        for(String studentLogin : studentLogins){
           String filePath = this.getStudentGRDPath(assignment, studentLogin);
           File file = new File(filePath);
            try{
                requests.add(new PrintRequest(file, taLogin, studentLogin));
            }
            catch (FileNotFoundException ex) {
                new ErrorView(ex);
            }
        }
        Allocator.getPortraitPrinter().print(requests, printer);

        
        /*
        if (printer != null) {
            //want to print cover sheet for first student's code
            boolean print_cover_sheet = true;

            for (String sL : studentLogins) {
                String printCommand;
                if (print_cover_sheet) {
                    printCommand = "lpr -P" + printer + " " + Allocator.getGeneralUtilities().getStudentGRDPath(assignment, sL);

                    //but not for subsequent students
                    print_cover_sheet = false;
                }
                else {
                    printCommand = "lpr -h -P" + printer + " " + Allocator.getGeneralUtilities().getStudentGRDPath(assignment, sL);                    
                }
                BashConsole.writeThreaded(printCommand);
            }
        }
        */
    }

    /**
     * Prompts the user to a select a printer.
     *
     * @return printer selected
     */
    public String getPrinter() {
        return this.getPrinter("Please select a printer.");
    }

    /**
     * Print dialogue for selecting printer.  Message passed in will be displayed as instructions to the user
     * 
     * @param message
     * @return the name of the printer selected
     */
    public String getPrinter(String message) {
        Object[] printerChoices = {"bw3", "bw4", "bw5"};
        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png");

        return (String) JOptionPane.showInputDialog(new JFrame(), message, "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
    }
    
}