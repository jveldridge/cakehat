package cakehat.services;

import cakehat.Allocator;
import cakehat.CakehatSession;
import cakehat.database.TA;
import java.util.Set;
import support.utils.posix.NativeException;

public class UserServicesImpl implements UserServices
{   
    @Override
    public TA getUser() 
    {
        return Allocator.getDataServices().getTA(CakehatSession.getUserId());
    }

    @Override
    public boolean isInStudentGroup(String studentLogin) throws NativeException
    {
        return Allocator.getUserUtilities().isMemberOfGroup(studentLogin, Allocator.getCourseInfo().getStudentGroup());
    }

    @Override
    public Set<String> getStudentLogins() throws NativeException
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getStudentGroup());
    }
    
    @Override
    public Set<String> getTALogins() throws NativeException
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getTAGroup());
    }
    
    @Override
    public Set<String> getHTALogins() throws NativeException
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getHTAGroup());
    }
}