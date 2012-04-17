package support.ui;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JScrollPane;

/**
 * A JScrollPanel that properly handles having a transparent background.
 *
 * @author jak2
 */
public class AlphaJScrollPane extends JScrollPane
{
    public AlphaJScrollPane() { }

    public AlphaJScrollPane(Component view)
    {
        super(view);
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