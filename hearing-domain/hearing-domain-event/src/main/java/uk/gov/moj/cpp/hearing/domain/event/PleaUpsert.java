package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.PleaValue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.hearing-offence-plea-updated")
public class PleaUpsert implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID hearingId;
    private UUID offenceId;
    private LocalDate pleaDate;
    private PleaValue value;
    private DelegatedPowers delegatedPowers;

    public PleaUpsert() {

    }

    @JsonCreator
    public PleaUpsert(@JsonProperty("hearingId") final UUID originHearingId,
                      @JsonProperty("offenceId") final UUID offenceId,
                      @JsonProperty("pleaDate") final LocalDate pleaDate,
                      @JsonProperty("value") final PleaValue value,
                      @JsonProperty("delegatedPowers") final DelegatedPowers delegatedPowers) {
        this.hearingId = originHearingId;
        this.offenceId = offenceId;
        this.pleaDate = pleaDate;
        this.value = value;
        this.delegatedPowers = delegatedPowers;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
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

    public PleaUpsert setHearingId(UUID hearingId) {
        this.hearingId = hearingId;
        return this;
    }

    public PleaUpsert setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public PleaUpsert setPleaDate(LocalDate pleaDate) {
        this.pleaDate = pleaDate;
        return this;
    }

    public PleaUpsert setValue(PleaValue value) {
        this.value = value;
        return this;
    }

    public PleaUpsert setDelegatedPowers(DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
        return this;
    }

    public static PleaUpsert pleaUpsert() {
        return new PleaUpsert();
    }
}