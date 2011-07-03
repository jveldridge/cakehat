package cakehat.labcheckoff;

import cakehat.Allocator;
import cakehat.config.ConfigurationException;
import cakehat.config.LabConfigurationParser;
import cakehat.config.LabPart;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.services.ServicesException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * The command line interface for checking off labs.
 *
 * @author jak2
 */
public class CheckoffCLI
{
    private CheckoffCLI() { }

    /**
     * Checks off a student or group for the lab as specified by
     * <code>args</code>. <code>args</code> is all but the first of the
     * arguments passed to the cakehat mainline. The expected format
     * of the arguments is:
     * <br/>
     * First entry: lab number <br/>
     * Second entry: student login <br/>
     * Third entry: points given (optional) <br/>
     * <br/>
     * Thorough data validation occurs, so the code will respond appropriately
     * if the data is not in the expected format.
     *
     * @param args
     */
    public static void performCheckoff(List<String> args)
    {
        try
        {
            performCheckoff(args, new DefaultInteractor(), new LabConfigurationParser());
        }
        catch(CheckoffException e)
        {
            e.printToConsole();
        }
    }

    /**
     * This method is for unit testing purposes. It behaves identically to
     * {@link #performCheckoff(java.util.List) } except that it does not
     * print its results to the terminal.
     *
     * @param args
     * @param interactor
     * @param parser
     * @throws gradesystem.labcheckoff.CheckoffCLI.CheckoffException
     */
    static CheckoffResult performCheckoff(List<String> args,
            CheckoffInteractor interactor, LabConfigurationParser parser) throws CheckoffException
    {
        String labNumber = null;
        String studentLogin = null;
        String pointsStr = null;

        //Giving the student's group full credit for the lab
        if(args.size() == 2)
        {
            labNumber = args.get(0);
            studentLogin = args.get(1);
        }
        //Giving the student's group a specific value for the lab
        else if(args.size() == 3)
        {
            labNumber = args.get(0);
            studentLogin = args.get(1);
            pointsStr = args.get(2);
        }
        //Illegal number of arguments
        else
        {
            throw new CheckoffException(
                    "Usage: cakehat_labCheckOff [lab number] [student login] OR",
                    "       cakehat_labCheckOff [lab number] [student login] [points earned]");
        }

        return enterLabGrade(labNumber, studentLogin, pointsStr, interactor, parser);
    }

    /**
     * Enters the lab grade for the student. Verification is performed
     * throughout.
     *
     * @param labString
     * @param studentLogin
     * @param pointsStr may be <code>null</code>, if so the full value of the
     * lab will be used
     * @param interactor
     * @throws CheckoffException
     * @return CheckoffResult
     */
    private static CheckoffResult enterLabGrade(String labString,
            String studentLogin, String pointsStr,
            CheckoffInteractor interactor, LabConfigurationParser parser) throws CheckoffException
    {
        //Verify student login
        Student student = getStudent(studentLogin);

        //Load lab
        LabPart lab = getLabPart(labString, parser);

        //Determine the number of points to give the student's group
        double pointsNum = getPoints(pointsStr, lab);

        //Get the student's group
        Group group = getStudentsGroup(student, lab);

        //If no group exists for this student and assignment pairing
        if(group == null)
        {
            group = resolveMissingGroup(lab, student);
        }

        //Enter grade, but first confirm that there is either no pre-existing
        //grade or that it is ok to overwrite the existing grade
        if(shouldEnterGrade(lab, group, interactor))
        {
            try
            {
                Allocator.getDatabase().enterGrade(group, lab, pointsNum);
            }
            catch(SQLException e)
            {
                throw new CheckoffException(e, "Internal database error occurred (unable to enter grade)");
            }

            //Confirm the student has been checked off
            if(lab.getAssignment().hasGroups())
            {
                interactor.println("Checked off group for lab number " + lab.getLabNumber());
                interactor.println("Group name: " + group.getName());
                interactor.println("Group members: " + group.getMembers());
            }
            else
            {
                interactor.println("Checked off student for lab number " + lab.getLabNumber());
                interactor.println("Student login: " + student.getLogin());
            }

            //If the number of points was specified, confirm number of points they
            //gave the group
            if(pointsStr != null)
            {
                interactor.println("Points given: " + pointsNum);
            }

            return CheckoffResult.SUCCEEDED;
        }
        else
        {
            return CheckoffResult.ABORTED;
        }
    }

    /**
     * Resolves there existing no group for <code>studentLogin</code> for the
     * assignment belonging to <code>lab</code>. If the assignment is a group
     * assignment this will result in an exception being thrown. If this is not
     * a group assignment, a group for just that student will be made.
     *
     * @param lab
     * @param student
     * @return
     * @throws CheckoffException
     */
    private static Group resolveMissingGroup(LabPart lab, Student student) throws CheckoffException
    {
        Group group;

        //There must be group if the assignment has groups
        if(lab.getAssignment().hasGroups())
        {
            throw new CheckoffException("This lab belongs to an assignment " +
                    "that requires groups",
                    "The provided student login [" + student.getLogin() +
                    "] has no group for this assignment [" +
                    lab.getAssignment().getName() + "]");
        }
        //Groups are implicit for assignments without groups, so one should
        //be created
        else
        {
            group = new Group(student.getLogin(), student);
            try
            {
                Allocator.getDatabase().setGroup(lab.getAssignment(), group);
            }
            catch(SQLException e)
            {
                throw new CheckoffException(e, "Internal database error " +
                        "occurred (unable to set group abstraction)");
            }
        }

        return group;
    }

    /**
     * Returns the Student object coresponding to the student login, throws a
     * CheckoffException if the student doesn't exist or is not enabled.
     *
     * @param studentLogin
     * @return
     * @throws CheckoffException
     */
    private static Student getStudent(String studentLogin) throws CheckoffException
    {
        if (!Allocator.getDataServices().isStudentLoginInDatabase(studentLogin)) {
            throw new CheckoffException("No such student [" + studentLogin + "].");
        }

        Student student = Allocator.getDataServices().getStudentFromLogin(studentLogin);

        if (!student.isEnabled()) {
            throw new CheckoffException("Provided student login [" + studentLogin +
                        "] is not enabled. A cakehat admin may re-enable the student");
        }
        
        return student;
    }

    /**
     * Retrieves the lab part that has the lab number specified by
     * <code>labString</code>.
     *
     * @param labString
     * @param parser
     * @return
     * @throws CheckoffException
     */
    private static LabPart getLabPart(String labString,
            LabConfigurationParser parser) throws CheckoffException
    {
        int labNum;
        try
        {
            labNum = Integer.valueOf(labString);
        }
        catch(NumberFormatException e)
        {
            throw new CheckoffException("Lab number [" + labString + "] is not an integer");
        }

        LabPart lab;
        try
        {
            lab = parser.getLabPart(labNum);
        }
        catch(ConfigurationException e)
        {
            throw new CheckoffException(e,
                    "Error encountered when parsing configuration file");
        }
        if(lab == null)
        {
            throw new CheckoffException("Provided lab number [" + labNum + "] is not a valid lab number");
        }

        return lab;
    }

    /**
     * Parses out the value represented by <code>pointsStr</code>, and verifies
     * that it does not exceed the points value for the lab. If
     * <code>pointsStr</code> is null, the total value of the lab will be used.
     *
     * @param pointsStr
     * @param lab
     * @return
     * @throws CheckoffException
     */
    private static double getPoints(String pointsStr, LabPart lab) throws CheckoffException
    {
        double pointsNum;
        if(pointsStr == null)
        {
            pointsNum = lab.getPoints();
        }
        else
        {
            try
            {
                pointsNum = Double.valueOf(pointsStr);
            }
            catch(NumberFormatException e)
            {
                throw new CheckoffException("Specified points value [" + pointsStr + "] is not a number");
            }

            if(pointsNum > lab.getPoints())
            {
                throw new CheckoffException("Specified points value [" + pointsNum +
                        "] exceeds lab value [" +lab.getPoints() + "]");
            }
        }

        return pointsNum;
    }

    /**
     * Retrieves the student's group for the assignment the lab belongs to.
     * 
     * @param student
     * @param lab
     * @return
     * @throws CheckoffException
     */
    private static Group getStudentsGroup(Student student, LabPart lab) throws CheckoffException
    {
        Group group = null;
        try
        {
            group = Allocator.getDatabase().getStudentsGroup(lab.getAssignment(), student);

        }
        catch(SQLException e)
        {
            if(lab.getAssignment().hasGroups())
            {
                throw new CheckoffException(e, "Cannot retrieve student's group");
            }
            else
            {
                throw new CheckoffException(e, "Internal database error occurred " +
                        "(unable to load group abstraction)");
            }
        }

        return group;
    }

    /**
     * Determines whether the grade should be entered. If no grade already
     * exists then <code>true</code> will be returned. If a grade already exists
     * the user will be asked whether they want to overwrite the grade.
     *
     * @param lab
     * @param group
     * @param responder
     * @return
     * @throws CheckoffException
     */
    private static boolean shouldEnterGrade(LabPart lab, Group group,
            CheckoffInteractor responder) throws CheckoffException
    {
        boolean enterGrade = true;

        try
        {
            Double score = Allocator.getDatabase().getGroupScore(group, lab);

            //If there is an existing group
            if(score != null)
            {
                if(lab.getAssignment().hasGroups())
                {
                    responder.println("This group has already been checked off");
                    responder.println("Group members: " + group.getMembers());
                }
                else
                {
                    responder.println("This student has already been checked off");
                }
                responder.println("Existing score: " + score);
                responder.print("Would you like to overwrite this score? (y/n) ");

                enterGrade = responder.shouldOverwriteScore();

                responder.println("");

                if(!enterGrade)
                {
                    responder.println("Check off aborted");
                }
            }
        }
        catch(SQLException e)
        {
            throw new CheckoffException(e, "Unable to determine if pre-existing grade exists");
        }

        return enterGrade;
    }

    /**
     * An exception that occurs while checking off a student or group. This
     * exception should never be interacted with outside of this class except
     * when running unit tests against it.
     *
     * @author jak2
     */
    static class CheckoffException extends Exception
    {
        private final String[] _messages;

        public CheckoffException(String... messages)
        {
            _messages = messages;
        }

        public CheckoffException(Throwable cause, String... messages)
        {
            super(cause);

            _messages = messages;
        }

        public void printToConsole()
        {
            System.err.println("Check off failed");

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
     * The result of a checkoff. Only used for testing.
     */
    static enum CheckoffResult
    {
        SUCCEEDED, ABORTED;
    }

    /**
     * Interacts with the messages sent to and requests made. This is only
     * designed to be used for testing.
     */
    static interface CheckoffInteractor
    {
        public boolean shouldOverwriteScore();
        public void println(String msg);
        public void print(String msg);
    }

    /**
     * The default interaction that makes use of the command prompt.
     */
    static class DefaultInteractor implements CheckoffInteractor
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
}