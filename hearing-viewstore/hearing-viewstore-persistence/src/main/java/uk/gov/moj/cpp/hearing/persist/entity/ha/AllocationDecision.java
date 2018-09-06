package uk.gov.moj.cpp.hearing.persist.entity.ha;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import uk.gov.justice.json.schemas.core.CourtDecision;
import uk.gov.justice.json.schemas.core.DefendantRepresentation;
import uk.gov.justice.json.schemas.core.ProsecutionRepresentation;

import java.util.Objects;

@Embeddable
public class AllocationDecision {

    @Column(name = "court_decision")
    private CourtDecision courtDecision;

    @Column(name = "prosecution_representation")
    private ProsecutionRepresentation prosecutionRepresentation;

    @Column(name = "defendant_representation")
    private DefendantRepresentation defendantRepresentation;

    @Column(name = "indication_of_sentence")
    private String indicationOfSentence;

    public CourtDecision getCourtDecision() {
        return courtDecision;
    }

    public void setCourtDecision(CourtDecision courtDecision2) {
        this.courtDecision = courtDecision2;
    }

    public ProsecutionRepresentation getProsecutionRepresentation() {
        return prosecutionRepresentation;
    }

    public void setProsecutionRepresentation(ProsecutionRepresentation prosecutionRepresentation2) {
        this.prosecutionRepresentation = prosecutionRepresentation2;
    }

    public DefendantRepresentation getDefendantRepresentation() {
        return defendantRepresentation;
    }

    public void setDefendantRepresentation(DefendantRepresentation defendantRepresentation2) {
        this.defendantRepresentation = defendantRepresentation2;
    }

    public String getIndicationOfSentence() {
        return indicationOfSentence;
    }

    public void setIndicationOfSentence(String indicationOfSentence) {
        this.indicationOfSentence = indicationOfSentence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AllocationDecision that = (AllocationDecision) o;
        return Objects.equals(courtDecision, that.courtDecision) &&
                Objects.equals(prosecutionRepresentation, that.prosecutionRepresentation) &&
                Objects.equals(defendantRepresentation, that.defendantRepresentation) &&
                Objects.equals(indicationOfSentence, that.indicationOfSentence);
    }

    @Override
    public int hashCode() {

        return Objects.hash(courtDecision, prosecutionRepresentation, defendantRepresentation, indicationOfSentence);
    }
}
