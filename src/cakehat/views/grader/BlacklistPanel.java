package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.database.Student;
import cakehat.database.TA;
import cakehat.logging.ErrorReporter;
import cakehat.services.ServicesException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import support.ui.DescriptionProvider;
import support.ui.DnDApprover;
import support.ui.DnDList;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
class BlacklistPanel extends JPanel
{
    private final TA USER = Allocator.getUserServices().getUser();
    private final DnDList<Student> _blacklistList;
    private final DnDList<Student> _nonBlacklistList;
    
    BlacklistPanel()
    {
        _blacklistList = new DnDList<Student>();
        _nonBlacklistList = new DnDList<Student>();

        try
        {   
            this.loadData();
            this.initUI();
            this.initTransferLogic();
        }
        catch(ServicesException e)
        {
            this.initErrorUI();
            ErrorReporter.report("Unable to load blacklist info", e);
        }
    }
    
    private void loadData() throws ServicesException
    {
        //Blacklisted students
        List<Student> blacklistedStudents = new ArrayList<Student>(Allocator.getDataServices().getBlacklist(USER));
        Collections.sort(blacklistedStudents);
        _blacklistList.setListData(blacklistedStudents);
        
        //Non-blacklisted students
        List<Student> nonBlacklistedStudents = new ArrayList<Student>();
        nonBlacklistedStudents.addAll(Allocator.getDataServices().getStudents());
        nonBlacklistedStudents.removeAll(blacklistedStudents);
        _nonBlacklistList.setListData(nonBlacklistedStudents);
    }
    
    private void initUI()
    {   
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        //Create an equally space content panel with buffer room on each side
        this.add(Box.createHorizontalStrut(100));
        JPanel contentPanel = new JPanel(new GridLayout(1, 3));
        this.add(contentPanel);
        this.add(Box.createHorizontalStrut(100));
        
        //Blacklist students
        JPanel blacklistPanel = new JPanel(new BorderLayout(0, 5));
        blacklistPanel.add(FormattedLabel.asHeader("Your Blacklist"), BorderLayout.NORTH);
        _blacklistList.setDescriptionProvider(new StudentDescriptionProvider());
        blacklistPanel.add(new JScrollPane(_blacklistList), BorderLayout.CENTER);
        contentPanel.add(blacklistPanel);
        
        //Spacing
        contentPanel.add(Box.createHorizontalBox());
        
        //Non-blacklisted students
        JPanel nonBlacklistPanel = new JPanel(new BorderLayout(0, 5));
        nonBlacklistPanel.add(FormattedLabel.asHeader("Other Students"), BorderLayout.NORTH);
        _nonBlacklistList.setDescriptionProvider(new StudentDescriptionProvider());
        nonBlacklistPanel.add(new JScrollPane(_nonBlacklistList), BorderLayout.CENTER);
        contentPanel.add(nonBlacklistPanel);
    }
    
    private void initErrorUI()
    {
        this.setLayout(new BorderLayout(0, 0));
        this.add(FormattedLabel.asHeader("Unable to load blacklist info").showAsErrorMessage(), BorderLayout.CENTER);
    }
    
    private void initTransferLogic()
    {
        //Allow dragging and dropping between the two lists
        _blacklistList.addDnDSource(_nonBlacklistList);
        _nonBlacklistList.addDnDSource(_blacklistList);
        
        //Add an approver that performs the data services call to blacklist a student
        //It will be allowed if the call succeeds
        _blacklistList.setDnDApprover(new DnDApprover<Student>()
        {
            @Override
            public boolean canAddValues(Map<Integer, Student> toAdd)
            {
                boolean success;
                try
                {
                    Allocator.getDataServices().blacklistStudents(new HashSet<Student>(toAdd.values()), USER);
                    success = true;
                }
                catch(ServicesException e)
                {
                    ErrorReporter.report("Cannot blacklist students\n" +
                            "TA: " + USER + "\n" + 
                            "Students: " + toAdd.values(), e);
                    success = false;
                }
                
                return success;
            }

            @Override
            public boolean canRemoveValues(Map<Integer, Student> toRemove)
            {
                return true;
            }

            @Override
            public boolean canReorderValues(Map<Integer, Student> toReorder)
            {
                return false;
            }
        });
        
        //Add an approve that performs the data services call to unblacklist a student
        //It will be allowed if the call succeeds
        _nonBlacklistList.setDnDApprover(new DnDApprover<Student>()
        {
            @Override
            public boolean canAddValues(Map<Integer, Student> toAdd)
            {
                boolean success;
                try
                {
                    Allocator.getDataServices().unBlacklistStudents(new HashSet<Student>(toAdd.values()), USER);
                    success = true;
                }
                catch(ServicesException e)
                {
                    ErrorReporter.report("Cannot unblacklist students\n" +
                            "TA: " + USER + "\n" + 
                            "Students: " + toAdd.values(), e);
                    success = false;
                }
                
                return success;
            }

            @Override
            public boolean canRemoveValues(Map<Integer, Student> toRemove)
            {
                return true;
            }

            @Override
            public boolean canReorderValues(Map<Integer, Student> toReorder)
            {
                return false;
            }
        });
    }
    
    private static class StudentDescriptionProvider implements DescriptionProvider<Student>
    {
        @Override
        public String getDisplayText(Student student)
        {
            String displayText = "";
            if(student != null)
            {
                displayText = student.getLogin() + " (" + student.getName() + ")";
            }
            
            return displayText;
        }

        @Override
        public String getToolTipText(Student student)
        {
            String toolTip = null;
            if(student != null)
            {
                toolTip = student.getName();
            }
            
            return toolTip;
        }
    }
}