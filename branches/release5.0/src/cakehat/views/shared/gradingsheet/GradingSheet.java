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
    
    public Component getAsComponent();
}