package cakehat.views.config;

import support.ui.DocumentAdapter;
import cakehat.Allocator;
import cakehat.database.assignment.ActionRepository;
import cakehat.database.DeadlineInfo;
import cakehat.database.assignment.PartActionDescription;
import cakehat.database.assignment.PartActionDescription.ActionType;
import cakehat.database.assignment.PartActionProperty;
import cakehat.database.DbActionProperty;
import cakehat.database.DbAssignment;
import cakehat.database.DbGradableEvent;
import cakehat.database.DbPart;
import cakehat.database.DbPartAction;
import cakehat.views.config.ValidationResult.ValidationState;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.joda.time.DateTime;
import org.joda.time.Period;
import support.ui.DateTimeControl;
import support.ui.DescriptionProvider;
import support.ui.DnDList;
import support.ui.DnDListener;
import support.ui.GenericJComboBox;
import support.ui.FormattedLabel;
import support.ui.ModalDialog;
import support.ui.PaddingPanel;
import support.ui.PartialDescriptionProvider;
import support.ui.PeriodControl;
import support.ui.SelectionListener;
import support.ui.SelectionListener.SelectionAction;

/**
 *
 * @author jak2
 */
class AssignmentPanel extends JPanel
{
    private static final String WORKER_TAG = "ASSIGNMENT";
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
    
    public AssignmentPanel(UniqueElementSingleThreadWorker worker)
    {
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
                
                boolean proceed = ModalDialog.showConfirmation("Delete Assignment",
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
    
    private void reinitialize(final Throwable t, final String msg)
    {
        //Cancel everything and re-initialize
        _worker.cancel(WORKER_TAG);
        
        initialize();
        
        //TODO: Improve this so no need to individually wrap ErrorView calls
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new ErrorView(t, msg);
            }
        });
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
                            
                            new ErrorView(e);
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
                    
                    boolean proceed = ModalDialog.showConfirmation("Change Groups",
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
                                Allocator.getDatabase().removeGroups(_asgn.getId());
                            }

                            @Override
                            public String dbFailureMessage()
                            {
                                throw new UnsupportedOperationException("Not supported yet.");
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
        
        private class FixedWidthJPanel extends JPanel implements Scrollable
        {
            @Override
            public Dimension getPreferredScrollableViewportSize()
            {
                return this.getPreferredSize();
            }

            @Override
            public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
            {
                return 1;
            }

            @Override
            public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
            {
                return 10;
            }

            @Override
            public boolean getScrollableTracksViewportWidth()
            {
                return true;
            }

            @Override
            public boolean getScrollableTracksViewportHeight()
            {
                return false;
            }
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
            
            JPanel addPanel = new JPanel(new BorderLayout(0, 0))
            {
                @Override
                public Dimension getMaximumSize()
                {
                    Dimension size = getPreferredSize();
                    size.width = Short.MAX_VALUE;

                    return size;
                }
            };
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
    
    private class GradableEventPanel extends JPanel
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
            PaddingPanel paddingPanel = new PaddingPanel(contentPanel, 5);
            paddingPanel.setBackground(this.getBackground());
            this.add(paddingPanel, BorderLayout.CENTER);
            
            JPanel headlinePanel = new JPanel(new BorderLayout(0, 0))
            {
                @Override
                public Dimension getMaximumSize()
                {
                    Dimension size = getPreferredSize();
                    size.width = Short.MAX_VALUE;

                    return size;
                }
            };            
            headlinePanel.setAlignmentX(LEFT_ALIGNMENT);
            headlinePanel.setBackground(this.getBackground());
            contentPanel.add(headlinePanel);
            
            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(this.getBackground());
            headlinePanel.add(controlPanel, BorderLayout.EAST);
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _upButton = new JButton("⇑");
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
            
            _downButton = new JButton("⇓");
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
            
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean proceed = ModalDialog.showConfirmation("Delete Gradable Event",
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
                    
                    if((newValue.isEmpty() && !currValue.isEmpty()) || (!newValue.isEmpty() && currValue.isEmpty()))
                    {
                        for(PartPanel panel : _partPanels.values())
                        {
                            panel.notifyChangeHasDigitalHandinDirectory(!newValue.isEmpty());
                        }
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
                    part.setOutOf(0D);
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
        
        @Override
        public Dimension getMaximumSize()
        {
            Dimension size = getPreferredSize();
            size.width = Short.MAX_VALUE;

            return size;
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
            
            JPanel addPanel = new JPanel(new BorderLayout(0, 0))
            {
                @Override
                public Dimension getMaximumSize()
                {
                    Dimension size = getPreferredSize();
                    size.width = Short.MAX_VALUE;

                    return size;
                }
            };
            addPanel.add(_addPartButton, BorderLayout.CENTER);
            _partsPanel.add(addPanel);
            
            //Force visual update to reflect these changes
            _partsPanel.repaint();
            _partsPanel.revalidate();
        }
        
        private class DeadlinePanel extends JPanel
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
            
            @Override
            public Dimension getMaximumSize()
            {
                Dimension size = getPreferredSize();
                size.width = Short.MAX_VALUE;

                return size;
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
    
    private class PartPanel extends JPanel
    {
        private final GradableEventPanel _gePanel;
        private final DbPart _part;
        
        private final JButton _upButton, _downButton;
        private final ValidatingTextField _nameField, _gradingGuideField, _gmlField, _outOfField, _quickNameField;
        
        private final JPanel _partActionsPanel;
        
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
            PaddingPanel paddingPanel = new PaddingPanel(contentPanel, 5);
            paddingPanel.setBackground(this.getBackground());
            this.add(paddingPanel, BorderLayout.CENTER);
            
            
            JPanel headlinePanel = new JPanel(new BorderLayout(0, 0))
            {
                @Override
                public Dimension getMaximumSize()
                {
                    Dimension size = getPreferredSize();
                    size.width = Short.MAX_VALUE;

                    return size;
                }
            };            
            headlinePanel.setAlignmentX(LEFT_ALIGNMENT);
            headlinePanel.setBackground(this.getBackground());
            contentPanel.add(headlinePanel);
            
            JPanel controlPanel = new JPanel();
            controlPanel.setBackground(this.getBackground());
            headlinePanel.add(controlPanel, BorderLayout.EAST);
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _upButton = new JButton("⇑");
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
            
            _downButton = new JButton("⇓");
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
            
            JButton deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    boolean proceed = ModalDialog.showConfirmation("Delete Part",
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
            
            JLabel gradingGuideLabel = FormattedLabel.asSubheader("Grading Guide");
            gradingGuideLabel.setToolTipText("A plain text file viewable by TAs to assist in grading a Part");
            contentPanel.add(gradingGuideLabel);
            
            _gradingGuideField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _part.getGradingGuide() == null ? "" : _part.getGradingGuide().getAbsolutePath();
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
                            result = new ValidationResult(ValidationState.WARNING, "Specified path does not exist");
                        }
                        else if(!file.isFile())
                        {
                            result = new ValidationResult(ValidationState.WARNING, "Specified path is not a file");
                        }
                        else if(!file.canRead())
                        {
                            result = new ValidationResult(ValidationState.WARNING, "Specified file cannot be read");
                        }
                        else if(!value.endsWith(".txt"))
                        {
                            result = new ValidationResult(ValidationState.WARNING, "Specified file does not end in " +
                                    ".txt, only plain text files are supported");
                        }
                    }
                    
                    return result;
                }

                @Override
                protected void applyChange(String newValue)
                {
                    if(newValue.isEmpty())
                    {
                        _part.setGradingGuide(null);
                    }
                    else
                    {
                        _part.setGradingGuide(new File(newValue));
                    }
                    _worker.submit(WORKER_TAG, new PartRunnable());
                }
            };
            _gradingGuideField.setAlignmentX(LEFT_ALIGNMENT);
            _gradingGuideField.setMaximumSize(new Dimension(Short.MAX_VALUE, 25));
            contentPanel.add(_gradingGuideField);
            
            contentPanel.add(Box.createVerticalStrut(10));
            
            JLabel gradingSheetLabel = FormattedLabel.asSubheader("Grading Sheet");
            gradingSheetLabel.setToolTipText("A grading sheet is filled out by TAs while grading a student");
            contentPanel.add(gradingSheetLabel);
            
            JPanel gmlPanel = new JPanel(new BorderLayout(5, 0));
            gmlPanel.setAlignmentX(LEFT_ALIGNMENT);
            gmlPanel.setBackground(contentPanel.getBackground());
            contentPanel.add(gmlPanel);
            gmlPanel.add(FormattedLabel.asContent("GML Template"), BorderLayout.WEST);
            _gmlField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _part.getGmlTemplate() == null ? "" : _part.getGmlTemplate().getAbsolutePath();
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
                            result = new ValidationResult(ValidationState.WARNING, "Specified file does not exist");
                        }
                        else if(!file.isFile())
                        {
                            result = new ValidationResult(ValidationState.WARNING, "Specified path is not a file");
                        }
                        else if(!file.canRead())
                        {
                            result = new ValidationResult(ValidationState.WARNING, "Cannot read specified file");
                        }
                        else if(!value.endsWith("gml"))
                        {
                            result = new ValidationResult(ValidationState.WARNING, "Specified file does not end with " +
                                    ".gml extension");
                        }
                    }
                    
                    return result;
                }

                @Override
                protected void applyChange(String newValue)
                {
                    if(newValue.isEmpty())
                    {
                        _part.setGmlTemplate(null);
                    }
                    else
                    {
                        _part.setGmlTemplate(new File(newValue));
                    }
                    
                    _part.setOutOf(null);
                    _outOfField.setText("");
                    
                    _part.setQuickName(null);
                    _quickNameField.setText("");
                    
                    _worker.submit(WORKER_TAG, new PartRunnable());
                }
            };
            gmlPanel.add(_gmlField, BorderLayout.CENTER);
            
            JPanel gradingSheetOrPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            gradingSheetOrPanel.setAlignmentX(LEFT_ALIGNMENT);
            gradingSheetOrPanel.setBackground(contentPanel.getBackground());
            contentPanel.add(gradingSheetOrPanel);
            gradingSheetOrPanel.add(FormattedLabel.asContent("OR"));
            
            JPanel outOfAndQuickNamePanel = new JPanel(new GridLayout(1, 3));
            outOfAndQuickNamePanel.setAlignmentX(LEFT_ALIGNMENT);
            outOfAndQuickNamePanel.setBackground(contentPanel.getBackground());
            contentPanel.add(outOfAndQuickNamePanel);
            
            JPanel outOfPanel = new JPanel(new BorderLayout(5, 0));
            outOfPanel.setAlignmentX(LEFT_ALIGNMENT);
            outOfPanel.setBackground(outOfAndQuickNamePanel.getBackground());
            outOfAndQuickNamePanel.add(outOfPanel);
            outOfPanel.add(FormattedLabel.asContent("Total Points"), BorderLayout.WEST);
            _outOfField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _part.getOutOf() == null ? "" : Double.toString(_part.getOutOf());
                }
                    
                @Override
                protected ValidationResult validate(String value)
                {
                    ValidationResult result = ValidationResult.NO_ISSUE;
                    if(!value.isEmpty())
                    {
                        try
                        {
                            double outOf = Double.parseDouble(value);
                            if(outOf < 0)
                            {
                                result = new ValidationResult(ValidationState.WARNING, "Value is negative");
                            }
                        }
                        catch(NumberFormatException e)
                        {
                            result = new ValidationResult(ValidationState.ERROR, "Numerical value not provided");
                        }
                    }
                    
                    return result;
                }

                @Override
                protected void applyChange(String newValue)
                {
                    _part.setOutOf(newValue.isEmpty() ? null : Double.parseDouble(newValue));
                    _part.setGmlTemplate(null);
                    _gmlField.setText("");
                    _worker.submit(WORKER_TAG, new PartRunnable());
                }
            };
            outOfPanel.add(_outOfField);
            
            outOfAndQuickNamePanel.add(FormattedLabel.asContent("and (optionally)").centerHorizontally());
            
            JPanel quickNamePanel = new JPanel(new BorderLayout(5, 0));
            quickNamePanel.setAlignmentX(LEFT_ALIGNMENT);
            quickNamePanel.setBackground(outOfAndQuickNamePanel.getBackground());
            outOfAndQuickNamePanel.add(quickNamePanel);
            quickNamePanel.add(FormattedLabel.asContent("Quick Name"), BorderLayout.WEST);
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
                    
                    //Allows a through z (upper and lower case), 0 through 9, underscore and dash
                    Matcher matcher = Pattern.compile("[0-9a-zA-Z_-]*").matcher(value);
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
                        result = new ValidationResult(ValidationState.ERROR, "Only letters, numbers, underscores and " +
                                "hypens are allowed");
                    }
                    
                    return result;
                }

                @Override
                protected void applyChange(String newValue)
                {
                    _part.setQuickName(newValue.isEmpty() ? null : newValue);
                    _part.setGmlTemplate(null);
                    _gmlField.setText("");
                    _worker.submit(WORKER_TAG, new PartRunnable());
                }
            };
            quickNamePanel.add(_quickNameField);
            
            _partActionsPanel = new JPanel();
            _partActionsPanel.setLayout(new BoxLayout(_partActionsPanel, BoxLayout.Y_AXIS));
            _partActionsPanel.setAlignmentX(LEFT_ALIGNMENT);
            _partActionsPanel.setBackground(contentPanel.getBackground());
            contentPanel.add(_partActionsPanel);
            this.notifyChangeHasDigitalHandinDirectory(_gePanel._gradableEvent.getDirectory() != null);
        }
        
        void notifyChangeHasDigitalHandinDirectory(final boolean hasDirectory)
        {
            //If there is no longer a digital handin directory, remove actions that require a digital handin directory
            if(!hasDirectory)
            {
                ImmutableSet.Builder<DbPartAction> toRemoveBuilder = ImmutableSet.builder();
                for(DbPartAction partAction : _part.getActions())
                {
                    if(partAction.getType().requiresDigitalHandin())
                    {
                        toRemoveBuilder.add(partAction);
                    }
                }
                final ImmutableSet<DbPartAction> toRemove = toRemoveBuilder.build();
                for(DbPartAction actionToRemove : toRemove)
                {
                    _part.removeAction(actionToRemove);
                }
                
                _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                {
                    @Override
                    public void dbCall() throws SQLException
                    {
                        Allocator.getDatabase().removePartActions(toRemove);
                    }

                    @Override
                    public String dbFailureMessage()
                    {
                        return "Unable to remove part actions as a result of removing digital handin directory";
                    }
                });
            }
            
            _partActionsPanel.removeAll();
            
            for(ActionType type : ActionType.values())
            {
                if(type.requiresDigitalHandin() && !hasDirectory)
                {
                    continue;
                }
                
                _partActionsPanel.add(Box.createVerticalStrut(10));
                
                DbPartAction partAction = _part.getAction(type);
                if(partAction == null)
                {
                    partAction = DbPartAction.build(_part, type);
                }
                PartActionPanel panel = new PartActionPanel(partAction, _partActionsPanel.getBackground());
                panel.setAlignmentX(LEFT_ALIGNMENT);
                _partActionsPanel.add(panel);
            }
            
            _partActionsPanel.repaint();
            _partActionsPanel.revalidate();
        }
        
        @Override
        public Dimension getMaximumSize()
        {
            Dimension size = getPreferredSize();
            size.width = Short.MAX_VALUE;

            return size;
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
        
        private class PartActionPanel extends JPanel
        {
            private final ActionType _type;
            private final GenericJComboBox<PartActionDescription> _actionComboBox;
            private final DbPartAction _partAction;
            private final JPanel _requiredPropsPanel, _optionalPropsPanel;
            
            PartActionPanel(DbPartAction partAction, Color background)
            {
                _type = partAction.getType();
                _partAction = partAction;
                this.setBackground(background);
                
                this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                
                String name = _type.name().charAt(0) + _type.name().substring(1, _type.name().length()).toLowerCase();
                this.add(FormattedLabel.asSubheader(name));
                
                //Text pane to display description info
                final JLabel descriptionLabel = FormattedLabel.asContent("No action will be taken");
                descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
                
                //List of descriptions
                List<PartActionDescription> suggestedDescriptions = new ArrayList<PartActionDescription>();
                List<PartActionDescription> compatibleNotSuggestedDescriptions = new ArrayList<PartActionDescription>();
                for(PartActionDescription desc : ActionRepository.get().getActionDescriptions().values())
                {
                    if(desc.getSuggestedTypes().contains(_type))
                    {
                        suggestedDescriptions.add(desc);
                    }
                    else if(desc.getCompatibleTypes().contains(_type))
                    {
                        compatibleNotSuggestedDescriptions.add(desc);
                    }
                }
                Comparator<PartActionDescription> descriptionNameComparator = new Comparator<PartActionDescription>()
                {
                    @Override
                    public int compare(PartActionDescription d1, PartActionDescription d2)
                    {
                        return d1.getFullName().compareTo(d2.getFullName());
                    }
                };
                Collections.sort(suggestedDescriptions, descriptionNameComparator);
                Collections.sort(compatibleNotSuggestedDescriptions, descriptionNameComparator);
                
                List<PartActionDescription> compatibleDescriptions = new ArrayList<PartActionDescription>();
                compatibleDescriptions.add(null);
                compatibleDescriptions.addAll(suggestedDescriptions);
                compatibleDescriptions.addAll(compatibleNotSuggestedDescriptions);
                
                _actionComboBox = new GenericJComboBox<PartActionDescription>(compatibleDescriptions,
                        new DescriptionProvider<PartActionDescription>()
                {
                    @Override
                    public String getDisplayText(PartActionDescription item)
                    {
                        String display = "No Action";
                        if(item != null)
                        {
                            display = item.getFullName();
                        }
                        
                        return display;
                    }

                    @Override
                    public String getToolTipText(PartActionDescription item)
                    {
                        String tooltip = null;
                        if(item != null)
                        {
                            tooltip = item.getDescription();
                        }
                        
                        return tooltip;
                    }
                });
                _actionComboBox.addSelectionListener(new SelectionListener<PartActionDescription>()
                {
                    @Override
                    public void selectionPerformed(PartActionDescription currValue, PartActionDescription newValue,
                        SelectionAction selectionAction)
                    {
                        final PartActionDescription action = newValue;
                        
                        
                        //Display updated text in the description label
                        String description = (action == null ? "No action will be taken" : action.getDescription());
                        descriptionLabel.setText(description);
                        
                        //Hacky way of not causing this method not to cause database writes if this already matches the
                        //database - this case occurs when the initial programmatic selection is made
                        if( (action == null && _partAction.getName() == null) ||
                            (action != null && action.getFullName().equals(_partAction.getName())) )
                        {
                            return;
                        }
                        
                        if(action != null)
                        {   
                            //Remove existing properties, set new name, and add all required properties
                            
                            final ImmutableSet<DbActionProperty> propsToRemove =
                                    ImmutableSet.copyOf(_partAction.getActionProperties());
                            _partAction.removeAllActionProperties();
                            
                            _partAction.setName(action.getFullName());
                            
                            for(PartActionProperty prop : action.getProperties())
                            {
                                if(prop.isRequired())
                                {
                                    DbActionProperty.build(_partAction, prop.getName());
                                }
                            }
                            final ImmutableSet<DbActionProperty> propsToAdd =
                                    ImmutableSet.copyOf(_partAction.getActionProperties());
                            
                            updatePropertiesPanels();

                            _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                            {
                                @Override
                                public void dbCall() throws SQLException
                                {
                                    Allocator.getDatabase().removePartActionProperties(propsToRemove);
                                    Allocator.getDatabase().putPartActions(ImmutableSet.of(_partAction));
                                    Allocator.getDatabase().putPartActionProperties(propsToAdd);
                                }

                                @Override
                                public String dbFailureMessage()
                                {
                                    return "Unable to set new part action";
                                }
                            });
                        }
                        else
                        {   
                            _partAction.setName(null);
                            _partAction.removeAllActionProperties();
                            
                            updatePropertiesPanels();
                            
                            _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                            {
                                @Override
                                public void dbCall() throws SQLException
                                {
                                    Allocator.getDatabase().removePartActions(ImmutableSet.of(_partAction));;
                                }

                                @Override
                                public String dbFailureMessage()
                                {
                                    return "Unable to remove part action and associated part action properties";
                                }
                            });
                        }
                    }
                });
                _actionComboBox.setAlignmentX(LEFT_ALIGNMENT);
                PartActionDescription currDesc = null;
                if(_partAction != null)
                {
                    currDesc = ActionRepository.get().getActionDescriptions().get(_partAction.getName());
                    _actionComboBox.setGenericSelectedItem(currDesc);
                }
                this.add(_actionComboBox);
                
                //Visually add description label
                this.add(descriptionLabel);
                
                JPanel propertiesPanel = new JPanel();
                propertiesPanel.setBackground(this.getBackground());
                propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
                PaddingPanel paddingPanel = new PaddingPanel(propertiesPanel, 5);
                paddingPanel.setAlignmentX(LEFT_ALIGNMENT);
                paddingPanel.setBackground(this.getBackground());
                this.add(paddingPanel);
                
                _requiredPropsPanel = new JPanel();
                _requiredPropsPanel.setBackground(this.getBackground());
                _requiredPropsPanel.setLayout(new BoxLayout(_requiredPropsPanel, BoxLayout.Y_AXIS));
                _requiredPropsPanel.setAlignmentX(LEFT_ALIGNMENT);
                propertiesPanel.add(_requiredPropsPanel);
                
                propertiesPanel.add(Box.createVerticalStrut(5));
                
                _optionalPropsPanel = new JPanel();
                _optionalPropsPanel.setBackground(this.getBackground());
                _optionalPropsPanel.setLayout(new BoxLayout(_optionalPropsPanel, BoxLayout.Y_AXIS));
                _optionalPropsPanel.setAlignmentX(LEFT_ALIGNMENT);
                propertiesPanel.add(_optionalPropsPanel);
                
                this.updatePropertiesPanels();
            }
            
            private void updatePropertiesPanels()
            {
                _requiredPropsPanel.removeAll();
                _optionalPropsPanel.removeAll();;
                
                PartActionDescription action = null;
                if(_partAction.getName() != null)
                {
                    action = ActionRepository.get().getActionDescriptions().get(_partAction.getName());
                }
                
                if(action != null && !action.getRequiredProperties().isEmpty())
                {
                    _requiredPropsPanel.add(FormattedLabel.asSubheader("Required Properties"));
                    
                    for(final PartActionProperty prop : action.getRequiredProperties())
                    {
                        DbActionProperty dbProp = null;
                        for(DbActionProperty propInDb : _partAction.getActionProperties())
                        {
                            if(prop.getName().equals(propInDb.getKey()))
                            {
                                dbProp = propInDb;
                                break;
                            }
                        }
                        final DbActionProperty propInDb = dbProp;
                        
                        _requiredPropsPanel.add(Box.createVerticalStrut(3));
                        _requiredPropsPanel.add(FormattedLabel.asSubheader(prop.getName()));
                        _requiredPropsPanel.add(FormattedLabel.asContent(prop.getDescription()
                                .replaceAll("\n", "<br/>")));
                        ValidatingTextField valueField = new ValidatingTextField()
                        {
                            @Override
                            protected String getDbValue()
                            {
                                return propInDb.getValue() == null ?  "" : propInDb.getValue();
                            }

                            @Override
                            protected ValidationResult validate(String value)
                            {
                                return ValidationResult.NO_VALIDATION;
                            }

                            @Override
                            protected void applyChange(String newValue)
                            {
                                propInDb.setValue(newValue);
                                
                                _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                                {
                                    @Override
                                    public void dbCall() throws SQLException
                                    {
                                        Allocator.getDatabase().putPartActionProperties(ImmutableSet.of(propInDb));
                                    }

                                    @Override
                                    public String dbFailureMessage()
                                    {
                                        return "Unable to update part action property";
                                    }
                                });
                            }
                        };
                        valueField.setAlignmentX(LEFT_ALIGNMENT);
                        _requiredPropsPanel.add(valueField);
                    }
                }
                
                if(action != null && !action.getOptionalProperties().isEmpty())
                {
                    _optionalPropsPanel.add(FormattedLabel.asSubheader("Optional Properties"));
                    
                    for(final PartActionProperty prop : action.getOptionalProperties())
                    {
                        DbActionProperty dbProp = null;
                        for(DbActionProperty propInDb : _partAction.getActionProperties())
                        {
                            if(prop.getName().equals(propInDb.getKey()))
                            {
                                dbProp = propInDb;
                                break;
                            }
                        }
                        if(dbProp == null)
                        {
                            dbProp = DbActionProperty.build(_partAction, prop.getName());
                        }
                        final DbActionProperty propInDb = dbProp;
                        
                        _optionalPropsPanel.add(Box.createVerticalStrut(3));
                        _optionalPropsPanel.add(FormattedLabel.asSubheader(prop.getName()));
                        _optionalPropsPanel.add(FormattedLabel.asContent(prop.getDescription()
                                .replaceAll("\n", "<br/>")));
                        ValidatingTextField valueField = new ValidatingTextField()
                        {
                            @Override
                            protected String getDbValue()
                            {
                                return propInDb.getValue() == null ?  "" : propInDb.getValue();
                            }

                            @Override
                            protected ValidationResult validate(String value)
                            {
                                return ValidationResult.NO_VALIDATION;
                            }

                            @Override
                            protected void applyChange(String newValue)
                            {
                                if(newValue.isEmpty())
                                {
                                    _partAction.removeActionProperty(propInDb);
                                }
                                else
                                {
                                    propInDb.setValue(newValue);
                                }
                                
                                _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                                {
                                    @Override
                                    public void dbCall() throws SQLException
                                    {
                                        if(propInDb.getValue().isEmpty())
                                        {
                                            Allocator.getDatabase().removePartActionProperties(ImmutableSet.of(propInDb));
                                        }
                                        else
                                        {
                                            Allocator.getDatabase().putPartActionProperties(ImmutableSet.of(propInDb));
                                        }
                                    }

                                    @Override
                                    public String dbFailureMessage()
                                    {
                                        return "Unable to update part action property";
                                    }
                                });
                            }
                        };
                        valueField.setAlignmentX(LEFT_ALIGNMENT);
                        _optionalPropsPanel.add(valueField);
                    }
                }
                
                _requiredPropsPanel.repaint();
                _requiredPropsPanel.revalidate();
                
                _optionalPropsPanel.repaint();
                _optionalPropsPanel.revalidate();
            }
        }
    }
}