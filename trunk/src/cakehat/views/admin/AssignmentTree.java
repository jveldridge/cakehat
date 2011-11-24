package cakehat.views.admin;

import cakehat.Allocator;
import cakehat.CakehatException;
import cakehat.CakehatMain;
import cakehat.config.Assignment;
import cakehat.config.LabPart;
import cakehat.config.NonHandinPart;
import cakehat.config.Part;
import cakehat.config.handin.DistributablePart;
import cakehat.resources.icons.IconLoader;
import cakehat.resources.icons.IconLoader.IconImage;
import cakehat.resources.icons.IconLoader.IconSize;
import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * A panel containing a tree of Assignments and Parts show in {@link AdminView}.
 *
 * @author jeldridg
 */
class AssignmentTree extends JScrollPane {

    private JTree _tree;

    public AssignmentTree() {
        this.setBackground(Color.WHITE);
        
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        for (Assignment asgn : Allocator.getConfigurationInfo().getAssignments()) {
            GenericTreeNode<Assignment> asgnNode = new GenericTreeNode<Assignment>(asgn);

            for (Part part : asgn.getParts()) {
                asgnNode.add(new GenericTreeNode<Part>(part));
            }

            root.add(asgnNode);
        }

        _tree = new JTree(root);
        _tree.setRootVisible(false);
        _tree.setShowsRootHandles(true);

        //Partially override the default renderer to use custom icons
        _tree.setCellRenderer(new DefaultTreeCellRenderer()
        {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean selected, boolean expanded, boolean leaf, int row,
                    boolean hasFocus)
            {
                //Extract value in the tree object
                Object userObject = null;
                if(value instanceof GenericTreeNode)
                {
                    userObject = ((GenericTreeNode) value).getUserObject();
                }

                if(userObject instanceof Assignment)
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
                else if(userObject instanceof LabPart)
                {
                    this.setLeafIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.NETWORK_IDLE));
                }
                else if(userObject instanceof NonHandinPart)
                {
                    this.setLeafIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.ACCESSORIES_TEXT_EDITOR));
                }
                else if(userObject instanceof DistributablePart)
                {
                    this.setLeafIcon(IconLoader.loadIcon(IconSize.s16x16, IconImage.PACKAGE_X_GENERIC));
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
        });

        this.setViewportView(_tree);
    }

    /**
     * Returns a Map containing the currently selected Assignments and Parts.
     * An Assignment will be in the returned Map's key set if and only if the
     * Assignment is selected or one if its consitutent Parts is selected.  If
     * an Assignment is selected but none of its Parts are, the value for that
     * entry in the Map will be an empty List.
     *
     * If nothing is selected, an empty map will be returned.
     *
     * @return
     */
    public Map<Assignment, List<Part>> getSelection() {
        Map<Assignment, List<Part>> selection = new HashMap<Assignment, List<Part>>();

        TreePath[] selectedPaths = _tree.getSelectionModel().getSelectionPaths();
        if (selectedPaths == null || selectedPaths.length == 0) {
            return selection;
        }

        for (TreePath path : selectedPaths) {
            Object[] pathComponents = path.getPath();

            //first element of pathComponents is null, the invisible root
            // if this is the only element of the path, it is not a real selected path
	    	if (pathComponents.length == 1) {
	            continue;
	        }

            //second element is the node representing the Assignment
            Assignment asgn = ((GenericTreeNode<Assignment>) pathComponents[1]).getUserObject();

            if (!selection.containsKey(asgn)) {
                selection.put(asgn, new LinkedList<Part>());
            }

            //third element, if present, is node representing the Part
            if (pathComponents.length > 2) {
                Part part = ((GenericTreeNode<Part>) pathComponents[2]).getUserObject();
                selection.get(asgn).add(part);
            }
        }

        return selection;
    }
    
    public void addSelectionListener(TreeSelectionListener tsl) {
        _tree.addTreeSelectionListener(tsl);
    }

    private class GenericTreeNode<E> extends DefaultMutableTreeNode {

        public GenericTreeNode(E data) {
            super(data);
        }

        @Override
        public E getUserObject() {
            return (E) super.getUserObject();
        }

    }
    
    public void expandAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) _tree.getModel().getRoot();
        expandCollapseAll(new TreePath(root), true);
    }
    
    public void collapseAll() {
        // we must reset selected paths after because collapsePath makes node unselected
	    TreePath[] selectedPaths = _tree.getSelectionPaths();
	    
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) _tree.getModel().getRoot();
	    expandCollapseAll(new TreePath(root), false);
		
		_tree.expandPath(new TreePath(root));

        // we must only add paths with assignments and not parts selected because
		// otherwise the paths to selected parts would remain expanded
	    for (TreePath path : selectedPaths) {
		    if (path.getPathCount() == 2) {
	            _tree.getSelectionModel().addSelectionPaths(selectedPaths);
	        }
	    }
    }
    
    private void expandCollapseAll(TreePath parent, boolean expand) {
        // Traverse children
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        for (int i = 0; i < node.getChildCount(); i++) {
                // Cast will always succeed because all of the nodes created in the assignment tree
                // are DefaultMutableTreeNodes
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                TreePath path = parent.pathByAddingChild(child);
                expandCollapseAll(path, expand);
        }

        if (expand) {
            _tree.expandPath(parent);
        } 
        else {
            _tree.collapsePath(parent);
        }
    }
    
    public static void main(String[] argv) throws CakehatException {
        CakehatMain.initializeForTesting();

        JFrame frame = new JFrame("Tree Test");

        frame.add(new AssignmentTree());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}