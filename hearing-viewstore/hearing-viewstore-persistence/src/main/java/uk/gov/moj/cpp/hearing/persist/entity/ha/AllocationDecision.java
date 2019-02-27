package uk.gov.moj.cpp.hearing.persist.entity.ha;

import uk.gov.justice.core.courts.CourtDecision;
import uk.gov.justice.core.courts.DefendantRepresentation;
import uk.gov.justice.core.courts.ProsecutionRepresentation;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class AllocationDecision {

    @Column(name = "court_decision")
    @Enumerated(EnumType.STRING)
    private CourtDecision courtDecision;

    @Column(name = "prosecution_representation")
    @Enumerated(EnumType.STRING)
    private ProsecutionRepresentation prosecutionRepresentation;

    @Column(name = "defendant_representation")
    @Enumerated(EnumType.STRING)
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
}
