package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.PleaValue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class UpdateHearingWithInheritedPleaCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;
    private UUID hearingId;
    private UUID originHearingId;
    private LocalDate pleaDate;
    private PleaValue value;
    private DelegatedPowers delegatedPowers;

    public UpdateHearingWithInheritedPleaCommand() {
    }

    @JsonCreator
    public UpdateHearingWithInheritedPleaCommand(@JsonProperty("offenceId") UUID offenceId,
                                                 @JsonProperty("caseId") UUID caseId,
                                                 @JsonProperty("defendantId") UUID defendantId,
                                                 @JsonProperty("hearingId") UUID hearingId,
                                                 @JsonProperty("originHearingId") UUID originHearingId,
                                                 @JsonProperty("pleaDate") LocalDate pleaDate,
                                                 @JsonProperty("value") PleaValue value,
                                                 @JsonProperty("delegatedPowers") DelegatedPowers delegatedPowers) {
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

    public UpdateHearingWithInheritedPleaCommand setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public UpdateHearingWithInheritedPleaCommand setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public UpdateHearingWithInheritedPleaCommand setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UpdateHearingWithInheritedPleaCommand setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public UpdateHearingWithInheritedPleaCommand setOriginHearingId(UUID originHearingId) {
        this.originHearingId = originHearingId;
        return this;
    }

    public UpdateHearingWithInheritedPleaCommand setPleaDate(LocalDate pleaDate) {
        this.pleaDate = pleaDate;
        return this;
    }

    public UpdateHearingWithInheritedPleaCommand setValue(PleaValue value) {
        this.value = value;
        return this;
    }

    public UpdateHearingWithInheritedPleaCommand setDelegatedPowers(DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
        return this;
    }

    public static UpdateHearingWithInheritedPleaCommand updateHearingWithInheritedPleaCommand(){
        return new UpdateHearingWithInheritedPleaCommand();
    }
}