package support.ui;

import java.awt.Color;
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
        
        this.setAlignmentX(LEFT_ALIGNMENT);
    }
    
    public PreferredHeightJPanel(LayoutManager manager, Color background)
    {
        super(manager);
        
        this.setBackground(background);
        this.setAlignmentX(LEFT_ALIGNMENT);
    }

    public PreferredHeightJPanel(Color background)
    {   
        this.setBackground(background);
        this.setAlignmentX(LEFT_ALIGNMENT);
    }
    
    @Override
    public Dimension getMaximumSize()
    {
        Dimension size = getPreferredSize();
        size.width = Short.MAX_VALUE;

        return size;
    }
}