package cakehat.views.shared.gradingsheet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 *
 * @author jak2
 */
class WidthTrackingViewportPanel<T extends JComponent & GradingSheet> extends JPanel implements GradingSheet, Scrollable
{   
    private final T _gradingSheet;
    
    WidthTrackingViewportPanel(T gradingSheet)
    {
        _gradingSheet = gradingSheet;
        
        this.setLayout(new BorderLayout(0, 0));
        this.add(gradingSheet, BorderLayout.CENTER);
    }
    
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        return this.getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) 
    {
        return 1;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) 
    {
        return 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
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