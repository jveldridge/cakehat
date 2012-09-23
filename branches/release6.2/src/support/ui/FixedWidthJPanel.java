package support.ui;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * 
 * @author jak2
 */
public class FixedWidthJPanel extends JPanel implements Scrollable
{
    @Override
    public Dimension getPreferredScrollableViewportSize()
    {
        return this.getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 1;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 10;
    }

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }
}