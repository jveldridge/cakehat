package cakehat.views.config;

import support.ui.DocumentAdapter;
import cakehat.Allocator;
import cakehat.email.EmailAccount;
import cakehat.database.DbNotifyAddress;
import cakehat.database.DbPropertyValue;
import cakehat.database.DbPropertyValue.DbPropertyKey;
import cakehat.views.config.ValidationResult.ValidationState;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ImmutableList;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import support.utils.SingleElementSet;

/**
 *
 * @author jak2
 */
class EmailPanel extends JPanel
{
    private static final String WORKER_TAG = "EMAIL";
    private final UniqueElementSingleThreadWorker _worker;
    private final JPanel _contentPanel;
    
    EmailPanel(UniqueElementSingleThreadWorker worker)
    {
        _worker = worker;
        
        this.setLayout(new BorderLayout(0, 0));
        
        _contentPanel = new JPanel();
        this.add(_contentPanel);
        
        this.initialize();
    }
    
    private void initialize()
    {
        _contentPanel.removeAll();
        _contentPanel.setLayout(new BorderLayout(0, 0));
        
        JLabel loadingLabel = new JLabel("Initializing...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD, 16));
        _contentPanel.add(loadingLabel, BorderLayout.CENTER);
        
        JProgressBar loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        _contentPanel.add(loadingBar, BorderLayout.SOUTH);
        
        _worker.submit(null, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //Retrieve account and password from database, create new values if not in database
                    DbPropertyValue<String> accountInDb = Allocator.getDatabase().getPropertyValue(DbPropertyKey.EMAIL_ACCOUNT);
                    final DbPropertyValue<String> accountProp =
                            (accountInDb == null ? new DbPropertyValue<String>("") : accountInDb);
                    DbPropertyValue<String> passwordInDb = Allocator.getDatabase().getPropertyValue(DbPropertyKey.EMAIL_PASSWORD);
                    final DbPropertyValue<String> passwordProp =
                            (passwordInDb == null ? new DbPropertyValue<String>("") : passwordInDb);
                    
                    //Adjust the list of notify addresses to have three as the UI shows 3 entries
                    Iterator<DbNotifyAddress> dbAddresses = Allocator.getDatabase().getNotifyAddresses().iterator();
                    ImmutableList.Builder<DbNotifyAddress> addressesBuilder = ImmutableList.builder();
                    for(int i = 0; i < 3; i++)
                    {
                        addressesBuilder.add(dbAddresses.hasNext() ? dbAddresses.next() : new DbNotifyAddress(""));
                    }
                    final List<DbNotifyAddress> notifyAddresses = addressesBuilder.build();
                    
                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {   
                            _contentPanel.removeAll();
                            _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
                            
                            _contentPanel.add(new CredentialsPanel(EmailPanel.this, _worker, accountProp, passwordProp));
                            _contentPanel.add(Box.createVerticalStrut(10));
                            _contentPanel.add(new NotificationsPanel(EmailPanel.this, _worker, notifyAddresses));
                            
                            //Force visual update to reflect these changes
                            _contentPanel.repaint();
                            _contentPanel.revalidate();
                        }
                    });
                }
                catch(final SQLException e)
                {
                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            _contentPanel.removeAll();
                            _contentPanel.setLayout(new BorderLayout(0, 0));

                            //Error message
                            JLabel errorLabel = new JLabel("Unable to retrieve email information from the database");
                            errorLabel.setForeground(Color.RED);
                            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            errorLabel.setFont(errorLabel.getFont().deriveFont(Font.BOLD, 16));
                            _contentPanel.add(errorLabel, BorderLayout.CENTER);

                            //Option to retry
                            JButton retryButton = new JButton("Retry");
                            retryButton.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent ae)
                                {
                                    initialize();
                                }
                            });
                            JPanel retryPanel = new JPanel();
                            retryPanel.add(retryButton);
                            _contentPanel.add(retryPanel, BorderLayout.SOUTH);

                            //Force visual update to reflect these changes
                            _contentPanel.repaint();
                            _contentPanel.revalidate();

                            //Show error report
                            new ErrorView(e);
                        }
                    });
                }
            }
        });
    }
    
    private static class CredentialsPanel extends JPanel
    {
        private final EmailPanel _emailPanel;
        private final UniqueElementSingleThreadWorker _worker;
        private final DbPropertyValue<String> _accountProp, _passwordProp;
        private final JTextField _accountField, _passwordField;
        private final JButton _testButton;
        
        CredentialsPanel(EmailPanel emailPanel, UniqueElementSingleThreadWorker worker,
                         DbPropertyValue<String> accountProp, DbPropertyValue<String> passwordProp)
        {
            _emailPanel = emailPanel;
            _worker = worker;
            _accountProp = accountProp;
            _passwordProp = passwordProp;
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            //Descriptive text
            JPanel descriptionPanel = new JPanel(new BorderLayout(0, 0));
            
            JLabel headerLabel = new JLabel("Credentials");
            headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18));
            descriptionPanel.add(headerLabel, BorderLayout.NORTH);
            
            descriptionPanel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
            
            JLabel descriptionLabel = new JLabel("<html>" +
                    "To send email, cakehat needs the email credentials of a Brown CS account. " +
                    "We suggest you make use of your course's test account. " +
                    "To set or reset an account's LDAP password (which is the password used for Brown CS email), run " +
                    "<tt>ldappasswd</tt> while signed into the account.</html>");
            descriptionPanel.add(descriptionLabel, BorderLayout.SOUTH);
            this.add(descriptionPanel);
         
            //Vertical space
            this.add(Box.createVerticalStrut(10));
            
            //Field labels
            JPanel labelsPanel = new JPanel(new GridLayout(1, 5));
            this.add(labelsPanel);
            labelsPanel.add(Box.createHorizontalBox());
            JLabel accountLabel = new JLabel("Account");
            accountLabel.setHorizontalAlignment(SwingConstants.CENTER);
            labelsPanel.add(accountLabel);
            labelsPanel.add(Box.createHorizontalBox());
            JLabel passwordLabel = new JLabel("Password");
            passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);
            labelsPanel.add(passwordLabel);
            labelsPanel.add(Box.createHorizontalBox());
            
            //Vertical space
            this.add(Box.createVerticalStrut(3));
            
            //Fields for the account and password credentials
            JPanel credentialsPanel = new JPanel(new GridLayout(1, 5));
            this.add(credentialsPanel);
            
            credentialsPanel.add(Box.createHorizontalBox());
            
            _accountField = new PropertyTextField(DbPropertyKey.EMAIL_ACCOUNT, accountProp);
            credentialsPanel.add(_accountField);
            
            credentialsPanel.add(Box.createHorizontalBox());
            
            _passwordField = new PropertyTextField(DbPropertyKey.EMAIL_PASSWORD, passwordProp);
            credentialsPanel.add(_passwordField);
            
            credentialsPanel.add(Box.createHorizontalBox());
            
            //Vertical space
            this.add(Box.createVerticalStrut(10));
            
            //Button to test the credentials
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            this.add(buttonPanel);
            _testButton = new JButton("Test Credentials");
            _testButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    new TestCredentialsDialog(_accountProp.getValue(), _passwordProp.getValue());
                }
            });
            buttonPanel.add(_testButton);
            determineTestButtonEnabledStatus();
        }
        
        private void determineTestButtonEnabledStatus()
        {
            _testButton.setEnabled(!_accountField.getText().isEmpty() && !_passwordField.getText().isEmpty());
        }
        
        @Override
        public Dimension getMaximumSize()
        {
            Dimension size = getPreferredSize();
            size.width = Short.MAX_VALUE;
            
            return size;
        }
        
        private class PropertyTextField extends ValidatingTextField
        {
            private final DbPropertyKey<String> _propertyKey;
            private final DbPropertyValue<String> _propertyValue;
            
            PropertyTextField(DbPropertyKey<String> propKey, DbPropertyValue<String> propValue)
            {
                _propertyKey = propKey;
                _propertyValue = propValue;
                
                this.getDocument().addDocumentListener(new DocumentAdapter()
                {
                    @Override
                    public void modificationOccurred(DocumentEvent de)
                    {
                        determineTestButtonEnabledStatus();
                    }
                });
            }
            
            @Override
            protected String getDbValue()
            {
                return _propertyValue.getValue();
            }

            @Override
            protected ValidationResult validate(String text)
            {
                return ValidationResult.validateNotEmpty(text);
            }

            @Override
            protected void applyChange(String text)
            {
                final String dbValue = _propertyValue.getValue();
                _propertyValue.setValue(text);

                _worker.submit(WORKER_TAG, new DbRunnable(_worker, _propertyValue)
                 {
                    @Override
                    public void dbCall() throws SQLException
                    {
                        Allocator.getDatabase().putPropertyValue(_propertyKey, _propertyValue);
                    }

                    @Override
                    public void onDbCallFailure()
                    {
                        _propertyValue.setValue(dbValue);
                        setText(dbValue);
                        determineTestButtonEnabledStatus();
                    }

                    @Override
                    public void onFinalFailureNow()
                    {
                         _worker.cancel(WORKER_TAG);
                    }

                    @Override
                    public void onFinalFailureLater()
                    {
                        _emailPanel.initialize();
                    }
                });
            }
        }
    }
    
    private static class TestCredentialsDialog extends JDialog
    {
        private final JPanel _contentPanel;
        
        TestCredentialsDialog(final String account, final String password)
        {
            super(Allocator.getGeneralUtilities().getFocusedFrame(), "Test Email Credentials", true);
            
            this.setLayout(new BorderLayout(0, 0));
            this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
            this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
            this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
            this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);

            _contentPanel = new JPanel();
            _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
            this.add(_contentPanel, BorderLayout.CENTER);
            
            final String from = account + "@" + Allocator.getConstants().getEmailDomain();
            final String to = Allocator.getUserUtilities().getUserLogin() + "@" + Allocator.getConstants().getEmailDomain();
            final String subject = "cakehat Email Verification";
            final String body = "This is a test. cakehat is conducting a test of the Email Delivery System. " +
                                "This is only a test.";
            
            _contentPanel.add(createCredentialsRow("From:", from));
            _contentPanel.add(Box.createVerticalStrut(2));
            _contentPanel.add(createCredentialsRow("To:", to));
            _contentPanel.add(Box.createVerticalStrut(2));
            _contentPanel.add(createCredentialsRow("Subject:", subject));
            _contentPanel.add(Box.createVerticalStrut(2));
            
            JTextArea bodyMessage = new JTextArea(body);
            bodyMessage.setBorder(BorderFactory.createTitledBorder("Body"));
            bodyMessage.setBackground(this.getBackground());
            bodyMessage.setDisabledTextColor(Color.GRAY);
            bodyMessage.setEnabled(false);
            _contentPanel.add(bodyMessage);
            
            _contentPanel.add(Box.createVerticalStrut(5));
            
            JPanel buttonPanel = new JPanel(new GridLayout(1, 5));
            buttonPanel.add(Box.createHorizontalBox());
            JButton sendButton = new JButton("Send");
            sendButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    try
                    {
                        EmailAccount emailAccount = new EmailAccount(account, password);
                        emailAccount.send(new InternetAddress(from),
                                          Arrays.asList(new InternetAddress(to)),
                                          null,
                                          null,
                                          subject,
                                          body,
                                          null);
                        
                        emailSent("Success! Please confirm by checking your email.", true);
                    }
                    //cakehat issue
                    catch(GeneralSecurityException ex)
                    {
                        emailSent("A cakehat issue prevented the email from being sent", false);
                        new ErrorView(ex, "A cakehat issue prevented the email from being sent");
                    }
                    //cakehat issue
                    catch(AddressException ex)
                    {
                        emailSent("A cakehat issue prevented the email from being sent", false);
                        new ErrorView(ex, "A cakehat issue prevented the email from being sent");
                    }
                    //Bad credentials
                    catch(MessagingException e)
                    {
                        emailSent("The credentials were not accepted by the Brown CS email server", false);
                    }
                }
            });
            buttonPanel.add(sendButton);
            buttonPanel.add(Box.createHorizontalBox());
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    TestCredentialsDialog.this.dispose();
                }
            });
            buttonPanel.add(cancelButton);
            buttonPanel.add(Box.createHorizontalBox());
            _contentPanel.add(buttonPanel);
            
            //Show
            this.setResizable(false);
            this.setSize(new Dimension(600, 225));
            this.setPreferredSize(new Dimension(600, 225));
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.setAlwaysOnTop(true);
            this.setLocationRelativeTo(Allocator.getGeneralUtilities().getFocusedFrame());
            this.setVisible(true);   
        }
        
        private void emailSent(String message, boolean success)
        {
            _contentPanel.removeAll();
            _contentPanel.setLayout(new BorderLayout(0, 0));
                    
            //Message
            JLabel label = new JLabel(message);
            if(!success)
            {
                label.setForeground(Color.RED);
            }
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 16));
            _contentPanel.add(label, BorderLayout.CENTER);

            //Close
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    TestCredentialsDialog.this.dispose();
                }
            });
            JPanel closePanel = new JPanel();
            closePanel.add(closeButton);
            _contentPanel.add(closePanel, BorderLayout.SOUTH);
            
            //Force visual update to reflect these changes
            _contentPanel.repaint();
            _contentPanel.revalidate();
        }
        
        private JPanel createCredentialsRow(String labelText, String valueText)
        {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0))
            {
                @Override
                public Dimension getMaximumSize()
                {
                    Dimension size = getPreferredSize();
                    size.width = Short.MAX_VALUE;

                    return size;
                }
            };
            
            JLabel label = new JLabel(labelText);
            label.setPreferredSize(new Dimension(50, 25));
            panel.add(label);
            
            panel.add(Box.createHorizontalStrut(10));
            
            JTextField textField = new JTextField(valueText);
            textField.setPreferredSize(new Dimension(518, 25));
            textField.setEnabled(false);
            textField.setDisabledTextColor(Color.GRAY);
            panel.add(textField);
            
            return panel;
        }
    }
    
    private static class NotificationsPanel extends JPanel
    {
        NotificationsPanel(final EmailPanel emailPanel, final UniqueElementSingleThreadWorker worker,
                           List<DbNotifyAddress> notifyAddresses)
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            //Descriptive text
            JPanel descriptionPanel = new JPanel(new BorderLayout(0, 0));
            JLabel headerLabel = new JLabel("Notifications");
            headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18));
            descriptionPanel.add(headerLabel, BorderLayout.NORTH);
            descriptionPanel.add(Box.createVerticalStrut(5), BorderLayout.CENTER);
            JLabel descriptionLabel = new JLabel("<html>" +
                    "When certain actions are taken in cakehat, such as TAs submitting grades, email will be sent " +
                    "to the following email addresses:</html>");
            descriptionPanel.add(descriptionLabel, BorderLayout.SOUTH);
            this.add(descriptionPanel);
         
            //Vertical space
            this.add(Box.createVerticalStrut(10));
            
            //Addresses
            JPanel addressesPanel = new JPanel();
            this.add(addressesPanel);
            addressesPanel.setLayout(new BoxLayout(addressesPanel, BoxLayout.X_AXIS));
            for(int i = 0; i < 3; i++)
            {
                addressesPanel.add(Box.createHorizontalStrut(10));
                
                final DbNotifyAddress notifyAddress = notifyAddresses.get(i);
                
                ValidatingTextField emailField = new ValidatingTextField()
                {
                    @Override
                    protected String getDbValue()
                    {
                        return notifyAddress.getAddress();
                    }

                    @Override
                    protected ValidationResult validate(String text)
                    {
                        ValidationResult result;
                        if(text.isEmpty())
                        {
                            result = ValidationResult.NO_ISSUE;
                        }
                        else
                        {
                            try
                            {
                                new InternetAddress(text, true);
                                result = ValidationResult.NO_ISSUE;
                            }
                            catch(AddressException e)
                            {
                                result = new ValidationResult(ValidationState.ERROR, "Invalid email address");
                            }
                        }
                        
                        return result;
                    }

                    @Override
                    protected void applyChange(final String newValue)
                    {
                        final String dbText = notifyAddress.getAddress();
                        notifyAddress.setAddress(newValue);
                        
                        worker.submit(WORKER_TAG, new DbRunnable(worker, notifyAddress)
                        {
                            @Override
                            public void dbCall() throws SQLException
                            {   
                                if(notifyAddress.getAddress().isEmpty())
                                {
                                    Allocator.getDatabase().removeNotifyAddresses(SingleElementSet.of(notifyAddress));
                                }
                                else
                                {
                                    Allocator.getDatabase().putNotifyAddresses(SingleElementSet.of(notifyAddress));
                                }
                            }

                            @Override
                            public void onDbCallFailure()
                            {
                                notifyAddress.setAddress(dbText);
                                setText(dbText);
                            }

                            @Override
                            public void onFinalFailureNow()
                            {
                                worker.cancel(WORKER_TAG);
                            }

                            @Override
                            public void onFinalFailureLater()
                            {
                                emailPanel.initialize();
                            }
                        });
                    }
                };
                
                addressesPanel.add(emailField);
            }
            addressesPanel.add(Box.createHorizontalStrut(10));
        }
        
        @Override
        public Dimension getMaximumSize()
        {
            Dimension size = getPreferredSize();
            size.width = Short.MAX_VALUE;
            
            return size;
        }
    }
}