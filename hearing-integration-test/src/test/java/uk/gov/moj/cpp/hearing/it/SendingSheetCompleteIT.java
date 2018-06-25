package uk.gov.moj.cpp.hearing.it;

import org.hamcrest.Matchers;
import org.junit.Test;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddProsecutionCounselCommandTemplates.addProsecutionCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

public class SendingSheetCompleteIT extends AbstractIT {

    @Test
    public void processSendingSheetComplete_shouldProduceNoHearings_givenNoneGuilty() throws IOException {

        final String eventName = "public.progression.events.sending-sheet-completed";

        final UUID caseID = randomUUID();

        final String eventPayloadString = getStringFromResource(eventName + ".noguilty.json").replaceAll("CASE_ID", caseID.toString());

        TestUtilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.caseId", is(caseID))));

        sendMessage(publicEvents.createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        publicEventTopic.expectNoneWithin(10000);
    }

    @Test
    public void processSendingSheetComplete_shouldProduceMagsPleaInformation_givenInitiatedHearing() throws IOException {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = new CommandHelpers.InitiateHearingCommandHelper(standardInitiateHearingTemplate());

        final String eventName = "public.progression.events.sending-sheet-completed";

        String eventPayloadString = getStringFromResource(eventName + ".json")
                .replaceAll("CASE_ID", hearingOne.getFirstCaseId().toString())
                .replaceAll("PLEA_ID", randomUUID().toString())
                .replaceAll("OFFENCE_ID_1", hearingOne.getFirstOffenceIdForFirstDefendant().toString())
                .replaceAll("COURT_CENTRE_ID", randomUUID().toString());

        TestUtilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.caseId", is(hearingOne.getFirstCaseId().toString()))));

        sendMessage(publicEvents.createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        publicEventTopic.waitFor();

        UseCases.initiateHearing(requestSpec, hearingOne.it());

        final String hearingDetailsQueryURL = getURL("hearing.get.hearing", hearingOne.getHearingId().toString());

        poll(requestParameters(hearingDetailsQueryURL, "application/vnd.hearing.get.hearing+json"))
                .until(
                        status().is(OK),
                        payload().isJson(allOf(withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString())),
                                withJsonPath("$.cases[0].caseId", is(hearingOne.getFirstCaseId().toString())),
                                withJsonPath("$.cases[0].defendants[0].defendantId", is(hearingOne.getFirstDefendantId().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].id", is(hearingOne.getFirstOffenceIdForFirstDefendant().toString())),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.pleaDate", is("2017-11-12")),
                                withJsonPath("$.cases[0].defendants[0].offences[0].plea.value", is("GUILTY"))
                        )));
    }

    @Test
    public void processSendingSheetComplete_shouldProduceHearings_given3GuiltyPleas() throws IOException {

        final String eventName = "public.progression.events.sending-sheet-completed";

        UUID caseId = randomUUID();
        UUID courtCentreId = randomUUID();

        String eventPayloadString = getStringFromResource(eventName + ".partialguilty.json")
                .replaceAll("CASE_ID", caseId.toString())
                .replaceAll("COURT_CENTRE_ID", courtCentreId.toString());

        for (int done = 0; done <= 4; done++) {
            final UUID offenceID = randomUUID();
            final UUID pleaID = randomUUID();
            eventPayloadString = eventPayloadString.replaceAll("OFFENCE_ID_" + done, offenceID.toString());
            eventPayloadString = eventPayloadString.replaceAll("PLEA_ID_" + done, pleaID.toString());
        }

        TestUtilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.caseId", is(caseId.toString()))));

        sendMessage(publicEvents.createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        IntStream.range(0, 3).forEach((i) -> {
            publicEventTopic.waitFor();
        });
    }
}
