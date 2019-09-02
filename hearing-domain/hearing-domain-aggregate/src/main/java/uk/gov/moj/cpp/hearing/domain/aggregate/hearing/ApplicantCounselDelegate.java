package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ApplicantCounselUpdated;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class ApplicantCounselDelegate implements Serializable {

    private final HearingAggregateMomento momento;

    public ApplicantCounselDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleApplicantCounselAdded(final ApplicantCounselAdded applicantCounselAdded) {
        final ApplicantCounsel applicantCounsel = applicantCounselAdded.getApplicantCounsel();
        this.momento.getApplicantCounsels().put(applicantCounsel.getId(), applicantCounsel);
    }

    public void handleApplicantCounselRemoved(final ApplicantCounselRemoved applicantCounselRemoved) {
        this.momento.getApplicantCounsels().remove(applicantCounselRemoved.getId());
    }

    public void handleApplicantCounselUpdated(final ApplicantCounselUpdated applicantCounselUpdated) {
        final ApplicantCounsel applicantCounsel = applicantCounselUpdated.getApplicantCounsel();
        this.momento.getApplicantCounsels().put(applicantCounsel.getId(), applicantCounsel);
    }

    public Stream<Object> addApplicantCounsel(final ApplicantCounsel applicantCounsel, final UUID hearingId) {
        if (this.momento.getApplicantCounsels().containsKey(applicantCounsel.getId())) {
            return Stream.of(new ApplicantCounselChangeIgnored(String.format("Provided applicantCounsel already exists, payload [%s]", applicantCounsel.toString())));
        }
        return Stream.of(new ApplicantCounselAdded(applicantCounsel, hearingId));
    }

    public Stream<Object> removeApplicantCounsel(final UUID id, final UUID hearingId) {
        return Stream.of(new ApplicantCounselRemoved(id, hearingId));
    }

    public Stream<Object> updateApplicantCounsel(final ApplicantCounsel applicantCounsel, final UUID hearingId) {

        final Map<UUID, ApplicantCounsel> ApplicantCounsels = this.momento.getApplicantCounsels();
        if (!(ApplicantCounsels.containsKey(applicantCounsel.getId()))) {
            return Stream.of(new ApplicantCounselChangeIgnored(String.format("Provided applicantCounsel does not exists, payload [%s]", applicantCounsel.toString())));
        } else if (ApplicantCounsels.get(applicantCounsel.getId()).equals(applicantCounsel)) {
            return Stream.of(new ApplicantCounselChangeIgnored(String.format("No change in provided applicantCounsel, payload [%s]", applicantCounsel.toString())));
        }
        return Stream.of(new ApplicantCounselUpdated(applicantCounsel, hearingId));
    }
}
