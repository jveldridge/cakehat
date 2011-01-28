package gradesystem.database;

import gradesystem.rubric.TimeStatus;

public class HandinStatusPair {
    private TimeStatus _status;
    private Integer _daysLate;

    public HandinStatusPair(TimeStatus status, Integer daysLate) {
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
