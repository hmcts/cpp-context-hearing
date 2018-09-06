package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;

import java.util.UUID;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AdjournHearingCommandHandlerTest {

    private static final String HEARING_EVENT_HEARING_ADJOURNED = "hearing.event.hearing-adjourned";
    private static final String HEARING_ADJOURN_HEARING = "hearing.adjourn-hearing";
    private static final String ARBITRARY_HEARING_ID = "61be1de8-8d5e-4471-99a4-40fe73afee7c";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingAdjourned.class
    );
    @Spy
    StringToJsonObjectConverter stringToJsonObjectConverter;
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @InjectMocks
    private AdjournHearingCommandHandler testObj;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void eventHearingAdjournedShouldCreated() throws Exception {
        //Given
        setupMockedEventStream(UUID.fromString(ARBITRARY_HEARING_ID), this.hearingEventStream, with(new HearingAggregate(), a -> {
            a.apply(jsonObjectToObjectConverter.convert(commandHearingChangedEvent(), HearingAdjourned.class));
        }));

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID(HEARING_ADJOURN_HEARING), commandAdjournHearingEvent());

        //when
        testObj.adjournHearing(command);

        //then
        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName(HEARING_EVENT_HEARING_ADJOURNED),

                        payloadIsJson(allOf(
                                withJsonPath("$.hearings[0].id", equalTo(ARBITRARY_HEARING_ID)),
                                withJsonPath("$.hearings[0].type", equalTo("Sentencing")),
                                withJsonPath("$.hearings[0].courtCentreId", equalTo("103f7a8c-db55-4a09-812e-824fe3250887"))
                        )))
        ));


    }

    private JsonObject commandHearingChangedEvent() {
        return stringToJsonObjectConverter.convert(getHearingAdjournedPayloadAsText());
    }

    private String getHearingAdjournedPayloadAsText() {

        return "{  \n" +
                "   \"caseId\":\"7517ea56-011e-4024-86ab-816b0a077e7f\",\n" +
                "   \"urn\":\"Qbm92cARgM\",\n" +
                "   \"hearings\":[  \n" +
                "      {  \n" +
                "         \"id\":\"" + ARBITRARY_HEARING_ID + "\",\n" +
                "         \"courtCentreId\":\"103f7a8c-db55-4a09-812e-824fe3250887\",\n" +
                "         \"type\":\"Sentencing\",\n" +
                "         \"startDate\":\"02/08/2018\",\n" +
                "         \"startTime\":\"11.30\",\n" +
                "         \"estimateMinutes\":30,\n" +
                "         \"defendants\":[  \n" +
                "            {  \n" +
                "               \"id\":\"9d0dd1d0-ab58-4717-84a8-3914b0d173b6\",\n" +
                "               \"offences\":[  \n" +
                "                  {  \n" +
                "                     \"id\":\"64b72ff5-78d9-436e-8d98-e1846acd2d84\"\n" +
                "                  }\n" +
                "               ]\n" +
                "            }\n" +
                "         ]\n" +
                "      }\n" +
                "   ]\n" +
                "}";
    }

    private JsonObject commandAdjournHearingEvent() {
        return stringToJsonObjectConverter.convert(getAdjournHearingPayloadAsText());
    }

    private String getAdjournHearingPayloadAsText() {

        return "{  \n" +
                "   \"caseId\":\"7517ea56-011e-4024-86ab-816b0a077e7f\",\n" +
                "   \"requestedByHearingId\":\"" + ARBITRARY_HEARING_ID + "\",\n" +
                "   \"urn\":\"Qbm92cARgM\",\n" +
                "   \"hearings\":[  \n" +
                "      {  \n" +
                "         \"courtCentreId\":\"103f7a8c-db55-4a09-812e-824fe3250887\",\n" +
                "         \"type\":\"Sentencing\",\n" +
                "         \"startDate\":\"02/08/2018\",\n" +
                "         \"startTime\":\"11.30\",\n" +
                "         \"estimateMinutes\":30,\n" +
                "         \"defendants\":[  \n" +
                "            {  \n" +
                "               \"id\":\"9d0dd1d0-ab58-4717-84a8-3914b0d173b6\",\n" +
                "               \"offences\":[  \n" +
                "                  {  \n" +
                "                     \"id\":\"64b72ff5-78d9-436e-8d98-e1846acd2d84\"\n" +
                "                  }\n" +
                "               ]\n" +
                "            }\n" +
                "         ]\n" +
                "      }\n" +
                "   ]\n" +
                "}";
    }

    private <T extends Aggregate> void setupMockedEventStream(final UUID id, final EventStream eventStream, final T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        final Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }

}