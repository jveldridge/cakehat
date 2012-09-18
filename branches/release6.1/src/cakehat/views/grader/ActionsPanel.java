package cakehat.views.grader;

import cakehat.database.Group;
import cakehat.assignment.Action;
import cakehat.assignment.Part;
import cakehat.assignment.TaskException;
import cakehat.logging.ErrorReporter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconSize;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
class ActionsPanel extends JPanel
{
    private final GraderView _graderView;
    private FormattedLabel _noActionsLabel;
    private final JPanel _nonHandinActionsPanel, _actionsSeparationPanel, _handinActionsPanel;
    
    ActionsPanel(GraderView graderView, PartAndGroupPanel partAndGroupPanel)
    {    
        _graderView = graderView;
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        this.add(FormattedLabel.asHeader("Actions"));
        this.add(Box.createVerticalStrut(5));
        
        _noActionsLabel = FormattedLabel.asContent(" - No Actions Available").usePlainFont();
        _noActionsLabel.setVisible(false);
        this.add(_noActionsLabel);
        
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        JScrollPane actionsScrollPane = new JScrollPane(actionsPanel);
        actionsScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        actionsScrollPane.setBorder(null);
        this.add(actionsScrollPane);
        
        _nonHandinActionsPanel = new JPanel();
        _nonHandinActionsPanel.setLayout(new BoxLayout(_nonHandinActionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.add(_nonHandinActionsPanel);
        
        _actionsSeparationPanel = new JPanel()
        {
            @Override
            public Dimension getMaximumSize()
            {
                Dimension size = getPreferredSize();
                size.width = Short.MAX_VALUE;

                return size;
            }
        };
        _actionsSeparationPanel.setLayout(new BoxLayout(_actionsSeparationPanel, BoxLayout.Y_AXIS));
        _actionsSeparationPanel.add(Box.createVerticalStrut(5));
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.LIGHT_GRAY);
        _actionsSeparationPanel.add(separator);
        _actionsSeparationPanel.add(Box.createVerticalStrut(5));
        _actionsSeparationPanel.setVisible(false);
        actionsPanel.add(_actionsSeparationPanel);
        
        _handinActionsPanel = new JPanel();
        _handinActionsPanel.setLayout(new BoxLayout(_handinActionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.add(_handinActionsPanel);
        
        //Notification of selection change
        partAndGroupPanel.addSelectionListener(new PartAndGroupPanel.PartAndGroupSelectionListener()
        {
            @Override
            public void selectionChanged(Part part, Set<Group> groups)
            {
                notifySelectionChanged(part, groups);
            }
        });
        this.notifySelectionChanged(partAndGroupPanel.getSelectedPart(), partAndGroupPanel.getSelectedGroups());
    }
    
    private void notifySelectionChanged(Part part, Set<Group> groups)
    {
        _nonHandinActionsPanel.removeAll();
        _handinActionsPanel.removeAll();
        
        if(part == null)
        {
            _actionsSeparationPanel.setVisible(false);
            _noActionsLabel.setVisible(true);
        }
        else
        {   
            boolean nonHandinActionDisplayed = false, handinActionDisplayed = false;
            for(Action action : part.getActions())
            {
                if(!action.requiresDigitalHandin())
                {
                    nonHandinActionDisplayed = true;
                    _nonHandinActionsPanel.add(createActionButton(action, groups));
                    _nonHandinActionsPanel.add(Box.createVerticalStrut(5));
                }
                else if(!groups.isEmpty())
                {
                    handinActionDisplayed = true;
                    _handinActionsPanel.add(Box.createVerticalStrut(5));
                    _handinActionsPanel.add(createActionButton(action, groups));
                }
            }
            
            _actionsSeparationPanel.setVisible(nonHandinActionDisplayed && handinActionDisplayed);
            _noActionsLabel.setVisible(!nonHandinActionDisplayed && !handinActionDisplayed);
        }
        
        //Force visual update to reflect these changes
        this.repaint();
        this.revalidate();
    }
    
    private JButton createActionButton(final Action action, final Set<Group> groups)
    {
        //Create button
        JButton button = new JButton();
        button.setText("<html><strong>" + action.getName() + "</strong></html>");
        button.setIcon(IconLoader.loadIcon(IconSize.s16x16, action.getIcon()));
        button.setMargin(new Insets(2, 10, 2, 10));
        button.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        button.setIconTextGap(10);
        
        //Determine if button is enabled
        boolean enable = false;
        try
        {
            enable = action.isTaskSupported(groups);
        }
        catch(TaskException ex)
        {
            ErrorReporter.report("Unable to determine if task is supported\n" + 
                    "Action: " + action.getName() + "\n" +
                    "Part: " + action.getPart().getFullDisplayName() + "\n" + 
                    "Groups: " + groups, ex);
        }
        button.setEnabled(enable);
        
        //If enable, wire it up to its action
        if(enable)
        {
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    try
                    {
                        action.performTask(_graderView, groups);
                    }
                    catch(TaskException ex)
                    {
                        ErrorReporter.report("Unable to perform task\n" + 
                            "Action: " + action.getName() + "\n" +
                            "Part: " + action.getPart().getFullDisplayName() + "\n" + 
                            "Groups: " + groups, ex);
                    }
                }
            });
        }
        
        return button;
    }
}