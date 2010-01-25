/*
 * FileViewerView.java
 *
 * Created on Oct 1, 2009, 8:51:12 PM
 */
package utils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Utilities;

/**
 *
 * @author psastras
 */
public class TextViewerView extends javax.swing.JFrame {

    private final Color HCOLOR = new Color(255, 181, 84);
    private final Color HCCOLOR = new Color(201, 233, 255);
    private final Color LHCOLOR = new Color(205, 233, 205);
    private Highlighter.HighlightPainter _highlighter = new Highlight(HCOLOR);
    private Highlighter.HighlightPainter _curhighlight = new Highlight(HCCOLOR);
    private Highlighter.HighlightPainter _linehighlight = new LineHighlight(LHCOLOR);
    private LinkedList<Integer> _refs = new LinkedList<Integer>();
    private LinkedList<Integer> _pos = new LinkedList<Integer>();
    private Object[] _linehighlighter;

    /** Creates new form FileViewerView */
    private TextViewerView() {
        initComponents();
        try {
            this.setIconImage(ImageIO.read(getClass().getResource("/gradesystem/resources/icons/32x32/text-x-generic.png")));
        } catch (Exception e) {
        }
        this.setTitle("File Viewer");
        m_textArea.addKeyListener(new KeyListener() {

            public void keyTyped(KeyEvent e) {
                if (!e.isActionKey() && !e.isControlDown() && !e.isAltDown()) {
                    e.consume();
                }
            }

            public void keyPressed(KeyEvent e) {
                if (!e.isActionKey() && !e.isControlDown() && !e.isAltDown()) {
                    e.consume();
                }
            }

            public void keyReleased(KeyEvent e) {
                if (!e.isActionKey() && !e.isControlDown() && !e.isAltDown()) {
                    e.consume();
                }
            }
        });
        m_textArea.requestFocus();
    }

    public TextViewerView(File file) {
        this();

        this.openFile(file);

        Dimension size = new Dimension(640,500);
        this.setSize(size);
        this.setPreferredSize(size);
        
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private class ReferencesPanel extends JPanel {

        private int height = 3;
        private int index = -1;

        public ReferencesPanel() {
            this.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    if (index < 0) {
                        return;
                    }
                    _curindex = index;
                    highlightText(jTextField1.getText());

                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }
            });

            this.addMouseMotionListener(new MouseMotionListener() {

                public void mouseDragged(MouseEvent e) {
                }

                public void mouseMoved(MouseEvent e) {
                    int l = m_textArea.getLineCount();
                    int h = getHeight(), w = getWidth();
                    int y = e.getY();
                    index = 0;
                    for (Integer i : _refs) {
                        int pos = (int) ((((double) i) / ((double) l)) * h);
                        if (y > pos - 3 && y < pos + height + 3) {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                            repaint();
                            return;
                        }
                        index++;
                    }
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    index = -1;
                    return;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int h = this.getHeight(), w = this.getWidth();
            int l = m_textArea.getLineCount();
            g.clearRect(0, 0, w, h);
            int c = 0;
            for (Integer i : _refs) {
                g.setColor(HCOLOR);
                if (c == _curindex) {
                    g.setColor(HCCOLOR);
                }
                g.fillRect(0, (int) ((((double) i) / ((double) l)) * h), w, height);
                c++;
            }
        }
    }
    private int _curindex;

    /**
     * Highlights all occurrences of the input string
     * @param text
     */
    private void highlightText(String text) {
        _refs.clear();
        _pos.clear();
        Highlighter h = m_textArea.getHighlighter();
        for (Highlighter.Highlight hilight : h.getHighlights()) {
            if (hilight.getPainter() instanceof Highlight) {
                h.removeHighlight(hilight);
            }
        }
        if (text.length() == 0 || text.isEmpty()) {
            jPanel2.repaint();
            return;
        }
        try {
            Document d = m_textArea.getDocument();
            String t = d.getText(0, d.getLength());
            if (!jCheckBox1.isSelected()) {
                t = t.toLowerCase();
                text = text.toLowerCase();
            }

            int p = 0, i = 0;
            boolean isScrolled = false;
            while ((p = t.indexOf(text, p)) >= 0) {
                if (i == _curindex) {
                    h.addHighlight(p, p + text.length(), _curhighlight);
                    isScrolled = true;
                    m_textArea.setCaretPosition(p);
                    _curindex = i;
                } else {
                    h.addHighlight(p, p + text.length(), _highlighter);
                }
                p += text.length();
                _refs.add(m_textArea.getLineOfOffset(p));
                _pos.add(p);
                i++;
            }
            if (!isScrolled) {
                if (t.indexOf(text, 0) < 0) {
                    return;
                }
                h.addHighlight(t.indexOf(text, 0), t.indexOf(text, 0) + text.length(), _curhighlight);
                _curindex = 0;
                m_textArea.setCaretPosition(t.indexOf(text, 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        jPanel2.repaint();
    }

    private void highlightNext() {
        _curindex++;
        highlightText(jTextField1.getText());
    }

    private void highlightCurrentLine() {
        Highlighter h = m_textArea.getHighlighter();
        try {
            int pos = m_textArea.getCaretPosition();
            javax.swing.text.Element e = Utilities.getParagraphElement(m_textArea, pos);
            int line = m_textArea.getLineOfOffset(pos);
            if (_linehighlighter[line] == null) {
                _linehighlighter[line] = h.addHighlight(e.getStartOffset(), e.getEndOffset(), _linehighlight);
            } else {
                h.removeHighlight(_linehighlighter[line]);
                _linehighlighter[line] = null;
            }
        } catch (Exception e) {
        }
    }

    private void removeLineHighlights() {
        Highlighter h = m_textArea.getHighlighter();
        for (int i = 0; i < _linehighlighter.length; i++) {
            if (_linehighlighter[i] != null) {
                h.removeHighlight(_linehighlighter[i]);
                _linehighlighter[i] = null;
            }
        }
    }

    private void openFile(File f) {
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
        _linehighlighter = new Object[m_textArea.getLineCount()];
    }

    private class Highlight extends DefaultHighlighter.DefaultHighlightPainter {

        public Highlight(Color color) {
            super(color);
        }
    }

    private class LineHighlight extends DefaultHighlighter.DefaultHighlightPainter {

        public LineHighlight(Color color) {
            super(color);
        }
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
        jPanel1 = new javax.swing.JPanel();
        m_scrollPane = new javax.swing.JScrollPane();
        m_textArea = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new ReferencesPanel();
        m_menu = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("Form"); // NOI18N

        m_tabbedPane.setFocusable(false);
        m_tabbedPane.setName("m_tabbedPane"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        m_scrollPane.setName("m_scrollPane"); // NOI18N

        m_textArea.setColumns(20);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(TextViewerView.class);
        m_textArea.setFont(resourceMap.getFont("m_textArea.font")); // NOI18N
        m_textArea.setRows(5);
        m_textArea.setName("m_textArea"); // NOI18N
        m_scrollPane.setViewportView(m_textArea);

        jTextField1.setFont(resourceMap.getFont("jTextField1.font")); // NOI18N
        jTextField1.setForeground(Color.gray);
        jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
        jTextField1.setName("jTextField1"); // NOI18N
        jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField1FocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                jTextField1FocusLost(evt);
            }
        });
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jCheckBox1.setFont(resourceMap.getFont("chkMatchCase.font")); // NOI18N
        jCheckBox1.setMnemonic('M');
        jCheckBox1.setText(resourceMap.getString("chkMatchCase.text")); // NOI18N
        jCheckBox1.setFocusable(false);
        jCheckBox1.setName("chkMatchCase"); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jButton1.setFont(resourceMap.getFont("jButton1.font")); // NOI18N
        jButton1.setMnemonic('N');
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel1.setFont(resourceMap.getFont("jLabel1.font")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jPanel2.setName("jPanel2"); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 16, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 408, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(m_scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1)
                        .addGap(22, 22, 22)))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        m_tabbedPane.addTab(resourceMap.getString("jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        m_menu.setName("m_menu"); // NOI18N

        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        jMenu1.add(jMenuItem1);

        m_menu.add(jMenu1);

        jMenu2.setText(resourceMap.getString("jMenu2.text")); // NOI18N
        jMenu2.setName("jMenu2"); // NOI18N

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText(resourceMap.getString("jMenuItem2.text")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        m_menu.add(jMenu2);

        setJMenuBar(m_menu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(m_tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(m_tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        highlightText(jTextField1.getText());
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        highlightNext();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            highlightNext();
        } else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            m_textArea.requestFocus();
        }
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu1ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenu1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        jTextField1.requestFocus();
        jTextField1.selectAll();
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jTextField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusLost
        if (jTextField1.getText().isEmpty()) {
            jTextField1.setForeground(Color.gray);
            jTextField1.setText("Press Ctrl-F to Search");
        }
    }//GEN-LAST:event_jTextField1FocusLost

    private void jTextField1FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusGained

        if (jTextField1.getForeground() == Color.gray) {
            jTextField1.setText("");
            jTextField1.setForeground(Color.black);
        }
    }//GEN-LAST:event_jTextField1FocusGained

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        highlightText(jTextField1.getText());
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        highlightCurrentLine();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        removeLineHighlights();
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new TextViewerView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JMenuBar m_menu;
    private javax.swing.JScrollPane m_scrollPane;
    private javax.swing.JTabbedPane m_tabbedPane;
    private javax.swing.JTextArea m_textArea;
    // End of variables declaration//GEN-END:variables
}