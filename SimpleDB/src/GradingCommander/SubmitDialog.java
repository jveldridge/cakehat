/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package GradingCommander;

import javax.swing.*;

/**
 *
 * @author jeldridg
 */
public class SubmitDialog {

    public SubmitDialog() {
        JCheckBox submitXMLcb = new JCheckBox("Submit XML Files");
        JCheckBox printGRDcb = new JCheckBox("Print GRD Files");
        JCheckBox notifyStudentscb = new JCheckBox("Notify Students");
        JPanel panel = new JPanel();
        panel.add(submitXMLcb);
        panel.add(printGRDcb);
        panel.add(notifyStudentscb);
        JOptionPane.showMessageDialog(null, panel);
    }

    //public void

  public static void main(String[] args)
  {
    JCheckBox cb = new JCheckBox("Check Box");
    JRadioButton rb = new JRadioButton("Radio Button");
    JTextField textfield = new JTextField(10);
    JPanel panel = new JPanel();
    JPanel panel2 = new JPanel();
    panel2.add(textfield);
    panel2.add(cb);
    panel2.add(rb);
    panel.add(panel2);
    JOptionPane.showMessageDialog(null, panel);

    System.out.println("Check Box checked: " + cb.isSelected());
    System.out.println("Radio Button checked: " + rb.isSelected());
    System.out.println("Text Field contains: " + textfield.getText());
  }
}
