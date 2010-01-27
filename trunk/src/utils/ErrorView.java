package utils;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

/**
 * Displays an error that occurs during runtime.
 * 
 * TODO: Have error emailed to dedicated cakehat email address, not the Grades
 * TA, as the Grades TA of a given course isn't likely going to be able to fix
 * the problem.
 *
 * @author psastras
 */
public class ErrorView extends javax.swing.JFrame {

    /** Creates new form ErrorView */
    public ErrorView() {
        super("Error encountered");

        initComponents();
        commentsTextArea.setBackground(Color.LIGHT_GRAY);

        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public ErrorView(Exception e) {
        this();

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        try { stringWriter.close(); }
        //Well...if we encounter an exception while trying to show an exception
        //we are just screwed so forget about doing anything about it
        catch (IOException ex) { }

        errorTextArea.setText(stringWriter.toString());
        errorTextArea.setCaretPosition(0);
    }

    public ErrorView(Exception e, String customMessage) {
        this(e);
        errorTextArea.insert(customMessage + "\n", 0);
        errorTextArea.setCaretPosition(0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorTextArea = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        commentsTextArea = new javax.swing.JTextArea();
        ConfirmButton = new javax.swing.JButton();
        notifyGradesTACheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        setName("Form"); // NOI18N
        setResizable(false);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(ErrorView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        errorTextArea.setColumns(20);
        errorTextArea.setEditable(false);
        errorTextArea.setRows(5);
        errorTextArea.setEnabled(false);
        errorTextArea.setFocusable(false);
        errorTextArea.setName("errorTextArea"); // NOI18N
        jScrollPane1.setViewportView(errorTextArea);

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        commentsTextArea.setColumns(20);
        commentsTextArea.setRows(5);
        commentsTextArea.setEnabled(false);
        commentsTextArea.setName("commentsTextArea"); // NOI18N
        jScrollPane2.setViewportView(commentsTextArea);

        ConfirmButton.setMnemonic('K');
        ConfirmButton.setText(resourceMap.getString("ConfirmButton.text")); // NOI18N
        ConfirmButton.setName("ConfirmButton"); // NOI18N
        ConfirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ConfirmButtonActionPerformed(evt);
            }
        });

        notifyGradesTACheckBox.setText(resourceMap.getString("notifyGradesTACheckBox.text")); // NOI18N
        notifyGradesTACheckBox.setName("notifyGradesTACheckBox"); // NOI18N
        notifyGradesTACheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notifyGradesTACheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 631, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(notifyGradesTACheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                        .addComponent(ConfirmButton, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 631, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ConfirmButton)
                    .addComponent(notifyGradesTACheckBox))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void notifyGradesTACheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notifyGradesTACheckBoxActionPerformed
        if (notifyGradesTACheckBox.isSelected()) {
            commentsTextArea.setEnabled(true);
            commentsTextArea.setBackground(Color.WHITE);
        }
        else {
            commentsTextArea.setEnabled(false);
            commentsTextArea.setBackground(Color.LIGHT_GRAY);
        }
}//GEN-LAST:event_notifyGradesTACheckBoxActionPerformed

    private void ConfirmButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ConfirmButtonActionPerformed
        if (notifyGradesTACheckBox.isSelected()) {
            String from = Allocator.getGeneralUtilities().getUserLogin() + "@" +
                          Allocator.getCourseInfo().getEmailDomain();
            String[] to = new String[] { Allocator.getCourseInfo().getCakehatEmailAddress() };
            String subject = "Grading System Error";
            String body = "[This is an autogenerated message] \n\n" +
                          commentsTextArea.getText() +
                          "\n\n" + errorTextArea.getText();

            Allocator.getCourseInfo().getEmailAccount().sendMail(from, to, null, null, subject, body, null);

        }
        this.dispose();
}//GEN-LAST:event_ConfirmButtonActionPerformed

    /**
     * For testing purposes only
     * 
     * @param args
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ErrorView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ConfirmButton;
    private javax.swing.JTextArea commentsTextArea;
    private javax.swing.JTextArea errorTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JCheckBox notifyGradesTACheckBox;
    // End of variables declaration//GEN-END:variables
}
