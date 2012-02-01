package cakehat.views.shared.gradingsheet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 *
 * @author jak2
 */
class GradingSheetFrame<T extends JComponent & GradingSheet> extends JFrame implements GradingSheet
{
    private final T _gradingSheet;
    
    GradingSheetFrame(T gradingSheet, String title)
    {
        super(title);
        
        _gradingSheet = gradingSheet;
        
        this.setLayout(new BorderLayout(0, 0));
        
        JScrollPane scrollPane = new JScrollPane(new WidthTrackingViewportPanel<T>(gradingSheet));
        scrollPane.setBorder(null);
        this.add(scrollPane, BorderLayout.CENTER);
        
        this.setMinimumSize(new Dimension(360, 400));
        this.setPreferredSize(new Dimension(500, 640));
        this.pack();
        this.setResizable(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    @Override
    public Component getAsComponent()
    {
        return this;
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
}