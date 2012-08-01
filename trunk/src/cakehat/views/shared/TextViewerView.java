package cakehat.views.shared;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.Utilities;
import cakehat.Allocator;
import cakehat.logging.ErrorReporter;
import cakehat.printing.CITPrinter;
import cakehat.printing.PrintRequest;
import java.awt.Window;
import support.resources.icons.IconLoader;
import support.resources.icons.IconLoader.IconImage;
import support.resources.icons.IconLoader.IconSize;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.swing.JDialog;
import javax.swing.text.BadLocationException;

/**
 * A simpler viewer for plain text files. Allows for searching within the
 * displayed text file.
 * <br/>
 * Adapted from the original GUI builder version by psastras.
 *
 * @author jak2
 * @author psastras
 */
public class TextViewerView extends JDialog
{
    private static final int TEXT_WIDTH = 610;
    private static final int SEARCH_HIGHLIGHTS_WIDTH = 30;
    private static final int CONTENT_WIDTH = TEXT_WIDTH + SEARCH_HIGHLIGHTS_WIDTH;
    private static final Dimension FIND_PANEL_SIZE = new Dimension(CONTENT_WIDTH, 30);
    private static final Dimension TEXT_PANE_SIZE = new Dimension(TEXT_WIDTH, 400);
    private static final Dimension SEARCH_HIGHLIGHTS_PANEL_SIZE = new Dimension(SEARCH_HIGHLIGHTS_WIDTH, TEXT_PANE_SIZE.height);
    private static final Dimension CONTENT_SIZE = new Dimension(CONTENT_WIDTH, FIND_PANEL_SIZE.height + TEXT_PANE_SIZE.height);

    private static final Color HIGHLIGHT_COLOR = new Color(255, 181, 84);
    private static final Color CURRENT_HIGHLIGHT_COLOR = new Color(201, 233, 255);
    private static final Color LINE_HIGHLIGHT_COLOR = new Color(205, 233, 205);

    private final Highlighter.HighlightPainter _highlighter = new Highlight(HIGHLIGHT_COLOR);
    private final Highlighter.HighlightPainter _currentHighlight = new Highlight(CURRENT_HIGHLIGHT_COLOR);
    private final Highlighter.HighlightPainter _lineHighlight = new LineHighlight(LINE_HIGHLIGHT_COLOR);

    private LinkedList<Integer> _refs = new LinkedList<Integer>();
    private LinkedList<Integer> _pos = new LinkedList<Integer>();
    private Object[] _linehighlighter;
    private int _currIndex;

    private JTextArea _textArea;
    private JTextField _searchField;
    private ReferencesPanel _searchHighlightsPanel;
    private JCheckBox _matchCaseCheckBox;

    public TextViewerView(Window owner, File file, String title)
    {
        this(owner, loadTextFromFile(file), title);
    }

    public TextViewerView(Window owner, String text, String title)
    {
        super(owner, title, ModalityType.MODELESS);
        
        this.initIcon();
        this.initMenu();
        this.initComponents();

        _textArea.setText(text);
        _textArea.setCaretPosition(0);
        _linehighlighter = new Object[_textArea.getLineCount()];

        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(owner);
        this.setVisible(true);
    }

    private static String loadTextFromFile(File file)
    {
        String text = "";
        if(file != null)
        {
            try
            {
                text = Allocator.getFileSystemUtilities().readFile(file);
            }
            catch (IOException ex)
            {
                ErrorReporter.report("Cannot read file: " + file.getAbsolutePath(), ex);
            }
        }

        return text;
    }

    private void initIcon()
    {
        try
        {
            this.setIconImage(IconLoader.loadBufferedImage(IconSize.s32x32, IconImage.TEXT_X_GENERIC));
        }
        catch (Exception e) {}
    }

    private void initMenu()
    {
        //Menu bar
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        //File menu
        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        //Print item
        JMenuItem menuItem = new JMenuItem("Print");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                printMenuItemActionPerformed(ae);
            }
        });
        menu.add(menuItem);

        //Quit item
        menuItem = new JMenuItem("Quit");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                TextViewerView.this.dispose();
            }
        });
        menu.add(menuItem);

        //Edit menu
        menu = new JMenu("Edit");
        menuBar.add(menu);

        //Find
        menuItem = new JMenuItem("Find");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                findMenuItemActionPerformed(ae);
            }
        });
        menu.add(menuItem);

        //Toggle
        menuItem = new JMenuItem("Toggle Line Highlights");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                toggleHighlightsMenuItemActionPerformed(ae);
            }
        });
        menu.add(menuItem);

        //Remove highlights
        menuItem = new JMenuItem("Remove All Line Highlights");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                removeHightlightsMenuItemActionPerformed(ae);
            }
        });
        menu.add(menuItem);
    }

    private void initComponents()
    {
        // Overall
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setSize(CONTENT_SIZE);
        mainPanel.setPreferredSize(CONTENT_SIZE);
        this.add(mainPanel);

        // Find
        JPanel findPanel = new JPanel(new BorderLayout(5, 0));
        findPanel.setSize(FIND_PANEL_SIZE);
        findPanel.setPreferredSize(FIND_PANEL_SIZE);
        mainPanel.add(findPanel, BorderLayout.NORTH);

        findPanel.add(new JLabel("  Find: "), BorderLayout.WEST);

        _searchField = new JTextField("Press Ctrl-F To Search");
        _searchField.setForeground(Color.gray);
        _searchField.addFocusListener(new FocusListener()
        {
            public void focusGained(FocusEvent e)
            {
                searchFieldFocusGained(e);
            }

            public void focusLost(FocusEvent e)
            {
                searchFieldFocusLost(e);
            }
        });

        _searchField.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent e) { }

            public void keyPressed(KeyEvent e)
            {
                searchFieldKeyPressed(e);
            }

            public void keyReleased(KeyEvent e)
            {
                searchFieldKeyReleased(e);
            }
        });
        findPanel.add(_searchField, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        _matchCaseCheckBox = new JCheckBox("Match Case");
        _matchCaseCheckBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                matchCaseCheckBoxActionPerformed(e);
            }
        });
        controlPanel.add(_matchCaseCheckBox);

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                nextButtonActionPerformed(e);
            }
        });
        controlPanel.add(nextButton);

        findPanel.add(controlPanel, BorderLayout.EAST);


        // Text
        _textArea = new JTextArea();
        _textArea.setLineWrap(true);
        _textArea.setWrapStyleWord(true);
        _textArea.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent e)
            {
                consumeEventAsNecessary(e);
            }

            public void keyPressed(KeyEvent e)
            {
                consumeEventAsNecessary(e);
            }

            public void keyReleased(KeyEvent e)
            {
                consumeEventAsNecessary(e);
            }

            private void consumeEventAsNecessary(KeyEvent e)
            {
                if (!e.isActionKey() && !e.isControlDown() && !e.isAltDown())
                {
                    e.consume();
                }
            }
        });
        _textArea.requestFocus();

        JScrollPane textPane = new JScrollPane(_textArea);
        textPane.setSize(TEXT_PANE_SIZE);
        textPane.setPreferredSize(TEXT_PANE_SIZE);
        mainPanel.add(textPane, BorderLayout.CENTER);

        // Search highlights
        _searchHighlightsPanel = new ReferencesPanel();
        _searchHighlightsPanel.setSize(SEARCH_HIGHLIGHTS_PANEL_SIZE);
        _searchHighlightsPanel.setPreferredSize(SEARCH_HIGHLIGHTS_PANEL_SIZE);
        mainPanel.add(_searchHighlightsPanel, BorderLayout.EAST);
    }

    private class ReferencesPanel extends JPanel
    {
        private int height = 3;
        private int index = -1;

        public ReferencesPanel()
        {
            this.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if (index < 0)
                    {
                        return;
                    }
                    _currIndex = index;
                    highlightText(_searchField.getText());
                }
            });

            this.addMouseMotionListener(new MouseMotionAdapter()
            {
                @Override
                public void mouseMoved(MouseEvent e)
                {
                    int l = _textArea.getLineCount();
                    int h = getHeight(), w = getWidth();
                    int y = e.getY();
                    index = 0;
                    for (int i : _refs)
                    {
                        int pos = (int) ((((double) i) / ((double) l)) * h);
                        if (y > pos - 3 && y < pos + height + 3)
                        {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                            repaint();
                            return;
                        }
                        index++;
                    }
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    index = -1;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            int h = this.getHeight(), w = this.getWidth();
            int l = _textArea.getLineCount();
            g.clearRect(0, 0, w, h);
            int c = 0;

            for (int i : _refs)
            {
                g.setColor(HIGHLIGHT_COLOR);
                if (c == _currIndex)
                {
                    g.setColor(CURRENT_HIGHLIGHT_COLOR);
                }
                g.fillRect(0, (int) ((((double) i) / ((double) l)) * h), w, height);
                c++;
            }
        }
    }

    /**
     * Highlights all occurrences of the input string
     * @param text
     */
    private void highlightText(String text)
    {
        _refs.clear();
        _pos.clear();
        Highlighter h = _textArea.getHighlighter();
        for (Highlighter.Highlight hilight : h.getHighlights())
        {
            if (hilight.getPainter() instanceof Highlight)
            {
                h.removeHighlight(hilight);
            }
        }

        if (text.length() == 0 || text.isEmpty())
        {
            _searchHighlightsPanel.repaint();
            return;
        }

        try
        {
            Document d = _textArea.getDocument();
            String t = d.getText(0, d.getLength());
            if (!_matchCaseCheckBox.isSelected())
            {
                t = t.toLowerCase();
                text = text.toLowerCase();
            }

            int p = 0, i = 0;
            boolean isScrolled = false;
            while ((p = t.indexOf(text, p)) >= 0)
            {
                if (i == _currIndex)
                {
                    h.addHighlight(p, p + text.length(), _currentHighlight);
                    isScrolled = true;
                    _textArea.setCaretPosition(p);
                    _currIndex = i;
                }
                else
                {
                    h.addHighlight(p, p + text.length(), _highlighter);
                }
                p += text.length();
                _refs.add(_textArea.getLineOfOffset(p));
                _pos.add(p);
                i++;
            }
            if (!isScrolled)
            {
                if (t.indexOf(text, 0) < 0)
                {
                    return;
                }
                h.addHighlight(t.indexOf(text, 0), t.indexOf(text, 0) + text.length(), _currentHighlight);
                _currIndex = 0;
                _textArea.setCaretPosition(t.indexOf(text, 0));
            }
        }
        catch (BadLocationException e)
        {
            ErrorReporter.report("Unable to highlight text", e);
        }
        _searchHighlightsPanel.repaint();
    }

    private void highlightNext()
    {
        _currIndex++;
        highlightText(_searchField.getText());
    }

    private void highlightCurrentLine()
    {
        Highlighter h = _textArea.getHighlighter();
        try
        {
            int pos = _textArea.getCaretPosition();
            Element e = Utilities.getParagraphElement(_textArea, pos);
            int line = _textArea.getLineOfOffset(pos);
            if (_linehighlighter[line] == null)
            {
                _linehighlighter[line] = h.addHighlight(e.getStartOffset(), e.getEndOffset(), _lineHighlight);
            }
            else
            {
                h.removeHighlight(_linehighlighter[line]);
                _linehighlighter[line] = null;
            }
        }
        catch (Exception e) { }
    }

    private void removeLineHighlights()
    {
        Highlighter h = _textArea.getHighlighter();
        for (int i = 0; i < _linehighlighter.length; i++)
        {
            if (_linehighlighter[i] != null)
            {
                h.removeHighlight(_linehighlighter[i]);
                _linehighlighter[i] = null;
            }
        }
    }

    private void nextButtonActionPerformed(ActionEvent evt)
    {
        highlightNext();
    }

    private void printMenuItemActionPerformed(ActionEvent ae)
    {
        try
        {
            File tmpFile = Allocator.getFileSystemUtilities().createTempFile(".tvv", ".tmp",
                    Allocator.getPathServices().getUserWorkspaceDir());
            PrintWriter writer = new PrintWriter(tmpFile);
            writer.print(_textArea.getText());
            writer.close();
            PrintRequest request = new PrintRequest(Arrays.asList(tmpFile));
            
            //printer will be null if user cancels printing
            CITPrinter printer = Allocator.getGradingServices().getPrinter();

            if(printer != null)
            {
                try
                {
                    Allocator.getPortraitPrintingService().print(request, printer);
                }
                catch(IOException e)
                {
                    ErrorReporter.report("Unable to print", e);
                }
            }
        }
        catch(IOException e)
        {
            ErrorReporter.report("Unable to create temporary file used for printing", e);
        }
    }

    private void findMenuItemActionPerformed(ActionEvent evt)
    {
        _searchField.requestFocus();
        _searchField.selectAll();
    }

    private void toggleHighlightsMenuItemActionPerformed(ActionEvent evt)
    {
        highlightCurrentLine();
    }

    private void removeHightlightsMenuItemActionPerformed(ActionEvent evt)
    {
        removeLineHighlights();
    }

    private void searchFieldFocusLost(FocusEvent evt)
    {
        if (_searchField.getText().isEmpty())
        {
            _searchField.setForeground(Color.gray);
            _searchField.setText("Press Ctrl-F to Search");
        }
    }

    private void searchFieldFocusGained(FocusEvent evt)
    {
        if (_searchField.getForeground() == Color.gray)
        {
            _searchField.setText("");
            _searchField.setForeground(Color.black);
        }
    }

    private void searchFieldKeyPressed(KeyEvent evt)
    {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
            highlightNext();
        }
        else if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            _textArea.requestFocus();
        }
    }

    private void searchFieldKeyReleased(KeyEvent evt)
    {
        highlightText(_searchField.getText());
    }

    private void matchCaseCheckBoxActionPerformed(java.awt.event.ActionEvent evt)
    {
        highlightText(_searchField.getText());
    }

    private class Highlight extends DefaultHighlighter.DefaultHighlightPainter
    {
        public Highlight(Color color)
        {
            super(color);
        }
    }

    private class LineHighlight extends DefaultHighlighter.DefaultHighlightPainter
    {
        public LineHighlight(Color color)
        {
            super(color);
        }
    }
}