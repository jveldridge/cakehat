package cakehat.database;

import java.util.Collections;

/**
 * Represents a detail of a subsection of a grading sheet as it is represented in the database and grading sheet editor.
 * 
 * @author jeldridg
 */
public class DbGradingSheetDetail extends DbDataItem implements Comparable<DbGradingSheetDetail> {
    
    private volatile DbGradingSheetSubsection _gradingSheetSubsection;
    private volatile String _text;
    private volatile int _order;
    
    public static DbGradingSheetDetail build(DbGradingSheetSubsection subsection, String text, int order) {
        DbGradingSheetDetail detail = new DbGradingSheetDetail(subsection, text, order);
        subsection.addDetail(detail);
        
        return detail;
    }
    
    /**
     * Constructor to be used by the grading sheet editor to create a detail for a subsection of a grading sheet template.
     * 
     * @param subsection
     * @param text
     * @param order
     */
    private DbGradingSheetDetail(DbGradingSheetSubsection subsection, String text, int order) {
        super(null);
        _gradingSheetSubsection = subsection;
        _text = text;
        _order = order;
    }

    /**
     * Constructor to be used by the database to load grading sheet section data into memory.
     * 
     * @param subsection
     * @param id
     * @param text
     * @param order
     */
    DbGradingSheetDetail(DbGradingSheetSubsection subsection, int id, String text, int order) {
        super(id);
        _gradingSheetSubsection = subsection;
        _text = text;
        _order = order;
    }
    
    public void setText(String text) {
        _text = text;
    }
    
    public String getText() {
        return _text;
    }
    
    public void setOrder(int order) {
        _order = order;
    }
    
    public int getOrder() {
        return _order;
    }
    
    DbGradingSheetSubsection getSubection() {
        return _gradingSheetSubsection;
    }
    
    @Override
    void setParentNull() {
        _gradingSheetSubsection = null;
    }
    
    @Override
    Iterable<? extends DbDataItem> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public int compareTo(DbGradingSheetDetail other) {
        return new Integer(_order).compareTo(other._order);
    }
    
}
