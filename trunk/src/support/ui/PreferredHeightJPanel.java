package support.ui;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * A JPanel's with a maximum height equal to its preferred height.
 *
 * @author jak2
 */
public class PreferredHeightJPanel extends JPanel
{
    public PreferredHeightJPanel() { }
    
    public PreferredHeightJPanel(LayoutManager layoutManager)
    {
        super(layoutManager);
    }
    
    @Override
    public Dimension getMaximumSize()
    {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;

        return size;
    }   
}