package uk.gov.moj.cpp.hearing.persist.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "hearing_defence_counsel_defendant")
@IdClass(value = DefenceCounselDefendantCompositeKey.class)
public class DefenceCounselDefendant {

    @Id
    @Column(name = "defence_counsel_attendee_id")
    private UUID defenceCounselAttendeeId;

    @Id
    @Column(name = "defendant_id")
    private UUID defendantId;

    public DefenceCounselDefendant() {
        // for JPA
    }

    public DefenceCounselDefendant(final UUID defenceCounselAttendeeId, final UUID defendantId) {
        this.defenceCounselAttendeeId = defenceCounselAttendeeId;
        this.defendantId = defendantId;
    }

    public UUID getDefenceCounselAttendeeId() {
        return defenceCounselAttendeeId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }
}
