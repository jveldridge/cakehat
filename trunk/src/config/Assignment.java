package config;

import java.io.File;
import java.util.Vector;
import rubric.Rubric;
import rubric.RubricManager;
import rubric.visualizers.GradingVisualizer;
import rubric.visualizers.PreviewVisualizer;
import utils.Allocator;
import utils.FileViewerView;

/**
 *
 * @author jak2
 */
public class Assignment
{
    private String _name;
    private int _number;
    private File _deductionsFile, _rubricFile;
    private Rubric _rubric;
    private Vector<NonCodePart> _nonCodeParts = new Vector<NonCodePart>();
    private Vector<LabPart> _labParts = new Vector<LabPart>();
    private CodePart _codePart;

    public Assignment(String name, int number)
    {
        _name = name;
        _number = number;
    }

    public String getName()
    {
        return _name;
    }

    public int getNumber()
    {
        return _number;
    }

    //Deduction List

    void setDeductionList(String filePath)
    {
        _deductionsFile = new File(filePath);
    }

    public boolean hasDeductionList()
    {
        boolean exists = (_deductionsFile != null) && (_deductionsFile.exists());

        return exists;
    }

    public void viewDeductionList()
    {
        FileViewerView fv = new FileViewerView(_deductionsFile);
        fv.setTitle(_name + " Deductions List");
    }

    //Rubric

    void setRubric(String filePath)
    {
        _rubricFile = new File(filePath);
    }

    public boolean hasRubric()
    {
        boolean exists = (_rubricFile != null) && (_rubricFile.exists());

        return exists;
    }

    Rubric getRubric()
    {
        if(_rubric == null)
        {
            _rubric = RubricManager.processXML(_rubricFile.getAbsolutePath());
        }

        return _rubric;
    }

    public void previewRubric()
    {
        new PreviewVisualizer(this.getRubric(), this.getName());
    }

    public void viewRubric(String studentLogin)
    {
        new GradingVisualizer(this.getName(), Allocator.getGeneralUtilities().getUserLogin(), studentLogin);
    }

    // Parts

    void addNonCodePart(NonCodePart part)
    {
        _nonCodeParts.add(part);
    }

    public Iterable<NonCodePart> getNoncodeParts()
    {
        return _nonCodeParts;
    }

    public boolean hasNoncodeParts()
    {
        return !_nonCodeParts.isEmpty();
    }

    void addLabPart(LabPart part)
    {
        _labParts.add(part);
    }

    public Iterable<LabPart> getLabParts()
    {
        return _labParts;
    }

    public boolean hasLabParts()
    {
        return !_labParts.isEmpty();
    }

    void addCodePart(CodePart codePart)
    {
        _codePart = codePart;
    }

    public CodePart getCodePart()
    {
        return _codePart;
    }

    public boolean hasCodePart()
    {
        return (_codePart != null);
    }

    // Points

    public int getTotalPoints()
    {
        int points = 0;

        for(Part part : _nonCodeParts)
        {
            points += part.getPoints();
        }
        for(Part part : _labParts)
        {
            points += part.getPoints();
        }
        if(this.hasCodePart())
        {
            points += _codePart.getPoints();
        }

        return points;
    }


    public String toString()
    {
        return _name;
    }
    
}