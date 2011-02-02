package gradesystem;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The about box displays very basic information about cakehat.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class GradeSystemAboutBox extends JFrame
{
    private static final int PANEL_HEIGHT = 200;
    private static final Dimension IMAGE_PANEL_SIZE = new Dimension(160, PANEL_HEIGHT);
    private static final Dimension INFO_PANEL_SIZE = new Dimension(270, PANEL_HEIGHT);
    private static final Dimension PANEL_SIZE = new Dimension(IMAGE_PANEL_SIZE.width + INFO_PANEL_SIZE.width, PANEL_HEIGHT);

    private static GradeSystemAboutBox _currentlyDisplayedBox;

    private GradeSystemAboutBox()
    {
        super("[cakehat] about");

        // Overall
        JPanel panel = new JPanel(new BorderLayout());
        panel.setSize(PANEL_SIZE);
        panel.setPreferredSize(PANEL_SIZE);        
        this.add(panel);

        // Icon
        Icon cakehatIcon = new ImageIcon(getClass().getResource("/gradesystem/resources/cakehat.png"));
        int vOffset = (IMAGE_PANEL_SIZE.height - cakehatIcon.getIconHeight()) / 2;
        int hOffset = (IMAGE_PANEL_SIZE.width - cakehatIcon.getIconWidth()) / 2;
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, hOffset, vOffset));
        imagePanel.setPreferredSize(IMAGE_PANEL_SIZE);
        JLabel imageLabel = new JLabel(cakehatIcon);
        imagePanel.add(imageLabel);
        panel.add(imagePanel, BorderLayout.WEST);

        // Info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoPanel.add(new JLabel("<html><h3>Version 3.0</h3></html>"));
        infoPanel.add(new JLabel("<html><h4>A Brown CS grading system started by<br/>the CS015 TA Staff in 2009.</h4></html>"));
        infoPanel.add(new JLabel("<html><h3>Creators</h3>Jonathan Eldridge, Joshua Kaplan, Stephen<br/>Poletto, Paul Sastrasinh, & Alex Unger</html>"));
        infoPanel.add(new JLabel("<html><br/><a href=\"http://cakehat.googlecode.com/\">http://cakehat.googlecode.com</a></html>"));
        infoPanel.setPreferredSize(INFO_PANEL_SIZE);
        panel.add(infoPanel, BorderLayout.EAST);

        // Display
        this.pack();
        this.setResizable(false);

        // Close operation
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                _currentlyDisplayedBox = null;
                GradeSystemAboutBox.this.dispose();
            }
        });
    }

    /**
     * Displays the about box. If the about box is already open it will be
     * brought to the front and positioned relatove to <code>relativeTo</code>.
     *
     * @param relativeTo The component the about box will be displayed relative
     * to. null may be passed in, in which case the about box is centered on the
     * screen.
     */
    public static void displayRelativeTo(Component relativeTo)
    {
        if(_currentlyDisplayedBox != null)
        {
            _currentlyDisplayedBox.toFront();
        }
        else
        {
            _currentlyDisplayedBox = new GradeSystemAboutBox();
        }
        _currentlyDisplayedBox.setLocationRelativeTo(relativeTo);
        _currentlyDisplayedBox.setVisible(true);
    }
}