package utils;

import java.util.Vector;

/**
 * @deprecated
 * A representation of the config.xml file.
 * Used by ConfigurationManager. Not visible to other packages.
 *
 * @author jak2 (Joshua Kaplan)
 */
class Configuration {
    public Vector<Assignment> Assignments = new Vector<Assignment>();
    public Vector<String> Graders = new Vector<String>();
    public Vector<String> Admins = new Vector<String>();
    public Vector<String> TAs = new Vector<String>();
}