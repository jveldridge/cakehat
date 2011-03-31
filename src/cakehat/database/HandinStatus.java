package cakehat.database;

import cakehat.rubric.TimeStatus;

public class HandinStatus {
    private final TimeStatus _status;
    private final Integer _daysLate;

    public HandinStatus(TimeStatus status, Integer daysLate) {
        _status = status;
        _daysLate = daysLate;
    }

    public TimeStatus getTimeStatus() {
        return _status;
    }

    public Integer getDaysLate() {
        return _daysLate;
    }
}