package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.assignment.ActionException;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.MissingHandinException;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.database.Student;
import cakehat.printing.CITPrinter;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import cakehat.services.ServicesException;
import cakehat.views.admin.AssignmentTree.AssignmentTreeSelection;
import cakehat.views.shared.ErrorView;
import com.google.common.collect.Iterables;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.compress.archivers.ArchiveException;
import support.ui.ModalDialog;

/**
 * A panel containing labels and buttons to showing applicable actions to be taken based on the current selection.
 *
 * @author jak2
 */
class ActionsPanel extends JPanel
{
    private final AdminView _adminView;
    
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

        JLabel actionsLabel = new JLabel("Actions");
        actionsLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        actionsLabel.setAlignmentX(LEFT_ALIGNMENT);
        this.add(actionsLabel);

        this.add(Box.createVerticalStrut(5));
        
        _assignmentPanel = new AssignmentPanel();
        _assignmentPanel.setAlignmentX(LEFT_ALIGNMENT);
        this.add(_assignmentPanel);
        
        _gradableEventPanel = new GradableEventPanel();
        _gradableEventPanel.setAlignmentX(LEFT_ALIGNMENT);
        this.add(_gradableEventPanel);
        
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
    }
    
    private class AssignmentPanel extends JPanel
    {
        private ActionButton _emailGradingSheetButton, _printGradingSheetButton;
        private JLabel _noActionsAvailable;
        
        AssignmentPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            JLabel assignmentLabel = new JLabel("Assignment");
            assignmentLabel.setFont(new Font("Dialog", Font.BOLD, 13));
            assignmentLabel.setAlignmentX(LEFT_ALIGNMENT);
            this.add(assignmentLabel);
            
            this.add(Box.createVerticalStrut(5));
            
            //Label
            _noActionsAvailable = new JLabel(" - No Actions Available");
            _noActionsAvailable.setFont(new Font("Dialog", Font.PLAIN, 11));
            _noActionsAvailable.setAlignmentX(LEFT_ALIGNMENT);
            _noActionsAvailable.setVisible(false);
            this.add(_noActionsAvailable);
            
            _emailGradingSheetButton = new ActionButton("Email Grading Sheet", "Email Grading Sheets", IconImage.MAIL_FORWARD);
            _emailGradingSheetButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    emailGradingSheetButtonActionPerformed();
                }
            });
            this.add(_emailGradingSheetButton);
            
            this.add(Box.createVerticalStrut(5));
            
            _printGradingSheetButton = new ActionButton("Print Grading Sheet", "Print Grading Sheets", IconImage.DOCUMENT_PRINT);
            _printGradingSheetButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    printGradingSheetButtonActionPerformed();
                }
            });
            this.add(_printGradingSheetButton);
            
            this.add(Box.createVerticalStrut(10));
        }
        
        void notifySelectionChanged(Assignment assignment, Set<Group> selectedGroups,
                Set<Student> selectedStudentsNotInGroups)
        {   
            _emailGradingSheetButton.setVisible(!selectedGroups.isEmpty());
            _printGradingSheetButton.setVisible(!selectedGroups.isEmpty());
            _noActionsAvailable.setVisible(selectedGroups.isEmpty());
            
            _emailGradingSheetButton.updateText(selectedGroups.size() < 2);
            _printGradingSheetButton.updateText(selectedGroups.size() < 2);
        }
    }
    
    private class GradableEventPanel extends JPanel
    {
        private ActionButton _autoDistributorButton;
        private JLabel _noActionsAvailable;
        
        GradableEventPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            JLabel gradableEventLabel = new JLabel("Gradable Event");
            gradableEventLabel.setFont(new Font("Dialog", Font.BOLD, 13));
            gradableEventLabel.setAlignmentX(LEFT_ALIGNMENT);
            this.add(gradableEventLabel);
            
            this.add(Box.createVerticalStrut(5));
            
            //Label
            _noActionsAvailable = new JLabel(" - No Actions Available");
            _noActionsAvailable.setFont(new Font("Dialog", Font.PLAIN, 11));
            _noActionsAvailable.setAlignmentX(LEFT_ALIGNMENT);
            _noActionsAvailable.setVisible(false);
            this.add(_noActionsAvailable);
            
            //Buttons
            _autoDistributorButton = new ActionButton("Auto Distributor", null, IconImage.DOCUMENT_SAVE_AS);
            _autoDistributorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    autoDistributorButtonActionPerformed();
                }
            });
            this.add(_autoDistributorButton);
        
            this.add(Box.createVerticalStrut(10));
        }
        
        void notifySelectionChanged(GradableEvent gradableEvent, Set<Group> selectedGroups,
            Set<Student> selectedStudentsNotInGroups)
        {   
            _autoDistributorButton.setVisible(gradableEvent != null && gradableEvent.hasDigitalHandins());
            _noActionsAvailable.setVisible(!(gradableEvent != null && gradableEvent.hasDigitalHandins()));
        }
    }
    
    private class PartPanel extends JPanel
    {
        private ActionButton _manualDistributorButton, _viewGradingGuideButton, _demoButton, _openButton, _runButton,
                             _testButton, _printButton, _readmeButton;
        
        PartPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            
            JLabel partLabel = new JLabel("Part");
            partLabel.setFont(new Font("Dialog", Font.BOLD, 13));
            partLabel.setAlignmentX(LEFT_ALIGNMENT);
            this.add(partLabel);
            
            this.add(Box.createVerticalStrut(5));
         
            //Buttons
            _manualDistributorButton = new ActionButton("Manual Distributor", null, IconImage.DOCUMENT_PROPERTIES);
            _manualDistributorButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    manualDistributorButtonActionPerformed();
                }
            });
            this.add(_manualDistributorButton);
            
            this.add(Box.createVerticalStrut(5));
            
            _viewGradingGuideButton = new ActionButton("Grading Guide", null, IconImage.TEXT_X_GENERIC);
            _viewGradingGuideButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    viewGradingGuideButtonActionPerformed();
                }
            });
            this.add(_viewGradingGuideButton);
            
            this.add(Box.createVerticalStrut(5));
            
            _demoButton = new ActionButton("Demo", null, IconImage.APPLICATIONS_SYSTEM);
            _demoButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    demoButtonActionPerformed();
                }
            });
            this.add(_demoButton);
            
            this.add(Box.createVerticalStrut(15));
            
            _printButton = new ActionButton("Print Handin", "Print Handins", IconImage.PRINTER);
            _printButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    printButtonActionPerformed();
                }
            });
            this.add(_printButton);
            
            this.add(Box.createVerticalStrut(5));
            
            _runButton = new ActionButton("Run Handin", null, IconImage.GO_NEXT);
            _runButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    runButtonActionPerformed();
                }
            });
            this.add(_runButton);
            
            this.add(Box.createVerticalStrut(5));
            
            _openButton = new ActionButton("Open Handin", null, IconImage.DOCUMENT_OPEN);
            _openButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    openButtonActionPerformed();
                }
            });
            this.add(_openButton);
            
            this.add(Box.createVerticalStrut(5));
            
            _testButton = new ActionButton("Test Handin", null, IconImage.UTILITIES_SYSTEM_MONITOR);
            _testButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    testButtonActionPerformed();
                }
            });
            this.add(_testButton);
            
            this.add(Box.createVerticalStrut(5));
            
            _readmeButton = new ActionButton("View Readme", null, IconImage.TEXT_X_GENERIC);
            _readmeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    viewReadmeButtonActionPerformed();
                }
            });
            this.add(_readmeButton);
        
            this.add(Box.createVerticalStrut(10));
        }
        
        void notifySelectionChanged(Part part, Set<Group> selectedGroups,
            Set<Student> selectedStudentsNotInGroups)
        {
            _manualDistributorButton.setEnabled(part != null);
            _viewGradingGuideButton.setEnabled(part != null && part.hasGradingGuide());
            _demoButton.setEnabled(part != null && part.hasDemo());
            
            boolean showDigitalHandinOptions = part != null && part.getGradableEvent().hasDigitalHandins() &&
                                               !selectedGroups.isEmpty();
            _runButton.setVisible(showDigitalHandinOptions && selectedGroups.size() == 1);
            _openButton.setVisible(showDigitalHandinOptions && selectedGroups.size() == 1);
            _testButton.setVisible(showDigitalHandinOptions && selectedGroups.size() == 1);
            _readmeButton.setVisible(showDigitalHandinOptions && selectedGroups.size() == 1);
            _printButton.setVisible(showDigitalHandinOptions);
            
            _printButton.updateText(selectedGroups.size() < 2);
            
            if(part != null && part.getGradableEvent().hasDigitalHandins() && !selectedGroups.isEmpty())
            {
                //Disable the digital handin related buttons and then re-enable them if conditions allow
                _runButton.setEnabled(false);
                _openButton.setEnabled(false);
                _testButton.setEnabled(false);
                _readmeButton.setEnabled(false);
                _printButton.setEnabled(false);
                
                //Run, open, test, and view readme require exactly one selected group that has a digital handin on disk
                if(selectedGroups.size() == 1)
                {
                    Group group = selectedGroups.iterator().next();

                    boolean hasDigitalHandin = false;
                    try
                    {
                        hasDigitalHandin = part.getGradableEvent().hasDigitalHandin(group);
                    }
                    catch(IOException e)
                    {
                        new ErrorView(e, "Unable to determine if a digital handin exists\n" +
                                "Gradable Event: " + part.getGradableEvent().getAssignment().getName() + " - " +
                                                     part.getGradableEvent().getName() + "\n" +
                                "Group: " + group.getName());
                    }

                    if(hasDigitalHandin)
                    {
                        _runButton.setEnabled(part.hasRun());
                        _openButton.setEnabled(part.hasOpen());
                        _testButton.setEnabled(part.hasTest());

                        //Determine if a readme exists for the group in their digital handin
                        try
                        {
                           _readmeButton.setEnabled(part.hasReadme(group));
                        }
                        catch(ActionException e)
                        {
                            new ErrorView(e, "Unable to determine if a readme exists\n" +
                                "Part: " + part.getFullDisplayName() + "\n" +
                                "Group: " + group.getName());
                        }
                        catch(MissingHandinException e)
                        {
                            new ErrorView(e, "Unable to determine if a readme exists\n" +
                                "Part: " + part.getFullDisplayName() + "\n" +
                                "Group: " + group.getName());
                        }
                        catch(ArchiveException e)
                        {
                            new ErrorView(e, "Unable to determine if a readme exists\n" +
                                "Part: " + part.getFullDisplayName() + "\n" +
                                "Group: " + group.getName());
                        }
                    }
                }
                        
                //Print requires at least one group with a digital handin on disk
                if(part.hasPrint())
                {
                    boolean atLeastOneDigitalHandin = false;
                    for(Group group : selectedGroups)
                    {
                        try
                        {
                            atLeastOneDigitalHandin = part.getGradableEvent().hasDigitalHandin(group);
                            break;
                        }
                        catch(IOException e)
                        {
                            new ErrorView(e, "Unable to determine if a digital handin exists\n" +
                                    "Gradable Event: " + part.getGradableEvent().getAssignment().getName() +
                                               " - " +   part.getGradableEvent().getName() + "\n" +
                                    "Group: " + group.getName());
                        }
                    }
                    _printButton.setEnabled(atLeastOneDigitalHandin);
                }
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

    private void viewGradingGuideButtonActionPerformed()
    {
        try
        {
            _treeSelection.getPart().viewGradingGuide();
        }
        catch(FileNotFoundException e)
        {
            new ErrorView(e);
        }
    }

    private void demoButtonActionPerformed()
    {
        try
        {
            _treeSelection.getPart().demo();
        }
        catch(ActionException e)
        {
            new ErrorView(e);
        }
    }
    
    private void openButtonActionPerformed()
    {
        try
        {
            _treeSelection.getPart().open(Iterables.get(_selectedGroups, 0));
        }
        catch(ActionException e)
        {
            new ErrorView(e);
        }
        catch(MissingHandinException e)
        {
            notifyHandinMissing(e);
        }
    }

    private void runButtonActionPerformed()
    {
        try
        {
            _treeSelection.getPart().run(Iterables.get(_selectedGroups, 0));
        }
        catch(ActionException e)
        {
            new ErrorView(e);
        }
        catch(MissingHandinException e)
        {
            notifyHandinMissing(e);
        }
    }

    private void testButtonActionPerformed()
    {
        try
        {
            _treeSelection.getPart().test(Iterables.get(_selectedGroups, 0));
        }
        catch(ActionException e)
        {
            new ErrorView(e);
        }
        catch(MissingHandinException e)
        {
            notifyHandinMissing(e);
        }
    }

    private void printButtonActionPerformed()
    {
        try
        {
            Set<Group> groupsWithHandin = new HashSet<Group>();
            Set<Group> groupsWithoutHandin = new HashSet<Group>();
            for(Group group : _selectedGroups)
            {
                File handin = _treeSelection.getGradableEvent().getDigitalHandin(group);
                if(handin == null)
                {
                    groupsWithoutHandin.add(group);
                }
                else
                {
                    groupsWithHandin.add(group);
                }
            }
            
            boolean proceed = true;
            if(!groupsWithoutHandin.isEmpty())
            {
                String message = "The following groups do not have handins; thus, their handins cannot be printed:\n";
                for(Group group : groupsWithoutHandin)
                {
                    message += group.getName() + "\n";
                }
                proceed = ModalDialog.showConfirmation("Handins Not Present", message, "Print Anyway", "Cancel");
            }
            
            if(proceed)
            {
                try
                {
                    _treeSelection.getPart().print(groupsWithHandin);
                }
                catch(ActionException e)
                {
                    new ErrorView(e);
                }
            }
        }
        catch(IOException e)
        {
            new ErrorView(e);
        }
    }
    
    private void viewReadmeButtonActionPerformed()
    {
        try
        {
            _treeSelection.getPart().viewReadme(Iterables.get(_selectedGroups, 0));
        }
        catch(ActionException e)
        {
            new ErrorView(e);
        }
        catch(MissingHandinException e)
        {
            notifyHandinMissing(e);
        }
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
                new ErrorView(ex);
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
            new ErrorView(ex);
        }
    }

    private void notifyHandinMissing(MissingHandinException ex)
    {
        ModalDialog.showMessage("Digital Handin Missing",
                "The handin for " + ex.getGroup().getName() + " can no longer be found");
        ex.getPart().getGradableEvent().clearDigitalHandinCache();
        notifySelectionChanged(_treeSelection, _selectedGroups, _selectedStudentsNotInGroups);
    }
    
    private static class ActionButton extends JButton
    {
        private final String _singleText, _pluralText;
        
        ActionButton(String singleText, String pluralText, IconImage image)
        {
            _singleText = "<html><b>" + singleText + "</b></html>";
            _pluralText = "<html><b>" + pluralText + "</b></html>";
            
            this.setIcon(IconLoader.loadIcon(IconSize.s16x16, image));
            this.updateText(true);
            this.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            this.setIconTextGap(10);
        }
        
        final void updateText(boolean showSingle)
        {
            if(showSingle)
            {
                this.setText(_singleText);
            }
            else
            {
                this.setText(_pluralText);
            }
        }
    }
}