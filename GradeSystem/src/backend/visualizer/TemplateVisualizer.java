package backend.visualizer;

/**
 * Class used to visualize XML stencils created for
 * each assignment.
 * 
 * @author spoletto
 */

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import frontend.grader.rubric.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import utils.Constants;

public class TemplateVisualizer extends JFrame
{

    public TemplateVisualizer(String asgn) {
            super("Visualizer for " + asgn);
            this.setVisible(true);

            //Get grading rubric
            final String XMLFilePath = Constants.TEMPLATE_GRADE_SHEET_DIR + asgn + "/" + Constants.TEMPLATE_GRADE_SHEET_FILENAME;
            final Rubric rubric = RubricManager.processXML(XMLFilePath);

            //Configure basic properties
            this.setLayout(new BorderLayout());

            VisualPanel mp = new VisualPanel(rubric);
            VizScrollPane mainPane = new VizScrollPane(mp);
            mainPane.getVerticalScrollBar().setUnitIncrement(16);
            this.add(mainPane, BorderLayout.CENTER);
            
            this.pack();

            this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

            //Open up a dialog on window close to save rubric data
            this.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    TemplateVisualizer.this.dispose();
                }
            });


        }
    }

