package grader;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.util.Vector;

import javax.swing.JScrollBar;

public class MyOwnFocusTraversalPolicy extends FocusTraversalPolicy {

    Vector<Component> order;
    JScrollBar _scroll;

    public MyOwnFocusTraversalPolicy(Vector<Component> order, JScrollBar scroll) {
        this.order = new Vector<Component>(order.size());
        this.order.addAll(order);
        _scroll = scroll;
    }

    public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
        if (order.indexOf(aComponent) + 1 < order.size()) {
            int idx = (order.indexOf(aComponent) + 1);
            Component toReturn = order.get(idx);
            if (toReturn.getY() + toReturn.getParent().getY() > _scroll.getValue() + _scroll.getVisibleAmount()) {
                _scroll.setValue(_scroll.getValue() + _scroll.getBlockIncrement(1));
            }
            return toReturn;
        } else {
            return order.lastElement();
        }

    }

    public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
        int idx = order.indexOf(aComponent) - 1;
        Component toReturn;
        if (idx < 0) {
            toReturn = order.get(0);
        } else {
            toReturn = order.get(idx);
        }
        if (toReturn.getY() + toReturn.getParent().getY() < _scroll.getValue()) {
            _scroll.setValue(_scroll.getValue() - _scroll.getBlockIncrement(-1));
        }
        return toReturn;
    }

    public Component getDefaultComponent(Container focusCycleRoot) {
        return order.get(0);
    }

    public Component getLastComponent(Container focusCycleRoot) {
        return order.lastElement();
    }

    public Component getFirstComponent(Container focusCycleRoot) {
        return order.get(0);
    }
}
