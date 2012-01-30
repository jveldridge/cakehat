package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.views.shared.ErrorView;
import java.awt.FlowLayout;
import cakehat.assignment.Part;
import cakehat.newdatabase.Group;
import cakehat.newdatabase.PartGrade;
import cakehat.services.ServicesException;
import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JPanel;

/**
 *
 * @author jak2
 */
class DBPartPanel extends PartPanel
{   
    private double _earned = 0;
    
    DBPartPanel(Part part, Group group, boolean isAdmin)
    {   
        super(part, group, isAdmin);
        
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
                initNormalUI(Allocator.getDataServicesV5().getEarned(_group, _part));
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
        //Add a panel for the user to input the score
        JPanel scorePanel = new PreferredHeightPanel(new BorderLayout(0, 0), this.getBackground());
        addContent(scorePanel);
        
        //Text
        scorePanel.add(createContentLabel("Earned", false, false), BorderLayout.CENTER);
        
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
                Allocator.getDataServicesV5().setEarned(_group, _part, _earned, true);
                notifySavedSuccessfully();
            }
            catch(ServicesException ex)
            {
                new ErrorView(ex, "Unable to save grade for part " + _part.getFullDisplayName() + " for group " + _group);
            }
        }
    }
}