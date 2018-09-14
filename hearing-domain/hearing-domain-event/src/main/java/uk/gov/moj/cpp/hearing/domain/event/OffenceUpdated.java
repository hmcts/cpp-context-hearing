package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.Jurors;
import uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence;
import uk.gov.justice.json.schemas.core.Offence;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
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

    public OffenceUpdated withPlea(final uk.gov.moj.cpp.hearing.domain.Plea plea) {
        if (null != plea) {
            this.offence.setPlea(uk.gov.justice.json.schemas.core.Plea.plea()
                    .withDelegatedPowers(DelegatedPowers.delegatedPowers() //TODO
                            .withFirstName(null)
                            .withLastName(null)
                            .withUserId(null)
                            .build())
                    .withOffenceId(plea.getOffenceId())
                    .withOriginatingHearingId(plea.getOriginHearingId())
                    .withPleaDate(plea.getPleaDate())
                    .withPleaValue(plea.getValue())
                    .build());
        }
        return this;
    }

    public OffenceUpdated withVerdict(final uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert verdict) {
        if (null != verdict) {
            this.offence.setVerdict(uk.gov.justice.json.schemas.core.Verdict.verdict()
                    .withJurors(Jurors.jurors()
                            .withNumberOfJurors(verdict.getNumberOfJurors())
                            .withNumberOfSplitJurors(verdict.getNumberOfSplitJurors())
                            .withUnanimous(verdict.getUnanimous())
                            .build())
                    .withLesserOrAlternativeOffence(LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                            .withDescription(verdict.getTitle())
                            .withLegislation(verdict.getLegislation())
                            .withOffenceCode(verdict.getOffenceCode())
                            .withOffenceDefinitionId(verdict.getOffenceDefinitionId())
                            .build())
                    .withOffenceId(verdict.getOffenceId())
                    .withVerdictDate(verdict.getVerdictDate())
                    .withVerdictType(uk.gov.justice.json.schemas.core.VerdictType.verdictType()
                            .withCategory(verdict.getCategory())
                            .withCategoryType(verdict.getCategoryType())
                            .withVerdictTypeId(verdict.getVerdictTypeId())
                            .build())
                    .build());
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