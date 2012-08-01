package cakehat.views.grader;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
class NotifyStudentsPanel extends JPanel
{
    NotifyStudentsPanel()
    {
        this.setLayout(new BorderLayout(0, 0));
        
        this.add(FormattedLabel.asHeader("Not Yet Implemented").centerHorizontally(), BorderLayout.CENTER);
    }
}