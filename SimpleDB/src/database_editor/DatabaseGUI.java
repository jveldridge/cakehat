/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DatabaseGUI.java
 *
 * Created on Sep 3, 2009, 1:28:33 PM
 */
package database_editor;

import cs015Database.DatabaseInterops;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import org.tmatesoft.sqljet.core.SqlJetException;

/**
 *
 * @author psastras
 */
public class DatabaseGUI extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    private boolean _isModified = false;
    private Timer _dbTimer = new Timer();

    /** Creates new form DatabaseGUI */
    public DatabaseGUI() {
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/cs015Database/accessories-calculator.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        initComponents();
        updateFormComponents();
        this.setLocationRelativeTo(null);
    }

    private void refreshTable(String tableName) {
        this.setTitle(tableName + " - cs015DatabaseEditor");
        gridView.refresh(tableName);
    }

    public GridView getGrid() {
        return gridView;
    }

    public void updateStatus(String message) {
        statusLabel.setText(message);
        Timer t = new Timer();
        class NewTask extends TimerTask {

            @Override
            public void run() {
                statusLabel.setText("Ready");
                this.cancel();
            }
        }

        t.schedule(new NewTask(), 1000);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        aboutDialog = new javax.swing.JDialog();
        aboutDialogTitleText = new javax.swing.JLabel();
        aboutDialogOkayButton = new javax.swing.JButton();
        statusBarPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        tableSelector = new javax.swing.JComboBox();
        addRowButton = new javax.swing.JButton();
        removeRowButton = new javax.swing.JButton();
        toolbarSeparator = new javax.swing.JSeparator();
        gridViewScrollPane = new javax.swing.JScrollPane();
        gridView = new database_editor.GridView();
        filterTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        mainMenu = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        fileMenuSeparator = new javax.swing.JSeparator();
        fileMenuExit = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        editMenuAddRow = new javax.swing.JMenuItem();
        editMenuRemoveRows = new javax.swing.JMenuItem();
        editMenuSeparator = new javax.swing.JSeparator();
        helpMenu = new javax.swing.JMenu();
        helpMenuAbout = new javax.swing.JMenuItem();

        aboutDialog.setTitle("About");
        aboutDialog.setLocationByPlatform(true);
        aboutDialog.setMinimumSize(new java.awt.Dimension(382, 140));
        aboutDialog.setModal(true);
        aboutDialog.setResizable(false);

        aboutDialogTitleText.setFont(new java.awt.Font("Tahoma", 1, 11));
        aboutDialogTitleText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database_editor/help-browser.png"))); // NOI18N
        aboutDialogTitleText.setText("cs015 Database Editor");
        aboutDialogTitleText.setIconTextGap(15);

        aboutDialogOkayButton.setText("Close");
        aboutDialogOkayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutDialogOkayButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout aboutDialogLayout = new javax.swing.GroupLayout(aboutDialog.getContentPane());
        aboutDialog.getContentPane().setLayout(aboutDialogLayout);
        aboutDialogLayout.setHorizontalGroup(
            aboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(aboutDialogTitleText, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addGap(40, 40, 40))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, aboutDialogLayout.createSequentialGroup()
                .addContainerGap(266, Short.MAX_VALUE)
                .addComponent(aboutDialogOkayButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        aboutDialogLayout.setVerticalGroup(
            aboutDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutDialogLayout.createSequentialGroup()
                .addComponent(aboutDialogTitleText, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addComponent(aboutDialogOkayButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("cs015 Database Editor");
        setBackground(java.awt.SystemColor.controlDkShadow);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        statusBarPanel.setBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("light")));

        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        statusLabel.setText("Ready");

        javax.swing.GroupLayout statusBarPanelLayout = new javax.swing.GroupLayout(statusBarPanel);
        statusBarPanel.setLayout(statusBarPanelLayout);
        statusBarPanelLayout.setHorizontalGroup(
            statusBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusBarPanelLayout.createSequentialGroup()
                .addContainerGap(693, Short.MAX_VALUE)
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 475, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        statusBarPanelLayout.setVerticalGroup(
            statusBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE)
        );

        tableSelector.setMaximumRowCount(20);
        tableSelector.setMinimumSize(new java.awt.Dimension(10, 25));
        tableSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tableSelectorActionPerformed(evt);
            }
        });

        addRowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database_editor/list-add.png"))); // NOI18N
        addRowButton.setText("Add Row");
        addRowButton.setFocusable(false);
        addRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowButtonActionPerformed(evt);
            }
        });

        removeRowButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database_editor/list-remove.png"))); // NOI18N
        removeRowButton.setText("Remove Row(s)");
        removeRowButton.setFocusable(false);
        removeRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRowButtonActionPerformed(evt);
            }
        });

        gridView.setDoubleBuffered(true);
        gridViewScrollPane.setViewportView(gridView);

        filterTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterTextFieldActionPerformed(evt);
            }
        });
        filterTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterTextFieldKeyTyped(evt);
            }
        });

        jLabel1.setText("Filter Table:");

        jButton1.setText("Refresh Table");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(gridViewScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 1158, Short.MAX_VALUE)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(tableSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 287, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(filterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addRowButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeRowButton)))
                .addContainerGap())
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                    .addComponent(toolbarSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1182, Short.MAX_VALUE)
                    .addGap(0, 0, 0)))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tableSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeRowButton)
                    .addComponent(addRowButton)
                    .addComponent(filterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridViewScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(mainPanelLayout.createSequentialGroup()
                    .addComponent(toolbarSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(705, Short.MAX_VALUE)))
        );

        fileMenu.setText("File");
        fileMenu.add(fileMenuSeparator);

        fileMenuExit.setText("Exit");
        fileMenuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuExitActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuExit);

        mainMenu.add(fileMenu);

        editMenu.setText("Edit");

        editMenuAddRow.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        editMenuAddRow.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database_editor/list-add.png"))); // NOI18N
        editMenuAddRow.setText("Add Row");
        editMenuAddRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuAddRowActionPerformed(evt);
            }
        });
        editMenu.add(editMenuAddRow);

        editMenuRemoveRows.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        editMenuRemoveRows.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database_editor/list-remove.png"))); // NOI18N
        editMenuRemoveRows.setText("Remove Row(s)");
        editMenuRemoveRows.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuRemoveRowsActionPerformed(evt);
            }
        });
        editMenu.add(editMenuRemoveRows);
        editMenu.add(editMenuSeparator);

        mainMenu.add(editMenu);

        helpMenu.setText("Help");
        helpMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuActionPerformed(evt);
            }
        });

        helpMenuAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.CTRL_MASK));
        helpMenuAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/database_editor/help-browser.png"))); // NOI18N
        helpMenuAbout.setText("About");
        helpMenuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuAboutActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuAbout);

        mainMenu.add(helpMenu);

        setJMenuBar(mainMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusBarPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusBarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void addRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowButtonActionPerformed

        updateStatus("Synchronizing...");
        gridView.addRow(tableSelector.getSelectedItem().toString());
    //_isModified = true;
}//GEN-LAST:event_addRowButtonActionPerformed

    private void removeRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRowButtonActionPerformed

        updateStatus("Synchronizing...");
        gridView.removeRows(tableSelector.getSelectedItem().toString());
    //_isModified = true;
}//GEN-LAST:event_removeRowButtonActionPerformed

    private void fileMenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuExitActionPerformed

        System.exit(0);
    }//GEN-LAST:event_fileMenuExitActionPerformed

    private void editMenuRemoveRowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuRemoveRowsActionPerformed

        gridView.removeRows(tableSelector.getSelectedItem().toString());
    }//GEN-LAST:event_editMenuRemoveRowsActionPerformed

    private void editMenuAddRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuAddRowActionPerformed

        gridView.addRow(tableSelector.getSelectedItem().toString());
    }//GEN-LAST:event_editMenuAddRowActionPerformed

    private void aboutDialogOkayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutDialogOkayButtonActionPerformed

        aboutDialog.setVisible(false);
    }//GEN-LAST:event_aboutDialogOkayButtonActionPerformed

    private void helpMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuActionPerformed
    }//GEN-LAST:event_helpMenuActionPerformed

    private void helpMenuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuAboutActionPerformed

        aboutDialog.setVisible(true);
    }//GEN-LAST:event_helpMenuAboutActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    }//GEN-LAST:event_formWindowClosing

    private void filterTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterTextFieldActionPerformed
    }//GEN-LAST:event_filterTextFieldActionPerformed

    private void filterTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterTextFieldKeyTyped

        gridView.filter(filterTextField.getText());
    }//GEN-LAST:event_filterTextFieldKeyTyped

    private void tableSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tableSelectorActionPerformed
        gridView.removeEditor();
        refreshTable((String) tableSelector.getSelectedItem());
    }//GEN-LAST:event_tableSelectorActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        gridView.refresh((String)tableSelector.getSelectedItem());
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Update components to current database information.
     */
    private void updateFormComponents() {
        updateStatus("Synchronizing...");

        try {
            String[] tableNames = DatabaseInterops.getTableNames();
            tableSelector.removeAllItems();
            Arrays.sort(tableNames);
            for (String s : tableNames) {
                tableSelector.insertItemAt(s, tableSelector.getItemCount());
            }
            if (tableSelector.getItemCount() > 0) {
                tableSelector.setSelectedIndex(0);
            }
        } catch (SqlJetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new DatabaseGUI().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog aboutDialog;
    private javax.swing.JButton aboutDialogOkayButton;
    private javax.swing.JLabel aboutDialogTitleText;
    private javax.swing.JButton addRowButton;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editMenuAddRow;
    private javax.swing.JMenuItem editMenuRemoveRows;
    private javax.swing.JSeparator editMenuSeparator;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem fileMenuExit;
    private javax.swing.JSeparator fileMenuSeparator;
    private javax.swing.JTextField filterTextField;
    private database_editor.GridView gridView;
    private javax.swing.JScrollPane gridViewScrollPane;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuAbout;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar mainMenu;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton removeRowButton;
    private javax.swing.JPanel statusBarPanel;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JComboBox tableSelector;
    private javax.swing.JSeparator toolbarSeparator;
    // End of variables declaration//GEN-END:variables
}
