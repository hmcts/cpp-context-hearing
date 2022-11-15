package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.stubUsersAndGroupsUserRoles;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class CaseDefendantsUpdatedForHearingIT extends AbstractIT {

    private final String hearingResultedCaseUpdatedEvent = "public.progression.hearing-resulted-case-updated";
    private final String publicProgressionDefendantOffencesChanged = "public.progression.defendant-offences-changed";
    private static final String HEARING_CASE_DEFENDANTS_UPDATED_FOR_HEARING = "hearing.case-defendants-updated-for-hearing";
    private static final String HEARING_EVENTS_EXISTING_HEARING_UPDATED = "hearing.events.existing-hearing-updated";
    private static final String HEARING_COMMAND_UPDATE_RELATED_HEARING = "hearing.update-related-hearing";

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
                                .withValue(d -> d.getDefendants().get(0).getProceedingsConcluded(), true)))
                )
        );
    }

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
}


