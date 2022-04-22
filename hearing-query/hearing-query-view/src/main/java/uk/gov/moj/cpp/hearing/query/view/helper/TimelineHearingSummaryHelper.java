package uk.gov.moj.cpp.hearing.query.view.helper;

import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourtDefendants;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Organisation;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Person;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary.TimelineHearingSummaryBuilder;

public class TimelineHearingSummaryHelper {

    private static final String HEARING_OUTCOME_EFFECTIVE = "Effective";
    private static final String HEARING_OUTCOME_VACATED = "Vacated";

    @Inject
    private CourtApplicationsSerializer courtApplicationsSerializer;

    private TimelineHearingSummaryBuilder createTimelineHearingSummaryBuilder(final HearingDay hearingDay, final Hearing hearing, final CrackedIneffectiveTrial crackedIneffectiveTrial, final JsonObject allCourtRooms, final List<HearingYouthCourtDefendants> hearingYouthCourtDefendants) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = new TimelineHearingSummaryBuilder();
        timelineHearingSummaryBuilder.withHearingId(hearing.getId());
        timelineHearingSummaryBuilder.withHearingDate(hearingDay.getDate());
        timelineHearingSummaryBuilder.withHearingType(null == hearing.getHearingType() ? null : hearing.getHearingType().getDescription());
        timelineHearingSummaryBuilder.withCourtHouse(getCourtCentreName(hearingDay, hearing, allCourtRooms));
        timelineHearingSummaryBuilder.withCourtRoom(getCourtRoomName(hearingDay, hearing, allCourtRooms));
        timelineHearingSummaryBuilder.withHearingTime(hearingDay.getDateTime());
        timelineHearingSummaryBuilder.withStartTime(hearingDay.getSittingDay());
        timelineHearingSummaryBuilder.withEstimatedDuration(hearingDay.getListedDurationMinutes());
        if (nonNull(hearingYouthCourtDefendants)){
            timelineHearingSummaryBuilder.withYouthDefendantIds(hearingYouthCourtDefendants.stream().map(e-> e.getId().getDefendantId().toString()).collect(toList()));
        }

        if (nonNull(hearing.getYouthCourt())) {
            final YouthCourt.Builder youthCourtBuilder = new YouthCourt.Builder();
            youthCourtBuilder.withYouthCourtId(hearing.getYouthCourt().getId());
            youthCourtBuilder.withName(hearing.getYouthCourt().getName());
            youthCourtBuilder.withWelshName(hearing.getYouthCourt().getWelshName());
            youthCourtBuilder.withCourtCode(hearing.getYouthCourt().getCourtCode());
            timelineHearingSummaryBuilder.withYouthCourt(youthCourtBuilder.build());
        }



        final List<String> defendantNames = getDefendantNames(hearing);

        if (!defendantNames.isEmpty()) {
            timelineHearingSummaryBuilder.withDefendants(defendantNames);
        }

        setHearingOutcome(hearing, crackedIneffectiveTrial, timelineHearingSummaryBuilder);
        if(Boolean.TRUE.equals(hearing.getIsBoxHearing()) ) {
            timelineHearingSummaryBuilder.withIsBoxHearing(hearing.getIsBoxHearing());
        }

        return timelineHearingSummaryBuilder;
    }

    private void setHearingOutcome(final Hearing hearing, final CrackedIneffectiveTrial crackedIneffectiveTrial, final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder) {
        if (nonNull(crackedIneffectiveTrial)) {
            timelineHearingSummaryBuilder.withOutcome(crackedIneffectiveTrial.getType());
        } else if (nonNull(hearing.getIsEffectiveTrial()) && hearing.getIsEffectiveTrial()) {
            timelineHearingSummaryBuilder.withOutcome(HEARING_OUTCOME_EFFECTIVE);
        } else if (nonNull(hearing.getIsVacatedTrial()) && hearing.getIsVacatedTrial()) {
            timelineHearingSummaryBuilder.withOutcome(HEARING_OUTCOME_VACATED);
        }
    }

    public TimelineHearingSummary createTimeLineHearingSummary(final HearingDay hearingDay,
                                                               final Hearing hearing,
                                                               final CrackedIneffectiveTrial crackedIneffectiveTrial,
                                                               final JsonObject allCourtRooms,
                                                               final List<HearingYouthCourtDefendants> hearingYouthCourtDefendants) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = createTimelineHearingSummaryBuilder(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, hearingYouthCourtDefendants);
        return timelineHearingSummaryBuilder.build();
    }

    public TimelineHearingSummary createTimeLineHearingSummary(final HearingDay hearingDay,
                                                               final Hearing hearing,
                                                               final CrackedIneffectiveTrial crackedIneffectiveTrial,
                                                               final JsonObject allCourtRooms,
                                                               final List<HearingYouthCourtDefendants> hearingYouthCourtDefendants,
                                                               final UUID applicationId) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = createTimelineHearingSummaryBuilder(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms,hearingYouthCourtDefendants);
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
        if (nonNull(applicant.getOrganisation())) {
            displayName = ofNullable(applicant.getOrganisation().getName()).filter(StringUtils::isNotBlank);
        }
        if (nonNull(applicant.getMasterDefendant())) {
            final MasterDefendant masterDefendant = applicant.getMasterDefendant();
            if (nonNull(masterDefendant.getPersonDefendant())) {
                final uk.gov.justice.core.courts.PersonDefendant personDefendant = masterDefendant.getPersonDefendant();
                displayName = ofNullable(Stream.of(personDefendant.getPersonDetails().getFirstName(), personDefendant.getPersonDetails().getLastName())
                        .filter(StringUtils::isNotBlank).collect(Collectors.joining(" ")));
            }
            if (nonNull(masterDefendant.getLegalEntityDefendant())) {
                displayName = ofNullable(masterDefendant.getLegalEntityDefendant().getOrganisation().getName()).filter(StringUtils::isNotBlank);
            }
        }

        if (!displayName.isPresent() && nonNull(applicant.getProsecutingAuthority())) {
            displayName = ofNullable(applicant.getProsecutingAuthority().getProsecutionAuthorityCode()).filter(StringUtils::isNotBlank);
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

    private String getCourtCentreName(final HearingDay hearingDay, final Hearing hearing, final JsonObject allCourtRooms){
        // First check hearingDay object
        // Otherwise use courtCentre on top level of hearing

        if (hearingDay.getCourtCentreId() != null) {
            return searchCourtCentreName(allCourtRooms, hearingDay.getCourtCentreId());
        } else if (hearing.getCourtCentre() != null){
            return hearing.getCourtCentre().getName();
        } else {
            return null;
        }
    }

    private String getCourtRoomName(final HearingDay hearingDay, final Hearing hearing, final JsonObject allCourtRooms){
        // First check hearingDay object
        // Otherwise use courtCentre on top level of hearing

        if (hearingDay.getCourtCentreId() != null && hearingDay.getCourtRoomId() != null) {
            return searchCourtRoomName(allCourtRooms, hearingDay.getCourtCentreId(), hearingDay.getCourtRoomId());
        } else if (hearing.getCourtCentre() != null){
            return hearing.getCourtCentre().getRoomName();
        } else {
            return null;
        }
    }

    private String searchCourtCentreName(JsonObject allCourtRooms, UUID courtCentreId){
        if (allCourtRooms == null || courtCentreId == null) {
            return null;
        }

        return allCourtRooms.getJsonArray("organisationunits")
                .stream()
                .filter(x -> x.getValueType() == JsonValue.ValueType.OBJECT)
                .map(x -> (JsonObject) x)
                .filter(x -> x.getString("id").equals(courtCentreId.toString()))
                .map(x -> x.getString("oucodeL3Name"))
                .findFirst().orElse(null);
    }

    private String searchCourtRoomName(JsonObject allCourtRooms, UUID courtCentreId, UUID courtRoomId){
        if (allCourtRooms == null || courtCentreId == null || courtRoomId == null) {
            return null;
        }

        return allCourtRooms.getJsonArray("organisationunits")
                .stream()
                .filter(x -> x.getValueType() == JsonValue.ValueType.OBJECT)
                .map(x -> (JsonObject) x)
                .filter(x -> x.getString("id").equals(courtCentreId.toString()))
                .filter(x -> x.containsKey("courtrooms"))
                .flatMap(x -> x.getJsonArray("courtrooms").stream())
                .map(x -> (JsonObject) x)
                .filter(x -> x.getString("id").equals(courtRoomId.toString()))
                .map(x -> x.getString("courtroomName"))
                .findFirst().orElse(null);
    }
}
