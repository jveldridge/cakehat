package gradesystem.services;

import gradesystem.Allocator;
import gradesystem.config.TA;
import java.util.Collection;
import javax.swing.JOptionPane;

public class UserServicesImpl implements UserServices
{
    public void addStudent(String studentLogin, String firstName,
            String lastName, ValidityCheck checkValidity)
    {

        if (checkValidity == ValidityCheck.CHECK)
        {
            boolean isLoginValid = Allocator.getUserUtilities().isLoginValid(studentLogin);
            boolean isInStudentGroup = this.isInStudentGroup(studentLogin);

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

    public boolean isUserTA()
    {
        String userLogin = Allocator.getUserUtilities().getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getTAs())
        {
            if(ta.getLogin().equals(userLogin))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isUserAdmin()
    {
        String userLogin = Allocator.getUserUtilities().getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getTAs())
        {
            if(ta.getLogin().equals(userLogin) && ta.isAdmin())
            {
                return true;
            }
        }

        return false;
    }

    public boolean isUserHTA()
    {
        String userLogin = Allocator.getUserUtilities().getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getHTAs())
        {
            if(ta.getLogin().equals(userLogin))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isInStudentGroup(String studentLogin)
    {
        return Allocator.getUserUtilities().isMemberOfGroup(studentLogin, Allocator.getCourseInfo().getStudentGroup());
    }

    public boolean isInTAGroup(String taLogin)
    {
        return Allocator.getUserUtilities().isMemberOfGroup(taLogin, Allocator.getCourseInfo().getTAGroup());
    }

    public Collection<String> getStudentLogins()
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getStudentGroup());
    }
}