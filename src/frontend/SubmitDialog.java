package frontend;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.*;

/**
 * This class allows TAs to select various options for submitting grades.  Users can choose to
 * submit XML files (which will copy them to the submitted directory), print .GRD files, and send
 * an email to the graded students notifying them that their assignment has be graded.
 *
 * Users can also select which students they have graded to include on the notification email so that
 * failing students can be easily excluded.
 * 
 * @author jeldridg
 */

public class SubmitDialog{

    JCheckBox _submitXMLcb, _printGRDcb, _notifyStudentscb;
    JPanel _panel;
    Vector<JCheckBox> _studentBoxes;

    public SubmitDialog(JList studentList) {
        _submitXMLcb = new JCheckBox("Submit XML Files");
        _printGRDcb = new JCheckBox("Print GRD Files");
        _notifyStudentscb = new JCheckBox("Notify Students");
        
        _submitXMLcb.setSelected(true);
        _printGRDcb.setSelected(true);
        _notifyStudentscb.setSelected(true);

        _panel = new JPanel();
        _panel.setLayout(new BorderLayout());

        JPanel optionsPanel = new JPanel();
        optionsPanel.add(new JLabel("<html><b>Select submit options: </b></html>"));
        optionsPanel.add(_submitXMLcb);
        optionsPanel.add(_printGRDcb);
        optionsPanel.add(_notifyStudentscb);

        JPanel studentPanel = new JPanel();
        _studentBoxes = new Vector<JCheckBox>();

        Vector<String> studentLogins = new Vector<String>();
        int size = studentList.getModel().getSize();
        for (int i = 0; i < size; i++) {
            studentLogins.add((String) studentList.getModel().getElementAt(i));
        }

        //create JCheckBox for each student
        for (String studentLogin : studentLogins) {
            _studentBoxes.add(new JCheckBox(studentLogin));
        }

        studentPanel.add(new JLabel("<html><b>Select students to email: </b><html>"));

        //default each each newly created JCheckBox to be selected and add to the panel
        for (JCheckBox box : _studentBoxes) {
            box.setSelected(true);
            studentPanel.add(box);
        }

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(studentPanel, BorderLayout.NORTH);
        JButton toggleAllButton = new JButton("Toggle All Students");           //(un-)checks all students
        toggleAllButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                for (JCheckBox box : SubmitDialog.this._studentBoxes) {
                    box.setSelected(!box.isSelected());
                }
            }

        });

        bottomPanel.add(toggleAllButton);

        _panel.add(optionsPanel, BorderLayout.NORTH);
        _panel.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Show a JOptionPane with the contents of _panel
     * @return whether the OK or CANCEL option was clicked
     */
    public int showDialog() {
        return JOptionPane.showConfirmDialog(null, _panel, "Submit Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
    }

    /**
     * @return whether the _submitXMLcb checkbox is selected
     */
    public boolean submitChecked() {
        return _submitXMLcb.isSelected();
    }

    /**
     * @return whether the _printGRDcb checkbox is selected
     */
    public boolean printChecked() {
        return _printGRDcb.isSelected();
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
    public Vector<String> getSelectedStudents() {
        Vector<String> selected = new Vector<String>();
        for (JCheckBox box : _studentBoxes) {
            if (box.isSelected()) {
                selected.add(box.getText());
            }
        }
        return selected;
    }
}
