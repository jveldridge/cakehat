package cakehat.newdatabase;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * Represents information about a group as stored in the database.  Unlike the
 * {@link Group} object, instances of this class are not managed and are not 
 * guaranteed to be valid.  This class should used only as a return type for
 * {@link Database} methods supporting {@link DataServices} methods.
 * 
 * @author jeldridg
 */
 class GroupRecord {
    
    private final int _dbId;
    private final String _assignmentId;
    private final String _name;
    private final Set<Integer> _memberIDs;

    GroupRecord(int dbId, String asgnId, String name, Set<Integer> memberIDs)
    {
        _dbId = dbId;
        _assignmentId = asgnId;
        _name = name;
        _memberIDs = ImmutableSet.copyOf(memberIDs);
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

    Set<Integer> getMemberIDs()
    {
        return _memberIDs;
    }

}
