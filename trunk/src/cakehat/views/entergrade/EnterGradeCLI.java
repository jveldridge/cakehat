package cakehat.views.entergrade;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.DbGroup;
import cakehat.database.Group;
import cakehat.database.PartGrade;
import cakehat.database.Student;
import cakehat.services.ServicesException;
import java.util.HashMap;
import java.util.Iterator;
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
     * Enters grade for a student or group for the part as specified by
     * <code>args</code>. <code>args</code> is all but the first of the
     * arguments passed to the cakehat mainline. The expected format
     * of the arguments is:
     * <br/>
     * First entry: part quick name <br/>
     * Second entry: student login <br/>
     * Third entry: points given (optional) <br/>
     * <br/>
     * Thorough data validation occurs, so the code will respond appropriately
     * if the data is not in the expected format.
     *
     * @param args
     */
    public static void performEnterGrade(List<String> args)
    {
        try
        {
            performEnterGrade(args, new DefaultInteractor());
        }
        catch(EnterGradeException e)
        {
            e.printToConsole();
        }
    }

    /**
     * This method is for unit testing purposes. It behaves identically to
     * {@link #performEnterGrade(java.util.List) } except that it does not
     * print its results to the terminal.
     *
     * @param args
     * @param interactor
     * @throws gradesystem.views.entergrade.EnterGradeCLI.EnterGradeException
     */
    static EnterGradeResult performEnterGrade(List<String> args,
            EnterGradeInteractor interactor) throws EnterGradeException
    {
        String partName = null;
        String studentLogin = null;
        String pointsStr = null;

        //Giving the student's group full credit for the part
        if(args.size() == 2)
        {
            partName = args.get(0);
            studentLogin = args.get(1);
        }
        //Giving the student's group a specific value for the part
        else if(args.size() == 3)
        {
            partName = args.get(0);
            studentLogin = args.get(1);
            pointsStr = args.get(2);
        }
        //Illegal number of arguments
        else
        {
            throw new EnterGradeException(
                    "Usage: cakehat_enterGrade [quick name] [student login] OR",
                    "       cakehat_enterGrade [quick name] [student login] [points earned]");
        }

        return enterPartGrade(partName, studentLogin, pointsStr, interactor);
    }
    
    /**
     * Enters the part grade for the student. Verification is performed
     * throughout.
     *
     * @param quickName
     * @param studentLogin
     * @param pointsStr may be <code>null</code>, if so the full value of the
     * part will be used
     * @param interactor
     * @throws EnterGradeException
     * @return EnterGradeResult
     */
    private static EnterGradeResult enterPartGrade(String quickName,
            String studentLogin, String pointsStr,
            EnterGradeInteractor interactor) throws EnterGradeException
    {
        //Verify student login
        Student student = getStudent(studentLogin);

        //Load part
        Part part = getQuickNamePart(quickName);

        //Determine the number of points to give the student's group
        double pointsNum = getPoints(pointsStr, part);

        //Get the student's group
        Group group = getStudentsGroup(student, part);

        //If no group exists for this student and assignment pairing
        if(group == null)
        {
            //There must be group if the assignment has groups
            if(part.getGradableEvent().getAssignment().hasGroups())
            {
                throw new EnterGradeException("This part belongs to an assignment that requires groups. The provided "
                                        + "student login [" + student.getLogin() + "] has no group for this assignment "
                                        + "[" + part.getGradableEvent().getAssignment().getName() + "]");
            }
            //this should never happen
            else {
                throw new EnterGradeException("You've found a bug in our code.");
            }
        }        
        
        //Enter grade, but first confirm that there is either no pre-existing
        //grade or that it is ok to overwrite the existing grade
        if(shouldEnterGrade(part, group, interactor))
        {
            try
            {
                Allocator.getDataServices().setEarned(group, part, pointsNum, true);
            }
            catch(ServicesException e)
            {
                throw new EnterGradeException(e, "Internal database error occurred (unable to enter grade)");
            }

            //Confirm the grade has been entered for the student
            if(part.getGradableEvent().getAssignment().hasGroups())
            {
                interactor.println("Entered grade for part: " + part.getQuickName() 
                                                                 + " ("+ part.getFullDisplayName() + ")");
                interactor.println("Group name: " + group.getName());
                interactor.println("Group members: " + group.getMembers());
            }
            else
            {
                interactor.println("Entered grade for part: " + part.getQuickName() 
                                                                 + " ("+ part.getFullDisplayName() + ")");
                interactor.println("Student login: " + student.getLogin());
            }

            //If the number of points was specified, confirm number of points they
            //gave the group
            if(pointsStr != null)
            {
                interactor.println("Points given: " + pointsNum);
            }

            return EnterGradeResult.SUCCEEDED;
        }
        else
        {
            return EnterGradeResult.ABORTED;
        }
    }
    
    /**
     * Returns the Student object corresponding to the student login, throws a
     * EnterGradeException if the student doesn't exist or is not enabled.
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
     * Retrieves the part that has quick name specified by <code>quickName</code>.
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
            throw new EnterGradeException("Provided quick name [" + quickName + "] is not a valid quick name. "
                                        + "Valid quick names are: " + nameToPart.keySet().toString());
        }

        return nameToPart.get(quickName);
    }
    
    /**
     * Parses out the value represented by <code>pointsStr</code>, and verifies
     * that it does not exceed the points value for the part. If
     * <code>pointsStr</code> is null, the total value of the part will be used.
     *
     * @param pointsStr
     * @param part
     * @return
     * @throws EnterGradeException
     */
    private static double getPoints(String pointsStr, Part part) throws EnterGradeException
    {
        double pointsNum;
        if(pointsStr == null)
        {
            pointsNum = part.getOutOf();
        }
        else
        {
            try
            {
                pointsNum = Double.valueOf(pointsStr);
            }
            catch(NumberFormatException e)
            {
                throw new EnterGradeException("Specified points value [" + pointsStr + "] is not a number");
            }

            if(pointsNum > part.getOutOf())
            {
                throw new EnterGradeException("Specified points value [" + pointsNum +
                        "] exceeds part value [" +part.getOutOf() + "]");
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
    private static Group getStudentsGroup(Student student, Part part) throws EnterGradeException
    {
        Group group = null;
        try {
            group = Allocator.getDataServices().getGroup(part.getGradableEvent().getAssignment(), student);

        } catch (ServicesException e) {
            throw new EnterGradeException(e, "Cannot retrieve student's group");
        }

        return group;
    }
    
    /**
     * Determines whether the grade should be entered. If no grade already
     * exists then <code>true</code> will be returned. If a grade already exists
     * the user will be asked whether they want to overwrite the grade.
     *
     * @param part
     * @param group
     * @param responder
     * @return
     * @throws EnterGradeException
     */
    private static boolean shouldEnterGrade(Part part, Group group, EnterGradeInteractor responder) 
            throws EnterGradeException
    {
        boolean enterGrade = true;

        try
        {
            PartGrade partGrade = Allocator.getDataServices().getEarned(group, part);

            //If there is an existing grade
            if(partGrade != null && partGrade.getEarned() != null)
            {
                if(part.getGradableEvent().getAssignment().hasGroups())
                {
                    responder.println("This group has already had a grade entered for this part");
                    responder.println("Group members: " + group.getMembers());
                }
                else
                {
                    responder.println("This student has already had a grade entered for this part");
                }
                responder.println("Existing score: " + partGrade.getEarned());
                responder.print("Would you like to overwrite this score? (y/n) ");

                enterGrade = responder.shouldOverwriteScore();

                responder.println("");

                if(!enterGrade)
                {
                    responder.println("Entering grade aborted");
                }
            }
        }
        catch(ServicesException e)
        {
            throw new EnterGradeException(e, "Unable to determine if pre-existing grade exists");
        }

        return enterGrade;
    }
    
    /**
     * Interacts with the messages sent to and requests made. This is only
     * designed to be used for testing.
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
    static class DefaultInteractor implements EnterGradeInteractor
    {
        public boolean shouldOverwriteScore()
        {
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine();

            boolean overwrite = (response.equalsIgnoreCase("yes") ||
                    response.equalsIgnoreCase("y"));

            return overwrite;
        }

        public void println(String msg)
        {
            System.out.println(msg);
        }

        public void print(String msg)
        {
            System.out.print(msg);
        }
    }
    
    /**
     * An exception that occurs while entering grade for a student or group. This
     * exception should never be interacted with outside of this class except
     * when running unit tests against it.
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
                cause.printStackTrace();
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
