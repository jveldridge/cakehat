package backend;

import config.TA;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import utils.Allocator;

/**
 *
 * @author jveldridge
 */
public class ModifyBlacklistView extends JFrame{

    private JTextField _filterBox;
    private String[] _studentLogins;
    private JList _studentList;
    private ButtonGroup _taButtons;
    private Map<String,JList> _lists;
    
    public ModifyBlacklistView() {
        super("Modify Blacklist");
        this.setLayout(new BorderLayout());  
        
        JPanel blacklistPanel = new JPanel();
        blacklistPanel.setPreferredSize(new Dimension(665,1000));
        blacklistPanel.setLayout(new GridLayout(0,1));
        
        _lists = new HashMap<String,JList>();
        
        _taButtons = new ButtonGroup();
        for (TA ta : Allocator.getCourseInfo().getTAs()) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(665,50));
            
            JRadioButton rb = new JRadioButton(ta.getLogin());
            rb.setActionCommand(ta.getLogin());
            rb.setPreferredSize(new Dimension(95,50));
            rb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _filterBox.requestFocus();
                }
            });
            _taButtons.add(rb);
            
            JList list = new JList();
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
        
        
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(200,1000));
        
        final JButton blacklistButton = new JButton("<< Add to Blacklist");
        blacklistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Allocator.getDatabaseIO().blacklistStudent((String) _studentList.getSelectedValue(), 
                                                            _taButtons.getSelection().getActionCommand());
                ModifyBlacklistView.this.updateGUI();
            }
        });
        blacklistButton.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    Allocator.getDatabaseIO().blacklistStudent((String) _studentList.getSelectedValue(), 
                                                            _taButtons.getSelection().getActionCommand());
                    ModifyBlacklistView.this.updateGUI();
                    _filterBox.requestFocus();
                }
            }
        });
        
        JButton unBlacklistButton = new JButton("Remove From Blacklist >>");
        unBlacklistButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Allocator.getDatabaseIO().unBlacklistStudent((String) _lists.get(_taButtons.getSelection().getActionCommand()).getSelectedValue(),
                                                            _taButtons.getSelection().getActionCommand());
                ModifyBlacklistView.this.updateGUI();
            }
        });
       
        controlPanel.add(blacklistButton);
        controlPanel.add(unBlacklistButton);
        
        JPanel listPanel = new JPanel();
        listPanel.setPreferredSize(new Dimension(150,1000));
        
        _filterBox = new JTextField();
        _filterBox.setPreferredSize(new Dimension(150,30));
        _filterBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String filterTerm = _filterBox.getText();
                List<String> matchingLogins;
                //if no filter term, include all logins
                if(filterTerm.isEmpty()) {
                    matchingLogins = Arrays.asList(_studentLogins);
                }
                //otherwise compared against beginning of each login
                else {
                    matchingLogins = new Vector<String>();
                    for(String login : _studentLogins){
                        if(login.startsWith(filterTerm)){
                            matchingLogins.add(login);
                        }
                    }
                }

                //display matching logins
                _studentList.setListData(matchingLogins.toArray());
                _studentList.setSelectedIndex(0);

                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    _filterBox.setText(matchingLogins.get(0));
                    blacklistButton.requestFocus();
                }
            }
        });
        listPanel.add(_filterBox);
        
        _studentList = new JList();
        _studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        List<String> students = new LinkedList<String>(Allocator.getDatabaseIO().getAllStudents().keySet());
        _studentLogins = students.toArray(new String[0]);
        Arrays.sort(_studentLogins);
        _studentList.setListData(_studentLogins);
        JScrollPane listPane = new JScrollPane(_studentList);
        listPane.setPreferredSize(listPanel.getPreferredSize());
        listPanel.add(listPane);
        
        this.add(blacklistPanel, BorderLayout.WEST);
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
                new ModifyBlacklistView();
            }
        });
    }
    
}
