package cakehat.views.shared.gradingsheet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author jak2
 */
abstract class GradingSheetPanel extends PreferredHeightPanel implements GradingSheet
{
    private final JPanel _contentPanel;
    
    private boolean _hasUnsavedChanges = false;
    private final Set<GradingSheetListener> _listeners = new HashSet<GradingSheetListener>();
    
    GradingSheetPanel(Color backgroundColor)
    {
        super(backgroundColor);
        
        this.setBorder(BorderFactory.createEtchedBorder());
        
        this.setLayout(new BorderLayout(0, 0));
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        
        _contentPanel = new PreferredHeightPanel(backgroundColor);
        _contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.Y_AXIS));
        this.add(_contentPanel, BorderLayout.CENTER);
    }
    
    @Override
    public Component getAsComponent()
    {
        return this;
    }
    
    // Listener
    
    @Override
    public void addGradingSheetListener(GradingSheetListener listener)
    {
        _listeners.add(listener);
    }
    
    @Override
    public void removeGradingSheetListener(GradingSheetListener listener)
    {
        _listeners.remove(listener);
    }
        
    protected void notifyEarnedChanged(double prevEarned, double currEarned)
    {
        for(GradingSheetListener listener : _listeners)
        {
            listener.earnedChanged(prevEarned, currEarned);
        }
    }
    
    protected void notifyUnsavedChangeOccurred()
    {
        if(!_hasUnsavedChanges)
        {
            _hasUnsavedChanges = true;
            notifySaveChanged(_hasUnsavedChanges);
        }
    }
    
    protected void notifySavedSuccessfully()
    {
        if(_hasUnsavedChanges)
        {
            _hasUnsavedChanges = false;
            notifySaveChanged(_hasUnsavedChanges);
        }
    }

    private void notifySaveChanged(boolean hasUnsavedChanges)
    {
        for(GradingSheetListener listener : _listeners)
        {
            listener.saveChanged(hasUnsavedChanges);
        }
    }  
    
    @Override
    public boolean hasUnsavedChanges()
    {
        return _hasUnsavedChanges;
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
        JPanel errorPanel = new PreferredHeightPanel(new BorderLayout(0, 0), this.getBackground());
        JLabel errorLabel = createSubheaderLabel(message, true);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        errorLabel.setForeground(Color.RED);
        errorPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        errorPanel.add(errorLabel, BorderLayout.CENTER);
        errorPanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        
        addContent(errorPanel);
    }
    
    static JLabel createHeaderLabel(String text, boolean grayOut)    
    {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(new Font("Dialog", Font.BOLD, 22));
        label.setForeground(grayOut ? Color.GRAY : Color.BLACK);
        
        return label;
    }
    
    static JLabel createSubheaderLabel(String text, boolean grayOut) 
    {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        label.setForeground(grayOut ? Color.GRAY : Color.BLACK);
        
        return label;
    }
    
    static JLabel createContentLabel(String text, boolean grayOut, boolean bold)
    {
        JLabel label = new JLabel("<html>" + text + "</html>");
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setForeground(grayOut ? Color.GRAY : Color.BLACK);
        if(bold)
        {
            label.setFont(new Font("Dialog", Font.BOLD, 12));
        }
        
        return label;
    }
    
    static JComponent createDisabledField(Double value)
    {
        JTextField field = new JTextField(5);
        field.setText(value == null ? "" : Double.toString(value));
        field.setEnabled(false);
        field.setHorizontalAlignment(JTextField.CENTER);
        
        return field;
    }
}