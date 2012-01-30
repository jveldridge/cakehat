package cakehat.newdatabase;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.AssignmentsBuilder;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    
    private List<Assignment> _assignments = null;
    
    private Map<Integer, Assignment> _asgnIdMap = null;
    
    /**
     * Maps a group's ID in the database to the corresponding Group object.
     */
    private final Map<Integer, Group> _groupIdMap = new ConcurrentHashMap<Integer, Group>();
    
    /**
     * Maps an assignment ID to a map that associates each student ID with the Group object for that student and
     * assignment.
     */
    private Map<Integer, Map<Integer, Group>> _groupsCache = new ConcurrentHashMap<Integer, Map<Integer, Group>>();
    
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
    public void addGroup(DbGroup toAdd) throws ServicesException {
        this.addGroups(ImmutableSet.of(toAdd));
    }
    
    @Override
    public void addGroups(Set<DbGroup> toAdd) throws ServicesException {
        //ensure that no student in a group to add is already to assigned to a group for the corresponding assignment
        for (DbGroup group : toAdd) {
            for (int memberId : group.getMemberIds()) {
                if (this.safeMapOfMapsGet(_groupsCache, group.getAssignmentId()).containsKey(memberId)) {
                    throw new ServicesException("Student [" + _studentIdMap.get(memberId) + "] is already "
                            + "assigned to a group for assignment [" + _asgnIdMap.get(group.getAssignmentId()) + "]. "
                            + "No groups have been added to the database.");
                }
            }
        }
        
        try {
            Allocator.getDatabaseV5().addGroups(toAdd);
            
            //update the cache
            for (DbGroup dbGroup : toAdd) {
                Assignment asgn = _asgnIdMap.get(dbGroup.getAssignmentId());
                Set<Student> members = this.idsToStudents(dbGroup.getMemberIds(), new CopyOnWriteArraySet<Student>());
                
                Group newGroup = new Group(dbGroup.getId(), asgn, dbGroup.getName(), members);
                _groupIdMap.put(newGroup.getId(), newGroup);
                for (Student member : newGroup.getMembers()) {
                    this.safeMapOfMapsGet(_groupsCache, asgn.getId()).put(member.getId(), newGroup);
                }
            }
        } catch (SQLException ex) {
            throw new ServicesException("An error occurred adding the groups to "
                    + "the database; no groups have been added.", ex);
        } catch (CakeHatDBIOException ex) {
            throw new ServicesException("An error occurred adding the groups to "
                    + "the database; no groups have been added.", ex);
        }
    }

    @Override
    public Group getGroup(Assignment asgn, Student student) throws ServicesException {
        if (!this.safeMapOfMapsGet(_groupsCache, asgn.getId()).containsKey(student.getId())) {
            this.updateDataCache();

            if (!this.safeMapOfMapsGet(_groupsCache, asgn.getId()).containsKey(student.getId())) {
                //if the assignment does not have groups, create group of one
                if (!asgn.hasGroups()) {
                    this.addGroup(new DbGroup(asgn, student));
                }
            }
        }
        
        return this.safeMapOfMapsGet(_groupsCache, asgn.getId()).get(student.getId());
    }
    
    @Override
    public Set<Group> getGroups(Assignment asgn) throws ServicesException {
        Set<Integer> groupIDs;
        try {
            groupIDs = Allocator.getDatabaseV5().getGroups(asgn.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read groups for assignment from the database", ex);
        }
        
        //if the assignment is not a group assignment, create a group of one for each student who does not already have one
        if (!asgn.hasGroups()) {
            Set<DbGroup> groupsToAdd = new HashSet<DbGroup>();
            
            for (Student student : _studentIdMap.values()) {
                if (!this.safeMapOfMapsGet(_groupsCache, asgn.getId()).containsKey(student.getId())) {
                    groupsToAdd.add(new DbGroup(asgn, student));
                }
            }
            
            if (!groupsToAdd.isEmpty()) {
                this.addGroups(groupsToAdd);

                try {
                    groupIDs = Allocator.getDatabaseV5().getGroups(asgn.getId());
                } catch (SQLException ex) {
                    throw new ServicesException("Could not read groups for assignment from the database "
                            + "(after adding auto-groups of one for students who did not have them).", ex);
                }
            }
        }
        
        return Collections.unmodifiableSet(this.idsToGroups(groupIDs, new HashSet<Group>(groupIDs.size())));
    }
    
    @Override
    public void removeGroups(Assignment asgn) throws ServicesException {
        try {
            Allocator.getDatabaseV5().removeGroups(asgn.getId());
            
            //if removing groups was successful, update caches
            this.safeMapOfMapsGet(_groupsCache, asgn.getId()).clear();
        } catch (SQLException ex) {
            throw new ServicesException("Could not remove groups for assignment [" + asgn + "] from the database.");
        }
    }
    
    @Override
    public List<Assignment> getAssignments() throws ServicesException
    {
        if(_assignments == null)
        {
            try
            {
                _assignments = new AssignmentsBuilder().buildAssigments(Allocator.getDatabaseV5().getAssignments());
                
                _asgnIdMap = new ConcurrentHashMap<Integer, Assignment>(_assignments.size());
                for (Assignment asgn : _assignments) {
                    _asgnIdMap.put(asgn.getId(), asgn);
                }
            }
            catch(SQLException ex)
            {
                throw new ServicesException(ex);
            }
        }
        
        return _assignments;
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
            
            //create an object for any student that has been added to the database
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
            Set<DbGroup> groups = Allocator.getDatabaseV5().getGroups();
            
            //remove any Group object for which the group has been removed from the database
            Set<Integer> validGroupIds = new HashSet<Integer>();
            for (DbGroup dbGroup : groups) {
                validGroupIds.add(dbGroup.getId());
            }
            for (Group group : _groupIdMap.values()) {
                if (!validGroupIds.contains(group.getId())) {
                    _groupIdMap.remove(group.getId());
                    for (Student member : group.getMembers()) {
                        this.safeMapOfMapsGet(_groupsCache, group.getAssignment().getId()).remove(member.getId());
                    }
                }
            }
            
            //update the groups in the map and create an object for any group that has been added to the database
            for (DbGroup dbGroup : groups) {
                if (_groupIdMap.containsKey(dbGroup.getId())) {
                    Group group = _groupIdMap.get(dbGroup.getId());
                    
                    //calling Group.update(...) will mutate any out-of-date fields of the Group object
                    Set<Student> members = this.idsToStudents(dbGroup.getMemberIds(), new HashSet<Student>());
                    group.update(dbGroup.getName(), members);
                }
                else {
                    Assignment asgn = _asgnIdMap.get(dbGroup.getAssignmentId());
                    Set<Student> members = this.idsToStudents(dbGroup.getMemberIds(), new CopyOnWriteArraySet<Student>());
                    
                    Group newGroup = new Group(dbGroup.getId(), asgn, dbGroup.getName(), members);
                    _groupIdMap.put(newGroup.getId(), newGroup);
                    for (Student member : newGroup.getMembers()) {
                        this.safeMapOfMapsGet(_groupsCache, asgn.getId()).put(member.getId(), newGroup);
                    }
                }
            }
        } catch (SQLException ex) {
            throw new ServicesException(ex);
        }
    }
    
    private <S, T, U> Map<T, U> safeMapOfMapsGet(Map<S, Map<T, U>> mapOfMaps, S key) {
        if (!mapOfMaps.containsKey(key)) {
            mapOfMaps.put(key, new HashMap<T, U>());
        }
        
        return mapOfMaps.get(key);
    }
    
    private <T extends Collection<Integer>, S extends Collection<Student>> S idsToStudents(T ids, S students) throws ServicesException {
        for (Integer id : ids) {
            if (!_studentIdMap.containsKey(id)) {
                this.updateDataCache();
                if (!_studentIdMap.containsKey(id)) {
                    throw new ServicesException("Student id [" + id + "] does not map to a Student object.");
                }
            }
            students.add(_studentIdMap.get(id));
        }
        
        return students;
    }
    
    private <T extends Collection<Integer>, S extends Collection<Group>> S idsToGroups(T ids, S groups) throws ServicesException {
        for (Integer id : ids) {
            groups.add(this.groupIdToGroup(id));
        }
        
        return groups;
    }
    
    private Group groupIdToGroup(int groupId) throws ServicesException {
        if (!_groupIdMap.containsKey(groupId)) {
            this.updateDataCache();
            if (!_groupIdMap.containsKey(groupId)) {
                throw new ServicesException("Group id [" + groupId + "] does not map to a Group object.");
            }
        }
        
        return _groupIdMap.get(groupId);
    }
    
}
