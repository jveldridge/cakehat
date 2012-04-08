package cakehat.views.shared.gradingsheet;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

/**
 *
 * @author jak2
 */
class GradingSheetFrame<T extends JComponent & GradingSheet> extends JDialog implements GradingSheet
{
    private final T _gradingSheet;
    
    GradingSheetFrame(Window owner, T gradingSheet, String title)
    {
        super(owner, title, ModalityType.MODELESS);
        
        _gradingSheet = gradingSheet;
        
        this.setLayout(new BorderLayout(0, 0));
        
        JScrollPane scrollPane = new JScrollPane(new WidthTrackingViewportPanel<T>(gradingSheet));
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        this.add(scrollPane, BorderLayout.CENTER);
        
        initFocusTraversalPolicy();
        
        this.setMinimumSize(new Dimension(360, 400));
        this.setPreferredSize(new Dimension(500, 640));
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setResizable(true);
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
    
    private void initFocusTraversalPolicy()
    {
        // Add Enter as forward traversal key
        Set<AWTKeyStroke> forwardKeys = this.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>(forwardKeys);
        newForwardKeys.add(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newForwardKeys);

        this.setFocusTraversalPolicy(new FocusTraversalPolicy()
        {
            @Override
            public Component getComponentAfter(Container cntnr, Component cmpnt)
            {
                Component next = GradingSheetFrame.this.getComponentAfter(cmpnt);
                if(next == null)
                {
                    next = GradingSheetFrame.this.getFirstComponent();
                }
                
                return next;
            }

            @Override
            public Component getComponentBefore(Container cntnr, Component cmpnt)
            {
                Component prev = GradingSheetFrame.this.getComponentBefore(cmpnt);
                if(prev == null)
                {
                    prev = GradingSheetFrame.this.getLastComponent();
                }
                
                return prev;
            }

            @Override
            public Component getFirstComponent(Container cntnr)
            {
                return GradingSheetFrame.this.getFirstComponent();
            }

            @Override
            public Component getLastComponent(Container cntnr)
            {
                return GradingSheetFrame.this.getLastComponent();
            }

            @Override
            public Component getDefaultComponent(Container cntnr)
            {
                return GradingSheetFrame.this.getFirstComponent();
            }
        });
    }
}