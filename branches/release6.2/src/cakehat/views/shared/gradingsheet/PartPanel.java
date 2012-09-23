package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.database.GroupGradingSheet;
import cakehat.database.GroupGradingSheet.GroupSectionComments;
import cakehat.database.GroupGradingSheet.GroupSubsectionEarned;
import cakehat.database.TA;
import cakehat.assignment.Part;
import cakehat.gradingsheet.GradingSheetDetail;
import cakehat.gradingsheet.GradingSheetSection;
import cakehat.gradingsheet.GradingSheetSubsection;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.plaf.BorderUIResource.CompoundBorderUIResource;
import javax.swing.plaf.basic.BasicBorders.MarginBorder;
import javax.swing.plaf.metal.MetalBorders.TextFieldBorder;
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
    private boolean _hasUnsavedChanges = false;
    
    PartPanel(Part part, Group group, boolean isAdmin, boolean showBorder) throws GradingSheetInitializationException
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
        initHeaderUI();
        addContent(Box.createVerticalStrut(10));
        initGradingSheetUI();
    }
    
    private GroupGradingSheet loadGradingSheet() throws GradingSheetInitializationException
    {
        try
        {
            return Allocator.getDataServices().getGroupGradingSheet(_part, _group);
        }
        catch(ServicesException ex)
        {
            throw new GradingSheetInitializationException("Unable to retrive group grading sheet.\n" +
                "Part: " + _part.getFullDisplayName() + "\n" +
                "Group: " + _group.getName(), ex, "Unable to retrieve grading sheet");
        }
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
                            notifyModificationOccurred();
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
        final Set<EarnedField> earnedFields = new HashSet<EarnedField>();
        final Set<JTextArea> commentFields = new HashSet<JTextArea>();
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

                        _hasUnsavedChanges = true;
                        notifyEarnedChanged(prevTotalEarned, currTotalEarned);
                        notifyModificationOccurred();
                    }
                });
                pointsPanel.add(earnedField);
                earnedFields.add(earnedField);
                _focusableComponents.add(earnedField);

                //Spacing
                pointsPanel.add(Box.createHorizontalStrut(5));

                //Out of
                pointsPanel.add(createDisabledField(subsection.getOutOf()));
            }
            
            //Comments
            addContent(Box.createVerticalStrut(5));
            final JTextArea commentArea = new JTextArea();
            _focusableComponents.add(commentArea);
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
                        _hasUnsavedChanges = true;
                        notifyModificationOccurred();
                    }
                    catch(BadLocationException e) { }
                }
            });
            commentFields.add(commentArea);
            
            //Override tab behavior to move focus instead of inserting the tab character
            commentArea.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                    ImmutableSet.of(KeyStroke.getKeyStroke("pressed TAB")));
            commentArea.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                    ImmutableSet.of(KeyStroke.getKeyStroke("shift pressed TAB")));
            
            //When the comment area gains focus, scroll it into view
            commentArea.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusGained(FocusEvent fe)
                {
                    if(commentArea.getParent() instanceof JComponent)
                    {
                        ((JComponent) commentArea.getParent()).scrollRectToVisible(commentArea.getBounds());
                    }
                }
            });
            
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
        
            addContent(Box.createVerticalStrut(10));
        }
        
        //Submit UI
        addContent(FormattedLabel.asSubheader("Submission"));
        JPanel submitPanel = new PreferredHeightJPanel(new BorderLayout(0, 0), this.getBackground());
        addContent(submitPanel);
        final JButton submitButton = new JButton();
        
        //When the submit button gains focus, scroll it into view
        submitButton.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent fe)
            {
                if(submitButton.getParent() instanceof JComponent)
                {
                    ((JComponent) submitButton.getParent()).scrollRectToVisible(submitButton.getBounds());
                }
            }
        });
        submitPanel.add(submitButton, BorderLayout.EAST);
        _focusableComponents.add(submitButton);
        final FormattedLabel submitLabel = FormattedLabel.asContent("").usePlainFont();
        submitPanel.add(submitLabel, BorderLayout.WEST);
        
        final Runnable updateSubmitUIRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                submitButton.setText(_groupSheet.isSubmitted() ? "Unsubmit" : "Submit");
                submitLabel.setText(_groupSheet.isSubmitted() ? "Unsubmit to modify grading sheet" :
                        "Submit when you are finished grading");
                for(EarnedField earnedField : earnedFields)
                {
                    earnedField.setEnabled(!_groupSheet.isSubmitted());
                }
                for (JTextArea commentField : commentFields) {
                    commentField.setEnabled(!_groupSheet.isSubmitted());
                    
                    if (commentField.isEnabled()) {
                        commentField.setBorder(BorderFactory.createEtchedBorder());
                    }
                    else {
                        //Creates and sets a border identical to the default Swing Metal border
                        MarginBorder innerBorder = new MarginBorder();
                        TextFieldBorder outerBorder = new TextFieldBorder();
                        CompoundBorderUIResource border = new CompoundBorderUIResource(innerBorder, outerBorder);
                        commentField.setBorder(border);
                    }
                }
            }
        };
        updateSubmitUIRunnable.run();
        submitButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                try
                {
                    Allocator.getDataServices().setGroupGradingSheetsSubmitted(ImmutableSet.of(_groupSheet),
                        !_groupSheet.isSubmitted());
                    updateSubmitUIRunnable.run();
                    notifySubmissionChanged(_part, _groupSheet.isSubmitted());
                }
                catch(ServicesException e)
                {
                    ErrorReporter.report("Unable to submit/unsubmit grading sheet\n" +
                            "Part: " + _part.getFullDisplayName() + "\n" +
                            "Group: " + _group, e);
                }
            }
        });
    }
    
    @Override
    List<Component> getFocusableComponents()
    {
        return _focusableComponents;
    }

    @Override
    Double getEarned()
    {
        return _groupSheet.getEarned();
    }

    @Override
    Double getOutOf()
    {
        return _groupSheet.getGradingSheet().getOutOf();
    }
    
    @Override
    public void save()
    {
        if(_hasUnsavedChanges)
        {
            try
            {
                Allocator.getDataServices().saveGroupGradingSheet(_groupSheet);
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