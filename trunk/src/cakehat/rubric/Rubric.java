package cakehat.rubric;

import cakehat.config.Part;
import java.util.Collection;
import java.util.Vector;
import javax.swing.JOptionPane;
import cakehat.Allocator;
import cakehat.database.Group;
import cakehat.config.handin.DistributablePart;
import cakehat.services.ServicesException;

/**
 * An object representation of a GML file. Used internally within the the rubric
 * package.
 *
 * @author spoletto
 * @author jak2
 */
@Deprecated
class Rubric
{
    private DistributablePart _distPart;
    private Group _group;
    private Vector<Section> _sections = new Vector<Section>();
    private Section _extraCredit = new Section();
    

    Rubric(DistributablePart part, Group group) {
        _distPart = part;
        _group = group;
    }

    public DistributablePart getDistributablePart() {
        return _distPart;
    }

    public Group getGroup() {
        return _group;
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
     * Total points earned by the student for all the rubric components,
     * including scores for components that are pulled from another source.
     *
     * Does not take into account handin status rewards/deductions.
     *
     * @return
     */
    double getTotalRubricScore()
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
     * Total points earned by the student for the rubric components that are
     * part of the handin; does not include any parts that pull from sources.
     *
     * Does not take into account handin status deductions.
     *
     * @return
     */
    double getTotalDistPartScore()
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
    public double getTotalDistPartOutOf()
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

        Subsection addSubsection(String name, double score, double outof, String source) throws RubricException
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

        Subsection(String name, double score, double outOf, String source) throws RubricException
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

        private void loadScoreFromSource() throws RubricException
        {
            //Get corresponding part
            Part sourcePart = null;
            for(Part part : Rubric.this._distPart.getAssignment().getParts())
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
                                              Rubric.this._distPart.getAssignment().getName() + "]. \n" +
                                              "This part was not found.");

                return;
            }

            //If a there is a student account (when not a template), load score
            if(Rubric.this.getGroup() != null) {
                Group group = Rubric.this.getGroup();
                try {
                    Double score = Allocator.getDataServices().getScore(group, sourcePart);
                    _score = (score == null ? 0 : score);
                } catch (ServicesException ex) {
                    throw new RubricException("The grade for part " + sourcePart + " could not be " +
                                              "loaded from the database.", ex);
                }
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