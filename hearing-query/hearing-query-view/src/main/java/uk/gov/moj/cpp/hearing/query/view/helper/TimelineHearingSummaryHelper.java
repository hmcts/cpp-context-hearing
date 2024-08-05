package uk.gov.moj.cpp.hearing.query.view.helper;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary.TimelineHearingSummaryBuilder;

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
import uk.gov.moj.cpp.hearing.persist.entity.ha.PersonDefendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.Application;
import uk.gov.moj.cpp.hearing.query.view.response.Person;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.json.JsonValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TimelineHearingSummaryHelper {

    private static final String HEARING_OUTCOME_EFFECTIVE = "Effective";
    private static final String HEARING_OUTCOME_VACATED = "Vacated";

    @Inject
    private CourtApplicationsSerializer courtApplicationsSerializer;

    private TimelineHearingSummaryBuilder createTimelineHearingSummaryBuilder(final HearingDay hearingDay, final Hearing hearing, final CrackedIneffectiveTrial crackedIneffectiveTrial, final JsonObject allCourtRooms, final List<HearingYouthCourtDefendants> hearingYouthCourtDefendants, final UUID caseId) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = new TimelineHearingSummaryBuilder();
        timelineHearingSummaryBuilder.withHearingId(hearing.getId());
        timelineHearingSummaryBuilder.withHearingDate(hearingDay.getDate());
        timelineHearingSummaryBuilder.withHearingType(null == hearing.getHearingType() ? null : hearing.getHearingType().getDescription());
        timelineHearingSummaryBuilder.withCourtHouse(getCourtCentreName(hearingDay, hearing, allCourtRooms));
        timelineHearingSummaryBuilder.withCourtRoom(getCourtRoomName(hearingDay, hearing, allCourtRooms));
        timelineHearingSummaryBuilder.withHearingTime(hearingDay.getDateTime());
        timelineHearingSummaryBuilder.withStartTime(hearingDay.getSittingDay());
        timelineHearingSummaryBuilder.withEstimatedDuration(hearingDay.getListedDurationMinutes());
        if (nonNull(hearingYouthCourtDefendants)) {
            timelineHearingSummaryBuilder.withYouthDefendantIds(hearingYouthCourtDefendants.stream().map(e -> e.getId().getDefendantId().toString()).collect(toList()));
        }

        if (nonNull(hearing.getYouthCourt())) {
            final YouthCourt.Builder youthCourtBuilder = new YouthCourt.Builder();
            youthCourtBuilder.withYouthCourtId(hearing.getYouthCourt().getId());
            youthCourtBuilder.withName(hearing.getYouthCourt().getName());
            youthCourtBuilder.withWelshName(hearing.getYouthCourt().getWelshName());
            youthCourtBuilder.withCourtCode(hearing.getYouthCourt().getCourtCode());
            timelineHearingSummaryBuilder.withYouthCourt(youthCourtBuilder.build());
        }

        final List<uk.gov.moj.cpp.hearing.query.view.response.Defendant> defendants = getDefendants(hearing, caseId);

        if (!defendants.isEmpty()) {
            timelineHearingSummaryBuilder.withDefendants(defendants);
        }

        setHearingOutcome(hearing, crackedIneffectiveTrial, timelineHearingSummaryBuilder);
        if (TRUE.equals(hearing.getIsBoxHearing())) {
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
                                                               final List<HearingYouthCourtDefendants> hearingYouthCourtDefendants,
                                                               final UUID caseId) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = createTimelineHearingSummaryBuilder(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms, hearingYouthCourtDefendants, caseId);
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(hearing.getCourtApplicationsJson());
        if (isNotEmpty(courtApplications)) {
            final List<Application> applications = new ArrayList<>();
            courtApplications.stream().forEach(courtApplication -> {
                final UUID applicationId = courtApplication.getId();
                final Application.ApplicationBuilder applicationBuilder = new Application.ApplicationBuilder();
                applicationBuilder.withApplicationId(applicationId);
                final List<Person> applicantList = getApplicants(applicationId, courtApplications);
                ofNullable(applicantList).ifPresent(applicationBuilder::withApplicants);
                final List<Person> respondentList = getRespondents(applicationId, courtApplications);
                ofNullable(respondentList).ifPresent(applicationBuilder::withRespondents);
                final List<Person> subjectList = getSubject(applicationId, courtApplications);
                ofNullable(subjectList).ifPresent(applicationBuilder::withSubjects);
                applications.add(applicationBuilder.build());
            });
            timelineHearingSummaryBuilder.withApplications(applications);
        }
        return timelineHearingSummaryBuilder.build();
    }

    public TimelineHearingSummary createTimeLineHearingSummary(final HearingDay hearingDay,
                                                               final Hearing hearing,
                                                               final CrackedIneffectiveTrial crackedIneffectiveTrial,
                                                               final JsonObject allCourtRooms,
                                                               final List<HearingYouthCourtDefendants> hearingYouthCourtDefendants,
                                                               final UUID applicationId,
                                                               final UUID caseId) {
        final TimelineHearingSummaryBuilder timelineHearingSummaryBuilder = createTimelineHearingSummaryBuilder(hearingDay, hearing, crackedIneffectiveTrial, allCourtRooms,hearingYouthCourtDefendants, caseId);
        final List<String> applicantNames = getApplicantNames(hearing.getCourtApplicationsJson(), applicationId);
        if (!applicantNames.isEmpty()) {
            timelineHearingSummaryBuilder.withApplicants(applicantNames);
        }
        return timelineHearingSummaryBuilder.build();
    }

    private List<uk.gov.moj.cpp.hearing.query.view.response.Defendant> getDefendants(final Hearing hearing, final UUID caseId) {
        if (nonNull(caseId) && isGroupCivilCaseHearing(hearing)) {
            return hearing.getProsecutionCases()
                    .stream()
                    .filter(pCase -> nonNull(pCase.getId()) && pCase.getId().getId().equals(caseId))
                    .map(ProsecutionCase::getDefendants)
                    .flatMap(Collection::stream)
                    .map(this::getDefendant)
                    .collect(toList());
        } else {
            return hearing.getProsecutionCases()
                    .stream()
                    .map(ProsecutionCase::getDefendants)
                    .flatMap(Collection::stream)
                    .map(this::getDefendant)
                    .collect(toList());
        }
    }

    private boolean isGroupCivilCaseHearing(final Hearing hearing) {
        return nonNull(hearing.getIsGroupProceedings()) && TRUE.equals(hearing.getIsGroupProceedings()) && hearing.getNumberOfGroupCases() > 1;
    }

    private Optional<String> getApplicantName(final CourtApplicationParty applicant) {
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

        return displayName;
    }

    private uk.gov.moj.cpp.hearing.query.view.response.Person buildApplicant(final CourtApplicationParty applicant) {
        final Optional<String> displayName = getApplicantName(applicant);
        return new uk.gov.moj.cpp.hearing.query.view.response.Person(applicant.getId(), displayName.orElse(EMPTY), getMasterDefendantId(applicant.getMasterDefendant()));

    }

    private UUID getMasterDefendantId(final MasterDefendant masterDefendant) {
        return nonNull(masterDefendant) ? masterDefendant.getMasterDefendantId() : null;
    }

    private List<Person> buildRespondent(final List<CourtApplicationParty> respondents) {
        return respondents.stream().map(respondent -> Person.builder().withId(respondent.getId())
                .withMasterDefendantId(getMasterDefendantId(respondent.getMasterDefendant()))
                .build()).collect(toList());

    }

    private List<Person> getRespondents(final UUID applicationId, final List<CourtApplication> courtApplications) {
        return courtApplications.stream()
                .filter(courtApplication -> courtApplication.getId().equals(applicationId))
                .filter(courtApplication -> isNotEmpty(courtApplication.getRespondents()))
                .map(courtApplication -> buildRespondent(courtApplication.getRespondents()))
                .flatMap(Collection::stream)
                .collect(toList());
    }


    private List<Person> getApplicants(final UUID applicationId, final List<CourtApplication> courtApplications) {
        return courtApplications
                .stream()
                .filter(courtApplication -> courtApplication.getId().equals(applicationId))
                .filter(courtApplication -> nonNull(courtApplication.getApplicant()))
                .map(courtApplication -> buildApplicant(courtApplication.getApplicant()))
                .collect(Collectors.toList());
    }

    private List<String> getApplicantNames(final String courtApplicationJson, UUID applicationId) {
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationJson);

        return courtApplications
            .stream()
            .filter(courtApplication -> courtApplication.getId().equals(applicationId))
            .map(courtApplication ->
                getApplicantName(courtApplication.getApplicant()).orElse(EMPTY))
            .collect(Collectors.toList());
    }

    private List<Person> getSubject(final UUID applicationId, final List<CourtApplication> courtApplications) {
        return courtApplications
                .stream()
                .filter(courtApplication -> courtApplication.getId().equals(applicationId))
                .filter(courtApplication -> nonNull(courtApplication.getSubject()))
                .map(courtApplication -> Person.builder()
                        .withId(courtApplication.getSubject().getId())
                        .withMasterDefendantId(getMasterDefendantId(courtApplication.getSubject().getMasterDefendant())).build())
                .collect(Collectors.toList());
    }

    private uk.gov.moj.cpp.hearing.query.view.response.Defendant getDefendant(final Defendant defendant) {

        String name;
        if (nonNull(defendant.getPersonDefendant())) {
            name = of(defendant)
                    .map(Defendant::getPersonDefendant)
                    .map(PersonDefendant::getPersonDetails)
                    .map(this::getName).orElse(null);
        } else {
            name = of(defendant)
                    .map(Defendant::getLegalEntityOrganisation)
                    .map(Organisation::getName).orElse(null);
        }

        return new uk.gov.moj.cpp.hearing.query.view.response.Defendant(defendant.getId().getId(), name);

    }

    private String getName(final uk.gov.moj.cpp.hearing.persist.entity.ha.Person person) {
        return Stream.of(person.getFirstName(), person.getLastName())
                .filter(s -> s != null && !s.isEmpty())
                .collect(joining(" "));
    }

    private String getCourtCentreName(final HearingDay hearingDay, final Hearing hearing, final JsonObject allCourtRooms) {
        // First check hearingDay object
        // Otherwise use courtCentre on top level of hearing

        if (hearingDay.getCourtCentreId() != null) {
            return searchCourtCentreName(allCourtRooms, hearingDay.getCourtCentreId());
        } else if (hearing.getCourtCentre() != null) {
            return hearing.getCourtCentre().getName();
        } else {
            return null;
        }
    }

    private String getCourtRoomName(final HearingDay hearingDay, final Hearing hearing, final JsonObject allCourtRooms) {
        // First check hearingDay object
        // Otherwise use courtCentre on top level of hearing

        if (hearingDay.getCourtCentreId() != null && hearingDay.getCourtRoomId() != null) {
            return searchCourtRoomName(allCourtRooms, hearingDay.getCourtCentreId(), hearingDay.getCourtRoomId());
        } else if (hearing.getCourtCentre() != null) {
            return hearing.getCourtCentre().getRoomName();
        } else {
            return null;
        }
    }

    private String searchCourtCentreName(JsonObject allCourtRooms, UUID courtCentreId) {
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

    private String searchCourtRoomName(JsonObject allCourtRooms, UUID courtCentreId, UUID courtRoomId) {
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
