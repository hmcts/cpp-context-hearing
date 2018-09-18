package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.http.RestPoller.poll;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.publicEvents;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.Test;

public class SendingSheetCompleteIT extends AbstractIT {

    @Test
    public void processSendingSheetComplete_shouldProduceNoHearings_givenNoneGuilty() throws IOException {

        final String eventName = "public.progression.events.sending-sheet-completed";

        final UUID caseID = randomUUID();

        final String eventPayloadString = getStringFromResource(eventName + ".noguilty.json").replaceAll("CASE_ID", caseID.toString());

        Utilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
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

        /*final CommandHelpers.InitiateHearingCommandHelper hearingOne = new CommandHelpers.InitiateHearingCommandHelper(standardInitiateHearingTemplate());

        final String eventName = "public.progression.events.sending-sheet-completed";

        String eventPayloadString = getStringFromResource(eventName + ".json")
                .replaceAll("CASE_ID", hearingOne.getFirstCaseId().toString())
                .replaceAll("PLEA_ID", randomUUID().toString())
                .replaceAll("OFFENCE_ID_1", hearingOne.getFirstOffenceIdForFirstDefendant().toString())
                .replaceAll("COURT_CENTRE_ID", randomUUID().toString());

        Utilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
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
                        )));*/
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

        Utilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.caseId", is(caseId.toString()))));

        sendMessage(publicEvents.createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        IntStream.range(0, 3).forEach((i) -> publicEventTopic.waitFor());
    }
}
