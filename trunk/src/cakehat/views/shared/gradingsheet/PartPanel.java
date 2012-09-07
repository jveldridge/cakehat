package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.database.GroupGradingSheet;
import cakehat.database.GroupGradingSheet.GroupSectionComments;
import cakehat.database.GroupGradingSheet.GroupSubsectionEarned;
import cakehat.database.TA;
import cakehat.database.assignment.Part;
import cakehat.gradingsheet.GradingSheetDetail;
import cakehat.gradingsheet.GradingSheetSection;
import cakehat.gradingsheet.GradingSheetSubsection;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import support.ui.DocumentAdapter;
import support.ui.FormattedLabel;
import support.ui.GenericJComboBox;
import support.ui.PaddingPanel;
import support.ui.PartialDescriptionProvider;
import support.ui.PreferredHeightJPanel;
import support.ui.SelectionListener;
import support.ui.SelectionListener.SelectionAction;
import support.utils.NullMath;

/**
 *
 * @author jak2
 */
class PartPanel extends GradingSheetPanel
{
    private final Part _part;
    private final Group _group;
    private final boolean _isAdmin;
    
    private final List<Component> _focusableComponents = new ArrayList<Component>();
    private final GroupGradingSheet _groupSheet;
    
    PartPanel(Part part, Group group, boolean isAdmin, boolean showBorder)
    {   
        super(Color.WHITE, showBorder);
        
        if(part == null)
        {
            throw new NullPointerException("part may not be null");
        }
        if(group == null)
        {
            throw new NullPointerException("group may not be null");
        }
        
        _part = part;
        _group = group;
        _isAdmin = isAdmin;
        
        _groupSheet = loadGradingSheet();
        if(_groupSheet != null)
        {
            initHeaderUI();
            addContent(Box.createVerticalStrut(10));
            initGradingSheetUI();
        }
    }
    
    private GroupGradingSheet loadGradingSheet()
    {
        GroupGradingSheet sheet = null;
        try
        {
            sheet = Allocator.getDataServices().getGroupGradingSheet(_part, _group);
        }
        catch(ServicesException ex)
        {
            addErrorMessagePanel("Unable to retrieve grading sheet");
            ErrorReporter.report("Unable to retrive group grading sheet.\n" +
                "Part: " + _part.getFullDisplayName() + "\n" +
                "Group: " + _group.getName(), ex);
        }
        
        return sheet;
    }
    
    
    private void initHeaderUI()
    {
        addContent(FormattedLabel.asHeader(_part.getName()));
        
        if(_isAdmin)
        {
            JPanel assignedPanel = new PreferredHeightJPanel(new FlowLayout(FlowLayout.LEFT, 0, 0),
                    this.getBackground());
            addContent(assignedPanel);

            assignedPanel.add(FormattedLabel.asSubheader("Grader:").grayOut());
            assignedPanel.add(Box.createHorizontalStrut(5));

            Set<TA> tas = new HashSet<TA>(Allocator.getDataServices().getTAs());
            tas.add(null);
            GenericJComboBox<TA> assignedComboBox = new GenericJComboBox<TA>(tas, new PartialDescriptionProvider<TA>()
            {
                @Override
                public String getDisplayText(TA ta)
                {
                    String text;
                    if(ta == null)
                    {
                        text = "Unassigned";
                    }
                    else
                    {
                        text = ta.getName() + " (" + ta.getLogin() + ")";
                    }

                    return text;
                };
            });
            assignedComboBox.setPreferredSize(new Dimension(200, 20));
            assignedComboBox.setGenericSelectedItem(_groupSheet.getAssignedTo());
            assignedComboBox.addSelectionListener(new SelectionListener<TA>()
            {
                @Override
                public void selectionPerformed(TA currValue, TA newValue, SelectionAction action)
                {   
                    try
                    {
                        if(Allocator.getGradingServices().isOkToDistribute(_group, newValue))
                        {
                            _groupSheet.setAssignedTo(newValue);
                            Allocator.getDataServices().saveGroupGradingSheet(_groupSheet);
                        }
                        else
                        {
                            action.cancel();
                        }
                    }
                    catch(ServicesException ex)
                    {
                        _groupSheet.setAssignedTo(currValue);
                        action.cancel();
                        ErrorReporter.report("Unable to assign new grader", ex);
                    }
                }
            });
            assignedPanel.add(assignedComboBox);
        }
        else
        {
            TA assignedTo = _groupSheet.getAssignedTo();
            String taText;
            if(assignedTo == null)
            {
                taText = "Unassigned";
            }
            else
            {
                taText = "Grader: " + assignedTo.getName() + " (" + assignedTo.getLogin() + ")";
            }

            addContent(FormattedLabel.asSubheader(taText).grayOut());
        }
    }
    
    private void initGradingSheetUI()
    {
        final JTextField totalEarnedField = new JTextField(5);
        boolean hasSubsection = false;
        
        for(final GradingSheetSection section : _groupSheet.getGradingSheet().getSections())
        {
            addContent(FormattedLabel.asSubheader(section.getName()));
            
            for(final GradingSheetSubsection subsection : section.getSubsections())
            {
                hasSubsection = true;
                
                //Add a panel for the user to input the score
                JPanel scorePanel = new PreferredHeightJPanel(new BorderLayout(0, 0), this.getBackground());
                addContent(scorePanel);

                //Text
                StringBuilder subsectionText = new StringBuilder();
                subsectionText.append("<p style='margin-top:0px; margin-bottom:0px; margin-left:5px'>");
                subsectionText.append(subsection.getText());
                subsectionText.append("</p>");
                if(!subsection.getDetails().isEmpty())
                {
                    subsectionText.append("<ul style='list-style-type:square; ");
                    subsectionText.append("margin-top:0px; margin-bottom:0px; margin-left:18px'>");
                    for(GradingSheetDetail detail : subsection.getDetails())
                    {
                        subsectionText.append("<li>");
                        subsectionText.append(detail.getText());
                        subsectionText.append("</li>");
                    }
                    subsectionText.append("</ul>");
                }
                scorePanel.add(FormattedLabel.asContent(subsectionText.toString()).usePlainFont(), BorderLayout.CENTER);

                //Points panel
                JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                pointsPanel.setBackground(this.getBackground());
                scorePanel.add(pointsPanel, BorderLayout.EAST);

                //Spacing
                pointsPanel.add(Box.createHorizontalStrut(5));

                //Earned
                GroupSubsectionEarned subsectionEarned = _groupSheet.getEarnedPoints().get(subsection);
                Double earned = subsectionEarned == null ? null : subsectionEarned.getEarned();
                EarnedField earnedField = new EarnedField(earned, subsection.getOutOf());
                earnedField.addEarnedListener(new EarnedField.EarnedListener()
                {
                    @Override
                    public void earnedChanged(Double prevEarned, Double currEarned)
                    {
                        Double prevTotalEarned = _groupSheet.getEarned();
                        _groupSheet.setEarnedPoints(subsection, currEarned);
                        Double currTotalEarned = _groupSheet.getEarned();

                        totalEarnedField.setText(NullMath.toString(currTotalEarned));

                        notifyEarnedChanged(prevTotalEarned, currTotalEarned);
                        notifyUnsavedChangeOccurred();
                    }
                });
                pointsPanel.add(earnedField);
                _focusableComponents.add(earnedField);

                //Spacing
                pointsPanel.add(Box.createHorizontalStrut(5));

                //Out of
                pointsPanel.add(createDisabledField(subsection.getOutOf()));
            }
            
            //Comments
            addContent(Box.createVerticalStrut(5));
            JTextArea commentArea = new JTextArea();
            addContent(new PaddingPanel(commentArea, 0, 0, 5, 122, this.getBackground()));
            commentArea.setRows(2);
            commentArea.setLineWrap(true);
            commentArea.setWrapStyleWord(true);
            commentArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Comments"));
            GroupSectionComments comments = _groupSheet.getComments().get(section);
            if(comments != null && comments.getComments() != null)
            {
                commentArea.setText(comments.getComments());
            }
            commentArea.getDocument().addDocumentListener(new DocumentAdapter()
            {
                @Override
                public void modificationOccurred(DocumentEvent de)
                {
                    try
                    {
                        _groupSheet.setComments(section, de.getDocument().getText(0, de.getDocument().getLength()));                
                        notifyUnsavedChangeOccurred();
                    }
                    catch(BadLocationException e) { }
                }
            });
            //Remap tab to insert 4 spaces
            commentArea.getActionMap().put("tab", new AbstractAction("tab")
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    try
                    {
                        //Replaces tab with 4 spaces and fires the key events so that the user sees the spaces happen
                        Robot robot = new Robot();
                        for(int i = 0; i < 4; i++)
                        {
                            robot.keyPress(KeyEvent.VK_SPACE);
                            robot.keyRelease(KeyEvent.VK_SPACE);
                        }
                    }
                    catch(AWTException ex) {}
                }
             });
            commentArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false), "tab");
            
            addContent(Box.createVerticalStrut(10));
        }
        
        if(hasSubsection)
        {
            //Total
            addContent(FormattedLabel.asSubheader("Part Total"));
            JPanel totalPanel = new PreferredHeightJPanel(new BorderLayout(0, 0), this.getBackground());
            addContent(totalPanel);
            totalPanel.add(FormattedLabel.asContent("Total").usePlainFont(), BorderLayout.CENTER);
            JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pointsPanel.setBackground(this.getBackground());
            totalPanel.add(pointsPanel, BorderLayout.EAST);
            pointsPanel.add(Box.createHorizontalStrut(5));

            //Total earned
            totalEarnedField.setEnabled(false);
            totalEarnedField.setHorizontalAlignment(JTextField.CENTER); 
            totalEarnedField.setText(NullMath.toString(_groupSheet.getEarned()));
            pointsPanel.add(totalEarnedField);

            pointsPanel.add(Box.createHorizontalStrut(5));

            //Total out of
            pointsPanel.add(createDisabledField(_groupSheet.getGradingSheet().getOutOf()));
        }
    }
    
    @Override
    List<Component> getFocusableComponents()
    {
        return _focusableComponents;
    }

    @Override
    Double getEarned()
    {
        Double earned = null;
        if(_groupSheet != null)
        {
            earned = _groupSheet.getEarned();
        }
        
        return earned;
    }

    @Override
    Double getOutOf()
    {
        Double outOf = null;
        if(_groupSheet != null)
        {
            outOf = _groupSheet.getGradingSheet().getOutOf();
        }
        
        return outOf;
    }

    @Override
    public void save()
    {
        if(this.hasUnsavedChanges())
        {
            if(_groupSheet != null)
            {
                try
                {
                    Allocator.getDataServices().saveGroupGradingSheet(_groupSheet);
                    notifySavedSuccessfully();
                }
                catch(ServicesException e)
                {
                    ErrorReporter.report("Unable to save grading sheet\n" +
                            "Part: " + _part.getFullDisplayName() + "\n" +
                            "Group: " + _group.getName(), e);
                }
            }
        }
    }
}