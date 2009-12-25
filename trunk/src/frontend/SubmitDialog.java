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
    JPanel _panel;

    public SubmitDialog() {
        _submitXMLcb = new JCheckBox("Submit XML Files");
        _printGRDcb = new JCheckBox("Print GRD Files");
        _notifyStudentscb = new JCheckBox("Notify Students");
        
        _submitXMLcb.setSelected(true);
        _printGRDcb.setSelected(true);
        _notifyStudentscb.setSelected(true);

        _panel = new JPanel();
        _panel.add(_submitXMLcb);
        _panel.add(_printGRDcb);
        _panel.add(_notifyStudentscb);
    }

    public int showDialog() {
        return JOptionPane.showConfirmDialog(null, _panel, "Submit Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
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
