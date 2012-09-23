package cakehat.views.shared.gradingsheet;

/**
 *
 * @author jak2
 */
class GradingSheetInitializationException extends Exception
{
    private final String _userFriendlyMessage;
    
    GradingSheetInitializationException(String message, Throwable cause, String userFriendlyMessage)
    {
        super(message, cause);
        
        _userFriendlyMessage = userFriendlyMessage;
    }
    
    String getUserFriendlyMessage()
    {
        return _userFriendlyMessage;
    }
}