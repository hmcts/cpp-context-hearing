package uk.gov.moj.cpp.hearing.it;

import org.hamcrest.Matcher;
import org.junit.Test;
import uk.gov.justice.core.courts.*;
import uk.gov.justice.hearing.courts.*;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import javax.annotation.concurrent.NotThreadSafe;
import java.time.ZoneId;
import java.util.*;

import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.*;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.*;

@NotThreadSafe
public class RecorderIT extends AbstractIT {

    @Test
    public void initiateHearingWithMultipleCasesForRecorder() {
        final UUID case1 = randomUUID();
        final UUID case2 = randomUUID();
        final UUID case3 = randomUUID();

        stubUsersAndGroupsGetLoggedInPermissionsWithCasesForRecorder(case1, case2, case3, getLoggedInUser());
        stubUsersAndGroupsUserRolesForRecorder(getLoggedInUser());

        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        final Map<UUID, List<UUID>> value = new HashMap<>();
        value.put(randomUUID(), asList(randomUUID(), randomUUID()));
        value.put(randomUUID(), asList(randomUUID()));

        caseStructure.put(case1, value);
        caseStructure.put(case2, toMap(randomUUID(), asList(randomUUID(), randomUUID())));
        caseStructure.put(case3, toMap(randomUUID(), asList(randomUUID())));

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(),
                initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(
                                defaultArguments().setStructure(caseStructure)
                                        .setDefendantType(PERSON)
                                        .setHearingLanguage(ENGLISH)
                                        .setJurisdictionType(CROWN)
                        ).build())));

        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId())))
                        .with(Hearing::getJurisdictionType, is(hearing.getJurisdictionType()))
                        .with(Hearing::getHearingLanguage, is(ENGLISH))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                .with(CourtCentre::getName, is(hearing.getCourtCentre().getName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .withValue(jr -> judicialRole.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, courtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, courtApplication.getApplicationReference())
                        ))
                        .with(Hearing::getProsecutionCases, hasSize(3))
                        .with(Hearing::getProsecutionCases, MatcherUtil.getProsecutionCasesMatchers(hearingOne.getHearing().getProsecutionCases()))
                )
        );
        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), hearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                .withValue(HearingSummaries::getReportingRestrictionReason, hearing.getReportingRestrictionReason())
                                .withValue(HearingSummaries::getHearingLanguage, ENGLISH.name())
                                .with(HearingSummaries::getCourtCentre, isBean(CourtCentre.class)
                                        .withValue(CourtCentre::getId, hearing.getCourtCentre().getId())
                                        .withValue(CourtCentre::getName, hearing.getCourtCentre().getName()))
                                .with(HearingSummaries::getType, isBean(HearingType.class)
                                        .withValue(HearingType::getId, hearing.getType().getId())
                                        .withValue(HearingType::getDescription, hearing.getType().getDescription()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .withValue(HearingDay::getSittingDay, hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC")))
                                        .withValue(HearingDay::getListedDurationMinutes, hearingDay.getListedDurationMinutes())
                                        .withValue(HearingDay::getListingSequence, hearingDay.getListingSequence())))
                                .with(HearingSummaries::getProsecutionCaseSummaries, hasProsecutionSummaries(hearing.getProsecutionCases()))
                                .with(HearingSummaries::getProsecutionCaseSummaries, hasSize(3))
                                .with(HearingSummaries::getCourtApplicationSummaries, first(isBean(CourtApplicationSummaries.class)
                                        .withValue(CourtApplicationSummaries::getId, courtApplication.getId())
                                ))
                        ))
        );


    }

    @Test
    public void initiateHearingWithFilteredCasesForRecorder() {
        final UUID case1 = randomUUID();
        final UUID case2 = randomUUID();
        final UUID case3 = randomUUID();

        stubUsersAndGroupsGetLoggedInPermissionsWithCasesForRecorder(case1, case2, case3, getLoggedInUser());
        stubUsersAndGroupsUserRolesForRecorder(getLoggedInUser());

        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        final Map<UUID, List<UUID>> value = new HashMap<>();
        value.put(randomUUID(), asList(randomUUID(), randomUUID()));
        value.put(randomUUID(), asList(randomUUID()));

        caseStructure.put(case1, value);
        caseStructure.put(case2, toMap(randomUUID(), asList(randomUUID(), randomUUID())));
        caseStructure.put(case3, toMap(randomUUID(), asList(randomUUID())));

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(),
                initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(
                                defaultArguments().setStructure(caseStructure)
                                        .setDefendantType(PERSON)
                                        .setHearingLanguage(ENGLISH)
                                        .setJurisdictionType(CROWN)
                        ).build())));

        final Hearing hearing = hearingOne.getHearing();
        final List<ProsecutionCase> prosecutionCases = new ArrayList<>();
        final List<ProsecutionCase> prosecutionCasesIdentified = hearingOne.getHearing().getProsecutionCases();
        for (ProsecutionCase prosecutionCase : prosecutionCasesIdentified) {
            if (prosecutionCase.getId().equals(case1)) {
                prosecutionCases.add(prosecutionCase);
            }
        }

        final Hearing hearingTwo = hearing;
        hearingTwo.setProsecutionCases(prosecutionCases);

        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId())))
                        .with(Hearing::getJurisdictionType, is(hearing.getJurisdictionType()))
                        .with(Hearing::getHearingLanguage, is(ENGLISH))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                .with(CourtCentre::getName, is(hearing.getCourtCentre().getName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .withValue(jr -> judicialRole.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, courtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, courtApplication.getApplicationReference())
                        ))
                        .with(Hearing::getProsecutionCases, MatcherUtil.getProsecutionCasesMatchers(hearingTwo.getProsecutionCases()))
                )
        );
        stubUsersAndGroupsGetLoggedInPermissionsWithFilteredCasesForRecorder(case1, getLoggedInUser());

        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), hearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                .withValue(HearingSummaries::getReportingRestrictionReason, hearing.getReportingRestrictionReason())
                                .withValue(HearingSummaries::getHearingLanguage, ENGLISH.name())
                                .with(HearingSummaries::getCourtCentre, isBean(CourtCentre.class)
                                        .withValue(CourtCentre::getId, hearing.getCourtCentre().getId())
                                        .withValue(CourtCentre::getName, hearing.getCourtCentre().getName()))
                                .with(HearingSummaries::getType, isBean(HearingType.class)
                                        .withValue(HearingType::getId, hearing.getType().getId())
                                        .withValue(HearingType::getDescription, hearing.getType().getDescription()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .withValue(HearingDay::getSittingDay, hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC")))
                                        .withValue(HearingDay::getListedDurationMinutes, hearingDay.getListedDurationMinutes())
                                        .withValue(HearingDay::getListingSequence, hearingDay.getListingSequence())))
                                .with(HearingSummaries::getProsecutionCaseSummaries, hasProsecutionSummaries(hearingTwo.getProsecutionCases()))
                                .with(HearingSummaries::getProsecutionCaseSummaries, hasSize(1))
                                .with(HearingSummaries::getCourtApplicationSummaries, first(isBean(CourtApplicationSummaries.class)
                                        .withValue(CourtApplicationSummaries::getId, courtApplication.getId())
                                ))
                        ))
        );

    }

    @Test
    public void initiateHearingWithNoHearingCasesForRecorder() {

        final UUID case1 = randomUUID();
        final UUID case2 = randomUUID();
        final UUID case3 = randomUUID();

        stubUsersAndGroupsGetLoggedInPermissionsWithCasesForRecorder(case1, case2, case3, getLoggedInUser());


        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        final Map<UUID, List<UUID>> value = new HashMap<>();
        value.put(randomUUID(), asList(randomUUID(), randomUUID()));
        value.put(randomUUID(), asList(randomUUID()));
        caseStructure.put(case1, value);
        caseStructure.put(case2, toMap(randomUUID(), asList(randomUUID(), randomUUID())));
        caseStructure.put(case3, toMap(randomUUID(), asList(randomUUID())));


        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(),
                initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(
                                defaultArguments().setStructure(caseStructure)
                                        .setDefendantType(PERSON)
                                        .setHearingLanguage(ENGLISH)
                                        .setJurisdictionType(CROWN)
                        ).build())));

        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId())))
                        .with(Hearing::getJurisdictionType, is(hearing.getJurisdictionType()))
                        .with(Hearing::getHearingLanguage, is(ENGLISH))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                .with(CourtCentre::getName, is(hearing.getCourtCentre().getName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .withValue(jr -> judicialRole.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, courtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, courtApplication.getApplicationReference())
                        ))
                        .with(Hearing::getProsecutionCases, hasSize(3))
                        .with(Hearing::getProsecutionCases, MatcherUtil.getProsecutionCasesMatchers(hearingOne.getHearing().getProsecutionCases()))
                )
        );

        stubUsersAndGroupsGetLoggedInPermissionsWithoutCasesForRecorder();

        Queries.getHearingsByDatePollForMatch(
                hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), hearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class).with(GetHearings::getHearingSummaries, is(nullValue()))
        );
    }

    @Test
    public void initiateHearingWithMultipleCasesForNonRecorder() {
        final UUID case1 = randomUUID();
        final UUID case2 = randomUUID();
        final UUID case3 = randomUUID();

        stubUsersAndGroupsGetLoggedInPermissionsWithCasesForRecorder(case1, case2, case3, getLoggedInUser());
        stubUsersAndGroupsUserRoles(getLoggedInUser());

        final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        final Map<UUID, List<UUID>> value = new HashMap<>();
        value.put(randomUUID(), asList(randomUUID(), randomUUID()));
        value.put(randomUUID(), asList(randomUUID()));
        caseStructure.put(case1, value);
        caseStructure.put(case2, toMap(randomUUID(), asList(randomUUID(), randomUUID())));
        caseStructure.put(case3, toMap(randomUUID(), asList(randomUUID())));

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(),
                initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(
                                defaultArguments().setStructure(caseStructure)
                                        .setDefendantType(PERSON)
                                        .setHearingLanguage(ENGLISH)
                                        .setJurisdictionType(CROWN)
                        ).build())));

        final Hearing hearing = hearingOne.getHearing();
        final CourtApplication courtApplication = hearing.getCourtApplications().get(0);

        final HearingDay hearingDay = hearing.getHearingDays().get(0);

        final JudicialRole judicialRole = hearing.getJudiciary().get(0);

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getType, isBean(HearingType.class)
                                .with(HearingType::getId, is(hearing.getType().getId())))
                        .with(Hearing::getJurisdictionType, is(JurisdictionType.CROWN))
                        .with(Hearing::getHearingLanguage, is(ENGLISH))
                        .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))
                                .with(CourtCentre::getName, is(hearing.getCourtCentre().getName())))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getSittingDay, is(hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC"))))
                                .with(HearingDay::getListingSequence, is(hearingDay.getListingSequence()))
                                .with(HearingDay::getListedDurationMinutes, is(hearingDay.getListedDurationMinutes()))))
                        .with(Hearing::getJudiciary, first(isBean(JudicialRole.class)
                                .with(JudicialRole::getJudicialId, is(judicialRole.getJudicialId()))
                                .withValue(jr -> judicialRole.getJudicialRoleType().getJudiciaryType(), judicialRole.getJudicialRoleType().getJudiciaryType())))
                        .with(Hearing::getCourtApplications, first(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, courtApplication.getId())
                                .withValue(CourtApplication::getApplicationReference, courtApplication.getApplicationReference())
                        ))
                        .with(Hearing::getProsecutionCases, MatcherUtil.getProsecutionCasesMatchers(hearingOne.getHearing().getProsecutionCases()))
                )
        );
        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), hearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, hasItem(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                .withValue(HearingSummaries::getReportingRestrictionReason, hearing.getReportingRestrictionReason())
                                .withValue(HearingSummaries::getHearingLanguage, ENGLISH.name())
                                .with(HearingSummaries::getCourtCentre, isBean(CourtCentre.class)
                                        .withValue(CourtCentre::getId, hearing.getCourtCentre().getId())
                                        .withValue(CourtCentre::getName, hearing.getCourtCentre().getName()))
                                .with(HearingSummaries::getType, isBean(HearingType.class)
                                        .withValue(HearingType::getId, hearing.getType().getId())
                                        .withValue(HearingType::getDescription, hearing.getType().getDescription()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .withValue(HearingDay::getSittingDay, hearingDay.getSittingDay().withZoneSameLocal(ZoneId.of("UTC")))
                                        .withValue(HearingDay::getListedDurationMinutes, hearingDay.getListedDurationMinutes())
                                        .withValue(HearingDay::getListingSequence, hearingDay.getListingSequence())))
                                .with(HearingSummaries::getProsecutionCaseSummaries, hasProsecutionSummaries(hearing.getProsecutionCases()))
                                .with(HearingSummaries::getProsecutionCaseSummaries, hasSize(3))
                                .with(HearingSummaries::getCourtApplicationSummaries, first(isBean(CourtApplicationSummaries.class)
                                        .withValue(CourtApplicationSummaries::getId, courtApplication.getId())
                                ))
                        ))
        );


    }

    public Matcher<Iterable<ProsecutionCaseSummaries>> hasProsecutionSummaries(final List<ProsecutionCase> prosecutionCases) {
        return hasItems(
                prosecutionCases.stream().map(
                        prosecutionCase -> hasProsecutionCaseSummary(prosecutionCase)
                ).toArray(BeanMatcher[]::new)
        );

    }

    public BeanMatcher<ProsecutionCaseSummaries> hasProsecutionCaseSummary(final ProsecutionCase prosecutionCase) {
        return isBean(ProsecutionCaseSummaries.class)
                .withValue(ProsecutionCaseSummaries::getId, prosecutionCase.getId())
                .with(ProsecutionCaseSummaries::getProsecutionCaseIdentifier,
                        isBean(ProsecutionCaseIdentifier.class)
                                .withValue(ProsecutionCaseIdentifier::getCaseURN, prosecutionCase.getProsecutionCaseIdentifier().getCaseURN())
                                .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityCode, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityCode())
                                .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityId, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId())
                                .withValue(ProsecutionCaseIdentifier::getProsecutionAuthorityReference, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityReference()))
                .with(ProsecutionCaseSummaries::getDefendants,
                        hasDefendantSummaries(prosecutionCase)
                );
    }

    public Matcher<Iterable<Defendants>> hasDefendantSummaries(final ProsecutionCase prosecutionCase) {
        return hasItems(prosecutionCase.getDefendants().stream().map(defendant ->
                isBean(Defendants.class)
                        .withValue(Defendants::getId, defendant.getId())
                        .withValue(Defendants::getFirstName, defendant.getPersonDefendant().getPersonDetails().getFirstName()))
                .toArray(BeanMatcher[]::new));
    }
}
