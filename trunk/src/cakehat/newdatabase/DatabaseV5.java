package cakehat.newdatabase;

import cakehat.newdatabase.DbPropertyValue.DbPropertyKey;
import java.sql.SQLException;
import java.util.Set;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public interface DatabaseV5
{
   
    public <T> DbPropertyValue<T> getPropertyValue(DbPropertyKey<T> key) throws SQLException;
    
    public <T> void putPropertyValue(DbPropertyKey<T> key, DbPropertyValue<T> value) throws SQLException;
    
    public Set<DbNotifyAddress> getNotifyAddresses() throws SQLException;
    
    public void putNotifyAddresses(Set<DbNotifyAddress> notifyAddresses) throws SQLException;
    
    //need to set ID to null for each DbNotifyAddress
    public void removeNotifyAddresses(Set<DbNotifyAddress> notifyAddresses) throws SQLException;
    
    public Set<DbTA> getTAs() throws SQLException;
    
    public void putTAs(Set<DbTA> tas) throws SQLException;
    
    public Set<DbStudent> getStudents() throws SQLException;
    
    public void putStudents(Set<DbStudent> students) throws SQLException;
    
    //needs to have all nested constituents
    public Set<DbAssignment> getAssignments() throws SQLException;
    
    //only edits assignment table
    public void putAssignments(Set<DbAssignment> assignments) throws SQLException;
    
    //cascades
    public void removeAssignments(Set<DbAssignment> assignments) throws SQLException;
    
    public void putGradableEvents(Set<DbGradableEvent> gradableEvents) throws SQLException;
    
    //cascades
    public void removeGradableEvents(Set<DbGradableEvent> gradableEvents) throws SQLException;
    
    //only edits parts table
    public void putParts(Set<DbPart> parts) throws SQLException;
    
    public void removeParts(Set<DbPart> parts) throws SQLException;
    
    //also edits actionproperty table
    public void putPartActions(Set<DbPartAction> partActions) throws SQLException;
    
    //cascades
    public void removePartActions(Set<DbPartAction> partActions) throws SQLException;
    
    public void putInclusionFilters(Set<DbInclusionFilter> inclusionFilters) throws SQLException;
    
    public void removeInclusionFilters(Set<DbInclusionFilter> inclusionFilters) throws SQLException;
    
    public void resetDatabase() throws SQLException;
    
}
