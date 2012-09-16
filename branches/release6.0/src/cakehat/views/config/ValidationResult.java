package cakehat.views.config;

/**
 *
 * @author jak2
 */
class ValidationResult
{   
    public static enum ValidationState
    {
        /**
         * No validation was performed.
         */
        NOT_VALIDATED, 
        
        /**
         * No issue is known with the user provided value, but it is not necessarily correct. For example, an email
         * address might be have a valid format, but there is no guarantee that the email address actually exists.
         */
        NO_KNOWN_ISSUE,
        
        /**
         * The value provided by the user is likely not what they want, but the value does not prevent cakehat from
         * running. For example, two assignments with identical names.
         */
        WARNING,
        
        /**
         * The value provided by the user is incorrect and cannot be accepted by cakehat. For example, two parts having
         * the same quick name.
         */
        ERROR
    }
    
    private final ValidationState _state;
    private final String _message;
    
    public ValidationResult(ValidationState state, String message)
    {
        _state = state;
        _message = message;
    }
    
    public ValidationState getValidationState()
    {
        return _state;
    }
    
    public String getMessage()
    {   
        return _message;
    }
    
    /**
     * Helper fields and methods for common validation situations.
     */
    
    public static final ValidationResult NO_VALIDATION = new ValidationResult(ValidationState.NOT_VALIDATED, null);
    public static final ValidationResult NO_ISSUE = new ValidationResult(ValidationState.NO_KNOWN_ISSUE, null);
    
    public static final ValidationResult TEXT_EMPTY =
            new ValidationResult(ValidationState.WARNING, "Please fill in this field");
    public static ValidationResult validateNotEmpty(String text)
    {
        ValidationResult result;
        
        if(text.isEmpty())
        {
            result = TEXT_EMPTY;
        }
        else
        {
            result = NO_ISSUE;
        }
        
        return result;
    }
}