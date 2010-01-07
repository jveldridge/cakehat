package utils;

import utils.printing.PrintRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

/**
 * This class provides functionality for the buttons in the standard and final project
 * frontend graders.
 *
 * @author jak2
 */
public class FrontendUtilities {

    /**
     * Takes each entry of a JList as a String and places them into a Vector.
     *
     * @param list the JList to iterate through
     * @return a Vector with String representations of the entries of the JList
     */
    public Vector<String> getJListAsVector(JList list){
        //Vector to return
        Vector<String> vector = new Vector<String>();

        //Get underlying model and its number of entries
        ListModel model = list.getModel();
        int size = model.getSize();

        //Add each entry from list model to vector to return
        for (int i = 0; i < size; i++) {
            vector.add((String) model.getElementAt(i));
        }

        return vector;
    }

    /**
     * This method runs a project demo.
     *
     * @param project the project whose demo should be run
     */
    public void demoProject(String project) {
        Allocator.getProject(project).runDemo();
    }

    /**
     * Untars each student's code for the currently selected project
     * from the handin directory to the appropriate .code directory
     *
     * @param project- currently selected project
     * @param studentList- list of students whose code must be untarred
     */
    public void untar(String project, JList studentList) {
        Allocator.getProject(project).untar(this.getJListAsVector(studentList));
    }

    /**
     * This method prints the code of all students the TA has been assigned to grade for
     * the current project.  It opens a pop-up window that allows the TA to select which printer should
     * be used (only allows bw3, bw4, and bw5).
     *
     * @param project
     * @param studentList
     */
    public void printAll(String project, JList studentList) {
        String printer = this.getPrinter("Choose printer on which to print all students' code");

        Allocator.getProject(project).print(getJListAsVector(studentList), printer);
        /*
        if (printer != null) {
            Vector<String> studentLogins = getJListAsVector(studentList);

            //want to print cover sheet for first student's code
            boolean print_cover_sheet = true;

            for (String sL : studentLogins) {
                printStudentProject(project, sL, printer, print_cover_sheet);

                //but not for subsequent students
                print_cover_sheet = false;              
            }
        }
        */
    }

    /**
     * TODO: Replace with printing code that is Java instead of Python.
     * TODO: Print source files as specified by constants, not just *.java
     *
     * Print particular student's code for a particular project on a particular printer
     *
     * @param project project for which the student's code should be printed
     * @param login login of student whose code should be printed
     * @param printer printer on which code should be printed
     */
    public void printStudentProject(String project, String studentLogin, String printer, boolean first) {
        if (printer == null) {
            printer = this.getPrinter("Choose printer on which to print student code");
        }

        Allocator.getProject(project).print(studentLogin, printer);
        //String printCommand = new String("cs015_gradingPrint "
        //                                 + printer + " "
        //                                 + first + " "
        //                                 + studentLogin
        //                                 + " " + Allocator.getProject(project).getStudentCodeDirectory(studentLogin)
        //                                 + "/*/*.java");

        //BashConsole.write(printCommand);
    }

    /**
     * Runs the currently selected student's code for the currently selected project
     *
     * @param project project to run
     * @param login login of student whose code will be run
     */
    public void runStudentProject(String project, String login) {
        Allocator.getProject(project).run(login);
    }

    /**
     * Runs the tester on the student's code.
     * 
     * @param project
     * @param login
     */
    public void runTester(String project, String login) {
        Allocator.getProject(project).runTester(login);
    }

    /**
     * This method indicates whether there is a tester for the currently selected project.
     *
     * @param asgn
     * @return true if has tester, false otherwise
     */
    public boolean hasTester(String project) {
       return Allocator.getProject(project).hasTester();
    }

    /**
     * TODO: Change this to open all types of source code, not just .java
     *
     * Opens the current student's project code in Kate
     *
     * @param project the project name that should be opened
     * @param login the login of the student whose project should be opened
     * @author jeldridg
     */
    public void openStudentProject(String project, String login) {
        //additional */ is to open code in all directories handin in
        String path = Allocator.getProject(project).getStudentCodeDirectory(login) + "*/";

        String cmd = "kate " + path + "*.java";
        
        BashConsole.writeThreaded(cmd);
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
    public void notifyStudents(String project, Vector<String> students) {
        String bccStringBuilder = "";
        for (String student: students) {
            bccStringBuilder += student + "@cs.brown.edu,";
        }

        EmailView ev = new EmailView(new String[] {Allocator.getGeneralUtilities().getUserLogin() + "@" + Allocator.getConstants().getEmailDomain()},
                                     new String[] {Allocator.getConstants().getGradesTA() + "@" + Allocator.getConstants().getEmailDomain(),
                                                   Allocator.getConstants().getGradesHTA() + "@" + Allocator.getConstants().getEmailDomain()},
                                     bccStringBuilder.split(","),
                                     "[" + Allocator.getConstants().getCourse() + "] " + project + " Graded",
                                     project + " has been graded and is available for pickup in the handback bin.");
        ev.setTitle(Allocator.getGeneralUtilities().getUserLogin() + "@cs.brown.edu - Send Email");
        ev.setVisible(true);
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
           String filePath = Allocator.getGeneralUtilities().getStudentGRDPath(assignment, studentLogin);
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

    public String getPrinter() {
        return this.getPrinter("Choose printer on which to print all students' code");
    }

    /**
     * Print dialogue for selecting printer.  Message passed in will be displayed as instructions to the user
     * 
     * @param message
     * @return the name of the printer selected
     */
    private String getPrinter(String message) {
        Object[] printerChoices = {"bw3", "bw4", "bw5"};
        ImageIcon icon = new javax.swing.ImageIcon("/GradingCommander/icons/print.png"); // NOI18N
        return (String) JOptionPane.showInputDialog(new JFrame(), message, "Select Printer", JOptionPane.PLAIN_MESSAGE, icon, printerChoices, "bw3");
    }

    /**
     * Displays the deduction list for the specified assignment.
     *
     * @param asgn
     */
    public void viewDeductionList(String asgn) {
        FileViewerView fv = new FileViewerView(new File(Allocator.getGeneralUtilities().getDeductionsListPath(asgn)));
        fv.setTitle(asgn + " Deductions List");
    }
}