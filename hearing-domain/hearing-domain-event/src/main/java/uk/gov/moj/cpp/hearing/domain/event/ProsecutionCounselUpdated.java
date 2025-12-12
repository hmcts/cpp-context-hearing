package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.justice.core.courts.ProsecutionCounsel;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.prosecution-counsel-updated")
public class ProsecutionCounselUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ProsecutionCounsel prosecutionCounsel;
    private final UUID hearingId;

    public ProsecutionCounselUpdated(final ProsecutionCounsel prosecutionCounsel, final UUID hearingId) {
        this.prosecutionCounsel = prosecutionCounsel;
        this.hearingId = hearingId;
    }


    public UUID getHearingId() {
        return hearingId;
    }

    public ProsecutionCounsel getProsecutionCounsel() {
        return prosecutionCounsel;
    }
}
