package support.ui;

import java.awt.BorderLayout;
import javax.swing.Box;
import javax.swing.JPanel;

/**
 *
 * @author jak2
 */
public class PaddingPanel extends JPanel
{       
    public PaddingPanel(JPanel panel, int pad)
    {
        super(new BorderLayout(0, 0));

        this.add(panel, BorderLayout.CENTER);
        this.add(Box.createVerticalStrut(pad), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(pad), BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(pad), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(pad), BorderLayout.EAST);
    }
}
