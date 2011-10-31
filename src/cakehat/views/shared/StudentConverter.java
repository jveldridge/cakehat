package cakehat.views.shared;

import cakehat.database.Student;
import support.ui.StringConverter;

/**
 * Displays the login and name as <code>login (FirstName LastName)</code>
 */
public class StudentConverter implements StringConverter<Student> {

    public static final StudentConverter INSTANCE = new StudentConverter();
    
    private StudentConverter() {}
    
    @Override
    public String convertToString(Student student) {
        return student.getLogin() + " (" + student.getName() + ")";
    }
}
