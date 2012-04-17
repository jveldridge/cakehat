package support.ui;

import java.awt.Graphics;
import java.awt.LayoutManager;
import javax.swing.JPanel;

/**
 * A JPanel that properly handles having a transparent background.
 *
 * @author jak2
 */
public class AlphaJPanel extends JPanel
{
    public AlphaJPanel() { }

    public AlphaJPanel(LayoutManager manager)
    {
        super(manager);
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        //If there is any transparency, set non-opaque and draw the entire background
        boolean hasTransparentBackground = (this.getBackground().getAlpha() != 255);
        this.setOpaque(!hasTransparentBackground);

        if(hasTransparentBackground)
        {
            g.setColor(this.getBackground());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

        super.paintComponent(g);
    }
}