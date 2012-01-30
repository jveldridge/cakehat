package cakehat.services;

import cakehat.Allocator;
import cakehat.newdatabase.TA;
import java.util.List;
import support.utils.posix.NativeException;

public class UserServicesImpl implements UserServices
{
    private TA _user;
    
    @Override
    public TA getUser() throws ServicesException
    {
        if(_user == null)
        {
            _user = Allocator.getDataServicesV5().getTA(Allocator.getUserUtilities().getUserId());
        }

        return _user;
    }

    @Override
    public boolean isInStudentGroup(String studentLogin) throws NativeException
    {
        return Allocator.getUserUtilities().isMemberOfGroup(studentLogin, Allocator.getCourseInfo().getStudentGroup());
    }

    @Override
    public boolean isInTAGroup(String taLogin) throws NativeException
    {
        return Allocator.getUserUtilities().isMemberOfGroup(taLogin, Allocator.getCourseInfo().getTAGroup());
    }

    @Override
    public List<String> getStudentLogins() throws NativeException
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getStudentGroup());
    }
    
    @Override
    public List<String> getTALogins() throws NativeException
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getTAGroup());
    }
    
    @Override
    public List<String> getHTALogins() throws NativeException
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getHTAGroup());
    }

}