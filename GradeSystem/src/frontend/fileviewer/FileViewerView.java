/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FileViewerView.java
 *
 * Created on Oct 1, 2009, 8:51:12 PM
 */
package frontend.fileviewer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import utils.ErrorView;

/**
 *
 * @author psastras
 */
public class FileViewerView extends javax.swing.JFrame {

    /** Creates new form FileViewerView */
    public FileViewerView() {
        initComponents();
        this.setTitle("File Viewer");
    }

    public void openFile(File f) {
        StringBuilder contents = new StringBuilder();
        try {
            BufferedReader input = new BufferedReader(new FileReader(f));
            String line = null; //not declared within while loop
            while ((line = input.readLine()) != null) {
                contents.append(line);
                contents.append(System.getProperty("line.separator"));
            }
            input.close();
            m_tabbedPane.setTitleAt(0, f.getName());
            this.setTitle(f.getName() + " - File Viewer");
            m_textArea.setText(contents.toString());
            m_textArea.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                }
                
            });

        } catch (Exception ex) {
            new ErrorView(ex);
        }
        m_textArea.setCaretPosition(0);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_tabbedPane = new javax.swing.JTabbedPane();
        m_scrollPane = new javax.swing.JScrollPane();
        m_textArea = new javax.swing.JTextArea();
        m_menu = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        m_tabbedPane.setFocusable(false);
        m_tabbedPane.setName("m_tabbedPane"); // NOI18N

        m_scrollPane.setName("m_scrollPane"); // NOI18N

        m_textArea.setColumns(20);
        m_textArea.setEditable(false);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(FileViewerView.class);
        m_textArea.setFont(resourceMap.getFont("m_textArea.font")); // NOI18N
        m_textArea.setRows(5);
        m_textArea.setName("m_textArea"); // NOI18N
        m_scrollPane.setViewportView(m_textArea);

        m_tabbedPane.addTab(resourceMap.getString("m_scrollPane.TabConstraints.tabTitle"), m_scrollPane); // NOI18N

        m_menu.setName("m_menu"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N
        m_menu.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N
        m_menu.add(jMenu2);

        setJMenuBar(m_menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(m_tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 585, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(m_tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new FileViewerView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar m_menu;
    private javax.swing.JScrollPane m_scrollPane;
    private javax.swing.JTabbedPane m_tabbedPane;
    private javax.swing.JTextArea m_textArea;
    // End of variables declaration//GEN-END:variables
}
