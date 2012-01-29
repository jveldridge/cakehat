package cakehat.newdatabase;

import cakehat.Allocator;
import cakehat.assignment.DeadlineInfo;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.joda.time.DateTime;

/**
 *
 * @author Hannah
 */
public class DataServicesV5Impl implements DataServicesV5 {
    
    /**
     * Maps a student's ID in the database to the corresponding Student object.
     */
    private final Map<Integer, Student> _studentIdMap = new ConcurrentHashMap<Integer, Student>();
    
    /**
     * Maps a student's login to the corresponding Student object.
     */
    private final Map<String, Student> _loginMap = new ConcurrentHashMap<String, Student>();
    
    private final Set<Student> _enabledStudents = new CopyOnWriteArraySet<Student>();
    
    private Set<TA> _tas = null;
    
    @Override
    public Set<Student> getStudents() throws ServicesException {
        //note: this is non-ideal- want to return a view of a Set, but the underlying data structure is a Collection
        return ImmutableSet.copyOf(_studentIdMap.values());
    }

    @Override
    public Set<Student> getEnabledStudents() throws ServicesException {
        return Collections.unmodifiableSet(_enabledStudents);
    }

    @Override
    public TA getGrader(Part part, Group group) throws ServicesException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setGrader(Part part, Group group, TA ta) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PartGrade getEarned(Group group, Part part) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setEarned(Group group, Part part, Double earned, boolean matchesGml) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<TA> getTAs() throws ServicesException
    {
        if (_tas == null) {
            try {
                Set<DbTA> dbTAs = Allocator.getDatabaseV5().getTAs();
                
                ImmutableSet.Builder<TA> tasBuilder = ImmutableSet.builder();
                for (DbTA ta : dbTAs) {
                    tasBuilder.add(new TA(ta));
                }
                
                _tas = tasBuilder.build();
            } catch (SQLException ex) {
                throw new ServicesException(ex);
            }
        }
        
        return _tas;
    }

    @Override
    public DeadlineInfo getDeadlineInfo(GradableEvent gradableEvent) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HandinTime getHandinTime(GradableEvent gradableEvent, Group group) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setHandinTime(GradableEvent gradableEvent, Group group, DateTime handinTime) throws ServicesException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void updateDataCache() throws ServicesException {
        try {
            /*
             * Get Students
             */
            Set<DbStudent> students = Allocator.getDatabaseV5().getStudents();
            
            //add any student that has been added to the database
            for (DbStudent dbStudent : students) {
                if (!_studentIdMap.containsKey(dbStudent.getId())) {
                    Student newStudent = new Student(dbStudent);
                    _studentIdMap.put(newStudent.getId(), newStudent);
                    _loginMap.put(newStudent.getLogin(), newStudent);
                }
                
                Student student = _studentIdMap.get(dbStudent.getId());
                if (dbStudent.isEnabled()) {
                    _enabledStudents.add(student);
                }
                else {
                    _enabledStudents.remove(student);
                }
            }
            
            /**
             * Get Groups
             */
            //TODO get and cache groups
        } catch (SQLException ex) {
            throw new ServicesException(ex);
        }
    }
    
}
