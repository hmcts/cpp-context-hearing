package uk.gov.moj.cpp.hearing.command.hearing.details;

import uk.gov.justice.core.courts.ProsecutionCase;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UpdateRelatedHearingCommand implements Serializable {

    private static final long serialVersionUID = -7248560470176271257L;
    private UUID hearingId;
    private List<ProsecutionCase> prosecutionCases;
    private List<UUID> shadowListedOffences;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public List<ProsecutionCase> getProsecutionCases() {
        return prosecutionCases;
    }

    public void setProsecutionCases(final List<ProsecutionCase> prosecutionCases) {
        this.prosecutionCases = prosecutionCases;
    }

    public List<UUID> getShadowListedOffences() {
        return shadowListedOffences;
    }

    public void setShadowListedOffences(final List<UUID> shadowListedOffences) {
        this.shadowListedOffences = shadowListedOffences;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hearingId, this.prosecutionCases, this.shadowListedOffences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UpdateRelatedHearingCommand that = (UpdateRelatedHearingCommand) o;
        return Objects.equals(this.hearingId, that.hearingId)
                && Objects.equals(this.prosecutionCases, that.prosecutionCases)
                && Objects.equals(this.shadowListedOffences, that.shadowListedOffences);
    }

}
