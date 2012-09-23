package cakehat.views.config;

import cakehat.Allocator;
import cakehat.database.DbAction;
import cakehat.database.DbActionProperty;
import cakehat.database.DbAssignment;
import cakehat.database.DbGradableEvent;
import cakehat.database.DbPart;
import cakehat.assignment.ActionDescription;
import cakehat.assignment.DeadlineInfo;
import cakehat.assignment.Task;
import cakehat.assignment.TaskProperty;
import cakehat.assignment.TaskRepository;
import cakehat.logging.ErrorReporter;
import cakehat.views.config.ValidationResult.ValidationState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.joda.time.DateTime;
import org.joda.time.Period;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import support.ui.DateTimeControl;
import support.ui.DescriptionProvider;
import support.ui.DocumentAdapter;
import support.ui.DnDList;
import support.ui.DnDListener;
import support.ui.FixedWidthJPanel;
import support.ui.FormattedLabel;
import support.ui.GenericJComboBox;
import support.ui.ModalDialog;
import support.ui.ModalJFrameHostHelper.CloseAction;
import support.ui.PaddingPanel;
import support.ui.PartialDescriptionProvider;
import support.ui.PeriodControl;
import support.ui.PreferredHeightJPanel;
import support.ui.SelectionListener;
import support.ui.SelectionListener.SelectionAction;

/**
 *
 * @author jak2
 */
class AssignmentPanel extends JPanel
{
    static final String WORKER_TAG = "ASSIGNMENT";
    
    private final ConfigManagerView _configManager;
    private final UniqueElementSingleThreadWorker _worker;
    
    private final DnDList<DbAssignment> _assignmentList;
    private final JPanel _selectedAssignmentPanel;
    private final Map<DbAssignment, JPanel> _assignmentPanels = new WeakHashMap<DbAssignment, JPanel>();
    
    /**
     * A map of {@link DbAssignment} objects to the currently user supplied name. This is not necessarily the name
     * stored in the DbAssignment object. It will be different if the user is currently modifying the input field for
     * the assignment.
     */
    private final ConcurrentMap<DbAssignment, String> _asgnDisplayName = new ConcurrentHashMap<DbAssignment, String>();
    
    public AssignmentPanel(ConfigManagerView configManager, UniqueElementSingleThreadWorker worker)
    {
        _configManager = configManager;
        _worker = worker;
        
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        _assignmentList = new DnDList<DbAssignment>();
        this.setupAssignmentList();
        
        this.add(Box.createHorizontalStrut(5));
        
        _selectedAssignmentPanel = new JPanel();
        this.add(_selectedAssignmentPanel);
        
        this.initialize();
    }
    
    private void setupAssignmentList()
    {
        _assignmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _assignmentList.addDnDSource(_assignmentList);
        _assignmentList.setDescriptionProvider(new PartialDescriptionProvider<DbAssignment>()
        {
            @Override
            public String getDisplayText(DbAssignment asgn)
            {
                String name = _asgnDisplayName.get(asgn);
                if(name == null)
                {
                    name = asgn.getName();
                }
                //A blank string will result in the cell being given an abnormally short height - so use a space instead
                if(name.isEmpty())
                {
                    name = " ";
                }
            
                return name;
            }
        });
        _assignmentList.addDnDListener(new DnDListener<DbAssignment>()
        {
            @Override
            public void valuesAdded(Map<Integer, DbAssignment> added) { }

            @Override
            public void valuesRemoved(List<DbAssignment> removed) { }

            @Override
            public void valuesReordered(Map<Integer, DbAssignment> values)
            {
                final Set<DbAssignment> reorderedAssignments = getReorderedAssignments();
                if(!reorderedAssignments.isEmpty())
                {
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putAssignments(reorderedAssignments);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder assignments";
                        }
                    });
                }
            }
        });
        
        JPanel listPanel = new JPanel(new BorderLayout(0, 0));
        listPanel.setMinimumSize(new Dimension(180, Short.MIN_VALUE));
        listPanel.setPreferredSize(new Dimension(180, Short.MAX_VALUE));
        listPanel.setMaximumSize(new Dimension(180, Short.MAX_VALUE));
        this.add(listPanel);
        
        final JScrollPane listPane = new JScrollPane(_assignmentList);
        listPanel.add(listPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new BorderLayout(0, 0));
        buttonPanel.add(Box.createVerticalStrut(5), BorderLayout.NORTH);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                //Determine the order and the default name for the new assignment
                int maxOrder = -1;
                HashSet<String> assignmentNames = new HashSet<String>();
                
                List<DbAssignment> assignments = _assignmentList.getListData();
                for(DbAssignment asgn : assignments)
                {
                    maxOrder = Math.max(maxOrder, asgn.getOrder());
                    assignmentNames.add(asgn.getName());
                }
                
                int asgnOrder = maxOrder + 1;
                String defaultName = "Assignment " + asgnOrder;
                if(assignmentNames.contains(defaultName))
                {   
                    //Attempt to generate a unique name - but don't loop forever
                    //Non-unique names are allowed, but a warning will be shown
                    for(int defaultNameEnding = asgnOrder + 1; defaultNameEnding < 1000; defaultNameEnding++)
                    {
                        defaultName = "Assignment " + defaultNameEnding;
                        if(assignmentNames.contains(defaultName))
                        {
                            break;
                        }
                    }
                }
                
                //Create the assignment
                final DbAssignment asgn = new DbAssignment(defaultName, asgnOrder);
                
                //Add the assignment visually and to the database
                _assignmentList.getModel().addElement(asgn);
                _assignmentList.setSelectedValue(asgn);
                int listSize = _assignmentList.getModel().getSize();
                listPane.getViewport().scrollRectToVisible(_assignmentList.getCellBounds(listSize - 1, listSize));
                
                //Add the assignment on the worker thread
                _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                {
                    @Override
                    public void dbCall() throws SQLException
                    {
                        Allocator.getDatabase().putAssignments(ImmutableSet.of(asgn));
                    }

                    @Override
                    public String dbFailureMessage()
                    {
                        return "Unable to add assignment: " + asgn.getName();
                    }
                });
            }
        });
        buttonPanel.add(addButton, BorderLayout.WEST);
        
        buttonPanel.add(Box.createHorizontalBox(), BorderLayout.CENTER);
        
        final JButton deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                final DbAssignment asgn = _assignmentList.getSelectedValue();
                
                boolean proceed = ModalDialog.showConfirmation(_configManager, "Delete Assignment",
                        "Deleting assignment " + asgn.getName() + " will delete all associated grades, " +
                        "extensions, exemptions, and groups.",
                        "Delete", "Cancel");
                if(proceed)
                {
                    //Remove the assignment from the visible list
                    _assignmentList.getModel().removeElement(asgn);

                    //Get a list of assignments that now have a new order as a result
                    final Set<DbAssignment> reorderedAssignments = getReorderedAssignments();

                    //Delete the assignment on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().removeAssignments(ImmutableSet.of(asgn));

                            //This absolutely needs to occur after the delete call so in case that fails this
                            //code will not execute
                            Allocator.getDatabase().putAssignments(reorderedAssignments);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to delete assignment: " + asgn.getName();
                        }
                    });
                }
            }
        });
        buttonPanel.add(deleteButton, BorderLayout.EAST);
        
        _assignmentList.addListSelectionListener(new ListSelectionListener()
        {
            @Override
            public void valueChanged(ListSelectionEvent lse)
            {
                DbAssignment selectedAsgn = _assignmentList.getSelectedValue();
                deleteButton.setEnabled(selectedAsgn != null);
                assignmentSelectionChanged(selectedAsgn);
            }
        });
    }
    
    private void assignmentSelectionChanged(final DbAssignment selectedAsgn)
    {   
        _selectedAssignmentPanel.removeAll();
        _selectedAssignmentPanel.setLayout(new BorderLayout(0, 0));
        
        if(selectedAsgn == null)
        {
            _selectedAssignmentPanel.add(FormattedLabel.asSubheader("No Assignment Selected").centerHorizontally(),
                    BorderLayout.CENTER);
        }
        else
        {
            JPanel panel = _assignmentPanels.get(selectedAsgn);
            if(panel == null)
            {
                panel = new SelectedAssignmentPanel(selectedAsgn);
                _assignmentPanels.put(selectedAsgn, panel);
            }
            _selectedAssignmentPanel.add(panel, BorderLayout.CENTER);
        }
        
        //Force visual update to reflect these changes
        _selectedAssignmentPanel.repaint();
        _selectedAssignmentPanel.revalidate();
    }
    
    /**
     * Returns a list of the assignments in the DnDList whose visual order does not match the order stored in the
     * assignment object.
     * 
     * @return 
     */
    private Set<DbAssignment> getReorderedAssignments()
    {
        Set<DbAssignment> reorderedAssignments = new HashSet<DbAssignment>();
        List<DbAssignment> assignments = _assignmentList.getListData();
        for(int i = 0; i < assignments.size(); i++)
        {
            DbAssignment asgn = assignments.get(i);
            if(asgn.getOrder() != i)
            {
                asgn.setOrder(i);
                reorderedAssignments.add(asgn);
            }
        }

        return reorderedAssignments;
    }
    
    private abstract class ReinitializeRunnable implements Runnable
    {
        public void run()
        {
            try
            {
                dbCall();
            }
            catch(SQLException e)
            {
                reinitialize(e, dbFailureMessage());
            }
        }
        
        public abstract void dbCall() throws SQLException;
        
        public abstract String dbFailureMessage();
    }
    
    void reinitialize(final Throwable t, final String msg)
    {
        //Cancel everything and re-initialize
        _worker.cancel(WORKER_TAG);
        
        initialize();
        
        ErrorReporter.report(msg, t);
    }
    
    private void initialize()
    {
        _worker.submit(null, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    List<DbAssignment> assignments = new ArrayList<DbAssignment>(Allocator.getDatabase().getAssignments());
                    Collections.sort(assignments);
                    final List<DbAssignment> sortedAssignments = ImmutableList.copyOf(assignments);
                    
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            _assignmentPanels.clear();
                            _selectedAssignmentPanel.removeAll();
                            _assignmentList.setListData(sortedAssignments, true);
                            if(_assignmentList.getSelectedValue() == null)
                            {
                                _assignmentList.selectFirst();
                            }
                            
                            //Force visual update to reflect these changes
                            AssignmentPanel.this.repaint();
                            AssignmentPanel.this.revalidate();
                        }
                    });
                }
                catch(final SQLException e)
                {
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            _assignmentPanels.clear();
                            _selectedAssignmentPanel.removeAll();
                            _assignmentList.clearListData();
                            
                            //Force visual update to reflect these changes
                            AssignmentPanel.this.repaint();
                            AssignmentPanel.this.revalidate();
                            
                            ErrorReporter.report(e);
                        }
                    });
                }
            }
        });
    }
    
    private class SelectedAssignmentPanel extends JPanel
    {
        private final DbAssignment _asgn;
        private final ValidatingTextField _asgnNameField;
        private final JCheckBox _hasGroupsCheckBox;
        private final Map<DbGradableEvent, GradableEventPanel> _gradableEventPanels =
                new HashMap<DbGradableEvent, GradableEventPanel>();
        private final JPanel _gradableEventsPanel;
        private final JButton _addGradableEventButton;
        
        SelectedAssignmentPanel(DbAssignment selectedAsgn)
        {
            _asgn = selectedAsgn;
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            //Assignment name
            _asgnNameField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _asgn.getName();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    ValidationResult result = ValidationResult.NO_ISSUE;

                    if(value.isEmpty())
                    {
                        result = ValidationResult.TEXT_EMPTY;
                    }
                    else
                    {
                        //TODO: Make this more efficient
                        boolean uniqueName = true;
                        for(DbAssignment asgn : _assignmentList.getListData())
                        {
                            if(asgn != _asgn && asgn.getName().equals(value))
                            {
                                uniqueName = false;
                                break;
                            }
                        }
                        if(!uniqueName)
                        {
                            result = new ValidationResult(ValidationResult.ValidationState.WARNING, value + " is not " +
                                    "a unique assignment name");
                        }
                    }

                    return result;
                }

                @Override
                protected void applyChange(final String newValue)
                {
                    _asgn.setName(newValue);
                    _worker.submit(WORKER_TAG, new AssignmentRunnable());
                }
            };
            _asgnNameField.getDocument().addDocumentListener(new DocumentAdapter()
            {
                @Override
                public void modificationOccurred(DocumentEvent de)
                {
                    _asgnDisplayName.put(_asgn, _asgnNameField.getText());
                    _assignmentList.refreshList();
                }
            });
            _asgnNameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
            _asgnNameField.setAlignmentX(LEFT_ALIGNMENT);
            this.add(_asgnNameField);

            //Has groups
            _hasGroupsCheckBox = new JCheckBox("Group Assignment", _asgn.hasGroups());
            _hasGroupsCheckBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean selected = _hasGroupsCheckBox.isSelected();
                    
                    boolean proceed = ModalDialog.showConfirmation(_configManager, "Change Groups",
                            "Changing whether this assignment has groups will delete all grades, extensions, and " +
                            "exemptions for this assignment.",
                            "Change", "Cancel");
                    if(proceed)
                    {
                        _asgn.setHasGroups(selected);
                        _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                        {
                            @Override
                            public void dbCall() throws SQLException
                            {
                                Allocator.getDatabase().putAssignments(ImmutableSet.of(_asgn));
                                Allocator.getDatabase().removeGroups(Allocator.getDatabase().getGroups(_asgn.getId()));
                            }

                            @Override
                            public String dbFailureMessage()
                            {
                                return "Unable to change whether assignment " + _asgn.getName() + " has groups";
                            }
                        });
                    }
                    else
                    {
                        _hasGroupsCheckBox.setSelected(!selected);
                    }
                }
            });
            _hasGroupsCheckBox.setAlignmentX(LEFT_ALIGNMENT);
            this.add(_hasGroupsCheckBox);
            
            this.add(Box.createVerticalStrut(10));
            
            JLabel gradableEventsLabel = FormattedLabel.asSubheader("Gradable Events");
            gradableEventsLabel.setToolTipText("A gradable event represents a gradable product of work done by a " +
                    "group of one or more students. This could be, but is not limited to, paper handins, digital " +
                    "handins, labs, design checks, and exams.");
            this.add(gradableEventsLabel);
            
            _addGradableEventButton = new JButton("Add Gradable Event");
            _addGradableEventButton.setAlignmentX(LEFT_ALIGNMENT);
            _addGradableEventButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    //Determine the order and the default name for the new gradable event
                    int maxOrder = -1;
                    HashSet<String> geNames = new HashSet<String>();

                    for(DbGradableEvent ge : _asgn.getGradableEvents())
                    {
                        maxOrder = Math.max(maxOrder, ge.getOrder());
                        geNames.add(ge.getName());
                    }

                    int geOrder = maxOrder + 1;
                    String defaultName = "Gradable Event " + geOrder;
                    if(geNames.contains(defaultName))
                    {   
                        //Attempt to generate a unique name - but don't loop forever
                        //Non-unique names are allowed, but a warning will be shown
                        for(int defaultNameEnding = geOrder + 1; defaultNameEnding < 1000; defaultNameEnding++)
                        {
                            defaultName = "Gradable Event " + defaultNameEnding;
                            if(!geNames.contains(defaultName))
                            {
                                break;
                            }
                        }
                    }
                    
                    final DbGradableEvent gradableEvent = DbGradableEvent.build(_asgn, defaultName, geOrder);
                    GradableEventPanel panel = new GradableEventPanel(SelectedAssignmentPanel.this, gradableEvent);
                    _gradableEventPanels.put(gradableEvent, panel);
                    updateDisplayedGradableEventPanels();
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putGradableEvents(ImmutableSet.of(gradableEvent));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to insert or update " + gradableEvent.getName() + " into the database";
                        }
                    });
                }
            });
            
            _gradableEventsPanel = new FixedWidthJPanel();
            _gradableEventsPanel.setLayout(new BoxLayout(_gradableEventsPanel, BoxLayout.Y_AXIS));
            JScrollPane scrollPane = new JScrollPane(_gradableEventsPanel);
            scrollPane.setBorder(null);
            this.add(scrollPane);
            scrollPane.setAlignmentX(LEFT_ALIGNMENT);
            
            List<DbGradableEvent> gradableEvents = new ArrayList<DbGradableEvent>(_asgn.getGradableEvents());
            Collections.sort(gradableEvents);
            for(DbGradableEvent gradableEvent : gradableEvents)
            {
                GradableEventPanel panel = new GradableEventPanel(this, gradableEvent);
                _gradableEventPanels.put(gradableEvent, panel);
            }
            this.updateDisplayedGradableEventPanels();
        }
        
        void updateDisplayedGradableEventPanels()
        {
            _gradableEventsPanel.removeAll();
            
            List<DbGradableEvent> gradableEvents = new ArrayList<DbGradableEvent>(_asgn.getGradableEvents());
            Collections.sort(gradableEvents);
            
            for(DbGradableEvent event : gradableEvents)
            {
                GradableEventPanel panel = _gradableEventPanels.get(event);
                _gradableEventsPanel.add(panel);
                _gradableEventsPanel.add(Box.createVerticalStrut(5));
                panel.reorderOccurred();
            }
            
            JPanel addPanel = new PreferredHeightJPanel(new BorderLayout(0, 0));
            addPanel.add(_addGradableEventButton, BorderLayout.CENTER);
            _gradableEventsPanel.add(addPanel);
            
            //Force visual update to reflect these changes
            _gradableEventsPanel.repaint();
            _gradableEventsPanel.revalidate();
        }
        
        private class AssignmentRunnable extends ReinitializeRunnable
        {
            @Override
            public void dbCall() throws SQLException
            {
                Allocator.getDatabase().putAssignments(ImmutableSet.of(_asgn));
            }

            @Override
            public String dbFailureMessage()
            {
                return "Unable to insert or update assignment " + _asgn.getName();
            }
        }
    }
    
    private class GradableEventPanel extends PreferredHeightJPanel
    {
        private final DbGradableEvent _gradableEvent;
        
        private final JButton _upButton, _downButton, _addPartButton;
        private final ValidatingTextField _nameField, _directoryField;
        private final SelectedAssignmentPanel _asgnPanel;

        private final Map<DbPart, PartPanel> _partPanels = new HashMap<DbPart, PartPanel>();
        private final JPanel _partsPanel;
        
        GradableEventPanel(SelectedAssignmentPanel asgnPanel, DbGradableEvent gradableEvent)
        {
            _asgnPanel = asgnPanel;
            _gradableEvent = gradableEvent;
            
            this.setBackground(new Color(225, 225, 225));
            this.setBorder(BorderFactory.createEtchedBorder());
            
            this.setLayout(new BorderLayout(0, 0));
            
            JPanel contentPanel = new JPanel();
            contentPanel.setBackground(this.getBackground());
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            this.add(new PaddingPanel(contentPanel, 5, this.getBackground()), BorderLayout.CENTER);
            
            JPanel headlinePanel = new PreferredHeightJPanel(new BorderLayout(0, 0));
            headlinePanel.setAlignmentX(LEFT_ALIGNMENT);
            headlinePanel.setBackground(this.getBackground());
            contentPanel.add(headlinePanel);
            
            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(this.getBackground());
            headlinePanel.add(controlPanel, BorderLayout.EAST);
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _upButton = new JButton("↑");
            _upButton.setToolTipText("Move up");
            _upButton.setMargin(new Insets(1, 1, 1, 1));
            _upButton.setEnabled(false);
            _upButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _gradableEvent.setOrder(_gradableEvent.getOrder() - 1);
                    
                    ImmutableSet.Builder<DbGradableEvent> reorderedBuilder = ImmutableSet.builder();
                    reorderedBuilder.add(_gradableEvent);
                    for(DbGradableEvent event : _asgnPanel._asgn.getGradableEvents())
                    {
                        if(event != _gradableEvent && event.getOrder() == _gradableEvent.getOrder())
                        {
                            event.setOrder(event.getOrder() + 1);
                            reorderedBuilder.add(event);
                        }
                    }
                    final ImmutableSet<DbGradableEvent> reorderedEvents = reorderedBuilder.build();
                    
                    _asgnPanel.updateDisplayedGradableEventPanels();
                    
                    //Reorder gradable events on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putGradableEvents(reorderedEvents);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder gradable event: " + _gradableEvent.getName();
                        }
                    });
                }
            });
            controlPanel.add(_upButton);
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _downButton = new JButton("↓");
            _downButton.setToolTipText("Move down");
            _downButton.setMargin(new Insets(1, 1, 1, 1));
            _downButton.setEnabled(false);
            _downButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _gradableEvent.setOrder(_gradableEvent.getOrder() + 1);
                    
                    ImmutableSet.Builder<DbGradableEvent> reorderedBuilder = ImmutableSet.builder();
                    reorderedBuilder.add(_gradableEvent);
                    for(DbGradableEvent event : _asgnPanel._asgn.getGradableEvents())
                    {
                        if(event != _gradableEvent && event.getOrder() == _gradableEvent.getOrder())
                        {
                            event.setOrder(event.getOrder() - 1);
                            reorderedBuilder.add(event);
                        }
                    }
                    final ImmutableSet<DbGradableEvent> reorderedEvents = reorderedBuilder.build();
                    
                    _asgnPanel.updateDisplayedGradableEventPanels();
                    
                    //Reorder gradable events on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putGradableEvents(reorderedEvents);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder gradable event: " + _gradableEvent.getName();
                        }
                    });
                }
            });
            controlPanel.add(_downButton);
            
            this.reorderOccurred();
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            JButton deleteButton = new JButton("✗");
            deleteButton.setToolTipText("Delete");
            deleteButton.setMargin(new Insets(1, 1, 1, 1));
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean proceed = ModalDialog.showConfirmation(_configManager, "Delete Gradable Event",
                            "Deleting this Gradable Event will delete all grades, extensions and exemptions for this " +
                            "Gradable Event.", "Delete", "Cancel");
                    
                    if(proceed)
                    {
                        _asgnPanel._asgn.removeGradableEvent(_gradableEvent);
                        ImmutableSet.Builder<DbGradableEvent> reorderedBuilder = ImmutableSet.builder();
                        for(DbGradableEvent event : _asgnPanel._asgn.getGradableEvents())
                        {
                            if(event.getOrder() > _gradableEvent.getOrder())
                            {
                                event.setOrder(event.getOrder() - 1);
                                reorderedBuilder.add(event);
                            }
                        }
                        final ImmutableSet<DbGradableEvent> reorderedEvents = reorderedBuilder.build();
                        
                        //Force visual update to reflect these changes
                        _asgnPanel.updateDisplayedGradableEventPanels();
                        
                        //Delete the gradable event and reordered others on the worker thread
                        _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                        {
                            @Override
                            public void dbCall() throws SQLException
                            {
                                Allocator.getDatabase().removeGradableEvents(ImmutableSet.of(_gradableEvent));

                                //This absolutely needs to occur after the delete call so in case that fails this
                                //code will not execute
                                Allocator.getDatabase().putGradableEvents(reorderedEvents);
                            }

                            @Override
                            public String dbFailureMessage()
                            {
                                return "Unable to delete gradable event: " + _gradableEvent.getName();
                            }
                        });
                    }
                }
            });
            controlPanel.add(deleteButton);
            
            _nameField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _gradableEvent.getName();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    return ValidationResult.validateNotEmpty(value);
                }

                @Override
                protected void applyChange(String newValue)
                {
                    _gradableEvent.setName(newValue);
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putGradableEvents(ImmutableSet.of(_gradableEvent));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to insert or update " + _gradableEvent.getName();
                        }
                    });
                }
            };            
            headlinePanel.add(_nameField, BorderLayout.CENTER);
            
            contentPanel.add(Box.createVerticalStrut(10));
             
            contentPanel.add(FormattedLabel.asSubheader("Deadlines"));
            
            JPanel deadlinePanel = new DeadlinePanel(this.getBackground());
            deadlinePanel.setAlignmentX(LEFT_ALIGNMENT);
            contentPanel.add(deadlinePanel);
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            JLabel directoryLabel = FormattedLabel.asSubheader("Digital Handin Directory");
            directoryLabel.setToolTipText("This directory will be searched recursively for digital handins");
            contentPanel.add(directoryLabel);
            
            _directoryField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _gradableEvent.getDirectory() == null ? "" : _gradableEvent.getDirectory().getAbsolutePath();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    ValidationResult result = ValidationResult.NO_ISSUE;
                    
                    if(!value.isEmpty())
                    {
                        File file = new File(value);
                        
                        if(!file.exists())
                        {
                            result = new ValidationResult(ValidationState.WARNING,
                                    "Specified directory does not exist");
                        }
                        else
                        {
                            if(file.isFile())
                            {
                                result = new ValidationResult(ValidationState.WARNING,
                                        "Specified directory is a file");
                            }
                            else if(!file.canExecute())
                            {
                                result = new ValidationResult(ValidationState.WARNING,
                                        "Cannot read contents of specified directory");
                            }
                        }
                    }
                    
                    return result;
                }

                @Override
                protected void applyChange(String newValue)
                {
                    String currValue = _gradableEvent.getDirectory() == null ? "" : _gradableEvent.getDirectory().getAbsolutePath();
                    
                    if(!newValue.equals(currValue))
                    {   
                        if(newValue.isEmpty())
                        {
                            _gradableEvent.setDirectory(null);
                        }
                        else
                        {
                            _gradableEvent.setDirectory(new File(newValue));
                        }
                    
                        _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                        {
                            @Override
                            public void dbCall() throws SQLException
                            {
                                Allocator.getDatabase().putGradableEvents(ImmutableSet.of(_gradableEvent));
                            }

                            @Override
                            public String dbFailureMessage()
                            {
                                return "Unable to insert or update " + _gradableEvent.getName();
                            }
                        });
                    }
                }
            };
            _directoryField.setAlignmentX(LEFT_ALIGNMENT);
            _directoryField.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
            contentPanel.add(_directoryField);
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            JLabel partsLabel = FormattedLabel.asSubheader("Parts");
            partsLabel.setToolTipText("A Part is an arbitrary portion of a Gradable Event that can be assigned to a TA");
            contentPanel.add(partsLabel);
            
            _addPartButton = new JButton("Add Part");
            _addPartButton.setAlignmentX(LEFT_ALIGNMENT);
            _addPartButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    //Determine the order and the default name for the new part
                    int maxOrder = -1;
                    HashSet<String> partNames = new HashSet<String>();

                    for(DbPart part : _gradableEvent.getParts())
                    {
                        maxOrder = Math.max(maxOrder, part.getOrder());
                        partNames.add(part.getName());
                    }

                    int partOrder = maxOrder + 1;
                    String defaultName = "Part " + partOrder;
                    if(partNames.contains(defaultName))
                    {   
                        //Attempt to generate a unique name - but don't loop forever; Non-unique names are allowed
                        for(int defaultNameEnding = partOrder + 1; defaultNameEnding < 1000; defaultNameEnding++)
                        {
                            defaultName = "Part " + defaultNameEnding;
                            if(!partNames.contains(defaultName))
                            {
                                break;
                            }
                        }
                    }
                    
                    final DbPart part = DbPart.build(_gradableEvent, defaultName, partOrder);
                    PartPanel panel = new PartPanel(GradableEventPanel.this, part);
                    _partPanels.put(part, panel);
                    updateDisplayedPartPanels();
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putParts(ImmutableSet.of(part));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to update or insert part " + part.getName();
                        }
                    });
                }
            });
            
            _partsPanel = new JPanel();
            _partsPanel.setBackground(this.getBackground());
            _partsPanel.setLayout(new BoxLayout(_partsPanel, BoxLayout.Y_AXIS));
            _partsPanel.setAlignmentX(LEFT_ALIGNMENT);
            contentPanel.add(_partsPanel);
            
            List<DbPart> parts = new ArrayList<DbPart>(_gradableEvent.getParts());
            Collections.sort(parts);
            for(DbPart part : parts)
            {
                PartPanel panel = new PartPanel(this, part);
                _partPanels.put(part, panel);
            }
            this.updateDisplayedPartPanels();
        }
        
        void reorderOccurred()
        {
            _upButton.setEnabled(_gradableEvent.getOrder() != 0);
            
            boolean isLastGradableEvent = true;
            for(DbGradableEvent event : _asgnPanel._asgn.getGradableEvents())
            {
                if(_gradableEvent.getOrder() < event.getOrder())
                {
                    isLastGradableEvent = false;
                    break;
                }
            }
            _downButton.setEnabled(!isLastGradableEvent);
        }
            
        void updateDisplayedPartPanels()
        {
            _partsPanel.removeAll();
            
            List<DbPart> parts = new ArrayList<DbPart>(_gradableEvent.getParts());
            Collections.sort(parts);
            
            for(DbPart part : parts)
            {
                PartPanel panel = _partPanels.get(part);
                _partsPanel.add(panel);
                _partsPanel.add(Box.createVerticalStrut(5));
                panel.reorderOccurred();
            }
            
            JPanel addPanel = new PreferredHeightJPanel(new BorderLayout(0, 0));
            addPanel.add(_addPartButton, BorderLayout.CENTER);
            _partsPanel.add(addPanel);
            
            //Force visual update to reflect these changes
            _partsPanel.repaint();
            _partsPanel.revalidate();
        }
        
        private class DeadlinePanel extends PreferredHeightJPanel
        {
            private final DateTimeControl _earlyDateControl, _onTimeDateControl, _lateDateControl;
            private final ValidatingTextField _earlyPointsField, _latePointsField;
            private final PeriodControl _latePeriodControl;
            
            private DeadlinePanel(Color backgroundColor)
            {
                this.setBackground(backgroundColor);
                
                //Dates
                _earlyDateControl = new DateTimeControl(_gradableEvent.getEarlyDate());
                _earlyDateControl.setBackground(backgroundColor);
                _earlyDateControl.addDateTimeChangeListener(new DateTimeControl.DateTimeChangeListener()
                {
                    @Override
                    public void dateTimeChanged(DateTime prevDateTime, DateTime newDateTime)
                    {
                        _gradableEvent.setEarlyDate(newDateTime);
                        _worker.submit(WORKER_TAG, new DeadlineRunnable());
                    }
                });
                
                _onTimeDateControl = new DateTimeControl(_gradableEvent.getOnTimeDate());
                _onTimeDateControl.setBackground(backgroundColor);
                _onTimeDateControl.addDateTimeChangeListener(new DateTimeControl.DateTimeChangeListener()
                {
                    @Override
                    public void dateTimeChanged(DateTime prevDateTime, DateTime newDateTime)
                    {
                        _gradableEvent.setOnTimeDate(newDateTime);
                        _worker.submit(WORKER_TAG, new DeadlineRunnable());
                    }
                });
                
                _lateDateControl = new DateTimeControl(_gradableEvent.getLateDate());
                _lateDateControl.setBackground(backgroundColor);
                _lateDateControl.addDateTimeChangeListener(new DateTimeControl.DateTimeChangeListener()
                {
                    @Override
                    public void dateTimeChanged(DateTime prevDateTime, DateTime newDateTime)
                    {
                        _gradableEvent.setLateDate(newDateTime);
                        _worker.submit(WORKER_TAG, new DeadlineRunnable());
                    }
                });
                
                //Points
                _earlyPointsField = new ValidatingTextField()
                {
                    @Override
                    protected String getDbValue()
                    {
                        return _gradableEvent.getEarlyPoints() == null ? "" : (_gradableEvent.getEarlyPoints() + "");
                    }

                    @Override
                    protected void applyChange(String newValue)
                    {
                        _gradableEvent.setEarlyPoints(newValue.isEmpty() ? null : Double.parseDouble(newValue));
                        _worker.submit(WORKER_TAG, new DeadlineRunnable());
                    }

                    @Override
                    protected ValidationResult validate(String value)
                    {
                        ValidationResult result;
                        if(this.isEnabled())
                        {
                            try
                            {
                                double numericValue = Double.parseDouble(value);
                                if(numericValue == 0.0D)
                                {
                                    result = new ValidationResult(ValidationState.WARNING, "No bonus given");
                                }
                                else if(numericValue < 0D)
                                {
                                    result = new ValidationResult(ValidationState.WARNING, "Bonus is negative. " +
                                            "Student work will lose points for being early.");
                                }
                                else
                                {
                                    result = ValidationResult.NO_ISSUE;
                                }
                            }
                            catch(NumberFormatException e)
                            {
                                result = new ValidationResult(ValidationState.ERROR, "Numerical value not provided");
                            }
                        }
                        else
                        {
                            result = ValidationResult.NO_VALIDATION;
                        }
                        
                        return result;
                    }
                };
                _earlyPointsField.setColumns(5);
                
                _latePointsField = new ValidatingTextField()
                {
                    @Override
                    protected String getDbValue()
                    {
                        return _gradableEvent.getLatePoints() == null ? "" : (_gradableEvent.getLatePoints() + "");
                    }

                    @Override
                    protected void applyChange(String newValue)
                    {
                        _gradableEvent.setLatePoints(newValue.isEmpty() ? null : Double.parseDouble(newValue));
                        _worker.submit(WORKER_TAG, new DeadlineRunnable());
                    }

                    @Override
                    protected ValidationResult validate(String value)
                    {
                        ValidationResult result;
                        if(this.isEnabled())
                        {
                            try
                            {
                                double numericValue = Double.parseDouble(value);
                                if(numericValue == 0.0D)
                                {
                                    result = new ValidationResult(ValidationState.WARNING, "No penalty given");
                                }
                                else if(numericValue > 0D)
                                {
                                    result = new ValidationResult(ValidationState.WARNING, "Penalty is positive. " +
                                            "Student work will gain points for being late.");
                                }
                                else
                                {
                                    result = ValidationResult.NO_ISSUE;
                                }
                            }
                            catch(NumberFormatException e)
                            {
                                result = new ValidationResult(ValidationState.ERROR, "Numerical value not provided");
                            }
                        }
                        else
                        {
                            result = ValidationResult.NO_VALIDATION;
                        }
                        
                        return result;
                    }
                };
                _latePointsField.setColumns(5);
                
                //Period
                _latePeriodControl = new PeriodControl(_gradableEvent.getLatePeriod());
                _latePeriodControl.setBackground(backgroundColor);
                _latePeriodControl.addPeriodChangeListener(new PeriodControl.PeriodChangeListener()
                {
                    @Override
                    public void periodChanged(Period prevPeriod, Period newPeriod)
                    {
                        _gradableEvent.setLatePeriod(newPeriod);
                        _worker.submit(WORKER_TAG, new DeadlineRunnable());
                    }
                });
                
                this.displayDeadlineInfo();
            }
            
            private class RemoveDeadlinesActionListener implements ActionListener
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _gradableEvent.setDeadlineType(DeadlineInfo.Type.NONE);
                    _gradableEvent.setEarlyDate(null);
                    _gradableEvent.setOnTimeDate(null);
                    _gradableEvent.setLateDate(null);
                    _gradableEvent.setEarlyPoints(null);
                    _gradableEvent.setLatePoints(null);
                    _gradableEvent.setLatePeriod(null);
                    
                    _worker.submit(WORKER_TAG, new DeadlineRunnable());

                    displayDeadlineInfo();
                }
            }
            
            private void displayDeadlineInfo()
            {
                this.removeAll();
                
                _earlyDateControl.setDateTime(_gradableEvent.getEarlyDate(), true);
                _onTimeDateControl.setDateTime(_gradableEvent.getOnTimeDate(), true);
                _lateDateControl.setDateTime(_gradableEvent.getLateDate(), true);
                _latePeriodControl.setPeriod(_gradableEvent.getLatePeriod(), true);
                
                DeadlineInfo.Type deadlineType = _gradableEvent.getDeadlineType();
                if(deadlineType == DeadlineInfo.Type.FIXED)
                {
                    this.setLayout(new BorderLayout(0, 0));
                    
                    //Controls for the fixed deadlines
                    JPanel controlsPanel = new JPanel();
                    controlsPanel.setBackground(this.getBackground());
                    this.add(controlsPanel, BorderLayout.CENTER);
                    controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
                    
                    //Early
                    boolean hasEarly = _earlyDateControl.getDateTime() != null;
                    final JCheckBox earlyCheckBox = new JCheckBox("Early", hasEarly);
                    earlyCheckBox.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {
                            if(earlyCheckBox.isSelected())
                            {
                                DateTime earlyDate = _onTimeDateControl.getDateTime().minusDays(2);
                                
                                _gradableEvent.setEarlyDate(earlyDate);
                                _gradableEvent.setEarlyPoints(0D);
                                _worker.submit(WORKER_TAG, new DeadlineRunnable());
                                
                                _earlyDateControl.setDateTime(earlyDate, true);
                                _earlyPointsField.setEnabled(true);
                                _earlyPointsField.setText("0");
                            }
                            else
                            {
                                _gradableEvent.setEarlyDate(null);
                                _gradableEvent.setEarlyPoints(null);
                                _worker.submit(WORKER_TAG, new DeadlineRunnable());
                                
                                _earlyDateControl.setDateTime(null, true);
                                _earlyPointsField.setEnabled(false);
                                _earlyPointsField.setText("");
                            }
                        }
                    });
                    earlyCheckBox.setBackground(this.getBackground());
                    earlyCheckBox.setAlignmentX(LEFT_ALIGNMENT);
                    controlsPanel.add(earlyCheckBox);
                    controlsPanel.add(createDeadlineComponentPanel("Deadline: ", _earlyDateControl));
                    controlsPanel.add(Box.createVerticalStrut(3));
                    _earlyPointsField.setEnabled(hasEarly);
                    _earlyPointsField.setTextToDbValue();
                    controlsPanel.add(createDeadlineComponentPanel("Bonus Points: ", _earlyPointsField));
                    
                    controlsPanel.add(Box.createVerticalStrut(5));
                    
                    //On Time
                    controlsPanel.add(FormattedLabel.asContent("On Time"));
                    _onTimeDateControl.setAlignmentX(LEFT_ALIGNMENT);
                    controlsPanel.add(createDeadlineComponentPanel("Deadline: ", _onTimeDateControl));
                    
                    controlsPanel.add(Box.createVerticalStrut(5));
                    
                    //Late
                    boolean hasLate = _lateDateControl.getDateTime() != null;
                    final JCheckBox lateCheckBox = new JCheckBox("Late", hasLate);
                    lateCheckBox.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {
                            if(lateCheckBox.isSelected())
                            {
                                DateTime lateDate = _onTimeDateControl.getDateTime().plusDays(2);
                                
                                _gradableEvent.setLateDate(lateDate);
                                _gradableEvent.setLatePoints(0D);
                                _worker.submit(WORKER_TAG, new DeadlineRunnable());
                                
                                _lateDateControl.setDateTime(lateDate, true);
                                _latePointsField.setEnabled(true);
                                _latePointsField.setText("0");
                            }
                            else
                            {
                                _gradableEvent.setLateDate(null);
                                _gradableEvent.setLatePoints(null);
                                _worker.submit(WORKER_TAG, new DeadlineRunnable());
                                
                                _lateDateControl.setDateTime(null, true);
                                _latePointsField.setEnabled(false);
                                _latePointsField.setText("");
                            }
                        }
                    });
                    lateCheckBox.setBackground(this.getBackground());
                    lateCheckBox.setAlignmentX(LEFT_ALIGNMENT);
                    controlsPanel.add(lateCheckBox);
                    controlsPanel.add(createDeadlineComponentPanel("Deadline: ", _lateDateControl));
                    controlsPanel.add(Box.createVerticalStrut(3));
                    _latePointsField.setEnabled(hasLate);
                    _latePointsField.setTextToDbValue();
                    controlsPanel.add(createDeadlineComponentPanel("Penalty Points: ", _latePointsField));
                    
                    //Remove deadlines
                    JPanel removePanel = new JPanel();
                    removePanel.setBackground(this.getBackground());
                    this.add(removePanel, BorderLayout.SOUTH);
                    JButton removeButton = new JButton("Remove Deadlines");
                    removeButton.setAlignmentX(CENTER_ALIGNMENT);
                    removeButton.addActionListener(new RemoveDeadlinesActionListener());
                    removePanel.add(removeButton);
                }
                else if(deadlineType == DeadlineInfo.Type.VARIABLE)
                {
                    this.setLayout(new BorderLayout(0, 0));
                    
                    //Controls for the variable deadlines
                    JPanel controlsPanel = new JPanel();
                    controlsPanel.setBackground(this.getBackground());
                    this.add(controlsPanel, BorderLayout.CENTER);
                    controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
                    
                    //On Time
                    controlsPanel.add(FormattedLabel.asContent("On Time"));
                    _onTimeDateControl.setAlignmentX(LEFT_ALIGNMENT);
                    controlsPanel.add(createDeadlineComponentPanel("Deadline: ", _onTimeDateControl));
                    
                    controlsPanel.add(Box.createVerticalStrut(5));
                    
                    //Deduction
                    controlsPanel.add(FormattedLabel.asContent("Deduction"));
                    _latePeriodControl.setAlignmentX(LEFT_ALIGNMENT);
                    controlsPanel.add(createDeadlineComponentPanel("Period: ", _latePeriodControl));
                    controlsPanel.add(Box.createVerticalStrut(3));
                    _latePointsField.setAlignmentX(LEFT_ALIGNMENT);
                    _latePointsField.setEnabled(true);
                    _latePointsField.setTextToDbValue();
                    controlsPanel.add(createDeadlineComponentPanel("Penalty Points per Period: ", _latePointsField));
                    
                    //Late
                    boolean hasLate = _lateDateControl.getDateTime() != null;
                    final JCheckBox lateCheckBox = new JCheckBox("Late", hasLate);
                    lateCheckBox.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {
                            if(lateCheckBox.isSelected())
                            {
                                Period period = _latePeriodControl.getPeriod();
                                DateTime lateDate = _onTimeDateControl.getDateTime().plus(period).plus(period);
                                
                                _gradableEvent.setLateDate(lateDate);
                                _worker.submit(WORKER_TAG, new DeadlineRunnable());
                                
                                _lateDateControl.setDateTime(lateDate, true);
                            }
                            else
                            {
                                _gradableEvent.setLateDate(null);
                                _worker.submit(WORKER_TAG, new DeadlineRunnable());
                                
                                _lateDateControl.setDateTime(null, true);
                            }
                        }
                    });
                    lateCheckBox.setBackground(this.getBackground());
                    lateCheckBox.setAlignmentX(LEFT_ALIGNMENT);
                    controlsPanel.add(lateCheckBox);
                    controlsPanel.add(createDeadlineComponentPanel("Deadline: ", _lateDateControl));
                    controlsPanel.add(Box.createVerticalStrut(3));
                    
                    //Remove deadlines
                    JPanel removePanel = new JPanel();
                    removePanel.setBackground(this.getBackground());
                    this.add(removePanel, BorderLayout.SOUTH);
                    JButton removeButton = new JButton("Remove Deadlines");
                    removeButton.setAlignmentX(CENTER_ALIGNMENT);
                    removeButton.addActionListener(new RemoveDeadlinesActionListener());
                    removePanel.add(removeButton);
                }
                else
                {
                    this.setLayout(new GridLayout(1, 2, 3, 0));

                    JButton fixedButton = new JButton("Add Fixed Deadlines");
                    fixedButton.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {   
                            _gradableEvent.setDeadlineType(DeadlineInfo.Type.FIXED);
                            _gradableEvent.setOnTimeDate(new DateTime());

                            _worker.submit(WORKER_TAG, new DeadlineRunnable());
                            
                            displayDeadlineInfo();
                        }
                    });
                    this.add(fixedButton);

                    JButton variableButton = new JButton("Add Variable Deadlines");
                    variableButton.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {   
                            _gradableEvent.setDeadlineType(DeadlineInfo.Type.VARIABLE);
                            _gradableEvent.setOnTimeDate(new DateTime());
                            _gradableEvent.setLatePeriod(new Period(0, 0, 0, 1, 0, 0, 0, 0));
                            _gradableEvent.setLatePoints(0D);

                            _worker.submit(WORKER_TAG, new DeadlineRunnable());
                            
                            displayDeadlineInfo();
                        }
                    });
                    this.add(variableButton);
                }
                
                //Force visual update to reflect these changes
                this.repaint();
                this.revalidate();
            }
            
            private JPanel createDeadlineComponentPanel(String labelText, Component component)
            {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                panel.setBackground(this.getBackground());
                panel.setAlignmentX(LEFT_ALIGNMENT);
                panel.add(Box.createHorizontalStrut(21));
                panel.add(FormattedLabel.asContent(labelText));
                panel.add(component);

                return panel;
            }
            
            private class DeadlineRunnable extends ReinitializeRunnable
            {
                @Override
                public void dbCall() throws SQLException
                {
                    Allocator.getDatabase().putGradableEvents(ImmutableSet.of(_gradableEvent));
                }

                @Override
                public String dbFailureMessage()
                {
                    return "Unable to save deadline information for gradable event " + _gradableEvent.getName();
                }
            }
        }
    }
    
    private class PartPanel extends PreferredHeightJPanel
    {
        private final GradableEventPanel _gePanel;
        private final DbPart _part;
        
        private final JButton _upButton, _downButton;
        private final ValidatingTextField _nameField, _quickNameField;
        private final JButton _gradingSheetButton;
        
        private final Map<DbAction, ActionPanel> _actionPanels = new HashMap<DbAction, ActionPanel>();
        private final JPanel _actionsPanel;
        private final JButton _addActionButton;
        
        private PartPanel(GradableEventPanel gePanel, DbPart part)
        {
            _gePanel = gePanel;
            _part = part;
            
            this.setBackground(new Color(212, 212, 212));
            this.setBorder(BorderFactory.createEtchedBorder());
            
            this.setLayout(new BorderLayout(0, 0));
            
            JPanel contentPanel = new JPanel();
            contentPanel.setBackground(this.getBackground());
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            this.add(new PaddingPanel(contentPanel, 5, this.getBackground()), BorderLayout.CENTER);
            
            
            JPanel headlinePanel = new PreferredHeightJPanel(new BorderLayout(0, 0));
            headlinePanel.setAlignmentX(LEFT_ALIGNMENT);
            headlinePanel.setBackground(this.getBackground());
            contentPanel.add(headlinePanel);
            
            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(this.getBackground());
            headlinePanel.add(controlPanel, BorderLayout.EAST);
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _upButton = new JButton("↑");
            _upButton.setToolTipText("Move up");
            _upButton.setMargin(new Insets(1, 1, 1, 1));
            _upButton.setEnabled(false);
            _upButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _part.setOrder(_part.getOrder() - 1);
                    
                    ImmutableSet.Builder<DbPart> reorderedBuilder = ImmutableSet.builder();
                    reorderedBuilder.add(_part);
                    for(DbPart part : _gePanel._gradableEvent.getParts())
                    {
                        if(part != _part && part.getOrder() == _part.getOrder())
                        {
                            part.setOrder(part.getOrder() + 1);
                            reorderedBuilder.add(part);
                        }
                    }
                    final ImmutableSet<DbPart> reorderedParts = reorderedBuilder.build();
                    
                    _gePanel.updateDisplayedPartPanels();
                    
                    //Reorder gradable events on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putParts(reorderedParts);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder part: " + _part.getName();
                        }
                    });
                }
            });
            controlPanel.add(_upButton);
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _downButton = new JButton("↓");
            _downButton.setToolTipText("Move down");
            _downButton.setMargin(new Insets(1, 1, 1, 1));
            _downButton.setEnabled(false);
            _downButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _part.setOrder(_part.getOrder() + 1);
                    
                    ImmutableSet.Builder<DbPart> reorderedBuilder = ImmutableSet.builder();
                    reorderedBuilder.add(_part);
                    for(DbPart part : _gePanel._gradableEvent.getParts())
                    {
                        if(_part != part && part.getOrder() == _part.getOrder())
                        {
                            part.setOrder(part.getOrder() - 1);
                            reorderedBuilder.add(part);
                        }
                    }
                    final ImmutableSet<DbPart> reorderedParts = reorderedBuilder.build();
                    
                    _gePanel.updateDisplayedPartPanels();
                    
                    //Reorder gradable events on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putParts(reorderedParts);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder part: " + _part.getName();
                        }
                    });
                }
            });
            controlPanel.add(_downButton);
            
            this.reorderOccurred();
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            JButton deleteButton = new JButton("✗");
            deleteButton.setToolTipText("Delete");
            deleteButton.setMargin(new Insets(1, 1, 1, 1));
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean proceed = ModalDialog.showConfirmation(_configManager, "Delete Part",
                            "Deleting this Part will delete all grades and distributions for this Part",
                            "Delete", "Cancel");
                    
                    if(proceed)
                    {
                        _gePanel._gradableEvent.removePart(_part);
                        ImmutableSet.Builder<DbPart> reorderedBuilder = ImmutableSet.builder();
                        for(DbPart part : _gePanel._gradableEvent.getParts())
                        {
                            if(part.getOrder() > _part.getOrder())
                            {
                                part.setOrder(part.getOrder() - 1);
                                reorderedBuilder.add(part);
                            }
                        }
                        final ImmutableSet<DbPart> reorderedParts = reorderedBuilder.build();
                        
                        _gePanel.updateDisplayedPartPanels();
                        
                        _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                        {
                            @Override
                            public void dbCall() throws SQLException
                            {
                                Allocator.getDatabase().removeParts(ImmutableSet.of(_part));

                                //This absolutely needs to occur after the delete call so in case that fails this
                                //code will not execute
                                Allocator.getDatabase().putParts(reorderedParts);
                            }

                            @Override
                            public String dbFailureMessage()
                            {
                                return "Unable to delete part: " + _part.getName();
                            }
                        });
                    }
                }
            });
            controlPanel.add(deleteButton);
            
            _nameField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _part.getName();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    return ValidationResult.validateNotEmpty(value);
                }

                @Override
                protected void applyChange(String newValue)
                {
                    _part.setName(newValue);
                    _worker.submit(WORKER_TAG, new PartRunnable());
                }
            };            
            headlinePanel.add(_nameField, BorderLayout.CENTER);
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            JPanel combinedPanel = new JPanel(new GridLayout(1, 2, 3, 0));
            combinedPanel.setAlignmentX(LEFT_ALIGNMENT);
            combinedPanel.setBackground(contentPanel.getBackground());
            contentPanel.add(combinedPanel);
            
            JPanel gradingSheetPanel = new JPanel(new BorderLayout(0, 0));
            gradingSheetPanel.setBackground(combinedPanel.getBackground());
            combinedPanel.add(gradingSheetPanel);
            
            JLabel gradingSheetLabel = FormattedLabel.asSubheader("Grading Sheet");
            gradingSheetLabel.setToolTipText("A grading sheet is filled out by TAs while grading a student");
            gradingSheetPanel.add(gradingSheetLabel, BorderLayout.NORTH);
            
            _gradingSheetButton = new JButton("Edit Grading Sheet");
            _gradingSheetButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    try
                    {
                        _worker.blockOnQueuedTasks();
                        GradingSheetCreatorPanel gradingSheetCreator = 
                                new GradingSheetCreatorPanel(AssignmentPanel.this, _worker, _part);
                        CloseAction closeAction = _configManager.hostModal(gradingSheetCreator);
                        gradingSheetCreator.setCloseAction(closeAction);
                    }
                    catch(InterruptedException e)
                    {
                        ErrorReporter.report("Unable to show grading sheet creator due to failure to block on " +
                                "queued tasks", e);
                    }
                }
            });
            gradingSheetPanel.add(_gradingSheetButton);
            
            JPanel quickNamePanel = new JPanel(new BorderLayout(0, 0));
            quickNamePanel.setBackground(combinedPanel.getBackground());
            combinedPanel.add(quickNamePanel);
            
            JLabel quickNameLabel = FormattedLabel.asSubheader("Quick Name");
            quickNameLabel.setToolTipText("A unique name for the part that can be referenced from a shell script");
            quickNamePanel.add(quickNameLabel, BorderLayout.NORTH);
            
            _quickNameField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _part.getQuickName() == null ? "" : _part.getQuickName();
                }
                
                @Override
                protected ValidationResult validate(String value)
                {
                    ValidationResult result;
                    
                    //Allows a through z (upper and lower case), 0 through 9, and underscore
                    Matcher matcher = Pattern.compile("[0-9a-zA-Z_]*").matcher(value);
                    if(matcher.find() && (matcher.end() - matcher.start() == value.length()))
                    {   
                        //TODO: Improve efficiency
                        HashSet<String> quickNames = new HashSet<String>();
                        List<DbAssignment> asgns = _assignmentList.getListData();
                        for(DbAssignment asgn : asgns)
                        {
                            for(DbGradableEvent ge : asgn.getGradableEvents())
                            {
                                for(DbPart part : ge.getParts())
                                {
                                    if(part != _part && part.getQuickName() != null)
                                    {
                                        quickNames.add(part.getQuickName());
                                    }
                                }
                            }
                        }
                        
                        if(quickNames.contains(value))
                        {
                            result = new ValidationResult(ValidationState.ERROR, "This quick name is not unique");
                        }
                        else
                        {
                            result = ValidationResult.NO_ISSUE;
                        }
                    }
                    else
                    {
                        result = new ValidationResult(ValidationState.ERROR, "Only letters, numbers, and underscores " +
                                "are allowed");
                    }
                    
                    return result;
                }

                @Override
                protected void applyChange(String newValue)
                {
                    _part.setQuickName(newValue.isEmpty() ? null : newValue);
                    _worker.submit(WORKER_TAG, new PartRunnable());
                }
            };
            quickNamePanel.add(_quickNameField, BorderLayout.CENTER);
            
            contentPanel.add(Box.createVerticalStrut(5));
            
            JLabel actionsLabel = FormattedLabel.asSubheader("Actions");
            actionsLabel.setToolTipText("An Action is a task performed for a part, which can include acting on a " +
                    "digital handin");
            contentPanel.add(actionsLabel);
            
            _addActionButton = new JButton("Add Action");
            _addActionButton.setAlignmentX(LEFT_ALIGNMENT);
            _addActionButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    //Determine the order and the default name for the new action
                    int maxOrder = -1;
                    HashSet<String> actionNames = new HashSet<String>();

                    for(DbAction action : _part.getActions())
                    {
                        maxOrder = Math.max(maxOrder, action.getOrder());
                        actionNames.add(action.getName());
                    }

                    int actionOrder = maxOrder + 1;
                    
                    String actionName;
                    IconImage actionIcon;
                    List<ActionDescription> availableSuggestedDescriptions = new ArrayList<ActionDescription>();
                    for(ActionDescription description : ActionDescription.getDefaultDescriptions())
                    {
                        if(!actionNames.contains(description.getName()))
                        {
                            availableSuggestedDescriptions.add(description);
                        }
                    }
                    if(availableSuggestedDescriptions.isEmpty())
                    {
                        actionName = ActionDescription.CUSTOM.getName();
                        actionIcon = ActionDescription.CUSTOM.getIcon();
                    }
                    else
                    {
                        ActionDescription description = availableSuggestedDescriptions.get(0);
                        actionName = description.getName();
                        actionIcon = description.getIcon();
                    }
                    
                    final DbAction action = DbAction.build(_part, actionName, actionIcon, actionOrder);
                    
                    ActionPanel panel = new ActionPanel(PartPanel.this, action);
                    _actionPanels.put(action, panel);
                    updateDisplayedActionPanels();
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putActions(ImmutableSet.of(action));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to update or insert action " + action.getName();
                        }
                    });
                }
            });
            
            _actionsPanel = new JPanel();
            _actionsPanel.setBackground(this.getBackground());
            _actionsPanel.setLayout(new BoxLayout(_actionsPanel, BoxLayout.Y_AXIS));
            _actionsPanel.setAlignmentX(LEFT_ALIGNMENT);
            contentPanel.add(_actionsPanel);
            
            List<DbAction> actions = new ArrayList<DbAction>(_part.getActions());
            Collections.sort(actions);
            for(DbAction action : actions)
            {
                ActionPanel panel = new ActionPanel(this, action);
                _actionPanels.put(action, panel);
            }
            this.updateDisplayedActionPanels();
        }
        
        void updateDisplayedActionPanels()
        {
            _actionsPanel.removeAll();
            
            List<DbAction> actions = new ArrayList<DbAction>(_part.getActions());
            Collections.sort(actions);
            
            for(DbAction action : actions)
            {
                ActionPanel panel = _actionPanels.get(action);
                _actionsPanel.add(panel);
                _actionsPanel.add(Box.createVerticalStrut(5));
                panel.reorderOccurred();
            }
            
            JPanel addPanel = new PreferredHeightJPanel(new BorderLayout(0, 0));
            addPanel.add(_addActionButton, BorderLayout.CENTER);
            _actionsPanel.add(addPanel);
            
            //Force visual update to reflect these changes
            _actionsPanel.repaint();
            _actionsPanel.revalidate();
        }
        
        void reorderOccurred()
        {
            _upButton.setEnabled(_part.getOrder() != 0);
            
            boolean isLastPart = true;
            for(DbPart part : _gePanel._gradableEvent.getParts())
            {
                if(_part.getOrder() < part.getOrder())
                {
                    isLastPart = false;
                    break;
                }
            }
            _downButton.setEnabled(!isLastPart);
        }
        
        private class PartRunnable extends ReinitializeRunnable
        {
            @Override
            public void dbCall() throws SQLException
            {
                Allocator.getDatabase().putParts(ImmutableSet.of(_part));
            }

            @Override
            public String dbFailureMessage()
            {
                return "Unable to insert or update part " + _part.getName();
            }
        }
    }
    
    private class ActionPanel extends JPanel
    {
        private final PartPanel _partPanel;
        private final DbAction _action;
        
        private final JButton _upButton, _downButton;
        private final JComboBox _iconComboBox;
        private final JComboBox _nameComboBox;
        private final GenericJComboBox<Task> _taskComboBox;
        private final JPanel _propertiesPanel;
        
        ActionPanel(PartPanel partPanel, DbAction action)
        {
            _partPanel = partPanel;
            _action = action;
            
            this.setBackground(new Color(200, 200, 200));
            this.setBorder(BorderFactory.createEtchedBorder());
            
            this.setLayout(new BorderLayout(0, 0));
            
            JPanel contentPanel = new JPanel();
            contentPanel.setBackground(this.getBackground());
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            this.add(new PaddingPanel(contentPanel, 5, this.getBackground()), BorderLayout.CENTER);
            
            
            JPanel headlinePanel = new PreferredHeightJPanel(new BorderLayout(0, 0));
            headlinePanel.setAlignmentX(LEFT_ALIGNMENT);
            headlinePanel.setBackground(this.getBackground());
            contentPanel.add(headlinePanel);
            
            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(this.getBackground());
            headlinePanel.add(controlPanel, BorderLayout.EAST);
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _upButton = new JButton("↑");
            _upButton.setToolTipText("Move up");
            _upButton.setMargin(new Insets(1, 1, 1, 1));
            _upButton.setEnabled(false);
            _upButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _action.setOrder(_action.getOrder() - 1);
                    
                    ImmutableSet.Builder<DbAction> reorderedBuilder = ImmutableSet.builder();
                    reorderedBuilder.add(_action);
                    for(DbAction action : _partPanel._part.getActions())
                    {
                        if(action != _action && action.getOrder() == _action.getOrder())
                        {
                            action.setOrder(action.getOrder() + 1);
                            reorderedBuilder.add(action);
                        }
                    }
                    final ImmutableSet<DbAction> reorderedActions = reorderedBuilder.build();
                    
                    _partPanel.updateDisplayedActionPanels();
                    
                    //Reorder actions on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putActions(reorderedActions);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder action: " + _action.getName();
                        }
                    });
                }
            });
            controlPanel.add(_upButton);
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _downButton = new JButton("↓");
            _downButton.setToolTipText("Move down");
            _downButton.setMargin(new Insets(1, 1, 1, 1));
            _downButton.setEnabled(false);
            _downButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _action.setOrder(_action.getOrder() + 1);
                    
                    ImmutableSet.Builder<DbAction> reorderedBuilder = ImmutableSet.builder();
                    reorderedBuilder.add(_action);
                    for(DbAction action : _partPanel._part.getActions())
                    {
                        if(_action != action && action.getOrder() == _action.getOrder())
                        {
                            action.setOrder(action.getOrder() - 1);
                            reorderedBuilder.add(action);
                        }
                    }
                    final ImmutableSet<DbAction> reorderedActions = reorderedBuilder.build();
                    
                    _partPanel.updateDisplayedActionPanels();
                    
                    //Reorder actions on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putActions(reorderedActions);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder action: " + _action.getName();
                        }
                    });
                }
            });
            controlPanel.add(_downButton);
            
            this.reorderOccurred();
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            JButton deleteButton = new JButton("✗");
            deleteButton.setToolTipText("Delete");
            deleteButton.setMargin(new Insets(1, 1, 1, 1));
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _partPanel._part.removeAction(_action);
                    ImmutableSet.Builder<DbAction> reorderedBuilder = ImmutableSet.builder();
                    for(DbAction action : _partPanel._part.getActions())
                    {
                        if(action.getOrder() > _action.getOrder())
                        {
                            action.setOrder(action.getOrder() - 1);
                            reorderedBuilder.add(action);
                        }
                    }
                    final ImmutableSet<DbAction> reorderedActions = reorderedBuilder.build();

                    _partPanel.updateDisplayedActionPanels();

                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().removeActions(ImmutableSet.of(_action));

                            //This absolutely needs to occur after the delete call so in case that fails this
                            //code will not execute
                            Allocator.getDatabase().putActions(reorderedActions);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to delete action: " + _action.getName();
                        }
                    });
                }
            });
            controlPanel.add(deleteButton);
            
            JPanel nameAndIconPanel = new JPanel(new BorderLayout(0, 0));
            nameAndIconPanel.setBackground(headlinePanel.getBackground());
            headlinePanel.add(nameAndIconPanel, BorderLayout.CENTER);
            
            _iconComboBox = new JComboBox(IconImage.values());
            _iconComboBox.setSelectedItem(_action.getIcon());
            _iconComboBox.setPreferredSize(new Dimension(40, 25));
            _iconComboBox.setRenderer(new ListCellRenderer()
            {
                @Override
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus)
                {
                    Icon icon = null;
                    if(value instanceof IconImage)
                    {
                        icon = IconLoader.loadIcon(IconSize.s16x16, (IconImage) value);
                    }
                    
                    JLabel label = new JLabel("", icon, SwingConstants.LEFT);
                    
                    if(isSelected)
                    {
                        label.setBackground(list.getSelectionBackground());
                        label.setForeground(list.getSelectionForeground());
                    }
                    else
                    {
                        label.setBackground(list.getBackground());
                        label.setForeground(list.getForeground());
                    }
                    
                    return label;
                }
            });
            _iconComboBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    final IconImage icon;
                    //This should always be true, but check just in case
                    if(_iconComboBox.getSelectedItem() instanceof IconImage)
                    {
                        icon = (IconImage) _iconComboBox.getSelectedItem();
                    }
                    else
                    {
                        icon = null;
                    }
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            _action.setIcon(icon);
                            Allocator.getDatabase().putActions(ImmutableSet.of(_action));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to modify action name: " + _action.getName();
                        }
                    });
                }
            });
            nameAndIconPanel.add(_iconComboBox, BorderLayout.WEST);
            
            _nameComboBox = new JComboBox(ActionDescription.getDefaultDescriptions().toArray());
            _nameComboBox.setEditable(true);
            _nameComboBox.setSelectedItem(_action.getName());
            _nameComboBox.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    final String name = _nameComboBox.getSelectedItem().toString();
                    
                    if(_nameComboBox.getSelectedItem() instanceof ActionDescription)
                    {
                        ActionDescription description = (ActionDescription) _nameComboBox.getSelectedItem();
                        _iconComboBox.setSelectedItem(description.getIcon());
                    }
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            _action.setName(name);
                            Allocator.getDatabase().putActions(ImmutableSet.of(_action));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to modify action name from " + _action.getName() + " to " + name;
                        }
                    });
                }
            });
            
            nameAndIconPanel.add(_nameComboBox, BorderLayout.CENTER);
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            contentPanel.add(FormattedLabel.asHeader("Task"));
            
            final FormattedLabel digitalHandinRequiredLabel = FormattedLabel.asContent("This task requires a digital " +
                    "handin to operate");
            digitalHandinRequiredLabel.setVisible(false);
            final FormattedLabel taskDescriptionLabel = FormattedLabel.asContent("").usePlainFont();
            JPanel propertyIndentPanel = new JPanel();
            propertyIndentPanel.setBackground(contentPanel.getBackground());
            _propertiesPanel = new JPanel();
            _propertiesPanel.setBackground(propertyIndentPanel.getBackground());
            _propertiesPanel.setLayout(new BoxLayout(_propertiesPanel, BoxLayout.Y_AXIS));
            
            _taskComboBox = new GenericJComboBox<Task>(TaskRepository.getTasks().values(),
            new DescriptionProvider<Task>()
            {
                @Override
                public String getDisplayText(Task task)
                {
                    String displayText = "";
                    if(task != null)
                    {
                        displayText = task.getFullName();
                    }
                    
                    return displayText;
                }

                @Override
                public String getToolTipText(Task task)
                {
                    String toolTip = "";
                    if(task != null)
                    {
                        toolTip = task.getDescription();
                    }
                    
                    return toolTip;
                }
            });
            Task task = TaskRepository.getTasks().get(_action.getTask());
            _taskComboBox.setGenericSelectedItem(task);
            //If there is a task stored in the database, load up the properties
            if(task != null)
            {
                digitalHandinRequiredLabel.setVisible(task.requiresDigitalHandin());
                taskDescriptionLabel.setText(task.getDescription());
                
                //Required        
                Set<DbActionProperty> requiredProperties = new HashSet<DbActionProperty>();
                for(TaskProperty property : task.getRequiredProperties())
                {
                    for(DbActionProperty propertyValue : _action.getActionProperties())
                    {
                        if(property.getName().equals(propertyValue.getKey()))
                        {
                            requiredProperties.add(propertyValue);
                        }
                    }
                }
                if(!task.getRequiredProperties().isEmpty())
                {
                    _propertiesPanel.add(Box.createVerticalStrut(5));
                    PropertiesPanel panel = new PropertiesPanel("Required Properties",
                            task.getRequiredProperties(), requiredProperties);
                    panel.setBackground(_propertiesPanel.getBackground());
                    _propertiesPanel.add(panel);
                }

                //Optional
                Set<DbActionProperty> optionalProperties = new HashSet<DbActionProperty>();
                for(TaskProperty property : task.getOptionalProperties())
                {
                    for(DbActionProperty propertyValue : _action.getActionProperties())
                    {
                        if(property.getName().equals(propertyValue.getKey()))
                        {
                            optionalProperties.add(propertyValue);
                        }
                    }
                }
                if(!task.getOptionalProperties().isEmpty())
                {
                    _propertiesPanel.add(Box.createVerticalStrut(5));
                    PropertiesPanel panel = new PropertiesPanel("Optional Properties",
                            task.getOptionalProperties(), optionalProperties);
                    panel.setBackground(_propertiesPanel.getBackground());
                    _propertiesPanel.add(panel);
                }
            }
            
            _taskComboBox.addSelectionListener(new SelectionListener<Task>()
            {
                @Override
                public void selectionPerformed(Task currTask, Task newTask, SelectionAction action)
                {
                    _propertiesPanel.removeAll();
                    
                    final String taskName;
                    if(newTask == null)
                    {
                        taskName = null;
                        digitalHandinRequiredLabel.setVisible(false);
                        taskDescriptionLabel.setText("No task selected");
                    }
                    else
                    {
                        taskName = newTask.getFullName();
                        digitalHandinRequiredLabel.setVisible(newTask.requiresDigitalHandin());
                        taskDescriptionLabel.setText(newTask.getDescription());
                        
                        if(!newTask.getRequiredProperties().isEmpty())
                        {
                            _propertiesPanel.add(Box.createVerticalStrut(5));
                            PropertiesPanel panel = new PropertiesPanel("Required Properties",
                                    newTask.getRequiredProperties(), ImmutableSet.<DbActionProperty>of());
                            panel.setBackground(_propertiesPanel.getBackground());
                            _propertiesPanel.add(panel);
                        }
                        if(!newTask.getOptionalProperties().isEmpty())
                        {
                            _propertiesPanel.add(Box.createVerticalStrut(5));
                            PropertiesPanel panel = new PropertiesPanel("Optional Properties",
                                    newTask.getOptionalProperties(), ImmutableSet.<DbActionProperty>of());
                            panel.setBackground(_propertiesPanel.getBackground());
                            _propertiesPanel.add(panel);
                        }
                    }
                    
                    _propertiesPanel.repaint();
                    _propertiesPanel.revalidate();
                    
                    //Remove the no longer valid properties, as they belong to a different task
                    final Set<DbActionProperty> properties = ImmutableSet.copyOf(_action.getActionProperties());

                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            _action.setTask(taskName);
                            Allocator.getDatabase().putActions(ImmutableSet.of(_action));
                            Allocator.getDatabase().removeActionProperties(properties);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to set action's task to: " + taskName;
                        }
                    });
                }
            });
            _taskComboBox.setAlignmentX(LEFT_ALIGNMENT);
            
            contentPanel.add(_taskComboBox);
            contentPanel.add(digitalHandinRequiredLabel);
            contentPanel.add(taskDescriptionLabel);
            
            propertyIndentPanel.setAlignmentX(LEFT_ALIGNMENT);
            propertyIndentPanel.setLayout(new BoxLayout(propertyIndentPanel, BoxLayout.X_AXIS));
            propertyIndentPanel.add(Box.createHorizontalStrut(10));
            propertyIndentPanel.add(_propertiesPanel);
            contentPanel.add(propertyIndentPanel);
        }
        
        private class PropertiesPanel extends JPanel
        {
            PropertiesPanel(String title, Set<TaskProperty> properties, Set<DbActionProperty> propertiesValues)
            {   
                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                this.add(FormattedLabel.asSubheader(title));
                
                for(final TaskProperty property : properties)
                {
                    this.add(Box.createVerticalStrut(3));
                    this.add(FormattedLabel.asContent(property.getName()));
                    this.add(FormattedLabel.asContent(property.getDescription().replaceAll("\n", "<br/>"))
                            .usePlainFont());
                    
                    //Find the existing value in the database, if it does not exist - make one
                    DbActionProperty propertyValue = null;
                    for(DbActionProperty value : propertiesValues)
                    {
                        if(property.getName().equals(value.getKey()))
                        {
                            propertyValue = value;
                        }
                    }
                    if(propertyValue == null)
                    {
                        propertyValue = DbActionProperty.build(_action, property.getName());
                    }
                    final DbActionProperty dbPropertyValue = propertyValue;
                    
                    ValidatingTextField propertyField = new ValidatingTextField()
                    {
                        @Override
                        protected String getDbValue()
                        {
                            return dbPropertyValue.getValue() == null ? "" : dbPropertyValue.getValue();
                        }

                        @Override
                        protected ValidationResult validate(String value)
                        {
                            ValidationResult result = ValidationResult.NO_VALIDATION;
                            if(property.isRequired() && (value == null || value.isEmpty()))
                            {
                                result = new ValidationResult(ValidationState.ERROR, "This property is required");
                            }
                            
                            return result;
                        }

                        @Override
                        protected void applyChange(final String newValue)
                        {
                            dbPropertyValue.setValue(newValue);
                            _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                            {
                                @Override
                                public void dbCall() throws SQLException
                                {
                                    if(newValue == null || newValue.isEmpty())
                                    {
                                        Allocator.getDatabase().removeActionProperties(
                                                ImmutableSet.of(dbPropertyValue));
                                    }
                                    else
                                    {
                                        Allocator.getDatabase().putActionProperties(ImmutableSet.of(dbPropertyValue));
                                    }
                                }

                                @Override
                                public String dbFailureMessage()
                                {
                                    return "Unable to update property: " + property.getName();
                                }
                            });
                        }
                    };
                    propertyField.setAlignmentX(LEFT_ALIGNMENT);
                    this.add(propertyField);
                }
            }
        }
        
        void reorderOccurred()
        {
            _upButton.setEnabled(_action.getOrder() != 0);
            
            boolean isLastAction = true;
            for(DbAction action : _partPanel._part.getActions())
            {
                if(_action.getOrder() < action.getOrder())
                {
                    isLastAction = false;
                    break;
                }
            }
            _downButton.setEnabled(!isLastAction);
        }
    }
}