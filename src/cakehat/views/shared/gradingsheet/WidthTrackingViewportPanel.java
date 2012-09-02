package cakehat.views.shared.gradingsheet;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JComponent;
import support.ui.FixedWidthJPanel;

/**
 *
 * @author jak2
 */
class WidthTrackingViewportPanel<T extends JComponent & GradingSheet> extends FixedWidthJPanel implements GradingSheet
{   
    private final T _gradingSheet;
    
    WidthTrackingViewportPanel(T gradingSheet)
    {
        _gradingSheet = gradingSheet;
        
        this.setLayout(new BorderLayout(0, 0));
        this.add(gradingSheet, BorderLayout.CENTER);
    }

    @Override
    public double getEarned()
    {
        return _gradingSheet.getEarned();
    }

    @Override
    public double getOutOf()
    {
        return _gradingSheet.getOutOf();
    }

    @Override
    public void save()
    {
        _gradingSheet.save();
    }

    @Override
    public boolean hasUnsavedChanges()
    {
        return _gradingSheet.hasUnsavedChanges();
    }

    @Override
    public void addGradingSheetListener(GradingSheetListener listener)
    {
        _gradingSheet.addGradingSheetListener(listener);
    }

    @Override
    public void removeGradingSheetListener(GradingSheetListener listener)
    {
        _gradingSheet.removeGradingSheetListener(listener);
    }

    @Override
    public Component getAsComponent()
    {
        return this;
    }

    @Override
    public boolean containsComponent(Component component)
    {
        return _gradingSheet.containsComponent(component);
    }

    @Override
    public Component getComponentAfter(Component component)
    {
        return _gradingSheet.getComponentAfter(component);
    }

    @Override
    public Component getComponentBefore(Component component)
    {
        return _gradingSheet.getComponentBefore(component);
    }

    @Override
    public Component getFirstComponent()
    {
        return _gradingSheet.getFirstComponent();
    }

    @Override
    public Component getLastComponent()
    {
        return _gradingSheet.getLastComponent();
    }
}