package cakehat.gradingsheet;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * A GradingSheetSection is an ordered grouping of related {@link GradingSheetSubsection}s under a heading, the name.
 * GradingSheetSections are themselves ordered within a GradingSheet.  A GradingSheetSection may also specify an out-of
 * points value to be used for subtractive grading; in that case, none of the section's subsections is permitted to 
 * specify an out-of value.
 * 
 * @author jeldridg
 */
public class GradingSheetSection {
    
    private final int _id;
    private volatile GradingSheet _gradingSheet;
    private final String _name;
    private final int _order;
    private final Double _outOf;
    
    private final ImmutableList<GradingSheetSubsection> _subsections;
    
    /**
     * Creates a GradingSheetSection.
     * 
     * @param id the unique identifier for this GradingSheetSection in the database, stable regardless of changes
     * @param name the name of this section displayed to users, may not be {@null}
     * @param order
     * @param outOf
     * @param subsections 
     */
    GradingSheetSection(int id, String name, int order, Double outOf, List<GradingSheetSubsection> subsections) {
        if (name == null) {
            throw new NullPointerException("name may not be null");
        }
        if (subsections == null) {
            throw new NullPointerException("subsections may not be null");
        }
        
        _subsections = ImmutableList.copyOf(subsections);
        
        //if outOf is not specified, every subsection *must* specify outOf
        if (outOf == null) {
            for (GradingSheetSubsection subsection : _subsections) {
                if (subsection.getOutOf() == null) {
                    throw new IllegalArgumentException("Since section [" + name + "] of the grading sheet for part ["
                            + _gradingSheet.getPart().getFullDisplayName() + "] does not specify an out of value, each "
                            + "of its subsections must do so; subsection [" + subsection.getText() + "] does not.");
                }
            }
        }
        else { //if outOf is specified, *no* subsection may specify outOf
            for (GradingSheetSubsection subsection : _subsections) {
                if (subsection.getOutOf() != null) {
                    throw new IllegalArgumentException("Since section [" + name + "] of the grading sheet for part ["
                            + _gradingSheet.getPart().getFullDisplayName() + "] specifies an out of value,  none of its "
                            + "subsections may do so; however, subsection [" + subsection.getText() + "] does.");
                }
            }
        }
        
        _id = id;
        _name = name;
        _order = order;
        _outOf = outOf;
    }
    
    /**
     * Sets the GradingSheet this section belongs to.
     * 
     * @param gradingSheet
     * @throws NullPointerException if {@code gradingSheet} is null
     * @throws IllegalStateException if this method has been called before for this instance
     */
    void setGradingSheet(GradingSheet gradingSheet) {
        if (gradingSheet == null) {
            throw new NullPointerException("Grading sheet section cannot belong to a null grading sheet");
        }

        if (_gradingSheet != null) {
            throw new IllegalStateException("Grading sheet may only be set once");
        }

        _gradingSheet = gradingSheet;
    }
    
    /**
     * The unique identifier for this section, stable across all changes.
     * 
     * @return
     */
    public int getId() {
        return _id;
    }
    
    /**
     * Returns the grading sheet this section belongs to.
     * 
     * @return 
     * @throws IllegalStateException if the grading sheet this section belongs to has not yet been set
     */
    public GradingSheet getGradingSheet() {
        if (_gradingSheet == null) {
            throw new IllegalStateException("Grading sheet has not yet been set");
        }

        return _gradingSheet;
    }
    
    /**
     * Returns the name of the section.
     * @return 
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Returns the order of this section among the sections of its {@code GradingSheet}.
     * @return 
     */
    public int getOrder() {
        return _order;
    }
    
    /**
     * Returns the number of points this section is out of.  If {@code null}, the section is worth the sum of the out of
     * values of its constituent subsections.  If not {@code null}, every constituent subsection will have an out of
     * value of {@code null}.
     * @return 
     */
    public Double getOutOf() {
        return _outOf;
    }
    
    /**
     * Returns an immutable list of all of the {@code GradingSheetSubsection}s that belong to this section, sorted by
     * order.
     * @return 
     */
    public ImmutableList<GradingSheetSubsection> getSubsections() {
        return _subsections;
    }
    
}