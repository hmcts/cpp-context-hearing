package uk.gov.moj.cpp.hearing.domain.event;

import static java.util.Objects.nonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.Plea;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.events.offence-updated")
@SuppressWarnings("squid:S00107")
public class OffenceUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID defendantId;
    private Offence offence;

    private OffenceUpdated() {
    }

    @JsonCreator
    protected OffenceUpdated(@JsonProperty(value = "hearingId", required = true) UUID hearingId,
                             @JsonProperty(value = "defendantId", required = true) UUID defendantId,
                             @JsonProperty(value = "offence", required = true) final Offence offence) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.offence = offence;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public Offence getOffence() {
        return offence;
    }

    public OffenceUpdated withHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public OffenceUpdated withDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public OffenceUpdated withOffence(final Offence offence) {
        this.offence = offence;
        return this;
    }

    public OffenceUpdated withPlea(final Plea plea) {
        this.offence.setPlea(plea);
        return this;
    }

    public OffenceUpdated withVerdict(final uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert verdictUpsert) {
        if (nonNull(verdictUpsert)) {
            this.offence.setVerdict(verdictUpsert.getVerdict());
        }
        return this;
    }

    public OffenceUpdated withConvictionDate(final LocalDate convictionDate) {
        offence.setConvictionDate(convictionDate);
        return this;
    }

    public static OffenceUpdated offenceUpdated() {
        return new OffenceUpdated();
    }
}