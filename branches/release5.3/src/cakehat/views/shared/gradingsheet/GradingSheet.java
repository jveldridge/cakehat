package cakehat.views.shared.gradingsheet;

import java.awt.Component;

/**
 *
 * @author jak2
 */
public interface GradingSheet
{
    public static interface GradingSheetListener
    {
        public void earnedChanged(double prevEarned, double currEarned);
        
        public void saveChanged(boolean hasUnsavedChanges);
    }
    
    public double getEarned();
    
    public double getOutOf();
    
    public void save();
    
    public boolean hasUnsavedChanges();
    
    public void addGradingSheetListener(GradingSheetListener listener);
    
    public void removeGradingSheetListener(GradingSheetListener listener);
    
    public boolean containsComponent(Component component);
    
    public Component getComponentAfter(Component component);
    
    public Component getComponentBefore(Component component);
    
    public Component getFirstComponent();

    public Component getLastComponent();
    
    public Component getAsComponent();
}