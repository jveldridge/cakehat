package cakehat.views.shared.gradingsheet;

import cakehat.logging.ErrorReporter;
import com.google.common.collect.ImmutableList;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import support.ui.FormattedLabel;

/**
 *
 * @author jak2
 */
class FailedInitializationPanel extends GradingSheetPanel
{
    FailedInitializationPanel(final GradingSheetInitializationException ex)
    {
        super(Color.WHITE, false);
        
        addContent(FormattedLabel.asSubheader(ex.getUserFriendlyMessage()).centerHorizontally().showAsErrorMessage());
        
        ErrorReporter.report(ex);
    }

    @Override
    List<Component> getFocusableComponents()
    {
        return ImmutableList.of();
    }

    @Override
    Double getEarned()
    {
        return null;
    }

    @Override
    Double getOutOf()
    {
        return null;
    }

    @Override
    public void save() { }
}