package gradesystem.views.shared;

import gradesystem.config.TA;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import gradesystem.Allocator;
import gradesystem.components.GenericJList;
import gradesystem.components.Student;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.ButtonModel;

/**
 * Provides an interface to manage the students on each TA's blacklist.
 * 
 * @author jveldridge
 */
public class ModifyBlacklistView extends JFrame{

    private JTextField _filterBox;
    private List<Student> _studentLogins;
    private GenericJList<Student> _studentList;
    private ButtonGroup _taButtons;
    private Map<TA, GenericJList<String>> _taToList;
    private Map<ButtonModel, TA> _rbToTA;

    private static final int DEFAULT_VIEW_HEIGHT = 800;
    
    public ModifyBlacklistView(Collection<TA> tas) {
        super("Modify Blacklist");
        this.setLayout(new BorderLayout());  
        
        JPanel blacklistPanel = new JPanel();
        blacklistPanel.setLayout(new GridLayout(0,1));
        JScrollPane blacklistScrollPane = new JScrollPane(blacklistPanel);
        blacklistScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        blacklistScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        blacklistScrollPane.setPreferredSize(new Dimension(700, DEFAULT_VIEW_HEIGHT));
        _taToList = new HashMap<TA, GenericJList<String>>();
        _rbToTA = new HashMap<ButtonModel, TA>();
        
        _taButtons = new ButtonGroup();
        for (TA ta : tas) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(665,50));

            final GenericJList<String> list = new GenericJList<String>();
            
            final JRadioButton rb = new JRadioButton(ta.getLogin());
            rb.setPreferredSize(new Dimension(95,50));
            rb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _filterBox.requestFocus();
                    //clear the other list selections
                    for (GenericJList<String> list2Clear : _taToList.values()) {
                        if (list2Clear == list) {
                            continue;
                        }
                        list2Clear.clearSelection();
                    }
                }
            });
            _taButtons.add(rb);
            _rbToTA.put(rb.getModel(), ta);

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //clear the selections in any other JLists
                    for (GenericJList<String> list2Clear : _taToList.values()) {
                        if (list2Clear == list) {
                            continue;
                        }
                        list2Clear.clearSelection();
                    }

                    //select the radio button corresponding to the TA whose
                    //blacklist this JList represents
                    rb.setSelected(true);
                }
            });
            
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(0);
            list.setFixedCellWidth(75);
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            try {
                list.setListData(Allocator.getDatabaseIO().getTABlacklist(ta));
            } catch (SQLException ex) {
                new ErrorView(ex, "Could not read blacklist for TA " + ta + " from " +
                                  "the database.");
            }
            _taToList.put(ta, list);
            
            JScrollPane jsp = new JScrollPane(list);
            jsp.setPreferredSize(new Dimension(560,50));
            
            panel.add(rb);
            panel.add(jsp);
            
            blacklistPanel.add(panel);
            
        }
        _taButtons.getElements().nextElement().setSelected(true);
        
        
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(200, DEFAULT_VIEW_HEIGHT));
        
        final JButton blacklistButton = new JButton("<< Add to Blacklist");
        blacklistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                add2BlacklistActionPerformed();
            }
        });
        blacklistButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    add2BlacklistActionPerformed();
                }
            }
        });
        
        JButton unBlacklistButton = new JButton("Remove From Blacklist >>");
        unBlacklistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeFromBlacklistActionPerformed();
            }
        });
       
        controlPanel.add(blacklistButton);
        controlPanel.add(unBlacklistButton);
        
        JPanel listPanel = new JPanel();
        listPanel.setPreferredSize(new Dimension(300, DEFAULT_VIEW_HEIGHT - 40));
        
        _filterBox = new JTextField();
        _filterBox.setPreferredSize(new Dimension(150,30));
        _filterBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String filterTerm = _filterBox.getText();
                List<Student> matchingLogins;
                //if no filter term, include all logins
                if(filterTerm.isEmpty()) {
                    matchingLogins = _studentLogins;
                }
                //otherwise compared against beginning of each login, lastname, and firstname
                else {
                    matchingLogins = new ArrayList<Student>();
                    for(Student student : _studentLogins){
                        if(student.getLogin().startsWith(filterTerm)){
                            matchingLogins.add(student);
                        }
                        else if(student.getLastName().toLowerCase().startsWith(filterTerm.toLowerCase())){
                            matchingLogins.add(student);
                        }
                        else if(student.getFirstName().toLowerCase().startsWith(filterTerm.toLowerCase())){
                            matchingLogins.add(student);
                        }
                    }
                }

                //display matching logins
                _studentList.setListData(matchingLogins);
                _studentList.setSelectedIndex(0);

                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (matchingLogins.get(0) != null) {
                        _filterBox.setText(matchingLogins.get(0).getLogin());
                        blacklistButton.requestFocus();
                    }
                }
            }
        });
        listPanel.add(_filterBox);
        
        _studentList = new GenericJList<Student>();
        _studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        Map<String, String> studentMap;
        try {
            studentMap = Allocator.getDatabaseIO().getAllStudents();
        } catch (SQLException ex) {
            new ErrorView(ex, "Could not read students from database.");
            studentMap = new HashMap<String, String>();
        }
        
        if (_studentLogins != null) {
            _studentLogins.clear();
        }
        else {
            _studentLogins = new ArrayList<Student>();
        }

        for (String studentLogin : studentMap.keySet()) {
            String[] studentName = studentMap.get(studentLogin).split(" ");
            _studentLogins.add(new Student(studentLogin, studentName[0], studentName[1]));
        }

        Collections.sort(_studentLogins);
        _studentList.setListData(_studentLogins);
        JScrollPane listPane = new JScrollPane(_studentList);
        listPane.setPreferredSize(listPanel.getPreferredSize());
        listPanel.add(listPane);
        
        this.add(blacklistScrollPane, BorderLayout.WEST);
        this.add(controlPanel, BorderLayout.CENTER);
        this.add(listPanel, BorderLayout.EAST);
        
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }

    private void add2BlacklistActionPerformed() {
        Collection<Student> students2Blacklist = _studentList.getGenericSelectedValues();
        TA ta = _rbToTA.get(_taButtons.getSelection());
        try {
            Allocator.getDatabaseIO().blacklistStudents(students2Blacklist, ta);
            ModifyBlacklistView.this.updateGUI();
            _filterBox.requestFocus();
            _filterBox.setText("");
        } catch (SQLException ex) {
            new ErrorView(ex, String.format("Could not add students: %s to TA %s's blacklist.", students2Blacklist, ta));
        }
    }

    private void removeFromBlacklistActionPerformed() {
        TA ta = _rbToTA.get(_taButtons.getSelection());
        Collection<String> students2Unblacklist = _taToList.get(ta).getGenericSelectedValues();
        try {
            Allocator.getDatabaseIO().unBlacklistStudents(students2Unblacklist, ta);
            ModifyBlacklistView.this.updateGUI();
        } catch (SQLException ex) {
            new ErrorView(ex, String.format("Could not remove the selected students: %s from TA %s's blacklist.", students2Unblacklist, ta));
        }
    }

    private void updateGUI() {
        TA ta = _rbToTA.get(_taButtons.getSelection());

        try {
            _taToList.get(ta).setListData(Allocator.getDatabaseIO().getTABlacklist(ta));
        } catch (SQLException ex) {
            new ErrorView(ex, "Could not get blacklist for TA " + ta + ".");
        }
    }
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ModifyBlacklistView(Allocator.getConfigurationInfo().getTAs());
            }
        });
    }
    
}
