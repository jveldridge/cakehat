package support.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * A panel that pads its content.
 *
 * @author jak2
 */
public class PaddingPanel extends JPanel
{
    public static final int DEFAULT_PAD = 10;
    
    protected PaddingPanel()
    {
        this(null, DEFAULT_PAD, DEFAULT_PAD, DEFAULT_PAD, DEFAULT_PAD, null);
    }
    
    protected PaddingPanel(int pad, Color background)
    {
        this(null, pad, pad, pad, pad, background);
    }
        
    protected PaddingPanel(int pad, JComponent component)
    {
        this(component, pad, pad, pad, pad, null);
    }
    
    public PaddingPanel(JComponent component)
    {
        this(component, DEFAULT_PAD, DEFAULT_PAD, DEFAULT_PAD, DEFAULT_PAD, null);
    }
    
    public PaddingPanel(JComponent component, int pad, Color background)
    {
        this(component, pad, pad, pad, pad, background);
    }
    
    public PaddingPanel(JComponent component, int northPad, int southPad, int westPad, int eastPad, Color background)
    {
        super(new BorderLayout(0, 0));
        
        if(background != null)
        {
            this.setBackground(background);
        }
        
        if(component != null)
        {
            this.add(component, BorderLayout.CENTER);
        }
        this.add(Box.createVerticalStrut(northPad), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(southPad), BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(westPad), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(eastPad), BorderLayout.EAST);
    }
    
    public void addContentComponent(JComponent component)
    {
        this.add(component, BorderLayout.CENTER);
    }
}