package cakehat.views.entergrade;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.Group;
import cakehat.database.GroupGradingSheet;
import cakehat.database.GroupGradingSheet.GroupSubsectionEarned;
import cakehat.database.Student;
import cakehat.gradingsheet.GradingSheetSection;
import cakehat.gradingsheet.GradingSheetSubsection;
import cakehat.services.ServicesException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * The command line interface for entering grade parts with quick names.
 *
 * @author jak2
 * @author Yudi
 */
public class EnterGradeCLI {
    private EnterGradeCLI() { };
    
    /**
     * Enters grade for a student or group for the part as specified by {@code args}. {@code args} is all but the first
     * of the arguments passed to the cakehat mainline. The expected format of the arguments is:
     * <br/>
     * First entry: part quick name <br/>
     * Second entry: student login <br/>
     * Subsequent entries: points given for each subsection (optional) <br/>
     * <br/>
     * Thorough data validation occurs, so the code will respond appropriately if the data is not in the expected format
     *
     * @param args
     */
    public static void performEnterGrade(List<String> args) {
        try {
            performEnterGrade(args, new DefaultInteractor());
        } catch (EnterGradeException e) {
            e.printToConsole();
        }
    }

    /**
     * This method is for unit testing purposes. It behaves identically to {@link #performEnterGrade(java.util.List)}
     * except that it does not print its results to the terminal.
     *
     * @param args
     * @param interactor
     * @throws EnterGradeException
     */
    static EnterGradeResult performEnterGrade(List<String> args, EnterGradeInteractor interactor)
            throws EnterGradeException {
        if (args.size() >= 2) {
            String quickName = args.get(0);
            String studentLogin = args.get(1);
            
            return enterPartGrade(quickName, studentLogin, args.subList(2, args.size()), interactor);
        }
        else {
            throw new EnterGradeException("Usage: cakehat -c [course] -m enterGrade -g [quick name] [student login] OR",
                                          "       cakehat -c [course] -m enterGrade -g [quick name] [student login] [points earned]...");
        }
    }
    
    /**
     * Enters the part grade for the student. Verification is performed throughout.
     *
     * @param quickName
     * @param studentLogin
     * @param pointsArgs if empty, full points will be awarded for each subsection
     * @param interactor
     * @throws EnterGradeException
     * @return EnterGradeResult
     */
    private static EnterGradeResult enterPartGrade(String quickName, String studentLogin, List<String> pointsArgs,
                                                   EnterGradeInteractor interactor) throws EnterGradeException {
        //Verify student login
        Student student = getStudent(studentLogin);

        //Load part
        Part part = getQuickNamePart(quickName);

        //Get the student's group
        Group group = getStudentsGroup(student, part);

        //If no group exists for this student and assignment pairing
        if (group == null) {
            //There must be a group if the assignment has groups
            if (part.getGradableEvent().getAssignment().hasGroups()) {
                throw new EnterGradeException("This part belongs to an assignment that requires groups. The provided "
                        + "student login [" + student.getLogin() + "] has no group for this assignment "
                        + "[" + part.getGradableEvent().getAssignment().getName() + "]");
            }
            //If the assignment does not have groups then an auto-group was not created, which is a cakehat bug
            else {
                throw new EnterGradeException("cakehat failure, auto-group of one was not created\n"
                        + "studentLogin: " + studentLogin + "\n"
                        + "quickName: " + quickName + "\n"
                        + "part: " + part.getFullDisplayName());
            }
        }

        try {
            GroupGradingSheet gradingSheet = Allocator.getDataServices().getGroupGradingSheet(part, group);
            
            //Get the subsections for the grading sheet in order
            List<GradingSheetSubsection> subsections = new ArrayList<GradingSheetSubsection>();
            for (GradingSheetSection section : gradingSheet.getGradingSheet().getSections()) {
                subsections.addAll(section.getSubsections());
            }
            
            //Validate that either no points args given or the number of points args given is equal to the number of
            //subsections for the part
            if (!pointsArgs.isEmpty() && pointsArgs.size() != subsections.size()) {
                throw new EnterGradeException("You must enter a score for each subsection of the part, or no scores to "
                        + "give full credit for all subsections.  This part's grading sheet has " + subsections.size()
                        + " subsections, but you entered " + pointsArgs.size() + " scores.");
            }
            
            return enterPartGrade(student, gradingSheet, subsections, getPoints(pointsArgs), interactor);
        } catch (ServicesException ex) {
            throw new EnterGradeException(ex, "An error occurred when trying to enter grade for group [" 
                    + group.getName() + "]  on part [" + part.getFullDisplayName() + "].");
        }

        
    }
    
    private static EnterGradeResult enterPartGrade(Student student, GroupGradingSheet gradingSheet,
                                                   List<GradingSheetSubsection> subsections, List<Double> points,
                                                   EnterGradeInteractor interactor) throws EnterGradeException {
        //First confirm that there is either no pre-existing grade or that it is ok to overwrite the existing grade
        if (shouldEnterGrade(gradingSheet, interactor)) {
            //if no points args given, assign full credit for each subsection
            if (points.isEmpty()) {
                for (GradingSheetSubsection subsection : subsections) {
                    gradingSheet.setEarnedPoints(subsection, subsection.getOutOf());
                }
            }
            //otherwise, assign the given number of points for the subsection
            else {
                for (int i = 0; i < subsections.size(); i++) {
                    gradingSheet.setEarnedPoints(subsections.get(i), points.get(i));
                }
            }

            Part part = gradingSheet.getGradingSheet().getPart();
            try {
                //write the grade to the database
                Allocator.getDataServices().saveGroupGradingSheet(gradingSheet);

                //Confirm to the user that the grade has been entered for the student
                if (part.getGradableEvent().getAssignment().hasGroups()) {
                    interactor.println("Entered grade for part: " + part.getQuickName()
                            + " (" + part.getFullDisplayName() + ")");
                    interactor.println("Group name: " + gradingSheet.getGroup().getName());
                    interactor.println("Group members: " + gradingSheet.getGroup().getMembers());
                }
                else {
                    interactor.println("Entered grade for part: " + part.getQuickName()
                            + " (" + part.getFullDisplayName() + ")");
                    interactor.println("Student login: " + student.getLogin());
                }

                return EnterGradeResult.SUCCEEDED;
            } catch (ServicesException ex) {
                throw new EnterGradeException(ex, "Could not store grade for group ["
                        + gradingSheet.getGroup().getName() + "] on part [" + part.getFullDisplayName() + "] in the "
                        + "database.");
            }
        }
        else {
            return EnterGradeResult.ABORTED;
        }
    }
    
    /**
     * Returns the Student object corresponding to the student login, throws a EnterGradeException if the student
     * doesn't exist or is not enabled.
     *
     * @param studentLogin
     * @return
     * @throws EnterGradeException
     */
    private static Student getStudent(String studentLogin) throws EnterGradeException
    {
        try {
            Student student = Allocator.getDataServices().getStudentFromLogin(studentLogin);
            
            if (student == null) {
                throw new EnterGradeException("No such student [" + studentLogin + "].");
            }

            if (!Allocator.getDataServices().getEnabledStudents().contains(student)) {
                throw new EnterGradeException("Provided student login [" + studentLogin +
                            "] is not enabled. A cakehat admin may re-enable the student");
            }

            return student;
        } catch (ServicesException ex) {
            throw new EnterGradeException(ex, "Could not retrieve Student object abstraction.");
        }
    }
    
    /**
     * Retrieves the part that has quick name specified by {@code quickName}.
     *
     * @param quickName
     * @return
     * @throws EnterGradeException
     */
    private static Part getQuickNamePart(String quickName) throws EnterGradeException
    {
        HashMap<String, Part> nameToPart = new HashMap<String, Part>();
        for (Assignment asgn : Allocator.getDataServices().getAssignments()) {
            for (GradableEvent ge : asgn.getGradableEvents()) {
                for (Part part : ge.getParts()) {
                    if (part.hasQuickName()) {
                        nameToPart.put(part.getQuickName(), part);
                    }
                }
            }
        }

        if(!nameToPart.containsKey(quickName))
        {
            throw new EnterGradeException("Provided quick name [" + quickName + "] is not a valid quick name. " +
                                          "Valid quick names are: " + nameToPart.keySet().toString());
        }

        return nameToPart.get(quickName);
    }
    
    private static List<Double> getPoints(List<String> pointsStrings) throws EnterGradeException {
        List<Double> points = new ArrayList<Double>(pointsStrings.size());
        
        for (String pointsString : pointsStrings) {
            points.add(getPoints(pointsString));
        }
        
        return points;
    }
    
    /**
     * Parses out the value represented by {@code pointsStr}.  If {@code pointsStr} is {@code --}, the value returned 
     * will be {@code null}.
     *
     * @param pointsStr - may not be {@code null} or the empty string
     * @param part
     * @return
     * @throws EnterGradeException
     */
    private static Double getPoints(String pointsStr) throws EnterGradeException {
        Double pointsNum = null;
        if(!pointsStr.equals("--")) {
            try
            {
                pointsNum = Double.valueOf(pointsStr);
            }
            catch(NumberFormatException e)
            {
                throw new EnterGradeException("Specified points value [" + pointsStr + "] is not a number");
            }
        }

        return pointsNum;
    }
    
    /**
     * Retrieves the student's group for the assignment the part belongs to.
     * 
     * @param student
     * @param part
     * @return
     * @throws EnterGradeException
     */
    private static Group getStudentsGroup(Student student, Part part) throws EnterGradeException {
        try {
            return Allocator.getDataServices().getGroup(part.getGradableEvent().getAssignment(), student);
        } catch (ServicesException e) {
            throw new EnterGradeException(e, "Cannot retrieve student's group");
        }
    }

    /**
     * Determines whether the grade should be entered. If no grade already exists then {@code true} will be returned. If
     * a grade already exists the user will be asked whether they want to overwrite the grade, unless the grade has
     * already been submitted (in which case {@code false} will be returned.
     *
     * @param gradingSheet
     * @param responder
     * @return
     * @throws EnterGradeException
     */
    private static boolean shouldEnterGrade(GroupGradingSheet gradingSheet, EnterGradeInteractor responder)
            throws EnterGradeException {
        if (gradingSheet.isSubmitted()) {
            responder.println("Grade has already been submitted for group [" + gradingSheet.getGroup().getName() + "] "
                    + "on part [" + gradingSheet.getGradingSheet().getPart().getFullDisplayName() + ".\nEntering grade "
                    + "aborted");
            return false;
        }
        
        boolean enterGrade = true;
        
        //Determine whether a grade has already been entered for
        boolean gradeExistsForSomeSubsection = false;
        for (GroupSubsectionEarned earnedRecord : gradingSheet.getEarnedPoints().values()) {
            if (earnedRecord.getEarned() != null) {
                gradeExistsForSomeSubsection = true;
            }
        }

        //If there is an existing grade
        if (gradeExistsForSomeSubsection) {
            if (gradingSheet.getGradingSheet().getPart().getAssignment().hasGroups()) {
                responder.println("This group has already had a grade entered for this part");
                responder.println("Group members: " + gradingSheet.getGroup().getMembers());
            }
            else {
                responder.println("This student has already had a grade entered for this part");
            }
            responder.println("Existing part score: " + gradingSheet.getEarned());
            responder.print("Would you like to overwrite this score? (y/n) ");

            enterGrade = responder.shouldOverwriteScore();

            responder.println("");

            if (!enterGrade) {
                responder.println("Entering grade aborted");
            }
        }
        
        return enterGrade;
    }
    
    /**
     * Interacts with the messages sent to and requests made. This is only designed to be used for testing.
     */
    static interface EnterGradeInteractor
    {
        public boolean shouldOverwriteScore();
        public void println(String msg);
        public void print(String msg);
    }

    /**
     * The default interaction that makes use of the command prompt.
     */
    private static class DefaultInteractor implements EnterGradeInteractor
    {
        @Override
        public boolean shouldOverwriteScore()
        {
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine();
            boolean overwrite = (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y"));

            return overwrite;
        }

        @Override
        public void println(String msg)
        {
            System.out.println(msg);
        }

        @Override
        public void print(String msg)
        {
            System.out.print(msg);
        }
    }
    
    /**
     * An exception that occurs while entering grade for a student or group. This exception should never be interacted
     * with outside of this class except when running unit tests against it.
     *
     * @author jak2
     */
    static class EnterGradeException extends Exception
    {
        private final String[] _messages;

        public EnterGradeException(String... messages)
        {
            _messages = messages;
        }

        public EnterGradeException(Throwable cause, String... messages)
        {
            super(cause);

            _messages = messages;
        }

        public void printToConsole()
        {
            System.err.println("Entering the grade failed");

            for(String line : _messages)
            {
                System.err.println(line);
            }

            Throwable cause = super.getCause();
            if(cause != null)
            {
                System.err.println("");
                System.err.println("Underlying exception:");
                cause.printStackTrace(System.err);
            }
        }
    }

    /**
     * The result of entering grade. Only used for testing.
     */
    static enum EnterGradeResult
    {
        SUCCEEDED, ABORTED;
    }
}
