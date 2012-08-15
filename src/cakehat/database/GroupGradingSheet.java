package cakehat.database;

import cakehat.Allocator;
import cakehat.gradingsheet.GradingSheet;
import cakehat.gradingsheet.GradingSheetSection;
import cakehat.gradingsheet.GradingSheetSubsection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.joda.time.DateTime;

/**
 * Represents the grading sheet for a group with points and comments.
 * 
 * @author jeldridg
 */
public class GroupGradingSheet extends DbDataItem {
    
    private final Group _group;
    private final GradingSheet _gradingSheet;
    
    /**
     * The TA who most recently submitted this group's grading sheet; may be {@code null} if never submitted.
     */
    private TA _submittedBy;
    
    /**
     * The time at which this group's grading sheet was most recently submitted; may be {@code null} if never submitted.
     */
    private DateTime _submittedTime;
    
    /**
     * These maps store points earned and comments.  They are concurrent so that they can be safely written to the
     * database on a thread other than the thread on which TAs enter this information (i.e., the UI thread).
     */
    private final ConcurrentHashMap<GradingSheetSubsection, GroupSubsectionEarned> _subsectionEarnedPoints;
    private final ConcurrentHashMap<GradingSheetSection, GroupSectionComments> _sectionComments;
    
    public GroupGradingSheet(Integer id, Group group, GradingSheet gradingSheet) {
        super(id);
        _group = group;
        _gradingSheet = gradingSheet;
        
        _subsectionEarnedPoints = new ConcurrentHashMap<GradingSheetSubsection, GroupSubsectionEarned>();
        _sectionComments = new ConcurrentHashMap<GradingSheetSection, GroupSectionComments>();
    }
    
    public Group getGroup() {
        return _group;
    }
    
    public GradingSheet getGradingSheet() {
        return _gradingSheet;
    }
    
    public void setEarnedPoints(GradingSheetSubsection subsection, Double pointsEarned) {
        if (!_gradingSheet.containsSubsection(subsection)) {
            throw new IllegalArgumentException("Grading sheet for part [" + _gradingSheet.getPart().getFullDisplayName()
                    + "] does not contain subsection [" + subsection.getText() + "].");
        }
        
        _subsectionEarnedPoints.put(subsection, new GroupSubsectionEarned(pointsEarned));
    }
    
    public void setComments(GradingSheetSection section, String comments) {
        if (!_gradingSheet.getSections().contains(section)) {
            throw new IllegalArgumentException("Grading sheet for part [" + _gradingSheet.getPart().getFullDisplayName()
                    + "] does not  contain section [" + section.getName() + "].");
        }
        
        _sectionComments.put(section, new GroupSectionComments(comments));
    }
    
    /**
     * Returns an unmodifiable map of grading sheet subsections to points earned by the group for that subsection.
     * 
     * @return 
     */
    public Map<GradingSheetSubsection, GroupSubsectionEarned> getEarnedPoints() {
        return Collections.unmodifiableMap(_subsectionEarnedPoints);
    }
    
    /**
     * Returns an unmodifiable map of grading sheet sections to comments for the group for that subsection.
     * @return 
     */
    public Map<GradingSheetSection, GroupSectionComments> getComments() {
        return Collections.unmodifiableMap(_sectionComments);
    }

    public TA getSubmittedBy() {
        return _submittedBy;
    }
    
    public DateTime getSubmittedTime() {
        return _submittedTime;
    }
    
    void markSubmitted() {
        _submittedBy = Allocator.getUserServices().getUser();
        _submittedTime = DateTime.now();
    }
    
    @Override
    void setParentNull() {
        throw new UnsupportedOperationException("This data item type has no parent.");
    }
    
    @Override
    Iterable<? extends DbDataItem> getChildren() {
        return Collections.emptyList();
    }
    
    public static class GroupSubsectionEarned {
        
        private Double _earned;
        private TA _lastModifiedBy;
        private DateTime _lastModifiedTime;
        
        private GroupSubsectionEarned(Double earned) {
            _earned = earned;
            _lastModifiedBy = Allocator.getUserServices().getUser();
            _lastModifiedTime = DateTime.now();
        }
        
        public Double getEarned() {
            return _earned;
        }
        
        public TA getLastModifiedby() {
            return _lastModifiedBy;
        }
        
        public DateTime getLastModifiedTime() {
            return _lastModifiedTime;
        }
        
    }
    
    public static class GroupSectionComments {
        
        private String _comments;
        private TA _lastModifiedBy;
        private DateTime _lastModifiedTime;
        
        private GroupSectionComments(String comments) {
            _comments = comments;
            _lastModifiedBy = Allocator.getUserServices().getUser();
            _lastModifiedTime = DateTime.now();
        }
        
        public String getComments() {
            return _comments;
        }
        
        public TA getLastModifiedby() {
            return _lastModifiedBy;
        }
        
        public DateTime getLastModifiedTime() {
            return _lastModifiedTime;
        }
        
    }
}
