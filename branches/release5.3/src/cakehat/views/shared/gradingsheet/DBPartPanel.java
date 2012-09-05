package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.views.shared.ErrorView;
import java.awt.FlowLayout;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.database.PartGrade;
import cakehat.services.ServicesException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
class DBPartPanel extends PartPanel
{   
    private final boolean _submitOnSave;
    private double _earned = 0;
    
    private final List<Component> _focusableComponents = new ArrayList<Component>();
    
    DBPartPanel(Part part, Group group, boolean isAdmin, boolean submitOnSave, boolean showBorder)
    {   
        super(part, group, isAdmin, showBorder);
        
        _submitOnSave = submitOnSave;
        
        this.init();
    }
    
    private void init()
    {
        //Template
        if(_group == null)
        {
            initNormalUI(null);
        }
        else
        {
            try
            {
                initNormalUI(Allocator.getDataServices().getEarned(_group, _part));
            }
            catch(ServicesException e)
            {
                new ErrorView(e, "Unable to retrieve database info for " + _part.getFullDisplayName() + " for group " +
                        _group);
                addErrorMessagePanel("Unable to retrieve needed grade data");
            }
        }
    }
    
    private void initNormalUI(PartGrade grade)
    {
        //Spacing between header UI and these components
        addContent(Box.createVerticalStrut(10));
        
        //Add a panel for the user to input the score
        JPanel scorePanel = new PreferredHeightPanel(new BorderLayout(0, 0), this.getBackground());
        addContent(scorePanel);
        
        //Text
        scorePanel.add(FormattedLabel.asContent("Earned").usePlainFont(), BorderLayout.CENTER);
        
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
            _earned = (grade == null || grade.getEarned() == null) ? 0 : grade.getEarned();
            EarnedField earnedField = new EarnedField(_earned, _part.getOutOf());
            earnedField.addEarnedListener(new EarnedField.EarnedListener()
            {
                @Override
                public void earnedChanged(double prevEarned, double currEarned)
                {
                    _earned = currEarned;
                    notifyEarnedChanged(prevEarned, currEarned);
                    notifyUnsavedChangeOccurred();
                }
            });
            pointsPanel.add(earnedField);
            _focusableComponents.add(earnedField);
        }
        
        //Spacing
        pointsPanel.add(Box.createHorizontalStrut(5));
        
        //Out of
        pointsPanel.add(createDisabledField(_part.getOutOf()));
    }
    
    @Override
    public double getEarned()
    {
        return _earned;
    }
    
    @Override
    public void save()
    {
        if(hasUnsavedChanges() && _group != null)
        {
            try
            {
                Allocator.getDataServices().setEarned(_group, _part, _earned, _submitOnSave);
                notifySavedSuccessfully();
            }
            catch(ServicesException ex)
            {
                new ErrorView(ex, "Unable to save grade for part " + _part.getFullDisplayName() + " for group " + _group);
            }
        }
    }

    @Override
    List<Component> getFocusableComponents()
    {
        return _focusableComponents;
    }
}