package cakehat.views.grader;

import cakehat.Allocator;
import cakehat.database.TA;
import cakehat.database.Student;
import cakehat.logging.ErrorReporter;
import support.resources.icons.IconLoader;
import cakehat.services.ServicesException;
import cakehat.views.shared.StudentDescriptionProvider;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import support.ui.AlphaJPanel;
import support.ui.AlphaJScrollPane;
import support.ui.DocumentAdapter;
import support.ui.GenericJList;
import support.ui.ShadowJTextField;

/**
 * Panel which allows for blacklisting and unblacklisting of students for the TA running cakehat.
 *
 * @author jak2
 */
class BlacklistPanel extends AlphaJPanel
{
    private final JTextField _filterField;
    private final GenericJList<Student> _blacklistJList;
    private final GenericJList<Student> _nonblacklistJList;
    private final List<Student> _nonblacklistedStudents;
    private final TA _user;

    /**
     * Constructs the panel.
     *
     * @param size
     * @param background
     * @throws SQLException if the data needed for the initial state cannot be retrieved from the database
     */
    public BlacklistPanel(Dimension size, Color background) throws SQLException, ServicesException
    {
        this.setPreferredSize(size);

        this.setBackground(background);
        _user = Allocator.getUserServices().getUser();

        _filterField = new ShadowJTextField("Filter List");

        List<Student> blacklist = new ArrayList<Student>(Allocator.getDataServices().getBlacklist(_user));
        Collections.sort(blacklist);

        _nonblacklistedStudents = new ArrayList<Student>();
        _nonblacklistedStudents.addAll(Allocator.getDataServices().getStudents());
        _nonblacklistedStudents.removeAll(blacklist);
        Collections.sort(_nonblacklistedStudents);

        _blacklistJList = new GenericJList<Student>(blacklist, StudentDescriptionProvider.INSTANCE);
        _nonblacklistJList = new GenericJList<Student>(_nonblacklistedStudents, StudentDescriptionProvider.INSTANCE);

        this.initUI();
    }

    private void initUI()
    {
        this.setLayout(new BorderLayout(0, 0));

        // Various size constants used in laying out components
        Dimension size = this.getPreferredSize();

        final int listPanelWidth = (int) (size.width * .32);
        final int commandPanelWidth = size.width - 2 * listPanelWidth;
        final int labelHeight = 15;
        final int buttonHeight = 30;
        final int padWidth = 10;

        // Panel to hold the contents
        AlphaJPanel contentPanel = new AlphaJPanel(new BorderLayout(0, 0));
        Dimension contentSize = new Dimension(size.width, size.height);
        contentPanel.setPreferredSize(contentSize);
        contentPanel.setBackground(this.getBackground());
        this.add(contentPanel, BorderLayout.NORTH);

        // Black list
        AlphaJPanel blacklistPanel = new AlphaJPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        blacklistPanel.setPreferredSize(new Dimension(listPanelWidth, contentSize.height));
        blacklistPanel.setBackground(contentPanel.getBackground());

        JLabel blacklistLabel = new JLabel("Blacklisted Students");
        blacklistLabel.setPreferredSize(new Dimension(listPanelWidth, labelHeight));
        blacklistPanel.add(blacklistLabel);

        AlphaJScrollPane blacklistScrollPane = new AlphaJScrollPane(_blacklistJList);
        blacklistScrollPane.setPreferredSize(new Dimension(listPanelWidth, contentSize.height - labelHeight));
        blacklistPanel.add(blacklistScrollPane);

        contentPanel.add(blacklistPanel, BorderLayout.WEST);

        // Commands
        AlphaJPanel paddingPanel = new AlphaJPanel(new BorderLayout(0, 0));
        paddingPanel.setPreferredSize(new Dimension(commandPanelWidth, contentSize.height));
        paddingPanel.setBackground(contentPanel.getBackground());
        paddingPanel.add(Box.createRigidArea(new Dimension(padWidth, contentSize.height)), BorderLayout.WEST);
        paddingPanel.add(Box.createRigidArea(new Dimension(padWidth, contentSize.height)), BorderLayout.EAST);
        contentPanel.add(paddingPanel, BorderLayout.CENTER);

        AlphaJPanel commandPanel = new AlphaJPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        paddingPanel.add(commandPanel, BorderLayout.CENTER);
        commandPanel.setBackground(contentPanel.getBackground());

        // Gap space between top and blacklist/unblacklist buttons
        int buttonGap = 10;
        commandPanel.add(Box.createRigidArea(new Dimension(commandPanelWidth - 2 * padWidth,
                (contentSize.height - buttonGap - 2 * buttonHeight)/2)));

        // Command buttons
        AlphaJPanel blacklistButtonsPanel = new AlphaJPanel(new GridLayout(2, 1, 0, buttonGap));
        blacklistButtonsPanel.setPreferredSize(new Dimension(commandPanelWidth - 2 * padWidth,
                2 * buttonHeight + buttonGap));
        blacklistButtonsPanel.setBackground(contentPanel.getBackground());
        commandPanel.add(blacklistButtonsPanel);

        // Blacklist button
        Icon previousIcon = IconLoader.loadIcon(IconLoader.IconSize.s16x16, IconLoader.IconImage.GO_PREVIOUS);
        JButton blacklistButton = Allocator.getGeneralUtilities().createTextCenteredButton("Blacklist",
                previousIcon, blacklistButtonsPanel.getPreferredSize().width, true);
        blacklistButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                List<Student> toBlacklist = _nonblacklistJList.getGenericSelectedValues();

                try
                {
                    //Perform black list
                    Allocator.getDataServices().blacklistStudents(ImmutableSet.copyOf(toBlacklist), _user);

                    //Update black list
                    List<Student> allBlacklisted = new ArrayList<Student>();
                    allBlacklisted.addAll(toBlacklist);
                    allBlacklisted.addAll(_blacklistJList.getListData());
                    Collections.sort(allBlacklisted);

                    List<Student> selected = _blacklistJList.getGenericSelectedValues();
                    _blacklistJList.setListData(allBlacklisted);
                    _blacklistJList.setSelectedValues(selected);

                    //Update non-black list
                    _nonblacklistedStudents.removeAll(toBlacklist);
                    _nonblacklistJList.setListData(_nonblacklistedStudents);
                    applyFilterField();
                }
                catch(ServicesException ex)
                {
                    ErrorReporter.report("Unable to blacklist students", ex);
                }
            }
        });
        blacklistButtonsPanel.add(blacklistButton);

        // Unblacklist button
        Icon nextIcon = IconLoader.loadIcon(IconLoader.IconSize.s16x16, IconLoader.IconImage.GO_NEXT);
        JButton unblacklistButton = Allocator.getGeneralUtilities().createTextCenteredButton("Unblacklist",
                nextIcon, blacklistButtonsPanel.getPreferredSize().width, false);
        unblacklistButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                List<Student> toUnblacklist = _blacklistJList.getGenericSelectedValues();

                try
                {
                    Allocator.getDataServices().unBlacklistStudents(ImmutableSet.copyOf(toUnblacklist), _user);

                    //Update black list
                    List<Student> blacklisted = new ArrayList<Student>(_blacklistJList.getListData());
                    blacklisted.removeAll(toUnblacklist);
                    _blacklistJList.setListData(blacklisted);
                    
                    //Update non-black list
                    _nonblacklistedStudents.addAll(toUnblacklist);
                    Collections.sort(_nonblacklistedStudents);
                    List<Student> selected = _nonblacklistJList.getGenericSelectedValues();
                    _nonblacklistJList.setListData(_nonblacklistedStudents);
                    _nonblacklistJList.setSelectedValues(selected);
                    applyFilterField();
                }
                catch(ServicesException ex)
                {
                    ErrorReporter.report("Unable to unblacklist students", ex);
                }
            }
        });
        blacklistButtonsPanel.add(unblacklistButton);

        // Non-black list
        AlphaJPanel nonblacklistPanel = new AlphaJPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nonblacklistPanel.setPreferredSize(new Dimension(listPanelWidth, contentSize.height));
        nonblacklistPanel.setBackground(contentPanel.getBackground());

        JLabel nonblacklistLabel = new JLabel("Non-blacklisted Students");
        nonblacklistLabel.setPreferredSize(new Dimension(listPanelWidth, labelHeight));
        nonblacklistPanel.add(nonblacklistLabel);

        int filterFieldHeight = 20;
        _filterField.getDocument().addDocumentListener(new DocumentAdapter()
        {
            @Override
            public void modificationOccurred(DocumentEvent de)
            {
                applyFilterField();
            }
        });
        _filterField.setPreferredSize(new Dimension(listPanelWidth, filterFieldHeight));
        nonblacklistPanel.add(_filterField);

        AlphaJScrollPane nonblacklistScrollPane = new AlphaJScrollPane(_nonblacklistJList);
        nonblacklistScrollPane.setPreferredSize(new Dimension(listPanelWidth,
                contentSize.height - labelHeight - filterFieldHeight));
        nonblacklistPanel.add(nonblacklistScrollPane);
        
        contentPanel.add(nonblacklistPanel, BorderLayout.EAST);
    }

    /**
     * Applies the text in the filter field to the nonblacklisted students and then displays this in the nonblacklisted
     * jlist.
     */
    private void applyFilterField()
    {
        String filterText = _filterField.getText();

        List<Student> filteredLogins = new ArrayList<Student>();
        if(filterText != null && !filterText.isEmpty())
        {
            filterText = _filterField.getText().toLowerCase();

            for(Student student : _nonblacklistedStudents)
            {
                if(student.getLogin().toLowerCase().startsWith(filterText) ||
                   student.getFirstName().toLowerCase().startsWith(filterText) ||
                   student.getLastName().toLowerCase().startsWith(filterText) ||
                   student.getName().toLowerCase().startsWith(filterText))
                {
                    filteredLogins.add(student);
                }
            }
        }
        else
        {
            filteredLogins.addAll(_nonblacklistedStudents);
        }

        List<Student> selected = _nonblacklistJList.getGenericSelectedValues();
        _nonblacklistJList.setListData(filteredLogins);
        _nonblacklistJList.setSelectedValues(selected);
    }
}