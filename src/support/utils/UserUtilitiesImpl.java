package support.utils;

import java.util.HashMap;
import java.util.List;
import support.utils.posix.NativeException;
import support.utils.posix.NativeFunctions;

public class UserUtilitiesImpl implements UserUtilities
{
    private final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();
    private final HashMap<String, List<String>> GROUP_MEMBERS = new HashMap<String, List<String>>();
    private final String USER_LOGIN = NATIVE_FUNCTIONS.getUserLogin();
    private final int USER_ID = NATIVE_FUNCTIONS.getUserId();

    public List<String> getMembers(String group) throws NativeException
    {
        List<String> members = GROUP_MEMBERS.get(group);

        if(!GROUP_MEMBERS.containsKey(group))
        {
            members = NATIVE_FUNCTIONS.getGroupMembers(group);
            GROUP_MEMBERS.put(group, members);
        }

        return members;
    }
    
    public int getUserId(String login) throws NativeException
    {
        return NATIVE_FUNCTIONS.getUserId(login);
    }
    
    public int getUserId()
    {
        return USER_ID;
    }
    
    public String getUserLogin(int userId) throws NativeException
    {
        return NATIVE_FUNCTIONS.getUserLogin(userId);
    }
    
    public String getUserLogin()
    {
        return USER_LOGIN;
    }

    public String getUserName(String login) throws NativeException
    {
        return NATIVE_FUNCTIONS.getRealName(login);
    }

    public boolean isLoginValid(String login)
    {
        return NATIVE_FUNCTIONS.isLogin(login);
    }

    public boolean isMemberOfGroup(String login, String group) throws NativeException
    {
        boolean isMember = false;
        List<String> logins = this.getMembers(group);

        if(logins != null)
        {
            isMember = logins.contains(login);
        }

        return isMember;
    }

    //Cache the result of this method as the value will not change and it is
    //slightly expensive (~100 milliseconds) to compute
    private Boolean _isRemote = null;
    public boolean isUserRemotelyConnected() throws NativeException
    {
        if(_isRemote == null)
        {
            _isRemote = NATIVE_FUNCTIONS.isUserRemotelyConnected();
        }

        return _isRemote;
    }
}