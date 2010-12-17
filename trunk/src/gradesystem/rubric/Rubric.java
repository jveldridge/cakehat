package gradesystem.rubric;

import gradesystem.config.HandinPart;
import gradesystem.config.Part;
import gradesystem.config.TimeInformation;
import java.util.Collection;
import java.util.Vector;
import javax.swing.JOptionPane;
import gradesystem.utils.Allocator;

/**
 * An object representation of a GML file. Used internally within the the rubric
 * package.
 *
 * @author spoletto
 * @author jak2
 */
class Rubric
{
    private String _name = "";
    private TimeStatus _status;
    private int _number = 0;
    private int _daysLate = 0;
    private Person _student = new Person();
    private Person _grader = new Person();
    private Vector<Section> _sections = new Vector<Section>();
    private Section _extraCredit = new Section();
    protected HandinPart _handinPart;

    Rubric(HandinPart part)
    {
        _handinPart = part;
    }

    // Time Information

    TimeInformation getTimeInformation()
    {
        return _handinPart.getTimeInformation();
    }

    // Name

    void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

    // Status

    void setStatus(TimeStatus status)
    {
        _status = status;
    }

    public TimeStatus getStatus()
    {
        return _status;
    }

    // Number

    void setNumber(int number)
    {
        _number = number;
    }

    public int getNumber()
    {
        return _number;
    }

    // Days Late

    void setDaysLate(int numDays)
    {
        _daysLate = numDays;
    }

    public int getDaysLate()
    {
        return _daysLate;
    }

    // Student

    void setStudent(String name, String acct)
    {
        _student.setName(name);
        _student.setAccount(acct);
    }

    void setStudent(Person student)
    {
        _student = student;
    }

    public Person getStudent()
    {
        return _student;
    }

    public String getStudentName()
    {
        return _student.getName();
    }

    public String getStudentAccount()
    {
        return _student.getAccount();
    }

    // Grader

    void setGrader(String name, String acct)
    {
        _grader.setName(name);
        _grader.setAccount(acct);
    }

    void setGrader(Person grader)
    {
        _grader = grader;
    }

    public Person getGrader()
    {
        return _grader;
    }

    public String getGraderName()
    {
        return _grader.getName();
    }

    public String getGraderAccount()
    {
        return _grader.getAccount();
    }

    // Sections

    Section addSection()
    {
        Section section = new Section();
        _sections.add(section);

        return section;
    }

    public Collection<Section> getSections()
    {
        return _sections;
    }

    // Extra credit

    Section addExtraCredit()
    {
        _extraCredit = new Section();

        return _extraCredit;
    }

    public Section getExtraCredit()
    {
        return _extraCredit;
    }

    public boolean hasExtraCredit()
    {
        return !_extraCredit.getSubsections().isEmpty();
    }

    /**
     * Total points earned by the student for all the rubric components.
     *
     * Does not take into account handin status rewards/deductions.
     *
     * @return
     */
    double getTotalRubricPoints()
    {
        double score = 0.0;
        for (Section section : _sections)
        {
            for(Subsection subsection : section.getSubsections())
            {
                score += subsection.getScore();
            }
        }
        for (Subsection subsection : _extraCredit.getSubsections())
        {
            score += subsection.getScore();
        }
        
        score = Allocator.getGeneralUtilities().round(score, 2);

        return score;
    }

    /**
     * Total points earned by the student for all the rubric components.
     *
     * Applies the handin status rewards/deductions.
     *
     * @return
     */
    public double getTotalRubricScore()
    {
        double points = getTotalRubricPoints();

        // Apply status
        points += getDeduction();

        return points;
    }

    /**
     * Total points earned by the student for the rubric components that are
     * part of the handin; does not include any parts that pull from sources.
     *
     * Does not take into account handin status deductions.
     *
     * @return
     */
    double getTotalHandinPoints()
    {
        double score = 0.0;
        for (Section section : _sections)
        {
            for(Subsection subsection : section.getSubsections())
            {
                if(!subsection.hasSource())
                {
                    score += subsection.getScore();
                }
            }
        }
        for (Subsection subsection : _extraCredit.getSubsections())
        {
            if(!subsection.hasSource())
            {
                score += subsection.getScore();
            }
        }

        score = Allocator.getGeneralUtilities().round(score, 2);

        return score;
    }

    /**
     * Total points earned by the student for all the rubric components that are
     * part of the handin; does not include any parts that pull from sources.
     *
     * Applies the handin status rewards/deductions.
     *
     * @return
     */
    public double getTotalHandinScore()
    {
        double points = getTotalHandinPoints();

        // Apply status
        points += getDeduction();

        return points;
    }

    double getDeduction()
    {
        return _status.getDeduction(_handinPart, this);
    }

    /**
     * Total outofs for all rubric components.
     *
     * @return
     */
    public double getTotalRubricOutOf()
    {
        double outOf = 0.0;
        for (Section section: _sections)
        {
            for(Subsection subsection : section.getSubsections())
            {
                outOf += subsection.getOutOf();
            }
        }
        
        outOf = Allocator.getGeneralUtilities().round(outOf, 2);
        
        return outOf;
    }

    /**
     * Total outofs for all rubric components that are part of the handin;
     * does not include any parts that pull from sources.
     *
     * @return
     */
    public double getTotalHandinOutOf()
    {
        double outOf = 0.0;
        for (Section section: _sections)
        {
            for(Subsection subsection : section.getSubsections())
            {
                if(!subsection.hasSource())
                {
                    outOf += subsection.getOutOf();
                }
            }
        }

        outOf = Allocator.getGeneralUtilities().round(outOf, 2);

        return outOf;
    }

    // Person

    public static class Person
    {
        private String _name = "", _acct = "";

        Person() { }

        Person(String name, String acct)
        {
            this.setName(name);
            this.setAccount(acct);
        }

        void setName(String name)
        {
            _name = name;
        }

        public String getName()
        {
            return _name;
        }

        void setAccount(String acct)
        {
            _acct = acct;
        }

        public String getAccount()
        {
            return _acct;
        }
    }

    // Section

    public class Section
    {
        private String _name = "";
        private Collection<Subsection> _subsections = new Vector<Subsection>();
        private Collection<String> _notes = new Vector<String>();
        private Collection<String> _comments = new Vector<String>();

        Section() { }

        // Name

        void setName(String name)
        {
            _name = name;
        }

        public String getName()
        {
            return _name;
        }

        // Subsections

        Subsection addSubsection(String name, double score, double outof, String source)
        {
            Subsection subsection = new Subsection(name, score, outof, source);
            _subsections.add(subsection);

            return subsection;
        }

        public Collection<Subsection> getSubsections()
        {
            return _subsections;
        }

        // Notes

        void setNotes(Collection<String> notes)
        {
            _notes = notes;
        }

        void addNote(String note)
        {
            _notes.add(note);
        }

        public Collection<String> getNotes()
        {
            return _notes;
        }

        public boolean hasNotes()
        {
            return !_notes.isEmpty();
        }

        // Coments

        void setComments(Collection<String> comments)
        {
            _comments = comments;
        }

        void addComment(String comment)
        {
            _comments.add(comment);
        }

        public Collection<String> getComments()
        {
            return _comments;
        }

        public boolean hasComments()
        {
            return !_comments.isEmpty();
        }

        // Point totaling

        public double getSectionScore()
        {
            double score = 0.0;
            for(Subsection subsection : _subsections)
            {
                score += subsection.getScore();
            }
            return score;
        }

        public double getSectionOutOf()
        {
            double outOf = 0.0;
            for(Subsection subsection : _subsections)
            {
                outOf += subsection.getOutOf();
            }
            return outOf;
        }
    }

    public class Subsection
    {
        private String _name = "";
        private double _score = 0.0, _outOf = 0.0;
        private Vector<Detail> _details = new Vector<Detail>();
        //What part this subsection pulls from in the database (if at all)
        private String _source = null;

        Subsection(String name, double score, double outOf, String source)
        {
            this.setName(name);
            this.setOutOf(outOf);
            this.setSource(source);

            if(this.hasSource())
            {
                this.loadScoreFromSource();
            }
            else
            {
                this.setScore(score);
            }
        }

        private void loadScoreFromSource()
        {
            //Get corresponding part
            Part sourcePart = null;
            for(Part part : Rubric.this._handinPart.getAssignment().getParts())
            {
                if(part.getName().equals(_source))
                {
                    sourcePart = part;
                }
            }
            //If no part was found we've got an issue
            if(sourcePart == null)
            {
                JOptionPane.showMessageDialog(null, "Rubric specifies source part named [" +
                                              _source + "] for assignment [" +
                                              Rubric.this._handinPart.getAssignment().getName() + "]. \n" +
                                              "This part was not found.");

                return;
            }

            //If a there is a student account (when not a template), load score
            if(!Rubric.this.getStudentAccount().isEmpty())
            {
                String studentLogin = Rubric.this.getStudentAccount();
                _score = Allocator.getDatabaseIO().getStudentScore(studentLogin, sourcePart);
            }
        }

        // Name

        void setName(String name)
        {
            _name = name;
        }

        public String getName()
        {
            return _name;
        }

        // Score

        void setScore(double score)
        {
            //If this doesn't have a source, store the score
            if(!this.hasSource())
            {
                _score = score;
            }
        }

        public double getScore()
        {
            return _score;
        }

        // OutOf

        void setOutOf(double outOf)
        {
            _outOf = outOf;
        }

        public double getOutOf()
        {
            return _outOf;
        }

        // Source

        void setSource(String source)
        {
            _source = source;
        }

        public String getSource()
        {
            return _source;
        }

        public boolean hasSource()
        {
            return (_source != null && !_source.isEmpty());
        }

        // Details

        void addDetail(Detail detail)
        {
            _details.add(detail);
        }

        void addDetail(String name, int value)
        {
            _details.add(new Detail(name, value));
        }

        public Collection<Detail> getDetails()
        {
            return _details;
        }
    }

    public static class Detail
    {
        private String _name;
        private double _value;

        Detail() { }

        Detail(String name, double value)
        {
            setName(name);
            setValue(value);
        }

        void setName(String name)
        {
            _name = name;
        }

        public String getName()
        {
            return _name;
        }

        void setValue(double value)
        {
            _value = value;
        }

        public double getValue()
        {
            return _value;
        }
    }

}