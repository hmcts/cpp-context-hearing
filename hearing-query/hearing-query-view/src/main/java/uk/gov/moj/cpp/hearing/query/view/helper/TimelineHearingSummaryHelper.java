package uk.gov.moj.cpp.hearing.query.view.helper;

import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary.TimelineHearingSummaryBuilder;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

public class TimelineHearingSummaryHelper {

    @Inject
    private CourtApplicationsSerializer courtApplicationsSerializer;

    private TimelineHearingSummaryBuilder createTimelineHearingSummaryBuilder(final HearingDay hearingDay, final Hearing hearing, final CrackedIneffectiveTrial crackedIneffectiveTrial) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = new TimelineHearingSummaryBuilder();
        timelineHearingSummaryBuilder.withHearingId(hearing.getId());
        timelineHearingSummaryBuilder.withHearingDate(hearingDay.getDate());
        timelineHearingSummaryBuilder.withHearingType(null == hearing.getHearingType() ? null : hearing.getHearingType().getDescription());
        timelineHearingSummaryBuilder.withCourtHouse(null == hearing.getCourtCentre() ? null : hearing.getCourtCentre().getName());
        timelineHearingSummaryBuilder.withCourtRoom(null == hearing.getCourtCentre() ? null : hearing.getCourtCentre().getRoomName());
        timelineHearingSummaryBuilder.withHearingTime(hearingDay.getDateTime());
        timelineHearingSummaryBuilder.withEstimatedDuration(hearingDay.getListedDurationMinutes());
        final List<String> defendantNames = getDefendantNames(hearing);
        if (!defendantNames.isEmpty()) {
            timelineHearingSummaryBuilder.withDefendants(defendantNames);
        }
        if (nonNull(crackedIneffectiveTrial)) {
            timelineHearingSummaryBuilder.withOutcome(crackedIneffectiveTrial.getType());
        }
        return timelineHearingSummaryBuilder;
    }

    public TimelineHearingSummary createTimeLineHearingSummary(final HearingDay hearingDay,
                                                               final Hearing hearing,
                                                               final CrackedIneffectiveTrial crackedIneffectiveTrial) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = createTimelineHearingSummaryBuilder(hearingDay, hearing, crackedIneffectiveTrial);
        return timelineHearingSummaryBuilder.build();
    }

    public TimelineHearingSummary createTimeLineHearingSummary(final HearingDay hearingDay,
                                                               final Hearing hearing,
                                                               final CrackedIneffectiveTrial crackedIneffectiveTrial,
                                                               final UUID applicationId) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = createTimelineHearingSummaryBuilder(hearingDay, hearing, crackedIneffectiveTrial);
        final List<String> applicantNames = getApplicantNames(hearing.getCourtApplicationsJson(), applicationId);
        if (!applicantNames.isEmpty()) {
            timelineHearingSummaryBuilder.withApplicants(applicantNames);
        }
        return timelineHearingSummaryBuilder.build();
    }

    private List<String> getDefendantNames(final Hearing hearing) {
        return hearing.getProsecutionCases()
                .stream()
                .map(ProsecutionCase::getDefendants)
                .flatMap(Collection::stream)
                .map(this::getDefendantName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private String extractDisplayName(final CourtApplicationParty applicant) {
        Optional<String> displayName = Optional.empty();

        if (Objects.nonNull(applicant.getPersonDetails())) {
            displayName = ofNullable(Stream.of(applicant.getPersonDetails().getFirstName(),
                    applicant.getPersonDetails().getLastName())
                    .filter(StringUtils::isNotBlank).collect(Collectors.joining(" ")));
        }
        return displayName.orElse(EMPTY);
    }

    private List<String> getApplicantNames(final String courtApplicationJson, UUID applicationId) {
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationJson);

        return courtApplications
                .stream()
                .filter(courtApplication -> courtApplication.getId().equals(applicationId))
                .map(courtApplication ->
                        extractDisplayName(courtApplication.getApplicant()))
                .collect(Collectors.toList());
    }

    private Optional<String> getDefendantName(final Defendant defendant) {

        if (nonNull(defendant.getPersonDefendant())) {
            return of(defendant)
                    .map(Defendant::getPersonDefendant)
                    .map(PersonDefendant::getPersonDetails)
                    .map(this::getName);
        } else {
            return of(defendant)
                    .map(Defendant::getLegalEntityOrganisation)
                    .map(Organisation::getName);
        }
    }

    private String getName(final Person person) {
        return Stream.of(person.getFirstName(), person.getLastName())
                .filter(s -> s != null && !s.isEmpty())
                .collect(joining(" "));
    }
}