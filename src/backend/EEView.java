/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * EEView.java
 *
 * Created on Jan 27, 2010, 5:41:04 PM
 */

package backend;

import components.calendar.CalendarListener;
import config.Assignment;
import config.HandinPart;
import config.Part;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import utils.Allocator;

/**
 *
 * @author jeldridg
 */
public class EEView extends javax.swing.JFrame {

    private Vector<Assignment> _asgns;
    private Vector<String> _students;
    private boolean _hasExtension;
    private static String[] MONTHS = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private int _maxYear = Integer.MAX_VALUE, _minYear = Integer.MIN_VALUE;
    private int _startYear;
    private Calendar _extensionDate;

    /** Creates new form EEView */
    public EEView(Collection<Assignment> asgns, Collection<String> students) {
        initComponents();

        _startYear = Allocator.getGeneralUtilities().getCurrentYear();

        _asgns = new Vector<Assignment>(asgns);
        _students = new Vector<String>(students);

        viewAsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) extCardPanel.getLayout();
                if (EEView.this.viewAsCombo.getSelectedItem().equals("View as Calendar")) {
                    cl.show(extCardPanel, "calendarCard");
                }
                else {
                    cl.show(extCardPanel, "listCard");
                }
            }
            
        });

        String studentLogin = "cs004000";
        HandinPart part = null;
        for (Assignment a : Allocator.getCourseInfo().getAssignments()) {
            if (a.getName().equals("Week1")) {
                part = a.getHandinPart();
            }
        }

        _extensionDate = Allocator.getDatabaseIO().getExtension(studentLogin, part);
        _hasExtension = (_extensionDate != null);
        if(_extensionDate == null)
        {
            _extensionDate = part.getTimeInformation().getOntimeDate();
        }
        int extYear = _extensionDate.get(Calendar.YEAR);
        int ontimeYear = part.getTimeInformation().getOntimeDate().get(Calendar.YEAR);
        _minYear = Math.min(extYear, ontimeYear);
        _maxYear = Math.max(extYear, ontimeYear) + 1;
        _startYear = extYear;


        calendar.addCalendarListener(new CalendarListener()
        {
            public void dateSelected(Calendar cal)
            {
                _extensionDate = cal;

                int month = cal.get(Calendar.MONTH);
                _monthBox.setSelectedIndex(month);

                int day = cal.get(Calendar.DAY_OF_MONTH);
                _dayBox.setSelectedItem(day);

                int year = cal.get(Calendar.YEAR);
                _yearBox.setSelectedItem(year);
            }
        });
        calendar.selectDate(_extensionDate);
        calendar.restrictYearRange(_minYear, _maxYear);

        _dayBox.setModel(new DefaultComboBoxModel(generateValues(1,31)));
        _dayBox.setSelectedItem(_extensionDate.get(Calendar.DAY_OF_MONTH));
        _dayBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
            }

        });

        _yearBox.setModel(new DefaultComboBoxModel(generateValues(_minYear, _maxYear)));
        _yearBox.setSelectedItem(_startYear);
        _yearBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
                updateDayBox();
            }
        });

        this.updateCalendar();

        this.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        exmViewByBG = new javax.swing.ButtonGroup();
        viewPane = new javax.swing.JPanel();
        viewSplitPane = new javax.swing.JSplitPane();
        viewExtensionsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        viewAsCombo = new javax.swing.JComboBox();
        extCardPanel = new javax.swing.JPanel();
        extAsCalPanel = new javax.swing.JPanel();
        calendar = new components.calendar.CalendarView();
        jScrollPane1 = new javax.swing.JScrollPane();
        extReasonTextArea = new javax.swing.JTextArea();
        _monthBox = new javax.swing.JComboBox(MONTHS);
        _dayBox = new javax.swing.JComboBox();
        _yearBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        _hourField = new components.IntegerField();
        _minuteField = new components.IntegerField();
        _secondField = new components.IntegerField();
        extAsListPanel = new javax.swing.JPanel();
        grantExtensionButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        viewExmPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        byAsgnRB = new javax.swing.JRadioButton();
        byStudRB = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setName("Form"); // NOI18N

        viewPane.setName("viewPane"); // NOI18N

        viewSplitPane.setName("viewSplitPane"); // NOI18N

        viewExtensionsPanel.setName("viewExtensionsPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(gradesystem.GradeSystemApp.class).getContext().getResourceMap(EEView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        viewAsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "View as Calendar", "View as List" }));
        viewAsCombo.setName("viewAsCombo"); // NOI18N

        extCardPanel.setName("extCardPanel"); // NOI18N
        extCardPanel.setLayout(new java.awt.CardLayout());

        extAsCalPanel.setName("extAsCalPanel"); // NOI18N

        calendar.setName("calendar"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        extReasonTextArea.setColumns(20);
        extReasonTextArea.setRows(5);
        extReasonTextArea.setText(resourceMap.getString("extReasonTextArea.text")); // NOI18N
        extReasonTextArea.setName("extReasonTextArea"); // NOI18N
        extReasonTextArea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                extReasonTextAreaMouseClicked(evt);
            }
        });
        extReasonTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                extReasonTextAreaFocusLost(evt);
            }
        });
        jScrollPane1.setViewportView(extReasonTextArea);

        _monthBox.setName("_monthBox"); // NOI18N

        _dayBox.setName("_dayBox"); // NOI18N

        _yearBox.setName("_yearBox"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        _hourField.setText(resourceMap.getString("_hourField.text")); // NOI18N
        _hourField.setName("_hourField"); // NOI18N

        _minuteField.setText(resourceMap.getString("_minuteField.text")); // NOI18N
        _minuteField.setName("_minuteField"); // NOI18N

        _secondField.setText(resourceMap.getString("_secondField.text")); // NOI18N
        _secondField.setName("_secondField"); // NOI18N

        javax.swing.GroupLayout extAsCalPanelLayout = new javax.swing.GroupLayout(extAsCalPanel);
        extAsCalPanel.setLayout(extAsCalPanelLayout);
        extAsCalPanelLayout.setHorizontalGroup(
            extAsCalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(extAsCalPanelLayout.createSequentialGroup()
                .addGroup(extAsCalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(extAsCalPanelLayout.createSequentialGroup()
                        .addComponent(_monthBox, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_dayBox, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_yearBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(_hourField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(_minuteField, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addGap(6, 6, 6)
                        .addComponent(_secondField, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(calendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        extAsCalPanelLayout.setVerticalGroup(
            extAsCalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(extAsCalPanelLayout.createSequentialGroup()
                .addComponent(calendar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(extAsCalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_monthBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_dayBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_yearBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(_secondField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_minuteField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(_hourField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(3, 3, 3)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        extCardPanel.add(extAsCalPanel, "calendarCard");

        extAsListPanel.setName("extAsListPanel"); // NOI18N

        javax.swing.GroupLayout extAsListPanelLayout = new javax.swing.GroupLayout(extAsListPanel);
        extAsListPanel.setLayout(extAsListPanelLayout);
        extAsListPanelLayout.setHorizontalGroup(
            extAsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 391, Short.MAX_VALUE)
        );
        extAsListPanelLayout.setVerticalGroup(
            extAsListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 412, Short.MAX_VALUE)
        );

        extCardPanel.add(extAsListPanel, "listCard");

        grantExtensionButton.setText(resourceMap.getString("grantExtensionButton.text")); // NOI18N
        grantExtensionButton.setName("grantExtensionButton"); // NOI18N
        grantExtensionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grantExtensionButtonActionPerformed(evt);
            }
        });

        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        javax.swing.GroupLayout viewExtensionsPanelLayout = new javax.swing.GroupLayout(viewExtensionsPanel);
        viewExtensionsPanel.setLayout(viewExtensionsPanelLayout);
        viewExtensionsPanelLayout.setHorizontalGroup(
            viewExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewExtensionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(extCardPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 391, Short.MAX_VALUE)
                    .addGroup(viewExtensionsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 172, Short.MAX_VALUE)
                        .addComponent(viewAsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(viewExtensionsPanelLayout.createSequentialGroup()
                        .addComponent(grantExtensionButton, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)))
                .addContainerGap())
        );
        viewExtensionsPanelLayout.setVerticalGroup(
            viewExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, viewExtensionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(viewAsCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(extCardPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 412, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(viewExtensionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(grantExtensionButton)
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        viewSplitPane.setLeftComponent(viewExtensionsPanel);

        viewExmPanel.setName("viewExmPanel"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        exmViewByBG.add(byAsgnRB);
        byAsgnRB.setText(resourceMap.getString("byAsgnRB.text")); // NOI18N
        byAsgnRB.setName("byAsgnRB"); // NOI18N

        exmViewByBG.add(byStudRB);
        byStudRB.setText(resourceMap.getString("byStudRB.text")); // NOI18N
        byStudRB.setName("byStudRB"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        javax.swing.GroupLayout viewExmPanelLayout = new javax.swing.GroupLayout(viewExmPanel);
        viewExmPanel.setLayout(viewExmPanelLayout);
        viewExmPanelLayout.setHorizontalGroup(
            viewExmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewExmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewExmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(viewExmPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(byAsgnRB)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(byStudRB))
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(270, Short.MAX_VALUE))
        );
        viewExmPanelLayout.setVerticalGroup(
            viewExmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(viewExmPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(viewExmPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(byAsgnRB)
                    .addComponent(byStudRB))
                .addContainerGap(445, Short.MAX_VALUE))
        );

        viewSplitPane.setRightComponent(viewExmPanel);

        javax.swing.GroupLayout viewPaneLayout = new javax.swing.GroupLayout(viewPane);
        viewPane.setLayout(viewPaneLayout);
        viewPaneLayout.setHorizontalGroup(
            viewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 966, Short.MAX_VALUE)
        );
        viewPaneLayout.setVerticalGroup(
            viewPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(viewSplitPane)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 6, Short.MAX_VALUE)
                .addComponent(viewPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 12, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 20, Short.MAX_VALUE)
                .addComponent(viewPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void grantExtensionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grantExtensionButtonActionPerformed
        JPanel panel = new JPanel();
        JComboBox asgns = new JComboBox();
        asgns.setModel(new DefaultComboBoxModel(_asgns));

        JComboBox studs = new JComboBox();
        studs.setModel(new DefaultComboBoxModel(_students));

        panel.add(asgns);
        panel.add(studs);

        System.out.println(this.getCalendar().getTime());
        
        if (JOptionPane.showConfirmDialog(null, panel,
                                            "Grant Extension",
                                            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String stud = (String) studs.getSelectedItem();
            Part part = ((Assignment) asgns.getSelectedItem()).getHandinPart();
            
            Allocator.getDatabaseIO().grantExtension(stud, part, this.getCalendar(), extReasonTextArea.getText());
        }
    }//GEN-LAST:event_grantExtensionButtonActionPerformed

    private void extReasonTextAreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extReasonTextAreaMouseClicked
        extReasonTextArea.setText("");
    }//GEN-LAST:event_extReasonTextAreaMouseClicked

    private void extReasonTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_extReasonTextAreaFocusLost
        if (extReasonTextArea.getText().equals("")) {
            extReasonTextArea.setText("To grant an extension, select a date on the calendar above," +
                    "\n enter an explanation of the extension here, then click\n" +
                    "'Grant Extension' and choose the student and assignment\n" +
                    "for which the extension should be granted.");
        }
    }//GEN-LAST:event_extReasonTextAreaFocusLost

        private JPanel createExtensionSelectionPanel()
    {
        FlowLayout layout = new FlowLayout();
        layout.setHgap(0);
        JPanel panel = new JPanel(layout);

        _monthBox = new JComboBox(MONTHS);
        //_monthBox.setSelectedIndex(_extensionDate.get(Calendar.MONTH));
        _monthBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
                updateDayBox();
            }

        });
        panel.add(_monthBox);

        _dayBox = new JComboBox(generateValues(1, 31));
        //_dayBox.setSelectedItem(_extensionDate.get(Calendar.DAY_OF_MONTH));
        _dayBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
            }

        });
        panel.add(_dayBox);

        _yearBox = new JComboBox(generateValues(_minYear, _maxYear));
        _yearBox.setSelectedItem(_startYear);
        _yearBox.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                updateCalendar();
                updateDayBox();
            }
        });
        panel.add(_yearBox);

        panel.add(Box.createRigidArea(new Dimension(10,5)));

        //_hourField = new IntegerField(_extensionDate.get(Calendar.HOUR_OF_DAY), 0, 23);
        panel.add(_hourField);

        panel.add(new JLabel(":"));

        //_minuteField = new IntegerField(_extensionDate.get(Calendar.MINUTE), 0, 59);
        panel.add(_minuteField);

        panel.add(new JLabel(":"));

        //_secondField = new IntegerField(_extensionDate.get(Calendar.SECOND), 0, 59);
        panel.add(_secondField);

        return panel;
    }

    private void updateCalendar()
    {
        calendar.selectDate(this.getComboBoxCal());
    }

    private Calendar getComboBoxCal()
    {
        int month = _monthBox.getSelectedIndex();
        int day = (Integer) _dayBox.getSelectedItem();
        int year = (Integer) _yearBox.getSelectedItem();

        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);

        return cal;
    }

    private Calendar getCalendar()
    {
        Calendar cal = getComboBoxCal();

        cal.set(Calendar.HOUR_OF_DAY, _hourField.getIntValue());
        cal.set(Calendar.MINUTE, _minuteField.getIntValue());
        cal.set(Calendar.SECOND, _secondField.getIntValue());

        return cal;
    }

    private Integer[] generateValues(int begin, int end)
    {
        Integer[] array = new Integer[end-begin+1];

        for(int i = 0; i < array.length; i++)
        {
            array[i] = begin + i;
        }

        return array;
    }

    private void updateDayBox()
    {
        int currValue = (Integer) _dayBox.getSelectedItem();

        Calendar cal = getComboBoxCal();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int nod = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
        ComboBoxModel model = new DefaultComboBoxModel(generateValues(1,nod));
        _dayBox.setModel(model);

        //If the selected value is an available selection
        if(currValue <= nod)
        {
            _dayBox.setSelectedItem(currValue);
        }
        //Otherwise just select the max value
        else
        {
            _dayBox.setSelectedIndex(model.getSize()-1);
        }
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EEView(Allocator.getCourseInfo().getAssignments(),
                        Allocator.getDatabaseIO().getEnabledStudents().keySet()).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox _dayBox;
    private components.IntegerField _hourField;
    private components.IntegerField _minuteField;
    private javax.swing.JComboBox _monthBox;
    private components.IntegerField _secondField;
    private javax.swing.JComboBox _yearBox;
    private javax.swing.JRadioButton byAsgnRB;
    private javax.swing.JRadioButton byStudRB;
    private components.calendar.CalendarView calendar;
    private javax.swing.ButtonGroup exmViewByBG;
    private javax.swing.JPanel extAsCalPanel;
    private javax.swing.JPanel extAsListPanel;
    private javax.swing.JPanel extCardPanel;
    private javax.swing.JTextArea extReasonTextArea;
    private javax.swing.JButton grantExtensionButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox viewAsCombo;
    private javax.swing.JPanel viewExmPanel;
    private javax.swing.JPanel viewExtensionsPanel;
    private javax.swing.JPanel viewPane;
    private javax.swing.JSplitPane viewSplitPane;
    // End of variables declaration//GEN-END:variables

}
