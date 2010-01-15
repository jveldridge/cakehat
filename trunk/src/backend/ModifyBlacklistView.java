/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package backend;

import config.TA;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
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

    private Vector<JRadioButton> _radioButtons;
    private Vector<JList> _lists;
    private JTextField _filterBox;
    private String[] _studentLogins;
    private JList _studentList;
    
    public ModifyBlacklistView() {
        super("Modify Blacklist");
        this.setLayout(new BorderLayout());  
        
        JPanel blacklistPanel = new JPanel();
        blacklistPanel.setPreferredSize(new Dimension(665,1000));
        blacklistPanel.setLayout(new GridLayout(0,1));
        
        _radioButtons = new Vector<JRadioButton>();
        _lists = new Vector<JList>();
        _studentLogins = OldDatabaseOps.getStudentNames();
        
        Vector<TA> tas = new Vector<TA>(Allocator.getCourseInfo().getTAs());
        //tas.add(new TA("jeldridg",true,true,false));
        ButtonGroup taButtons = new ButtonGroup();
        for (TA ta : tas) {
            JPanel panel = new JPanel();
            panel.setPreferredSize(new Dimension(665,50));
            JRadioButton rb = new JRadioButton(ta.getLogin());
            _radioButtons.add(rb);
            rb.setPreferredSize(new Dimension(95,50));
            panel.add(rb);
            
            JList list = new JList();
            _lists.add(list);
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            list.setVisibleRowCount(0);
            list.setFixedCellWidth(75);
            try {
                list.setListData(OldDatabaseOps.getData("blacklist", "ta_blist_logins", ta.getLogin()).getString("studLogins").split(", "));
            }
            catch(Exception e) {}
            JScrollPane jsp = new JScrollPane(list);
            jsp.setPreferredSize(new Dimension(560,50));
            panel.add(jsp);
            
            blacklistPanel.add(panel);
                //taPanel.add(bp);
            taButtons.add(rb);
        }

        _radioButtons.firstElement().setSelected(true);
        
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(200,1000));
        
        JButton blacklistButton = new JButton("<< Add to Blacklist");
        JButton unBlacklistButton = new JButton("Remove From Blacklist >>");
        controlPanel.add(blacklistButton);
        controlPanel.add(unBlacklistButton);
        
        JPanel listPanel = new JPanel();
        listPanel.setPreferredSize(new Dimension(150,1000));
        
        _filterBox = new JTextField();
        _filterBox.setPreferredSize(new Dimension(150,30));
        _filterBox.addKeyListener(new KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterBoxKeyReleased(evt);
            }
        });
        listPanel.add(_filterBox);
        
        _studentList = new JList();
        _studentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        String[] logins = OldDatabaseOps.getStudentNames();
        Arrays.sort(logins);
        _studentList.setListData(logins);
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
    
    private void filterBoxKeyReleased(java.awt.event.KeyEvent evt) {
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
        }
    }
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ModifyBlacklistView();
            }
        });
    }
    
}
