package cakehat.email;

import cakehat.Allocator;
import cakehat.email.EmailManager.EmailAccountStatus;
import cakehat.newdatabase.DbNotifyAddress;
import cakehat.newdatabase.DbPropertyValue.DbPropertyKey;
import cakehat.views.shared.ErrorView;
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
    private final ImmutableSet<InternetAddress> _addresses;
    
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
            String emailLogin = Allocator.getDatabaseV5().getPropertyValue(DbPropertyKey.EMAIL_ACCOUNT).getValue();
            String emailPassword = Allocator.getDatabaseV5().getPropertyValue(DbPropertyKey.EMAIL_PASSWORD).getValue();
                    
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
        ImmutableSet<InternetAddress> addresses;
        try
        {
            Set<DbNotifyAddress> dbAddresses = Allocator.getDatabaseV5().getNotifyAddresses();
            
            ImmutableSet.Builder<InternetAddress> addressesBuilder = ImmutableSet.builder();
            for(DbNotifyAddress address : dbAddresses)
            {
                //The configuration manager should prevent email addresses with invalid formatting from being stored
                //so if there is an exception when constructing then something went wrong along the way
                try
                {
                    addressesBuilder.add(new InternetAddress(address.getAddress(), true));
                }
                catch(AddressException ex)
                {
                    new ErrorView(ex, "Unable to construct an email address from: " + address.getAddress());
                }
            }
            addresses = addressesBuilder.build();
        }
        catch(SQLException ex)
        {
            addresses = ImmutableSet.<InternetAddress>of();
            
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
    public ImmutableSet<InternetAddress> getNotifyAddresses()
    {
        return _addresses;
    }
}