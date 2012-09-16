package cakehat.gradingsheet;

import cakehat.assignment.Part;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import support.utils.NullMath;

/**
 * A GradingSheet encapsulates the criteria by which groups are graded for a particular Part.  There is exactly one
 * GradingSheet per part, and a GradingSheet is merely a collection of GradingSheetSections that further define grading
 * criteria.
 * 
 * @author jeldridg
 */
public class GradingSheet implements Comparable<GradingSheet> {
    
    private volatile Part _part;
    private final ImmutableList<GradingSheetSection> _sections;
    private final ImmutableMap<Integer, GradingSheetSection> _sectionMap;
    private final ImmutableMap<Integer, GradingSheetSubsection> _subsectionMap;
    
    /**
     * Constructs a GradingSheet.
     * 
     * @param id the unique identifier for this GradingSheet in the database, stable regardless of changes
     * @param part the Part this GradingSheet belongs to
     * @param sections the GradingSheetSections that make up this GradingSheet
     */
    GradingSheet(List<GradingSheetSection> sections) {
        if (sections == null) {
            throw new NullPointerException("sections may not be null");
        } 
        _sections = ImmutableList.copyOf(sections);
        
        ImmutableMap.Builder<Integer, GradingSheetSection> sectionsBuilder = ImmutableMap.builder();       
        ImmutableMap.Builder<Integer, GradingSheetSubsection> subsectionBuilder = ImmutableMap.builder();
        for (GradingSheetSection section : _sections) {
            sectionsBuilder.put(section.getId(), section);
            for (GradingSheetSubsection subsection : section.getSubsections()) {
                subsectionBuilder.put(subsection.getId(), subsection);
            }
        }
        _sectionMap = sectionsBuilder.build();
        _subsectionMap = subsectionBuilder.build();
    }
    
    /**
     * Sets the Part this GradingSheet belongs to.
     * 
     * @param part
     * @throws NullPointerException if {@code gradableEvent} is null
     * @throws IllegalStateException if this method has been called before for this instance
     */
    public void setPart(Part part) {
        if (part == null) {
            throw new NullPointerException("Grading sheet cannot belong to a null Part");
        }

        if (_part != null) {
            throw new IllegalStateException("Part may only be set once");
        }

        _part = part;
    }

    /**
     * The Part to which this grading sheet belongs.
     * 
     * @return 
     * @throws IllegalStateException if the Part this GradingSheet belongs to has not yet been set
     */
    public Part getPart() {
        if (_part == null) {
            throw new IllegalStateException("Part has not yet been set");
        }

        return _part;
    }
    
    public Double getOutOf() {
        Double totalOutOf = null;
        
        for (GradingSheetSection section : _sections) {
            Double sectionOutOf = section.getOutOf();
            
            //If the section's out of is null then its total out of is defined by the sum of its subsections' out ofs
            if (sectionOutOf == null) {
                for (GradingSheetSubsection subsection : section.getSubsections()) {
                    sectionOutOf = NullMath.add(sectionOutOf, subsection.getOutOf());
                }
            }
            
            totalOutOf = NullMath.add(totalOutOf, sectionOutOf);
        }
        
        return totalOutOf;
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
    
    public GradingSheetSection getSection(int sectionId) {
        return _sectionMap.get(sectionId);
    }
    
    public GradingSheetSubsection getSubsection(int subsectionId) {
        return _subsectionMap.get(subsectionId);
    }
    
    /**
     * Returns whether some section of this grading sheet contains the given {@code GradingSheetSubsection}; that is,
     * some section contains a {@code GradingSheetSubsection} with the same ID as the one given.
     * 
     * @param subsection
     * @return 
     */
    public boolean containsSubsection(GradingSheetSubsection subsection) {
        return _subsectionMap.containsKey(subsection.getId());
    }
    
    @Override
    public int hashCode() {
        return this.getPart().getId();
    }
    
    @Override
    public boolean equals(Object o) {        
        return o instanceof GradingSheet && this.getPart().equals(((GradingSheet) o).getPart());
    }

    @Override
    public int compareTo(GradingSheet gs) {
        return this.getPart().compareTo(gs.getPart());
    }
    
}