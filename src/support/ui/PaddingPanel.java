package support.ui;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 *
 * @author jak2
 */
public class PaddingPanel extends JPanel
{       
    public PaddingPanel(JComponent component, int pad)
    {
        this(component, pad, pad, pad, pad);
    }
    
    public PaddingPanel(JComponent component, int northPad, int southPad, int westPad, int eastPad)
    {
        super(new BorderLayout(0, 0));

        this.add(component, BorderLayout.CENTER);
        this.add(Box.createVerticalStrut(northPad), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(southPad), BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(westPad), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(eastPad), BorderLayout.EAST);
    }
}