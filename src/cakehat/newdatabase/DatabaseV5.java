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
    
    /**
     * Returns an immutable set of DbNotifyAddress objects representing rows of the {@code notifyaddresses} table in the
     * database.
     * 
     * @return
     * @throws SQLException 
     */
    public Set<DbNotifyAddress> getNotifyAddresses() throws SQLException;
    
    /**
     * For each DbNotifyAddress in the given Set, adds the address to the database if it does not yet exist, or updates
     * the corresponding entry in the database if it already exists.  Whether or not {@link DbNotifyAddress#getId()}
     * returns {@code null} is used to determine whether or not the address yet exists in the database.  All
     * DbNotifyAddress objects that resulted in the addition of an entry in the database will be updated such that its 
     * ID field matches the auto-generated ID of the corresponding row in the database.
     * 
     * Note that it is permitted to have multiple notify addresses entries in the database that have the same actual
     * address.  If the set contains a DbNotifyAddress with a non-null ID that does not correspond to an entry in the
     * database, however, a SQLException will be thrown.
     * 
     * @param notifyAddresses
     * @throws SQLException 
     */
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
    
    public void putPartActions(Set<DbPartAction> partActions) throws SQLException;
    
    //cascades
    public void removePartActions(Set<DbPartAction> partActions) throws SQLException;
    
    public void putPartActionProperties(Set<DbActionProperty> actionProperties) throws SQLException;
    
    public void removePartActionProperties(Set<DbActionProperty> actionProperties) throws SQLException;
    
    public void putInclusionFilters(Set<DbInclusionFilter> inclusionFilters) throws SQLException;
    
    public void removeInclusionFilters(Set<DbInclusionFilter> inclusionFilters) throws SQLException;
    
    public void resetDatabase() throws SQLException;
    
}
