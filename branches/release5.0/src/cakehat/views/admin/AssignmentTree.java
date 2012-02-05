package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A panel containing a tree of Assignments, GradableEvents, and Parts.
 *
 * @author jak2
 */
class AssignmentTree extends JPanel
{
    private final JTree _tree;
    private final List<AssignmentTreeListener> _listeners = new CopyOnWriteArrayList<AssignmentTreeListener>();

    AssignmentTree()
    {
        this.setLayout(new BorderLayout(0, 0));

        //Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        this.add(headerPanel, BorderLayout.NORTH);

        JLabel assignmentsLabel = new JLabel("Assignments");
        assignmentsLabel.setFont(new Font("Dialog", Font.BOLD, 16));
        assignmentsLabel.setAlignmentX(LEFT_ALIGNMENT);
        headerPanel.add(assignmentsLabel);

        headerPanel.add(Box.createVerticalStrut(5));

        //Tree
        _tree = new JTree();
        _tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        _tree.setModel(new AssignmentTreeModel());
        _tree.setRootVisible(false);
        _tree.setShowsRootHandles(true);
        _tree.setCellRenderer(new AssignmentTreeCellRenderer());

        this.expandTree();

        JScrollPane treeScrollPane = new JScrollPane(_tree);
        treeScrollPane.setBackground(Color.WHITE);
        this.add(treeScrollPane, BorderLayout.CENTER);

        // Notify listeners of selection
        _tree.addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent tse)
            {
                AssignmentTreeSelection selection = AssignmentTree.this.getSelection();
                for(AssignmentTreeListener listener : _listeners)
                {
                    listener.selectionChanged(selection);
                }
            }
        });

        this.setMinimumSize(this.getPreferredSize());
    }
    
    private void expandTree()
    {
        int row = 0;
        while(row < _tree.getRowCount())
        {
            _tree.expandRow(row);
            row++;
        }
    }

    AssignmentTreeSelection getSelection()
    {
        AssignmentTreeSelection selection = new AssignmentTreeSelection();
        
        TreePath[] selectedPaths = _tree.getSelectionModel().getSelectionPaths();
        if(selectedPaths != null)
        {
            for(TreePath path : selectedPaths)
            {
                Object selected = path.getLastPathComponent();
                if(selected instanceof Assignment)
                {
                    selection = new AssignmentTreeSelection((Assignment) selected);
                    break;
                }
                else if(selected instanceof GradableEvent)
                {
                    selection = new AssignmentTreeSelection((GradableEvent) selected);
                    break;
                }
                else if(selected instanceof Part)
                {
                    selection = new AssignmentTreeSelection((Part) selected);
                    break;
                }
            }
        }
        
        return selection;
    }
    
    JTree getTree()
    {
        return _tree;
    }

    void selectFirstAssignment()
    {
        Object root = _tree.getModel().getRoot();
        int childCount = _tree.getModel().getChildCount(root);
        if(childCount > 0)
        {
            _tree.setSelectionPath(new TreePath(new Object[]{ root, _tree.getModel().getChild(root, 0) }));
        }
    }

    void addSelectionListener(AssignmentTreeListener listener)
    {
        _listeners.add(listener);
    }

    void removeSelectionListener(AssignmentTreeListener listener)
    {
        _listeners.remove(listener);
    }
    
    static interface AssignmentTreeListener
    {
        public void selectionChanged(AssignmentTreeSelection assignmentTreeSelection);
    }
    
    static class AssignmentTreeSelection
    {
        private final Assignment _assignment;
        private final GradableEvent _gradableEvent;
        private final Part _part;
        
        private AssignmentTreeSelection(Assignment asgn)
        {
            _assignment = asgn;
            _gradableEvent = null;
            _part = null;
        }
        
        private AssignmentTreeSelection(GradableEvent ge)
        {
            _assignment = ge.getAssignment();
            _gradableEvent = ge;
            _part = null;
        }
        
        private AssignmentTreeSelection(Part part)
        {
            _assignment = part.getAssignment();
            _gradableEvent = part.getGradableEvent();
            _part = part;
        }
        
        private AssignmentTreeSelection()
        {
            _assignment = null;
            _gradableEvent = null;
            _part = null;
        }
        
        Assignment getAssignment()
        {
            return _assignment;
        }
        
        GradableEvent getGradableEvent()
        {
            return _gradableEvent;
        }
        
        Part getPart()
        {
            return _part;
        }
        
        @Override
        public String toString()
        {
            return "[" + this.getClass().getName() + " assignment=" + _assignment +
                                                    ", gradableevent=" + _gradableEvent + 
                                                    ", part=" + _part + "]"; 
        }
    }

    private static class AssignmentTreeCellRenderer extends DefaultTreeCellRenderer
    {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus)
        {
            if(value instanceof Assignment)
            {
                if(leaf)
                {
                    this.setLeafIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.FOLDER_OPEN));
                }
                else
                {
                    if(expanded)
                    {
                        this.setOpenIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.FOLDER_OPEN));
                    }
                    else
                    {
                        this.setClosedIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.FOLDER));
                    }
                }
            }
            else if(value instanceof GradableEvent)
            {
                if(leaf)
                {
                    this.setLeafIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.FOLDER_OPEN));
                }
                else
                {
                    if(expanded)
                    {
                        this.setOpenIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.FOLDER_OPEN));
                    }
                    else
                    {
                        this.setClosedIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.FOLDER));
                    }
                }
            }
            else if(value instanceof Part)
            {
                if(((Part) value).getGradableEvent().hasDigitalHandins())
                {
                    this.setLeafIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.COMPUTER));
                }
                else
                {
                    this.setLeafIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.ACCESSORIES_TEXT_EDITOR));
                }
            }
            else
            {
                if(leaf)
                {
                    this.setIcon(this.getLeafIcon());
                }
                else
                {
                    if(expanded)
                    {
                        this.setIcon(this.getDefaultOpenIcon());
                    }
                    else
                    {
                        this.setIcon(this.getDefaultClosedIcon());
                    }
                }
            }

            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    }

    private static class AssignmentTreeModel implements TreeModel
    {
        private final List<Assignment> _assignments;
        private final Object ROOT = "ROOT";

        public AssignmentTreeModel()
        {
            _assignments = Allocator.getDataServices().getAssignments();
        }

        @Override
        public Object getRoot()
        {
            return ROOT;
        }

        @Override
        public Object getChild(Object o, int i)
        {
            Object child = null;
            if(o == ROOT)
            {
                return _assignments.get(i);
            }
            else if(o instanceof Assignment)
            {
                child = ((Assignment) o).getGradableEvents().get(i);
            }
            else if(o instanceof GradableEvent)
            {
                child = ((GradableEvent) o).getParts().get(i);
            }

            return child;
        }

        @Override
        public int getChildCount(Object o)
        {
            int count = 0;
            if(o == ROOT)
            {
                count = _assignments.size();
            }
            else if(o instanceof Assignment)
            {
                count = ((Assignment) o).getGradableEvents().size();
            }
            else if(o instanceof GradableEvent)
            {
                count = ((GradableEvent) o).getParts().size();
            }

            return count;
        }

        @Override
        public boolean isLeaf(Object o)
        {
            boolean isLeaf = (o instanceof Part) || 
                             (o instanceof GradableEvent && ((GradableEvent) o).getParts().isEmpty()) ||
                             (o instanceof Assignment && ((Assignment) o).getGradableEvents().isEmpty());

            return isLeaf;
        }

        @Override
        public int getIndexOfChild(Object parent, Object child)
        {
            int index = -1;
            if(parent == ROOT)
            {
                index = _assignments.indexOf(child);
            }
            else if(parent instanceof Assignment)
            {
                index = ((Assignment) parent).getGradableEvents().indexOf(child);
            }
            else if(parent instanceof GradableEvent)
            {
                index = ((GradableEvent) parent).getParts().indexOf(child);
            }

            return index;
        }

        public void valueForPathChanged(TreePath tp, Object o) { }
        public void addTreeModelListener(TreeModelListener tl) { }
        public void removeTreeModelListener(TreeModelListener tl) { }
    }
}