package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationDefendantsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.CourtApplicationEjected;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeletedForCourtApplication;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

@SuppressWarnings({"squid:S1068", "squid:S1948"})
public class ApplicationAggregate implements Aggregate {

    private static final long serialVersionUID = 101L;

    private List<UUID> hearingIds = new ArrayList<>();

    @SuppressWarnings("squid:S2250")
    @Override
    public Object apply(final Object event) {
        return match(event).with(
                when(RegisteredHearingAgainstApplication.class).apply(e -> hearingIds.add(e.getHearingId())),
                when(HearingDeletedForCourtApplication.class).apply(e -> hearingIds.remove(e.getHearingId())),
                otherwiseDoNothing());
    }

    public Stream<Object> registerHearingId(final UUID applicationId, final UUID hearingId) {
        return apply(Stream.of(
                RegisteredHearingAgainstApplication.builder()
                        .withApplicationId(applicationId)
                        .withHearingId(hearingId)
                        .build()));
    }

    public Stream<Object> ejectApplication(final UUID applicationId, final List<UUID> hearingIds) {
        if(hearingIds.isEmpty() && !this.hearingIds.isEmpty()) {
            return apply(Stream.of(CourtApplicationEjected.aCourtApplicationEjected().
                    withApplicationId(applicationId)
                    .withHearingIds(this.hearingIds)
                    .build()));
        } else if(hearingIds.isEmpty() && this.hearingIds.isEmpty()) {
            return apply(Stream.empty());
        }
        else {
            return apply(Stream.of(CourtApplicationEjected.aCourtApplicationEjected().
                    withApplicationId(applicationId)
                    .withHearingIds(hearingIds)
                    .build()));
        }

    }

    public Stream<Object> applicationDefendantsUpdated(final CourtApplication courtApplication) {
        if (!this.hearingIds.isEmpty()) {
            return apply(Stream.of(ApplicationDefendantsUpdated.applicationDefendantsUpdatd().withHearingIds(hearingIds).withCourtApplication(courtApplication).build()));
        } else {
            return apply(Stream.empty());
        }
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public Stream<Object> deleteHearingForCourtApplication(final UUID courtApplicationId, final UUID hearingId) {
        return apply(Stream.of(new HearingDeletedForCourtApplication(courtApplicationId, hearingId)));
    }
}
