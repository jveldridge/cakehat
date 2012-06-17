package support.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import support.utils.posix.NativeException;
import support.utils.posix.NativeFunctions;

public class UserUtilitiesImpl implements UserUtilities
{
    private final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();
    private final ConcurrentHashMap<String, Set<String>> GROUP_MEMBERS = new ConcurrentHashMap<String, Set<String>>();
    private final String USER_LOGIN = NATIVE_FUNCTIONS.getUserLogin();
    private final int USER_ID = NATIVE_FUNCTIONS.getUserId();

    @Override
    public Set<String> getMembers(String group) throws NativeException
    {
        Set<String> members = GROUP_MEMBERS.get(group);

        if(members == null)
        {
            members = NATIVE_FUNCTIONS.getGroupMembers(group);
            GROUP_MEMBERS.put(group, members);
        }

        return members;
    }
    
    @Override
    public int getUserId(String login) throws NativeException
    {
        return NATIVE_FUNCTIONS.getUserId(login);
    }
    
    @Override
    public int getUserId()
    {
        return USER_ID;
    }
    
    @Override
    public String getUserLogin(int userId) throws NativeException
    {
        return NATIVE_FUNCTIONS.getUserLogin(userId);
    }
    
    @Override
    public String getUserLogin()
    {
        return USER_LOGIN;
    }

    @Override
    public String getUserName(String login) throws NativeException
    {
        return NATIVE_FUNCTIONS.getRealName(login);
    }

    @Override
    public boolean isLoginValid(String login)
    {
        return NATIVE_FUNCTIONS.isLogin(login);
    }

    @Override
    public boolean isMemberOfGroup(String login, String group) throws NativeException
    {
        return this.getMembers(group).contains(login);
    }

    //Cache the result of this method as the value will not change and it is expensive (~100 milliseconds) to compute
    private volatile Boolean _isRemote = null;
    
    @Override
    public boolean isUserRemotelyConnected() throws NativeException
    {
        if(_isRemote == null)
        {
            _isRemote = NATIVE_FUNCTIONS.isUserRemotelyConnected();
        }

        return _isRemote;
    }
}