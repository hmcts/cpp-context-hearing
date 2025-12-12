package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.respondent-counsel-updated")
public class RespondentCounselUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private final RespondentCounsel respondentCounsel;
    private final UUID hearingId;

    public RespondentCounselUpdated(final RespondentCounsel respondentCounsel, final UUID hearingId) {
        this.respondentCounsel = respondentCounsel;
        this.hearingId = hearingId;
    }


    public UUID getHearingId() {
        return hearingId;
    }

    public RespondentCounsel getRespondentCounsel() {
        return respondentCounsel;
    }
}
