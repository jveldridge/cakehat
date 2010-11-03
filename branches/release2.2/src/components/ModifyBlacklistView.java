package components;

import config.TA;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
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
import utils.Allocator;

/**
 * Provides an interface to manage the students on each TA's blacklist.
 * 
 * @author jveldridge
 */
public class ModifyBlacklistView extends JFrame{

    private JTextField _filterBox;
    private Student[] _studentLogins;
    private GenericJList<Student> _studentList;
    private ButtonGroup _taButtons;
    private Map<String, JList> _lists;

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
        _lists = new HashMap<String, JList>();
        
        _taButtons = new ButtonGroup();
        for (TA ta : tas) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(665,50));
            
            final JRadioButton rb = new JRadioButton(ta.getLogin());
            rb.setActionCommand(ta.getLogin());
            rb.setPreferredSize(new Dimension(95,50));
            rb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _filterBox.requestFocus();
                }
            });
            _taButtons.add(rb);
            
            final JList list = new JList();
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    //clear the selections in any other JLists
                    int selectedIndex = list.getSelectedIndex();
                    for (Entry<String,JList> entry : _lists.entrySet()) {
                        entry.getValue().clearSelection(); 
                    }
                    list.setSelectedIndex(selectedIndex);

                    //select the radio button corresponding to the TA whose
                    //blacklist this JList represents
                    rb.setSelected(true);
                }
            });
            
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(0);
            list.setFixedCellWidth(75);
            list.setListData(Allocator.getDatabaseIO().getTABlacklist(ta.getLogin()).toArray(new String[0]));
            _lists.put(ta.getLogin(), list);
            
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
                Allocator.getDatabaseIO().blacklistStudent(_studentList.getSelectedValue().getLogin(),
                                                            _taButtons.getSelection().getActionCommand());
                ModifyBlacklistView.this.updateGUI();
            }
        });
        blacklistButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    Allocator.getDatabaseIO().blacklistStudent(_studentList.getSelectedValue().getLogin(),
                                                            _taButtons.getSelection().getActionCommand());
                    ModifyBlacklistView.this.updateGUI();
                    _filterBox.requestFocus();
                    _filterBox.setText("");
                }
            }
        });
        
        JButton unBlacklistButton = new JButton("Remove From Blacklist >>");
        unBlacklistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String taLogin = _taButtons.getSelection().getActionCommand();
                String studentLogin = (String) _lists.get(taLogin).getSelectedValue();
                Allocator.getDatabaseIO().unBlacklistStudent(studentLogin, taLogin);
                ModifyBlacklistView.this.updateGUI();
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
                    matchingLogins = Arrays.asList(_studentLogins);
                }
                //otherwise compared against beginning of each login
                else {
                    matchingLogins = new Vector<Student>();
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
                _studentList.setListData(matchingLogins.toArray());
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
        
        Map<String, String> studentMap = Allocator.getDatabaseIO().getAllStudents();
        
        List<Student> students = new LinkedList<Student>();

        for (String studentLogin : studentMap.keySet()) {
            String[] studentName = studentMap.get(studentLogin).split(" ");
            students.add(new Student(studentLogin, studentName[0], studentName[1]));
        }

        _studentLogins = students.toArray(new Student[0]);
        Arrays.sort(_studentLogins);
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
    
    
    private void updateGUI() {
        String taLogin = _taButtons.getSelection().getActionCommand();
        _lists.get(taLogin).setListData(Allocator.getDatabaseIO().getTABlacklist(taLogin).toArray(new String[0]));
    }
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ModifyBlacklistView(Allocator.getCourseInfo().getTAs());
            }
        });
    }
    
}
