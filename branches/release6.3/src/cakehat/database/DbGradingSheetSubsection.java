package cakehat.database;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a subsection of a grading sheet as it is represented in the database and grading sheet editor.
 * 
 * @author jeldridg
 */
public class DbGradingSheetSubsection extends DbDataItem implements Comparable<DbGradingSheetSubsection>,
        Orderable<DbGradingSheetSubsection> {
    
    private volatile DbGradingSheetSection _gradingSheetSection;
    private volatile String _text;
    private volatile int _order;
    private volatile Double _outOf;
    
    private final Set<DbGradingSheetDetail> _details;
    
    public static DbGradingSheetSubsection build(DbGradingSheetSection section, String text, int order, Double outOf) {
        DbGradingSheetSubsection subsection = new DbGradingSheetSubsection(section, text, order, outOf);
        section.addSubsection(subsection);
        
        return subsection;
    }

    /**
     * Constructor to be used by the grading sheet editor to create a subsection for a grading sheet template.
     * 
     * @param section
     * @param text
     * @param order
     * @param outOf 
     */
    private DbGradingSheetSubsection(DbGradingSheetSection section, String text, int order, Double outOf) {
        super(null);
        _gradingSheetSection = section;
        _text = text;
        _order = order;
        _outOf = outOf;
        
        _details = new HashSet<DbGradingSheetDetail>();
    }

    /**
     * Constructor to be used by the database to load grading sheet section data into memory.
     * 
     * @param sectionId
     * @param id
     * @param text
     * @param order
     * @param outOf
     */
    DbGradingSheetSubsection(DbGradingSheetSection section, int id, String text, int order, Double outOf) {
        super(id);
        _gradingSheetSection = section;
        _text = text;
        _order = order;
        _outOf = outOf;
        
        _details = new HashSet<DbGradingSheetDetail>();
    }
    
    public void setText(String text) {
        _text = text;
    }
    
    public String getText() {
        return _text;
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
    
    public void addDetail(DbGradingSheetDetail detail) {
        synchronized (_details) {
            _details.add(detail);
        }
    }
    
    public void removeDetail(DbGradingSheetDetail detail) {
        synchronized (_details) {
            _details.remove(detail);
        }
    }
    
    public ImmutableSet<DbGradingSheetDetail> getDetails() {
        synchronized (_details) {
            return ImmutableSet.copyOf(_details);
        }
    }
    
    DbGradingSheetSection getSection() {
        return _gradingSheetSection;
    }

    @Override
    public int compareTo(DbGradingSheetSubsection other) {
        return new Integer(_order).compareTo(other._order);
    }

    @Override
    public Iterable<DbGradingSheetSubsection> getOrderedElements() {
        return _gradingSheetSection.getSubsections();
    }
}
