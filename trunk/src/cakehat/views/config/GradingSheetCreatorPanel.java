package cakehat.views.config;

import cakehat.Allocator;
import cakehat.database.DbGradingSheet;
import cakehat.database.DbGradingSheetDetail;
import cakehat.database.DbGradingSheetSection;
import cakehat.database.DbGradingSheetSubsection;
import cakehat.database.DbPart;
import cakehat.database.Orderable;
import cakehat.logging.ErrorReporter;
import cakehat.views.config.ValidationResult.ValidationState;
import com.google.common.collect.ImmutableSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import support.ui.FixedWidthJPanel;
import support.ui.FormattedLabel;
import support.ui.PaddingPanel;
import support.ui.PreferredHeightJPanel;

/**
 *
 * @author jak2
 */
class GradingSheetCreatorPanel extends JPanel
{
    private static final String WORKER_TAG = "GRADING_SHEET";
    private final UniqueElementSingleThreadWorker _worker;
    private final DbPart _part;
    private final JPanel _contentPanel;
    private final JPanel _headerPanel;
    
    GradingSheetCreatorPanel(UniqueElementSingleThreadWorker worker, DbPart part)
    {
        _worker = worker;
        _part = part;
        
        _contentPanel = new FixedWidthJPanel();
        _contentPanel.setLayout(new BorderLayout(0, 0));
        
        this.setLayout(new BorderLayout(0, 0));
        
        _headerPanel = new JPanel(new BorderLayout(0, 0));
        
        this.add(new PaddingPanel(_headerPanel, 10, 10, 5, 5), BorderLayout.NORTH);
        this.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
        this.add(Box.createHorizontalStrut(5), BorderLayout.EAST);
        this.add(Box.createHorizontalStrut(5), BorderLayout.WEST);
        
        JScrollPane contentScrollPane = new JScrollPane(_contentPanel);
        contentScrollPane.setBorder(null);
        this.add(contentScrollPane, BorderLayout.CENTER);
        
        //Load data
        this.initialize();
    }
    
    private void initialize()
    {
        _contentPanel.removeAll();
        
        JLabel loadingLabel = FormattedLabel.asHeader("Initializing...");
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        _contentPanel.add(loadingLabel, BorderLayout.CENTER);
        
        JProgressBar loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        _contentPanel.add(loadingBar, BorderLayout.SOUTH);
        
        _worker.submit(null, new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    _contentPanel.removeAll();
                    _headerPanel.removeAll();
                    
                    _headerPanel.add(FormattedLabel.asHeader(_part.getName() + " Grading Sheet"), BorderLayout.WEST);
                    _headerPanel.add(Box.createHorizontalBox(), BorderLayout.CENTER);
        
                    //Retrieve the grading sheet, and if none exists yet - create one
                    DbGradingSheet gradingSheet = Allocator.getDatabase().getGradingSheet(_part);
                    if(gradingSheet == null)
                    {
                        gradingSheet = DbGradingSheet.build(_part);
                        Allocator.getDatabase().putGradingSheets(ImmutableSet.of(gradingSheet));
                    }
                    final DbGradingSheet finalGradingSheet = gradingSheet;
                    
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            _contentPanel.add(new GradingSheetPanel(finalGradingSheet), BorderLayout.CENTER);
                            
                            //Force visual update to reflect these changes
                            _contentPanel.repaint();
                            _contentPanel.revalidate();
                            _headerPanel.repaint();
                            _headerPanel.revalidate();
                        }
                    });
                }
                catch(final SQLException e)
                {
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            _contentPanel.removeAll();
                            _headerPanel.removeAll();
                            
                            //Force visual update to reflect these changes
                            _contentPanel.repaint();
                            _contentPanel.revalidate();
                            
                            ErrorReporter.report(e);
                        }
                    });
                }
            }
        });
    }
    
    private abstract class ReinitializeRunnable implements Runnable
    {
        public void run()
        {
            try
            {
                dbCall();
            }
            catch(SQLException e)
            {
                //Cancel everything and re-initialize
                _worker.cancel(WORKER_TAG);

                initialize();

                ErrorReporter.report(dbFailureMessage(), e);
            }
        }
        
        public abstract void dbCall() throws SQLException;
        
        public abstract String dbFailureMessage();
    }

    private class GradingSheetPanel extends JPanel implements OrderableController<DbGradingSheetSection>
    {
        private final DbGradingSheet _gradingSheet;
        private final OrderableControllerHelper<DbGradingSheetSection> _helper;
        
        GradingSheetPanel(DbGradingSheet sheet)
        {   
            _gradingSheet = sheet;
            
            this.setLayout(new BorderLayout(0, 0));
            
            _helper = new OrderableControllerHelper<DbGradingSheetSection>("Section", this.getBackground(),
                    new Color(225, 225, 225))
             {

                @Override
                Set<DbGradingSheetSection> getChildren()
                {
                    return _gradingSheet.getSections();
                }

                @Override
                DbGradingSheetSection createChild(int order)
                {
                    return DbGradingSheetSection.build(_gradingSheet, "Section " + order, order, null);
                }

                @Override
                ReorderablePanel createChildPanel(DbGradingSheetSection child, Color background)
                {
                    return new SectionPanel(child, GradingSheetPanel.this, background);
                }

                @Override
                void putChild(DbGradingSheetSection child) throws SQLException
                {
                    Allocator.getDatabase().putGradingSheetSections(ImmutableSet.of(child));
                }
            };
            this.add(_helper.getChildrenPanel(), BorderLayout.CENTER);
         
            //Add to the header panel the add section button
            _headerPanel.add(_helper.getAddChildButton(), BorderLayout.EAST);
        }
        
        @Override
        public void putOrderables(Set<DbGradingSheetSection> sections) throws SQLException
        {
            Allocator.getDatabase().putGradingSheetSections(sections);
        }

        @Override
        public void removeOrderables(Set<DbGradingSheetSection> sections) throws SQLException
        {
            Allocator.getDatabase().removeGradingSheetSections(sections);
        }

        @Override
        public void removeFromParent(DbGradingSheetSection section)
        {
            _gradingSheet.removeSection(section);
            _helper.visuallyRemoveChild(section);
        }

        @Override
        public void relayout()
        {
            _helper.relayout();
        }
    }
    
    private class SectionPanel extends ReorderablePanel<DbGradingSheetSection>
        implements OrderableController<DbGradingSheetSubsection>
    {        
        private final DbGradingSheetSection _section;
        private final OrderableControllerHelper<DbGradingSheetSubsection> _helper;
        
        SectionPanel(DbGradingSheetSection section, OrderableController<DbGradingSheetSection> controller,
                Color background)
        {
            super(section, controller, background);
            
            _section = section;
            
            this.setLeftComponent(createNameField());
            
            _helper = new OrderableControllerHelper<DbGradingSheetSubsection>("Subsection", background,
                    new Color(212, 212, 212))
             {
                @Override
                Set<DbGradingSheetSubsection> getChildren()
                {
                    return _section.getSubsections();
                }

                @Override
                DbGradingSheetSubsection createChild(int order)
                {
                    return DbGradingSheetSubsection.build(_section, "Subsection " + order, order, 0.0);
                }

                @Override
                ReorderablePanel createChildPanel(DbGradingSheetSubsection child, Color background)
                {
                    return new SubsectionPanel(child, SectionPanel.this, background);
                }

                @Override
                void putChild(DbGradingSheetSubsection child) throws SQLException
                {
                    Allocator.getDatabase().putGradingSheetSubsections(ImmutableSet.of(child));
                }
            };
            
            this.setBottomComponent(_helper.getChildrenPanel());
            this.setControlPanelLeftComponent(_helper.getAddChildButton());
        }
        
        private ValidatingTextField createNameField()
        {
            ValidatingTextField nameField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _section.getName();
                }

                @Override
                protected ValidationResult validate(String value)
                {
                    return ValidationResult.NO_ISSUE;
                }

                @Override
                protected void applyChange(String newName)
                {
                    _section.setName(newName);
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putGradingSheetSections(ImmutableSet.of(_section));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to update name of section";
                        }
                    });
                }
            };
            
            nameField.setBorder(BorderFactory.createEtchedBorder());
            
            return nameField;
        }

        @Override
        public void putOrderables(Set<DbGradingSheetSubsection> subsections) throws SQLException
        {
            Allocator.getDatabase().putGradingSheetSubsections(subsections);
        }

        @Override
        public void removeOrderables(Set<DbGradingSheetSubsection> subsections) throws SQLException
        {
            Allocator.getDatabase().removeGradingSheetSubsections(subsections);
        }

        @Override
        public void removeFromParent(DbGradingSheetSubsection subsection)
        {
            _section.removeSubsection(subsection);
            _helper.visuallyRemoveChild(subsection);
        }

        @Override
        public void relayout()
        {
            _helper.relayout();
        }
    }
    
    private class SubsectionPanel extends ReorderablePanel<DbGradingSheetSubsection>
        implements OrderableController<DbGradingSheetDetail>
    {
        private final DbGradingSheetSubsection _subsection;
        private final OrderableControllerHelper<DbGradingSheetDetail> _helper;
        
        SubsectionPanel(DbGradingSheetSubsection subsection,
                OrderableController<DbGradingSheetSubsection> controller, Color background)
        {
            super(subsection, controller, background);
            
            _subsection = subsection;
            
            JPanel leftPanel = new JPanel(new BorderLayout(0, 0));
            leftPanel.add(createTextField(), BorderLayout.CENTER);
            leftPanel.add(createOutOfField(), BorderLayout.EAST);
            this.setLeftComponent(leftPanel);
            
            _helper = new OrderableControllerHelper<DbGradingSheetDetail>("Detail", background,
                    new Color(200, 200, 200))
             {
                 @Override
                Set<DbGradingSheetDetail> getChildren()
                {
                    return _subsection.getDetails();
                }

                @Override
                DbGradingSheetDetail createChild(int order)
                {
                    return DbGradingSheetDetail.build(_subsection, "Detail " + order, order);
                }

                @Override
                ReorderablePanel createChildPanel(DbGradingSheetDetail child, Color background)
                {
                    return new DetailPanel(child, SubsectionPanel.this, background);
                }

                @Override
                void putChild(DbGradingSheetDetail child) throws SQLException
                {
                    Allocator.getDatabase().putGradingSheetDetails(ImmutableSet.of(child));
                }
            };
            this.setBottomComponent(_helper.getChildrenPanel());
            this.setControlPanelLeftComponent(_helper.getAddChildButton());
        }
        
        private JTextArea createTextField()
        {
            final JTextArea textArea = new JTextArea(_subsection.getText());
            textArea.setRows(1);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createEtchedBorder());
            
            textArea.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent fe)
                {
                    _subsection.setText(textArea.getText());

                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putGradingSheetSubsections(ImmutableSet.of(_subsection));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to update text of subsection";
                        }
                    });
                }
            });
            
            return textArea;
        }
        
        private JComponent createOutOfField()
        {
            ValidatingTextField outOfField = new ValidatingTextField()
            {
                @Override
                protected String getDbValue()
                {
                    return _subsection.getOutOf() == null ? "" : Double.toString(_subsection.getOutOf());
                }
                    
                @Override
                protected ValidationResult validate(String value)
                {
                    ValidationResult result;
                    if(value.isEmpty())
                    {
                        result = new ValidationResult(ValidationState.ERROR, "Value must be specified");
                    }
                    else
                    {
                        try
                        {
                            double outOf = Double.parseDouble(value);
                            if(outOf < 0)
                            {
                                result = new ValidationResult(ValidationState.ERROR, "Value may not be negative");
                            }
                            else
                            {
                                result = ValidationResult.NO_ISSUE;
                            }
                        }
                        catch(NumberFormatException e)
                        {
                            result = new ValidationResult(ValidationState.ERROR, "Numerical value not provided");
                        }
                    }
                    
                    return result;
                }
                
                @Override
                protected void applyChange(String newValue)
                {
                    _subsection.setOutOf(newValue.isEmpty() ? null : Double.parseDouble(newValue));
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putGradingSheetSubsections(ImmutableSet.of(_subsection));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to update text of subsection";
                        }
                    });
                }
                
                @Override
                public Dimension getMaximumSize()
                {
                    Dimension size = getPreferredSize();
                    size.width = Short.MAX_VALUE;

                    return size;
                }
            };
            
            outOfField.setBorder(null);
            outOfField.setColumns(4);
            outOfField.setHorizontalAlignment(JTextField.RIGHT);
            
            JPanel outOfPanel = new JPanel();
            outOfPanel.setLayout(new BoxLayout(outOfPanel, BoxLayout.Y_AXIS));
            outOfPanel.add(outOfField);
            outOfPanel.setBackground(outOfField.getBackground());
            outOfPanel.setBorder(BorderFactory.createEtchedBorder());
            
            return outOfPanel;
        }

        @Override
        public void putOrderables(Set<DbGradingSheetDetail> details) throws SQLException
        {
            Allocator.getDatabase().putGradingSheetDetails(details);
        }

        @Override
        public void removeOrderables(Set<DbGradingSheetDetail> details) throws SQLException
        {
            Allocator.getDatabase().removeGradingSheetDetails(details);
        }

        @Override
        public void removeFromParent(DbGradingSheetDetail detail)
        {
            _subsection.removeDetail(detail);
            _helper.visuallyRemoveChild(detail);
        }

        @Override
        public void relayout()
        {
            _helper.relayout();
        }
    }
    
    private class DetailPanel extends ReorderablePanel<DbGradingSheetDetail>
    {
        private final DbGradingSheetDetail _detail;
        
        DetailPanel(DbGradingSheetDetail detail, OrderableController<DbGradingSheetDetail> controller, Color background)
        {
            super(detail, controller, background);
            
            _detail = detail;
            
            this.setLeftComponent(createTextArea());
        }
        
        private JTextArea createTextArea()
        {
            final JTextArea textArea = new JTextArea(_detail.getText());
            textArea.setRows(1);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setBorder(BorderFactory.createEtchedBorder());
            
            textArea.addFocusListener(new FocusAdapter()
            {
                @Override
                public void focusLost(FocusEvent fe)
                {
                    _detail.setText(textArea.getText());

                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            Allocator.getDatabase().putGradingSheetDetails(ImmutableSet.of(_detail));
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to update text of detail";
                        }
                    });
                }
            });
            
            return textArea;
        }
    }
    
    
    /**
     * Core Logic 
     */
    
    
    private static interface OrderableController<E extends Orderable<E>>
    {
        void putOrderables(Set<E> orderables) throws SQLException;
        void removeOrderables(Set<E> orderables) throws SQLException;
        
        void removeFromParent(E orderable);
        
        void relayout();
    }
    
    private abstract class OrderableControllerHelper<E extends Orderable & Comparable<E>>
    {
        private final Map<E, ReorderablePanel> _childrenPanelMap;
        private final JPanel _childrenPanel;
        private final JButton _addChildButton;
        
        OrderableControllerHelper(String childName, final Color background, final Color childBackground)
        {   
            _childrenPanel = new JPanel();
            _childrenPanel.setLayout(new BoxLayout(_childrenPanel, BoxLayout.Y_AXIS));
            _childrenPanel.setBackground(background);
            
            _childrenPanelMap = new HashMap<E, ReorderablePanel>();
            for(E child : getChildren())
            {
                _childrenPanelMap.put(child, createChildPanel(child, childBackground));
            }
            this.relayout();
            
            _addChildButton = new JButton("+" + childName);
            _addChildButton.setToolTipText("Add " + childName);
            _addChildButton.setMargin(new Insets(1, 1, 1, 1));
            _addChildButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    //Determine the order and the default name for the child
                    int maxOrder = -1;
                    for(Orderable child : getChildren())
                    {
                        maxOrder = Math.max(maxOrder, child.getOrder());
                    }

                    final E child = createChild(maxOrder + 1);
                    _childrenPanelMap.put(child, createChildPanel(child, childBackground));
                    relayout();
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            putChild(child);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to insert or update child into the database";
                        }
                    });
                }
            });
        }
        
        protected JButton getAddChildButton()
        {   
            return _addChildButton;
        }
        
        protected void relayout()
        {
            _childrenPanel.removeAll();
            
            List<E> children = new ArrayList<E>(getChildren());
            if(!children.isEmpty())
            {
                Collections.sort(children);
                for(E child : children)
                {
                    ReorderablePanel panel = _childrenPanelMap.get(child);
                    panel.reorderOccurred();
                    
                    PaddingPanel childPaddingPanel = new PaddingPanel(panel, 3, 3, 10, 10);
                    childPaddingPanel.setBackground(_childrenPanel.getBackground());
                    
                    _childrenPanel.add(childPaddingPanel);
                }
            }

            //Force visual update to reflect these changes
            _childrenPanel.repaint();
            _childrenPanel.revalidate();
        }
        
        protected void visuallyRemoveChild(E child)
        {
            _childrenPanelMap.remove(child);
            this.relayout();
        }
        
        protected JPanel getChildrenPanel()
        {
            return _childrenPanel;
        }
        
        abstract Set<E> getChildren();
        
        abstract E createChild(int order);
        
        abstract ReorderablePanel createChildPanel(E child, Color background);
        
        abstract void putChild(E child) throws SQLException;
    }
    
    private abstract class ReorderablePanel<E extends Orderable<E>> extends PreferredHeightJPanel
    {
        private final E _orderable;
        private final OrderableController<E> _controller;
        private final JPanel _topPanel;
        private final JPanel _contentPanel;
        private JButton _upButton, _downButton;
        private JPanel _controlPanelComponentPanel;
        
        ReorderablePanel(E orderable, OrderableController<E> controller, Color background)
        {
            _orderable = orderable;
            _controller = controller;
            
            this.setLayout(new BorderLayout(0, 0));
            this.add(Box.createVerticalStrut(3), BorderLayout.NORTH);
            this.add(Box.createVerticalStrut(3), BorderLayout.SOUTH);
            this.add(Box.createHorizontalStrut(3), BorderLayout.EAST);
            this.add(Box.createHorizontalStrut(3), BorderLayout.WEST);
            this.setBackground(background);
            this.setBorder(BorderFactory.createEtchedBorder());
            
            _contentPanel = new JPanel(new BorderLayout());
            _contentPanel.setBackground(this.getBackground());
            this.add(_contentPanel, BorderLayout.CENTER);
            
            _topPanel = new JPanel(new BorderLayout(0, 0));
            _topPanel.setBackground(this.getBackground());
            _contentPanel.add(_topPanel, BorderLayout.NORTH);
            
            _controlPanelComponentPanel = new JPanel();
            JPanel controlPanelHost = new JPanel();
            controlPanelHost.setBackground(_topPanel.getBackground());
            controlPanelHost.setLayout(new BoxLayout(controlPanelHost, BoxLayout.Y_AXIS));
            controlPanelHost.add(createControlPanel());
            _topPanel.add(controlPanelHost, BorderLayout.EAST);
        }
        
        protected void setLeftComponent(JComponent component)
        {
            _topPanel.add(component, BorderLayout.CENTER);
        }
        
        protected void setBottomComponent(JComponent component)
        {   
            _contentPanel.add(component, BorderLayout.CENTER);
        }
        
        protected void setControlPanelLeftComponent(JComponent component)
        {
            _controlPanelComponentPanel.add(component, BorderLayout.CENTER);
            _controlPanelComponentPanel.setVisible(true);
        }
        
        private JPanel createControlPanel()
        {
            JPanel controlPanel = new PreferredHeightJPanel();
            controlPanel.setBackground(_topPanel.getBackground());
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
            
            _controlPanelComponentPanel.setBackground(controlPanel.getBackground());
            _controlPanelComponentPanel.setLayout(new BorderLayout(0, 0));
            _controlPanelComponentPanel.add(Box.createHorizontalStrut(3), BorderLayout.WEST);
            _controlPanelComponentPanel.setVisible(false);
            controlPanel.add(_controlPanelComponentPanel);
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _upButton = new JButton("↑");
            _upButton.setToolTipText("Move up");
            _upButton.setMargin(new Insets(1, 1, 1, 1));
            _upButton.setEnabled(false);
            _upButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _orderable.setOrder(_orderable.getOrder() - 1);
                    
                    ImmutableSet.Builder<E> reorderedBuilder = ImmutableSet.builder();
                    reorderedBuilder.add(_orderable);
                    for(E elem : _orderable.getOrderedElements())
                    {
                        if(elem != _orderable && elem.getOrder() == _orderable.getOrder())
                        {
                            elem.setOrder(elem.getOrder() + 1);
                            reorderedBuilder.add(elem);
                        }
                    }
                    final ImmutableSet<E> reorderedElements = reorderedBuilder.build();
                    
                    _controller.relayout();
                    
                    //Reorder elements on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            _controller.putOrderables(reorderedElements);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder items";
                        }
                    });
                }
            });
            controlPanel.add(_upButton);
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            _downButton = new JButton("↓");
            _downButton.setToolTipText("Move down");
            _downButton.setMargin(new Insets(1, 1, 1, 1));
            _downButton.setEnabled(false);
            _downButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _orderable.setOrder(_orderable.getOrder() + 1);
                    
                    ImmutableSet.Builder<E> reorderedBuilder = ImmutableSet.builder();
                    reorderedBuilder.add(_orderable);
                    for(E elem : _orderable.getOrderedElements())
                    {
                        if(elem != _orderable && elem.getOrder() == _orderable.getOrder())
                        {
                            elem.setOrder(elem.getOrder() - 1);
                            reorderedBuilder.add(elem);
                        }
                    }
                    final ImmutableSet<E> reorderedElements = reorderedBuilder.build();
                    
                    _controller.relayout();
                    
                    //Reorder elements on the worker thread
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            _controller.putOrderables(reorderedElements);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to reorder items";
                        }
                    });
                }
            });
            controlPanel.add(_downButton);
            
            this.reorderOccurred();
            
            controlPanel.add(Box.createHorizontalStrut(3));
            
            JButton deleteButton = new JButton("✗");
            deleteButton.setToolTipText("Delete");
            deleteButton.setMargin(new Insets(1, 1, 1, 1));
            deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    _controller.removeFromParent(_orderable);
                    ImmutableSet.Builder<E> reorderedBuilder = ImmutableSet.builder();
                    for(E elem : _orderable.getOrderedElements())
                    {
                        if(elem.getOrder() > _orderable.getOrder())
                        {
                            elem.setOrder(elem.getOrder() - 1);
                            reorderedBuilder.add(elem);
                        }
                    }
                    final ImmutableSet<E> reorderedElements = reorderedBuilder.build();

                    _controller.relayout();
                    
                    _worker.submit(WORKER_TAG, new ReinitializeRunnable()
                    {
                        @Override
                        public void dbCall() throws SQLException
                        {
                            _controller.removeOrderables(ImmutableSet.of(_orderable));

                            //This absolutely needs to occur after the delete call so in case that fails this
                            //code will not execute
                            _controller.putOrderables(reorderedElements);
                        }

                        @Override
                        public String dbFailureMessage()
                        {
                            return "Unable to delete or reorder items";
                        }
                    });
                }
            });
            controlPanel.add(deleteButton);
            
            controlPanel.add(Box.createHorizontalStrut(1));
            
            return controlPanel;
        }
        
        void reorderOccurred()
        {
            _upButton.setEnabled(_orderable.getOrder() != 0);
            
            boolean isLastPart = true;
            for(E elem : _orderable.getOrderedElements())
            {
                if(_orderable.getOrder() < elem.getOrder())
                {
                    isLastPart = false;
                    break;
                }
            }
            _downButton.setEnabled(!isLastPart);
        }
    }
}