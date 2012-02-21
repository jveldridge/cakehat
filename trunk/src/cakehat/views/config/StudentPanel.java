package cakehat.views.config;

import cakehat.Allocator;
import cakehat.database.DbStudent;
import cakehat.views.config.ValidationResult.ValidationState;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import support.utils.posix.NativeException;

/**
 *
 * @author jak2
 */
class StudentPanel extends JPanel
{
    private static final String WORKER_TAG = "STUDENT";
    
    private final NotificationRow _notificationRow;
    private final JPanel _scrollablePanel;
    private final JScrollBar _verticalScrollBar;
    private final UniqueElementSingleThreadWorker _worker;
    private volatile StudentData _currentStudentData = null;
    
    StudentPanel(UniqueElementSingleThreadWorker worker)
    {        
        _worker = worker;
        
        this.setLayout(new BorderLayout(0, 0));
        
        //Header row at top
        final HeaderRow headerRow = new HeaderRow();
        this.add(headerRow, BorderLayout.NORTH);
        
        //A panel in a scroll pane visualizing student data
        _scrollablePanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(_scrollablePanel);
        _verticalScrollBar = scrollPane.getVerticalScrollBar();
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
        
        //Notification
        _notificationRow = new NotificationRow(_worker, this);
        this.add(_notificationRow, BorderLayout.SOUTH);
        
        //Load data
        this.initialize();
    }
    
    private StudentData getCurrentStudentData()
    {
        StudentData data = _currentStudentData;
        if(data == null)
        {
            throw new IllegalStateException("Student data has never been retrieved");
        }
        
        return data;
    }
    
    private void initialize()
    {   
        //Show initialization message while retrieving data
        _scrollablePanel.removeAll();
        _scrollablePanel.setLayout(new BorderLayout(0, 0));
        _notificationRow.setVisible(false);
        
        JLabel loadingLabel = new JLabel("Initializing...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingLabel.setFont(loadingLabel.getFont().deriveFont(Font.BOLD, 16));
        _scrollablePanel.add(loadingLabel, BorderLayout.CENTER);
        
        JProgressBar loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        _scrollablePanel.add(loadingBar, BorderLayout.SOUTH);
        
        _worker.submit(null, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    //Retrieve data on the students
                    final StudentData data = retrieveInfo();
                    _currentStudentData = data;
                    
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            _scrollablePanel.removeAll();
                            _scrollablePanel.setLayout(new BoxLayout(_scrollablePanel, BoxLayout.Y_AXIS));
                            
                            //For each student in the database, add a row
                            final JPanel studentsPanel = new JPanel();
                            studentsPanel.setLayout(new BoxLayout(studentsPanel, BoxLayout.Y_AXIS));
                            _scrollablePanel.add(studentsPanel);
                            for(DbStudent student : data.getStudentsInDb())
                            {
                                studentsPanel.add(new StudentRow(StudentPanel.this, studentsPanel, _worker, data, student));
                            }

                            //Allow students to be added manually
                            JPanel buttonPanel = new JPanel(new BorderLayout(0, 0));
                            buttonPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
                            buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 35));
                            JButton addStudentButton = new JButton("Add Student");
                            buttonPanel.add(Box.createHorizontalStrut(5), BorderLayout.WEST);
                            buttonPanel.add(addStudentButton, BorderLayout.CENTER);
                            buttonPanel.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
                            _scrollablePanel.add(buttonPanel);
                            addStudentButton.addActionListener(new ActionListener()
                            {
                                @Override
                                public void actionPerformed(ActionEvent ae)
                                {
                                    DbStudent student = new DbStudent("", "", "", "");
                                    StudentRow row = new StudentRow(StudentPanel.this, studentsPanel, _worker, data, student);
                                    studentsPanel.add(row);
                                    row.grabFocus();

                                    studentsPanel.revalidate();

                                    //After revalidation has occurred, scroll to the bottom so that the Add Students button is
                                    //still on screen
                                    EventQueue.invokeLater(new Runnable()
                                    {
                                        public void run()
                                        {
                                            _verticalScrollBar.setValue(_verticalScrollBar.getMaximum());
                                        }
                                    });
                                }
                            });

                            //Show notification row
                            data.updateNotificationRow();
                            _notificationRow.setVisible(true);
                        }
                    });
                }
                catch(final StudentDataException e)
                {
                    EventQueue.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            _scrollablePanel.removeAll();
                            _scrollablePanel.setLayout(new BorderLayout(0, 0));

                            //Error message
                            JLabel errorLabel = new JLabel(e.getMessage());
                            errorLabel.setForeground(Color.RED);
                            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                            errorLabel.setFont(errorLabel.getFont().deriveFont(Font.BOLD, 16));
                            _scrollablePanel.add(errorLabel, BorderLayout.CENTER);
                            
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
                            _scrollablePanel.add(retryPanel, BorderLayout.SOUTH);
                            
                            //Hide notification row
                            _notificationRow.setVisible(false); 
                            
                            //Force visual update to reflect these changes
                            _scrollablePanel.repaint();
                            _scrollablePanel.revalidate();
                            
                            //Show error report
                            new ErrorView(e);
                        }
                    });
                }
            }
        });
    }
    
    /**
     * Exception used just within {@code StudentPanel} to convey useful error messages that will be shown to the user.
     */
    private static class StudentDataException extends Exception
    {
        StudentDataException(String msg, Exception cause)
        {
            super(msg, cause);
        }
    }
    
    private StudentData retrieveInfo() throws StudentDataException
    {
        try
        {
            Set<DbStudent> students = Allocator.getDatabase().getStudents();
            Set<String> logins = null;
            try
            {
                logins = new HashSet<String>(Allocator.getUserServices().getStudentLogins());
            }
            catch(NativeException e) { }
            
            return new StudentData(_notificationRow, students, logins);
        }
        catch(SQLException e)
        {
            throw new StudentDataException("Unable to retrieve student information from the database", e);
        }
    }
    
    private static class StudentData
    {
        private final NotificationRow _notificationRow;
        
        /**
         * A set of the students in the database. This is sorted according to a comparator - see the constructor for
         * details. Students will be added to this through user interaction as students are manually added.
         */
        private final SortedSet<DbStudent> _studentsInDb;
        
        /**
         * A cache of the student logins in the database. Used to prevent the need to iterate through all of the
         * {@link DbStudent} objects in {@link #_studentsInDb}. This optimization is necessary because a contains check
         * on the login occurs on every modification (typically every keystroke) made by the user using a login field.
         */
        private final Set<String> _loginsInDb;
        
        /**
         * A set of the logins in the csXXXstudent group that are not in the database.
         */
        private Set<String> _loginsInGroupNotInDb;
        
        /**
         * Locks access to {@link #_loginsInDb}, {@link #_studentsInDb}, and {@link #_loginsInGroupNotInDb} so that
         * they may be updated atomically.
         */
        private final Lock _lock = new ReentrantLock();
        
        /**
         * Immutable set of the logins in the csXXXstudent group.
         */
        private final ImmutableSet<String> _loginsInGroup;
        
        /**
         * If there is a csXXXstudent group for this course. If there is not, {@link #_loginsInGroup} will be an empty
         * set.
         */
        private final boolean _studentGroupExists;
        
        StudentData(NotificationRow notifactionRow, Collection<DbStudent> studentsInDb, Iterable<String> loginsInGroup)
        {
            _notificationRow = notifactionRow;
            _loginsInGroup = (loginsInGroup == null ? ImmutableSet.<String>of() : ImmutableSet.copyOf(loginsInGroup));
            _studentGroupExists = (loginsInGroup != null);
            
            _studentsInDb = new TreeSet<DbStudent>(new Comparator<DbStudent>()
            {
                @Override
                public int compare(DbStudent s1, DbStudent s2)
                {
                    int comparison;
                    if(_loginsInGroup.contains(s1.getLogin()) && !_loginsInGroup.contains(s2.getLogin()))
                    {
                        comparison = 1;
                    }
                    else if(!_loginsInGroup.contains(s1.getLogin()) && _loginsInGroup.contains(s2.getLogin()))
                    {
                        comparison = -1;
                    }
                    else
                    {
                        comparison = s1.getLogin().compareTo(s2.getLogin());
                    }
                    
                    return comparison;
                }
            });
            _studentsInDb.addAll(studentsInDb);
            
            _loginsInDb = new HashSet<String>();
            for(DbStudent student : _studentsInDb)
            {
                _loginsInDb.add(student.getLogin());
            }
            
            _loginsInGroupNotInDb = new HashSet<String>();
            for(String login : _loginsInGroup)
            {
                if(!_loginsInDb.contains(login))
                {
                    _loginsInGroupNotInDb.add(login);
                }
            }
        }
        
        public boolean dbContainsLogin(String login)
        {
            _lock.lock();
            
            try
            {
                return _loginsInDb.contains(login);
            }
            finally
            {
                _lock.unlock();
            }
        }
        
        public boolean studentGroupContainsLogin(String login)
        {
            return _loginsInGroup.contains(login);
        }
        
        public void notifyStudentToBeAddedOrUpdated(String oldLogin, DbStudent student)
        {
            _lock.lock();
            
            try
            {
                _studentsInDb.add(student);
                _loginsInDb.add(student.getLogin());
                
                if(_loginsInGroup.contains(student.getLogin()))
                {
                    _loginsInGroupNotInDb.remove(student.getLogin());
                }
                
                if(_loginsInGroup.contains(oldLogin))
                {
                    _loginsInGroupNotInDb.add(oldLogin);
                }
                
                updateNotificationRow();
            }
            finally
            {
                _lock.unlock();
            }
        }
        
        public void studentFailedToBeAdded(DbStudent student)
        {
            _lock.lock();
            
            try
            {
                _studentsInDb.remove(student);
                _loginsInDb.remove(student.getLogin());
                
                if(_loginsInGroup.contains(student.getLogin()))
                {
                    _loginsInGroupNotInDb.add(student.getLogin());
                }
                
                updateNotificationRow();
            }
            finally
            {
                _lock.unlock();
            }
        }
        
        public void studentFailedToBeUpdated(String revertedLogin, DbStudent student)
        {
            _lock.lock();
            
            try
            {
                _loginsInDb.remove(student.getLogin());
                _loginsInDb.add(student.getLogin());
                
                if(_loginsInGroup.contains(student.getLogin()))
                {
                    _loginsInGroupNotInDb.add(student.getLogin());
                }
                
                if(_loginsInGroup.contains(revertedLogin))
                {
                    _loginsInGroupNotInDb.remove(revertedLogin);
                }
                
                updateNotificationRow();
            }
            finally
            {
                _lock.unlock();
            }
        }
        
        /**
         * An immutable {@link Iterable} of the student rows in the database.
         * 
         * @return 
         */
        public Iterable<DbStudent> getStudentsInDb()
        {
            return ImmutableList.copyOf(_studentsInDb);
        }
        
        public Set<String> getLoginsInGroupNotInDb()
        {
            return ImmutableSet.copyOf(_loginsInGroupNotInDb);
        }
        
        /**
         * Whether a student group exists. This should always be accurate, but it's possible that due to an error in
         * native code a student group actually exists but cakehat failed at retrieving and is unable to distinguish
         * the two situations.
         * 
         * @return 
         */
        public boolean doesStudentGroupExist()
        {
            return _studentGroupExists;
        }
        
        void updateNotificationRow()
        {   
            final int numLoginsInGroupNotInDb = _loginsInGroupNotInDb.size();
            
            //Update the notification row component on the UI thread to reflect the number of logins in the student
            //group that are not in the database
            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    _notificationRow.updateInfo(_studentGroupExists, numLoginsInGroupNotInDb);
                }
            });
        }
    }
    
    private static class NotificationRow extends JPanel
    {
        private static final String STUDENT_GROUP_NAME = Allocator.getCourseInfo().getStudentGroup();
        private final UniqueElementSingleThreadWorker _worker;
        private final StudentPanel _studentPanel;
        private final JLabel _statusLabel;
        private final JButton _addStudentsButton;
        
        NotificationRow(UniqueElementSingleThreadWorker worker, StudentPanel studentPanel)
        {
            this.setLayout(new BorderLayout(0, 0));
            
            _worker = worker;
            _studentPanel = studentPanel;
            
            _statusLabel = new JLabel();
            _statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            _statusLabel.setFont(_statusLabel.getFont().deriveFont(Font.BOLD, 12));
            this.add(_statusLabel, BorderLayout.CENTER);
            
            _addStudentsButton = new JButton();
            _addStudentsButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    addStudents();
                }
            });
            _addStudentsButton.setVisible(false);
            this.add(_addStudentsButton, BorderLayout.EAST);
            
            this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        }
        
        void updateInfo(boolean studentGroupExists, int inGroupNotInDb)
        {
            if(studentGroupExists)
            {   
                //All members in the csXXXstudent group are in the database
                if(inGroupNotInDb == 0)
                {
                    _statusLabel.setText("All members of the " + STUDENT_GROUP_NAME + " group have been added");
                    _statusLabel.setForeground(Color.BLACK);
                    _addStudentsButton.setVisible(false);
                }
                //One or more members of the csXXXstudent group is not in the database
                else
                {
                    if(inGroupNotInDb == 1)
                    {
                        _statusLabel.setText("1 member of the " + STUDENT_GROUP_NAME + " group has not been added");
                        _addStudentsButton.setText("Add Student");
                    }
                    else
                    {
                        _statusLabel.setText(inGroupNotInDb + " members of the " + STUDENT_GROUP_NAME +
                                " group have not been added");
                        _addStudentsButton.setText("Add Students");
                    }
                    _statusLabel.setForeground(Color.RED);
                    
                    _addStudentsButton.setVisible(true);
                }
            }
            else
            {
                _statusLabel.setText("No information on " + STUDENT_GROUP_NAME + " group could be retrieved");
                _statusLabel.setForeground(Color.BLACK);
                
                _addStudentsButton.setVisible(false);
            }
            
            this.repaint();
            this.revalidate();
        }
        
        private void addStudents()
        {   
            //Add students to the database while blocking the UI thread
            try
            {   
                //Wait for the worker thread to finish all of its tasks
                _worker.blockOnQueuedTasks();
                
                //Now back on the UI thread there are no pending database calls and none can be made while the code
                //below is running because tasks are only being adding to the worker queue from the UI thread
                
                List<String> loginsInGroupNotInDb = new ArrayList<String>(_studentPanel
                        .getCurrentStudentData().getLoginsInGroupNotInDb());
                
                //Retrieve the names for the logins and build DbStudent objects from them
                String emailDomain = Allocator.getConstants().getEmailDomain();
                List<DbStudent> students = new ArrayList<DbStudent>();
                for(String login : loginsInGroupNotInDb)
                {
                    String firstName, lastName;
                    try
                    {
                        String[] nameParts = Allocator.getUserUtilities().getUserName(login).split(" ");
                        firstName = nameParts[0];
                        lastName = nameParts[nameParts.length - 1];
                    }
                    catch(NativeException e)
                    {
                        new ErrorView(e, "Unable to retrieve name for student with login " + login);

                        firstName = "[Unknown]";
                        lastName = "[Unknown]";
                    }
                    students.add(new DbStudent(login, firstName, lastName, login + "@" + emailDomain));
                }
                
                //Display students to be added to the user for confirmation, and allow for not adding a given student
                Set<DbStudent> studentsToBeAdded = AddStudentsConfirmationDialog.showConfirmation(students);
                
                //If there are students to be added - add them
                if(!studentsToBeAdded.isEmpty())
                {
                    try
                    {
                        Allocator.getDatabase().putStudents(studentsToBeAdded);
                    }
                    catch(SQLException e)
                    {
                        new ErrorView("Failed to add students to the database");
                    }
                    
                    //Re-initialize to show all of the added students
                    _studentPanel.initialize();
                }
            }
            //If unable to wait, re-initialize to guarantee the update is shown visually
            catch(InterruptedException e)
            {
                _studentPanel.initialize();
            }
        }
                
        @Override
        public Dimension getMaximumSize()
        {
            Dimension size = getPreferredSize();
            size.width = Short.MAX_VALUE;
            
            return size;
        }
    }
    
    private static class AddStudentsConfirmationDialog extends JDialog
    {
        static Set<DbStudent> showConfirmation(List<DbStudent> students)
        {
            Set<DbStudent> studentsSet = new TreeSet<DbStudent>(new Comparator<DbStudent>()
            {
                @Override
                public int compare(DbStudent s1, DbStudent s2)
                {
                    return s1.getLogin().compareTo(s2.getLogin());
                }
            });
            studentsSet.addAll(students);
            
            AddStudentsConfirmationDialog dialog = new AddStudentsConfirmationDialog(studentsSet);
            
            return studentsSet;
        }
        
        private AddStudentsConfirmationDialog(final Set<DbStudent> students)
        {
            super(Allocator.getGeneralUtilities().getFocusedFrame(), "Students To Be Added", true);
            
            this.setLayout(new BorderLayout(0, 0));
            
            //Put a 10 pixel pad around the content
            JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
            this.add(contentPanel, BorderLayout.CENTER);
            this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
            this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
            this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
            this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
            
            //Header row
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            contentPanel.add(topPanel, BorderLayout.NORTH);
            JPanel infoPanel = new JPanel(new BorderLayout(0, 0));
            JLabel infoLabel = new JLabel("<html>The following students will be added. Uncheck a student to prevent " +
                    "that student from being added.</html>");
            infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            infoPanel.add(infoLabel, BorderLayout.CENTER);
            topPanel.add(infoPanel);
            topPanel.add(Box.createVerticalStrut(10));
            final JPanel headerPanel = new JPanel(new BorderLayout(0, 0));
            JPanel labelRow = new JPanel(new GridLayout(1, 5));
            headerPanel.add(labelRow, BorderLayout.CENTER);
            labelRow.add(makeHeaderLabel("Will Be Added", "If unchecked the student will not be added."));
            labelRow.add(makeHeaderLabel("Login", "The student's CS login."));
            labelRow.add(makeHeaderLabel("First Name", "The student's first name, for display purposes only."));
            labelRow.add(makeHeaderLabel("Last Name", "The student's last name, for display purposes only."));
            labelRow.add(makeHeaderLabel("Email", "Used to email the student grades and grade reports."));
            final Filler scrollPanePadding = new Filler(new Dimension(), new Dimension(), new Dimension()); 
            headerPanel.add(scrollPanePadding, BorderLayout.EAST); 
            topPanel.add(headerPanel);
            topPanel.add(Box.createVerticalStrut(3));
            topPanel.add(new JSeparator());
            topPanel.add(Box.createVerticalStrut(3));
            
            //Students
            JPanel scrollablePanel = new JPanel();
            JScrollPane scrollPane = new JScrollPane(scrollablePanel);
            scrollPane.getViewport().addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(ComponentEvent ce)
                {
                    Dimension paddingDim = new Dimension(headerPanel.getWidth() - ce.getComponent().getWidth(), 0);
                    scrollPanePadding.changeShape(paddingDim, paddingDim, paddingDim);
                }
            });
            scrollPane.setBorder(null);
            contentPanel.add(scrollPane, BorderLayout.CENTER);
            scrollablePanel.setLayout(new BoxLayout(scrollablePanel, BoxLayout.Y_AXIS));
            
            for(final DbStudent student : students)
            {
                JPanel studentRow = new JPanel(new GridLayout(1, 5))
                {
                    @Override
                    public Dimension getMaximumSize()
                    {
                        Dimension size = getPreferredSize();
                        size.width = Short.MAX_VALUE;

                        return size;
                    }  
                };
                scrollablePanel.add(studentRow);
                
                final JCheckBox addCheckBox = new JCheckBox("", true);
                addCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
                addCheckBox.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        if(addCheckBox.isSelected())
                        {
                            students.add(student);
                        }
                        else
                        {
                            students.remove(student);
                        }
                    }
                });
                studentRow.add(addCheckBox);
                
                studentRow.add(makeStudentLabel(student.getLogin()));
                studentRow.add(makeStudentLabel(student.getFirstName()));
                studentRow.add(makeStudentLabel(student.getLastName()));
                studentRow.add(makeStudentLabel(student.getEmailAddress()));
            }
            
            //Buttons to Add or Cancel
            JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
            contentPanel.add(bottomPanel, BorderLayout.SOUTH);
            
            bottomPanel.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
            
            JPanel buttonPanel = new JPanel(new GridLayout(1, 5));
            bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
            buttonPanel.add(Box.createHorizontalBox());
            
            JButton addButton = new JButton("Add");
            addButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    AddStudentsConfirmationDialog.this.dispose();
                }
            });
            buttonPanel.add(addButton);
            
            buttonPanel.add(Box.createHorizontalBox());
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    students.clear();
                    AddStudentsConfirmationDialog.this.dispose();
                }
            });
            buttonPanel.add(cancelButton);
            
            buttonPanel.add(Box.createHorizontalBox());
            
            //Show
            this.setResizable(true);
            this.setMinimumSize(new Dimension(800, 360));
            this.setPreferredSize(new Dimension(800, 360));
            this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            this.setAlwaysOnTop(true);
            this.setLocationRelativeTo(Allocator.getGeneralUtilities().getFocusedFrame());
            this.setVisible(true);   
        }
        
        private JLabel makeHeaderLabel(String text, String tooltip)
        {
            JLabel label = new JLabel(text);
            label.setToolTipText(tooltip);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14));
            
            return label;
        }
        
        private JLabel makeStudentLabel(String text)
        {
            JLabel label = new JLabel(text);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            
            return label;
        }
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
            labelPanel.add(makeLabel("Login", "The student's CS or Banner login."));
            labelPanel.add(makeLabel("First Name", "The student's first name, for display purposes only."));
            labelPanel.add(makeLabel("Last Name", "The student's last name, for display purposes only."));
            labelPanel.add(makeLabel("Email", "Used to email the student grades and grade reports."));
            labelPanel.add(makeLabel("Enabled", "Disabled students are not shown in the grader and admin views."));
            
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
            JLabel label = new JLabel(text);
            label.setToolTipText(tooltip);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(label.getFont().deriveFont(Font.BOLD, 14));
            
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
    
    private static class StudentRow extends JPanel
    {
        private final StudentData _studentData;
        private final StudentPanel _studentPanel;
        private final JPanel _studentsPanel;
        private final UniqueElementSingleThreadWorker _worker;
        private final DbStudent _student;
        private final ValidatingTextField _loginField, _firstNameField, _lastNameField, _emailField;
        private final JCheckBox _enabledCheckBox;
        
        StudentRow(StudentPanel studentPanel, JPanel studentsPanel,
                   UniqueElementSingleThreadWorker worker, StudentData studentData, DbStudent student)
        {   
            _studentPanel = studentPanel;
            _studentsPanel = studentsPanel;
            _worker = worker;
            _studentData = studentData;
            _student = student;
            
            this.setLayout(new GridLayout(1, 5));
            
            final Set<Component> inputComponents = new HashSet<Component>();
            FocusAdapter removeRowIfIncomplete = new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent fe)
                {
                    //If this student has yet to be stored in the database, focus is removed from one of the components
                    //of this row, and it is not in a savable state, then remove the row
                    //Once a student has been stored in the database it is not possible to put that student into a
                    //permanently unsavable state because fields with errors will be reverted upon loss of focus
                    //This will occur prior to this focus handler being notified
                    if(_student.getId() == null && !inputComponents.contains(fe.getOppositeComponent()) && !isStateSavable())
                    {   
                        _studentsPanel.remove(StudentRow.this);
                        _studentsPanel.revalidate();
                    }
                }
            };
            
            //Login
            _loginField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _student.getLogin();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    ValidationResult result;
                    
                    //If the login is the empty string
                    if(value.isEmpty())
                    {
                        result = new ValidationResult(ValidationState.ERROR, "A login must be provided");
                    }
                    //If the login is not unique and not the current login then revert the field
                    else if(!_student.getLogin().equals(value) && _studentData.dbContainsLogin(value))
                    {
                        result = new ValidationResult(ValidationState.ERROR,
                                "There is already a student with login " + value + " in the database");
                    }
                    //Login not in student group
                    else if(_studentData.doesStudentGroupExist() && !_studentData.studentGroupContainsLogin(value))
                    {
                        result = new ValidationResult(ValidationState.WARNING,
                                value + " is not a member of " + Allocator.getCourseInfo().getStudentGroup());
                    }
                    //Otherwise the login has no known issues
                    else
                    {
                        result = ValidationResult.NO_ISSUE;
                    }
                    
                    return result;
                }

                @Override
                protected void applyChange(String newValue)
                {
                    if(isStateSavable())
                    {
                        StudentRunnable runnable = new StudentRunnable(_student);
                        String oldLogin = _student.getLogin();
                        _student.setLogin(newValue);
                        _studentData.notifyStudentToBeAddedOrUpdated(oldLogin, _student);
                        _worker.submit(WORKER_TAG, runnable);
                    }
                    else
                    {   
                        _student.setLogin(newValue);
                    }
                }
            };
            inputComponents.add(_loginField);
            _loginField.addFocusListener(removeRowIfIncomplete);
            this.add(_loginField);
            
            //First name
            _firstNameField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _student.getFirstName();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    return ValidationResult.validateNotEmpty(value);
                }

                @Override
                protected void applyChange(String newValue)
                {
                    if(isStateSavable())
                    {
                        StudentRunnable runnable = new StudentRunnable(_student);
                        _student.setFirstName(newValue);
                        _studentData.notifyStudentToBeAddedOrUpdated(null, _student);
                        _worker.submit(WORKER_TAG, runnable);
                    }
                    else
                    {
                        _student.setFirstName(newValue);
                    }
                }
            };
            inputComponents.add(_firstNameField);
            _firstNameField.addFocusListener(removeRowIfIncomplete);
            this.add(_firstNameField);
            
            //Last name
            _lastNameField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _student.getLastName();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    return ValidationResult.validateNotEmpty(value);
                }
                
                @Override
                protected void applyChange(String newValue)
                {
                    if(isStateSavable())
                    {
                        StudentRunnable runnable = new StudentRunnable(_student);
                        _student.setLastName(newValue);
                        _studentData.notifyStudentToBeAddedOrUpdated(null, _student);
                        _worker.submit(WORKER_TAG, runnable);
                    }
                    else
                    {
                        _student.setLastName(newValue);
                    }
                    
                }
            };
            inputComponents.add(_lastNameField);
            _lastNameField.addFocusListener(removeRowIfIncomplete);
            this.add(_lastNameField);      
            
            //Email
            _emailField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _student.getEmailAddress();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    ValidationResult result;
                    try
                    {
                        new InternetAddress(value, true);
                        result = ValidationResult.NO_ISSUE;
                    }
                    catch(AddressException e)
                    {
                        result = new ValidationResult(ValidationState.ERROR, "Invalid email address");
                    }
                    
                    return result;
                }

                @Override
                protected void applyChange(String newValue)
                {
                    if(isStateSavable())
                    {
                        StudentRunnable runnable = new StudentRunnable(_student);
                        _student.setEmailAddress(newValue);
                        _studentData.notifyStudentToBeAddedOrUpdated(null, _student);
                        _worker.submit(WORKER_TAG, runnable);
                    }
                    else
                    {
                        _student.setEmailAddress(newValue);
                    }
                }
            };
            inputComponents.add(_emailField);
            _emailField.addFocusListener(removeRowIfIncomplete);
            this.add(_emailField);
            
            //Enabled
            _enabledCheckBox = new JCheckBox("", _student.isEnabled());
            _enabledCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            _enabledCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    if(isStateSavable())
                    {
                        StudentRunnable runnable = new StudentRunnable(_student);
                        _student.setEnabled(_enabledCheckBox.isSelected());
                        _studentData.notifyStudentToBeAddedOrUpdated(null, _student);
                        _worker.submit(WORKER_TAG, runnable);
                    }
                    else
                    {
                        _student.setEnabled(_enabledCheckBox.isSelected());
                    }
                }
            });
            inputComponents.add(_enabledCheckBox);
            _enabledCheckBox.addFocusListener(removeRowIfIncomplete);
            this.add(_enabledCheckBox);
        }
        
        @Override
        public void grabFocus()
        {
            _loginField.grabFocus();
        }
        
        /**
         * Determines if the current inputs are valid such that they can be saved.
         * 
         * @return 
         */
        private boolean isStateSavable()
        {
            boolean canSave = true;
            
            String login = _loginField.getText();
            String email = _emailField.getText();
            
            if(login.isEmpty())
            {
                canSave = false;
            }
            //If the login has changed from the one currently stored and that login is already in the database
            if(!_student.getLogin().equals(login) && _studentData.dbContainsLogin(login))
            {
                canSave = false;
            }
            
            //If the email address has a valid format
            try
            {
                new InternetAddress(email, true);
            }
            catch(AddressException e)
            {
                canSave = false;
            }
            
            return canSave;
        }
        
        @Override
        public Dimension getMaximumSize()
        {
            Dimension size = getPreferredSize();
            size.width = Short.MAX_VALUE;
            
            return size;
        }
        
        private class StudentRunnable extends DbRunnable
        {
            //Preserve the current values so if a failure occurs when writing to the database, values can be reverted
            private final String _login, _firstName, _lastName, _emailAddress;
            private final boolean _enabled;
            
            StudentRunnable(DbStudent student)
            {   
                super(_worker, student);
                
                _login = student.getLogin();
                _firstName = student.getFirstName();
                _lastName = student.getLastName();
                _emailAddress = student.getEmailAddress();
                _enabled = student.isEnabled();
            }

            @Override
            public void dbCall() throws SQLException
            {
                Allocator.getDatabase().putStudents(ImmutableSet.of(_student));
            }

            @Override
            public void onDbCallFailure()
            {   
                //If the login was to be updated
                if(!_login.equals(_student.getLogin()))
                {
                    //If the student has never been added to the database
                    if(_student.getId() == null)
                    {
                        _studentData.studentFailedToBeAdded(_student);
                    }
                    //Failed to update login but is in database
                    else
                    {
                        _studentData.studentFailedToBeUpdated(_login, _student);
                    }
                }
                
                //If the student was not in the database prior and thefore failed to be added, visually remove the row
                if(_student.getLogin() == null)
                {
                    _studentsPanel.remove(StudentRow.this);
                    _studentsPanel.revalidate();
                }
                //Otherwise, revert everything to what it was before
                else
                {
                    _student.setLogin(_login);
                    _loginField.setText(_login);

                    _student.setFirstName(_firstName);
                    _firstNameField.setText(_firstName);

                    _student.setLastName(_lastName);
                    _lastNameField.setText(_lastName);

                    _student.setEmailAddress(_emailAddress);
                    _emailField.setText(_emailAddress);

                    _student.setEnabled(_enabled);
                    _enabledCheckBox.setSelected(_enabled);
                }
            }

            @Override
            public void onFinalFailureNow()
            {
                _worker.cancel(WORKER_TAG);
            }

            @Override
            public void onFinalFailureLater()
            {
                _studentPanel.initialize();
            }
        }
    }
}