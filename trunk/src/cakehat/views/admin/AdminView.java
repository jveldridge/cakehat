package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.CakehatSession;
import cakehat.CakehatSession.ConnectionType;
import cakehat.CakehatReleaseInfo;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import cakehat.views.admin.AssignmentTree.AssignmentTreeSelection;
import cakehat.views.shared.gradingsheet.GradingSheet;
import com.google.common.collect.Iterables;
import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
public class AdminView extends JFrame
{
    private final JScrollPane _mainPane;
    private final AssignmentTree _assignmentTree;
    private final StudentList _studentList;
    private final ActionsPanel _actionsPanel;
    private final StatisticsPanel _statisticsPanel;
    private final StudentInfoPanel _studentInfoPanel;
    
    private AssignmentTreeSelection _treeSelection;
    private Set<Student> _selectedStudents; 
    private GradingSheet _currentlyDisplayedSheet;
    
    private AdminView()
    {
        super("cakehat (admin)" + (CakehatSession.getUserConnectionType() == ConnectionType.REMOTE ? " [ssh]" : ""));
        
        //Close operation
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent we)
            {
                saveDisplayedGradingSheet();
            }
        });
        
        //Setup UI
        _mainPane = new JScrollPane();
        _actionsPanel = new ActionsPanel(this);
        _assignmentTree = new AssignmentTree();
        _studentList = new StudentList();
        _statisticsPanel = new StatisticsPanel();
        _studentInfoPanel = new StudentInfoPanel();
        this.initUI();
        this.setJMenuBar(new AdminMenu(this));
        
        //Setup focus traversal
        this.initFocusTraversalPolicy();
        
        //Display
        this.setMinimumSize(new Dimension(1024, 550));
        this.setPreferredSize(new Dimension(1024, 550));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(true);
    }
    
    private void initUI()
    {
        //Visual setup
        this.setLayout(new BorderLayout(0, 0));
        JPanel contentPanel = new JPanel();
        this.add(contentPanel, BorderLayout.CENTER);
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        
        Dimension assignmentTreeSize = new Dimension(200, Short.MAX_VALUE);
        _assignmentTree.setMinimumSize(assignmentTreeSize);
        _assignmentTree.setPreferredSize(assignmentTreeSize);
        _assignmentTree.setMaximumSize(assignmentTreeSize);
        contentPanel.add(_assignmentTree);
        
        contentPanel.add(Box.createHorizontalStrut(5));
        
        Dimension studentListSize = new Dimension(140, Short.MAX_VALUE);
        _studentList.setMinimumSize(studentListSize);
        _studentList.setPreferredSize(studentListSize);
        _studentList.setMaximumSize(studentListSize);
        contentPanel.add(_studentList);
        
        contentPanel.add(Box.createHorizontalStrut(5));
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        contentPanel.add(mainPanel);
        //This label exists to match the exact spacing of the label headers for assignments, students, and actions
        mainPanel.add(FormattedLabel.asHeader(" "));
        mainPanel.add(Box.createVerticalStrut(5));
        _mainPane.setAlignmentX(LEFT_ALIGNMENT);
        _mainPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(_mainPane);
        
        contentPanel.add(Box.createHorizontalStrut(5));
        
        Dimension actionsPanelSize = new Dimension(185, Short.MAX_VALUE);
        _actionsPanel.setMinimumSize(actionsPanelSize);
        _actionsPanel.setPreferredSize(actionsPanelSize);
        _actionsPanel.setMaximumSize(actionsPanelSize);
        contentPanel.add(_actionsPanel);
        
        //Selection change
        _treeSelection = _assignmentTree.getSelection();
        _assignmentTree.addSelectionListener(new AssignmentTree.AssignmentTreeListener()
        {
            @Override
            public void selectionChanged(AssignmentTreeSelection treeSelection)
            {
                notifySelectionChanged(treeSelection);
            }
        });
        
        _selectedStudents = new HashSet<Student>(_studentList.getSelection());
        _studentList.addSelectionListener(new StudentList.StudentListListener()
        {
            @Override
            public void selectionChanged(List<Student> selection)
            {
                notifySelectionChanged(new HashSet<Student>(selection));
            }
        });
        
        this.notifySelectionChanged(_treeSelection, _selectedStudents);
    }
    
    private void notifySelectionChanged(AssignmentTreeSelection treeSelection)
    {
        _treeSelection = treeSelection;
        notifySelectionChanged(treeSelection, _selectedStudents);
    }
    
    private void notifySelectionChanged(Set<Student> students)
    {
        _selectedStudents = students;
        notifySelectionChanged(_treeSelection, students);
    }
    
    private void notifySelectionChanged(AssignmentTreeSelection treeSelection, Set<Student> students)
    {   
        //Determine the groups that are selected as a result of this selection, and any students not in groups
        //Students not in any groups should only be possible for the case that the assignment is a group assignment
        Set<Group> selectedGroups = new HashSet<Group>();
        Set<Student> selectedStudentsNotInGroups = new HashSet<Student>();
        
        Assignment selectedAsgn = treeSelection.getAssignment();
        if(selectedAsgn == null)
        {
            selectedStudentsNotInGroups.addAll(students);
        }
        else
        {
            try
            {
                for(Student student : students)
                {
                    Group group = Allocator.getDataServices().getGroup(selectedAsgn, student);
                    if(group == null)
                    {
                        selectedStudentsNotInGroups.add(student);
                    }
                    else
                    {
                        selectedGroups.add(group);
                    }
                }
            }
            //If an issue occurs trying to get a group treat the situation like no students or groups have been selected
            catch(ServicesException e)
            {
                selectedGroups.clear();
                selectedStudentsNotInGroups.clear();
                ErrorReporter.report("Unable to retrieve a group for a student", e);
            }
        }
        
        //Tell the actions panel to update
        _actionsPanel.notifySelectionChanged(treeSelection, selectedGroups, selectedStudentsNotInGroups);
        
        //Update the center panel
        updateMainPane(treeSelection, selectedGroups, selectedStudentsNotInGroups);
    }
    
    private void updateMainPane(AssignmentTreeSelection treeSelection, Set<Group> selectedGroups,
            Set<Student> selectedStudentsNotInGroups)
    {
        saveDisplayedGradingSheet();
        _currentlyDisplayedSheet = null;
        
        Component componentToDisplay = null;
        
        Assignment asgn = treeSelection.getAssignment();
        GradableEvent ge = treeSelection.getGradableEvent();
        Part part = treeSelection.getPart();
        
        if(asgn == null)
        {
            if(selectedStudentsNotInGroups.isEmpty())
            {
                componentToDisplay = FormattedLabel.asHeader("cakehat v" + CakehatReleaseInfo.getVersion())
                        .centerHorizontally();
            }
            else
            {
                _studentInfoPanel.displayFor(selectedStudentsNotInGroups);
                componentToDisplay = _studentInfoPanel;
            }
        }
        else
        {
            //If there are students selected that do not belong to a group
            if(!selectedStudentsNotInGroups.isEmpty())
            {
                //This case is fine - just means groups have not been created for all of the selected students
                if(asgn.hasGroups())
                {
                    StringBuilder msgBuilder = new StringBuilder();
                    msgBuilder.append("<center>");
                    msgBuilder.append("<font size=4>");
                    msgBuilder.append("The following selected students are not in a group");
                    msgBuilder.append("</font>");
                    msgBuilder.append("<br/><br/>");
                    for(Student student : selectedStudentsNotInGroups)
                    {
                        msgBuilder.append("<font size=3>");
                        msgBuilder.append(student.getLogin());
                        msgBuilder.append(" - ");
                        msgBuilder.append(student.getName());
                        msgBuilder.append("</font>");
                        msgBuilder.append("<br/>");
                    }
                    msgBuilder.append("</center>");
                    
                    componentToDisplay = FormattedLabel.asContent(msgBuilder.toString()).centerHorizontally();
                }
                //If this occurs it is a cakehat bug - autogroups of 1 should have been created
                else
                {
                    componentToDisplay = FormattedLabel.asHeader("Internal Failure - autogroups of one were not created")
                            .showAsErrorMessage().centerHorizontally();
                    
                    ErrorReporter.report("Missing autogroups of one\n" +
                            "Assignment: " + asgn + "\n" +
                            "Students: " + selectedStudentsNotInGroups);
                }
            }
            //Multiple groups have been selected
            else if(selectedGroups.size() > 1)
            {
                _statisticsPanel.displayFor(treeSelection, selectedGroups);
                componentToDisplay = _statisticsPanel;
            }       
            //Zero or one groups have been selected
            else if(selectedGroups.isEmpty() || selectedGroups.size() == 1)
            {
                //If the group is null a template will be shown
                Group group = selectedGroups.isEmpty() ? null : Iterables.get(selectedGroups, 0);
                if(part != null)
                {
                    _currentlyDisplayedSheet = Allocator.getGradingSheetManager()
                            .getGradingSheet(part, group, true, true);
                }
                else if(ge != null)
                {
                    _currentlyDisplayedSheet = Allocator.getGradingSheetManager()
                            .getGradingSheet(ge, group, true, true);
                }
                else
                {
                    _currentlyDisplayedSheet = Allocator.getGradingSheetManager()
                            .getGradingSheet(asgn, group, true, true);
                }
                
                componentToDisplay = _currentlyDisplayedSheet.getAsComponent();
            }
        }
        
        _mainPane.setViewportView(componentToDisplay);
        _mainPane.repaint();
        _mainPane.revalidate();
    }
    
    void saveDisplayedGradingSheet()
    {
        if(_currentlyDisplayedSheet != null)
        {
            _currentlyDisplayedSheet.save();
        }
    }
    
    void refresh()
    {
        //Get most recent student and group information from the database
        try
        {
            Allocator.getDataServices().updateDataCache();
            
            saveDisplayedGradingSheet();
            
            //Reapply the filter on the student list which will cause any additions or changes to students to be
            //reflected
            _studentList.applyFilterTerm();
        }
        catch(ServicesException e)
        {
            ErrorReporter.report("Unable to refresh", e);
        }
    }
    
    private void initFocusTraversalPolicy()
    {
        //Add Enter as forward traversal key
        Set<AWTKeyStroke> forwardKeys = this.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);

        this.setFocusTraversalPolicy(new FocusTraversalPolicy()
        {
            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt)
            {
                Component next = null;
                
                if(cmpnt == _studentList.getFilterField())
                {
                    //If there are students displayed in the student list
                    if(_studentList.getList().hasListData())
                    {
                        //If this is because of a filter term
                        if(!_studentList.getFilterField().getText().isEmpty())
                        {
                            _studentList.getFilterField().setText(_studentList.getList().getListData().get(0).getLogin());
                            _studentList.getList().selectFirst();

                            //If the main pane holds a grading sheet
                            if(_currentlyDisplayedSheet != null)
                            {
                                next = _currentlyDisplayedSheet.getFirstComponent();
                            }
                        }
                        else
                        {
                            next = _studentList.getList();
                        }
                    }
                }
                else if(cmpnt == _studentList.getList())
                {
                    if(_studentList.getList().hasSelectedValue() && _currentlyDisplayedSheet != null)
                    {
                        next = _currentlyDisplayedSheet.getFirstComponent();
                    }
                }
                else if(_currentlyDisplayedSheet != null && _currentlyDisplayedSheet.containsComponent(cmpnt))
                {
                    next = _currentlyDisplayedSheet.getComponentAfter(cmpnt);
                }
                
                //If no next was defined, select all text in the filter field and move focus to it
                if(next == null)
                {
                    _studentList.getFilterField().selectAll();
                    next = _studentList.getFilterField();
                }

                return next;
            }

            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt)
            {
                Component prev = null;
                
                if(_currentlyDisplayedSheet != null && _currentlyDisplayedSheet.containsComponent(cmpnt))
                {
                    prev = _currentlyDisplayedSheet.getComponentBefore(cmpnt);
                }
                else if(cmpnt == _studentList.getFilterField())
                {
                    prev = _assignmentTree.getTree();
                }
                
                //If no previous was defined, select all text in the filter field and move focus to it  
                if(prev == null)
                {
                    _studentList.getFilterField().selectAll();
                    prev = _studentList.getFilterField();
                }

                return prev;
            }

            @Override
            public Component getFirstComponent(Container cntnr)
            {
                return _studentList.getFilterField();
            }

            @Override
            public Component getLastComponent(Container cntnr)
            {
                return _studentList.getFilterField();
            }

            @Override
            public Component getDefaultComponent(Container cntnr)
            {
                return _studentList.getFilterField();
            }
        });
    }
    
    public static void launch()
    {   
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {   
                new AdminView().setVisible(true);
            }
        });
    }
}