package cakehat.newdatabase;

/**
 * Represents information about a grade record as stored in the database.  
 * This class should used only as a return type for {@link DatabaseV5} methods 
 * supporting {@link DataServicesV5} methods.
 * 
 * 
 * @author yf6
 */
public class GradeRecord {
    
    private final double _earned;
    private final boolean _matchesGml;
    
    GradeRecord(double earned, boolean matchesGml) {
        _earned = earned;
        _matchesGml = matchesGml;
    } 
    
    public double getEarned() {
        return _earned;
    }
    
    public boolean doesMatchGml(){
        return _matchesGml;
    }
}
