package cakehat.newdatabase;

import cakehat.Allocator;
import cakehat.assignment.Assignment;
import cakehat.assignment.AssignmentsBuilder;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
    
    /**
     * Maps a TA's ID in the database to the corresponding TA object.
     */
    private final Map<Integer, TA> _taIdMap = new HashMap<Integer, TA>();
    
    private List<Assignment> _assignments = null;
    
    private Map<Integer, Assignment> _asgnIdMap = null;
    
    private Map<Integer, Part> _partIdMap = null;
    
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
        int id = 0;
        try {
            id = Allocator.getDatabaseV5().getGrader(part.getId(), group.getId());
            return _taIdMap.get(id);
        } catch (SQLException ex) {
            throw new ServicesException("Could not get grader for group [" + group.getName() + "] "
                    + "on part [" + part.getName() + "] from the database.", ex);
        }
    }

    @Override
    public void setGrader(Part part, Group group, TA ta) throws ServicesException
    {
        try {
            Allocator.getDatabaseV5().assignGroup(group.getId(), part.getId(), ta.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not set grader for group [" + group.getName() + "]"
                    + " on part [" + part.getName() + "].", ex);
        }
    }

    @Override
    public PartGrade getEarned(Group group, Part part) throws ServicesException
    {
        try {
            GradeRecord record = Allocator.getDatabaseV5().getEarned(group.getId(), part.getId());
            
            TA ta = getGrader(part, group);
            PartGrade grade = null;
            if (record != null) {
                grade = new PartGrade(part, group, ta, new DateTime(record.getDateRecorded()), record.getEarned(), record.doesMatchGml());
            }
            return grade;
        } catch (SQLException ex) {
            throw new ServicesException("Could not get the score for group [" + group.getName() + "]"
                    + " on part [" + part.getName() + "].", ex);
        }
    }

    @Override
    public void setEarned(Group group, Part part, Double earned, boolean matchesGml) throws ServicesException
    {
        int taId = Allocator.getUserUtilities().getUserId();
        try {
            Allocator.getDatabaseV5().setEarned(group.getId(), part.getId(), taId, earned, matchesGml, new DateTime().toString());
        } catch (SQLException ex) {
            throw new ServicesException("Could not store the score for group [" + group.getName() + "]"
                    + " on part " + part.getName() + "].", ex);
        }
    }

    @Override
    public Set<TA> getTAs() throws ServicesException
    {
        if (_tas == null) {
            try {
                Set<DbTA> dbTAs = Allocator.getDatabaseV5().getTAs();
                
                ImmutableSet.Builder<TA> tasBuilder = ImmutableSet.builder();
                for (DbTA ta : dbTAs) {
                    TA toAdd = new TA(ta);
                    tasBuilder.add(toAdd);
                    _taIdMap.put(toAdd.getId(), toAdd);
                }
                
                _tas = tasBuilder.build();
            } catch (SQLException ex) {
                throw new ServicesException(ex);
            }
        }
        
        return _tas;
    }
    
    @Override    
    public TA getTA(int taId) throws ServicesException {
        //TODO: dehackify
        for (TA ta : this.getTAs()) {
            if (ta.getId() == taId) {
                return ta;
            }
        }
        
        return null;
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

        DbGradableEvent dbEvent = null;
        try {
            dbEvent = Allocator.getDatabaseV5().getDbGradableEvent(gradableEvent.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Unable to retrieve gradable event [" + gradableEvent.getName() + "] from the database.", ex);
        }
       
        if (dbEvent == null) {
            throw new ServicesException("Could not find gradable event [" + gradableEvent.getName() + "] in database.");
        }
        
        DeadlineInfo info = null;
        if (dbEvent.getDeadlineType().equals(DeadlineInfo.Type.VARIABLE)) {
            info = DeadlineInfo.newVariableDeadlineInfo(dbEvent.getOnTimeDate(), dbEvent.getLateDate(), dbEvent.getLatePoints(), 
                    dbEvent.getLatePeriod());
        }
        else if (dbEvent.getDeadlineType().equals(DeadlineInfo.Type.FIXED)) {
            info = DeadlineInfo.newFixedDeadlineInfo(dbEvent.getEarlyDate(), dbEvent.getEarlyPoints(), dbEvent.getOnTimeDate()
                    , dbEvent.getLateDate(), dbEvent.getLatePoints());
        }
        else if (dbEvent.getDeadlineType().equals(DeadlineInfo.Type.NONE)) {
            info = DeadlineInfo.newNoDeadlineInfo();
        }
        return info;
    }

    @Override
    public HandinTime getHandinTime(GradableEvent gradableEvent, Group group) throws ServicesException
    {
        try {
            HandinRecord record = Allocator.getDatabaseV5().getHandinTime(gradableEvent.getId(), group.getId());
            TA ta = _taIdMap.get(Allocator.getUserUtilities().getUserId());
            if (record != null) {
                return new HandinTime(gradableEvent, group, ta, new DateTime(), DateTime.parse(record.getTime()));
            }
            return null;
        } catch (SQLException ex) {
            throw new ServicesException("Unable to determine handin time for group [" + group.getName() + 
                    "] for gradable event " + gradableEvent.getName() + ".", ex);
        }
    }

    @Override
    public void setHandinTime(GradableEvent gradableEvent, Group group, DateTime handinTime) throws ServicesException
    {
        Map<Group, DateTime> map = new HashMap<Group, DateTime>();
        map.put(group, handinTime);
        this.setHandinTimes(gradableEvent, map);
    }
    
    @Override
    public void setHandinTimes(GradableEvent gradableEvent, Map<Group, DateTime> statuses) throws ServicesException {
        try {
            for (Group group : statuses.keySet()) {
                Allocator.getDatabaseV5().setHandinTime(gradableEvent.getId(), group.getId(),
                    statuses.get(group).toString(), new DateTime().toString(), Allocator.getUserUtilities().getUserId());
            }
        } catch (SQLException ex) {
            throw new ServicesException("Could not set handin statuses for given groups.", ex);
        }
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

    @Override
    public void setStudentEnabled(Student student, boolean enabled) throws ServicesException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void blacklistStudents(Set<Student> students, TA ta) throws ServicesException {
        try {
            Allocator.getDatabaseV5().blacklistStudents(this.studentsToIDs(students), ta.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Students could not be added to the TA's blacklist."
                    + "Attempted to add: " + students + ".", ex);
        }
    }

    @Override
    public void unBlacklistStudents(Set<Student> students, TA ta) throws ServicesException {
        try {
            Allocator.getDatabaseV5().unBlacklistStudents(this.studentsToIDs(students), ta.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Students could not be removed from the TA's blacklist.", ex);
        }
    }

    @Override
    public Collection<Student> getBlacklistedStudents() throws ServicesException {
        try {
            Collection<Integer> blacklistedIDs = Allocator.getDatabaseV5().getBlacklistedStudents();
            return this.idsToStudents(blacklistedIDs, new ArrayList<Student>(blacklistedIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklisted students from the database.", ex);
        }
    }

    @Override
    public Collection<Student> getBlacklist(TA ta) throws ServicesException {
        try {
            Collection<Integer> blacklistedIDs = Allocator.getDatabaseV5().getBlacklist(ta.getId());
            return this.idsToStudents(blacklistedIDs, new ArrayList<Student>(blacklistedIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklisted students from the database", ex);
        }
    }

    @Override
    public boolean isDistEmpty(Assignment asgn) throws ServicesException {
        Set<Integer> partIDs = new HashSet<Integer>();
        for (GradableEvent ge : asgn.getGradableEvents()) {
            for (Part p : ge.getParts()) {
                partIDs.add(p.getId());
            }
        }
        
        try {
            return Allocator.getDatabaseV5().isDistEmpty(partIDs);
        } catch (SQLException ex) {
            throw new ServicesException("Could not determine whether the distribution "
                    + "is empty for assignment [" + asgn + "].", ex);
        }
    }

    @Override
    public Map<TA, Collection<Group>> getDistribution(Part dp) throws ServicesException {
        Map<TA, Collection<Group>> dist = new HashMap<TA, Collection<Group>>();
        Map<Integer, Collection<Integer>> idDist;
        try {
            idDist = Allocator.getDatabaseV5().getDistribution(dp.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read distribution for part [" +
                    dp + "] from the database.", ex);
        }
        
        for (TA ta : this.getTAs()) {
            if (idDist.containsKey(ta.getId())) {
                Collection<Integer> toGrade = idDist.get(ta.getId());
                dist.put(ta, this.idsToGroups(toGrade, new ArrayList<Group>(toGrade.size())));
            }
            else {
                //for any TA who has not been assigned any groups to grade,
                //add an empty collection to the map 
                dist.put(ta, Collections.EMPTY_LIST);
            }
        }
        
        return dist;
    }

    @Override
    public void setDistribution(Map<Part, Map<TA, Collection<Group>>> distribution) throws ServicesException {
        try {
            Map<Integer, Map<Integer, Set<Integer>>> distForDb = new HashMap<Integer, Map<Integer, Set<Integer>>>();
            for (Part part : distribution.keySet()) {
                distForDb.put(part.getId(), new HashMap<Integer, Set<Integer>>());
                
                for (TA ta : distribution.get(part).keySet()) {
                    distForDb.get(part.getId()).put(ta.getId(), new HashSet<Integer>(distribution.get(part).get(ta).size()));
                    
                    for (Group group : distribution.get(part).get(ta)) {
                        distForDb.get(part.getId()).get(ta.getId()).add(group.getId());
                    }
                }
            }
            
            Allocator.getDatabaseV5().setDistribution(distForDb);
        } catch (SQLException ex) {
            throw new ServicesException("Could write distribution to the database.", ex);
        }
    }

    @Override
    public void assignGroup(Group group, Part part, TA ta) throws ServicesException {
        try {
            Allocator.getDatabaseV5().assignGroup(group.getId(), part.getId(), ta.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Group [" + group + "] could not be assigned "
                    + "to TA [" + ta + "] on part [" + part + "].", ex);
        }
    }

    @Override
    public void unassignGroup(Group group, Part part, TA ta) throws ServicesException {
        try {
            Allocator.getDatabaseV5().unassignGroup(group.getId(), part.getId(), ta.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Group [" + group + "] could not be unassigned "
                    + "from TA [" + ta + "] on part [" + part + "].", ex);
        }
    }

    @Override
    public Collection<Group> getAssignedGroups(Part part, TA ta) throws ServicesException {
        Collection<Integer> groupIDs;
        try {
            groupIDs = Allocator.getDatabaseV5().getAssignedGroups(part.getId(), ta.getId());
            return this.idsToGroups(groupIDs, new ArrayList<Group>(groupIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read groups assigned to TA [" +
                    ta + "] on part [" + part + "] from the database.", ex);
        }
    }

    @Override
    public Collection<Group> getAssignedGroups(Part part) throws ServicesException {
        Collection<Integer> groupIDs;
        try {
            groupIDs = Allocator.getDatabaseV5().getAssignedGroups(part.getId());
            return this.idsToGroups(groupIDs, new ArrayList<Group>(groupIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read all assigned groups "
                    + "for part [" + part + "] from the database.", ex);
        }   
    }

    @Override
    public Set<Part> getDPsWithAssignedGroups(TA ta) throws ServicesException {
        try {
            Set<Part> toReturn = new HashSet<Part>();
            for (int partID : Allocator.getDatabaseV5().getPartsWithAssignedGroups(ta.getId())) {
                toReturn.add(this.partIdToPart(partID));
            }
            
            return toReturn;
        } catch (SQLException ex) {
            throw new ServicesException("Could not read DPs with assigned groups "
                    + "for TA [" + ta + "] from the database.", ex);
        }
    }

    @Override
    public void resetDatabase() throws ServicesException {
        try {
            Allocator.getDatabaseV5().resetDatabase();
        } catch (SQLException ex) {
            throw new ServicesException("Could not reset database.", ex);
        }
    }
    
    @Override
    public Student getStudentFromLogin(String studentLogin) throws ServicesException {
        if (!_loginMap.containsKey(studentLogin)) {
            this.updateDataCache();
        }
        return _loginMap.get(studentLogin);
    }
    
    @Override
    public boolean isStudentLoginInDatabase(String studentLogin) throws ServicesException {
        if (!_loginMap.containsKey(studentLogin)) {
            this.updateDataCache();
        }
        return _loginMap.containsKey(studentLogin);
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
    
    private Set<Integer> studentsToIDs(Set<Student> students) {
        return this.studentsToIDs(students, new HashSet<Integer>(students.size()));
    }
    
    private <T extends Set<Student>, S extends Set<Integer>> S studentsToIDs(T students, S ids) {
        for (Student student : students) {
            ids.add(student.getId());
        }
        
        return ids;
    }
    
    private Part partIdToPart(int partID) throws ServicesException {
        if(_partIdMap == null)
        {
            ImmutableMap.Builder<Integer, Part> builder = ImmutableMap.builder();
            
            for (Assignment asgn : this.getAssignments()) {
                for (GradableEvent ge : asgn.getGradableEvents()) {
                    for (Part part : ge.getParts()) {
                        builder.put(part.getId(), part);
                    }
                }
            }
            _partIdMap = builder.build();
        }

        return _partIdMap.get(partID);
    }
}
