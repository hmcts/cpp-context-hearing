package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CourtIndicatedSentence {

    @Column(name = "court_indicated_sentence_id")
    private UUID id;

    @Column(name = "court_indicated_sentence_description")
    private String courtIndicatedSentenceDescription;

    @Column(name = "court_indicated_sentence_type_id")
    private UUID courtIndicatedSentenceTypeId;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getCourtIndicatedSentenceDescription() {
        return courtIndicatedSentenceDescription;
    }

    public void setCourtIndicatedSentenceDescription(final String courtIndicatedSentenceDescription) {
        this.courtIndicatedSentenceDescription = courtIndicatedSentenceDescription;
    }

    public UUID getCourtIndicatedSentenceTypeId() {
        return courtIndicatedSentenceTypeId;
    }

    public void setCourtIndicatedSentenceTypeId(final UUID courtIndicatedSentenceTypeId) {
        this.courtIndicatedSentenceTypeId = courtIndicatedSentenceTypeId;
    }
}
