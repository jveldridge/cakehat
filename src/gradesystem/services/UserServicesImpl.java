package gradesystem.services;

import gradesystem.Allocator;
import gradesystem.config.TA;
import gradesystem.views.shared.ErrorView;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JOptionPane;
import utils.system.NativeException;

public class UserServicesImpl implements UserServices
{
    private final TA USER = Allocator.getCourseInfo().getTA(Allocator.getUserUtilities().getUserLogin());

    public void addStudent(String studentLogin, String firstName, String lastName,
            ValidityCheck checkValidity) throws ServicesException
    {
        if (checkValidity == ValidityCheck.CHECK)
        {
            String warningMessage = "";
            boolean isLoginValid = Allocator.getUserUtilities().isLoginValid(studentLogin);
            boolean isInStudentGroup = false;

            try
            {
                isInStudentGroup = this.isInStudentGroup(studentLogin);
            }
            catch(NativeException e)
            {
                throw new ServicesException("Unable to retrieve student group", e);
            }

            if (!isLoginValid)
            {
                warningMessage += String.format("The login %s is not a valid login\n",
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

        try {
            Allocator.getDatabaseIO().addStudent(studentLogin, firstName, lastName);
        } catch (SQLException e) {
            throw new ServicesException(String.format("Student %s (%s %s) could not " +
                                                      "be added to the database",
                                                      studentLogin, firstName, lastName),
                                        e);
        }
    }

    public void addStudent(String studentLogin, ValidityCheck checkValidity) throws ServicesException
    {
        try
        {
            String name = Allocator.getUserUtilities().getUserName(studentLogin);
            String names[] = name.split(" ");
            String firstName = names[0];
            String lastName = names[names.length - 1];

            this.addStudent(studentLogin, firstName, lastName, checkValidity);
        }
        catch(NativeException e)
        {
            new ErrorView(e, "Student will not be added to the database because " +
                    "the user's real name cannot be retrieved");
        }
    }
    
    public TA getUser() {
        return USER;
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

    public boolean isInStudentGroup(String studentLogin) throws NativeException
    {
        return Allocator.getUserUtilities().isMemberOfGroup(studentLogin, Allocator.getCourseInfo().getStudentGroup());
    }

    public boolean isInTAGroup(String taLogin) throws NativeException
    {
        return Allocator.getUserUtilities().isMemberOfGroup(taLogin, Allocator.getCourseInfo().getTAGroup());
    }

    public List<String> getStudentLogins() throws NativeException
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getStudentGroup());
    }

    public String getSanitizedTALogin(TA ta) {
        if (ta == null) {
            return "UNASSIGNED";
        }

        return ta.getLogin();
    }

    public String getSanitizedTAName(TA ta) {
        if (ta == null) {
            return "UNASSIGNED";
        }

        return ta.getName();
    }
}