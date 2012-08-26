package cakehat.email;

import cakehat.Allocator;
import cakehat.InitializationException;
import cakehat.email.EmailManager.EmailAccountStatus;
import cakehat.database.DbNotifyAddress;
import cakehat.database.DbPropertyValue;
import cakehat.database.DbPropertyValue.DbPropertyKey;
import com.google.common.collect.ImmutableSet;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Set;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

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
        try
        {
            DbPropertyValue<String> accountProp = Allocator.getDatabase().getPropertyValue(DbPropertyKey.EMAIL_ACCOUNT);
            DbPropertyValue<String> passwordProp = Allocator.getDatabase().getPropertyValue(DbPropertyKey.EMAIL_PASSWORD);
            String emailLogin = accountProp == null ? null : accountProp.getValue();
            String emailPassword = passwordProp == null ? null : passwordProp.getValue();
                    
            if(emailLogin == null || emailLogin.isEmpty() || emailPassword == null || emailPassword.isEmpty())
            {
                _account = null;
                _status = EmailAccountStatus.NOT_CONFIGURED;
            }
            else
            {
                try
                {
                    _account = new EmailAccount(emailLogin, emailPassword);
                    _status = EmailAccountStatus.AVAILABLE;
                }
                catch(GeneralSecurityException ex)
                {
                    throw new InitializationException("Unable to create the email account. " +
                            "cakehat will be unable to send email", ex);
                }
            }
        }
        catch(SQLException ex)
        {
            throw new InitializationException("Unable to create the email account. " +
                            "cakehat will be unable to send email", ex);
        }
        
        //Notify addresses
        try
        {
            Set<DbNotifyAddress> dbAddresses = Allocator.getDatabase().getNotifyAddresses();
            
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
                    throw new InitializationException("Unable to construct an email address from: " +
                            address.getAddress() + "\n" +
                            "Please contact the cakehat team - your course's database has become corrupted.", ex);
                }
            }
            _addresses = addressesBuilder.build();
        }
        catch(SQLException ex)
        {
            throw new InitializationException("Unable to retrieve notify addresses from database", ex);
        }
    }
    
    @Override
    public InternetAddress getCakehatEmailAddress()
    {
        try
        {
            return new InternetAddress("cakehat@cs.brown.edu");
        }
        catch(AddressException ex)
        {
            throw new RuntimeException("Unable to construct email address for cakehat@cs.brown.edu", ex);
        }
    }
    
    @Override
    public InternetAddress getHeadTAsEmailAddress()
    {
        try
        {
            return new InternetAddress(Allocator.getCourseInfo().getCourse() + "headtas@" +
                    Allocator.getConstants().getEmailDomain());
        }
        catch (AddressException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public EmailAccountStatus getEmailAccountStatus()
    {
        return _status;
    }
    
    @Override
    public void send(InternetAddress from,
                     Iterable<InternetAddress> to, Iterable<InternetAddress> cc, Iterable<InternetAddress> bcc,
                     String subject, String body, Iterable<? extends DataSource> attachments) throws MessagingException
    {
        if(_status == EmailAccountStatus.AVAILABLE)
        {
            _account.send(from, to, cc, bcc, subject, body, attachments);
        }
        else if(_status == EmailAccountStatus.NOT_CONFIGURED)
        {
            throw new MessagingException("Email has not been configured by your course");
        }
        else
        {
            throw new IllegalStateException("Illegal email account status state: " + _status);
        }
    }
    
    @Override
    public Set<InternetAddress> getNotifyAddresses()
    {
        return _addresses;
    }
}