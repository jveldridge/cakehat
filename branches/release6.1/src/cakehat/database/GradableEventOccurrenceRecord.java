package cakehat.database;

/**
 *
 * @author wyegelwe
 */
 class GradableEventOccurrenceRecord {

    private final String _dateRecorded;
    private final String _occurrenceDate;
    private final int _tid;

    GradableEventOccurrenceRecord(int tid, String dateRecorded, String occurrenceDate) {
        _tid = tid;
        _dateRecorded = dateRecorded;
        _occurrenceDate = occurrenceDate;
    }

    int getTA() {
        return _tid;
    }

    String getDateRecorded() {
        return _dateRecorded;
    }

    String getOccurrenceDate() {
        return _occurrenceDate;
    }
}