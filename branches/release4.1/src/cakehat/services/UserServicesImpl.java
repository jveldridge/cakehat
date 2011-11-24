package cakehat.services;

import cakehat.Allocator;
import cakehat.config.TA;
import java.util.List;
import support.utils.posix.NativeException;

public class UserServicesImpl implements UserServices
{
    private TA _user;
    
    public TA getUser()
    {
        if(_user == null)
        {
            _user = Allocator.getConfigurationInfo().getTA(Allocator.getUserUtilities().getUserLogin());
        }

        return _user;
    }

    public boolean isUserTA()
    {
        return (getUser() != null);
    }

    public boolean isUserAdmin()
    {
        return (getUser() != null) && getUser().isAdmin();
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