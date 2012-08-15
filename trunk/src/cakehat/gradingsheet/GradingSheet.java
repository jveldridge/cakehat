package cakehat.gradingsheet;

import cakehat.database.DbGradingSheet;
import cakehat.database.DbPart;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A GradingSheet encapsulates the criteria by which groups are graded for a particular Part.  There is exactly one
 * GradingSheet per part, and a GradingSheet is merely a collection of GradingSheetSections that further define grading
 * criteria.
 * 
 * Note that unlike {@link Assignment}, {@link GradableEvent}, etc., GradingSheet objects are not managed. That is, 
 * there may be multiple GradingSheet objects that represent a single grading sheet; GradingSheet objects are assumed to
 * be equal if their IDs are equal.  Thus, {@code GradingSheet} differs from {@link DbGradingSheet} only in that it is
 * immutable and refers to {@link Part} rather than {@link DbPart}.
 * 
 * @author jeldridg
 */
public class GradingSheet implements Comparable<GradingSheet> {
    
    private final int _id;
    private final Part _part;
    private final ImmutableList<GradingSheetSection> _sections;
    private final ImmutableSet<GradingSheetSubsection> _subsections;
    
    /**
     * Constructs a GradingSheet.
     * 
     * @param id the unique identifier for this GradingSheet in the database, stable regardless of changes
     * @param part the Part this GradingSheet belongs to
     * @param sections the GradingSheetSections that make up this GradingSheet
     */
    GradingSheet(int id, Part part, List<GradingSheetSection> sections) {
        if (part == null) {
            throw new NullPointerException("part may not be null.");
        }
        if (sections == null) {
            throw new NullPointerException("sections may not be null");
        }
        
        _id = id;
        _part = part;
        
        List<GradingSheetSection> sectionsToSort = new ArrayList<GradingSheetSection>(sections);
        Collections.sort(sectionsToSort);
        _sections = ImmutableList.copyOf(sectionsToSort);
        
        ImmutableSet.Builder<GradingSheetSubsection> subsectionBuilder = ImmutableSet.builder();
        for (GradingSheetSection section : _sections) {
            for (GradingSheetSubsection subsection : section.getSubsections()) {
                subsectionBuilder.add(subsection);
            }
        }
        _subsections = subsectionBuilder.build();
    }
    
    /**
     * The unique identifier for this grading sheet, stable across all changes.
     * 
     * @return
     */
    public int getId() {
        return _id;
    }
    
    /**
     * The Part to which this grading sheet belongs.
     * 
     * @return 
     */
    public Part getPart() {
        return _part;
    }
    
    /**
     * Returns an immutable list of all of the {@code GradingSheetSection}s that belong to this grading sheet, sorted 
     * by order. 
     * 
     * @return 
     */
    public ImmutableList<GradingSheetSection> getSections() {
        return _sections;
    }
    
    /**
     * Returns whether some section of this grading sheet contains the given {@code GradingSheetSubsection}; that is,
     * some section contains a {@code GradingSheetSubsection} with the same ID as the one given.
     * 
     * @param subsection
     * @return 
     */
    public boolean containsSubsection(GradingSheetSubsection subsection) {
        return _subsections.contains(subsection);
    }
    
    @Override
    public int hashCode() {
        return _id;
    }
    
    @Override
    public boolean equals(Object o) {        
        return o instanceof GradingSheet && this.getId() == ((GradingSheet) o).getId();
    }

    @Override
    public int compareTo(GradingSheet gs) {
        return this.getPart().compareTo(gs.getPart());
    }
    
}