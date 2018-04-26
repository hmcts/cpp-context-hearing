package uk.gov.moj.cpp.hearing.command.handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;

import java.util.UUID;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(MockitoJUnitRunner.class)
public class AddDefenceCounselCommandHandlerTest {

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            DefenceCounselUpsert.class
    );

    @InjectMocks
    private AddDefenceCounselCommandHandler addDefenceCounselCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void addDefenceCounsel() throws EventStreamException {

        AddDefenceCounselCommand addDefenceCounselCommand = AddDefenceCounselCommand.builder()
                .withHearingId(randomUUID())
                .withPersonId(randomUUID())
                .withAttendeeId(randomUUID())
                .withTitle(STRING.next())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withStatus(STRING.next())
                .addDefendantId(DefendantId.builder().withDefendantId(randomUUID()))
                .build();

        setupMockedEventStream(addDefenceCounselCommand.getHearingId(), this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.add-defence-counsel"),
                objectToJsonObjectConverter.convert(addDefenceCounselCommand));

        this.addDefenceCounselCommandHandler.addDefenceCounsel(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.newdefence-counsel-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(addDefenceCounselCommand.getHearingId().toString())),
                                withJsonPath("$.personId", is(addDefenceCounselCommand.getPersonId().toString())),
                                withJsonPath("$.attendeeId", is(addDefenceCounselCommand.getAttendeeId().toString())),
                                withJsonPath("$.title", is(addDefenceCounselCommand.getTitle())),
                                withJsonPath("$.firstName", is(addDefenceCounselCommand.getFirstName())),
                                withJsonPath("$.lastName", is(addDefenceCounselCommand.getLastName())),
                                withJsonPath("$.status", is(addDefenceCounselCommand.getStatus())),
                                withJsonPath("$.defendantIds.[0]", is(addDefenceCounselCommand.getDefendantIds().get(0).getDefendantId().toString()))
                        ))).thatMatchesSchema()
        ));
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}