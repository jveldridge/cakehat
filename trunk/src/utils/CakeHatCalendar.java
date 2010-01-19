/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utils;

import java.util.GregorianCalendar;

/**
 *
 * @author alexku
 */
public class CakeHatCalendar extends GregorianCalendar {

    public void postUpdate() {
        this.complete();
    }

}
