package gradesystem.views.backend;

import gradesystem.Allocator;
import gradesystem.config.Assignment;
import gradesystem.config.Part;
import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * A panel containing a tree of Assignments and Parts show in the backend.
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
        _tree.setToggleClickCount(1);

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

    public static void main(String[] argv) {
        JFrame frame = new JFrame("Tree Test");

        frame.add(new AssignmentTree());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
