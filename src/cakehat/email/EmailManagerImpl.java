package cakehat.email;

import cakehat.email.EmailManager.EmailAccountStatus;
import cakehat.newdatabase.DbNotifyAddress;
import cakehat.newdatabase.DbPropertyValue;
import cakehat.newdatabase.DbPropertyValue.DbPropertyKey;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import support.ui.ModalDialog;

/**
 *
 * @author jak2
 */
public class EmailManagerImpl implements EmailManager
{
    private final EmailAccount _account;
    private final EmailAccountStatus _status;
    private final ImmutableCollection<InternetAddress> _addresses;
    
    public EmailManagerImpl()
    {
        //It's necessary to assign to local variables and then assign to the final instance variables due to the Java
        //compiler being unable to statically determine if the instance variables will be set due to the try/catch
        //blocks
        
        //Email account & status
        EmailAccount account;
        EmailAccountStatus status;
        try
        {
            //TODO: Retrieve information from the database
            String emailLogin = dummyDb_getPropertyValue(DbPropertyKey.EMAIL_ACCOUNT).getValue();
            String emailPassword = dummyDb_getPropertyValue(DbPropertyKey.EMAIL_PASSWORD).getValue();
                    
            if(emailLogin == null || emailLogin.isEmpty() || emailPassword == null || emailPassword.isEmpty())
            {
                account = null;
                status = EmailAccountStatus.NOT_CONFIGURED;
            }
            else
            {
                try
                {
                    account = new EmailAccount(emailLogin, emailPassword);
                    status = EmailAccountStatus.AVAILABLE;
                }
                catch(GeneralSecurityException ex)
                {
                    account = null;
                    status = EmailAccountStatus.INITIALIZATION_ERROR;

                    new ErrorView(ex, "Unable to create the email account. cakehat will be unable to send email");
                }
            }
        }
        catch(SQLException ex)
        {
            account = null;
            status = EmailAccountStatus.INITIALIZATION_ERROR;
            
            new ErrorView(ex, "Unable to create the email account. cakehat will be unable to send email");
        }
        _account = account;
        _status = status;
        
        //Notify addresses
        ImmutableCollection<InternetAddress> addresses;
        try
        {
            //TODO: Retrieve information from the database
            Set<DbNotifyAddress> dbAddresses = dummyDb_getNotifyAddresses();
            
            ImmutableList.Builder<InternetAddress> addressesBuilder = ImmutableList.builder();
            for(DbNotifyAddress address : dbAddresses)
            {
                String addressString = address.getAddress();
                
                //Ignore addresses that are empty strings
                if(!addressString.isEmpty())
                {
                    //The configuration manager should prevent email addresses with invalid formatting from being stored
                    //so if there is an exception when constructing then something went wrong along the way
                    try
                    {
                        addressesBuilder.add(new InternetAddress(addressString, true));
                    }
                    catch(AddressException ex)
                    {
                        new ErrorView(ex, "Unable to construct an email address from: " + address);
                    }
                }
            }
            addresses = addressesBuilder.build();
        }
        catch(SQLException ex)
        {
            addresses = ImmutableList.<InternetAddress>of();
            
            new ErrorView(ex, "Unable to retrieve notify addresses from database");
        }
        _addresses = addresses;
    }
    
    @Override
    public EmailAccountStatus getEmailAccountStatus()
    {
        return _status;
    }
    
    @Override
    public void send(InternetAddress from,
                     Iterable<InternetAddress> to, Iterable<InternetAddress> cc, Iterable<InternetAddress> bcc,
                     String subject, String body, Iterable<File> attachments) throws MessagingException
    {
        if(_status == EmailAccountStatus.AVAILABLE)
        {
            _account.send(from, to, cc, bcc, subject, body, attachments);
        }
        else if(_status == EmailAccountStatus.NOT_CONFIGURED)
        {
            ModalDialog.showMessage("Email Unavailable", "Email has not been configured by your course");
        }
        else if(_status == EmailAccountStatus.INITIALIZATION_ERROR)
        {
            ModalDialog.showMessage("Email Unavailable", "An error occurred while initializing the email account, " +
                    "cakehat is unable to send email. Please try restarting cakehat.");
        }
        else
        {
            throw new IllegalStateException("Illegal email account status state: " + _status);
        }
    }
    
    @Override
    public ImmutableCollection<InternetAddress> getNotifyAddresses()
    {
        return _addresses;
    }
    
    
    /******************************************************************************************************************\
    |*                                         Dummy Methods for Testing                                              *|
    \******************************************************************************************************************/
    
    
    private static <T> DbPropertyValue<T> dummyDb_getPropertyValue(DbPropertyKey<T> key) throws SQLException
    {
        if(key.equals(DbPropertyKey.EMAIL_ACCOUNT))
        {
            DbPropertyValue<String> prop = new DbPropertyValue<String>("fakelogn");
            
            return (DbPropertyValue<T>) prop;
        }
        else if(key.equals(DbPropertyKey.EMAIL_PASSWORD))
        {
            DbPropertyValue<String> prop = new DbPropertyValue<String>("s3cur3");
            
            return (DbPropertyValue<T>) prop;
        }
        
        throw new IllegalArgumentException("Unknown key: " + key);
    }
    
    private static Set<DbNotifyAddress> dummyDb_getNotifyAddresses() throws SQLException
    {
        return ImmutableSet.of(new DbNotifyAddress("foo@bar.com"));
    }
}