package cakehat.views.shared.gradingsheet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 *
 * @author jak2
 */
class PreferredHeightPanel extends JPanel
{
    PreferredHeightPanel(LayoutManager manager)
    {
        super(manager);
        
        this.setAlignmentX(LEFT_ALIGNMENT);
    }
    
    PreferredHeightPanel(LayoutManager manager, Color background)
    {
        super(manager);
        
        this.setBackground(background);
        this.setAlignmentX(LEFT_ALIGNMENT);
    }

    PreferredHeightPanel(Color background)
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