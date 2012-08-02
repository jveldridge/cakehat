package cakehat.database;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a grading sheet template as it is represented in the database and grading sheet editor.
 * 
 * @author jeldridg
 */
public class DbGradingSheet extends DbDataItem {
    
    private volatile DbPart _part;
    private final Set<DbGradingSheetSection> _sections;
    
    public static DbGradingSheet build(DbPart part) {
        DbGradingSheet gradingSheet = new DbGradingSheet(part);
        
        return gradingSheet;
    }
    
    /**
     * Constructor to be used by the grading sheet editor to create a grading sheet template for a part.
     * 
     * @param part
     */
    private DbGradingSheet(DbPart part) {
        super(null);
        _part = part;      
        _sections = new HashSet<DbGradingSheetSection>();
    }

    /**
     * Constructor to be used by the database to load grading sheet data into memory.
     * 
     * @param part
     * @param id
     */
    DbGradingSheet(DbPart part, int id) {
        super(id);
        _part = part;
        _sections = new HashSet<DbGradingSheetSection>();
    }
    
    void addSection(DbGradingSheetSection section) {
        synchronized (_sections) {
            _sections.add(section);
        }
    }
    
    void removeSection(DbGradingSheetSection section) {
        synchronized (_sections) {
            _sections.remove(section);
        }
    }
    
    public ImmutableSet<DbGradingSheetSection> getSections() {
        synchronized (_sections) {
            return ImmutableSet.copyOf(_sections);
        }
    }
    
    DbPart getPart() {
        return _part;
    }
    
    @Override
    void setParentNull() {
        _part = null;
    }
    
    @Override
    Iterable<DbGradingSheetSection> getChildren() {
        return this.getSections();
    }
    
}
