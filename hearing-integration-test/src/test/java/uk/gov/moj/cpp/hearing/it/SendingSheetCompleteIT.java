package uk.gov.moj.cpp.hearing.it;

import org.junit.Test;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
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

        final String eventName = "public.progression.events.sending-sheet-completed";

        UUID caseId = randomUUID();
        UUID offenceId = randomUUID();

        String eventPayloadString = getStringFromResource(eventName + ".json")
                .replaceAll("CASE_ID", caseId.toString())
                .replaceAll("PLEA_ID", randomUUID().toString())
                .replaceAll("OFFENCE_ID_1", offenceId.toString())
                .replaceAll("COURT_CENTRE_ID", randomUUID().toString());

        TestUtilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.caseId", is(caseId.toString()))));

        sendMessage(publicEvents.createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        publicEventTopic.waitFor();


        InitiateHearingCommand initiateHearingCommand = UseCases.initiateHearing(requestSpec, initiateHearing -> {
            initiateHearing.getCases().get(0).withCaseId(caseId);

            with(initiateHearing.getHearing().getDefendants().get(0).getOffences().get(0), offence -> {
                offence.withId(offenceId)
                        .withCaseId(caseId);
            });

            with(initiateHearing.getHearing().getDefendants().get(0).getDefendantCases().get(0), defendantCase -> {
                defendantCase.withCaseId(caseId);
            });

            with(initiateHearing.getHearing().getWitnesses().get(0), witness -> {
                witness.withCaseId(caseId);
            });

        });
/*
        //TODO - GPE-3032 - need to check plea information when pleas are finished saving plea against hearing.
        final String queryAPIEndPoint = MessageFormat.format(ENDPOINT_PROPERTIES.getProperty("hearing.get.hearing.v2"), initiateHearingCommand.getHearing().getId());
        final String url = getBaseUri() + "/" + queryAPIEndPoint;
        poll(requestParams(url, "application/vnd.hearing.get.hearing.v2+json")
                .withHeader(CPP_UID_HEADER.getName(), CPP_UID_HEADER.getValue()).build())
                .until(status().is(OK),
                        print(),
                        payload().isJson(allOf(
                                withJsonPath("$.cases[0].caseId", is(caseId.toString()))
                                //TODO - plea assertions here.
                        )));
*/
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
