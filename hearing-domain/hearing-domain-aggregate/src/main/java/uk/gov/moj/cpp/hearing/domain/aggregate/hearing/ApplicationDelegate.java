package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationRespondent;
import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.moj.cpp.hearing.domain.event.application.ApplicationResponseSaved;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings({"squid:S00107", "squid:S1068", "squid:S1172"})
public class ApplicationDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public ApplicationDelegate(HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public Stream<Object> applicationResponseSaved(final UUID applicationPartyId, final CourtApplicationResponse courtApplicationResponse) {
        return Stream.of(ApplicationResponseSaved.applicationResponseSaved()
                .setApplicationPartyId(applicationPartyId)
                .setCourtApplicationResponse(courtApplicationResponse)
        );
    }

    public void handleApplicationResponseSaved(final ApplicationResponseSaved applicationResponseSaved) {
        if (momento == null || momento.getHearing() == null) {
            return;
        }
        final List<CourtApplication> courtApplications = momento.getHearing().getCourtApplications();
        if (courtApplications == null) {
            return;
        }

        final CourtApplication courtApplication = courtApplications.stream().filter(
                ca -> ca.getId().equals(applicationResponseSaved.getCourtApplicationResponse().getApplicationId())
        ).findFirst().orElse(null);

        if (courtApplication != null) {
            courtApplication.getRespondents().stream().filter(r -> r.getPartyDetails().getId().equals(applicationResponseSaved.getApplicationPartyId())).findFirst().orElse(CourtApplicationRespondent.courtApplicationRespondent().build()).setApplicationResponse(applicationResponseSaved.getCourtApplicationResponse());
        }
    }
}
