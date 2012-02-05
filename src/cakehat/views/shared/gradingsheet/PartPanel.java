package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.database.TA;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Box;
import javax.swing.JPanel;
import support.ui.GenericJComboBox;
import support.ui.PartialDescriptionProvider;
import support.ui.SelectionListener;
import support.ui.SelectionListener.SelectionAction;

/**
 *
 * @author jak2
 */
abstract class PartPanel extends GradingSheetPanel
{
    protected final Part _part;
    protected final Group _group;
    protected final boolean _isAdmin;
    
    PartPanel(Part part, Group group, boolean isAdmin)
    {   
        super(Color.WHITE);
        
        _part = part;
        _group = group;
        _isAdmin = isAdmin;
        
        this.init();
    }
    
    static PartPanel getPartPanel(Part part, Group group, boolean isAdmin, boolean submitOnSave)
    {
        PartPanel panel;
        if(part.hasSpecifiedGMLTemplate())
        {
            panel = new GMLPartPanel(part, group, isAdmin, submitOnSave);
        }
        else
        {
            panel = new DBPartPanel(part, group, isAdmin, submitOnSave);
        }
        
        return panel;
    }
    
    private void init()
    {
        addContent(createHeaderLabel(_part.getName(), false));
        
        //Template
        if(_group == null)
        {
            initHeaderUI(null, null);
        }
        else
        {
            try
            {
                TA assignedTo = Allocator.getDataServices().getGrader(_part, _group);
                Set<TA> tas = _isAdmin ? Allocator.getDataServices().getTAs() : null;
                    
                initHeaderUI(assignedTo, tas);
            }
            catch(ServicesException e)
            {
                new ErrorView(e, "Unable to retrieve TA info from the database for " + _part.getFullDisplayName() +
                        " for group " + _group.getName());
                addErrorMessagePanel("Unable to retrieve needed TA data");
            }
        }
    }
    
    private void initHeaderUI(TA assignedTo, Set<TA> tas)
    {
        if(_group == null)
        {
            addContent(createSubheaderLabel("Template", true));
        }
        else
        {
            if(_isAdmin)
            {
                JPanel assignedPanel = new PreferredHeightPanel(new FlowLayout(FlowLayout.LEFT, 0, 0), this.getBackground());
                addContent(assignedPanel);

                assignedPanel.add(createSubheaderLabel("Grader:", true));

                assignedPanel.add(Box.createHorizontalStrut(5));

                tas = new HashSet<TA>(tas);
                tas.add(null);
                final GenericJComboBox<TA> assignedComboBox = new GenericJComboBox<TA>(tas, new PartialDescriptionProvider<TA>()
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
                assignedComboBox.addSelectionListener(new SelectionListener<TA>()
                {
                    @Override
                    public void selectionPerformed(TA currValue, TA newValue, SelectionAction action)
                    {   
                        try
                        {
                            Allocator.getDataServices().setGrader(_part, _group, newValue);
                        }
                        catch(ServicesException ex)
                        {
                            action.cancel();
                            new ErrorView(ex, "Unable to assign new grader");
                        }
                    }
                });
                assignedComboBox.setGenericSelectedItem(assignedTo);
                assignedPanel.add(assignedComboBox);
            }
            else
            {
                String taText;
                if(assignedTo == null)
                {
                    taText = "Unassigned";
                }
                else
                {
                    taText = "Grader: " + assignedTo.getName() + " (" + assignedTo.getLogin() + ")";
                }

                addContent(createSubheaderLabel(taText, true));
            }
        }
        
        addContent(Box.createVerticalStrut(10));
    }
    
    @Override
    public double getOutOf()
    {
        return _part.getOutOf();
    }
}