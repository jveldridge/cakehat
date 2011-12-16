package cakehat.config;

import cakehat.rubric.TimeStatus;
import com.google.common.collect.ImmutableList;
import java.util.Collection;

/**
 * The late policy applied to an assignment.
 *
 * @author jak2
 */
@Deprecated
public enum LatePolicy
{
    NO_LATE(TimeStatus.ON_TIME, TimeStatus.NC_LATE),
    DAILY_DEDUCTION(TimeStatus.ON_TIME, TimeStatus.LATE),
    MULTIPLE_DEADLINES(TimeStatus.EARLY, TimeStatus.ON_TIME, TimeStatus.LATE, TimeStatus.NC_LATE);
    
    private final Collection<TimeStatus> _availableStatuses;
    
    LatePolicy(TimeStatus... availableStatuses) {
        _availableStatuses = ImmutableList.of(availableStatuses);
    }
    
    public Collection<TimeStatus> getAvailableStatuses() {
        return _availableStatuses;
    }
    
}