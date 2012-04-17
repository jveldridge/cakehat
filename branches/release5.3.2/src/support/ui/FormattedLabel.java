package support.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author jak2
 */
public class FormattedLabel extends JLabel
{
    public static FormattedLabel asHeader(String text)    
    {
        return new FormattedLabel(text, 16);
    }
    
    public static FormattedLabel asSubheader(String text) 
    {
        return new FormattedLabel(text, 14);
    }
    
    public static FormattedLabel asContent(String text)
    {
        return new FormattedLabel(text, 12);
    }
    
    private FormattedLabel(String text, int size)
    {
        this.setText(text);
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.setFont(new Font("Dialog", Font.BOLD, size));
    }

    @Override
    public void setText(String text)
    {
        super.setText("<html>" + text + "</html>");
    }

    @Override
    public void setToolTipText(String tooltip)
    {
        final int wrapLength = 80;
        if(tooltip != null && tooltip.length() > wrapLength)
        {
            StringBuilder wrappedTooltip = new StringBuilder("<html>");
            int lineCounter = 0;
            for(char c : tooltip.toCharArray())
            {
                lineCounter++;

                if(lineCounter > wrapLength && c == ' ')
                {
                    wrappedTooltip.append("<br/>");
                    lineCounter = 0;
                }
                else
                {
                    wrappedTooltip.append(c);
                }
            }
            wrappedTooltip.append("</html>");

            tooltip = wrappedTooltip.toString();
        }
        super.setToolTipText(tooltip);
    }

    public FormattedLabel grayOut()
    {
        this.setForeground(Color.GRAY);

        return this;
    }

    public FormattedLabel showAsErrorMessage()
    {
        this.setForeground(Color.RED);

        return this;
    }

    public FormattedLabel centerHorizontally()
    {
        this.setHorizontalAlignment(SwingConstants.CENTER);

        return this;
    }

    public FormattedLabel usePlainFont()
    {
        this.setFont(new Font("Dialog", Font.PLAIN, this.getFont().getSize()));

        return this;
    }
}