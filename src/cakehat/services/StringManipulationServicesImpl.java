package cakehat.services;

import cakehat.config.Assignment;
import cakehat.database.Group;
import java.util.Collection;

/**
 * Default implementation of {@link StringManipulationServices}.
 * 
 * @author jeldridg
 */
public class StringManipulationServicesImpl implements StringManipulationServices {

    public String localizeText(String text, Assignment asgn, Collection<Group> groups) {
        text = text.replaceAll(StringManipulationServices.BE_TAG, getBeText(groups));
        text = text.replaceAll(StringManipulationServices.UNIT_TAG, getUnitText(asgn, groups));
        text = text.replaceAll(StringManipulationServices.CAP_UNIT_TAG, getCapUnitText(asgn, groups));
        text = text.replaceAll(StringManipulationServices.NUM_TAG, Integer.toString(groups.size()));

        return text;
    }

    private String getBeText(Collection<Group> groups) {
        if (groups.size() == 1) {
            return "is";
        } else {
            return "are";
        }
    }

    private String getUnitText(Assignment asgn, Collection<Group> groups) {
        if (asgn.hasGroups()) {
            if (groups.size() == 1) {
                return "group";
            }
            else {
                return "groups";
            }
        }
        else {
            if (groups.size() == 1) {
                return "student";
            }
            else {
                return "students";
            }
        }
    }

    private String getCapUnitText(Assignment asgn, Collection<Group> groups) {
        if (asgn.hasGroups()) {
            if (groups.size() == 1) {
                return "Group";
            }
            else {
                return "Groups";
            }
        }
        else {
            if (groups.size() == 1) {
                return "Student";
            }
            else {
                return "Students";
            }
        }
    }
}