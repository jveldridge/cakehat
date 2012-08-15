package cakehat.gradingsheet;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A GradingSheetSubsection represents the level of a grading sheet on which points can by awarded (for additive
 * grading) or subtracted (for subtractive grading).  For additive grading, an out-of value is specified; for subtractive
 * grading, the out-of value will be {@code null}.
 * 
 * @author jeldridg
 */
public class GradingSheetSubsection implements Comparable<GradingSheetSubsection> {
    
    private final int _id;
    private volatile GradingSheetSection _section;
    private final String _text;
    private final int _order;
    private final Double _outOf;
    
    private final ImmutableList<GradingSheetDetail> _details;

    public GradingSheetSubsection(int id, String text, int order, Double outOf, List<GradingSheetDetail> details) {
        if (text == null) {
            throw new NullPointerException("text may not be null");
        }
        if (details == null) {
            throw new NullPointerException("details may not be null");
        }
        
        _id = id;
        _text = text;
        _order = order;
        _outOf = outOf;
        
        List<GradingSheetDetail> detailsToSort = new ArrayList<GradingSheetDetail>(details);
        Collections.sort(detailsToSort);
        _details = ImmutableList.copyOf(details);
    }
    
    /**
     * Sets the GradingSheetSection this subsection belongs to.
     * 
     * @param section
     * @throws NullPointerException if {@code gradingSheet} is null
     * @throws IllegalStateException if this method has been called before for this instance
     */
    void setGradingSheetSection(GradingSheetSection section) {
        if (section == null) {
            throw new NullPointerException("Grading sheet subsection cannot belong to a null section");
        }

        if (_section != null) {
            throw new IllegalStateException("Grading sheet section may only be set once");
        }

        _section = section;
    }
    
    /**
     * The unique identifier for this subsection, stable across all changes.
     * 
     * @return
     */
    public int getId() {
        return _id;
    }
    
    /**
     * Returns the section this subsection belongs to.
     * 
     * @return 
     * @throws IllegalStateException if the section this subsection belongs to has not yet been set
     */
    public GradingSheetSection getSection() {
        if (_section == null) {
            throw new IllegalStateException("Grading sheet section has not yet been set");
        }
        
        return _section;
    }
    
    /**
     * Returns the text for the subsection that describes the criteria for awarding or deducting points.
     * @return 
     */
    public String getText() {
        return _text;
    }
    
    /**
     * Returns the order of this subsection among the sections of its {@code GradingSheetSection}.
     * @return 
     */
    public int getOrder() {
        return _order;
    }
    
    /**
     * Returns the number of points this subsection is out of.  This may be {@code null} for subtractive grading, in 
     * which case the out-of value for the subsection's section will be non-{@code null}.
     * @return 
     */
    public Double getOutOf() {
        return _outOf;
    }
    
    /**
     * Returns an immutable list of all of the {@code GradingSheetDetails}s that belong to this subsection, sorted by
     * order. 
     * 
     * @return 
     */
    public ImmutableList<GradingSheetDetail> getDetails() {
        return _details;
    }
    
    @Override
    public int hashCode() {
        return _id;
    }
    
    @Override
    public boolean equals(Object o) {        
        return o instanceof GradingSheetSubsection && this.getId() == ((GradingSheetSubsection) o).getId();
    }

    /**
     * Compares first by section, then by order within a section.
     * 
     * @param gsss
     * @return 
     */
    @Override
    public int compareTo(GradingSheetSubsection gsss) {
        int comparison = this.getSection().compareTo(gsss.getSection());
        
        if (comparison == 0) {
            comparison = ((Integer) this.getOrder()).compareTo(gsss.getOrder());
        }

        return comparison;
    }
    
}
