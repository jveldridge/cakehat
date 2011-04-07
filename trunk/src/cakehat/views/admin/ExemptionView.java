package cakehat.views.admin;

import cakehat.config.Assignment;
import cakehat.config.Part;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import cakehat.Allocator;
import support.ui.GenericJComboBox;
import cakehat.database.Group;
import cakehat.views.shared.EmailView;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;

/**
 *
 * @author yf6
 */
class ExemptionView extends javax.swing.JFrame {

    private Assignment _assignment;
    private Collection<Part> _selectedParts;
    private Group _group;
    private GenericJComboBox<Group> _groupMenu;
    private JPanel _mainPanel, _west, _viewPanel;
    final static String VIEW_BY_STUD = "View By Student";
    final static String VIEW_BY_ASGN = "View By Assignment";

    public ExemptionView(Assignment asgn, Group group) {
        super("Manage Exemptions");
	this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        _assignment = asgn;
        _group = group;
	_selectedParts = new ArrayList<Part>();

        _groupMenu = new GenericJComboBox<Group>();

	_mainPanel = new JPanel(new BorderLayout());
	this.add(_mainPanel);

	this.initNorth();
	this.updateWest();
	this.initCenter();

	this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
	this.setVisible(true);
    }

    private void initNorth() {
        JPanel north = new JPanel();
        north.setBorder(BorderFactory.createLineBorder(Color.black));

        north.add(new JLabel("Select an assignment "));

	final GenericJComboBox<Assignment> asgnMenu =
                new GenericJComboBox<Assignment>(Allocator.getConfigurationInfo().getHandinAssignments());
        
        if (_assignment == null) {
            _assignment = asgnMenu.getItemAt(0);
        }

        asgnMenu.setGenericSelectedItem(_assignment);
        asgnMenu.addActionListener(new AsgnMenuListener(asgnMenu));

        Collection<Group> groups = Collections.emptyList();
        try {
            groups = Allocator.getDatabaseIO().getGroupsForAssignment(asgnMenu.getSelectedItem());
        } catch (SQLException ex) {
            new ErrorView(ex, "Could not get groups for assignment " + asgnMenu.getSelectedItem());
        }
        _groupMenu.setItems(groups);
        
	north.add(asgnMenu);
        _mainPanel.add(north, BorderLayout.NORTH);
    }

    private void updateWest() {
        JPanel west = new JPanel();
        west.setBorder(BorderFactory.createLineBorder(Color.black));
        Dimension dim = new Dimension(200,400);
        west.setPreferredSize(dim);
        west.setSize(dim);
	west.setLayout(new GridLayout(0,1));

	JLabel title = new JLabel("Grant Exemption");
	title.setFont(title.getFont().deriveFont(Font.BOLD, 14));
	west.add(title);

        JPanel selected = new JPanel(new GridLayout(0,1));
        JPanel groupP = new JPanel();
        groupP.add(new JLabel("Group:" ));
        if (_group == null) {
            _group = _groupMenu.getItemAt(0);
        }
        _groupMenu.setGenericSelectedItem(_group);
        _groupMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _group = _groupMenu.getSelectedItem();
                updateWest();
            }
        });
	groupP.add(_groupMenu);

        selected.add(groupP);
        west.add(selected);

        if (_assignment == null || _group == null){
            selected.add(new JLabel("assignment: --"));
        }
        else{
            selected.add(new JLabel("assignment: " + _assignment.getName()));

            JPanel partsPanel = new JPanel(new GridLayout(0,1));
            west.add(partsPanel);
            _selectedParts.clear();
            for (final Part part : _assignment.getParts()) {
                _selectedParts.add(part);
                final JCheckBox box = new JCheckBox(_assignment.getName() + ": " + part.getName());
                box.setSelected(true);
                box.setVisible(true);
                partsPanel.add(box);
                box.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (box.isSelected()) {
                            _selectedParts.add(part);
                        }
                        else {
                            _selectedParts.remove(part);
                        }
                    }
                });
            }
        }

        JPanel note = new JPanel(new BorderLayout());
        note.add(new JLabel("Notes:"), BorderLayout.NORTH);
        final JTextArea text = new JTextArea();
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        note.add(text, BorderLayout.CENTER);
        west.add(note);

        JPanel buttonP = new JPanel();
        //center the button in both directions
        buttonP.setLayout(new BoxLayout(buttonP, BoxLayout.PAGE_AXIS));
        JButton grant = new JButton("Grant");
        grant.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonP.add(Box.createVerticalGlue());
        buttonP.add(grant);
        buttonP.add(Box.createVerticalGlue());

        grant.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                new ConfirmationView(_group,text.getText());
            }
        });
        west.add(buttonP);

        if (_assignment == null || _group == null) {
            grant.setEnabled(false);
            text.setText("WARNING: Can only grant exemption when one assignment and one student are selected.");
            text.setFont(text.getFont().deriveFont(Font.BOLD, 14));
            text.setEditable(false);
        }

        //replace the old west panel with the new one
        if (_west != null) {
            _mainPanel.remove(_west);
        }
        _mainPanel.add(west, BorderLayout.WEST);
        _mainPanel.validate();
        _west = west;
    }//updateWest


    private void initCenter() {
        JPanel center = new JPanel();
        center.setBorder(BorderFactory.createLineBorder(Color.black));
        Dimension dim = new Dimension(500,400);
        center.setPreferredSize(dim);
        center.setSize(dim);
	center.setLayout(new BorderLayout());

        _viewPanel = new JPanel();
        this.updateViewByAsgn();

        JScrollPane scrollView = new JScrollPane(_viewPanel);
	scrollView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scrollView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

	center.add(scrollView, BorderLayout.CENTER);

	JPanel header = new JPanel();
	header.add(new JLabel("View Exemptions for Assignment: " + _assignment.getName()));

	center.add(header, BorderLayout.NORTH);

        _mainPanel.add(center, BorderLayout.CENTER);
    }//initCenter

    private void updateViewByAsgn() {
        _viewPanel.removeAll();

        GroupLayout asgnLayout = new GroupLayout(_viewPanel);
        _viewPanel.setLayout(asgnLayout);
        ParallelGroup pg = asgnLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        SequentialGroup sg = asgnLayout.createSequentialGroup();

        Map<Part, Collection<Group>> exemptions = null;
        try {
            exemptions = Allocator.getDatabaseIO().getAllExemptions(_assignment);
        } catch (SQLException ex) {
            new ErrorView(ex, "Could not get exemptions for assignment " + _assignment);
        }
	for (final Part part : _assignment.getParts()) {
            JLabel partLab = new JLabel("   " + part.getName());
            pg.addComponent(partLab, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE);
            sg.addComponent(partLab, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
            sg.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);

            if (exemptions.containsKey(part)) {
                for (final Group group : exemptions.get(part)) {
                    String note = null;
                    try {
                        note = Allocator.getDatabaseIO().getExemptionNote(group, part);
                    } catch (SQLException ex) {
                        new ErrorView(ex, "Could not get exemption note for group "
                                        + group + " on part " + part + ".");
                    }

                    JPanel groupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    JButton delete = new JButton("Delete");
                    delete.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            int resp = JOptionPane.showConfirmDialog(ExemptionView.this,
                                    "Do you really want to delete the exemption?",
                                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                            if (resp != JOptionPane.YES_OPTION) {
                                return;
                            }
                            try {
                                Allocator.getDatabaseIO().removeExemption(group, part);
                            } catch (SQLException ex) {
                                new ErrorView(ex, "Could not remove exemption for group "
                                        + group + " on part " + part + ".");
                            }
                            updateViewByAsgn();
                        }
                    });
                    groupPanel.add(delete);
                    groupPanel.add(Box.createHorizontalStrut(15));
                    JLabel groupLab = new JLabel(group.getName() + ": " + note);
                    pg.addComponent(groupPanel, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE);
                    groupPanel.add(groupLab);

                    sg.addComponent(groupPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
                    sg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
                }
            }
        }

        asgnLayout.setHorizontalGroup(pg);
        asgnLayout.setVerticalGroup(asgnLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(sg));
    }//updateViewByAsgn()


     private class AsgnMenuListener implements ActionListener{
        private GenericJComboBox<Assignment> _asgnMenu;
        public AsgnMenuListener(GenericJComboBox<Assignment> asgnMenu){
            _asgnMenu = asgnMenu;
        }
        public void actionPerformed(ActionEvent e) {
            Collection<Group> groups = Collections.emptyList();
            try {
                groups = Allocator.getDatabaseIO().getGroupsForAssignment(_asgnMenu.getSelectedItem());
            } catch (SQLException ex) {
                new ErrorView(ex, "Could not get groups for assignment " + _asgnMenu.getSelectedItem());
            }

            if (_asgnMenu.getSelectedItem().hasGroups() && groups.isEmpty()) {
                JOptionPane.showMessageDialog(ExemptionView.this, "No group has been created yet.");
                return;
            }

            _assignment = _asgnMenu.getSelectedItem();
            updateViewByAsgn();
            _groupMenu.setItems(groups);
            updateWest();
        }
    }


    /**
     * The confirmation window pops up when clicking Grant in the main window
     */
    private class ConfirmationView extends JFrame{

        private Group _group;
        private String _notes;
        private JCheckBox _emailCheck;

    	public ConfirmationView(Group group, String notes){
            super("Grant Confirmation?");

            _group = group;
            _notes = notes;

            JPanel panel = new JPanel(new GridLayout(0,1));
            this.add(panel);

            panel.add(new JLabel("Grant Exemption to"));
            panel.add(new JLabel("     " + group.getName()));
            panel.add(new JLabel("on the following:"));
            panel.add(new JLabel("   " + _assignment.getName()));
            for (Part p : _selectedParts){
                panel.add(new JLabel("    > " + p.getName()));
            }
            panel.add(new JLabel("Note:"));
            JTextArea noteText = new JTextArea(notes);
            noteText.setEditable(false);
            noteText.setLineWrap(true);
            noteText.setWrapStyleWord(true);
            panel.add(noteText);

            _emailCheck = new JCheckBox("Email Student & cc HTAs");
            _emailCheck.setSelected(true);
            panel.add(_emailCheck);

            JPanel buttonPanel = new JPanel();
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new CancelListener(this));
            buttonPanel.add(cancelButton);

            JButton confirmButton = new JButton("Confirm");
            confirmButton.addActionListener(new ConfirmListener(this));
            buttonPanel.add(confirmButton);
            panel.add(buttonPanel);

            Dimension dim = new Dimension(300,400);
            panel.setSize(dim);
            panel.setPreferredSize(dim);

            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.pack();
            this.setResizable(true);
            this.setLocationRelativeTo(null);
            this.setVisible(true);
    	}

        private class ConfirmListener implements ActionListener{
            private ConfirmationView _confirmationView;
            private ConfirmListener(ConfirmationView cv){
                _confirmationView = cv;
            }
            public void actionPerformed(ActionEvent arg0) {
                for (Part p : _selectedParts) {
                    try {
                        Allocator.getDatabaseIO().grantExemption(_group, p, _notes);
                    } catch (SQLException ex) {
                        new ErrorView(ex, "Could not grant exemption for group "
                                        + _group + " on part " + p + ".");
                    }
                }
                if (_emailCheck.isSelected()){
                    String body = "You have been granted an exemption on the following: ";
                    for (Part p : _selectedParts){
                        body = body + "\n";
                        body = body + _assignment.getName() + " - " + p.getName();
                    }
                    new EmailView(_group.getMembers(), Allocator.getConfigurationInfo().getNotifyAddresses(),
                                    "[" + Allocator.getCourseInfo().getCourse() + "] " + _assignment.getName() + " Exemption Granted",
                                    body, "granted exemption", null).setVisible(true);

                }
                updateViewByAsgn();
                _confirmationView.dispose();
            }
        }

        private class CancelListener implements ActionListener{

            private ConfirmationView _confirmationView;
            private CancelListener(ConfirmationView cv){
                _confirmationView = cv;
            }
            public void actionPerformed(ActionEvent arg0) {
                _confirmationView.dispose();
            }
        }
    }
}