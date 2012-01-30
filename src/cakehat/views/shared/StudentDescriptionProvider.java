package cakehat.views.shared;

import cakehat.database.Student;
import support.ui.PartialDescriptionProvider;

/**
 * Displays the login and name as {@code login (FirstName LastName){.
 * 
 * @author jak2
 */
public class StudentDescriptionProvider extends PartialDescriptionProvider<Student> {

    public static final StudentDescriptionProvider INSTANCE = new StudentDescriptionProvider();
    
    private StudentDescriptionProvider() {}
    
    @Override
    public String getDisplayText(Student student) {
        return student.getLogin() + " (" + student.getName() + ")";
    }
}
