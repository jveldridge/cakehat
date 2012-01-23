package cakehat.gml;

import cakehat.assignment.Part;
import cakehat.newdatabase.Group;
import java.util.ArrayList;

/**
 *
 * @author Hannah
 */
public class InMemoryGML {
    
    private final Part _part;
    private final Group _group;
    private String _type, _version;
    private final ArrayList<Section> _sections = new ArrayList<Section>();
    
    public InMemoryGML(Part part, Group group) {
        _group = group;
        _part = part;
    }
    
    public void setVersion(String version) {
        _version = version;
    }
    
    public String getVersion() {
        return _version;
    }
    
    public void setType(String type) {
        _type = type;
    }
    
    public String getType() {
        return _type;
    }
    
    public Part getPart() {
        return _part;
    }

    public Group getGroup() {
        return _group;
    } 
    
    // Sections

    public Section addSection(String name) {
        Section section = new Section(name);
        _sections.add(section);

        return section;
    }
    
    public ArrayList<Section> getSections() {
        return _sections;
    }
    
    public class Section {
        private final String _name;
        private ArrayList<Subsection> _subsections = new ArrayList<Subsection>();
        private String _comment;
        
        public Section(String name) {
            _name = name;
        }
        
        // Name

        public String getName() {
            return _name;
        }

        // Subsections

        public Subsection addSubsection(String name, double earned, double outof) throws GradingSheetException {
            Subsection subsection = new Subsection(name, earned, outof);
            _subsections.add(subsection);

            return subsection;
        }

        public ArrayList<Subsection> getSubsections() {
            return _subsections;
        }
        
        // Comments
        
        public void setComment(String text) {
            _comment = text;
        }

        public String getComment() {
            return _comment;
        }
    }
    
     public class Subsection {
        private final String _name;
        private double _earned = 0.0;
        private final double _outOf;
        private final ArrayList<String> _details = new ArrayList<String>();


        public Subsection(String name, double earned, double outOf) throws GradingSheetException {
            _name = name;
            _earned = earned;
            _outOf = outOf;
        }

        // Name

        public String getName() {
            return _name;
        }

        // Earned
        
        public void setEarned(double earned) {
            _earned = earned;
        }

        public double getEarned() {
            return _earned;
        }

        // OutOf

        public double getOutOf() {
            return _outOf;
        }

        // Details

        public void addDetail(String name) {
            _details.add(name);
        }

        public ArrayList<String> getDetails() {
            return _details;
        }   
    }   
}