package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.services.ServicesException;
import cakehat.views.admin.AssignmentTree.AssignmentTreeSelection;
import cakehat.views.shared.ErrorView;
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
import java.awt.Font;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

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
    
    private AssignmentTreeSelection _treeSelection;
    private Set<Student> _selectedStudents; 
    private GradingSheet _currentlyDisplayedSheet;
    
    private AdminView(boolean isSSH)
    {
        super("cakehat (admin)" + (isSSH ? " [ssh]" : ""));
        
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
        this.initUI();
        
        //Setup focus traversal
        this.initFocusTraversalPolicy();
        _assignmentTree.selectFirstAssignment();
        
        //Display
        this.setMinimumSize(new Dimension(1125, 550));
        this.setPreferredSize(new Dimension(1125, 550));
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
        
        Dimension assignmentTreeSize = new Dimension(215, Short.MAX_VALUE);
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
        
        _mainPane.setBorder(null);
        _mainPane.getViewport().setBackground(Color.WHITE);
        contentPanel.add(_mainPane);
        
        contentPanel.add(Box.createHorizontalStrut(5));
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setMinimumSize(new Dimension(5, Short.MAX_VALUE));
        separator.setPreferredSize(new Dimension(5, Short.MAX_VALUE));
        separator.setMaximumSize(new Dimension(5, Short.MAX_VALUE));
        contentPanel.add(separator);
        contentPanel.add(Box.createHorizontalStrut(5));
        
        Dimension actionsPanelSize = new Dimension(200, Short.MAX_VALUE);
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
                new ErrorView(e, "Unable to retrieve a group for a student");
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
            JLabel label = new JLabel("<html>Select an Assignment, Gradable Event, or Part</html>");
            label.setFont(new Font("Dialog", Font.BOLD, 16));
            label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            
            componentToDisplay = label;
        }
        else
        {
            if(selectedGroups.size() < 2)
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
            else
            {
                String studentOrGroup = asgn.hasGroups() ? "group" : "student";
                JLabel label = new JLabel("<html>Multi-" + studentOrGroup + " display not yet supported</html>");
                label.setFont(new Font("Dialog", Font.BOLD, 16));
                label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                
                componentToDisplay = label;
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
    
    private void initFocusTraversalPolicy()
    {
        // Add Enter as forward traversal key
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
                    if(_studentList.getList().hasSelectedValue())
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
    
    public static void launch(final boolean isSSH)
    {   
        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {   
                new AdminView(isSSH).setVisible(true);
            }
        });
    }
}