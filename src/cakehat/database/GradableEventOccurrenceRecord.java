package cakehat.database;

/**
 *  Represents information about a handin stored in the database.  Unlike the
 * {@link GradableEventOccurrence} object, instances of this class are not managed and are not
 * guaranteed to be valid.  This class should be used only as a return type for
 * {@link Database} methods supporting {@link DataServices} methods.
 *
 * @author wyegelwe
 */
 class GradableEventOccurrenceRecord {

    private final int _agid;
    private final int _geid;
    private final String _dateRecorded;
    private final String _time;
    private final int _tid;

    GradableEventOccurrenceRecord(int geid, int agid, String time, String dateRecorded,
                                                                    int tid){
        _agid = agid;
        _geid = geid;
        _dateRecorded = dateRecorded;
        _time = time;
        _tid = tid;
    }

    public int getGradeableEventId(){
        return _geid;
    }

    public int getAsgnGroupId(){
        return _agid;
    }

    public int getTaId(){
        return _tid;
    }

    public String getDateRecorded(){
        return _dateRecorded;
    }

    public String getTime(){
        return _time;
    }
}
