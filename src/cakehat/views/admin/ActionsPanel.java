package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.assignment.ActionException;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.database.assignment.PartActionDescription.ActionType;
import cakehat.logging.ErrorReporter;
import cakehat.printing.CITPrinter;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import cakehat.services.ServicesException;
import cakehat.views.admin.AssignmentTree.AssignmentTreeSelection;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
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
        private final StandardButton _emailGradingSheetButton, _printGradingSheetButton, _manageGroupsButton;
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
            
            _printGradingSheetButton = new StandardButton("Print Grading Sheet", "Print Grading Sheets", IconImage.DOCUMENT_PRINT);
            _printGradingSheetButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    printGradingSheetButtonActionPerformed();
                }
            });
            this.add(_printGradingSheetButton);
            
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
            _printGradingSheetButton.setVisible(showGradingSheetButtons);
            _manageGroupsButton.setVisible(showManageGroupsButton);
            
            _noActionsAvailable.setVisible(!(showGradingSheetButtons || showManageGroupsButton));
            
            _emailGradingSheetButton.updateText(selectedGroups.size() < 2);
            _printGradingSheetButton.updateText(selectedGroups.size() < 2);
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
        private final StandardButton _manualDistributorButton;
        private final Map<ActionType, StandardButton> _actionButtons =
                new EnumMap<ActionType, StandardButton>(ActionType.class);
        
        PartPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            this.add(FormattedLabel.asSubheader("Part"));
         
            //Manual distributor button
            _manualDistributorButton = new StandardButton("Manual Distributor", null, IconImage.DOCUMENT_PROPERTIES);
            _manualDistributorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    manualDistributorButtonActionPerformed();
                }
            });
            this.add(_manualDistributorButton);
            
            //Action buttons
            this.add(createActionButton("Grading Guide", "Grading Guide", IconImage.TEXT_X_GENERIC,
                    ActionType.GRADING_GUIDE));
            this.add(createActionButton("Demo", "Demo", IconImage.APPLICATIONS_SYSTEM,
                    ActionType.DEMO));
            this.add(createActionButton("Print Handin", "Print Handins", IconImage.PRINTER, ActionType.PRINT));
            this.add(createActionButton("Run Handin", "Run Handins", IconImage.GO_NEXT, ActionType.RUN));
            this.add(createActionButton("Open Handin", "Open Handins", IconImage.DOCUMENT_OPEN, ActionType.OPEN));
            this.add(createActionButton("Test Handin", "Test Handins", IconImage.UTILITIES_SYSTEM_MONITOR,
                    ActionType.TEST));
            this.add(createActionButton("View Readme", "View Readmes", IconImage.TEXT_X_GENERIC,
                    ActionType.README));
        }
        
        private StandardButton createActionButton(String singleText, String pluralText, IconImage image,
                final ActionType type)
        {
            StandardButton button = new StandardButton(singleText, pluralText, image);
            _actionButtons.put(type, button);
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    actionButtonActionPerformed(type);
                }
            });

            return button;
        }
        
        void notifySelectionChanged(Part part, Set<Group> selectedGroups, Set<Student> selectedStudentsNotInGroups)
        {
            if(part == null)
            {
                _manualDistributorButton.setEnabled(false);
                for(StandardButton button : _actionButtons.values())
                {
                    button.setEnabled(false);
                }
            }
            else
            {
                _manualDistributorButton.setEnabled(true);
                
                for(Entry<ActionType, StandardButton> entry : _actionButtons.entrySet())
                {
                    boolean enable = false;
                    try
                    {
                        enable = part.isActionSupported(entry.getKey(), selectedGroups);
                    }
                    catch(ActionException e)
                    {
                        ErrorReporter.report("Could not determine if action is supported\n" +
                                "Part: " + part.getFullDisplayName() + "\n" +
                                "Groups: " + selectedGroups, e);
                    }
                    entry.getValue().setEnabled(enable);
                }
            }
            
            //Determine whether to show buttons that act on digital handins
            boolean showDigitalHandinOptions = part != null && part.getGradableEvent().hasDigitalHandins() &&
                                               !selectedGroups.isEmpty();
            for(Entry<ActionType, StandardButton> entry : _actionButtons.entrySet())
            {
                if(entry.getKey().requiresDigitalHandin())
                {
                    entry.getValue().setVisible(showDigitalHandinOptions);
                }
            }
            
            //Determine the pluralization of the action buttons
            for(StandardButton button : _actionButtons.values())
            {
                button.updateText(selectedGroups.size() == 1);
            }
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
    
    private void printGradingSheetButtonActionPerformed()
    {
        CITPrinter printer = Allocator.getGradingServices().getPrinter();
        if(printer != null)
        {
            //Save the current grading sheet so that GRD generation reflects any changes made
            _adminView.saveDisplayedGradingSheet();
            
            try
            {
                Allocator.getGradingServices().printGRDFiles(_treeSelection.getAssignment(), _selectedGroups, printer);
            }
            catch(ServicesException ex)
            {
                ErrorReporter.report(ex);
            }
        }
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
    
    private void actionButtonActionPerformed(ActionType type)
    {
        Part part = _treeSelection.getPart();
        Set<Group> groups = _selectedGroups;
        try
        {
            part.performAction(_adminView, type, groups);
        }
        catch(ActionException ex)
        {   
            String message = "Could not " + type;
            if(groups.isEmpty())
            {
                message += " for group(s) " + groups;
            }
            if(part != null)
            {
                message += " on part " + part.getFullDisplayName();
            }
            message += ".";

            ErrorReporter.report(message, ex);
        }
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