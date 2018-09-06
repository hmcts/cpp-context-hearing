package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.PleaValue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.events.inherited-plea")
public class InheritedPlea implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;
    private UUID hearingId;
    private UUID originHearingId;
    private LocalDate pleaDate;
    private PleaValue value;
    private DelegatedPowers delegatedPowers;

    public InheritedPlea() {

    }

    @JsonCreator
    public InheritedPlea(@JsonProperty("offenceId") final UUID offenceId,
                         @JsonProperty("caseId") final UUID caseId,
                         @JsonProperty("defendantId") final UUID defendantId,
                         @JsonProperty("hearingId") final UUID hearingId,
                         @JsonProperty("originHearingId") final UUID originHearingId,
                         @JsonProperty("pleaDate") final LocalDate pleaDate,
                         @JsonProperty("value") final PleaValue value,
                         @JsonProperty("delegatedPowers") final DelegatedPowers delegatedPowers) {
        this.offenceId = offenceId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
        this.originHearingId = originHearingId;
        this.pleaDate = pleaDate;
        this.value = value;
        this.delegatedPowers = delegatedPowers;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOriginHearingId() {
        return originHearingId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public PleaValue getValue() {
        return value;
    }

    public DelegatedPowers getDelegatedPowers() {
        return delegatedPowers;
    }

    public InheritedPlea setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public InheritedPlea setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public InheritedPlea setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public InheritedPlea setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public InheritedPlea setOriginHearingId(UUID originHearingId) {
        this.originHearingId = originHearingId;
        return this;
    }

    public InheritedPlea setPleaDate(LocalDate pleaDate) {
        this.pleaDate = pleaDate;
        return this;
    }

    public InheritedPlea setValue(PleaValue value) {
        this.value = value;
        return this;
    }

    public InheritedPlea setDelegatedPowers(DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
        return this;
    }

    public static InheritedPlea inheritedPlea(){
        return new InheritedPlea();
    }
}
