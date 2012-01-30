package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.assignment.Part;
import cakehat.gml.GMLParser;
import cakehat.gml.GMLWriter;
import cakehat.gml.GradingSheetException;
import cakehat.gml.InMemoryGML;
import cakehat.gml.InMemoryGML.Section;
import cakehat.gml.InMemoryGML.Subsection;
import cakehat.newdatabase.Group;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import support.ui.DocumentAdapter;

/**
 *
 * @author jak2
 */
class GMLPartPanel extends PartPanel
{
    private InMemoryGML _gml;
    private double _totalEarned = 0;
    
    GMLPartPanel(Part part, Group group, boolean isAdmin)
    {   
        super(part, group, isAdmin);
        
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
                new ErrorView("A permissions issue has arisen - unable to read GML file.\n" +
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
                    new ErrorView(ex, "GML file has been corrupted:" + gmlFile.getAbsolutePath());
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
                    
                    //TODO: Have a way to indicate this is user error
                    new ErrorView(ex, "GML template has invalid format: " + gmlFile.getAbsolutePath());
                }
            }
        }
    }
    
    private void initNormalUI()
    {
        for(final Section section : _gml.getSections())
        {
            addContent(createSubheaderLabel(section.getName(), false));
            
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
                scorePanel.add(createContentLabel(subsectionText, false, false), BorderLayout.CENTER);

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

                            notifyEarnedChanged(prevTotalEarned, _totalEarned);
                            notifyUnsavedChangeOccurred();
                        }
                    });
                    pointsPanel.add(earnedField);
                }

                //Spacing
                pointsPanel.add(Box.createHorizontalStrut(5));

                //Out of
                pointsPanel.add(createDisabledField(subsection.getOutOf()));
            }
            
            //Comments
            addContent(createContentLabel("Comments", false, true));
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
            commentArea.setEnabled(_group != null);
            JScrollPane commentScrollPanel = new JScrollPane(commentArea);
            commentScrollPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
            commentScrollPanel.setAlignmentX(LEFT_ALIGNMENT);
            addContent(commentScrollPanel);
            
            addContent(Box.createVerticalStrut(10));
        }
    }

    @Override
    public double getEarned()
    {
        return _totalEarned;
    }

    @Override
    public void save()
    {
        //If not a template and the gml file was parsed successfully
        if(_group != null && _gml != null)
        {
            if(this.hasUnsavedChanges())
            {
                File gmlFile = Allocator.getPathServices().getGroupGMLFile(_part, _group);
                try
                {
                    GMLWriter.write(_gml, gmlFile);
                    notifySavedSuccessfully();
                }
                catch(GradingSheetException e)
                {
                    new ErrorView(e, "Unable to save GML file\n" +
                            "Part: " + _part.getFullDisplayName() + "\n" +
                            "Group: " + _group.getName() + "\n" + 
                            "File: " + gmlFile.getAbsolutePath());
                }
            }
        }
    }
}