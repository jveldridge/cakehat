package cakehat.database;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;

/**
 * Represents information about a group as stored in the database.  Unlike the
 * {@link Group} object, instances of this class are not managed and are not 
 * guaranteed to be valid.  This class should used only as a return type for
 * {@link Database} methods supporting {@link DataServices} methods.
 * 
 * @author jeldridg
 */
@Deprecated
 class GroupRecord {
    
    private final int _dbId;
    private final String _assignmentId;
    private final String _name;
    private final List<Integer> _memberIDs;

    GroupRecord(int dbId, String asgnId, String name, Collection<Integer> memberIDs)
    {
        _dbId = dbId;
        _assignmentId = asgnId;
        _name = name;
        _memberIDs = ImmutableList.copyOf(memberIDs);
    }
    
    int getDbId() {
        return _dbId;
    }
    
    String getAssignmentID() {
        return _assignmentId;
    }

    String getName()
    {
        return _name;
    }

    List<Integer> getMemberIDs()
    {
        return _memberIDs;
    }

}