package cakehat.views.shared.gradingsheet;

import cakehat.database.Group;
import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import support.ui.FixedWidthJPanel;
import support.ui.FormattedLabel;
import support.ui.PreferredHeightJPanel;

/**
 *
 * @author jak2
 */
public abstract class GradingSheetPanel extends FixedWidthJPanel
{
    public static GradingSheetPanel getPanel(Assignment asgn, Group group, boolean isAdmin)
    {
        return new AssignmentPanel(asgn, group, isAdmin, false);
    }
    
    public static GradingSheetPanel getPanel(GradableEvent ge, Group group, boolean isAdmin)
    {
        return new GradableEventPanel(ge, ge.getParts(), group, isAdmin, false);
    }
    
    public static GradingSheetPanel getPanel(Part part, Group group, boolean isAdmin, boolean partiallyShowOtherParts)
    {
        GradingSheetPanel panel;
        if(partiallyShowOtherParts)
        {
            panel = new GradableEventPanel(part.getGradableEvent(), ImmutableSet.of(part), group, isAdmin, false);
        }
        else
        {
            panel = new PartPanel(part, group, isAdmin, false);
        }
        
        return panel;
    }
    
    private final JPanel _contentPanel;
    private final Set<GradingSheetListener> _listeners = new HashSet<GradingSheetListener>();
    
    GradingSheetPanel(Color backgroundColor, boolean showBorder)
    {
        this.setBackground(backgroundColor);
        
        if(showBorder)
        {
            this.setBorder(BorderFactory.createEtchedBorder());
        }
        
        this.setLayout(new BorderLayout(0, 0));
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        
        _contentPanel = new PreferredHeightJPanel(backgroundColor);
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
        this.add(_contentPanel, BorderLayout.CENTER);
    }
    
    abstract List<Component> getFocusableComponents();
    
    public Component getFirstComponent()
    {
        Component first = null;
        if(!getFocusableComponents().isEmpty())
        {
            first = getFocusableComponents().get(0);
        }
        
        return first;
    }

    public Component getLastComponent()
    {
        Component last = null;
        if(!getFocusableComponents().isEmpty())
        {
            last = getFocusableComponents().get(getFocusableComponents().size() - 1);
        }
        
        return last;
    }

    public boolean containsComponent(Component component)
    {
        return getFocusableComponents().contains(component);
    }

    public Component getComponentAfter(Component component)
    {   
        int index = getFocusableComponents().indexOf(component);
        
        Component next;
        //If the component is not contained or is the last component, the next component is undefined
        if(index == -1 || index == getFocusableComponents().size() - 1)
        {
            next = null;
        }
        else
        {
            next = getFocusableComponents().get(index + 1);
        }
        
        return next;
    }

    public Component getComponentBefore(Component component)
    {
        int index = getFocusableComponents().indexOf(component);
        
        Component prev;
        //If the component is not contained or is the first component, the previous component is undefined
        if(index == -1 || index == 0)
        {
            prev = null;
        }
        else
        {
            prev = getFocusableComponents().get(index - 1);
        }
        
        return prev;
    }
    
    // Listener
    
    public static interface GradingSheetListener
    {
        public void earnedChanged(Double prevEarned, Double currEarned);
        
        public void modificationOccurred();
        
        public void submissionChanged(Part part, boolean submitted);
    }
        
    public void addGradingSheetListener(GradingSheetListener listener)
    {
        _listeners.add(listener);
    }
    
    public void removeGradingSheetListener(GradingSheetListener listener)
    {
        _listeners.remove(listener);
    }
        
    protected void notifyEarnedChanged(Double prevEarned, Double currEarned)
    {
        for(GradingSheetListener listener : _listeners)
        {
            listener.earnedChanged(prevEarned, currEarned);
        }
    }
    
    protected void notifyModificationOccurred()
    {
        for(GradingSheetListener listener : _listeners)
        {
            listener.modificationOccurred();
        }
    }
    
    protected void notifySubmissionChanged(Part part, boolean submitted)
    {
        for(GradingSheetListener listener : _listeners)
        {
            listener.submissionChanged(part, submitted);
        }
    }
    
    // Helper methods
    
    protected void addContent(Component comp)
    {
        if(comp instanceof JComponent)
        {
            ((JComponent) comp).setAlignmentX(LEFT_ALIGNMENT);
        }
        
        _contentPanel.add(comp);
    }
    
    protected void addErrorMessagePanel(String message)
    {
        JPanel errorPanel = new PreferredHeightJPanel(new BorderLayout(0, 0), this.getBackground());
        JLabel errorLabel = FormattedLabel.asSubheader(message).centerHorizontally().showAsErrorMessage();
        errorPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        errorPanel.add(errorLabel, BorderLayout.CENTER);
        errorPanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        
        addContent(errorPanel);
    }
    
    static JComponent createDisabledField(Double value)
    {
        JTextField field = new JTextField(5);
        field.setText(value == null ? "" : Double.toString(value));
        field.setEnabled(false);
        field.setHorizontalAlignment(JTextField.CENTER);
        
        return field;
    }
    
    abstract Double getEarned();
    
    abstract Double getOutOf();
    
    public abstract void save();
    
    @Override
    public Dimension getMaximumSize()
    {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;

        return size;
    }
}