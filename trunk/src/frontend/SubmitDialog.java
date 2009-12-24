/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package frontend;

import javax.swing.*;

/**
 *
 * @author jeldridg
 */
public class SubmitDialog {

    JCheckBox _submitXMLcb, _printGRDcb, _notifyStudentscb;

    public SubmitDialog() {
        _submitXMLcb = new JCheckBox("Submit XML Files");
        _printGRDcb = new JCheckBox("Print GRD Files");
        _notifyStudentscb = new JCheckBox("Notify Students");
        
        _submitXMLcb.setSelected(true);
        _printGRDcb.setSelected(true);
        _notifyStudentscb.setSelected(true);

        JPanel panel = new JPanel();
        panel.add(_submitXMLcb);
        panel.add(_printGRDcb);
        panel.add(_notifyStudentscb);
        JOptionPane.showMessageDialog(null, panel);
    }

    public boolean submitChecked() {
        return _submitXMLcb.isSelected();
    }
    
    public boolean printChecked() {
        return _printGRDcb.isSelected();
    }
    
    public boolean notifyChecked() {
        return _notifyStudentscb.isSelected();
    }
}
