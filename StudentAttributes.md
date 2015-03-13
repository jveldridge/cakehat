Cakehat stores two boolean attributes about each student: whether the student is enabled, and whether the student has submitted a signed collaboration policy.  This document describes the ways in which these attributes affect how cakehat handles students.

1. Enabled Attribute

---

All students are enabled when they are added to the database.  Setting the enabled attribute to false for a student (i.e., "disabling" the student) is used in place of deleting the student.  This is to preserve data (handin availability, grades, etc.) associated with that student for historical purposes such as investigating potential collaboration policy violations.  Disabling a student has the following effects:
> -The student is displayed differently in the admin view UI
> > This will likely include changing the font/color of the student's login and dispaying the student at the bottom of the student list.


> -The student will not be included when all students are selected
> > Buttons or other UI elements used to select all students will not select disabled students; rather, disabled students must be separately and explicitly selected.


> -Disabled students cannot be distributed, and a notification will be shown in the admin view when a disabled student has handin
> > Disabling a student means that that the student is not expected to submit any more assignments.  Thus, if an admin user goes to make a distribution and a disabled student has a handin, the user will be notified of this fact and given the option to re-enable the student.  If the user chooses not to re-enable the student, the student will not be included in the list of students to be distributed.  This notification will also be shown in case of a group project in which some member of a group with a handin is disabled.  Again, the group is still distributable if at least one student is **not** disabled.


> -The student will not be included in charts/statistics
> > Charts and statistics that display information about the entire class will not include the grades of or other data about disabled students.


2. Has Collaboration Policy Attribute

---

This attribute is used solely to distinguish in the admin view UI between students who have submitted a signed collaboration policy and those who have not.  Students who have not submitted a collaboration policy will be specially marked so that admins can deal with them according to course policy.  However, they will not be handled differently by cakehat in any other respect.