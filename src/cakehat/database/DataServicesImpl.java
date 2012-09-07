package cakehat.database;

import cakehat.Allocator;
import cakehat.CakehatSession;
import cakehat.InitializationException;
import cakehat.database.assignment.Assignment;
import cakehat.database.assignment.AssignmentsBuilder;
import cakehat.database.assignment.GradableEvent;
import cakehat.database.assignment.Part;
import cakehat.gradingsheet.GradingSheet;
import cakehat.gradingsheet.GradingSheetSection;
import cakehat.gradingsheet.GradingSheetSubsection;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import java.sql.SQLException;
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
    private final ImmutableMap<Integer, Part> _partIdMap;
    
    private final ImmutableMap<Integer, GradingSheet> _gradingSheetIdMap;
            
    public DataServicesImpl()
    {
        try
        {
            //Assignments
            _assignments = new AssignmentsBuilder().buildAssigments(Allocator.getDatabase().getAssignments());
            ImmutableMap.Builder<Integer, Assignment> asgnMapBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<Integer, Part> partMapBuilder = ImmutableMap.builder();
            ImmutableMap.Builder<Integer, GradingSheet> gradingSheetMapBuilder = ImmutableMap.builder();
            for(Assignment asgn : _assignments)
            {
                asgnMapBuilder.put(asgn.getId(), asgn);
                _groupsCache.put(asgn.getId(), new ConcurrentHashMap<Integer, Group>());
                
                for(GradableEvent ge : asgn)
                {                    
                    for(Part part : ge)
                    {
                        partMapBuilder.put(part.getId(), part);
                        gradingSheetMapBuilder.put(part.getId(), part.getGradingSheet());
                    }
                }
            }
            _asgnIdMap = asgnMapBuilder.build();
            _partIdMap = partMapBuilder.build();
            _gradingSheetIdMap = gradingSheetMapBuilder.build();
            
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
        throw new UnsupportedOperationException("No longer supported.  Use GroupGradingSheet#getAssignedTo() instead.");
    }

    @Override
    public void setGrader(Part part, Group group, TA ta) throws ServicesException
    {
        throw new UnsupportedOperationException("No longer supported.  Use GroupGradingSheet#setAssignedTo(TA) instead.");
    }
    
    @Override
    public void setExtensions(GradableEvent gradableEvent, Set<Group> groups, DateTime ontime, boolean shiftDates,
        String note) throws ServicesException
    {
        checkAssignmentEquality(gradableEvent.getAssignment(), groups);
        
        HashSet<Integer> groupIds = groupsToIdCollection(groups, new HashSet<Integer>());
        
        try
        {
            Allocator.getDatabase().setExtensions(gradableEvent.getId(), ontime.toString(), shiftDates, 
                    note, new DateTime().toString(), CakehatSession.getUserId(), groupIds);
        }
        catch(SQLException e)
        {
            throw new ServicesException("Could not store extensions for gradable event: " +
                    gradableEvent.getFullDisplayName() + " for groups: " + groups, e);
        }
    }
    
    @Override
    public void deleteExtensions(GradableEvent gradableEvent, Set<Group> groups) throws ServicesException
    {
        checkAssignmentEquality(gradableEvent.getAssignment(), groups);
        
        HashSet<Integer> groupIds = groupsToIdCollection(groups, new HashSet<Integer>());
        
        try
        {
            Allocator.getDatabase().deleteExtensions(gradableEvent.getId(), groupIds);
        }
        catch(SQLException e)
        {
            throw new ServicesException("Could not delete extensions for gradable event: " +
                    gradableEvent.getFullDisplayName() + " for groups: " + groups, e);
        }
    }
    
    @Override
    public Map<Group, Extension> getExtensions(GradableEvent gradableEvent, Set<Group> groups) throws ServicesException
    {       
        checkAssignmentEquality(gradableEvent.getAssignment(), groups);
        
        try
        {
            Map<Integer, Group> idsToGroups = groupsToIdMap(groups, new HashMap<Integer, Group>());
            
            Map<Integer, ExtensionRecord> records = Allocator.getDatabase().getExtensions(gradableEvent.getId(),
                    idsToGroups.keySet());
            
            Map<Group, Extension> extensions = new HashMap<Group, Extension>(records.size());
            for(Entry<Integer, ExtensionRecord> entry : records.entrySet())
            {
                ExtensionRecord record = entry.getValue();
                Group group = idsToGroups.get(entry.getKey());
                Extension extension = new Extension(gradableEvent, group, _taIdMap.get(record.getTAId()),
                        new DateTime(record.getDateRecorded()), new DateTime(record.getOnTime()),
                        record.getShiftDates(), record.getNote());
                extensions.put(group, extension);
            }
            
            return extensions;
        }
        catch(SQLException e)
        {
            throw new ServicesException("Unable to retrieve extensions for gradable event " +
                    gradableEvent.getFullDisplayName() + " for groups: " + groups, e);
        }
    }
    
    @Override
    public GroupGradingSheet getGroupGradingSheet(Part part, Group group) throws ServicesException {
        return this.getGroupGradingSheets(ImmutableSetMultimap.of(part, group)).get(part).get(group);
    }
    
    @Override
    public Map<Part, Map<Group, GroupGradingSheet>> getGroupGradingSheets(SetMultimap<Part, Group> toRetrieve) throws ServicesException {
        try {
            Map<Part, Map<Group, GroupGradingSheet>> gradingSheets = new HashMap<Part, Map<Group, GroupGradingSheet>>();
            
            //create db variable so subsequent lines of code don't get too long and hard to read
            Database db = Allocator.getDatabase();
            
            Map<Integer, Part> partMap = new HashMap<Integer, Part>();
            Set<Integer> sectionIds = new HashSet<Integer>();
            Set<Integer> subsectionIds = new HashSet<Integer>();
            for (Part part : toRetrieve.keySet()) {
                partMap.put(part.getId(), part);
                GradingSheet gs = part.getGradingSheet();
                for (GradingSheetSection section : gs.getSections()) {
                    sectionIds.add(section.getId());

                    for (GradingSheetSubsection subsection : section.getSubsections()) {
                        subsectionIds.add(subsection.getId());
                    }
                }
            }
           
            Map<Integer, Map<Integer, DbGroupGradingSheet>> gradingSheetsFromDb =
                    db.getGroupGradingSheets(partMap.keySet(), subsectionIds,  sectionIds,
                                             groupsToIdCollection(toRetrieve.values(), new HashSet<Integer>()));
            
            for (Part part : toRetrieve.keySet()) {
                gradingSheets.put(part, new HashMap<Group, GroupGradingSheet>());
                
                Map<Integer, DbGroupGradingSheet> gradingSheetsForPart = gradingSheetsFromDb.get(part.getId()) == null ?
                        ImmutableMap.<Integer, DbGroupGradingSheet>of() : gradingSheetsFromDb.get(part.getId());
                
                for (Group group : toRetrieve.get(part)) {
                    DbGroupGradingSheet dbGradingSheet = gradingSheetsForPart.get(group.getId());
                    if (dbGradingSheet == null) {
                        dbGradingSheet = new DbGroupGradingSheet(group.getId(), part.getId());
                    }
                    
                    gradingSheets.get(part).put(group, new GroupGradingSheet(group, part.getGradingSheet(), dbGradingSheet));
                }
            }

            return gradingSheets;
        } catch (SQLException ex) {
            throw new ServicesException("Unable to retrieve group grading sheets.", ex);
        }
    }
    
    @Override
    public void saveGroupGradingSheet(GroupGradingSheet groupGradingSheet) throws ServicesException {
        try {
            Allocator.getDatabase().putGroupGradingSheets(ImmutableSet.of(groupGradingSheet.getDbGroupGradingSheet()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not save group grading sheet.", ex);
        }
    }

    @Override
    public void setGroupGradingSheetsSubmitted(Set<GroupGradingSheet> groupGradingSheets,
                                               boolean submitted) throws ServicesException {
        Set<DbGroupGradingSheet> ggsForDb = new HashSet<DbGroupGradingSheet>();      
        for (GroupGradingSheet ggs : groupGradingSheets) {
            ggsForDb.add(ggs.getDbGroupGradingSheet());
        }
        
        Integer submittedById = null;
        String submissionTime = null;
        if (submitted) {
            submittedById = Allocator.getUserServices().getUser().getId();
            submissionTime = DateTime.now().toString();
        }

        try {
            //if grading sheets are being submitted, save them first
            if (submitted) {
                Allocator.getDatabase().putGroupGradingSheets(ggsForDb);
            }
            
            Allocator.getDatabase().submitGroupGradingSheets(ggsForDb, submittedById, submissionTime);
        } catch (SQLException ex) {
            throw new ServicesException("Could not save group grading sheet.", ex);
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
            Map<Integer, Group> idsToGroups = groupsToIdMap(groups, new HashMap<Integer, Group>());
            
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
        int taId = CakehatSession.getUserId();
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
            Allocator.getDatabase().setEarned(part.getId(), CakehatSession.getUserId(),
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
            Allocator.getDatabase().setEarnedSubmitted(part.getId(), CakehatSession.getUserId(),
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

    private void addGroups(Set<DbGroup> toAdd) throws ServicesException {
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
            Allocator.getDatabase().putGroups(toAdd);
            
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
        }
    }

    @Override
    public Group getGroup(Assignment asgn, Student student) throws ServicesException {
        if (!_groupsCache.get(asgn.getId()).containsKey(student.getId())) {
            this.updateDataCache();

            if (!_groupsCache.get(asgn.getId()).containsKey(student.getId())) {
                //if the assignment does not have groups, create group of one
                if (!asgn.hasGroups()) {
                    this.addGroups(ImmutableSet.of(new DbGroup(asgn, student)));
                }
            }
        }
        
        return _groupsCache.get(asgn.getId()).get(student.getId());
    }
    
    @Override
    public Set<Group> getGroups(Assignment asgn) throws ServicesException {
        Set<DbGroup> dbGroups;
        try {
            dbGroups = Allocator.getDatabase().getGroups(asgn.getId());
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
                    dbGroups = Allocator.getDatabase().getGroups(asgn.getId());
                } catch (SQLException ex) {
                    throw new ServicesException("Could not read groups for assignment from the database "
                            + "(after adding auto-groups of one for students who did not have them).", ex);
                }
            }
        }

        ImmutableSet.Builder<Group> builder = ImmutableSet.builder();
        
        for (DbGroup group : dbGroups) {
            builder.add(this.getGroup(group.getId()));
        }

        return builder.build();
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
    public Map<Group, GradableEventOccurrence> getGradableEventOccurrences(GradableEvent gradableEvent,
        Set<Group> groups) throws ServicesException
    {
        checkAssignmentEquality(gradableEvent.getAssignment(), groups);
        
        try
        {
            Map<Integer, GradableEventOccurrenceRecord> records = Allocator.getDatabase().getGradableEventOccurrences(
                    gradableEvent.getId(), groupsToIdCollection(groups, new HashSet<Integer>()));
            
            ImmutableMap.Builder<Group, GradableEventOccurrence> occurrences = ImmutableMap.builder();
            for(Group group : groups)
            {
                GradableEventOccurrenceRecord record = records.get(group.getId());
                if(record != null)
                {
                    GradableEventOccurrence occurrence = new GradableEventOccurrence(gradableEvent, group,
                            _taIdMap.get(record.getTA()), new DateTime(record.getDateRecorded()),
                            new DateTime(record.getOccurrenceDate()));
                    occurrences.put(group, occurrence);
                }
            }
            
            return occurrences.build();
        }
        catch(SQLException ex)
        {
            throw new ServicesException("Could not get gradable event occurences for given groups.", ex);
        }
    }
    
    @Override
    public void setGradableEventOccurrences(GradableEvent gradableEvent, Map<Group, DateTime> statuses)
            throws ServicesException
    {
        checkAssignmentEquality(gradableEvent.getAssignment(), statuses.keySet());
        
        try
        {
            Map<Integer, String> dbStatuses = new HashMap<Integer, String>();
            for(Entry<Group, DateTime> status : statuses.entrySet())
            {
                dbStatuses.put(status.getKey().getId(), status.getValue().toString());
            }
            
            Allocator.getDatabase().setGradableEventOccurrences(gradableEvent.getId(), dbStatuses,
                    CakehatSession.getUserId(), new DateTime().toString());
        }
        catch(SQLException ex)
        {
            throw new ServicesException("Could not set gradable event occurences for given groups.", ex);
        }
    }
        
    @Override
    public void deleteGradableEventOccurrences(GradableEvent gradableEvent, Set<Group> groups) throws ServicesException
    {
        checkAssignmentEquality(gradableEvent.getAssignment(), groups);
        
        HashSet<Integer> groupIds = groupsToIdCollection(groups, new HashSet<Integer>());
        
        try
        {
            Allocator.getDatabase().deleteGradableEventOccurrences(gradableEvent.getId(), groupIds);
        }
        catch(SQLException e)
        {
            throw new ServicesException("Could not delete gradable event occurrences for gradable event: " +
                    gradableEvent.getFullDisplayName() + " for groups: " + groups, e);
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
    public void setStudentsAreEnabled(Map<Student, Boolean> studentsToUpdate) throws ServicesException {
        try {
            Set<Student> toEnable = new HashSet<Student>();
            Set<Student> toDisable = new HashSet<Student>();
            Map<Integer, Boolean> idMap = new HashMap<Integer, Boolean>();
            for (Student student : studentsToUpdate.keySet()) {
                boolean enable = studentsToUpdate.get(student);
                idMap.put(student.getId(), enable);
                
                if (enable)  {
                    toEnable.add(student);
                }
                else {
                    toDisable.add(student);
                }
            }
            
            Allocator.getDatabase().setStudentsAreEnabled(idMap);
            _enabledStudents.addAll(toEnable);
            _enabledStudents.removeAll(toDisable);
        } catch (SQLException ex) {
            throw new ServicesException("Could not update enabled statuses for students " + studentsToUpdate.keySet());
        }
    }
    
    @Override
    public void setStudentsHasCollaborationContract(Map<Student, Boolean> studentsToUpdate) throws ServicesException {
        Map<Integer, Boolean> idMap = new HashMap<Integer, Boolean>();
        for(Entry<Student, Boolean> entry : studentsToUpdate.entrySet()) {
            idMap.put(entry.getKey().getId(), entry.getValue());
        }
        
        try {
            Allocator.getDatabase().setStudentsHasCollaborationContract(idMap);
        } catch (SQLException e) {
            throw new ServicesException("Unable to set collaboration contracts for " + studentsToUpdate, e);
        }
    }

    @Override
    public Set<Student> getStudentsWithCollaborationContracts() throws ServicesException {
        try {
            ImmutableSet.Builder<Student> studentsWithCollabContract = ImmutableSet.builder();
            
            Set<DbStudent> students = Allocator.getDatabase().getStudents();
            for(DbStudent student : students) {
                if(student.hasCollabContract()) {
                    if(!_studentIdMap.containsKey(student.getId())) {
                        this.updateDataCache();
                    }
                        
                    studentsWithCollabContract.add(_studentIdMap.get(student.getId()));
                }
            }
            
            return studentsWithCollabContract.build();
        } catch (SQLException e) {
            throw new ServicesException("Students could not be retrieved from the database", e);
        }
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
    public Map<TA, Set<Group>> getDistribution(Part part) throws ServicesException {
        Map<TA, Set<Group>> dist = new HashMap<TA, Set<Group>>();
        SetMultimap<Integer, Integer> idDist;
        try {
            idDist = Allocator.getDatabase().getDistribution(part.getId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read distribution for part [" + part + "] from the database.", ex);
        }
        
        for (TA ta : this.getTAs()) {
            if (idDist.containsKey(ta.getId())) {
                Set<Integer> toGrade = idDist.get(ta.getId());
                dist.put(ta, this.idsToGroups(toGrade, new HashSet<Group>(toGrade.size())));
            }
            else {
                //for any TA who has not been assigned any groups to grade, add an empty set to the map 
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
            throw new ServicesException("Could not read parts with assigned groups for TA [" + ta + 
                    "] from the database.", ex);
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
    
    private static void checkAssignmentEquality(Assignment asgn, Iterable<Group> groups) throws ServicesException
    {
        Set<Group> notEqual = new HashSet<Group>();
        
        for(Group group : groups)
        {
            //Compare on assignment id instead of the object so that mocked test objects can satisfy this constraint
            if(group.getAssignment().getId() != asgn.getId())
            {
                notEqual.add(group);
            }
        }
        
        if(!notEqual.isEmpty())
        {
            StringBuilder builder = new StringBuilder();
            builder.append("One or more groups do not belong to assignment [");
            builder.append(asgn.getName());
            builder.append("]\n");
            for(Group group : notEqual)
            {
                builder.append("Group [");
                builder.append(group.getName());
                builder.append("] belong to assignment [");
                builder.append(group.getAssignment().getName());
                builder.append("]\n");
            }
            
            throw new ServicesException(builder.toString());
        }
    }
    
    private static <T extends Collection<Integer>> T groupsToIdCollection(Iterable<Group> groups, T idCollection)
    {
        for(Group group : groups)
        {
            idCollection.add(group.getId());
        }
        
        return idCollection;
    }
    
    private static <T extends Map<Integer, Group>> T groupsToIdMap(Iterable<Group> groups, T idMap)
    {
        for(Group group : groups)
        {
            idMap.put(group.getId(), group);
        }
        
        return idMap;
    }
    
    private <T extends Collection<Integer>, S extends Collection<Group>> S idsToGroups(T ids, S groups) throws ServicesException {
        for (Integer id : ids) {
            groups.add(this.getGroup(id));
        }
        
        return groups;
    }
    
    @Override
    public Group getGroup(int groupId) throws ServicesException {
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