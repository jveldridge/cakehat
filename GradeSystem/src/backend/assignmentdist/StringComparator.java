/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package backend.assignmentdist;

import java.util.Comparator;

/**
 *
 * @author jeldridg
 */
class StringComparator implements Comparator {

    public StringComparator() {
    }

    public int compare(Object o1, Object o2)
    {
        return ((String) o1).compareTo( (String) o2 );
    }

}