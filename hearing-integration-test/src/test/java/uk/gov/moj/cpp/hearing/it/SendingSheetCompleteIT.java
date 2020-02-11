package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.IntStream;

import org.hamcrest.Matchers;
import org.junit.Test;

public class SendingSheetCompleteIT extends AbstractIT {

    @Test
    public void processSendingSheetComplete_shouldProduceNoHearings_givenNoneGuilty() throws IOException {

        final String eventName = "public.progression.events.sending-sheet-completed";

        final UUID caseID = randomUUID();

        final String eventPayloadString = getStringFromResource(eventName + ".noguilty.json").replaceAll("CASE_ID", caseID.toString());

        Utilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.caseId", is(caseID))));

        sendMessage(getPublicTopicInstance().createProducer(),
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

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        final String eventName = "public.progression.events.sending-sheet-completed";

        String eventPayloadString = getStringFromResource(eventName + ".json")
                .replaceAll("CASE_ID", hearingOne.getFirstCase().getId().toString())
                .replaceAll("PLEA_ID", randomUUID().toString())
                .replaceAll("OFFENCE_ID_1", hearingOne.getFirstOffenceIdForFirstDefendant().toString())
                .replaceAll("COURT_CENTRE_ID", randomUUID().toString());

        Utilities.EventListener publicEventTopic = listenFor("public.mags.hearing.initiated")
                .withFilter(isJson(withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))));

        sendMessage(getPublicTopicInstance().createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        publicEventTopic.waitFor();

        UseCases.initiateHearing(requestSpec, hearingOne.it());

        Queries.getHearingPollForMatch(hearingOne.getHearingId(), 30, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingOne.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, Matchers.is(hearingOne.getFirstCase().getId()))
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getId, Matchers.is(hearingOne.getFirstDefendantForFirstCase().getId()))
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, Matchers.is(hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getPlea, isBean(Plea.class)
                                                        .with(Plea::getPleaValue, is(PleaValue.GUILTY))
                                                        .with(Plea::getPleaDate, is(LocalDate.parse("2017-11-12")))
                                                )

                                        ))
                                ))
                        ))
                )
        );
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

        sendMessage(getPublicTopicInstance().createProducer(),
                eventName,
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), eventName)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        IntStream.range(0, 3).forEach((i) -> publicEventTopic.waitFor());
    }
}
