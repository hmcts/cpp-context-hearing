package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.Defendant.defendant;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.MAGISTRATES;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand.initiateHearingCommand;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResultsPerDay;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingWith2Defendants;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandV2Template;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.InitiationCode;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantsUpdatedForHearing;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class CaseDefendantsUpdatedForHearingIT extends AbstractIT {
    private static final String PROGRESSION_HEARING_RESULTED_CASE_UPDATED = "public.progression.hearing-resulted-case-updated";
    private static final String PROGRESSION_DEFENDANT_OFFENCES_CHANGED = "public.progression.defendant-offences-changed";
    private static final String HEARING_CASE_DEFENDANTS_UPDATED_FOR_HEARING = "hearing.case-defendants-updated-for-hearing";

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    private final StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    @Test
    public void testCaseDefendantsUpdated() throws IOException {
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID caseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();
        String eventPayloadString = getStringFromResource("public.progression.hearing-resulted-case-updated.json")
                .replaceAll("CASE_ID", caseId.toString())
                .replaceAll("DEFENDANT_ID", defendantId.toString());

        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_HEARING_RESULTED_CASE_UPDATED,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), PROGRESSION_HEARING_RESULTED_CASE_UPDATED)
                        .withUserId(randomUUID().toString())
                        .build()
        );
        getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                .withValue(prosecutionCase -> prosecutionCase.getCaseStatus(), "CLOSED")
                                .with(ProsecutionCase::getDefendants, hasItem(isBean(Defendant.class)))
                                .withValue(prosecutionCase -> prosecutionCase.getDefendants().get(0).getProceedingsConcluded(), true)
                                .with(ProsecutionCase::getDefendants, hasItem(isBean(Defendant.class)
                                        .with(Defendant::getPersonDefendant, isBean(PersonDefendant.class)
                                                .withValue(personDefendant -> personDefendant.getDriverNumber(), "AACC12345")))
                                )))
                )
        );
    }

    @Test
    public void testTwoHearingsWithDifferentDefendantsSameCaseUrnWhenFirstOneIsResultedItsdefendantShouldNotBeAddedToSecondHearing() throws IOException {
        final UUID prosecutionCaseId = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final InitiateHearingCommand initiateHearingCommand1 = initiateHearingWith1DefendantAndCaseUrn(defendantId1, offenceId1, prosecutionCaseId);
        final InitiateHearingCommand initiateHearingCommand2 = initiateHearingWith1DefendantAndCaseUrn(defendantId2, offenceId2, prosecutionCaseId);

        final UUID hearingId1 = initiateHearingCommand1.getHearing().getId();
        initiateHearing(getRequestSpec(), initiateHearingCommand1);

        final UUID hearingId2 = initiateHearingCommand2.getHearing().getId();
        initiateHearing(getRequestSpec(), initiateHearingCommand2);

        String eventPayloadString = getStringFromResource("public.progression.hearing-resulted-case-updated.json")
                .replaceAll("CASE_ID", prosecutionCaseId.toString())
                .replaceAll("DEFENDANT_ID", defendantId1.toString());

        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_HEARING_RESULTED_CASE_UPDATED,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), PROGRESSION_HEARING_RESULTED_CASE_UPDATED)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        pollForHearing(hearingId1.toString(),
                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(1)),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[*].id", contains(defendantId1.toString())),
                withJsonPath("$.hearing.prosecutionCases[0].defendants.[*].offences[*].id",
                        contains(offenceId1.toString()))
        );

        pollForHearing(hearingId2.toString(),
                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(1)),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[*].id", contains(defendantId2.toString())),
                withJsonPath("$.hearing.prosecutionCases[0].defendants.[*].offences[*].id",
                        contains(offenceId2.toString()))
        );
    }

    @Test
    public void testApplicationDefendantsUpdated() throws IOException {
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate(), false, true, false, true, false, false));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getCourtApplications().get(0).getApplicant().getMasterDefendant().getMasterDefendantId();
        final UUID applicationId = hearingOne.getHearing().getCourtApplications().get(0).getId();
        String eventPayloadString = getStringFromResource("public.progression.hearing-resulted-application-updated.json")
                .replaceAll("APPLICATION_ID", applicationId.toString())
                .replaceAll("DEFENDANT_ID", defendantId.toString());

        String hearingResultedApplicationUpdatedEvent = "public.progression.hearing-resulted-application-updated";
        sendMessage(getPublicTopicInstance().createProducer(),
                hearingResultedApplicationUpdatedEvent,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), hearingResultedApplicationUpdatedEvent)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(courtApplication -> courtApplication.getId(), applicationId)
                                .with(CourtApplication::getApplicant, isBean(CourtApplicationParty.class)
                                        .with(CourtApplicationParty::getMasterDefendant, isBean(MasterDefendant.class)
                                                .with(MasterDefendant::getPersonDefendant, isBean(PersonDefendant.class)
                                                        .withValue(PersonDefendant::getDriverNumber, "ALI123"))))
                                .with(CourtApplication::getSubject, isBean(CourtApplicationParty.class)
                                        .with(CourtApplicationParty::getMasterDefendant, isBean(MasterDefendant.class)
                                                .with(MasterDefendant::getPersonDefendant, isBean(PersonDefendant.class)
                                                        .withValue(PersonDefendant::getDriverNumber, "ALI123"))))
                        ))
                )
        );
    }

    @Test
    public void shouldUpdateDefendantWhenPromptHasDrivingNumber() throws IOException {
        final LocalDate hearingDay = LocalDate.now();
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID caseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();


        stubCourtRoom(hearingOne.getHearing());

        final String eventPayloadString = getStringFromResource("hearing.share-result-with-driving_number-prompts-result.json")
                .replaceAll("HEARING_ID", hearingId.toString())
                .replaceAll("CASE_ID", caseId.toString())
                .replaceAll("DEFENDANT_ID", defendantId.toString())
                .replaceAll("ORDERED_DATE", hearingDay.toString())
                .replaceAll("OFFENCE_ID", hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString());

        final Target target = jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(eventPayloadString), Target.class);
        final List<Target> targets = new ArrayList<>();
        targets.add(target);

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingId)))))) {

            shareDaysResultWithCourtClerk(hearingOne.getHearing(), targets, hearingDay);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].personDefendant.driverNumber"), is("DVL123"));
        }
    }

    @Test
    public void shouldUpdateDefendantWhenPromptHasDrivingNumberForApplication() throws IOException {
        final LocalDate hearingDay = LocalDate.now();
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate(), true, false, false, true, false, false));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getMasterDefendantId();
        final UUID applicationId = hearingOne.getHearing().getCourtApplications().get(0).getId();


        stubCourtRoomForApplication(hearingOne.getHearing());

        final String eventPayloadString = getStringFromResource("hearing.share-application-result-with-driving_number-prompts-result.json")
                .replaceAll("HEARING_ID", hearingId.toString())
                .replaceAll("APPLICATION_ID", applicationId.toString())
                .replaceAll("DEFENDANT_ID", defendantId.toString())
                .replaceAll("ORDERED_DATE", hearingDay.toString());

        final Target target = jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(eventPayloadString), Target.class);
        final List<Target> targets = new ArrayList<>();
        targets.add(target);

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingId)))))) {

            shareDaysResultWithCourtClerkForApplication(hearingOne.getHearing(), targets, hearingDay);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.courtApplications[0].applicant.masterDefendant.personDefendant.driverNumber"), is("DVL123"));
            assertThat(publicHearingResulted.getString("hearing.courtApplications[0].subject.masterDefendant.personDefendant.driverNumber"), is("DVL123"));
        }
    }

    @Test
    public void testCaseDefendantsUpdatedForMergedCase() throws Exception {
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final InitiateHearingCommandHelper hearingOne = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing h1 = hearingOne.getHearing();
        final ProsecutionCase prosecutionCase1 = h1.getProsecutionCases().get(0);
        final UUID hearingId1 = h1.getId();
        final UUID caseId1 = prosecutionCase1.getId();
        final UUID defendantId1 = prosecutionCase1.getDefendants().get(0).getId();
        final UUID offenceId1 = prosecutionCase1.getDefendants().get(0).getOffences().get(0).getId();

        pollForHearing(hearingId1.toString(), withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId1.toString())),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", anyOf(is(offenceId1.toString()), is(offenceId1.toString()))
                ));

        final InitiateHearingCommandHelper hearingTwo = h(initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing h2 = hearingTwo.getHearing();

        final ProsecutionCase prosecutionCase2 = h2.getProsecutionCases().get(0);
        final UUID defendantId2 = prosecutionCase2.getDefendants().get(0).getId();
        final UUID offenceId2 = prosecutionCase2.getDefendants().get(0).getOffences().get(0).getId();
        pollForHearing(h2.getId().toString(), withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId2.toString())),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString()))
                ));

        final String publicProgressionHearingResultedPayload = getStringFromResource("public.progression.hearing-resulted-case-updated-merged-case.json")
                .replaceAll("CASE_ID", caseId1.toString())
                .replaceAll("DEFENDANT_ID", defendantId2.toString())
                .replaceAll("OFFENCE_ID", offenceId2.toString());

        final String updateRelatedHearingPayload = getStringFromResource("hearing.update-related-hearing.json")
                .replaceAll("CASE_ID", caseId1.toString())
                .replaceAll("DEFENDANT_ID", defendantId2.toString())
                .replaceAll("OFFENCE_ID", offenceId2.toString());


        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_HEARING_RESULTED_CASE_UPDATED,
                new StringToJsonObjectConverter().convert(publicProgressionHearingResultedPayload),
                metadataOf(randomUUID(), PROGRESSION_HEARING_RESULTED_CASE_UPDATED)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        makeCommand(getRequestSpec(), "hearing.update-hearing")
                .ofType("application/vnd.hearing.related-hearing+json")
                .withArgs(hearingId1)
                .withPayload(updateRelatedHearingPayload)
                .withCppUserId(USER_ID_VALUE_AS_ADMIN)
                .executeSuccessfully();


        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[*].id", containsInAnyOrder(defendantId1.toString(), defendantId2.toString())),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[*].offences[*].id", containsInAnyOrder(offenceId1.toString(), offenceId2.toString())),
                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(2)),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[*].offences", hasSize(2))
        );

        //Add offence 3 to defendant2 recently moved to Hearing1
        final UUID offenceId3 = randomUUID();

        final String eventPayloadForDefendantOffencesChanged = getStringFromResource("public.progression.defendant-offences-changed.json")
                .replaceAll("CASE_ID", caseId1.toString())
                .replaceAll("DEFENDANT_ID", defendantId2.toString())
                .replaceAll("OFFENCE_ID", offenceId3.toString());


        sendMessage(getPublicTopicInstance().createProducer(),
                PROGRESSION_DEFENDANT_OFFENCES_CHANGED,
                new StringToJsonObjectConverter().convert(eventPayloadForDefendantOffencesChanged),
                metadataOf(randomUUID(), PROGRESSION_DEFENDANT_OFFENCES_CHANGED)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(2)),
                withJsonPath("$.hearing.prosecutionCases[0].defendants[*].id", containsInAnyOrder(defendantId1.toString(), defendantId2.toString())),
                withJsonPath("$.hearing.prosecutionCases[0].defendants.[*].offences[*].id",
                        containsInAnyOrder(offenceId1.toString(), offenceId2.toString(), offenceId3.toString()))
        );
    }

    @Test
    public void shouldNotAddDefendantToOtherHearing() throws IOException {
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID defendantId3 = randomUUID();
        final UUID prosecutionCaseId = randomUUID();

        initiateHearingAndAssertDefendantAndOffencesInHearing(prosecutionCaseId, defendantId1, defendantId2, randomUUID(), randomUUID(), randomUUID());

        final UUID hearingId2 = initiateHearingAndAssertDefendantAndOffencesInHearing(prosecutionCaseId, defendantId2, defendantId3, randomUUID(), randomUUID(), randomUUID());

        String eventPayloadString = getStringFromResource("public.progression.hearing-resulted-case-updated.json")
                .replaceAll("CASE_ID", prosecutionCaseId.toString())
                .replaceAll("DEFENDANT_ID", defendantId1.toString());

        try (final Utilities.EventListener caseDefendantsUpdatedForHearing = listenFor(HEARING_CASE_DEFENDANTS_UPDATED_FOR_HEARING, "hearing.event")
                .withFilter(convertStringTo(CaseDefendantsUpdatedForHearing.class, isBean(CaseDefendantsUpdatedForHearing.class)
                        .with(CaseDefendantsUpdatedForHearing::getProsecutionCase, isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(prosecutionCaseId)))
                        .with(CaseDefendantsUpdatedForHearing::getHearingId, is(hearingId2))
                ))) {
            sendMessage(getPublicTopicInstance().createProducer(),
                    PROGRESSION_HEARING_RESULTED_CASE_UPDATED,
                    new StringToJsonObjectConverter().convert(eventPayloadString),
                    metadataOf(randomUUID(), PROGRESSION_HEARING_RESULTED_CASE_UPDATED)
                            .withUserId(randomUUID().toString())
                            .build()
            );

            caseDefendantsUpdatedForHearing.waitFor();

        }
        final UUID offenceId4 = randomUUID();

        try (final Utilities.EventListener registeredHearingAgainstDefendant = listenFor("hearing.events.registered-hearing-against-defendant", "hearing.event")
                .withFilter(convertStringTo(RegisteredHearingAgainstDefendant.class, isBean(RegisteredHearingAgainstDefendant.class)
                        .with(RegisteredHearingAgainstDefendant::getDefendantId, Matchers.is(defendantId1))
                        .with(RegisteredHearingAgainstDefendant::getHearingId, Matchers.is(hearingId2))
                ))) {

            final String eventPayloadForDefendantOffencesChanged = getStringFromResource("public.progression.defendant-offences-changed.json")
                    .replaceAll("CASE_ID", prosecutionCaseId.toString())
                    .replaceAll("DEFENDANT_ID", defendantId1.toString())
                    .replaceAll("OFFENCE_ID", offenceId4.toString());

            sendMessage(getPublicTopicInstance().createProducer(),
                    PROGRESSION_DEFENDANT_OFFENCES_CHANGED,
                    new StringToJsonObjectConverter().convert(eventPayloadForDefendantOffencesChanged),
                    metadataOf(randomUUID(), PROGRESSION_DEFENDANT_OFFENCES_CHANGED)
                            .withUserId(randomUUID().toString())
                            .build()
            );

            registeredHearingAgainstDefendant.expectNone();
        }
    }

    private void shareDaysResultWithCourtClerk(final Hearing hearing, final List<Target> targets, final LocalDate hearingDay) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();
        ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
        shareDaysResultsCommand.setHearingDay(hearingDay);
        shareResultsPerDay(getRequestSpec(), hearing.getId(), hearing.getProsecutionCases().get(0).getId(), with(
                shareDaysResultsCommand,
                command -> command.setCourtClerk(courtClerk1)
        ), targets);
    }

    private void shareDaysResultWithCourtClerkForApplication(final Hearing hearing, final List<Target> targets, final LocalDate hearingDay) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();
        ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
        shareDaysResultsCommand.setHearingDay(hearingDay);
        shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                shareDaysResultsCommand,
                command -> command.setCourtClerk(courtClerk1)
        ), targets);
    }

    private static InitiateHearingCommand initiateHearingWith1DefendantAndCaseUrn(final UUID defendantId1,
                                                                                  final UUID offenceId1,
                                                                                  final UUID prosecutionCaseId) {
        final List<ProsecutionCase> prosecutionCases = singletonList(
                prosecutionCase()
                        .withId(prosecutionCaseId)
                        .withDefendants(Arrays.asList(
                                defendant()
                                        .withId(defendantId1)
                                        .withMasterDefendantId(randomUUID())
                                        .withProsecutionCaseId(prosecutionCaseId)
                                        .withCourtProceedingsInitiated(ZonedDateTime.now())
                                        .withPersonDefendant(PersonDefendant.personDefendant()
                                                .withPersonDetails(Person.person()
                                                        .withGender(Gender.FEMALE)
                                                        .withLastName(STRING.next())
                                                        .build())
                                                .build())
                                        .withOffences(Arrays.asList(Offence.offence()
                                                .withId(offenceId1)
                                                .withOffenceDefinitionId(randomUUID())
                                                .withOffenceCode("OFC")
                                                .withOffenceTitle("OFC TITLE")
                                                .withWording("WORDING")
                                                .withStartDate(LocalDate.now())
                                                .withOffenceLegislation("OffenceLegislation")
                                                .build()))
                                        .build()
                        ))
                        .withInitiationCode(InitiationCode.C)
                        .withProsecutionCaseIdentifier(prosecutionCaseIdentifier()
                                .withProsecutionAuthorityId(randomUUID())
                                .withProsecutionAuthorityCode(STRING.next())
                                .withCaseURN("caseUrn")
                                .build())
                        .build());

        return initiateHearingCommand()
                .setHearing(CoreTestTemplates.hearing(defaultArguments()
                                .setDefendantType(PERSON)
                                .setHearingLanguage(ENGLISH)
                                .setJurisdictionType(MAGISTRATES)
                        )
                        .withProsecutionCases(prosecutionCases)
                        .build());
    }


    private UUID initiateHearingAndAssertDefendantAndOffencesInHearing(final UUID prosecutionCaseId, final UUID defendantId1, final UUID defendantId2, final UUID offenceId1, final UUID offenceId2, final UUID offenceId3) {
        final InitiateHearingCommand initiateHearingCommand = initiateHearingWith2Defendants(prosecutionCaseId, defendantId1, offenceId1, offenceId2, defendantId2, offenceId3);

        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        initiateHearing(getRequestSpec(), initiateHearingCommand);

        Matcher[] matchers = new Matcher[]{
                anyOf(
                        allOf(
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId1.toString())),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(2)),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString()))),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[1].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString()))),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].id", is(defendantId2.toString())),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences", hasSize(1)),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[0].id", is(offenceId3.toString()))),
                        allOf(
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].id", is(defendantId2.toString())),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences", hasSize(1)),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[0].offences[0].id", is(offenceId3.toString())),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].id", is(defendantId1.toString())),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences", hasSize(2)),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[0].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString()))),
                                withJsonPath("$.hearing.prosecutionCases[0].defendants[1].offences[1].id", anyOf(is(offenceId1.toString()), is(offenceId2.toString()))))
                )
        };

        pollForHearing(hearingId.toString(), matchers);
        return hearingId;
    }
}


