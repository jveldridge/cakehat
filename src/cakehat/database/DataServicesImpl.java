package cakehat.database;

import cakehat.Allocator;
import cakehat.InitializationException;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.AssignmentsBuilder;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.joda.time.DateTime;
import support.utils.Pair;
import support.utils.posix.NativeException;

/**
 *
 * @author Hannah
 */
public class DataServicesImpl implements DataServices {
    
    /**
     * Maps a student's ID in the database to the corresponding Student object.
     */
    private final Map<Integer, Student> _studentIdMap = new ConcurrentHashMap<Integer, Student>();
    
    /**
     * Maps a student's login to the corresponding Student object.
     */
    private final Map<String, Student> _studentLoginMap = new ConcurrentHashMap<String, Student>();
    
    private final Set<Student> _enabledStudents = new CopyOnWriteArraySet<Student>();
    
    /**
     * Maps a group's ID in the database to the corresponding Group object.
     */
    private final Map<Integer, Group> _groupIdMap = new ConcurrentHashMap<Integer, Group>();
    
    /**
     * Maps an assignment ID to a map that associates each student ID with the Group object for that student and
     * assignment.
     */
    private Map<Integer, Map<Integer, Group>> _groupsCache = new ConcurrentHashMap<Integer, Map<Integer, Group>>();
    
    private final ImmutableSet<TA> _tas;
    private final ImmutableSet<TA> _defaultGraders;
    private final ImmutableMap<Integer, TA> _taIdMap;
    
    private final ImmutableList<Assignment> _assignments;
    private final ImmutableMap<Integer, Assignment> _asgnIdMap;
    private final ImmutableMap<Integer, GradableEvent> _geIdMap;
    private final ImmutableMap<Integer, Part> _partIdMap;
    
    public DataServicesImpl()
    {
        try
        {
            //Assignments
            _assignments = new AssignmentsBuilder().buildAssigments(Allocator.getDatabase().getAssignments());
            ImmutableMap.Builder<Integer, Assignment> asgnMapBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<Integer, GradableEvent> geMapBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<Integer, Part> partMapBuilder = ImmutableMap.builder();
            for(Assignment asgn : _assignments)
            {
                asgnMapBuilder.put(asgn.getId(), asgn);
                _groupsCache.put(asgn.getId(), new ConcurrentHashMap<Integer, Group>());
                
                for(GradableEvent ge : asgn)
                {
                    geMapBuilder.put(ge.getId(), ge);
                    
                    for(Part part : ge)
                    {
                        partMapBuilder.put(part.getId(), part);
                    }
                }
            }
            _asgnIdMap = asgnMapBuilder.build();
            _geIdMap = geMapBuilder.build();
            _partIdMap = partMapBuilder.build();
            
            //TAs
            ImmutableSet.Builder<TA> tasBuilder = ImmutableSet.builder();
            ImmutableMap.Builder<Integer, TA> taIdMapBuilder = ImmutableMap.builder();
            ImmutableSet.Builder<TA> defaultGradersBuilder = ImmutableSet.builder();
            Set<DbTA> dbTAs = Allocator.getDatabase().getTAs();
            for(DbTA ta : dbTAs)
            {
                TA toAdd = new TA(ta);
                tasBuilder.add(toAdd);
                taIdMapBuilder.put(toAdd.getId(), toAdd);
                
                if (toAdd.isDefaultGrader()) {
                    defaultGradersBuilder.add(toAdd);
                }
            }
            _tas = tasBuilder.build();
            _taIdMap = taIdMapBuilder.build();
            _defaultGraders = defaultGradersBuilder.build();
        }
        catch(SQLException e)
        {
            throw new InitializationException("Unable to initialize data services", e);
        }
        
        try
        {
            //Load student and groups cache
            this.updateDataCache();
        }
        catch(ServicesException e)
        {
            throw new InitializationException("Unable to load data cache", e);
        }
    }
    
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
    public void addStudentsByLogin(Set<String> studentLogins) throws ServicesException {
        Set<DbStudent> toAdd = new HashSet<DbStudent>();
        for (String studentLogin : studentLogins) {
            try {
                if (Allocator.getUserServices().isInStudentGroup(studentLogin)) {
                    throw new ServicesException("Login [" + studentLogin + "] is not in the course's student group. "
                            + "No students have been added to the database.  To add the student anyway, use the config "
                            + "manager.");
                }
            } catch (NativeException ex) {
                throw new ServicesException("Could not determine if login [" + studentLogin + "] is in the course's"
                        + "student group.  No students have been added to the database.  To add the student anyway, use"
                        + "the config manager.", ex);
            }
            try {
                String[] nameParts = Allocator.getUserUtilities().getUserName(studentLogin).split(" ");
                String firstName = nameParts[0];
                String lastName = nameParts[nameParts.length - 1];
                String email = studentLogin + "@" + Allocator.getConstants().getEmailDomain();
               
                toAdd.add(new DbStudent(studentLogin, firstName, lastName, email));
            } catch (NativeException ex) {
                throw new ServicesException("Cannot determine the name of student with login [" + studentLogin + "].  No "
                        + "students have been added to the database.", ex);
            }
        }
        
        this.addStudents(toAdd);
    }
    
    @Override
    public void addStudents(Set<DbStudent> students) throws ServicesException {
        for (DbStudent student : students) {
            if (student.getId() != null) {
                throw new RuntimeException("Student [" + student + "] already has an ID set, and thus cannot be added " +
                                           "to the database.  No students have been added.");
            }
            if (!student.isEnabled()) {
                throw new RuntimeException("Student [" + student + "] is not enabled, and thus cannot be added " +
                                           "to the database.  No students have been added.");
            }
        }
        try {
            Allocator.getDatabase().putStudents(students);
            
            //update the cache
            for (DbStudent dbStudent : students) {
                Student newStudent = new Student(dbStudent);
                _studentIdMap.put(newStudent.getId(), newStudent);
                _studentLoginMap.put(newStudent.getLogin(), newStudent);
                _enabledStudents.add(newStudent);
            }
        } catch (SQLException ex) {
            throw new ServicesException("Could not add students to the database.", ex);
        }
    }

    @Override
    public TA getGrader(Part part, Group group) throws ServicesException {
        try {
            return _taIdMap.get(Allocator.getDatabase().getGrader(part.getId(), group.getId()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not get grader for group [" + group.getName() + "] "
                    + "on part [" + part.getName() + "] from the database.", ex);
        }
    }

    @Override
    public void setGrader(Part part, Group group, TA ta) throws ServicesException
    {
        try {
            if (ta == null) {
                Allocator.getDatabase().unassignGroup(group.getId(), part.getId());
            } else {
                Allocator.getDatabase().assignGroup(group.getId(), part.getId(), ta.getId());
            }
        } catch (SQLException ex) {
            throw new ServicesException("Could not set grader for group [" + group.getName() + "]"
                    + " on part [" + part.getName() + "].", ex);
        }
    }

    @Override
    public PartGrade getEarned(Group group, Part part) throws ServicesException
    {
        try {
            GradeRecord record = Allocator.getDatabase().getEarned(group.getId(), part.getId());
            
            PartGrade grade = null;
            if (record != null) {
                grade = new PartGrade(part, group, _taIdMap.get(record.getTAId()),
                            new DateTime(record.getDateRecorded()), record.getEarned(), record.isSubmitted());
            }
            return grade;
        } catch (SQLException ex) {
            throw new ServicesException("Could not get the score for group [" + group.getName() + "]"
                    + " on part [" + part.getName() + "].", ex);
        }
    }
    
    @Override
    public Map<Group, PartGrade> getEarned(Set<Group> groups, Part part) throws ServicesException
    {
        try
        {
            Map<Integer, Group> idsToGroups = new HashMap<Integer, Group>();
            for(Group group : groups)
            {
                idsToGroups.put(group.getId(), group);
            }
            
            Map<Integer, GradeRecord> records = Allocator.getDatabase().getEarned(part.getId(), idsToGroups.keySet());
            
            Map<Group, PartGrade> grades = new HashMap<Group, PartGrade>(records.size());
            for(Entry<Integer, GradeRecord> entry : records.entrySet())
            {
                GradeRecord record = entry.getValue();
                Group group = idsToGroups.get(entry.getKey());
                PartGrade grade = new PartGrade(part, group, _taIdMap.get(record.getTAId()),
                        new DateTime(record.getDateRecorded()), record.getEarned(), record.isSubmitted());
                grades.put(group, grade);
            }
            
            return grades;
        }
        catch(SQLException e)
        {
            throw new ServicesException("Unable to retrieve earned for part " + part.getFullDisplayName() + " for " +
                    "groups: " + groups, e);
        }
    }

    @Override
    public void setEarned(Group group, Part part, Double earned, boolean matchesGml) throws ServicesException
    {
        int taId = Allocator.getUserUtilities().getUserId();
        try {
            Allocator.getDatabase().setEarned(group.getId(), part.getId(), taId, earned, matchesGml, new DateTime().toString());
        } catch (SQLException ex) {
            throw new ServicesException("Could not store the score for group [" + group.getName() + "]"
                    + " on part " + part.getName() + "].", ex);
        }
    }

    @Override
    public void setEarned(Part part, Map<Group, Pair<Double, Boolean>> earned) throws ServicesException
    {
        Map<Integer, Pair<Double, Boolean>> earnedWithIds = new HashMap<Integer, Pair<Double, Boolean>>();
        for(Entry<Group, Pair<Double, Boolean>> entry : earned.entrySet())
        {
            earnedWithIds.put(entry.getKey().getId(), entry.getValue());
        }
        
        try
        {
            Allocator.getDatabase().setEarned(part.getId(), Allocator.getUserUtilities().getUserId(),
                    new DateTime().toString(), earnedWithIds);
        }
        catch(SQLException e)
        {
            throw new ServicesException("Could not store earned for part: " + part.getFullDisplayName() +" for " +
                    "groups: " + earned.keySet(), e);
        }
    }

    @Override
    public void setEarnedSubmitted(Part part, Map<Group, Boolean> submitted) throws ServicesException
    {
        Map<Integer, Boolean> submittedWithIds = new HashMap<Integer, Boolean>();
        for(Entry<Group, Boolean> entry : submitted.entrySet())
        {
            submittedWithIds.put(entry.getKey().getId(), entry.getValue());
        }
        
        try
        {
            Allocator.getDatabase().setEarnedSubmitted(part.getId(), Allocator.getUserUtilities().getUserId(),
                    new DateTime().toString(), submittedWithIds);
        }
        catch(SQLException e)
        {
            throw new ServicesException("Could not store submitted status for part: " + part.getFullDisplayName() +
                    " for  groups: " + submitted.keySet(), e);
        }
    }

    @Override
    public Set<TA> getTAs()
    {   
        return _tas;
    }
    
    @Override
    public Set<TA> getDefaultGraders() {
        return _defaultGraders;
    }
    
    @Override    
    public TA getTA(int taId)
    {
        return _taIdMap.get(taId);
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
                if (_groupsCache.get(group.getAssignmentId()).containsKey(memberId)) {
                    throw new ServicesException("Student [" + _studentIdMap.get(memberId) + "] is already "
                            + "assigned to a group for assignment [" + _asgnIdMap.get(group.getAssignmentId()) + "]. "
                            + "No groups have been added to the database.");
                }
            }
        }
        
        try {
            Allocator.getDatabase().addGroups(toAdd);
            
            //update the cache
            for (DbGroup dbGroup : toAdd) {
                Assignment asgn = _asgnIdMap.get(dbGroup.getAssignmentId());
                Set<Student> members = this.idsToStudents(dbGroup.getMemberIds(), new CopyOnWriteArraySet<Student>());
                
                Group newGroup = new Group(dbGroup.getId(), asgn, dbGroup.getName(), members);
                _groupIdMap.put(newGroup.getId(), newGroup);
                for (Student member : newGroup.getMembers()) {
                    _groupsCache.get(asgn.getId()).put(member.getId(), newGroup);
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
        if (!_groupsCache.get(asgn.getId()).containsKey(student.getId())) {
            this.updateDataCache();

            if (!_groupsCache.get(asgn.getId()).containsKey(student.getId())) {
                //if the assignment does not have groups, create group of one
                if (!asgn.hasGroups()) {
                    this.addGroup(new DbGroup(asgn, student));
                }
            }
        }
        
        return _groupsCache.get(asgn.getId()).get(student.getId());
    }
    
    @Override
    public Set<Group> getGroups(Assignment asgn) throws ServicesException {
        Set<Integer> groupIDs;
        try {
            groupIDs = Allocator.getDatabase().getGroups(asgn.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read groups for assignment from the database", ex);
        }
        
        //if the assignment is not a group assignment, create a group of one for each student who does not already have one
        if (!asgn.hasGroups()) {
            Set<DbGroup> groupsToAdd = new HashSet<DbGroup>();
            
            for (Student student : _studentIdMap.values()) {
                if (!_groupsCache.get(asgn.getId()).containsKey(student.getId())) {
                    groupsToAdd.add(new DbGroup(asgn, student));
                }
            }
            
            if (!groupsToAdd.isEmpty()) {
                this.addGroups(groupsToAdd);

                try {
                    groupIDs = Allocator.getDatabase().getGroups(asgn.getId());
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
            Allocator.getDatabase().removeGroups(asgn.getId());
            
            //if removing groups was successful, update caches
            _groupsCache.get(asgn.getId()).clear();
        } catch (SQLException ex) {
            throw new ServicesException("Could not remove groups for assignment [" + asgn + "] from the database.");
        }
    }
    
    @Override
    public List<Assignment> getAssignments()
    {   
        return _assignments;
    }

    @Override
    public DeadlineInfo getDeadlineInfo(GradableEvent gradableEvent) throws ServicesException
    {
        DbGradableEvent dbEvent = null;
        try
        {
            dbEvent = Allocator.getDatabase().getDbGradableEvent(gradableEvent.getId());
        }
        catch(SQLException ex)
        {
            throw new ServicesException("Unable to retrieve gradable event [" + gradableEvent.getName() +
                    "] from the database.", ex);
        }
       
        if(dbEvent == null)
        {
            throw new ServicesException("Gradable event [" + gradableEvent.getName() + "] no longer in database.");
        }
        
        DeadlineInfo info = DeadlineInfo.newNoDeadlineInfo();
        if(DeadlineInfo.Type.VARIABLE.equals(dbEvent.getDeadlineType()))
        {
            info = DeadlineInfo.newVariableDeadlineInfo(dbEvent.getOnTimeDate(),
                                                        dbEvent.getLateDate(), dbEvent.getLatePoints(), 
                                                        dbEvent.getLatePeriod());
        }
        else if(DeadlineInfo.Type.FIXED.equals(dbEvent.getDeadlineType()))
        {
            info = DeadlineInfo.newFixedDeadlineInfo(dbEvent.getEarlyDate(), dbEvent.getEarlyPoints(),
                                                     dbEvent.getOnTimeDate(),
                                                     dbEvent.getLateDate(), dbEvent.getLatePoints());
        }
        
        return info;
    }

    @Override
    public GradableEventOccurrence getGradableEventOccurrence(GradableEvent gradableEvent, Group group) throws ServicesException
    {
        try {
            GradableEventOccurrenceRecord record = Allocator.getDatabase().getGradableEventOccurrence(gradableEvent.getId(), group.getId());
            TA ta = _taIdMap.get(Allocator.getUserUtilities().getUserId());
            if (record != null) {
                return new GradableEventOccurrence(gradableEvent, group, ta, new DateTime(), DateTime.parse(record.getTime()));
            }
            return null;
        } catch (SQLException ex) {
            throw new ServicesException("Unable to determine handin time for group [" + group.getName() + 
                    "] for gradable event " + gradableEvent.getName() + ".", ex);
        }
    }

    @Override
    public void setGradableEventOccurrence(GradableEvent gradableEvent, Group group, DateTime handinTime) throws ServicesException
    {
        this.setGradableEventOccurrences(gradableEvent, ImmutableMap.of(group, handinTime));
    }
    
    @Override
    public void setGradableEventOccurrences(GradableEvent gradableEvent, Map<Group, DateTime> statuses) throws ServicesException {
        try {
            for (Group group : statuses.keySet()) {
                Allocator.getDatabase().setGradableEventOccurrence(gradableEvent.getId(), group.getId(),
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
            Set<DbStudent> students = Allocator.getDatabase().getStudents();

            //create an object for any student that has been added to the database
            for (DbStudent dbStudent : students) {
                if (!_studentIdMap.containsKey(dbStudent.getId())) {
                    Student newStudent = new Student(dbStudent);
                    _studentIdMap.put(newStudent.getId(), newStudent);
                    _studentLoginMap.put(newStudent.getLogin(), newStudent);
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
            Set<DbGroup> groups = Allocator.getDatabase().getGroups();
            
            //remove any Group object for which the group has been removed from the database
            Set<Integer> validGroupIds = new HashSet<Integer>();
            for (DbGroup dbGroup : groups) {
                validGroupIds.add(dbGroup.getId());
            }
            for (Group group : _groupIdMap.values()) {
                if (!validGroupIds.contains(group.getId())) {
                    _groupIdMap.remove(group.getId());
                    for (Student member : group.getMembers()) {
                        _groupsCache.get(group.getAssignment().getId()).remove(member.getId());
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
                        _groupsCache.get(asgn.getId()).put(member.getId(), newGroup);
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
            Allocator.getDatabase().blacklistStudents(this.studentsToIDs(students), ta.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Students could not be added to the TA's blacklist."
                    + "Attempted to add: " + students + ".", ex);
        }
    }

    @Override
    public void unBlacklistStudents(Set<Student> students, TA ta) throws ServicesException {
        try {
            Allocator.getDatabase().unBlacklistStudents(this.studentsToIDs(students), ta.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Students could not be removed from the TA's blacklist.", ex);
        }
    }

    @Override
    public Set<Student> getBlacklistedStudents() throws ServicesException {
        try {
            Set<Integer> blacklistedIDs = Allocator.getDatabase().getBlacklistedStudents();
            return this.idsToStudents(blacklistedIDs, new HashSet<Student>(blacklistedIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklisted students from the database.", ex);
        }
    }

    @Override
    public Set<Student> getBlacklist(TA ta) throws ServicesException {
        try {
            Set<Integer> blacklistedIDs = Allocator.getDatabase().getBlacklist(ta.getId());
            return this.idsToStudents(blacklistedIDs, new HashSet<Student>(blacklistedIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklisted students from the database", ex);
        }
    }

    @Override
    public boolean isDistEmpty(GradableEvent ge) throws ServicesException {
        Set<Integer> partIDs = new HashSet<Integer>();
        for (Part p : ge.getParts()) {
            partIDs.add(p.getId());
        }
        
        try {
            return Allocator.getDatabase().isDistEmpty(partIDs);
        } catch (SQLException ex) {
            throw new ServicesException("Could not determine whether the distribution "
                    + "is empty for gradable event [" + ge.getFullDisplayName() + "].", ex);
        }
    }

    @Override
    public Map<TA, Set<Group>> getDistribution(Part dp) throws ServicesException {
        Map<TA, Set<Group>> dist = new HashMap<TA, Set<Group>>();
        SetMultimap<Integer, Integer> idDist;
        try {
            idDist = Allocator.getDatabase().getDistribution(dp.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read distribution for part [" +
                    dp + "] from the database.", ex);
        }
        
        for (TA ta : this.getTAs()) {
            if (idDist.containsKey(ta.getId())) {
                Set<Integer> toGrade = idDist.get(ta.getId());
                dist.put(ta, this.idsToGroups(toGrade, new HashSet<Group>(toGrade.size())));
            }
            else {
                //for any TA who has not been assigned any groups to grade,
                //add an empty collection to the map 
                dist.put(ta, Collections.<Group>emptySet());
            }
        }
        
        return dist;
    }

    @Override
    public void setDistribution(Map<Part, Map<TA, Set<Group>>> distribution) throws ServicesException {
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
            
            Allocator.getDatabase().setDistribution(distForDb);
        } catch (SQLException ex) {
            throw new ServicesException("Could write distribution to the database.", ex);
        }
    }

    @Override
    public Set<Group> getAssignedGroups(Part part, TA ta) throws ServicesException {
        try {
            Set<Integer> groupIDs = Allocator.getDatabase().getAssignedGroups(part.getId(), ta.getId());
            return this.idsToGroups(groupIDs, new HashSet<Group>(groupIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read groups assigned to TA [" +
                    ta + "] on part [" + part + "] from the database.", ex);
        }
    }

    @Override
    public Set<Group> getAssignedGroups(Part part) throws ServicesException {
        try {
            Set<Integer> groupIDs = Allocator.getDatabase().getAssignedGroups(part.getId());
            return this.idsToGroups(groupIDs, new HashSet<Group>(groupIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read all assigned groups "
                    + "for part [" + part + "] from the database.", ex);
        }   
    }

    @Override
    public Set<Part> getPartsWithAssignedGroups(TA ta) throws ServicesException {
        try {
            Set<Part> toReturn = new HashSet<Part>();
            for (int partID : Allocator.getDatabase().getPartsWithAssignedGroups(ta.getId())) {
                toReturn.add(_partIdMap.get(partID));
            }
            
            return toReturn;
        } catch (SQLException ex) {
            throw new ServicesException("Could not read DPs with assigned groups "
                    + "for TA [" + ta + "] from the database.", ex);
        }
    }
    
    @Override
    public Student getStudentFromLogin(String studentLogin) throws ServicesException {
        if (!_studentLoginMap.containsKey(studentLogin)) {
            this.updateDataCache();
        }
        return _studentLoginMap.get(studentLogin);
    }
    
    @Override
    public boolean isStudentLoginInDatabase(String studentLogin) throws ServicesException {
        if (!_studentLoginMap.containsKey(studentLogin)) {
            this.updateDataCache();
        }
        return _studentLoginMap.containsKey(studentLogin);
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
}