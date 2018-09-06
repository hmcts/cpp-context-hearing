package uk.gov.moj.cpp.hearing.domain.updatepleas;

import uk.gov.justice.json.schemas.core.DelegatedPowers;
import uk.gov.justice.json.schemas.core.PleaValue;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public class Plea implements Serializable {

    private UUID originatingHearingId;

    private uk.gov.justice.json.schemas.core.DelegatedPowers delegatedPowers;

    private UUID offenceId;

    private LocalDate pleaDate;

    private PleaValue value;

    public UUID getOriginatingHearingId() {
        return this.originatingHearingId;
    }

    public uk.gov.justice.json.schemas.core.DelegatedPowers getDelegatedPowers() {
        return this.delegatedPowers;
    }

    public UUID getOffenceId() {
        return this.offenceId;
    }

    public LocalDate getPleaDate() {
        return this.pleaDate;
    }

    public PleaValue getValue() {
        return this.value;
    }

    public Plea setOriginatingHearingId(UUID originatingHearingId) {
        this.originatingHearingId = originatingHearingId;
        return this;
    }

    public Plea setDelegatedPowers(DelegatedPowers delegatedPowers) {
        this.delegatedPowers = delegatedPowers;
        return this;
    }

    public Plea setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public Plea setPleaDate(LocalDate pleaDate) {
        this.pleaDate = pleaDate;
        return this;
    }

    public Plea setValue(PleaValue value) {
        this.value = value;
        return this;
    }

    public static Plea plea() {
        return new Plea();
    }
}
