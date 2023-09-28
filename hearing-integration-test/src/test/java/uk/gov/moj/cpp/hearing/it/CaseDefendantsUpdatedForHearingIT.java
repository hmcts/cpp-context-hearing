package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResultsPerDay;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandV2Template;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.path.json.JsonPath;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.mockito.Spy;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;

public class CaseDefendantsUpdatedForHearingIT extends AbstractIT {

    private final String hearingResultedCaseUpdatedEvent = "public.progression.hearing-resulted-case-updated";
    private final String publicProgressionDefendantOffencesChanged = "public.progression.defendant-offences-changed";
    private static final String HEARING_CASE_DEFENDANTS_UPDATED_FOR_HEARING = "hearing.case-defendants-updated-for-hearing";
    private static final String HEARING_EVENTS_EXISTING_HEARING_UPDATED = "hearing.events.existing-hearing-updated";
    private static final String HEARING_COMMAND_UPDATE_RELATED_HEARING = "hearing.update-related-hearing";
    private final String hearingResultedApplicationUpdatedEvent = "public.progression.hearing-resulted-application-updated";

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    private StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    private final static LocalDate orderedDate = PAST_LOCAL_DATE.next();


    @Test
    public void testCaseDefendantsUpdated() throws IOException {
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID caseId = hearingOne.getHearing().getProsecutionCases().get(0).getId();
        String eventPayloadString = getStringFromResource("public.progression.hearing-resulted-case-updated.json")
                .replaceAll("CASE_ID", caseId.toString())
                .replaceAll("DEFENDANT_ID", defendantId.toString());

        sendMessage(getPublicTopicInstance().createProducer(),
                hearingResultedCaseUpdatedEvent,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), hearingResultedCaseUpdatedEvent)
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
    public void testApplicationDefendantsUpdated() throws IOException {
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate(), false, true, false, true, false));

        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getCourtApplications().get(0).getApplicant().getMasterDefendant().getMasterDefendantId();
        final UUID applicationId = hearingOne.getHearing().getCourtApplications().get(0).getId();
        String eventPayloadString = getStringFromResource("public.progression.hearing-resulted-application-updated.json")
                .replaceAll("APPLICATION_ID", applicationId.toString())
                .replaceAll("DEFENDANT_ID", defendantId.toString());

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
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

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
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate(), true, false, false, true, false));

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

    @Ignore("passing locally, but flaky in jenkin build")
    @Test
    public void testCaseDefendantsUpdatedForMergedCase() throws Exception {
        stubUsersAndGroupsUserRoles(getLoggedInUser());
        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing h1 = hearingOne.getHearing();
        final ProsecutionCase prosecutionCase1 = h1.getProsecutionCases().get(0);
        final UUID hearingId1 = h1.getId();
        final UUID caseId1 = prosecutionCase1.getId();
        final UUID defendantId1 = prosecutionCase1.getDefendants().get(0).getId();
        final UUID offenceId1 = prosecutionCase1.getDefendants().get(0).getOffences().get(0).getId();

        final CommandHelpers.InitiateHearingCommandHelper hearingTwo = h(UseCases.initiateHearing(getRequestSpec(), minimumInitiateHearingTemplate()));

        final Hearing h2 = hearingTwo.getHearing();

        final ProsecutionCase prosecutionCase2 = h2.getProsecutionCases().get(0);
        final UUID defendantId2 = prosecutionCase2.getDefendants().get(0).getId();
        final UUID offenceId2 = prosecutionCase2.getDefendants().get(0).getOffences().get(0).getId();

        final String publicProgressionHearingResultedPayload = getStringFromResource("public.progression.hearing-resulted-case-updated-merged-case.json")
                .replaceAll("CASE_ID", caseId1.toString())
                .replaceAll("DEFENDANT_ID", defendantId2.toString())
                .replaceAll("OFFENCE_ID", offenceId2.toString());

        final String updateRelatedHearingPayload = getStringFromResource("hearing.update-related-hearing.json")
                .replaceAll("CASE_ID", caseId1.toString())
                .replaceAll("DEFENDANT_ID", defendantId2.toString())
                .replaceAll("OFFENCE_ID", offenceId2.toString());


        sendMessage(getPublicTopicInstance().createProducer(),
                hearingResultedCaseUpdatedEvent,
                new StringToJsonObjectConverter().convert(publicProgressionHearingResultedPayload),
                metadataOf(randomUUID(), hearingResultedCaseUpdatedEvent)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        makeCommand(getRequestSpec(), "hearing.update-hearing")
                .ofType("application/vnd.hearing.related-hearing+json")
                .withArgs(hearingId1)
                .withPayload(updateRelatedHearingPayload)
                .withCppUserId(USER_ID_VALUE_AS_ADMIN)
                .executeSuccessfully();


        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(CoreMatchers.allOf(
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[*].id", containsInAnyOrder(defendantId1.toString(), defendantId2.toString())),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[*].offences[*].id", containsInAnyOrder(offenceId1.toString(), offenceId2.toString())),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(2)),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[*].offences", hasSize(2))
                                )
                        )
                );

        //Add offence 3 to defendant2 recently moved to Hearing1
        final UUID offenceId3 = UUID.randomUUID();

        try (final Utilities.EventListener registeredHearingAgainstDefendant = listenFor("hearing.events.registered-hearing-against-defendant", "hearing.event")
                .withFilter(convertStringTo(RegisteredHearingAgainstDefendant.class, isBean(RegisteredHearingAgainstDefendant.class)
                        .with(RegisteredHearingAgainstDefendant::getHearingId, is(hearingId1))
                        .with(RegisteredHearingAgainstDefendant::getDefendantId, is(defendantId2))
                ))) {


            final String eventPayloadForDefendantOffencesChanged = getStringFromResource("public.progression.defendant-offences-changed.json")
                    .replaceAll("CASE_ID", caseId1.toString())
                    .replaceAll("DEFENDANT_ID", defendantId2.toString())
                    .replaceAll("OFFENCE_ID", offenceId3.toString());


            sendMessage(getPublicTopicInstance().createProducer(),
                    publicProgressionDefendantOffencesChanged,
                    new StringToJsonObjectConverter().convert(eventPayloadForDefendantOffencesChanged),
                    metadataOf(randomUUID(), publicProgressionDefendantOffencesChanged)
                            .withUserId(randomUUID().toString())
                            .build()
            );

            registeredHearingAgainstDefendant.waitFor();

        }

        poll(requestParams(getURL("hearing.get.hearing", hearingOne.getHearingId()), "application/vnd.hearing.get.hearing+json")
                .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                .timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
                .until(status().is(OK),
                        print(),
                        payload().isJson(CoreMatchers.allOf(
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants", hasSize(2)),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants[*].id", containsInAnyOrder(defendantId1.toString(), defendantId2.toString())),
                                        withJsonPath("$.hearing.prosecutionCases[0].defendants.[*].offences[*].id",
                                                containsInAnyOrder(offenceId1.toString(), offenceId2.toString(), offenceId3.toString()))
                                )
                        )
                );
    }

    private void shareDaysResultWithCourtClerk(final Hearing hearing, final List<Target> targets, final LocalDate hearingDay) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();
        ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
        shareDaysResultsCommand.setHearingDay(hearingDay);
        shareResultsPerDay(getRequestSpec(), hearing.getId(), hearing.getProsecutionCases().get(0).getId(),  with(
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
        shareResultsPerDay(getRequestSpec(), hearing.getId(),  with(
                shareDaysResultsCommand,
                command -> command.setCourtClerk(courtClerk1)
        ), targets);
    }
}


