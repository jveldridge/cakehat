package cakehat.views.shared.gradingsheet;

import cakehat.assignment.Part;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 *
 * @author jak2
 */
class PartIndicatorPanel extends PreferredHeightPanel
{
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    
    PartIndicatorPanel(Part part)
    {
        super(new BorderLayout(0, 0), BACKGROUND_COLOR);
        
        this.setBorder(BorderFactory.createEtchedBorder());
        
        this.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
        this.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
        this.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        
        JPanel contentPanel = new PreferredHeightPanel(BACKGROUND_COLOR);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        this.add(contentPanel, BorderLayout.CENTER);
        
        contentPanel.add(GradingSheetPanel.createHeaderLabel(part.getName(), true));
    }
}