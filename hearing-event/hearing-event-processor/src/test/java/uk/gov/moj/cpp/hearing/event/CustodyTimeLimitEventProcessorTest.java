package uk.gov.moj.cpp.hearing.event;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.Framework5Fix.toJsonEnvelope;

import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.json.Json;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CustodyTimeLimitEventProcessorTest {

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @InjectMocks
    private CustodyTimeLimitEventProcessor custodyTimeLimitEventProcessor;

    @Test
    public void shouldPublishStopClockCustodyTimeLimit() {
        final String hearingId = randomUUID().toString();
        final String offence1Id = randomUUID().toString();
        final String offence2Id = randomUUID().toString();
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.event.custody-time-limit-clock-stopped"),
                Json.createObjectBuilder()
                        .add("hearingId", hearingId)
                        .add("offenceIds", createArrayBuilder()
                                .add(offence1Id)
                                .add(offence2Id)
                                .build())
                        .build());

        custodyTimeLimitEventProcessor.publishStopClockCustodyTimeLimit(event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getValue()),
                jsonEnvelope(metadata().withName("public.events.hearing.custody-time-limit-clock-stopped"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId)),
                                withJsonPath("$.offenceIds[0]", is(offence1Id)),
                                withJsonPath("$.offenceIds[1]", is(offence2Id))
                        )))
        );
    }

    @Test
    public void shouldUpdateHearingsWithExtendedCustodyTimeLimit() {
        final String hearingId1 = randomUUID().toString();
        final String hearingId2 = randomUUID().toString();
        final String offenceId = randomUUID().toString();
        final String extendedCustodyTimeLimit = "2021-05-23";

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("public.events.progression.custody-time-limit-extended"),
                Json.createObjectBuilder()
                        .add("hearingIds", createArrayBuilder()
                                .add(hearingId1)
                                .add(hearingId2)
                                .build())
                        .add("extendedTimeLimit", extendedCustodyTimeLimit)
                        .add("offenceId", offenceId)
                        .build());

        custodyTimeLimitEventProcessor.updateHearingsWithExtendedCustodyTimeLimit(event);

        verify(this.sender, times(2)).send(this.envelopeArgumentCaptor.capture());

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getAllValues().get(0)),
                jsonEnvelope(metadata().withName("hearing.command.extend-custody-time-limit"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId1)),
                                withJsonPath("$.offenceId", is(offenceId)),
                                withJsonPath("$.extendedTimeLimit", is(extendedCustodyTimeLimit))
                        )))
        );

        assertThat(
                toJsonEnvelope(this.envelopeArgumentCaptor.getAllValues().get(1)),
                jsonEnvelope(metadata().withName("hearing.command.extend-custody-time-limit"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId2)),
                                withJsonPath("$.offenceId", is(offenceId)),
                                withJsonPath("$.extendedTimeLimit", is(extendedCustodyTimeLimit))
                        )))
        );
    }

}
