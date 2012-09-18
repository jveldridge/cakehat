package support.utils;

/**
 *
 * @author jak2
 */
public class NullMath
{
    private NullMath() { }
    
    /**
     * Adds {@code num1} to {@code num2}.
     * 
     * @param num1
     * @param num2
     * @return 
     */
    public static Double add(Double num1, Double num2)
    {
        Double sum = null;
        if(num1 != null && num2 != null)
        {
            sum = num1 + num2;
        }        
        else if(num1 != null && num2 == null)
        {
            sum = num1;
        }
        else if(num1 == null && num2 != null)
        {
            sum = num2;
        }
        
        return sum;
    }
    
    /**
     * Subtracts {@code num2} from {@code num1}.
     * 
     * @param num1
     * @param num2
     * @return 
     */
    public static Double subtract(Double num1, Double num2)
    {
        Double difference = null;
        if(num1 != null && num2 != null)
        {
            difference = num1 - num2;
        }        
        else if(num1 != null && num2 == null)
        {
            difference = num1;
        }
        else if(num1 == null && num2 != null)
        {
            difference = -num2;
        }
        
        return difference;
    }
    
    /**
     * Returns the string representation of {@code num} if not {@code null} and the empty string if {@code null}.
     * 
     * @param num
     * @return 
     */
    public static String toString(Double num)
    {
        String str;
        if(num == null)
        {
            str = "";
        }
        else
        {
            str = Double.toString(num);
        }
        
        return str;
    }
}