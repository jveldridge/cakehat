package cakehat.email;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

/**
 * Manages sending email and addresses to be notified.
 *
 * @author jak2
 */
public interface EmailManager
{
    public enum EmailAccountStatus
    {
        /**
         * The account has been configured and is ready to be used. No verification of the email credentials has been
         * done so sending email could still fail.
         */
        AVAILABLE,
        
        /**
         * The account has not been configured.
         */
        NOT_CONFIGURED,
        
        /**
         * The account could not be created due to an initialization error. This is not user error.
         */
        INITIALIZATION_ERROR;
    }
    
    public InternetAddress getCakehatEmailAddress();
    
    public InternetAddress getHeadTAsEmailAddress();
    
    public EmailAccountStatus getEmailAccountStatus();
    
    /**
     * Sends an email as HTML if {@link #getEmailAccountStatus()} returns {@link EmailAccountStatus#AVAILABLE}. If
     * {@link EmailAccountStatus#NOT_CONFIGURED} is returned the user will be shown a modal message dialog that states
     * that the course has not configured the email account information. If
     * {@link EmailAccountStatus#INITIALIZATION_ERROR} is returned the user will be shown a modal message dialog that
     * explains that an error has occurred.
     *
     * @param from may not be {@code null}
     * @param to may be {@code null}
     * @param cc may be {@code null}
     * @param bcc may be {@code null}
     * @param subject may be {@code null}, if {@code null} the empty string will be used
     * @param body may be {@code null}, if {@code null} the empty string will be used
     * @param attachments may be {@code null}
     * 
     * @throws NullPointerException if {@code from} is {@code null}
     * @throws MessagingException if unable to send message
     */
    public void send(InternetAddress from,
                     Iterable<InternetAddress> to, Iterable<InternetAddress> cc, Iterable<InternetAddress> bcc,
                     String subject, String body, Iterable<File> attachments) throws MessagingException;
    
    /**
     * Gets an immutable set of {@link InternetAddress}es that are to be notified.
     * 
     * @return addresses
     */
    public ImmutableSet<InternetAddress> getNotifyAddresses();
}