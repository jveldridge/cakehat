package cakehat.email;

import cakehat.Allocator;
import cakehat.services.PathServices;
import cakehat.services.ServicesException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
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
 * Sends email from a Brown CS account using LDAP over secure SMTP using a Java KeyStore.
 * <br/><br/>
 * LDAP credentials should be kept course confidential and should not be distributed in any form.
 * <br/><br/>
 * A Java KeyStore file is included in this package and will be extracted if not already present in the course's cakehat
 * installation. The file contains the Brown CS certificate. The Brown CS certificate is available at
 * <a href="http://www.cs.brown.edu/system/net_remote/certificates/browncs-ca.crt">
 * http://www.cs.brown.edu/system/net_remote/certificates/browncs-ca.crt</a> as of 12/16/2011. This certificate expires
 * on Friday, April 3, 2015 at 10:40:35AM EDT. A Java KeyStore is a file which contains trusted certificates meaning
 * that the Java application will trust secure communications with a server that has a certificate in the specified
 * keystore. It is not actually required by the Brown CS Email server, it is possible (although difficult and hacky) to
 * disable Java's authentication process.
 * <br/><br/>
 * All courses may use the same keystore that is included in cakehat. However, for completeness the process to create
 * this file is documented here.  The Java KeyStore is created by using the {@code keystore} command which comes as
 * part of a Java developer installation (it is on the department machines):
 * <pre>
 * {@code
 * keytool -genkey -keyalg RSA -keystore <KEYSTORE FILE PATH> -storepass <PASSWORD> -validity <DAYS> -dname "cn=cakehat, OU=Department of Computer Science, O=Brown University, L=Providence, ST=RI, C=US" -import -file <BROWN CS CERTIFICATE FILE PATH> 
 * }
 * </pre>
 * <pre>{@code <KEYSTORE FILE PATH>}</pre>
 * The path to the keystore file this command will generate. By convention this file is given a {@code jks} file
 * extension.
 * <br/><br/><pre>{@code <PASSWORD>}</pre>
 * The password that this keystore will have. This password will be needed at runtime to access the keystore. It is
 * also necessary in order to add more certificates, but cakehat only requires the Brown CS certificate.
 * <br/><br/><pre>{@code <DAYS>}</pre>
 * The number of days this keystore is valid for.
 * <br/><br/><pre>{@code <BROWN CS CERTIFICATE FILE PATH>}</pre>
 * The path to the location on disk where the Brown CS certificate was downloaded to.
 * <br/><br/>
 * When asked "Trust this certificate? [no]" type in yes and press return.
 *
 * @author jak2
 */
public class EmailAccount
{
    /**
     * The address of the Brown CS email SMTP server.
     */
    private static final String EMAIL_HOST = "smtps.cs.brown.edu";
    
    /**
     * The port of the Brown CS SMTP server.
     */
    private static final String EMAIL_PORT = "465";
    
    /**
     * The store pass for the Java KeyStore (JKS) file. This password is needed at runtime to access the keystore. It is
     * also necessary in order to add more certificates, but cakehat only requires the Brown CS certificate. There is no
     * significant security issue in having this password accessible. Anyone can create a similar Java KeyStore using
     * the department's certificate file. The actual keystore file will be protected by file system permissions. If a
     * person gained access to the keystore and modified it they would be able to cause cakehat to incorrectly determine
     * whether they were actually talking to the Brown CS email server. This could potentially cause email sending to
     * fail because cakehat would no longer be able to correctly identify the Brown CS email server. If there was a
     * redirection or man in the middle attack and the requests to the Brown CS email server were being redirected this
     * maliciously modified keystore could fool cakehat into thinking it was actually talking to the Brown CS email
     * server. However, if the malicious user had access to the keystore they almost certainly would have access to the
     * database.
     */
    private static final String JAVA_KEY_STORE_PASS = "neither_cake_nor_hat";
    
    /**
     * The name of the file in this package. For consistency this is also the name of the file provided by
     * {@link PathServices#getJavaKeyStoreFile()}.
     */
    private static final String JAVA_KEY_STORE_FILE_NAME = "brown_cs_email.jks";

    /**
     * A Brown CS login such as {@code jak2} or {@code cs015000}. A course is strongly encouraged to use a test account
     * for this.
     */
    private final String _login;
    
    /**
     * The LDAP password for the account specified by {@link #_login}. This password is set by being logged in as the
     * user specified by {@link #_login}, running the {@code ldappasswd} command and setting a password. By default a
     * Brown CS account does not have an LDAP password.
     * <br/><br/>
     * This information was retrieved from <a href="http://www.cs.brown.edu/system/accounts/passwords.html">
     * http://www.cs.brown.edu/system/accounts/passwords.html</a> on 12/16/2011.
     */
    private final String _ldapPassword;
    
    /**
     * Properties used to establish an email session.
     */
    private final Properties _properties;
    
    /**
     * Provider of the {@link #_login} and {@link #_ldapPassword} to the email session.
     */
    private final Authenticator _authenticator;
    
    /**
     * Constructs an {@code EmailAccount} that can send emails using the credentials passed to this constructor.
     * However, emails can be sent as if coming from anyone.
     * 
     * @param login a Brown CS login, may not be {@code null}
     * @param ldapPassword the LDAP password belong to the user specified by {@code login}, may not be {@code null}
     * 
     * @throws IOException If there is an existing unreadable keystore file or if unable to extract the file and set the
     * correct permissions and group ownership. This exception does not represent user error; it is either cakehat or
     * file system error.
     */
    EmailAccount(String login, String ldapPassword) throws IOException
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
        
        ensureJavaKeyStoreFile();
        
        _login = login;
        _ldapPassword = ldapPassword;
        
        _properties = buildProperties();
        _authenticator = buildAuthenticator();
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
                     String subject, String body, Iterable<File> attachments) throws MessagingException
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

        //File attachments
        if(attachments != null)
        {
            for(File attachment : attachments)
            {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setDataHandler(new DataHandler(new FileDataSource(attachment)));
                attachmentPart.setFileName(attachment.getName());
                multipart.addBodyPart(attachmentPart);
            }
        }

        //Put parts in message
        msg.setContent(multipart);

        //Send message
        Transport.send(msg);
    }
    
    /**
     * Ensures the Java KeyStore file exists by extracting it from the jar (or when developing from the directory) and
     * writing it into the .cakehat directory.
     * 
     * @throws IOException If there is an existing unreadable keystore file or if unable to extract the file and set the
     * correct permissions and group ownership. This exception does not represent user error; it is either cakehat or
     * file system error.
     */
    private void ensureJavaKeyStoreFile() throws IOException
    {
        File keyStoreFile = Allocator.getPathServices().getJavaKeyStoreFile();
        
        //As this file is managed internally by cakehat the permissions should never be wrong, but check to be sure
        if(keyStoreFile.exists() && !keyStoreFile.canRead())
        {
            throw new IOException("Unable to read Java KeyStore file needed for email. Location: " +
                    keyStoreFile.getAbsolutePath());
            
        }
        
        //If the file does not exist, extract it from the cakehat jar into the .cakehat directory
        if(!keyStoreFile.exists())
        {
            extractJavaKeyStoreFile(keyStoreFile);
        }
    }
    
    /**
     * Extracts the Java KeyStore file to the location specified by {@code dstFile}. If the directory it is to be
     * extracted into does not exist it will create it and all parent directories as necessary with correct permissions
     * and group ownership. The file will be created with correct permissions and group ownership.
     * 
     * @param dstFile the location to the write the file to
     * @throws IOException if unable to extract the file and set the correct permissions and group ownership
     */
    private void extractJavaKeyStoreFile(File dstFile) throws IOException
    {
        //Ensure directory exists where the file is going to be copied to
        try
        {
            Allocator.getFileSystemServices().makeDirectory(dstFile.getParentFile());
        }
        catch(ServicesException e)
        {
            throw new IOException("Unable to create directory for: " + dstFile.getAbsolutePath(), e);
        }
        
        //Copy        
        FileOutputStream dstStream = new FileOutputStream(dstFile);
        InputStream keyStoreStream = EmailAccount.class.getResourceAsStream(JAVA_KEY_STORE_FILE_NAME);
        try
        {
            byte[] buffer = new byte[4096];
            int n = 0;
            while(-1 != (n = keyStoreStream.read(buffer)))
            {
                dstStream.write(buffer, 0, n);
            }
        }
        catch(IOException e)
        {
            //If the file was created, attempt to delete it as something went wrong during the copy
            if(dstFile.exists())
            {
                Allocator.getFileSystemUtilities().deleteFiles(Arrays.asList(dstFile));
            }
            
            throw e;
        }
        finally
        {
            //Attempt to close the streams, but if it fails that does not actually mean anything went wrong with
            //copying, so there is no need to do anything about it
            try
            {
                keyStoreStream.close();
                dstStream.close();
            }
            catch(IOException e) { }
        }
        
        //Set the permissions and group ownership, if it fails then delete the file
        try
        {
            Allocator.getFileSystemServices().sanitize(dstFile);
        }
        catch(ServicesException e)
        {
            Allocator.getFileSystemUtilities().deleteFiles(Arrays.asList(dstFile));
            
            throw new IOException("Unable to set the permissions correctly, the file was deleted", e);
        }
    }
    
    /**
     * Properties needed for email.
     *
     * @return
     */
    private Properties buildProperties()
    {
        //Set system properties
        System.setProperty("javax.net.ssl.trustStore", Allocator.getPathServices().getJavaKeyStoreFile().getAbsolutePath());
        System.setProperty("javax.net.ssl.trustStorePassword", JAVA_KEY_STORE_PASS);

        //Build properties
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtps");
        properties.put("mail.smtps.host", EMAIL_HOST);
        properties.put("mail.smtps.user", _login);
        properties.put("mail.smtp.host", EMAIL_HOST);
        properties.put("mail.smtp.port", EMAIL_PORT);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.socketFactory.port", EMAIL_PORT);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        //properties.put("mail.smtp.debug", "true"); //Leave this for debugging purposes

        return properties;
    }

    /**
     * Authenticator that provides the Brown CS login and LDAP password.
     *
     * @return
     */
    private Authenticator buildAuthenticator()
    {
        Authenticator authenticator = new Authenticator()
        {
            @Override
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(_login, _ldapPassword);
            }
        };

        return authenticator;
    }
}