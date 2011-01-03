package gradesystem.components;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComboBox;

/**
 * A parameterized JComboBox.
 * 
 * @author jeldridg
 */
public class GenericJComboBox<E> extends JComboBox {

    public GenericJComboBox(Collection<E> items) {
        super(items.toArray());
    }

    public GenericJComboBox(E[] items) {
        super(items);
    }

    @Override
    public E getSelectedItem() {
        return (E) super.getSelectedItem();
    }

    public List<E> getGenericSelectedObjects() {
        List<E> objects = new LinkedList<E>();
        for (Object o : super.getSelectedObjects()) {
            objects.add((E) o);
        }

        return objects;
    }

}
