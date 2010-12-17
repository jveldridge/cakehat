package gradesystem.services;

import gradesystem.Allocator;
import javax.swing.JOptionPane;

/**
 *
 * @author jak2
 */
public class StudentServices
{
    public enum ValidityCheck {BYPASS, CHECK};

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login (i.e., is not snoopable)
     * or is not in the course's student group; the user will then have the option
     * of adding the student anyway or cancelling the operation.  The user's
     * first and last name will be set to the firstName and lastName parameters,
     * respectively.
     *
     * @param studentLogin
     * @param firstName
     * @param lastName
     * @param checkValidity parameter that indicates whether the student should be
     *                    added to the database without checking that the login is
     *                    valid and that the the student is in the course student
     *                    group.  This should be passed as BYPASS when both of
     *                    these conditions are known to be true (for example, when
     *                    adding all members of the course group)
     */
    public void addStudent(String studentLogin, String firstName,
            String lastName, ValidityCheck checkValidity)
    {

        if (checkValidity == ValidityCheck.CHECK)
        {
            boolean isLoginValid = Allocator.getUserUtilities().isLoginValid(studentLogin);
            boolean isInStudentGroup = Allocator.getUserUtilities().isInStudentGroup(studentLogin);

            String warningMessage = "";
            if (!isLoginValid)
            {
                warningMessage += String.format("The login %s is not a valid (snoopable) login\n",
                        studentLogin);
            }
            else if (!isInStudentGroup)
            {
                warningMessage += String.format("The login %s is not in the student group",
                        studentLogin);
            }

            if (!isLoginValid || !isInStudentGroup)
            {
                Object[] options = {"Proceed", "Cancel"};
                int shouldContinue = JOptionPane.showOptionDialog(null, warningMessage,
                        "Invalid Student Login",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);

                if (shouldContinue != JOptionPane.OK_OPTION)
                {
                    return;
                }
            }
        }

        Allocator.getDatabaseIO().addStudent(studentLogin, firstName, lastName);
    }
}