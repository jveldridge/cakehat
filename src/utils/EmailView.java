package utils;

import com.inet.jortho.SpellChecker;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 *
 * @author psastras
 * @author jeldridg
 */
public class EmailView extends javax.swing.JFrame {

    private Map<String,String> _attachments;
    
    /** Creates new form EmailGUI */
    public EmailView() {
        initComponents();
        this.setTitle(Allocator.getGeneralUtilities().getUserLogin() + "@cs.brown.edu - Send Email");
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/internet-mail.png")));
        } catch (Exception e) {
        }
        fromBox.setText(Allocator.getGeneralUtilities().getUserLogin() + "@cs.brown.edu");
        this.setLocationRelativeTo(null);
        SpellChecker.register(bodyText);
        SpellChecker.registerDictionaries(getClass().getResource("/gradesystem/resources/dictionary_en.ortho"), "en");
    }

    public EmailView(Collection<String> students, Collection<String> toNotify, String subject, String body, Map<String,String> attachments) {
        initComponents();
        this.setTitle(Allocator.getGeneralUtilities().getUserLogin() + "@cs.brown.edu - Send Email");
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/internet-mail.png")));
        } catch (Exception e) {
        }
        studentsBox.setText(Arrays.toString(students.toArray()).replace("[", "").replace("]", ""));
        notifyBox.setText(Arrays.toString(toNotify.toArray()).replace("[", "").replace("]", ""));
        subjectBox.setText(subject);
        fromBox.setText(Allocator.getGeneralUtilities().getUserLogin() + "@" + Allocator.getCourseInfo().getEmailDomain());
        bodyText.setText(body);
        
        if (attachments == null) {
            attachmentMessage.setText("No attachments will be sent with this message.");
        }
        else {
            attachmentMessage.setText("Each student's .GRD file will be sent to that student as an attachment.");
        }
            
        _attachments = attachments;
        this.setVisible(true);
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

        sentDialog = new javax.swing.JDialog();
        dialogCloseButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        sendButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        subjectBox = new javax.swing.JTextField();
        notifyBox = new javax.swing.JTextField();
        studentsBox = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        bodyText = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        fromBox = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        attachmentMessage = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        sentDialog.setMinimumSize(new java.awt.Dimension(300, 100));
        sentDialog.setModal(true);
        sentDialog.setResizable(false);

        dialogCloseButton.setText("Close");
        dialogCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialogCloseButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Mail sent.");

        javax.swing.GroupLayout sentDialogLayout = new javax.swing.GroupLayout(sentDialog.getContentPane());
        sentDialog.getContentPane().setLayout(sentDialogLayout);
        sentDialogLayout.setHorizontalGroup(
            sentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sentDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addContainerGap(257, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sentDialogLayout.createSequentialGroup()
                .addContainerGap(239, Short.MAX_VALUE)
                .addComponent(dialogCloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        sentDialogLayout.setVerticalGroup(
            sentDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sentDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dialogCloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        sendButton.setFont(new java.awt.Font("Lucida Grande", 0, 14)); // NOI18N
        sendButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gradesystem/resources/icons/32x32/mail-forward.png"))); // NOI18N
        sendButton.setMnemonic('S');
        sendButton.setText("Send");
        sendButton.setFocusable(false);
        sendButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Students:");

        jLabel2.setText("Notify:");

        jLabel3.setText("Subject:");

        bodyText.setColumns(20);
        bodyText.setFont(new java.awt.Font("SansSerif", 0, 12));
        bodyText.setRows(5);
        bodyText.setMargin(new java.awt.Insets(10, 10, 10, 10));
        jScrollPane1.setViewportView(bodyText);

        jLabel5.setText("From:");

        fromBox.setEnabled(false);

        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gradesystem/resources/icons/16x16/mail-mark-not-junk.png"))); // NOI18N
        cancelButton.setMnemonic('C');
        cancelButton.setText("Cancel");
        cancelButton.setFocusable(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Attach:");

        attachmentMessage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gradesystem/resources/icons/16x16/mail-attachment.png"))); // NOI18N

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
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(sendButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(7, 7, 7)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel5)))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(subjectBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 875, Short.MAX_VALUE)
                            .addComponent(notifyBox, javax.swing.GroupLayout.DEFAULT_SIZE, 875, Short.MAX_VALUE)
                            .addComponent(studentsBox, javax.swing.GroupLayout.DEFAULT_SIZE, 875, Short.MAX_VALUE)
                            .addComponent(fromBox, javax.swing.GroupLayout.DEFAULT_SIZE, 875, Short.MAX_VALUE)
                            .addComponent(attachmentMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 367, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
                            .addComponent(studentsBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(notifyBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(subjectBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sendButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(attachmentMessage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        //get the date and time of submission for notification message
        final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        String now = sdf.format(cal.getTime());
        
        //send message to each student
        String[] students = studentsBox.getText().replace(" ", "").split("(,|;)");
        for (String student : students) {
            String attachmentPath = null;
            if (_attachments != null) {
                attachmentPath = _attachments.get(student.split("@")[0]);
            }
            Allocator.getCourseInfo().getEmailAccount().sendMail(fromBox.getText(),                     //from
                                                                  new String[] {student},               //to
                                                                  null,                                 //cc
                                                                  null,                                 //bcc
                                                                  subjectBox.getText(),
                                                                  bodyText.getText(), 
                                                                      new String[] {attachmentPath});   //attachment paths
        }
        
        //send notification of message sent to sender and to the course's notification addresses
        String[] toNotify = notifyBox.getText().replace(" ", "").split("(,|;)");
        
        String notificationMessage = "At " + now + ", grader " + Allocator.getGeneralUtilities().getUserLogin() +
                " submitted grading for assignment " + subjectBox.getText().split(" ")[1] + " for the following students: <blockquote>";
        for (String student : students) {
            student = student.split("@")[0];
            String attachment = "none";
            if (_attachments != null) {
                attachment = _attachments.get(student);
            }
            notificationMessage += student + "; attachment: " + attachment + "<br />";
        }
        notificationMessage += "</blockquote> The following message was sent to the students: <blockquote>" + bodyText.getText()
                + "</blockquote>";
        Allocator.getCourseInfo().getEmailAccount().sendMail(fromBox.getText(),                         //from
                                                                  new String[] {fromBox.getText()},     //to
                                                                  toNotify,                             //cc
                                                                  null,                                 //bcc
                                                                  subjectBox.getText(),
                                                                  notificationMessage, 
                                                                  null);                                //attachment paths
        sentDialog.setLocationRelativeTo(null);
        sentDialog.setVisible(true);
}//GEN-LAST:event_sendButtonActionPerformed

    private void dialogCloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dialogCloseButtonActionPerformed
        sentDialog.dispose();
        this.dispose();
}//GEN-LAST:event_dialogCloseButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
}//GEN-LAST:event_cancelButtonActionPerformed

    public void setSubject(String s) {
        subjectBox.setText(s);
    }

    public void setTo(String s) {
        studentsBox.setText(s);
    }

    public void setCc(String s) {
        notifyBox.setText(s);
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
    private javax.swing.JLabel attachmentMessage;
    private javax.swing.JTextArea bodyText;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton dialogCloseButton;
    private javax.swing.JTextField fromBox;
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
    private javax.swing.JTextField notifyBox;
    private javax.swing.JButton sendButton;
    private javax.swing.JDialog sentDialog;
    private javax.swing.JTextField studentsBox;
    private javax.swing.JTextField subjectBox;
    // End of variables declaration//GEN-END:variables
}
