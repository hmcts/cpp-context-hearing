package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.applicant-counsel-added")
public class ApplicantCounselAdded implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ApplicantCounsel applicantCounsel;
    private final UUID hearingId;

    public ApplicantCounselAdded(final ApplicantCounsel applicantCounsel, final UUID hearingId) {
        this.applicantCounsel = applicantCounsel;
        this.hearingId = hearingId;
    }


    public UUID getHearingId() {
        return hearingId;
    }

    public ApplicantCounsel getApplicantCounsel() {
        return applicantCounsel;
    }
}
