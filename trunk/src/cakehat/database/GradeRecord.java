package cakehat.database;

/**
 * Represents information about a grade record as stored in the database.  
 * This class should used only as a return type for {@link DatabaseV5} methods 
 * supporting {@link DataServicesV5} methods.
 * 
 * 
 * @author yf6
 */
class GradeRecord {
    
    private final String _dateRecorded;
    private final int _taId;
    private final Double _earned;
    private final boolean _matchesGml;
    
    GradeRecord(String dateRecorded, int taId, Double earned, boolean matchesGml) {
        _dateRecorded = dateRecorded;
        _taId = taId;
        _earned = earned;
        _matchesGml = matchesGml;
    } 
    
    public String getDateRecorded() {
        return _dateRecorded;
    }
    
    public int getTAId() {
        return _taId;
    }
    
    public double getEarned() {
        return _earned;
    }
    
    public boolean doesMatchGml(){
        return _matchesGml;
    }
}