package cakehat.database;

import cakehat.Allocator;
import cakehat.database.DbGroupGradingSheet.GroupSectionCommentsRecord;
import cakehat.database.DbGroupGradingSheet.GroupSubsectionEarnedRecord;
import cakehat.gradingsheet.GradingSheet;
import cakehat.gradingsheet.GradingSheetSection;
import cakehat.gradingsheet.GradingSheetSubsection;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.joda.time.DateTime;
import support.utils.NullMath;

/**
 * Represents the grading sheet for a group with points and comments.
 * 
 * @author jeldridg
 */
public class GroupGradingSheet {
    
    private final Group _group;
    private final DbGroupGradingSheet _dbSheet;
    private final GradingSheet _gradingSheet;
    
    GroupGradingSheet(Group group, GradingSheet gradingSheet, DbGroupGradingSheet dbSheet) {
        if (group.getId() != dbSheet.getGroupId()) {
            throw new IllegalArgumentException("group and dbSheet must have the same group ID");
        }
        if (gradingSheet.getPart().getId() != dbSheet.getPartId()) {
            throw new IllegalArgumentException("gradingSheet and dbSheet must have the same part ID");
        }
        
        _group = group;
        _gradingSheet = gradingSheet;
        _dbSheet = dbSheet;
    }
    
    /**
     * Returns the {@link DbGroupGradingSheet} that backs this {@code GroupGradingSheet} object.
     * 
     * @return
     */
    DbGroupGradingSheet getDbGroupGradingSheet() {
        return _dbSheet;
    }
    
    public Integer getId() {
        return _dbSheet.getId();
    }
    
    public Group getGroup() {
        return _group;
    }
    
    public boolean isSubmitted() {
        return _dbSheet.getSubmittedDate() != null;
    }
    
    public GradingSheet getGradingSheet() {
        return _gradingSheet;
    }
    
    public void setEarnedPoints(GradingSheetSubsection subsection, Double pointsEarned) {
        if (!_gradingSheet.containsSubsection(subsection)) {
            throw new IllegalArgumentException("Grading sheet for part [" + _gradingSheet.getPart().getFullDisplayName()
                    + "] does not contain subsection [" + subsection.getText() + "].");
        }
        
        _dbSheet.setEarnedPoints(subsection.getId(), pointsEarned, Allocator.getUserServices().getUser().getId(),
                              DateTime.now().toString());
    }
    
    public void setComments(GradingSheetSection section, String comments) {
        if (!_gradingSheet.getSections().contains(section)) {
            throw new IllegalArgumentException("Grading sheet for part [" + _gradingSheet.getPart().getFullDisplayName()
                    + "] does not  contain section [" + section.getName() + "].");
        }
        
        _dbSheet.setComments(section.getId(), comments, Allocator.getUserServices().getUser().getId(),
                           DateTime.now().toString());
    }
    
    /**
     * Returns a map of grading sheet subsections to points earned by the group for that subsection.
     * 
     * @return 
     */
    public Map<GradingSheetSubsection, GroupSubsectionEarned> getEarnedPoints() {
        Map<Integer, GroupSubsectionEarnedRecord> earnedRecords = _dbSheet.getSubsectionEarnedPoints();
        
        ImmutableMap.Builder<GradingSheetSubsection, GroupSubsectionEarned> earned = ImmutableMap.builder();
        for (Integer subsectionId : earnedRecords.keySet()) {
            earned.put(_gradingSheet.getSubsection(subsectionId),
                       new GroupSubsectionEarned(earnedRecords.get(subsectionId)));
        }
        
        return earned.build();
    }
    
    public Double getEarned() {
        Double totalEarned = null;
        
        Map<GradingSheetSubsection, GroupSubsectionEarned> earnedMap = getEarnedPoints();
        
        for (GradingSheetSection section : _gradingSheet.getSections()) {
            //If the section's out of is not null then this is the starting value earned and the subsection values
            //will be negative and deduct from this value as needed
            if(section.getOutOf() != null) {
                totalEarned = NullMath.add(totalEarned, section.getOutOf());
            }
            
            for (GradingSheetSubsection subsection : section.getSubsections()) {
                GroupSubsectionEarned earned = earnedMap.get(subsection);
                totalEarned = NullMath.add(totalEarned, earned == null ? null : earned.getEarned());
            }
        }
        
        return totalEarned;
    }
    
    /**
     * Returns a map of grading sheet sections to comments for the group for that subsection.
     * @return 
     */
    public Map<GradingSheetSection, GroupSectionComments> getComments() {
        Map<Integer, GroupSectionCommentsRecord> commentRecords = _dbSheet.getSectionComments();
        
        ImmutableMap.Builder<GradingSheetSection, GroupSectionComments> comments = ImmutableMap.builder();
        for (Integer sectionId : commentRecords.keySet()) {
            comments.put(_gradingSheet.getSection(sectionId),
                       new GroupSectionComments(commentRecords.get(sectionId)));
        }
        
        return comments.build();
    }

    public TA getAssignedTo() {
        return _dbSheet.getAssignedToId() == null ? null : Allocator.getDataServices().getTA(_dbSheet.getAssignedToId());
    }
    
    public void setAssignedTo(TA grader) {
        _dbSheet.setAssignedToId(grader == null ? null : grader.getId());
    }
    
    public TA getSubmittedBy() {
        return _dbSheet.getSubmittedById() == null ? null : Allocator.getDataServices().getTA(_dbSheet.getSubmittedById());
    }
    
    public DateTime getSubmittedTime() {
        return _dbSheet.getSubmittedDate() == null ? null : new DateTime(_dbSheet.getSubmittedDate());
    }

    public static class GroupSubsectionEarned {
        
        private final Double _earned;
        private final TA _lastModifiedBy;
        private final DateTime _lastModifiedTime;
        
        GroupSubsectionEarned(GroupSubsectionEarnedRecord record) {
            _earned = record.getEarnedPoints();
            _lastModifiedBy = Allocator.getDataServices().getTA(record.getLastModifiedBy());
            _lastModifiedTime = new DateTime(record.getLastModifiedTime());
        }
        
        public Double getEarned() {
            return _earned;
        }
        
        public TA getLastModifiedBy() {
            return _lastModifiedBy;
        }
        
        public DateTime getLastModifiedTime() {
            return _lastModifiedTime;
        }
        
    }
    
    public static class GroupSectionComments {
        
        private final String _comments;
        private final TA _lastModifiedBy;
        private final DateTime _lastModifiedTime;

        GroupSectionComments(GroupSectionCommentsRecord record) {
            _comments = record.getComments();
            _lastModifiedBy = Allocator.getDataServices().getTA(record.getLastModifiedBy());
            _lastModifiedTime = new DateTime(record.getLastModifiedTime());
        }
        
        public String getComments() {
            return _comments;
        }
        
        public TA getLastModifiedBy() {
            return _lastModifiedBy;
        }
        
        public DateTime getLastModifiedTime() {
            return _lastModifiedTime;
        }
        
    }
}
