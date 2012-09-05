package cakehat.database;

import cakehat.database.DbGroupGradingSheet.GroupSectionCommentsRecord;
import cakehat.database.GroupGradingSheet.GroupSectionComments;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Representation of a group's grading sheet used for reading from and writing to the database.
 * 
 * @author jeldridg
 */
class DbGroupGradingSheet extends DbDataItem {
    
    private final int _groupId;
    private final int _partId;
    private volatile Integer _assignedToId;
    private volatile Integer _submittedById;
    private volatile String _submittedDate;

    private final ConcurrentMap<Integer, GroupSubsectionEarnedRecord> _subsectionEarnedPoints;
    private final ConcurrentMap<Integer, GroupSectionCommentsRecord> _sectionComments;
    
    /**
     * Constructor to be used for creating DbGroupGradingSheet for a group that does not yet have one.
     * 
     * @param groupId
     * @param partId 
     */
    DbGroupGradingSheet(int groupId, int partId) {
        super(null);
        _groupId = groupId;
        _partId = partId;
        _assignedToId = null;
        _submittedById = null;
        _submittedDate = null;
        
        _subsectionEarnedPoints = new ConcurrentHashMap<Integer, GroupSubsectionEarnedRecord>();
        _sectionComments = new ConcurrentHashMap<Integer, GroupSectionCommentsRecord>();
    }
    
    /**
     * Constructor to be used when reading an existing group grading sheet from the database.
     * 
     * @param id
     * @param groupId
     * @param partId
     * @param assignedToId
     * @param submittedById
     * @param submittedDate
     * @param subsectionEarnedPoints - may be {@code null}
     * @param sectionComments - may be {@code null}
     */
    DbGroupGradingSheet(int id, int groupId, int partId, Integer assignedToId, Integer submittedById,
                        String submittedDate, Map<Integer, GroupSubsectionEarnedRecord> subsectionEarnedPoints,
                        Map<Integer, GroupSectionCommentsRecord> sectionComments) {
        super(id);
        if (submittedById == null && (submittedDate != null) ||
           (submittedById != null && submittedDate == null)) {
                throw new IllegalArgumentException("submittedById and submittedDate must both be null or both be "
                        + "non-null");
            }
        
        _groupId = groupId;
        _partId = partId;
        _assignedToId = assignedToId;
        _submittedById = submittedById;
        _submittedDate = submittedDate;
        
        _subsectionEarnedPoints = subsectionEarnedPoints == null ?
                new ConcurrentHashMap<Integer, GroupSubsectionEarnedRecord>() :
                new ConcurrentHashMap<Integer, GroupSubsectionEarnedRecord>(subsectionEarnedPoints);
        _sectionComments = sectionComments == null ?
                new ConcurrentHashMap<Integer, GroupSectionCommentsRecord>() :
                new ConcurrentHashMap<Integer, GroupSectionCommentsRecord>(sectionComments);
    }
    
    int getGroupId() {
        return _groupId;
    }
    
    int getPartId() {
        return _partId;
    }
    
    Integer getAssignedToId() {
        return _assignedToId;
    }
    
    void setAssignedToId(Integer graderId) {
        _assignedToId = graderId;
    }
    
    Integer getSubmittedById() {
        return _submittedById;
    }
    
    String getSubmittedDate() {
        return _submittedDate;
    }
    
    void setEarnedPoints(int subsectionId, Double pointsEarned, int taId, String dateTime) {
        _subsectionEarnedPoints.put(subsectionId, new GroupSubsectionEarnedRecord(pointsEarned, taId, dateTime));
    }
    
    void setComments(int sectionId, String comments, int taId, String dateTime) {
        _sectionComments.put(sectionId, new GroupSectionCommentsRecord(comments, taId, dateTime));
    }
    
    Map<Integer, GroupSubsectionEarnedRecord> getSubsectionEarnedPoints() {
        return _subsectionEarnedPoints;
    }
    
    Map<Integer, GroupSectionCommentsRecord> getSectionComments() {
        return _sectionComments;
    }
    
    void markSubmitted(int submitterId, String submissionDate) {
        _submittedById = submitterId;
        _submittedDate = submissionDate;
    }
    
    static class GroupSubsectionEarnedRecord {
        
        private final Double _earned;
        private final int _lastModifiedById;
        private final String _lastModifiedTime;
        
        GroupSubsectionEarnedRecord(Double earned, int lastModifiedById, String lastModifiedTime) {
            _earned = earned;
            _lastModifiedById = lastModifiedById;
            _lastModifiedTime = lastModifiedTime;
        }
        
        Double getEarnedPoints() {
            return _earned;
        }
        
        int getLastModifiedBy() {
            return _lastModifiedById;
        }
        
        String getLastModifiedTime() {
            return _lastModifiedTime;
        }
        
    }
    
    static class GroupSectionCommentsRecord {
        
        private final String _comments;
        private final int _lastModifiedById;
        private final String _lastModifiedTime;
        
        GroupSectionCommentsRecord(String comments, int lastModifiedById, String lastModifiedTime) {
            _comments = comments;
            _lastModifiedById = lastModifiedById;
            _lastModifiedTime = lastModifiedTime;
        }
        
        GroupSectionCommentsRecord(GroupSectionComments comments) {
            _comments = comments.getComments();
            _lastModifiedById = comments.getLastModifiedBy().getId();
            _lastModifiedTime = comments.getLastModifiedTime().toString();
        }
        
        String getComments() {
            return _comments;
        }
        
        int getLastModifiedBy() {
            return _lastModifiedById;
        }
        
        String getLastModifiedTime() {
            return _lastModifiedTime;
        }
        
    }
    
}
