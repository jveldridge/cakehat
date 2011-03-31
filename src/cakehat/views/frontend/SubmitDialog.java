package cakehat.views.frontend;

import support.ui.GenericJCheckBox;
import cakehat.config.Assignment;
import cakehat.config.SubmitOptions;
import cakehat.database.Group;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.*;

/**
 * This class allows TAs to select various options for submitting grades.  Users can choose to
 * submit grades to the database, send an email to the graded students notifying them that their
 * their assignment has be graded, and print or email GRD files if the Assignment only has a
 * single DistributablePart.
 *
 * Users can also select which Groups they have graded to include on the notification email so that
 * failing Groups can be easily excluded.
 * 
 * @author jeldridg
 */

public class SubmitDialog {

    private JCheckBox _submitcb, _printGRDcb, _emailGRDcb, _notifyStudentscb;
    private JPanel _panel;
    private Collection<GenericJCheckBox<Group>> _groupBoxes;

    public SubmitDialog(Assignment asgn, Collection<Group> groups, SubmitOptions options) {
        _submitcb = new JCheckBox("Submit Grades");
        _printGRDcb = new JCheckBox("Print GRD Files");
        _emailGRDcb = new JCheckBox("Email GRD Files");
        _notifyStudentscb = new JCheckBox("Notify Students");
        
        //if notifyStudentscb is unselected, emailGRDcb must be as well
        _notifyStudentscb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!_notifyStudentscb.isSelected())
                    _emailGRDcb.setSelected(false);
            }
            
        });
        
        //if emailGRDcb is selected, notifyStudentscb must be as well
        _emailGRDcb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (_emailGRDcb.isSelected())
                    _notifyStudentscb.setSelected(true);
            }
            
        });
        
        _submitcb.setSelected(options.isSubmitDefaultEnabled());
        _printGRDcb.setSelected(options.isPrintGrdDefaultEnabled());
        _emailGRDcb.setSelected(options.isEmailGrdDefaultEnabled());
        _notifyStudentscb.setSelected(options.isNotifyDefaultEnabled());

        _panel = new JPanel();
        _panel.setLayout(new BorderLayout());

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(0,1));
        optionsPanel.add(new JLabel("<html><b>Select submit options: </b></html>"));
        optionsPanel.add(_submitcb);
        optionsPanel.add(_notifyStudentscb);

        //only allow printing/emailing GRDs if there is only 1 DistributablePart for the Assignment
        if (asgn.getDistributableParts().size() == 1) {
            optionsPanel.add(_emailGRDcb);
            optionsPanel.add(_printGRDcb);
        }
        else {
            optionsPanel.add(new JLabel("<html>For handins with multiple parts,<br/>" +
                                        "rubrics must be printed or emailed<br/>" +
                                        "from the admin interface.</html>"));
        }

        JPanel studentPanel = new JPanel();
        studentPanel.setLayout(new GridLayout(0,1));
        _groupBoxes = new LinkedList<GenericJCheckBox<Group>>();

        //create JCheckBox for each group
        for (Group group : groups) {
            _groupBoxes.add(new GenericJCheckBox<Group>(group));
        }

        studentPanel.add(new JLabel("<html><b>Select students to notify: </b><html>"));

        //default each each newly created JCheckBox to be selected and add to the panel
        for (JCheckBox box : _groupBoxes) {
            box.setSelected(true);
            studentPanel.add(box);
        }

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(studentPanel, BorderLayout.NORTH);
        JButton toggleAllButton = new JButton("Toggle All Students");           //(un-)checks all students
        toggleAllButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                for (JCheckBox box : SubmitDialog.this._groupBoxes) {
                    box.setSelected(!box.isSelected());
                }
            }

        });

        optionsPanel.add(toggleAllButton);

        _panel.add(optionsPanel, BorderLayout.WEST);
        _panel.add(bottomPanel, BorderLayout.EAST);
    }

    /**
     * Show a JOptionPane with the contents of _panel
     * @return whether the OK or CANCEL option was clicked
     */
    public int showDialog() {
        return JOptionPane.showConfirmDialog(null, _panel, "Submit Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
    }

    /**
     * @return whether the _submitcb checkbox is selected
     */
    public boolean submitChecked() {
        return _submitcb.isSelected();
    }
    
    /**
     * @return whether the _printGRDcb checkbox is selected
     */
    public boolean printChecked() {
        return _printGRDcb.isSelected();
    }
    
    /**
     * @return whether the _emailGRDcb checkbox is selected
     */
    public boolean emailChecked() {
        return _emailGRDcb.isSelected();
    }

    /**
     * @return whether the _notifyStudentscb checkbox is selected
     */
    public boolean notifyChecked() {
        return _notifyStudentscb.isSelected();
    }

    /**
     * @return a vector with the logins of the students whose checkboxes were checked
     * (meaning they should be included on the outgoing notification email)
     */
    public Collection<Group> getSelectedGroups() {
        Collection<Group> selected = new LinkedList<Group>();
        for (GenericJCheckBox<Group> box : _groupBoxes) {
            if (box.isSelected()) {
                selected.add(box.getItem());
            }
        }

        return selected;
    }
}
