package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.event.witness-added-to-hearing")
@SuppressWarnings({"squid:S2384", "pmd:BeanMembersShouldSerialize", "pmd:BeanMembersShouldSerialize"})
public class WitnessAddedToHearing implements Serializable {

   private String witness;

   private UUID hearingId;

    public WitnessAddedToHearing(final String witness, final UUID hearingId) {
        this.witness = witness;
        this.hearingId = hearingId;
    }

    public String getWitness() {
        return witness;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
