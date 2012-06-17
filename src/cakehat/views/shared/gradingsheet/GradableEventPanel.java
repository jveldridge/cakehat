package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.database.DeadlineInfo;
import cakehat.database.DeadlineInfo.DeadlineResolution;
import cakehat.database.Extension;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.database.Group;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;
import support.ui.DateTimeControl;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
class GradableEventPanel extends GradingSheetPanel
{
    private final GradableEvent _gradableEvent;
    private final Set<Part> _partsToFullyShow;
    private final boolean _fullyShowingAllParts;
    private final Group _group;
    private final boolean _isAdmin;
    private final boolean _submitOnSave;
    
    //Map from panel to if it has unsaved changes (true = unsaved changes)
    private final Map<PartPanel, Boolean> _partPanelSaveStatus = new HashMap<PartPanel, Boolean>();
    
    private final List<Component> _focusableComponents = new ArrayList<Component>();
    
    private double _totalEarned = 0;
    private double _totalOutOf = 0;
    
    private GroupDeadlineResolutionPanel _groupDeadlineResolutionPanel;
    
    GradableEventPanel(GradableEvent gradableEvent, Set<Part> partsToFullyShow, Group group,
                       boolean isAdmin, boolean submitOnSave, boolean showBorder)
    {
        super(Color.WHITE, showBorder);
        
        _gradableEvent = gradableEvent;
        _partsToFullyShow = partsToFullyShow;
        _fullyShowingAllParts = _partsToFullyShow.containsAll(_gradableEvent.getParts());
        _group = group;
        _isAdmin = isAdmin;
        _submitOnSave = submitOnSave;
        
        init();
    }
    
    private void init()
    {   
        try
        {
            DeadlineInfo deadlineInfo = Allocator.getDataServices().getDeadlineInfo(_gradableEvent);
            
            DateTime occurrenceDate = null;
            Extension extension = null;
            if(_group != null)
            {
                occurrenceDate = Allocator.getGradingServices().getOccurrenceDates(_gradableEvent, ImmutableSet.of(_group)).get(_group);
                extension = Allocator.getDataServices().getExtensions(_gradableEvent, ImmutableSet.of(_group)).get(_group);
            }
            
            initUI(deadlineInfo, occurrenceDate, extension);
        }
        catch(ServicesException e)
        {
            ErrorReporter.report("Unable to retrieve deadline, occurrence time, or extension info.\n" +
                    "Gradable Event: " + _gradableEvent.getName() + "\n" +
                    "Group: " + _group.getName(), e);
            addErrorMessagePanel("Unable to retrieve deadline related info");
        }
    }
    
    private void initUI(DeadlineInfo deadlineInfo, DateTime receivedTime, Extension extension)
    {
        addContent(FormattedLabel.asHeader(_gradableEvent.getName()));
        
        addContent(Box.createVerticalStrut(10));
        
        this.initDeadlineUI(deadlineInfo);
        
        addContent(Box.createVerticalStrut(10));
        
        if(deadlineInfo.getType() != DeadlineInfo.Type.NONE && _group != null)
        {
            if(_isAdmin)
            {
                this.initAdminGroupExtensionUI(deadlineInfo, extension);
            }
            else
            {
                this.initViewGroupExtensionUI(extension);
            }
            
            this.initGroupDeadlineResolutionUI(deadlineInfo, receivedTime, extension);
        }
        
        this.initPartsUI();
        
        if(_groupDeadlineResolutionPanel != null)
        {
            _groupDeadlineResolutionPanel.notifyTotalEarnedChanged(false);
        }
    }
    
    private void initDeadlineUI(DeadlineInfo deadlineInfo)
    {
        addContent(FormattedLabel.asSubheader("Deadline Info"));
        addContent(Box.createVerticalStrut(3));
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.shortDateTime();
        PeriodFormatter periodFormatter = PeriodFormat.getDefault();
        
        //Display info describing the deadlines for this gradable event
        DeadlineInfo.Type deadlineType = deadlineInfo.getType();
        if(deadlineType == DeadlineInfo.Type.NONE)
        {
            addContent(FormattedLabel.asContent("No deadline specified"));
        }
        else if(deadlineType == DeadlineInfo.Type.VARIABLE)
        {
            addContent(FormattedLabel.asContent("On Time"));
            String onTimeMessage = " • Before or on " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
            addContent(FormattedLabel.asContent(onTimeMessage).usePlainFont());
            
            addContent(Box.createVerticalStrut(3));
            
            if(deadlineInfo.getLateDate() != null)
            {
                addContent(FormattedLabel.asContent("Late"));
                String lateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate()) +
                        " and before or on " + dateTimeFormatter.print(deadlineInfo.getLateDate());
                addContent(FormattedLabel.asContent(lateMessage));
                String lateDeductionMessage = " • " + deadlineInfo.getLatePoints() + " deducted at least once, and " +
                        "additionally every " + periodFormatter.print(deadlineInfo.getLatePeriod());
                addContent(FormattedLabel.asContent(lateDeductionMessage).usePlainFont());
                
                addContent(Box.createVerticalStrut(3));
                
                addContent(FormattedLabel.asContent("NC Late"));
                String ncLateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getLateDate());
                addContent(FormattedLabel.asContent(ncLateMessage).usePlainFont());
                String ncLateDeductionMessage = " • All earned points will be deducted";
                addContent(FormattedLabel.asContent(ncLateDeductionMessage).usePlainFont());
            }
            else
            {   
                addContent(FormattedLabel.asContent("NC Late"));
                String lateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
                addContent(FormattedLabel.asContent(lateMessage).usePlainFont());
                String ncLateDeductionMessage = " • All earned points will be deducted";
                addContent(FormattedLabel.asContent(ncLateDeductionMessage).usePlainFont());
            }
        }
        else if(deadlineType == DeadlineInfo.Type.FIXED)
        {   
            if(deadlineInfo.getEarlyDate() != null)
            {
                addContent(FormattedLabel.asContent("Early"));
                String earlyMessage = " • Before or on " + dateTimeFormatter.print(deadlineInfo.getEarlyDate());
                addContent(FormattedLabel.asContent(earlyMessage).usePlainFont());
                String earlyBonusMessage = " • " + deadlineInfo.getEarlyPoints() + " added";
                addContent(FormattedLabel.asContent(earlyBonusMessage).usePlainFont());
                
                addContent(Box.createVerticalStrut(3));
                
                addContent(FormattedLabel.asContent("On Time"));
                String onTimeMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getEarlyDate()) +
                        " and before or on " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
                addContent(FormattedLabel.asContent(onTimeMessage).usePlainFont());
            }
            else
            {
                addContent(FormattedLabel.asContent("On Time"));
                String onTimeMessage = " • Before or on " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
                addContent(FormattedLabel.asContent(onTimeMessage).usePlainFont());
            }
            
            addContent(Box.createVerticalStrut(3));
            
            if(deadlineInfo.getLateDate() != null)
            {
                addContent(FormattedLabel.asContent("Late"));
                String lateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate()) +
                        " and before or on " + dateTimeFormatter.print(deadlineInfo.getLateDate());
                addContent(FormattedLabel.asContent(lateMessage).usePlainFont());
                String lateDeductionMessage = " • " + deadlineInfo.getLatePoints() + " deducted";
                addContent(FormattedLabel.asContent(lateDeductionMessage).usePlainFont());
                
                addContent(Box.createVerticalStrut(3));
                
                addContent(FormattedLabel.asContent("NC Late"));
                String ncLateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getLateDate());
                addContent(FormattedLabel.asContent(ncLateMessage).usePlainFont());
                String ncLateDeductionMessage = " • All earned points will be deducted";
                addContent(FormattedLabel.asContent(ncLateDeductionMessage).usePlainFont());
            }
            else
            {
                addContent(FormattedLabel.asContent("NC Late"));
                String ncLateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
                addContent(FormattedLabel.asContent(ncLateMessage).usePlainFont());
                String ncLateDeductionMessage = " • All earned points will be deducted";
                addContent(FormattedLabel.asContent(ncLateDeductionMessage).usePlainFont());
            }
        }
    }
    
    private void initViewGroupExtensionUI(Extension extension)
    {
        if(extension != null)
        {
            addContent(FormattedLabel.asSubheader("Extension"));
            
            addContent(FormattedLabel.asContent("On Time"));
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.shortDateTime();
            String extensionMessage = " • Before or on " + dateTimeFormatter.print(extension.getNewOnTime());
            addContent(FormattedLabel.asContent(extensionMessage).usePlainFont());
            if(extension.getShiftDates())
            {
                addContent(FormattedLabel.asContent(" • Other deadlines shifted").usePlainFont());
            }
            else
            {
                addContent(FormattedLabel.asContent(" • Other deadlines ignored").usePlainFont());
            }
            
            String note = extension.getNote();
            if(note != null)
            {
                addContent(FormattedLabel.asContent("Note"));
                addContent(FormattedLabel.asContent(note).usePlainFont());
            }
            
            //Vertical spacing
            addContent(Box.createVerticalStrut(10));    
        }
    }
    
    private void initAdminGroupExtensionUI(final DeadlineInfo deadlineInfo, Extension extension)
    {
        //Button to revoke or grant extension
        final JButton extensionButton = new JButton(extension == null ? "Grant Extension" : "Revoke Extension");
        
        //Panel holding extension controls, the panel can be hidden / made visible as needed
        final JPanel extensionPanel = new PreferredHeightPanel(this.getBackground());
        extensionPanel.setVisible(extension != null);
        
        //Extension date time control
        DateTime extensionDate = extension == null ? null : extension.getNewOnTime();
        final DateTimeControl dateTimeControl = new DateTimeControl(extensionDate);
        dateTimeControl.setBackground(this.getBackground());
        
        //Check box for whether extension dates are shifted
        final JCheckBox shiftDatesCheckBox = new JCheckBox();
        shiftDatesCheckBox.setBackground(this.getBackground());
        shiftDatesCheckBox.setSelected(extension == null || (extension != null && extension.getShiftDates()));
        
        //Area for a note explaining the extension
        final JTextArea noteArea = new JTextArea();
        if(extension != null && extension.getNote() != null)
        {
            noteArea.setText(extension.getNote());
        }
        noteArea.setRows(4);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        JScrollPane noteScrollPane = new JScrollPane(noteArea);
        noteScrollPane.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        noteScrollPane.setAlignmentX(LEFT_ALIGNMENT);
        
        //Logic
        extensionButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                DateTime dateTime = dateTimeControl.getDateTime();
                
                //In this state the extension button says 'Grant Extension'
                //Currently no extension, set extension to current on time and show controls
                if(dateTime == null)
                {
                    try
                    {
                        DateTime defaultExtensionDateTime = deadlineInfo.getOnTimeDate();
                        Allocator.getDataServices().setExtensions(_gradableEvent, ImmutableSet.of(_group),
                                defaultExtensionDateTime, true, null);
                        _groupDeadlineResolutionPanel.updateExtension(defaultExtensionDateTime, true);
                        
                        extensionButton.setText("Revoke Extension");
                        noteArea.setText("");
                        dateTimeControl.setDateTime(defaultExtensionDateTime, true);
                        shiftDatesCheckBox.setSelected(true);
                        
                        extensionPanel.setVisible(true);
                    }
                    catch(ServicesException e)
                    {
                        ErrorReporter.report("Could not grant extension", e);
                    }
                }
                //In this state the extension button says 'Revoke Extension'
                //Currently an extension, remove it and hide controls
                else
                {
                    try
                    {
                        Allocator.getDataServices().deleteExtensions(_gradableEvent, ImmutableSet.of(_group));
                        _groupDeadlineResolutionPanel.updateExtension(null, null);
                        
                        extensionButton.setText("Grant Extension");
                        dateTimeControl.setDateTime(null, true);
                        noteArea.setText("");
                        shiftDatesCheckBox.setSelected(true);
                        
                        extensionPanel.setVisible(false);
                    }
                    catch(ServicesException e)
                    {
                        ErrorReporter.report("Could not revoke extension", e);
                    }
                }
            }
        });
        
        dateTimeControl.addDateTimeChangeListener(new DateTimeControl.DateTimeChangeListener()
        {
            @Override
            public void dateTimeChanged(DateTime prevDateTime, DateTime newDateTime)
            {
                try
                {
                    Allocator.getDataServices().setExtensions(_gradableEvent, ImmutableSet.of(_group), newDateTime,
                            shiftDatesCheckBox.isSelected(), noteArea.getText());
                    _groupDeadlineResolutionPanel.updateExtension(newDateTime, shiftDatesCheckBox.isSelected());
                }
                catch(ServicesException e)
                {
                    dateTimeControl.setDateTime(prevDateTime, true);
                    ErrorReporter.report("Unable to update extension to new date and time", e);
                }
            }
        });
        
        shiftDatesCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                boolean selected = shiftDatesCheckBox.isSelected();
                
                try
                {
                    Allocator.getDataServices().setExtensions(_gradableEvent, ImmutableSet.of(_group),
                            dateTimeControl.getDateTime(), selected, noteArea.getText());
                    _groupDeadlineResolutionPanel.updateExtension(dateTimeControl.getDateTime(), selected);
                }
                catch(ServicesException e)
                {
                    shiftDatesCheckBox.setSelected(selected);
                    ErrorReporter.report("Unable to update extension shift dates property", e);
                }
            }
        });
        
        noteArea.addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent fe)
            {
                String note = noteArea.getText();
                
                try
                {
                    Allocator.getDataServices().setExtensions(_gradableEvent, ImmutableSet.of(_group),
                            dateTimeControl.getDateTime(), shiftDatesCheckBox.isSelected(), note);
                }
                catch(ServicesException e)
                {
                    noteArea.setText(note);
                    ErrorReporter.report("Unable to update extension note", e);
                }
            }
        });
        
        //Layout
        addContent(FormattedLabel.asSubheader("Extension"));
        
        extensionPanel.setLayout(new BoxLayout(extensionPanel, BoxLayout.Y_AXIS));
        addContent(extensionPanel);
        
        JPanel datePanel = new PreferredHeightPanel(this.getBackground());
        datePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        extensionPanel.add(datePanel);
        datePanel.add(FormattedLabel.asContent("On Time:"));
        datePanel.add(Box.createHorizontalStrut(23));
        datePanel.add(dateTimeControl);
        
        extensionPanel.add(Box.createVerticalStrut(5));
        
        JPanel shiftPanel = new PreferredHeightPanel(this.getBackground());
        shiftPanel.setToolTipText("<html>Check to shift all deadline dates relative to the new on time date." +
                                  "<br/>Otherwise only this new on time date will be used, and any other" +
                                  "<br/>dates from the original deadline will be ignored.</html>");
        shiftPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        extensionPanel.add(shiftPanel);
        shiftPanel.add(FormattedLabel.asContent("Shift Dates:"));
        shiftPanel.add(Box.createHorizontalStrut(3));
        shiftPanel.add(shiftDatesCheckBox);
        
        extensionPanel.add(Box.createVerticalStrut(5));
        
        extensionPanel.add(noteScrollPane);
        
        addContent(Box.createVerticalStrut(5));
        
        extensionButton.setAlignmentX(LEFT_ALIGNMENT);
        addContent(extensionButton);
        
        //Vertical spacing
        addContent(Box.createVerticalStrut(10));
    }
    
    private void initGroupDeadlineResolutionUI(DeadlineInfo deadlineInfo, DateTime receivedTime, Extension extension)
    {
        addContent(FormattedLabel.asSubheader("Deadline Resolution"));
        
        _groupDeadlineResolutionPanel = new GroupDeadlineResolutionPanel(this.getBackground(), deadlineInfo,
                receivedTime, extension == null ? null : extension.getNewOnTime(),
                extension == null ? null : extension.getShiftDates());
        addContent(_groupDeadlineResolutionPanel);
        
        addContent(Box.createVerticalStrut(10));
    }
    
    private class GroupDeadlineResolutionPanel extends JPanel
    {
        private double _currPenaltyOrBonus;
        private final JLabel _receivedLabel, _resolutionStatusLabel;
        private final DateTimeControl _receivedControl;
        private final JButton _receivedButton;
        private final JTextField _penaltyOrBonusField;
        
        private final DeadlineInfo _deadlineInfo;
        
        private DateTime _currReceivedTime;
        private DateTime _currExtensionTime;
        private Boolean _currShiftDates;
        
        GroupDeadlineResolutionPanel(Color background, DeadlineInfo deadlineInfo, DateTime receivedTime,
                DateTime extensionTime, Boolean shiftDates)
        {
            _currPenaltyOrBonus = 0;
            
            _deadlineInfo = deadlineInfo;
            
            _currReceivedTime = receivedTime;
            _currExtensionTime = extensionTime;
            _currShiftDates = shiftDates;
            
            //Create UI elements
            _receivedLabel = FormattedLabel.asContent("").usePlainFont();
            _receivedControl = new DateTimeControl(receivedTime);
            _receivedButton = new JButton();
            _resolutionStatusLabel = FormattedLabel.asContent("").usePlainFont();
            _penaltyOrBonusField = new JTextField(5);
            _penaltyOrBonusField.setEnabled(false);
            _penaltyOrBonusField.setHorizontalAlignment(JTextField.CENTER);
            
            //Visually add and layout UI elements
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBackground(background);
            
            //Add a panel to show the received date
            JPanel receivedPanel = new PreferredHeightPanel(new FlowLayout(FlowLayout.LEFT, 0, 0), this.getBackground());
            addContent(receivedPanel);
            receivedPanel.add(_receivedLabel);
            
            //If admin, and either the gradable event does not have digital handins or the group being shown does not
            //a digital handin then allow for modification of the gradable event occurrence date time
            if(_isAdmin && (!_gradableEvent.hasDigitalHandins() || receivedTime == null))
            {
                //UI
                //Horizontally align receivedControl with the control for the extension ate
                receivedPanel.add(Box.createHorizontalStrut(22));
                
                _receivedControl.setBackground(this.getBackground());
                _receivedControl.setAlignmentX(LEFT_ALIGNMENT);
                receivedPanel.add(_receivedControl);
                
                addContent(Box.createVerticalStrut(5));
                
                addContent(_receivedButton);
                
                addContent(Box.createVerticalStrut(5));
                
                //Logic
                _receivedButton.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent ae)
                    {
                        receivedButtonActionPerformed();
                    }
                });
                
                _receivedControl.addDateTimeChangeListener(new DateTimeControl.DateTimeChangeListener()
                {
                    @Override
                    public void dateTimeChanged(DateTime prevDateTime, DateTime newDateTime)
                    {
                        try
                        {
                            Allocator.getDataServices().setGradableEventOccurrences(_gradableEvent,
                                    ImmutableMap.of(_group, newDateTime));
                            updateReceived(newDateTime);
                        }
                        catch(ServicesException e)
                        {
                            _receivedControl.setDateTime(prevDateTime, true);
                            ErrorReporter.report("Unable to set new occurrence date", e);
                        }
                    }
                });
            }
            
            //Add a panel to show the resolution from the deadline
            JPanel resolutionPanel = new PreferredHeightPanel(new BorderLayout(0, 0), this.getBackground());
            addContent(resolutionPanel);

            //Text
            resolutionPanel.add(_resolutionStatusLabel, BorderLayout.CENTER);

            //Only show the points impact of the deadline resolution if showing all of the parts for the gradable event
            //This is necessary because when the resolution is NC Late the amount of points deducted is equal to the
            //total earned amount of points for the gradable event and that info will not be available/current/reliable
            //when not all of the parts are shown
            if(_fullyShowingAllParts)
            {
                //Points panel
                JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                pointsPanel.setBackground(this.getBackground());
                resolutionPanel.add(pointsPanel, BorderLayout.EAST);

                //Spacing
                pointsPanel.add(Box.createHorizontalStrut(5));

                //Penalty or bonus
                pointsPanel.add(_penaltyOrBonusField);

                //Spacing
                pointsPanel.add(Box.createHorizontalStrut(5));

                //Out of
                pointsPanel.add(createDisabledField(null));
            }
            
            //Apply initial values
            update(receivedTime, extensionTime, shiftDates);
        }
        
        private void receivedButtonActionPerformed()
        {
            try
            {
                DateTime newReceivedOn;
                if(_currReceivedTime == null)
                {
                    newReceivedOn = new DateTime().millisOfSecond().setCopy(0);
                    Allocator.getDataServices().setGradableEventOccurrences(_gradableEvent,
                            ImmutableMap.of(_group, newReceivedOn));
                }
                else
                {
                    newReceivedOn = null;
                    Allocator.getDataServices().deleteGradableEventOccurrences(_gradableEvent, ImmutableSet.of(_group));
                }
                
                _receivedControl.setDateTime(newReceivedOn, true);
                updateReceived(newReceivedOn);
            }
            catch(ServicesException e)
            {
                ErrorReporter.report("Unable to update occurrence date", e);
            }
        }
        
        void updateReceived(DateTime receivedTime)
        {
            _currReceivedTime = receivedTime;
            
            update(receivedTime, _currExtensionTime, _currShiftDates);
        }
        
        void updateExtension(DateTime extensionTime, Boolean shiftDates)
        {
            _currExtensionTime = extensionTime;
            _currShiftDates = shiftDates;
            
            update(_currReceivedTime, extensionTime, shiftDates);
        }
        
        private void update(DateTime receivedTime, DateTime extensionTime, Boolean shiftDates)
        {   
            //Show updated received on
            if(_isAdmin)
            {
                _receivedLabel.setText("<strong>Received:</strong>");
                _receivedControl.setDateTime(receivedTime, true);
                if(receivedTime == null)
                {
                    _receivedButton.setText("Set");
                }
                else
                {
                    _receivedButton.setText("Remove");
                }
            }
            else
            {
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.shortDateTime(); 
                String receivedOnText = "<strong>Received:</strong> " +
                         (receivedTime == null ? "Date and time unknown" : dateTimeFormatter.print(receivedTime));
                _receivedLabel.setText(receivedOnText);
            }
            
            //Show updated deadline resolution
            DeadlineResolution resolution = _deadlineInfo.apply(receivedTime, extensionTime, shiftDates);
            _resolutionStatusLabel.setText("<strong>Status:</strong> " + resolution.getTimeStatus());
            updatePenaltyOrBonus(resolution, true);
        }
        
        void notifyTotalEarnedChanged(boolean notify)
        {
            updatePenaltyOrBonus(_deadlineInfo.apply(_currReceivedTime, _currExtensionTime, _currShiftDates), notify);
        }
        
        private void updatePenaltyOrBonus(DeadlineResolution resolution, boolean notify)
        {
            //Do not perform when all parts are not shown as in the NC Late case this value cannot be calculated
            //correctly - the UI for this value is also not show in this case
            if(_fullyShowingAllParts)
            {
                //Subtract out the current penalty or bonus, use raw total earned to calculate new penalty or bonus,
                //then add that to the raw total earned
                double prevTotalEarned = _totalEarned;
                _totalEarned -= _currPenaltyOrBonus;
                _currPenaltyOrBonus = resolution.getPenaltyOrBonus(_totalEarned);
                _totalEarned += _currPenaltyOrBonus;

                //Visually update
                _penaltyOrBonusField.setText(Double.toString(_currPenaltyOrBonus));

                //Notify
                if(notify && prevTotalEarned != _totalEarned)
                {
                    notifyEarnedChanged(prevTotalEarned, _totalEarned);
                }
            }
        }
    }
    
    private void initPartsUI()
    {
        for(int i = 0; i < _gradableEvent.getParts().size(); i++)
        {
            Part part = _gradableEvent.getParts().get(i);
            
            final PartPanel partPanel;
            if(_partsToFullyShow.contains(part))
            {
                partPanel = PartPanel.getPartPanel(part, _group, _isAdmin, _submitOnSave, true);
                _focusableComponents.addAll(partPanel.getFocusableComponents());

                _partPanelSaveStatus.put(partPanel, false);
                _totalEarned += partPanel.getEarned();
                _totalOutOf += partPanel.getOutOf();

                partPanel.addGradingSheetListener(new GradingSheetListener()
                {
                    @Override
                    public void earnedChanged(double prevEarned, double currEarned)
                    {
                        double prevTotalEarned = _totalEarned;

                        _totalEarned -= prevEarned;
                        _totalEarned += currEarned;

                        if(_groupDeadlineResolutionPanel != null)
                        {
                            _groupDeadlineResolutionPanel.notifyTotalEarnedChanged(false);
                        }

                        notifyEarnedChanged(prevTotalEarned, _totalEarned);
                        notifyUnsavedChangeOccurred();
                    }

                    @Override
                    public void saveChanged(boolean hasUnsavedChanges)
                    {
                        _partPanelSaveStatus.put(partPanel, hasUnsavedChanges);

                        if(hasUnsavedChanges)
                        {
                            notifyUnsavedChangeOccurred();
                        }
                        else
                        {
                            boolean allSaved = true;
                            for(boolean unsavedChanges : _partPanelSaveStatus.values())
                            {
                                allSaved = allSaved && !unsavedChanges;
                            }

                            if(allSaved)
                            {
                                notifySavedSuccessfully();
                            }
                        }
                    };
                });
            }
            else
            {
                partPanel = new PartInfoPanel(part, _group, true);
            }
            
            //Visually add
            partPanel.setAlignmentX(LEFT_ALIGNMENT);
            addContent(partPanel);
            if(i != _gradableEvent.getParts().size() - 1)
            {
                addContent(Box.createVerticalStrut(10));
            }
        }
    }
    
    @Override
    List<Component> getFocusableComponents()
    {
        return _focusableComponents;
    }

    @Override
    public double getEarned()
    {
        return _totalEarned;
    }

    @Override
    public double getOutOf()
    {
        return _totalOutOf;
    }

    @Override
    public void save()
    {
        for(PartPanel panel : _partPanelSaveStatus.keySet())
        {
            if(panel.hasUnsavedChanges())
            {
                panel.save();
            }
        }
    }
}