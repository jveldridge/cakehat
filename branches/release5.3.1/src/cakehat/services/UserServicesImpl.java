package cakehat.services;

import cakehat.Allocator;
import cakehat.database.TA;
import java.util.List;
import support.utils.posix.NativeException;

public class UserServicesImpl implements UserServices
{   
    @Override
    public TA getUser() 
    {
        return Allocator.getDataServices().getTA(Allocator.getUserUtilities().getUserId());
    }

    @Override
    public boolean isInStudentGroup(String studentLogin) throws NativeException
    {
        return Allocator.getUserUtilities().isMemberOfGroup(studentLogin, Allocator.getCourseInfo().getStudentGroup());
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