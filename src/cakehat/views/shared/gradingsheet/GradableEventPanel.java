package cakehat.views.shared.gradingsheet;

import cakehat.Allocator;
import cakehat.newdatabase.DeadlineInfo;
import cakehat.newdatabase.DeadlineInfo.DeadlineResolution;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.newdatabase.Group;
import cakehat.newdatabase.HandinTime;
import cakehat.services.ServicesException;
import cakehat.views.shared.ErrorView;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

/**
 *
 * @author jak2
 */
class GradableEventPanel extends GradingSheetPanel
{
    private final GradableEvent _gradableEvent;
    private final Group _group;
    private final boolean _isAdmin;
    
    //Map from panel to if it has unsaved changes (true = unsaved changes)
    private final Map<PartPanel, Boolean> _partPanelSaveStatus = new HashMap<PartPanel, Boolean>();
    
    private double _totalEarned = 0;
    private double _totalOutOf = 0;
    
    private PenaltyOrBonusField _penaltyOrBonusField;
    
    GradableEventPanel(GradableEvent gradableEvent, Group group, boolean isAdmin)
    {
        super(Color.WHITE);
        
        _gradableEvent = gradableEvent;
        _group = group;
        _isAdmin = isAdmin;
        
        init();
    }
    
    private void init()
    {   
        try
        {
            DeadlineInfo deadlineInfo = Allocator.getDataServicesV5().getDeadlineInfo(_gradableEvent);
            
            DateTime receivedDate = null;
            if(_group != null)
            {
                if(_gradableEvent.hasDigitalHandins())
                {
                    File digitalHandin = _gradableEvent.getDigitalHandin(_group);
                    if(digitalHandin != null)
                    {
                        receivedDate = new DateTime(digitalHandin.lastModified());
                    }
                }
                else
                {
                    HandinTime handinTime = Allocator.getDataServicesV5().getHandinTime(_gradableEvent, _group);
                    if(handinTime != null)
                    {
                        receivedDate = handinTime.getHandinTime();
                    }
                }
            }
            
            initUI(deadlineInfo, receivedDate);
        }
        catch(ServicesException e)
        {
            new ErrorView(e, "Unable to retrieve deadline info or handin time.\n" +
                    "Gradable Event: " + _gradableEvent.getName() +
                    "Group: " + _group.getName());
            addErrorMessagePanel("Unable to retrieve deadline info");
        }
        catch(IOException e)
        {
            new ErrorView(e, "IO error when attempting to access digital handin.\n" +
                    "Gradable Event: " + _gradableEvent.getName() + "\n" +
                    "Group: " + _group.getName());
            addErrorMessagePanel("Unable to access student/group's digital handin");
        }
    }
    
    private void initUI(DeadlineInfo deadlineInfo, DateTime receivedTime)
    {
        addContent(createHeaderLabel(_gradableEvent.getName(), false));
        addContent(Box.createVerticalStrut(10));
        
        this.initDeadlineUI(deadlineInfo);
        
        if(deadlineInfo.getType() != DeadlineInfo.Type.NONE && _group != null)
        {
            this.initGroupDeadlineUI(deadlineInfo, receivedTime);
        }
        
        this.initPartsUI();
        
        if(_penaltyOrBonusField != null)
        {
            _totalEarned += _penaltyOrBonusField.updatePenaltyOrBonus(_totalEarned);
        }
    }
    
    private void initDeadlineUI(DeadlineInfo deadlineInfo)
    {
        addContent(createSubheaderLabel("Deadline Info", false));
        addContent(Box.createVerticalStrut(3));
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.shortDateTime();
        PeriodFormatter periodFormatter = PeriodFormat.getDefault();
        
        //Display info describing the deadlines for this gradable event
        DeadlineInfo.Type deadlineType = deadlineInfo.getType();
        if(deadlineType == DeadlineInfo.Type.NONE)
        {
            addContent(createContentLabel("No deadline specified", false, true));
        }
        else if(deadlineType == DeadlineInfo.Type.VARIABLE)
        {
            addContent(createContentLabel("On Time", false, true));
            String onTimeMessage = " • Before or on " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
            addContent(createContentLabel(onTimeMessage, false, false));
            
            addContent(Box.createVerticalStrut(3));
            
            if(deadlineInfo.getLateDate() != null)
            {
                addContent(createContentLabel("Late", false, true));
                String lateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate()) +
                        " and before or on " + dateTimeFormatter.print(deadlineInfo.getLateDate());
                addContent(createContentLabel(lateMessage, false, false));
                String lateDeductionMessage = " • " + deadlineInfo.getLatePoints() + " deducted at least once, and " +
                        "additionally every " + periodFormatter.print(deadlineInfo.getLatePeriod());
                addContent(createContentLabel(lateDeductionMessage, false, false));
                
                addContent(Box.createVerticalStrut(3));
                
                addContent(createContentLabel("NC Late", false, true));
                String ncLateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getLateDate());
                addContent(createContentLabel(ncLateMessage, false, false));
                String ncLateDeductionMessage = " • All earned points will be deducted";
                addContent(createContentLabel(ncLateDeductionMessage, false, false));
            }
            else
            {   
                addContent(createContentLabel("NC Late", false, true));
                String lateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
                addContent(createContentLabel(lateMessage, false, false));
                String ncLateDeductionMessage = " • All earned points will be deducted";
                addContent(createContentLabel(ncLateDeductionMessage, false, false));
            }
        }
        else if(deadlineType == DeadlineInfo.Type.FIXED)
        {   
            if(deadlineInfo.getEarlyDate() != null)
            {
                addContent(createContentLabel("Early", false, true));
                String earlyMessage = " • Before or on " + dateTimeFormatter.print(deadlineInfo.getEarlyDate());
                addContent(createContentLabel(earlyMessage, false, false));
                String earlyBonusMessage = " • " + deadlineInfo.getEarlyPoints() + " added";
                addContent(createContentLabel(earlyBonusMessage, false, false));
                
                addContent(Box.createVerticalStrut(3));
                
                addContent(createContentLabel("On Time", false, true));
                String onTimeMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getEarlyDate()) +
                        " and before or on " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
                addContent(createContentLabel(onTimeMessage, false, false));
            }
            else
            {
                addContent(createContentLabel("On Time", false, true));
                String onTimeMessage = " • Before or on " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
                addContent(createContentLabel(onTimeMessage, false, false));
            }
            
            addContent(Box.createVerticalStrut(3));
            
            if(deadlineInfo.getLateDate() != null)
            {
                addContent(createContentLabel("Late", false, true));
                String lateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate()) +
                        " and before or on " + dateTimeFormatter.print(deadlineInfo.getLateDate());
                addContent(createContentLabel(lateMessage, false, false));
                String lateDeductionMessage = " • " + deadlineInfo.getLatePoints() + " deducted";
                addContent(createContentLabel(lateDeductionMessage, false, false));
                
                addContent(Box.createVerticalStrut(3));
                
                addContent(createContentLabel("NC Late", false, true));
                String ncLateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getLateDate());
                addContent(createContentLabel(ncLateMessage, false, false));
                String ncLateDeductionMessage = " • All earned points will be deducted";
                addContent(createContentLabel(ncLateDeductionMessage, false, false));
            }
            else
            {
                addContent(createContentLabel("NC Late", false, true));
                String ncLateMessage = " • After " + dateTimeFormatter.print(deadlineInfo.getOnTimeDate());
                addContent(createContentLabel(ncLateMessage, false, false));
                String ncLateDeductionMessage = " • All earned points will be deducted";
                addContent(createContentLabel(ncLateDeductionMessage, false, false));
            }
        }
    }
    
    private void initGroupDeadlineUI(DeadlineInfo deadlineInfo, DateTime receivedTime)
    {
        addContent(createSubheaderLabel("Deadline Resolution", false));
        
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.shortDateTime(); 
        String receivedOn = "Received On: " + receivedTime == null ? "not received" : dateTimeFormatter.print(receivedTime);
        addContent(createContentLabel(receivedOn, false, true));
        
        DeadlineResolution deadlineResolution = deadlineInfo.apply(receivedTime, null, null);
        
        //Add a panel to show the resolution from the deadline
        JPanel resolutionPanel = new PreferredHeightPanel(new BorderLayout(0, 0), this.getBackground());
        addContent(resolutionPanel);
        
        //Text
        String resolutionStatus = deadlineResolution.getTimeStatus().toString();
        resolutionPanel.add(createContentLabel(resolutionStatus, false, false), BorderLayout.CENTER);
        
        //Points panel
        JPanel pointsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pointsPanel.setBackground(this.getBackground());
        resolutionPanel.add(pointsPanel, BorderLayout.EAST);
        
        //Spacing
        pointsPanel.add(Box.createHorizontalStrut(5));
        
        //Penalty or bonus
        _penaltyOrBonusField = new PenaltyOrBonusField(deadlineResolution);
        pointsPanel.add(_penaltyOrBonusField);
        
        //Spacing
        pointsPanel.add(Box.createHorizontalStrut(5));
        
        //Out of
        pointsPanel.add(createDisabledField(null));
    }
    
    private static class PenaltyOrBonusField extends JTextField
    {
        private final DeadlineResolution _resolution;
        private double _penaltyOrBonus;
        
        PenaltyOrBonusField(DeadlineResolution resolution)
        {
            super(5);
            
            _resolution = resolution;
            _penaltyOrBonus = 0;
            
            this.setEnabled(false);
            this.setHorizontalAlignment(JTextField.CENTER);
        }
        
        public double getPenaltyOrBonus()
        {
            return _penaltyOrBonus;
        }
        
        public double updatePenaltyOrBonus(double unadjustedGradableEventTotalEarned)
        {
            _penaltyOrBonus = _resolution.getPenaltyOrBonus(unadjustedGradableEventTotalEarned);
            this.setText(Double.toString(_penaltyOrBonus));
            
            return _penaltyOrBonus;
        }
    }
    
    private void initPartsUI()
    {
        for(int i = 0; i < _gradableEvent.getParts().size(); i++)
        {
            final Part part = _gradableEvent.getParts().get(i);
            
            final PartPanel panel = PartPanel.getPartPanel(part, _group, _isAdmin);
            
            _partPanelSaveStatus.put(panel, false);
            _totalEarned += panel.getEarned();
            _totalOutOf += panel.getOutOf();
            
            panel.addGradingSheetListener(new GradingSheetListener()
            {
                @Override
                public void earnedChanged(double prevEarned, double currEarned)
                {
                    double prevTotalEarned = _totalEarned;

                    _totalEarned -= prevEarned;
                    _totalEarned += currEarned;
                    
                    if(_penaltyOrBonusField != null)
                    {
                        _totalEarned -= _penaltyOrBonusField.getPenaltyOrBonus();
                        _totalEarned += _penaltyOrBonusField.updatePenaltyOrBonus(_totalEarned);
                    }

                    notifyEarnedChanged(prevTotalEarned, _totalEarned);
                    notifyUnsavedChangeOccurred();
                }
              
                @Override
                public void saveChanged(boolean hasUnsavedChanges)
                {
                    _partPanelSaveStatus.put(panel, hasUnsavedChanges);
                    
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
            
            panel.setAlignmentX(LEFT_ALIGNMENT);
            addContent(panel);
            if(i != _gradableEvent.getParts().size() - 1)
            {
                addContent(Box.createVerticalStrut(10));
            }
        }
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