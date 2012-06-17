package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.database.assignment.Part;
import cakehat.gml.GMLParser;
import cakehat.gml.GradingSheetException;
import cakehat.gml.InMemoryGML;
import cakehat.gml.InMemoryGML.Section;
import cakehat.gml.InMemoryGML.Subsection;
import cakehat.database.Group;
import cakehat.gml.GMLWriter;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import support.ui.DocumentAdapter;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
class GMLPartPanel extends PartPanel
{
    private final boolean _submitOnSave;
    private InMemoryGML _gml;
    private double _totalEarned = 0;
    
    private final List<Component> _focusableComponents = new ArrayList<Component>();
    
    GMLPartPanel(Part part, Group group, boolean isAdmin, boolean submitOnSave, boolean showBorder)
    {   
        super(part, group, isAdmin, showBorder);
        
        _submitOnSave = submitOnSave;
        
        this.init();
    }
    
    private void init()
    {
        //Group exists and a GML file for that group exists, attempt to parse group's GML file
        if(_group != null && Allocator.getPathServices().getGroupGMLFile(_part, _group).exists())
        {
            //If a GML file has been created for this group but cannot be read - cakehat (not user) error
            File gmlFile = Allocator.getPathServices().getGroupGMLFile(_part, _group);
            if(!gmlFile.canRead())
            {
                addErrorMessagePanel("Unable to read GML file");
                ErrorReporter.report("A permissions issue has arisen - unable to read GML file.\n" +
                    "Part: " + _part.getFullDisplayName() + "\n" +
                    "Group: " + _group.getName() + "\n" +
                    "Path: " + gmlFile.getAbsolutePath());
            }
            else
            {
                try
                {
                    _gml = GMLParser.parse(gmlFile, _part, _group);
                    initNormalUI();
                }
                catch(GradingSheetException ex)
                {
                    addErrorMessagePanel("GML file has been corrupted");
                    ErrorReporter.report("GML file has been corrupted: " + gmlFile.getAbsolutePath(), ex);
                }
            }
        }
        //Template or Group exists but no GML for that group exists yet, attempt to parse GML template
        else
        {
            File gmlFile = _part.getGMLTemplate();
            if(!gmlFile.exists())
            {
                addErrorMessagePanel("GML template does not exist");
            }
            else if(!gmlFile.canRead())
            {
                addErrorMessagePanel("GML template cannot be read");
            }
            else
            {
                try
                {
                    _gml = GMLParser.parse(gmlFile, _part, _group);
                    initNormalUI();
                }
                catch(GradingSheetException ex)
                {
                    addErrorMessagePanel("GML template has invalid format");
                }
            }
        }
    }
    
    private void initNormalUI()
    {
        //Spacing between header UI and these components
        addContent(Box.createVerticalStrut(10));
        
        final JTextField totalEarnedField = new JTextField(5);
        totalEarnedField.setEnabled(false);
        totalEarnedField.setHorizontalAlignment(JTextField.CENTER); 
        
        for(final Section section : _gml.getSections())
        {
            addContent(FormattedLabel.asSubheader(section.getName()));
            
            for(final Subsection subsection : section.getSubsections())
            {
                //Add a panel for the user to input the score
                JPanel scorePanel = new PreferredHeightPanel(new BorderLayout(0, 0), this.getBackground());
                addContent(scorePanel);

                //Text
                String subsectionText = subsection.getName();
                if(!subsection.getDetails().isEmpty())
                {
                    subsectionText += "<ul>";
                    for(String detail : subsection.getDetails())
                    {
                        subsectionText += "<li>" + detail + "</li>";
                    }
                    subsectionText += "</ul>";
                }
                scorePanel.add(FormattedLabel.asContent(subsectionText).usePlainFont(), BorderLayout.CENTER);

                //Points panel
                JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                pointsPanel.setBackground(this.getBackground());
                scorePanel.add(pointsPanel, BorderLayout.EAST);

                //Spacing
                pointsPanel.add(Box.createHorizontalStrut(5));

                //Earned
                if(_group == null)
                {
                    pointsPanel.add(createDisabledField(null));
                }
                else
                {                
                    _totalEarned += subsection.getEarned();
                    EarnedField earnedField = new EarnedField(subsection.getEarned(), subsection.getOutOf());
                    earnedField.addEarnedListener(new EarnedField.EarnedListener()
                    {
                        @Override
                        public void earnedChanged(double prevEarned, double currEarned)
                        {
                            double prevTotalEarned = _totalEarned;

                            subsection.setEarned(currEarned);
                            _totalEarned -= prevEarned;
                            _totalEarned += currEarned;

                            totalEarnedField.setText(Double.toString(_totalEarned));
                            
                            notifyEarnedChanged(prevTotalEarned, _totalEarned);
                            notifyUnsavedChangeOccurred();
                        }
                    });
                    pointsPanel.add(earnedField);
                    
                    _focusableComponents.add(earnedField);
                }

                //Spacing
                pointsPanel.add(Box.createHorizontalStrut(5));

                //Out of
                pointsPanel.add(createDisabledField(subsection.getOutOf()));
            }
            
            //Comments
            addContent(FormattedLabel.asContent("Comments"));
            JTextArea commentArea = new JTextArea();
            commentArea.setRows(4);
            commentArea.setLineWrap(true);
            commentArea.setWrapStyleWord(true);
            if(section.getComment() != null)
            {
                commentArea.setText(section.getComment());
            }
            commentArea.getDocument().addDocumentListener(new DocumentAdapter()
            {
                @Override
                public void modificationOccurred(DocumentEvent de)
                {
                    try
                    {   
                        String text = de.getDocument().getText(0, de.getDocument().getLength());
                        if(text.isEmpty())
                        {
                            section.setComment(null);
                        }
                        else
                        {
                            section.setComment(text);
                        }
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
                    try {
                        // replaces tab with 4 spaces and fires the key events so that the user sees
                        // the spaces happen
                        Robot robot = new Robot();
                        for (int i = 0; i < 4; i++) {
                            robot.keyPress(KeyEvent.VK_SPACE);
                            robot.keyRelease(KeyEvent.VK_SPACE);
                        }
                    } catch (AWTException ex) {}
                                
                }
             });
            commentArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0, false), "tab");
            commentArea.setEnabled(_group != null);
            JScrollPane commentScrollPanel = new JScrollPane(commentArea);
            commentScrollPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
            commentScrollPanel.setAlignmentX(LEFT_ALIGNMENT);
            addContent(commentScrollPanel);
            
            addContent(Box.createVerticalStrut(10));
        }
        
        //Total
        addContent(FormattedLabel.asSubheader("Part Total"));
        JPanel totalPanel = new PreferredHeightPanel(new BorderLayout(0, 0), this.getBackground());
        addContent(totalPanel);
        totalPanel.add(FormattedLabel.asContent("Total").usePlainFont(), BorderLayout.CENTER);
        JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pointsPanel.setBackground(this.getBackground());
        totalPanel.add(pointsPanel, BorderLayout.EAST);
        pointsPanel.add(Box.createHorizontalStrut(5));
        
        //Total earned
        totalEarnedField.setText(Double.toString(_totalEarned));
        pointsPanel.add(totalEarnedField);
        
        pointsPanel.add(Box.createHorizontalStrut(5));
        
        //Total out of
        pointsPanel.add(createDisabledField(_part.getOutOf()));
    }

    @Override
    public double getEarned()
    {
        return _totalEarned;
    }

    @Override
    public void save()
    {
        //If not a template, the gml file was parsed successfully, and has unsaved changes
        if(_group != null && _gml != null && this.hasUnsavedChanges())
        {
            File gmlFile = Allocator.getPathServices().getGroupGMLFile(_part, _group);
            try
            {
                GMLWriter.write(_gml, gmlFile);
                Allocator.getDataServices().setEarned(_group, _part, _totalEarned, _submitOnSave);
                notifySavedSuccessfully();
            }
            catch(GradingSheetException e)
            {
                ErrorReporter.report("Unable to save GML file\n" +
                        "Part: " + _part.getFullDisplayName() + "\n" +
                        "Group: " + _group.getName() + "\n" + 
                        "File: " + gmlFile.getAbsolutePath(), e);
            }
            catch(ServicesException e)
            {
                ErrorReporter.report("Unable to record changes in database\n" +
                        "Part: " + _part.getFullDisplayName() + "\n" +
                        "Group: " + _group.getName(), e);
            }
        }
    }

    @Override
    List<Component> getFocusableComponents()
    {
        return _focusableComponents;
    }
}