package cakehat.gradingsheet;

/**
 * A GradingSheetDetail simply represents text that can be shown under a {@link GradingSheetSubsection} to provide
 * additional information about the grading criteria for that section.
 * 
 * @author jeldridg
 */
public class GradingSheetDetail implements Comparable<GradingSheetDetail> {
    
    private final int _id;
    private volatile GradingSheetSubsection _subsection;
    private final String _text;
    private final int _order;
    
    GradingSheetDetail(int id, String text, int order) {
        if (text == null) {
            throw new NullPointerException("text may not be null");
        }
        
        _id = id;
        _text = text;
        _order = order;
    }
    
    /**
     * Sets the GradingSheetSubsection this detail belongs to.
     * 
     * @param subsection
     * @throws NullPointerException if {@code subsection} is null
     * @throws IllegalStateException if this method has been called before for this instance
     */
    void setGradingSheetSubsection(GradingSheetSubsection subsection) {
        if (subsection == null) {
            throw new NullPointerException("Grading sheet detail cannot belong to a null subsection");
        }

        if (_subsection != null) {
            throw new IllegalStateException("Grading sheet subsection may only be set once");
        }

        _subsection = subsection;
    }
    
    @Override
    public int hashCode() {
        return _id;
    }
    
    /**
     * The unique identifier for this detail, stable across all changes.
     * 
     * @return
     */
    public int getId() {
        return _id;
    }
    
    /**
     * Returns the subsection this detail belongs to.
     * 
     * @return 
     * @throws IllegalStateException if the subsection this detail belongs to has not yet been set
     */
    public GradingSheetSubsection getSubsection() {
        if (_subsection == null) {
            throw new IllegalStateException("Grading sheet subsection has not yet been set");
        }
        
        return _subsection;
    }
    
    /**
     * Returns the detail's text message to be shown to users to provide additional information on grading criteria.
     * @return 
     */
    public String getText() {
        return _text;
    }
    
    /**
     * Returns the order of this detail among the sections of its {@code GradingSheetSubsection}.
     * @return 
     */
    public int getOrder() {
        return _order;
    }
    
    @Override
    public boolean equals(Object o) {        
        return o instanceof GradingSheetDetail && this.getId() == ((GradingSheetDetail) o).getId();
    }

    /**
     * Compares first by section, then by order within a section.
     * 
     * @param gsd
     * @return 
     */
    @Override
    public int compareTo(GradingSheetDetail gsd) {
        int comparison = this.getSubsection().compareTo(gsd.getSubsection());
        
        if (comparison == 0) {
            comparison = ((Integer) this.getOrder()).compareTo(gsd.getOrder());
        }

        return comparison;
    }
    
}
