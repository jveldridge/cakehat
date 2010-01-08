package config;

import java.util.Collection;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import utils.Allocator;
import utils.ErrorView;

/**
 * The email account specified by the configuration file. Allows for sending
 * email from this account.
 *
 * @author jak2
 */
public class EmailAccount
{
    private String _login, _password, _certPath, _certPassword;

    private Properties _properties = null;
    private Authenticator _authenticator = null;

    EmailAccount() { }

    void setLogin(String login)
    {
        _login = login;
    }

    void setPassword(String pass)
    {
        _password = pass;
    }

    void setCertPath(String certPath)
    {
        _certPath = certPath;
    }

    void setCertPassword(String certPassword)
    {
        _certPassword = certPassword;
    }

    /**
     * Properties needed for email.
     *
     * @return
     */
    private Properties getProperties()
    {
        //Create properties if necessary
        if(_properties == null)
        {
            //Set system properties
            System.setProperty("javax.net.ssl.trustStore", _certPath);
            System.setProperty("javax.net.ssl.trustStorePassword", _certPassword);

            //Build properties
            _properties = new Properties();
            _properties.put("mail.transport.protocol", "smtps");
            _properties.put("mail.smtps.host", Allocator.getConstants().getEmailHost());
            _properties.put("mail.smtps.user", _login);
            _properties.put("mail.smtp.host", Allocator.getConstants().getEmailHost());
            _properties.put("mail.smtp.port", Allocator.getConstants().getEmailPort());
            _properties.put("mail.smtp.ssl.enable", "true");
            _properties.put("mail.smtp.starttls.enable", "true");
            _properties.put("mail.smtp.auth", "true");
            //props.put("mail.smtp.debug", "true");
            _properties.put("mail.smtp.socketFactory.port", Allocator.getConstants().getEmailPort());
            _properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            _properties.put("mail.smtp.socketFactory.fallback", "false");
        }

        return _properties;
    }

    /**
     * Authenticator need for email.
     *
     * @return
     */
    private Authenticator getAuthenticator()
    {
        //Create authenticator if necessary
        if(_authenticator == null)
        {
            _authenticator = new Authenticator()
            {
                @Override
                public PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(_login, _password);
                }
            };
        }

        return _authenticator;
    }

    private Session getSession()
    {
        return Session.getInstance(getProperties(), getAuthenticator());
    }


    /**
     * Sends email from the account specified in the configuration file;
     * however, can appear as if sent from another address.
     * 
     * @author jak2 1/7/10
     *
     * @param from
     * @param to collection of addresses - may be null
     * @param cc collection of addresses - may be null
     * @param bcc collection of addresses - may be null
     * @param subject
     * @param body
     * @param attachmentNames files paths to the attachments - may be null
     */
    public void sendMail(String from, Collection<String> to,
                         Collection<String> cc, Collection<String> bcc,
                         String subject, String body,
                         Collection<String> attachments)
    {
        String[] toArray = (to != null ? to.toArray(new String[0]) : null);
        String[] ccArray = (cc != null ? cc.toArray(new String[0]) : null);
        String[] bccArray = (bcc != null ? bcc.toArray(new String[0]) : null);
        String[] attachmentArray = (attachments != null ? attachments.toArray(new String[0]) : null);

        this.sendMail(from, toArray, ccArray, bccArray, subject, body, attachmentArray);
    }

    /**
     * Sends email from the account specified in the configuration file;
     * however, can appear as if sent from another address.
     *
     * @author aunger 12/10/09
     * @author jak2 1/7/10
     *
     * @param from
     * @param to array of addresses - may be null
     * @param cc array of addresses - may be null
     * @param bcc array of addresses - may be null
     * @param subject
     * @param body
     * @param attachmentNames files paths to the attachments - may be null
     */
    public void sendMail(String from, String[] to, String[] cc, String[] bcc,
                         String subject, String body, String[] attachmentNames)
    {
        try
        {
            // get session
            Session session = this.getSession();

            // build message
            MimeMessage msg = new MimeMessage(session);
            msg.setSubject(subject);
            msg.setFrom(new InternetAddress(from));

            // to
            if(to != null)
            {
                for(String s : to)
                {
                    if(!s.isEmpty() && s != null)
                    {
                        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(s));
                    }
                }
            }
            // cc
            if(cc != null)
            {
                for(String s : cc)
                {
                    if(!s.isEmpty() && s != null)
                    {
                        msg.addRecipient(Message.RecipientType.CC, new InternetAddress(s));
                    }
                }
            }
            // bcc
            if(cc != null)
            {
                for(String s : bcc)
                {
                    if(!s.isEmpty() && s != null)
                    {
                        msg.addRecipient(Message.RecipientType.BCC, new InternetAddress(s));
                    }
                }
            }

            // multi part message to add parts to
            Multipart multipart = new MimeMultipart();

            // add message text
            MimeBodyPart mainTextPart = new MimeBodyPart();
            mainTextPart.setContent("<html>" + body + "</html>", "text/html");
            multipart.addBodyPart(mainTextPart);

            // attachments
            if(attachmentNames != null)
            {
                for(String s : attachmentNames)
                {
                    if(!s.isEmpty() && s != null)
                    {
                        // add attachment
                        MimeBodyPart attachmentPart = new MimeBodyPart();
                        DataSource source = new FileDataSource(s);
                        attachmentPart.setDataHandler(new DataHandler(source));
                        attachmentPart.setFileName(s.substring(s.lastIndexOf("/")+1));
                        multipart.addBodyPart(attachmentPart);
                    }
                }
            }

            // Put parts in message
            msg.setContent(multipart);

            // send message
            Transport.send(msg);
        }
        catch (Exception ex)
        {
            new ErrorView(ex);
        }
    }
}