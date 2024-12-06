package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.offence-updated-v2")
@SuppressWarnings("squid:S00107")
public class OffenceUpdatedV2 implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID defendantId;
    private List<Offence> offences;

    private OffenceUpdatedV2() {
    }

    @JsonCreator
    protected OffenceUpdatedV2(@JsonProperty(value = "hearingId", required = true) UUID hearingId,
                               @JsonProperty(value = "defendantId", required = true) UUID defendantId,
                               @JsonProperty(value = "offences", required = true) final List<Offence> offences) {
        this.hearingId = hearingId;
        this.defendantId = defendantId;
        this.offences = offences;
    }

    public static OffenceUpdatedV2 offenceUpdatedV2() {
        return new OffenceUpdatedV2();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public List<Offence> getOffences() {
        return offences;
    }

    public OffenceUpdatedV2 withHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public OffenceUpdatedV2 withDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public OffenceUpdatedV2 withOffences(final List<Offence> offences) {
        this.offences = offences;
        return this;
    }
}