package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.assignment.Action;
import cakehat.assignment.TaskException;
import cakehat.logging.ErrorReporter;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import cakehat.services.ServicesException;
import cakehat.views.admin.AssignmentTree.AssignmentTreeSelection;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import support.ui.FormattedLabel;

/**
 * A panel containing labels and buttons to showing applicable actions to be taken based on the current selection.
 *
 * @author jak2
 */
class ActionsPanel extends JPanel
{
    private final AdminView _adminView;
    
    private final JLabel _noActionsAvailable;
    private final AssignmentPanel _assignmentPanel;
    private final GradableEventPanel _gradableEventPanel;
    private final PartPanel _partPanel;
    
    private AssignmentTreeSelection _treeSelection;
    private Set<Group> _selectedGroups;
    private Set<Student> _selectedStudentsNotInGroups;
    
    ActionsPanel(AdminView adminView)
    {
        _adminView = adminView;
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.add(FormattedLabel.asHeader("Actions"));

        this.add(Box.createVerticalStrut(5));
        
        _noActionsAvailable = FormattedLabel.asContent(" - No Actions Available").usePlainFont();
        _noActionsAvailable.setVisible(false);
        this.add(_noActionsAvailable);
        
        _assignmentPanel = new AssignmentPanel();
        _assignmentPanel.setAlignmentX(LEFT_ALIGNMENT);
        this.add(_assignmentPanel);
        
        this.add(Box.createVerticalStrut(10));
        
        _gradableEventPanel = new GradableEventPanel();
        _gradableEventPanel.setAlignmentX(LEFT_ALIGNMENT);
        this.add(_gradableEventPanel);
        
        this.add(Box.createVerticalStrut(10));
        
        _partPanel = new PartPanel();
        _partPanel.setAlignmentX(LEFT_ALIGNMENT);
        this.add(_partPanel);
    }
    
    /**
     * 
     * 
     * @param treeSelection
     * @param selectedGroups the groups that exist for the selected students
     * @param selectedStudentsNotInGroups the students who are not in any of the groups passed in. If no assignment is
     * selected (directly or indirectly) then all selected students should be in this set and selectedGroups should be
     * an empty set because groups are defined by the assignment they belong to
     */
    void notifySelectionChanged(AssignmentTreeSelection treeSelection, Set<Group> selectedGroups,
            Set<Student> selectedStudentsNotInGroups)
    {
        _treeSelection = treeSelection;
        _selectedGroups = selectedGroups;
        _selectedStudentsNotInGroups = selectedStudentsNotInGroups;
        
        _assignmentPanel.notifySelectionChanged(treeSelection.getAssignment(), selectedGroups, selectedStudentsNotInGroups);
        _gradableEventPanel.notifySelectionChanged(treeSelection.getGradableEvent(), selectedGroups, selectedStudentsNotInGroups);
        _partPanel.notifySelectionChanged(treeSelection.getPart(), selectedGroups, selectedStudentsNotInGroups);
        
        _assignmentPanel.setVisible(treeSelection.getAssignment() != null);
        _gradableEventPanel.setVisible(treeSelection.getGradableEvent() != null);
        _partPanel.setVisible(treeSelection.getPart() != null);
        
        //If no assignment, gradable event, or part panel has been shown - indicate there are no actions available
        _noActionsAvailable.setVisible(treeSelection.getAssignment() == null);
    }
    
    private class AssignmentPanel extends JPanel
    {
        private final StandardButton _emailGradingSheetButton, _manageGroupsButton;
        private final JLabel _noActionsAvailable;
        
        AssignmentPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            this.add(FormattedLabel.asSubheader("Assignment"));
            
            //Label
            _noActionsAvailable = FormattedLabel.asContent(" - No Actions Available").usePlainFont();
            _noActionsAvailable.setVisible(false);
            this.add(_noActionsAvailable);
            
            _emailGradingSheetButton = new StandardButton("Email Grading Sheet", "Email Grading Sheets", IconImage.MAIL_FORWARD);
            _emailGradingSheetButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    emailGradingSheetButtonActionPerformed();
                }
            });
            this.add(_emailGradingSheetButton);
            
            _manageGroupsButton = new StandardButton("Manage Groups", null, IconImage.SYSTEM_USERS);
            _manageGroupsButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    manageGroupsButtonActionPerformed();
                }
            });
            this.add(_manageGroupsButton);
        }
        
        void notifySelectionChanged(Assignment assignment, Set<Group> selectedGroups,
                Set<Student> selectedStudentsNotInGroups)
        {
            boolean showGradingSheetButtons = !selectedGroups.isEmpty();
            boolean showManageGroupsButton = assignment != null && assignment.hasGroups();
            
            _emailGradingSheetButton.setVisible(showGradingSheetButtons);
            _manageGroupsButton.setVisible(showManageGroupsButton);
            
            _noActionsAvailable.setVisible(!(showGradingSheetButtons || showManageGroupsButton));
            
            _emailGradingSheetButton.updateText(selectedGroups.size() < 2);
        }
    }
    
    private class GradableEventPanel extends JPanel
    {
        private final StandardButton _autoDistributorButton;
        private final JLabel _noActionsAvailable;
        
        GradableEventPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            this.add(FormattedLabel.asSubheader("Gradable Event"));
            
            //Label
            _noActionsAvailable = FormattedLabel.asContent(" - No Actions Available").usePlainFont();
            _noActionsAvailable.setVisible(false);
            this.add(_noActionsAvailable);
            
            //Buttons
            _autoDistributorButton = new StandardButton("Auto Distributor", null, IconImage.DOCUMENT_SAVE_AS);
            _autoDistributorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    autoDistributorButtonActionPerformed();
                }
            });
            this.add(_autoDistributorButton);
        }
        
        void notifySelectionChanged(GradableEvent gradableEvent, Set<Group> selectedGroups,
            Set<Student> selectedStudentsNotInGroups)
        {   
            boolean showButtons = gradableEvent != null && gradableEvent.hasDigitalHandins();
            
            _autoDistributorButton.setVisible(showButtons);
            _noActionsAvailable.setVisible(!showButtons);
        }
    }
    
    private class PartPanel extends JPanel
    {   
        private final JPanel _builtinButtonsPanel, _nonHandinActionsPanel, _handinActionsPanel;
        
        PartPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            this.add(FormattedLabel.asSubheader("Part"));
            
            //Panel that contains buttons which are not user configured
            _builtinButtonsPanel = new JPanel();
            _builtinButtonsPanel.setLayout(new BoxLayout(_builtinButtonsPanel, BoxLayout.Y_AXIS));
            _builtinButtonsPanel.setAlignmentX(LEFT_ALIGNMENT);
            StandardButton manualDistributorButton = new StandardButton("Manual Distributor", null,
                        IconImage.DOCUMENT_PROPERTIES);
            manualDistributorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    manualDistributorButtonActionPerformed();
                }
            });
            _builtinButtonsPanel.add(manualDistributorButton);
            this.add(_builtinButtonsPanel);
            
            //Panel for course configured actions that do not rely on a digital handin
            _nonHandinActionsPanel = new JPanel();
            _nonHandinActionsPanel.setLayout(new BoxLayout(_nonHandinActionsPanel, BoxLayout.Y_AXIS));
            _nonHandinActionsPanel.setAlignmentX(LEFT_ALIGNMENT);
            this.add(_nonHandinActionsPanel);
            
            //Panel for course configured actions that rely on a digital handin
            _handinActionsPanel = new JPanel();
            _handinActionsPanel.setLayout(new BoxLayout(_handinActionsPanel, BoxLayout.Y_AXIS));
            _handinActionsPanel.setAlignmentX(LEFT_ALIGNMENT);
            this.add(_handinActionsPanel);
        }
        
        void notifySelectionChanged(final Part part, final Set<Group> selectedGroups,
                Set<Student> selectedStudentsNotInGroups)
        {
            _builtinButtonsPanel.setVisible(part != null);
            _nonHandinActionsPanel.removeAll();
            _handinActionsPanel.removeAll();
            
            if(part != null)
            {
                for(final Action action : part.getActions())
                {
                    StandardButton actionButton = new StandardButton(action.getName(), null, action.getIcon());

                    //Determine if it should be enabled
                    boolean enableButton = false;
                    try
                    {
                        enableButton = action.isTaskSupported(selectedGroups);
                    }
                    catch(TaskException e)
                    {
                        ErrorReporter.report("Could not determine if action is supported\n" +
                                    "Action: " + action.getName() + "\n" +
                                    "Part: " + part.getFullDisplayName() + "\n" +
                                    "Groups: " + selectedGroups, e);
                    }
                    actionButton.setEnabled(enableButton);

                    //Wire up action
                    actionButton.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent ae)
                        {
                            try
                            {
                                action.performTask(_adminView, selectedGroups);
                            }
                            catch(TaskException e)
                            {
                                ErrorReporter.report("Could not peform action\n" +
                                    "Action: " + action.getName() + "\n" +
                                    "Part: " + part.getFullDisplayName() + "\n" +
                                    "Groups: " + selectedGroups, e);
                            }
                        }
                    });
                    
                    if(!action.requiresDigitalHandin())
                    {
                        _nonHandinActionsPanel.add(actionButton);
                    }
                    else if(!selectedGroups.isEmpty())
                    {
                        _handinActionsPanel.add(actionButton);
                    }
                }
            }
            
            //Force visual update to reflect these changes
            this.repaint();
            this.revalidate();
        }
    }
    
    private void autoDistributorButtonActionPerformed()
    {
        new AutomaticDistributorView(_treeSelection.getGradableEvent(), _adminView);
    }

    private void manualDistributorButtonActionPerformed()
    {
        new ManualDistributorView(_treeSelection.getPart(), _adminView);
    }
    
    private void emailGradingSheetButtonActionPerformed()
    {
        //Save the current grading sheet so that GRD generation reflects any changes made
        _adminView.saveDisplayedGradingSheet();
            
        try
        {
            Allocator.getGradingServices().emailGRDFiles(_treeSelection.getAssignment(), _selectedGroups);
        }
        catch(ServicesException ex)
        {
            ErrorReporter.report(ex);
        }
    }
    
    private void manageGroupsButtonActionPerformed()
    {
        new ManageGroupsView(_adminView, _treeSelection.getAssignment());
    }
    
    private static class StandardButton extends JPanel
    {
        private final JButton _button;
        private final String _singleText, _pluralText;
        
        StandardButton(String singleText, String pluralText, IconImage image)
        {
            _singleText = "<html><b>" + singleText + "</b></html>";
            _pluralText = "<html><b>" + pluralText + "</b></html>";
            
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setAlignmentX(LEFT_ALIGNMENT);
            
            this.add(Box.createVerticalStrut(5));
            
            _button = new JButton();
            _button.setMargin(new Insets(2, 10, 2, 10));
            _button.setIcon(IconLoader.loadIcon(IconSize.s16x16, image));
            _button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            _button.setIconTextGap(10);
            this.add(_button);
            
            this.updateText(true);
        }
        
        final void updateText(boolean showSingle)
        {
            if(showSingle)
            {
                _button.setText(_singleText);
            }
            else
            {
                _button.setText(_pluralText);
            }
        }
        
        final void addActionListener(ActionListener listener)
        {
            _button.addActionListener(listener);
        }
        
        public void setEnabled(boolean enable)
        {
            _button.setEnabled(enable);
        }
    }
}