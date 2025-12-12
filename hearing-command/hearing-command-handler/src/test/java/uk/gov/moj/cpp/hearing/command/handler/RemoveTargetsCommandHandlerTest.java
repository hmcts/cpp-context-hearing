package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RemoveTargetsCommandHandlerTest {

    private static final String HEARING_COMMAND = "hearing.command.remove-targets";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            TargetRemoved.class,
            HearingChangeIgnored.class
    );

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

    @InjectMocks
    private RemoveTargetsCommandHandler handler;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void eventActionIgnoredShouldBeRaisedForInvalidTargetId() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearingObject.getHearingId();
        final UUID targetId = randomUUID();

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearingObject.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonObject payload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("targetIds", createArrayBuilder().add(targetId.toString()).build())
                .build();
        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(HEARING_COMMAND), payload);

        handler.removeTargets(jsonEnvelope);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), HearingChangeIgnored.class), isBean(HearingChangeIgnored.class)
                .with(HearingChangeIgnored::getHearingId, is(hearingId))
        );
    }
}