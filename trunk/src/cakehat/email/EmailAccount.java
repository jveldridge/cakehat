package cakehat.email;

import com.sun.mail.util.MailSSLSocketFactory;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Sends email from a Brown CS account using LDAP over secure SMTP. LDAP credentials should be kept course confidential
 * and should not be distributed in any form.
 * <br/><br/>
 * This class is public so that it may be used for verification of email credentials when using the configuration
 * manager. Otherwise, this class should be used indirectly via {@link EmailManager}.
 * 
 * @author jak2
 */
public class EmailAccount
{
    /**
     * The address of the Brown CS email SMTP server.
     */
    private static final String EMAIL_HOST = "smtp.gmail.com";
    
    /**
     * The port of the Brown CS SMTP server.
     */
    private static final String EMAIL_PORT = "465";
    
    /**
     * Properties used to establish an email session.
     */
    private final Properties _properties;
    
    /**
     * Provider of the login and password to the email session.
     */
    private final Authenticator _authenticator;
    
    /**
     * Constructs an {@code EmailAccount} that can send emails using the credentials passed to this constructor.
     * However, emails can be sent as if coming from anyone.
     * <br/><br/>
     * The {@code login} is a Brown CS login such as {@code jak2} or {@code cs015000}. A course is strongly encouraged
     * to use a test account for this.
     * <br/>
     * The LDAP password for the account specified by {@code login}. This password is set by being logged in as the
     * user specified by {@code login}, running the {@code ldappasswd} command and setting a password. By default a
     * Brown CS account does not have an LDAP password.
     * <br/>
     * This information was retrieved from <a href="http://www.cs.brown.edu/system/accounts/passwords.html">
     * http://www.cs.brown.edu/system/accounts/passwords.html</a> on 12/16/2011.
     * <br/><br/>
     * This constructor is public so that it may be used for verification of email credentials when using the
     * configuration manager. Otherwise, this class should be used indirectly via {@link EmailManager}.
     * 
     * @param login a Brown CS login, may not be {@code null}
     * @param ldapPassword the LDAP password belong to the user specified by {@code login}, may not be {@code null}
     * 
     * @throws GeneralSecurityException if unable to construct the socket factory used for sending email
     */
    public EmailAccount(String login, String ldapPassword) throws GeneralSecurityException
    {
        //Validation
        if(login == null)
        {
            throw new NullPointerException("login may not be null");
        }
        if(ldapPassword == null)
        {
            throw new NullPointerException("ldapPassword may not be null");
        }
        
        _properties = buildProperties(login);
        _authenticator = buildAuthenticator(login, ldapPassword);
    }

    /**
     * Sends an email as HTML.
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
                     String subject, String body, Iterable<? extends DataSource> attachments) throws MessagingException
    {
        //Validation
        if(from == null)
        {
            throw new NullPointerException("from address may not be null");
        }
        subject = (subject == null ? "" : subject);
        body = (body == null ? "" : body);
        body = (body.startsWith("<html>") ? body : "<html>" + body);
        body = (body.endsWith("</html>") ? body : body + "</html>");
        to = (to == null ? Collections.<InternetAddress>emptyList() : to);
        cc = (cc == null ? Collections.<InternetAddress>emptyList() : cc);
        bcc = (bcc == null ? Collections.<InternetAddress>emptyList() : bcc);
        attachments = (attachments == null ? Collections.<DataSource>emptyList() : attachments);
        
        //Session
        Session session = Session.getInstance(_properties, _authenticator);

        //Message
        MimeMessage msg = new MimeMessage(session);
        msg.setSubject(subject);
        msg.setFrom(from);
        
        //Recipients
        for(InternetAddress recipient : to)
        {
            msg.addRecipient(Message.RecipientType.TO, recipient);
        }
        for(InternetAddress recipient : cc)
        {
            msg.addRecipient(Message.RecipientType.CC, recipient);
        }
        for(InternetAddress recipient : bcc)
        {
            msg.addRecipient(Message.RecipientType.BCC, recipient);
        }

        //Multi-part message to add parts to
        Multipart multipart = new MimeMultipart();

        //Add body
        MimeBodyPart mainTextPart = new MimeBodyPart();
        mainTextPart.setContent(body, "text/html");
        multipart.addBodyPart(mainTextPart);

        //Attachments
        for(DataSource attachment : attachments)
        {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(attachment));
            attachmentPart.setFileName(attachment.getName());
            multipart.addBodyPart(attachmentPart);
        }

        //Put parts in message
        msg.setContent(multipart);

        //Send message
        Transport.send(msg);
    }
    
    /**
     * Properties which tell Java Mail how to talk to the Brown CS email server.
     *
     * @return
     */
    private static Properties buildProperties(String login) throws GeneralSecurityException
    {
        //Build properties
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtps");
        properties.put("mail.smtps.host", EMAIL_HOST);
        properties.put("mail.smtps.user", login);
        properties.put("mail.smtp.host", EMAIL_HOST);
        properties.put("mail.smtp.port", EMAIL_PORT);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.port", EMAIL_PORT);
        properties.put("mail.smtp.socketFactory.fallback", "false");
        //properties.put("mail.smtp.debug", "true"); //Leave this for debugging purposes
        
        //Use a socket factory which trust all of the hosts it connects to so that we do not need a Java KeyStore
        MailSSLSocketFactory socketFactory = new MailSSLSocketFactory();
        socketFactory.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.socketFactory", socketFactory);

        return properties;
    }

    /**
     * Authenticator that provides the Brown CS login and LDAP password.
     *
     * @return
     */
    private static Authenticator buildAuthenticator(final String login, final String ldapPassword)
    {
        Authenticator authenticator = new Authenticator()
        {
            @Override
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(login, ldapPassword);
            }
        };

        return authenticator;
    }
}