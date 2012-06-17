package cakehat.views.config;

import cakehat.Allocator;
import cakehat.database.DbTA;
import cakehat.logging.ErrorReporter;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import support.ui.FormattedLabel;
import support.utils.posix.NativeException;

/**
 *
 * @author jak2
 */
class TAPanel extends JPanel
{
    private static final String WORKER_TAG = "TA";
    
    private final JPanel _contentPanel;
    private final UniqueElementSingleThreadWorker _worker;
    
    public TAPanel(UniqueElementSingleThreadWorker worker)
    {   
        _worker = worker;
        
        this.setLayout(new BorderLayout(0, 0));
        
        final HeaderRow headerRow = new HeaderRow();
        this.add(headerRow, BorderLayout.NORTH);
        
        _contentPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(_contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent ce)
            {
                headerRow.notifyViewportWidthChange(ce.getComponent().getWidth());
            }
        });
        this.add(scrollPane, BorderLayout.CENTER);
        
        this.initialize();
    }
    
    private class HeaderRow extends JPanel
    {
        private final Filler _scrollPanePadding;
        
        HeaderRow()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            //Vertical offset
            this.add(Box.createVerticalStrut(3));
            
            //Content panel - holds labels and padding
            JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
            this.add(contentPanel);
            
            //Labels
            JPanel labelPanel = new JPanel(new GridLayout(1, 5));
            contentPanel.add(labelPanel, BorderLayout.CENTER);
            labelPanel.add(makeLabel("Login", "The TA's CS login."));
            labelPanel.add(makeLabel("First Name", "The TA's first name, for display purposes only."));
            labelPanel.add(makeLabel("Last Name", "The TA's last name, for display purposes only."));
            labelPanel.add(makeLabel("Default Grader", "Default graders are automatically added to the list of TAs " +
                    "to distribute digital handins to. This list may be modified before creating a distribution."));
            labelPanel.add(makeLabel("Administrator", "Only administrators may launch the configuration and " + 
                    "administrative cakehat interfaces. HTAs are always administrators."));
            
            //Padding to offset for the scroll pane's appearance
            _scrollPanePadding = new Filler(new Dimension(), new Dimension(), new Dimension()); 
            contentPanel.add(_scrollPanePadding, BorderLayout.EAST);
            
            //Divider
            this.add(Box.createVerticalStrut(3));
            this.add(new JSeparator());
            this.add(Box.createVerticalStrut(3));
        }
        
        void notifyViewportWidthChange(int width)
        {
            Dimension paddingDim = new Dimension(this.getWidth() - width, 0);
            _scrollPanePadding.changeShape(paddingDim, paddingDim, paddingDim);
        }
        
        private JLabel makeLabel(String text, String tooltip)
        {
            JLabel label = FormattedLabel.asSubheader(text);
            label.setToolTipText(tooltip);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            return label;
        }
        
        @Override
        public Dimension getMaximumSize()
        {
            Dimension size = getPreferredSize();
            size.width = Short.MAX_VALUE;
            
            return size;
        }
    }
    
    private void initialize()
    {   
        _contentPanel.removeAll();
        _contentPanel.setLayout(new BorderLayout(0, 0));
        
        JLabel loadingLabel = FormattedLabel.asHeader("Initializing...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        _contentPanel.add(loadingLabel, BorderLayout.CENTER);
        
        JProgressBar loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        _contentPanel.add(loadingBar, BorderLayout.SOUTH);
        
        //Initialize on a seperate thread so that the interface loads faster
        _worker.submit(null, new Runnable()
        {
            public void run()
            {   
                try
                {   
                    final List<DbTAInfo> tasInfo = retrieveInfo();

                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {   
                            _contentPanel.removeAll();
                            _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
                            
                            for(DbTAInfo taInfo : tasInfo)
                            {
                                _contentPanel.add(new TARow(TAPanel.this, _worker, taInfo));
                            }
                            
                            //Force visual update to reflect these changes
                            _contentPanel.repaint();
                            _contentPanel.revalidate();
                        }
                    });
                }
                catch(final TADataException e)
                {
                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            _contentPanel.removeAll();
                            _contentPanel.setLayout(new BorderLayout(0, 0));

                            //Error message
                            _contentPanel.add(FormattedLabel.asHeader(e.getMessage()).centerHorizontally()
                                    .showAsErrorMessage(), BorderLayout.CENTER);
                            
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
                            ErrorReporter.report(e);
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Exception used just within {@code TAPanel} to convey useful error messages that will be shown to the user.
     */
    private static class TADataException extends Exception
    {
        TADataException(String msg, Exception cause)
        {
            super(msg, cause);
        }
    }
    
    private List<DbTAInfo> retrieveInfo() throws TADataException
    {   
        //Get TA info out of the database and from the Linux system
        Set<DbTA> tasInDb;
        Set<String> tasOnSystem;
        Set<String> htasOnSystem;
        try
        {
            tasInDb = Allocator.getDatabase().getTAs();
        }
        catch(SQLException e)
        {
            throw new TADataException("Unable to retrieve TA information from the database", e);
        }
        try
        {
            tasOnSystem = Allocator.getUserServices().getTALogins();
            htasOnSystem = Allocator.getUserServices().getHTALogins();
        }
        catch(NativeException e)
        {
            throw new TADataException("Unable to retrieve TA information from the system", e);
        }
        
        //TAs to add to database or update their information in the database
        Set<DbTA> toAddToDb = new HashSet<DbTA>();
        
        //Information on the TAs that are in the database and will be displayed to the user
        List<DbTAInfo> tasInfo = new ArrayList<DbTAInfo>();
        
        //Will become a list of only TA logins not in the database
        List<String> tasNotInDb = new ArrayList<String>(tasOnSystem);
        
        //For each TA currently stored in the database
        for(DbTA ta : tasInDb)
        {
            //Remove this login from tasNotInDb so that list contains only the TAs not in the database
            tasNotInDb.remove(ta.getLogin()); 
            
            //If the TA's login is no longer in the csXXXta group
            if(!tasOnSystem.contains(ta.getLogin()))
            {
                //See if the TA's login has changed and this new login is in the TA group
                try
                {
                    String login = Allocator.getUserUtilities().getUserLogin(ta.getId());
                    if(tasOnSystem.contains(login))
                    {
                        //Update database to reflect new login
                        ta.setLogin(login);
                        
                        //This login will now be in the database
                        tasNotInDb.remove(login);
                        
                        //Update info in database
                        toAddToDb.add(ta);
                        
                        tasInfo.add(new DbTAInfo(ta, true, htasOnSystem.contains(ta.getLogin())));
                    }
                    else
                    {
                        tasInfo.add(new DbTAInfo(ta, false, false));
                    }
                }
                //If the user id no longer exists an exception will be thrown
                catch(NativeException e)
                {
                    tasInfo.add(new DbTAInfo(ta, false, false));
                }
            }
            else
            {
                tasInfo.add(new DbTAInfo(ta, true, htasOnSystem.contains(ta.getLogin())));
            }
        }
        
        //For each TA login not in the database
        for(String login : tasNotInDb)
        {
            try
            {
                //Create a DbTA object to represent the TA
                String[] nameParts = Allocator.getUserUtilities().getUserName(login).split(" ");
                String firstName = nameParts[0];
                String lastName = nameParts[nameParts.length - 1];
                int userID = Allocator.getUserUtilities().getUserId(login);
                boolean isHTA = htasOnSystem.contains(login);
                DbTA ta = new DbTA(userID, login, firstName, lastName, true, isHTA);

                //Add the TA to the database
                toAddToDb.add(ta);

                tasInfo.add(new DbTAInfo(ta, true, isHTA));
            }
            catch(NativeException e)
            {
                throw new TADataException("Unable to retrieve information on TA " + login, e);
            }
        }
        
        //If there are any TAs to add/update
        if(!toAddToDb.isEmpty())
        {
            try
            {
                Allocator.getDatabase().putTAs(toAddToDb);
            }
            catch(SQLException e)
            {
                throw new TADataException(("Unable to add or update TA information"), e);
            }
        }
        
        //Sort the list
        Collections.sort(tasInfo);
        
        return tasInfo;
    }
    
    /**
     * Wraps around a {@link DbTA} to provide additional data that does not need to be / cannot be stored in the
     * database.
     */
    private static class DbTAInfo implements Comparable<DbTAInfo>
    {
        private final DbTA _ta;
        private final boolean _inTAGroup;
        private final boolean _hta;
        
        DbTAInfo(DbTA ta, boolean inTAGroup, boolean hta)
        {
            _ta = ta;
            _inTAGroup = inTAGroup;
            _hta = hta;
        }
        
        public DbTA getTA()
        {
            return _ta;
        }

        public boolean isInTAGroup()
        {
            return _inTAGroup;
        }

        public boolean isHTA()
        {
            return _hta;
        }

        @Override
        public int compareTo(DbTAInfo other)
        {
            int comparison;
            if(this.isInTAGroup() && !other.isInTAGroup())
            {
                comparison = 1;
            }
            else if(!this.isInTAGroup() && other.isInTAGroup())
            {
                comparison = -1;
            }
            else
            {
                comparison = this.getTA().getLogin().compareTo(other.getTA().getLogin());
            }
            
            return comparison;
        }
    }
    
    private static class TARow extends JPanel
    {
        private final TAPanel _taPanel;
        private final UniqueElementSingleThreadWorker _worker;
        private final DbTAInfo _taInfo;
        private final ValidatingTextField _firstNameField, _lastNameField;
        private final JCheckBox _defaultGraderCheckBox, _adminCheckBox;
        
        TARow(TAPanel taPanel, UniqueElementSingleThreadWorker worker, DbTAInfo taInfo)
        {   
            _taPanel = taPanel;
            _worker = worker;
            _taInfo = taInfo;
            
            this.setLayout(new GridLayout(1, 0));
            
            FormattedLabel loginLabel = FormattedLabel.asContent(_taInfo.getTA().getLogin()).centerHorizontally();
            if(!_taInfo.isInTAGroup())
            {
                loginLabel.showAsErrorMessage();
                loginLabel.setToolTipText(_taInfo.getTA().getLogin() + " is no longer a member of " + 
                        Allocator.getCourseInfo().getTAGroup());
            }
            this.add(loginLabel);
            
            _firstNameField = new ValidatingTextField()
            {

                @Override
                protected String getDbValue()
                {
                    return _taInfo.getTA().getFirstName();
                }
                
                @Override
                protected ValidationResult validate(String text)
                {
                    return ValidationResult.validateNotEmpty(text);
                }

                @Override
                protected void applyChange(String text)
                {
                    TARunnable runnable = new TARunnable(_taInfo.getTA());
                    _taInfo.getTA().setFirstName(text);
                    _worker.submit(WORKER_TAG, runnable);
                }
            };
            this.add(_firstNameField);
            
            _lastNameField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _taInfo.getTA().getLastName();
                }
                
                @Override
                protected ValidationResult validate(String text)
                {
                    return ValidationResult.validateNotEmpty(text);
                }

                @Override
                protected void applyChange(String text)
                {
                    TARunnable runnable = new TARunnable(_taInfo.getTA());
                    _taInfo.getTA().setLastName(text);
                    _worker.submit(WORKER_TAG, runnable);
                }
            };
            this.add(_lastNameField);
            
            _defaultGraderCheckBox = new JCheckBox("", _taInfo.getTA().isDefaultGrader());
            _defaultGraderCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            _defaultGraderCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean selection = _defaultGraderCheckBox.isSelected();
                    if(selection != _taInfo.getTA().isDefaultGrader())
                    {
                        TARunnable runnable = new TARunnable(_taInfo.getTA());
                        _taInfo.getTA().setIsDefaultGrader(selection);
                        _worker.submit(WORKER_TAG, runnable);
                    }
                }
            });
            this.add(_defaultGraderCheckBox);
            
            _adminCheckBox = new JCheckBox("", _taInfo.getTA().isAdmin());
            _adminCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            _adminCheckBox.setEnabled(!_taInfo.isHTA());
            _adminCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean selection = _adminCheckBox.isSelected();
                    if(selection != _taInfo.getTA().isAdmin())
                    {
                        TARunnable runnable = new TARunnable(_taInfo.getTA());
                        _taInfo.getTA().setIsAdmin(selection);
                        _worker.submit(WORKER_TAG, runnable);
                    }
                }
            });
            this.add(_adminCheckBox);
        }
        
        @Override
        public Dimension getMaximumSize()
        {
            Dimension size = getPreferredSize();
            size.width = Short.MAX_VALUE;
            
            return size;
        }
        
        private class TARunnable extends DbRunnable
        {
            private final DbTA _ta;
            
            //Values before submission
            private final String _firstName, _lastName;
            private final boolean _admin, _defaultGrader;
            
            TARunnable(DbTA ta)
            {
                super(_worker, ta);
                
                _ta = ta;
                
                _firstName = _ta.getFirstName();
                _lastName = _ta.getLastName();
                _admin = _ta.isAdmin();
                _defaultGrader = _ta.isDefaultGrader();
            }
            
            @Override
            public void dbCall() throws SQLException
            {
                Allocator.getDatabase().putTAs(ImmutableSet.of(_ta));
            }

            @Override
            public void onDbCallFailure()
            {
                _ta.setFirstName(_firstName);
                _firstNameField.setText(_firstName);
                
                _ta.setLastName(_lastName);
                _lastNameField.setText(_lastName);
                
                _ta.setIsDefaultGrader(_defaultGrader);
                _defaultGraderCheckBox.setSelected(_defaultGrader);
                
                _ta.setIsAdmin(_admin);
                _adminCheckBox.setSelected(_admin);
            }

            @Override
            public void onFinalFailureNow()
            {
                _worker.cancel(WORKER_TAG);
            }

            @Override
            public void onFinalFailureLater()
            {
                _taPanel.initialize();
            }
        }
    }
}