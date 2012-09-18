package cakehat.database;

/**
 *
 * @author jak2
 */
class ExtensionRecord
{
    private final String _onTime;
    private final boolean _shiftDates;
    private final String _note;
    private final String _dateRecorded;
    private final int _taId;

    public ExtensionRecord(String onTime, boolean shiftDates, String note, String dateRecorded, int taId)
    {
        _onTime = onTime;
        _shiftDates = shiftDates;
        _note = note;
        _dateRecorded = dateRecorded;
        _taId = taId;
    }

    String getDateRecorded()
    {
        return _dateRecorded;
    }

    String getNote()
    {
        return _note;
    }

    String getOnTime()
    {
        return _onTime;
    }

    boolean getShiftDates()
    {
        return _shiftDates;
    }

    int getTAId()
    {
        return _taId;
    }    
}