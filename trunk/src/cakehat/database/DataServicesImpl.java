package cakehat.database;

import cakehat.Allocator;
import cakehat.config.Assignment;
import cakehat.config.Part;
import cakehat.config.TA;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.Handin;
import cakehat.services.ServicesException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JOptionPane;
import support.utils.posix.NativeException;

/**
 * Implementation of {@link DataServices} interface.
 *
 * @author jeldridg
 */
@Deprecated
public class DataServicesImpl implements DataServices {

    /**
     * Maps a student's ID in the database to the corresponding Student object.
     */
    private final Map<Integer, Student> _studentIdMap = new HashMap<Integer, Student>();
    
    /**
     * Maps a student's login to the corresponding Student object.
     */
    private final Map<String, Student> _loginMap = new HashMap<String, Student>();
    
    private final Set<Student> _enabledStudents = new HashSet<Student>();
    
    /**
     * Maps a group's ID in the database to the corresponding Group object.
     */
    private final Map<Integer, Group> _groupIdMap = new HashMap<Integer, Group>();
    
    /**
     * Maps an Assignment to a map that associates each Student object with the
     * Group object for that student and assignment.
     */
    private Map<Assignment, Map<Student, Group>> _groupsCache = new HashMap<Assignment, Map<Student, Group>>();
    
    public DataServicesImpl() {
        Collection<Assignment> asgns = Allocator.getConfigurationInfo().getAssignments();
        for (Assignment a : asgns) {
            _groupsCache.put(a, new HashMap<Student, Group>());
        }
    }

    @Override
    public Collection<Student> getAllStudents() {
        return Collections.unmodifiableCollection(_studentIdMap.values());
    }

    
    @Override
    public Collection<Student> getEnabledStudents() {
        return Collections.unmodifiableCollection(_enabledStudents);
    }

    @Override
    public Student addStudent(String studentLogin, ValidityCheck checkValidity) throws ServicesException {
        try {
            String name = Allocator.getUserUtilities().getUserName(studentLogin);
            String names[] = name.split(" ");
            String firstName = names[0];
            String lastName = names[names.length - 1];

            return this.addStudent(studentLogin, firstName, lastName, checkValidity);
        } catch (NativeException e) {
            throw new ServicesException("Student will not be added to the database because " +
                                        "the user's real name cannot be retrieved", e);
        }
    }
    
    @Override
    public Student addStudent(String studentLogin, String firstName, String lastName,
                           ValidityCheck checkValidity) throws ServicesException {
        if (checkValidity == ValidityCheck.CHECK) {
            String warningMessage = "";
            boolean isLoginValid = Allocator.getUserUtilities().isLoginValid(studentLogin);
            boolean isInStudentGroup = false;

            try {
                isInStudentGroup = Allocator.getUserServices().isInStudentGroup(studentLogin);
            } catch (NativeException e) {
                throw new ServicesException("Unable to retrieve student group", e);
            }

            if (!isLoginValid) {
                warningMessage += String.format("The login %s is not a valid login\n",
                                                studentLogin);
            }
            else if (!isInStudentGroup) {
                warningMessage += String.format("The login %s is not in the student group",
                                                studentLogin);
            }

            if (!isLoginValid || !isInStudentGroup) {
                Object[] options = {"Proceed", "Cancel"};
                int shouldContinue = JOptionPane.showOptionDialog(null, warningMessage,
                                                                  "Invalid Student Login",
                                                                  JOptionPane.OK_CANCEL_OPTION,
                                                                  JOptionPane.WARNING_MESSAGE,
                                                                  null, options, options[0]);

                if (shouldContinue != JOptionPane.OK_OPTION) {
                    return null;
                }
            }
        }

        try {
            int addedID = Allocator.getDatabase().addStudent(studentLogin, firstName, lastName);
            
            //if addedID is 0, the student was already in the database and was not re-added
            if (addedID != 0) {
                Student newStudent = new Student(addedID, studentLogin, firstName, lastName, true);
                _studentIdMap.put(addedID, newStudent);
                _loginMap.put(studentLogin, newStudent);
                _enabledStudents.add(newStudent);
            }
            
            return _loginMap.get(studentLogin);
        } catch (SQLException e) {
            throw new ServicesException(String.format("Student %s (%s %s) could not "
                    + "be added to the database", studentLogin, firstName, lastName), e);
        }
    }

    @Override
    public void setStudentEnabled(Student student, boolean setEnabled) throws ServicesException {
        if (setEnabled) {       //attempt to enable student
            try {
                Allocator.getDatabase().enableStudent(student.getDbId());
                _enabledStudents.add(student);
                student.setEnabled(true);
            } catch (SQLException ex) {
                throw new ServicesException("Could not enable student " + student + ".", ex);
            }
        }
        else {                  //attempt to disable student
            try {
                Allocator.getDatabase().disableStudent(student.getDbId());
                _enabledStudents.remove(student);
                student.setEnabled(false);
            } catch (SQLException ex) {
                throw new ServicesException("Could not disable student " + student + ".", ex);
            }
        }
    }
    
    @Override
    public void blacklistStudents(Collection<Student> students, TA ta) throws ServicesException {
        try {
            Allocator.getDatabase().blacklistStudents(this.studentsToIDs(students), ta.getLogin());
        } catch (SQLException ex) {
            throw new ServicesException("Students could not be added to the TA's blacklist."
                    + "Attempted to add: " + students + ".", ex);
        }
    }
    
    @Override
    public void unBlacklistStudents(Collection<Student> students, TA ta) throws ServicesException {
        try {
            Allocator.getDatabase().unBlacklistStudents(this.studentsToIDs(students), ta.getLogin());
        } catch (SQLException ex) {
            throw new ServicesException("Students could not be removed from the TA's blacklist.", ex);
        }
    }
    
    @Override
    public Collection<Student> getBlacklistedStudents() throws ServicesException {
        try {
            Collection<Integer> blacklistedIDs = Allocator.getDatabase().getBlacklistedStudents();
            return this.idsToStudents(blacklistedIDs, new ArrayList<Student>(blacklistedIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklisted students from the database.", ex);
        }
    }

    @Override
    public Collection<Student> getBlacklist(TA ta) throws ServicesException {
        try {
            Collection<Integer> blacklistedIDs = Allocator.getDatabase().getBlacklist(ta.getLogin());
            return this.idsToStudents(blacklistedIDs, new ArrayList<Student>(blacklistedIDs.size()));
        } catch (SQLException ex) {
            throw new ServicesException("Could not read blacklisted students from the database", ex);
        }
    }
    
    @Override
    public Group addGroup(NewGroup toAdd) throws ServicesException {
        return Iterables.get(this.addGroups(ImmutableList.of(toAdd)), 0);
    }

    @Override
    public Collection<Group> addGroups(Collection<NewGroup> toAdd) throws ServicesException {
        //ensure that no student in a group to add is already to assigned
        //to a group for the corresponding assignment
        for (NewGroup group : toAdd) {
            for (Student member : group.getMembers()) {
                if (_groupsCache.get(group.getAssignment()).containsKey(member)) {
                    throw new ServicesException("Student [" + member + "] is already "
                            + "assigned to a group for assignment [" + group.getAssignment() + "]. "
                            + "No groups have been added to the database.");
                }
            }
        }
        
        try {
            Collection<Group> toReturn = new ArrayList<Group>(toAdd.size());
            Collection<GroupRecord> added = Allocator.getDatabase().addGroups(toAdd);
            
            //if adding groups was successful, update caches
            for (GroupRecord record : added) {
                Assignment asgn = Allocator.getConfigurationInfo().getAssignment(record.getAssignmentID());
                if (asgn == null) {
                    throw new ServicesException("Assignment id [" + record.getAssignmentID() + "] "
                            + "is included in a group record but does not map to an Assignment object.");
                }
                
                Collection<Student> members = this.idsToStudents(record.getMemberIDs(), new ArrayList<Student>(record.getMemberIDs().size()));
                Group newGroup = new Group(record.getDbId(), asgn, record.getName(), members);
                _groupIdMap.put(newGroup.getDbId(), newGroup);
                for (Student member : newGroup.getMembers()) {
                    _groupsCache.get(asgn).put(member, newGroup);
                }
                toReturn.add(newGroup);
            }
            
            return toReturn;
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
        if (!_groupsCache.get(asgn).containsKey(student)) {
            this.updateDataCache();
            
            if (!_groupsCache.get(asgn).containsKey(student)) {
                //if the assignment does not have groups, create group of one
                if (!asgn.hasGroups()) {
                    this.addGroup(new NewGroup(asgn, student));
                }
                else {
                    throw new ServicesException("Could not retrieve group for student [" + student
                        + "] on assignment [" + asgn + "].");
                }
            }
        }
        
        return _groupsCache.get(asgn).get(student);
    } 
    
    @Override
    public Collection<Group> getGroups(Assignment asgn) throws ServicesException {
        Collection<Integer> groupIDs;
        try {
            groupIDs = Allocator.getDatabase().getGroups(asgn.getDBID());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read groups for assignment from the database", ex);
        }
        
        //if the assignment is not a group assignment, create a group of one for
        //each student who does not already have one
        if (!asgn.hasGroups()) {
            Set<NewGroup> groupsToAdd = new HashSet<NewGroup>();
            
            for (Student student : _studentIdMap.values()) {
                if (!_groupsCache.get(asgn).containsKey(student)) {
                    groupsToAdd.add(new NewGroup(asgn, student));
                }
            }
            
            if (!groupsToAdd.isEmpty()) {
                this.addGroups(groupsToAdd);

                try {
                    groupIDs = Allocator.getDatabase().getGroups(asgn.getDBID());
                } catch (SQLException ex) {
                    throw new ServicesException("Could not read groups for assignment from the database "
                            + "(after adding auto-groups of one for students who did not have them).", ex);
                }
            }
        }
        
        return this.idsToGroups(groupIDs, new ArrayList<Group>(groupIDs.size()));
    }
    
    @Override
    public void removeGroups(Assignment asgn) throws ServicesException {
        try {
            Allocator.getDatabase().removeGroups(asgn.getDBID());
            
            //if removing groups was successful, update caches
            _groupsCache.get(asgn).clear();
        } catch (SQLException ex) {
            throw new ServicesException("Could not remove groups for assignment "
                    + "[" + asgn + "] from the database.");
        }
    }
    
    @Override
    public boolean isDistEmpty(Assignment asgn) throws ServicesException {
        try {
            return Allocator.getDatabase().isDistEmpty(asgn.getPartIDs());
        } catch (SQLException ex) {
            throw new ServicesException("Could not determine whether the distribution "
                    + "is empty for assignment [" + asgn + "].", ex);
        }
    }
    
    @Override
    public Map<TA, Collection<Group>> getDistribution(DistributablePart dp) throws ServicesException {
        Map<TA, Collection<Group>> dist = new HashMap<TA, Collection<Group>>();
        Map<String, Collection<Integer>> idDist;
        try {
            idDist = Allocator.getDatabase().getDistribution(dp.getDBID());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read distribution for part [" +
                    dp + "] from the database.", ex);
        }
        
        for (TA ta : Allocator.getConfigurationInfo().getTAs()) {
            if (idDist.containsKey(ta.getLogin())) {
                Collection<Integer> toGrade = idDist.get(ta.getLogin());
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
    public void setDistribution(Map<DistributablePart, Map<TA, Collection<Group>>> distribution) throws ServicesException {
        try {
            Map<String, Map<String, Collection<Integer>>> distForDb = new HashMap<String, Map<String, Collection<Integer>>>();
            for (DistributablePart dp : distribution.keySet()) {
                distForDb.put(dp.getDBID(), new HashMap<String, Collection<Integer>>());
                
                for (TA ta : distribution.get(dp).keySet()) {
                    distForDb.get(dp.getDBID()).put(ta.getLogin(), new ArrayList<Integer>(distribution.get(dp).get(ta).size()));
                    
                    for (Group group : distribution.get(dp).get(ta)) {
                        distForDb.get(dp.getDBID()).get(ta.getLogin()).add(group.getDbId());
                    }
                }
            }
            
            Allocator.getDatabase().setDistribution(distForDb);
        } catch (SQLException ex) {
            throw new ServicesException("Could write distribution to the database.", ex);
        }
    }
    
    @Override
    public void assignGroup(Group group, DistributablePart part, TA ta) throws ServicesException {
        try {
            Allocator.getDatabase().assignGroup(group.getDbId(), part.getDBID(), ta.getLogin());
        } catch (SQLException ex) {
            throw new ServicesException("Group [" + group + "] could not be assigned "
                    + "to TA [" + ta + "] on part [" + part + "].", ex);
        }
    }
    
    @Override
    public void unassignGroup(Group group, DistributablePart part, TA ta) throws ServicesException {
        try {
            Allocator.getDatabase().unassignGroup(group.getDbId(), part.getDBID(), ta.getLogin());
        } catch (SQLException ex) {
            throw new ServicesException("Group [" + group + "] could not be unassigned "
                    + "from TA [" + ta + "] on part [" + part + "].", ex);
        }
    }
    
    @Override
    public Collection<Group> getAssignedGroups(DistributablePart part, TA ta) throws ServicesException {
        Collection<Integer> groupIDs;
        try {
            groupIDs = Allocator.getDatabase().getAssignedGroups(part.getDBID(), ta.getLogin());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read groups assigned to TA [" +
                    ta + "] on part [" + part + "] from the database.", ex);
        }
        
        return this.idsToGroups(groupIDs, new ArrayList<Group>(groupIDs.size()));
    }

    @Override
    public Collection<Group> getAssignedGroups(DistributablePart part) throws ServicesException {
        Collection<Integer> groupIDs;
        try {
            groupIDs = Allocator.getDatabase().getAssignedGroups(part.getDBID());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read all assigned groups "
                    + "for part [" + part + "] from the database.", ex);
        }
        
        return this.idsToGroups(groupIDs, new ArrayList<Group>(groupIDs.size()));
    }
    
    @Override
    public Set<DistributablePart> getDPsWithAssignedGroups(TA ta) throws ServicesException {
        try {
            Set<DistributablePart> toReturn = new HashSet<DistributablePart>();
            for (String partID : Allocator.getDatabase().getDPsWithAssignedGroups(ta.getLogin())) {
                toReturn.add(Allocator.getConfigurationInfo().getDistributablePart(partID));
            }
            
            return toReturn;
        } catch (SQLException ex) {
            throw new ServicesException("Could not read DPs with assigned groups "
                    + "for TA [" + ta + "] from the database.", ex);
        }
    }
    
    @Override
    public TA getGrader(DistributablePart part, Group group) throws ServicesException {
        try {
            String graderLogin = Allocator.getDatabase().getGrader(part.getDBID(), group.getDbId());
            return Allocator.getConfigurationInfo().getTA(graderLogin);
        } catch (SQLException ex) {
            throw new ServicesException("Could not get grader for group [" + group + "] "
                    + "on part [" + part + "] from the database.", ex);
        }
    }
    
    @Override
    public void grantExtension(Group group, Calendar newDate, String note) throws ServicesException {
        try {
            Allocator.getDatabase().grantExtension(group.getDbId(),
                                                   group.getAssignment().getDBID(),
                                                   newDate,
                                                   note);
        } catch (SQLException ex) {
            throw new ServicesException("Could not grant extension for group [" + group + "] "
                    + "on assignment [" + group.getAssignment() + "].", ex);
        }
    }

    @Override
    public void removeExtension(Group group) throws ServicesException {
        try {
            Allocator.getDatabase().removeExtension(group.getDbId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not remove extension for group [" + group + "] "
                    + "on assignment [" + group.getAssignment() + "].", ex);
        }
    }
    
    @Override
    public Calendar getExtension(Group group) throws ServicesException {
        try {
            return Allocator.getDatabase().getExtension(group.getDbId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not retrieve extension for group [" + group + "] "
                    + "on assignment [" + group.getAssignment() + "] from the database.", ex);
        }
    }
    
    @Override
    public Map<Group, Calendar> getExtensions(Handin handin) throws ServicesException {
        try {
            Map<Integer, Calendar> idExtensions = Allocator.getDatabase().getExtensions(handin.getAssignment().getDBID());
            return this.idMapToGroupMap(idExtensions);
        } catch (SQLException ex) {
            throw new ServicesException("Could not read extensions for handin " + 
                    handin + " from the database.", ex);
        }
    }
    
    @Override
    public String getExtensionNote(Group group) throws ServicesException {
        try {
            return Allocator.getDatabase().getExtensionNote(group.getDbId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read extension note for group ["
                    + group + "] for assignment [" + group.getAssignment() + "] from the database.", ex);
        }
    }
    
    @Override
    public void grantExemption(Group group, Part part, String note) throws ServicesException {
        try {
            Allocator.getDatabase().grantExemption(group.getDbId(), part.getDBID(), note);
        } catch (SQLException ex) {
            throw new ServicesException("Could not grant exemption for group [" + group + "] "
                    + "on part [" + part + "].", ex);
        }
    }

    @Override
    public void removeExemption(Group group, Part part) throws ServicesException {
        try {
            Allocator.getDatabase().removeExemption(group.getDbId(), part.getDBID());
        } catch (SQLException ex) {
            throw new ServicesException("Could not remove exemption for group [" + group + "] "
                    + "on part [" + part + "].", ex);
        }
    }
    
    @Override
    public Set<Group> getExemptions(Part part) throws ServicesException {
        Set<Integer> idExemptions;
        try {
            idExemptions = Allocator.getDatabase().getExemptions(part.getDBID());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read exemptions for part " + 
                    part + " from the database.", ex);
        }
        
        return this.idsToGroups(idExemptions, new HashSet<Group>());
    }
    
    @Override
    public String getExemptionNote(Group group, Part part) throws ServicesException {
        try {
            return Allocator.getDatabase().getExemptionNote(group.getDbId(), part.getDBID());
        } catch (SQLException ex) {
            throw new ServicesException("Could not retrieve exemption note for group [" +
                    group + "] on part [" + part + "] from the database.", ex);
        }
    }
    
    @Override
    public void enterGrade(Group group, Part part, double score) throws ServicesException {
        try {
            Allocator.getDatabase().enterGrade(group.getDbId(), part.getDBID(), score);
        } catch (SQLException ex) {
            throw new ServicesException("Could not enter grade for group [" + group + "] "
                    + "on part [" + part + "].", ex);
        }
    }
    
    @Override
    public Double getScore(Group group, Part part) throws ServicesException {
        try {
            return Allocator.getDatabase().getPartScore(group.getDbId(), part.getDBID());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read score for group [" + group + "] "
                    + "on part [" + part + "] from the database.", ex);
        }
    }

    @Override
    public Double getScore(Group group) throws ServicesException {
        try {
            return Allocator.getDatabase().getScore(group.getDbId(), group.getAssignment().getPartIDs());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read score for group [" + group + "] "
                    + "on assignment [" + group.getAssignment() + "] from the database.", ex);
        }
    }
    
    @Override
    public Map<Group, Double> getScores(Part part, Collection<Group> groups) throws ServicesException {
        try {
            Map<Integer, Double> idScores = Allocator.getDatabase().getPartScores(part.getDBID(), this.groupsToIDs(groups));
            return this.idMapToGroupMap(idScores);
        } catch (SQLException ex) {
            throw new ServicesException("Could not read scores for part " + 
                    part + " from the database.", ex);
        }
    }

    @Override
    public Map<Group, Double> getScores(Assignment asgn, Collection<Group> groups) throws ServicesException {
        try {
            Map<Integer, Double> idScores = Allocator.getDatabase().getScores(asgn.getPartIDs(), this.groupsToIDs(groups));
            return this.idMapToGroupMap(idScores);
        } catch (SQLException ex) {
            throw new ServicesException("Could not read scores for assignment " + 
                    asgn + " from the database.", ex);
        }
    }
    
    @Override
    public void setHandinStatus(Group group, HandinStatus status) throws ServicesException {
        try {
            Allocator.getDatabase().setHandinStatus(group.getDbId(), status);
        } catch (SQLException ex) {
            throw new ServicesException("Could not set handin status for group [ " +
                    group + "] on assignment [" + group.getAssignment() + "].", ex);
        }
    }

    @Override
    public void setHandinStatuses(Map<Group, HandinStatus> statuses) throws ServicesException {
        try {
            Map<Integer, HandinStatus> statusesForDB = new HashMap<Integer, HandinStatus>();
            for (Group group : statuses.keySet()) {
                statusesForDB.put(group.getDbId(), statuses.get(group));
            }
            
            Allocator.getDatabase().setHandinStatuses(statusesForDB);
        } catch (SQLException ex) {
            throw new ServicesException("Could not set handin statuses for given groups.", ex);
        }
    }

    @Override
    public HandinStatus getHandinStatus(Group group) throws ServicesException {
        try {
            return Allocator.getDatabase().getHandinStatus(group.getDbId());
        } catch (SQLException ex) {
            throw new ServicesException("Could not read handin status for group [ " +
                    group + "] on assignment [" + group.getAssignment() + "] from the database.", ex);
        }
    }

    @Override
    public boolean areHandinStatusesSet(Handin handin) throws ServicesException {
        try {
            return Allocator.getDatabase().areHandinStatusesSet(handin.getAssignment().getDBID());
        } catch (SQLException ex) {
            throw new ServicesException("Could not determine if handin statuses "
                    + "have been set for assignment [" + handin.getAssignment() + "].", ex);
        } catch (CakeHatDBIOException ex) {
            throw new ServicesException("Could not determine if handin statuses "
                    + "have been set for assignment [" + handin.getAssignment() + "].", ex);
        }
    }

    @Override
    public void resetDatabase() throws ServicesException {
        try {
            Allocator.getDatabase().resetDatabase();
        } catch (SQLException ex) {
            throw new ServicesException("Could not reset database.", ex);
        }
    }
    
    @Override
    public boolean isStudentLoginInDatabase(String studentLogin) throws ServicesException {
        if (!_loginMap.containsKey(studentLogin)) {
            this.updateDataCache();
        }
        return _loginMap.containsKey(studentLogin);
    }
    
    @Override
    public Student getStudentFromLogin(String studentLogin) throws ServicesException {
        if (!_loginMap.containsKey(studentLogin)) {
            this.updateDataCache();
        }
        return _loginMap.get(studentLogin);
    }
    
    private <T> Map<Group, T> idMapToGroupMap(Map<Integer, T> idMap) throws ServicesException {
        Map<Group, T> groupMap = new HashMap<Group, T>();
        for (Integer groupID : idMap.keySet()) {
            groupMap.put(this.groupIdToGroup(groupID), idMap.get(groupID));
        }
        
        return groupMap;
    }
    
    private Collection<Integer> studentsToIDs(Collection<Student> students) {
        return this.studentsToIDs(students, new ArrayList<Integer>(students.size()));
    }
    
    private <T extends Collection<Student>, S extends Collection<Integer>> S studentsToIDs(T students, S ids) {
        for (Student student : students) {
            ids.add(student.getDbId());
        }
        
        return ids;
    }
    
    private Collection<Integer> groupsToIDs(Collection<Group> groups) {
        return this.groupsToIDs(groups, new ArrayList<Integer>(groups.size()));
    }
    
    private <T extends Collection<Group>, S extends Collection<Integer>> S groupsToIDs(T groups, S ids) {
        for (Group group : groups) {
            ids.add(group.getDbId());
        }
        
        return ids;
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
    
    @Override
    public void updateDataCache() throws ServicesException {
        /*
         * update Student objects
         */
        Collection<StudentRecord> studentRecords;
        try {
            studentRecords = Allocator.getDatabase().getAllStudents();
        } catch (SQLException ex) {
            throw new ServicesException("Could not retrieve students from the "
                    + "database.", ex);
        }

        _enabledStudents.clear();

        //removing from the map any student that has been removed from the database
        Set<Integer> validStudentIds = new HashSet<Integer>();
        for (StudentRecord sr : studentRecords) {
            validStudentIds.add(sr.getDbId());
        }
        Iterator<Entry<Integer, Student>> studentMapIterator = _studentIdMap.entrySet().iterator();
        while (studentMapIterator.hasNext()) {
            Student student = studentMapIterator.next().getValue();
            if (!validStudentIds.contains(student.getDbId())) {
                studentMapIterator.remove();
                _loginMap.remove(student.getLogin());
            }
        }

        //update the students in the map and
        //add any student that has been added to the database
        for (StudentRecord studentRecord : studentRecords) {
            if (_studentIdMap.containsKey(studentRecord.getDbId())) {
                Student student = _loginMap.get(studentRecord.getLogin());

                //calling Student.update(...) will mutate any out-of-date fields of the
                //Student object (and return true if any changes were made)
                student.update(studentRecord.getFirstName(), studentRecord.getLastName(),
                                                             studentRecord.isEnabled());
            }
            else {
                Student newStudent = new Student(studentRecord.getDbId(),
                                                 studentRecord.getLogin(),
                                                 studentRecord.getFirstName(),
                                                 studentRecord.getLastName(),
                                                 studentRecord.isEnabled());
                _studentIdMap.put(newStudent.getDbId(), newStudent);
                _loginMap.put(newStudent.getLogin(), newStudent);
            }

            if (studentRecord.isEnabled()) {
                _enabledStudents.add(_studentIdMap.get(studentRecord.getDbId()));
            }
        }
        
        /*
         * update Group objects
         */
        Collection<GroupRecord> groupRecords;
        try {
            groupRecords = Allocator.getDatabase().getAllGroups();
        } catch (SQLException ex) {
            throw new ServicesException("Could not retrieve groups from the "
                    + "database.", ex);
        }
        
        //removing any Group that has been removed from the database
        Set<Integer> validGroupIDs = new HashSet<Integer>();
        for (GroupRecord gr : groupRecords) {
            validGroupIDs.add(gr.getDbId());
        }
        Iterator<Entry<Integer, Group>> groupMapIterator = _groupIdMap.entrySet().iterator();
        while (groupMapIterator.hasNext()) {
            Group group = groupMapIterator.next().getValue();
            if (!validGroupIDs.contains(group.getDbId())) {
                groupMapIterator.remove();
                for (Student member : group.getMembers()) {
                    _groupsCache.get(group.getAssignment()).remove(member);
                }
            }
        }

        //update the groups in the map and
        //add any group that has been added to the database
        for (GroupRecord groupRecord : groupRecords) {
            Collection<Student> groupMembers = new ArrayList<Student>(groupRecord.getMemberIDs().size());
            for (Integer memberID : groupRecord.getMemberIDs()) {
                if (!_studentIdMap.containsKey(memberID)) {
                    throw new ServicesException("Student id [" + memberID + "] is "
                            + "included as a group member but does not map to a Student object.");
                }

                groupMembers.add(_studentIdMap.get(memberID));
            }
            
            if (_groupIdMap.containsKey(groupRecord.getDbId())) {
                Group group = _groupIdMap.get(groupRecord.getDbId());

                //calling Group.update(...) will mutate any out-of-date fields of the Group object
                group.update(groupRecord.getName(), groupMembers);
            }
            else {
                Assignment asgn = Allocator.getConfigurationInfo().getAssignment(groupRecord.getAssignmentID());
                if (asgn == null) {
                    throw new ServicesException("Assignment id [" + groupRecord.getAssignmentID() + "] "
                            + "is included in a group record but does not map to an Assignment object.");
                }
                
                Group newGroup = new Group(groupRecord.getDbId(), asgn, groupRecord.getName(), groupMembers);
                _groupIdMap.put(newGroup.getDbId(), newGroup);
                for (Student member : newGroup.getMembers()) {
                    _groupsCache.get(newGroup.getAssignment()).put(member, newGroup);
                }
            }
        }
    }
    
}
