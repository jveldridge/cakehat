/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EmailGUI.java
 *
 * Created on Sep 15, 2009, 9:50:50 PM
 */
package utils;

import com.inet.jortho.SpellChecker;
import java.util.Arrays;
import javax.imageio.ImageIO;

/**
 *
 * @author psastras
 */
public class EmailView extends javax.swing.JFrame {

    /** Creates new form EmailGUI */
    public EmailView() {
        initComponents();
        this.setTitle(Utils.getUserLogin() + "@cs.brown.edu - Send Email");
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/internet-mail.png")));
        } catch (Exception e) {
        }
        fromBox.setText(Utils.getUserLogin() + "@cs.brown.edu");
        this.setLocationRelativeTo(null);
        SpellChecker.register(bodyText);
        SpellChecker.registerDictionaries(getClass().getResource("/gradesystem/resources/dictionary_en.ortho"), "en");
    }

    public EmailView(String[] to, String[] cc, String[] bcc, String subject, String body) {
        initComponents();
        this.setTitle(Utils.getUserLogin() + "@cs.brown.edu - Send Email");
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/internet-mail.png")));
        } catch (Exception e) {
        }
        toBox.setText(Arrays.toString(to).replace("[", "").replace("]", ""));
        System.out.println("set to with: " + toBox.getText());
        ccBox.setText(Arrays.toString(cc).replace("[", "").replace("]", ""));
        bccBox.setText(Arrays.toString(bcc).replace("[", "").replace("]", ""));
        subjectBox.setText(subject);
        toBox.setText(Utils.getUserLogin() + Constants.EMAIL_DOMAIN);
        System.out.println("later, to box is: " + toBox.getText());
        fromBox.setText(Utils.getUserLogin() + Constants.EMAIL_DOMAIN);
        ccBox.setText(Constants.GRADES_TA+"@"+Constants.EMAIL_DOMAIN + ", " + Constants.GRADES_HTA+"@"+Constants.EMAIL_DOMAIN);
        bodyText.setText(body);
        this.setLocationRelativeTo(null);
        SpellChecker.register(bodyText);
        SpellChecker.registerDictionaries(getClass().getResource("/gradesystem/resources/dictionary_en.ortho"), "en");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        subjectBox = new javax.swing.JTextField();
        bccBox = new javax.swing.JTextField();
        ccBox = new javax.swing.JTextField();
        toBox = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        bodyText = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        fromBox = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        jDialog1.setMinimumSize(new java.awt.Dimension(300, 100));
        jDialog1.setModal(true);
        jDialog1.setResizable(false);

        jButton2.setText("Close");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel6.setText("Mail sent.");

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jDialog1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addContainerGap(257, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
                .addContainerGap(238, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jDialog1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jButton1.setMnemonic('S');
        jButton1.setText("Send");
        jButton1.setFocusable(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setText("To");

        jLabel2.setText("Cc");

        jLabel3.setText("Subject");

        jLabel4.setText("Bcc");

        subjectBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subjectBoxActionPerformed(evt);
            }
        });

        toBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toBoxActionPerformed(evt);
            }
        });

        bodyText.setColumns(20);
        bodyText.setFont(new java.awt.Font("SansSerif", 0, 12));
        bodyText.setRows(5);
        bodyText.setMargin(new java.awt.Insets(10, 10, 10, 10));
        jScrollPane1.setViewportView(bodyText);

        jLabel5.setText("From");

        fromBox.setEnabled(false);

        jButton3.setMnemonic('C');
        jButton3.setText("Cancel");
        jButton3.setFocusable(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuItem1.setText("Quit");
        jMenu1.add(jMenuItem1);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1033, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bccBox, javax.swing.GroupLayout.DEFAULT_SIZE, 884, Short.MAX_VALUE)
                            .addComponent(subjectBox, javax.swing.GroupLayout.DEFAULT_SIZE, 884, Short.MAX_VALUE)
                            .addComponent(ccBox, javax.swing.GroupLayout.DEFAULT_SIZE, 884, Short.MAX_VALUE)
                            .addComponent(toBox, javax.swing.GroupLayout.DEFAULT_SIZE, 884, Short.MAX_VALUE)
                            .addComponent(fromBox, javax.swing.GroupLayout.DEFAULT_SIZE, 884, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fromBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(toBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ccBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(bccBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(subjectBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 393, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void toBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toBoxActionPerformed
}//GEN-LAST:event_toBoxActionPerformed

    private void subjectBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subjectBoxActionPerformed
}//GEN-LAST:event_subjectBoxActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Utils.sendMail(toBox.getText().replace(" ", "").split("(,|;)"), ccBox.getText().replace(" ", "").split("(,|;)"), bccBox.getText().replace(" ", "").split("(,|;)"), subjectBox.getText(), bodyText.getText(), new String[0]);
        jDialog1.setLocationRelativeTo(null);
        jDialog1.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jDialog1.setVisible(false);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    public void setSubject(String s) {
        subjectBox.setText(s);
    }

    public void setTo(String s) {
        toBox.setText(s);
    }

    public void setCc(String s) {
        ccBox.setText(s);
    }

    public void setBcc(String s) {
        bccBox.setText(s);
    }

    public void setBody(String s) {
        bodyText.setText(s);
    }

    public void setFrom(String s) {
        fromBox.setText(s);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new EmailView().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField bccBox;
    private javax.swing.JTextArea bodyText;
    private javax.swing.JTextField ccBox;
    private javax.swing.JTextField fromBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField subjectBox;
    private javax.swing.JTextField toBox;
    // End of variables declaration//GEN-END:variables
}
