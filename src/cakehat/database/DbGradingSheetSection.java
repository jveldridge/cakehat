package cakehat.database;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a section of a grading sheet as it is represented in the database and grading sheet editor.
 * 
 * @author jeldridg
 */
public class DbGradingSheetSection extends DbDataItem implements Comparable<DbGradingSheetSection>,
        Orderable<DbGradingSheetSection> {
    
    private volatile DbPart _part;
    private volatile String _name;
    private volatile int _order;
    private volatile Double _outOf;
    
    private final Set<DbGradingSheetSubsection> _subsections;
    
    public static DbGradingSheetSection build(DbPart part, String name, int order, Double outOf) {
        DbGradingSheetSection section = new DbGradingSheetSection(part, name, order, outOf);
        part.addGradingSheetSection(section);
        
        return section;
    }

    /**
     * Constructor to be used by the grading sheet editor to create a subsection for a grading sheet template.
     * 
     * @param part
     * @param name
     * @param order
     * @param outOf 
     */
    private DbGradingSheetSection(DbPart part, String name, int order, Double outOf) {
        super(null);
        _part = part;
        _name = name;
        _order = order;
        _outOf = outOf;
        
        _subsections = new HashSet<DbGradingSheetSubsection>();
    }

    /**
     * Constructor to be used by the database to load grading sheet section data into memory.
     * 
     * @param part
     * @param id
     * @param name
     * @param order
     * @param outOf
     */
    DbGradingSheetSection(DbPart part, int id, String name, int order, Double outOf) {
        super(id);
        _part = part;
        _name = name;
        _order = order;
        _outOf = outOf;
        
        _subsections = new HashSet<DbGradingSheetSubsection>();
    }
    
    public void setName(String name) {
        _name = name;
    }
    
    public String getName() {
        return _name;
    }
    
    @Override
    public void setOrder(int order) {
        _order = order;
    }
    
    @Override
    public int getOrder() {
        return _order;
    }
    
    public void setOutOf(Double outOf) {
        _outOf = outOf;
    }
    
    public Double getOutOf() {
        return _outOf;
    }
    
    public void addSubsection(DbGradingSheetSubsection subsection) {
        synchronized (_subsections) {
            _subsections.add(subsection);
        }
    }
    
    public void removeSubsection(DbGradingSheetSubsection subsection) {
        synchronized (_subsections) {
            _subsections.remove(subsection);
        }
    }
    
    public ImmutableSet<DbGradingSheetSubsection> getSubsections() {
        synchronized (_subsections) {
            return ImmutableSet.copyOf(_subsections);
        }
    }
    
    DbPart getPart() {
        return _part;
    }

    @Override
    public int compareTo(DbGradingSheetSection other) {
        return new Integer(_order).compareTo(other._order);
    }
    
    @Override
    public Iterable<DbGradingSheetSection> getOrderedElements() {
        return _part.getGradingSheetSections();
    }
}
