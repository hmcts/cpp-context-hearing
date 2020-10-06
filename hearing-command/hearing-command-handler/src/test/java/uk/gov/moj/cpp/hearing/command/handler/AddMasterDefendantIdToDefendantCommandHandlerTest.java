package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.MasterDefendantIdAdded;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import javax.json.JsonObject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class AddMasterDefendantIdToDefendantCommandHandlerTest {

    private static final String HEARING_COMMAND = "hearing.command.add-master-defendant-id-to-defendant";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            MasterDefendantIdAdded.class
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
    private AddMasterDefendantIdToDefendantCommandHandler handler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void eventMasterDefendantIdAddedShouldBeRaised() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearingObject.getHearingId();
        final UUID prosecutionCaseId = hearingObject.getHearing().getProsecutionCases().get(0).getId();
        final UUID defendantId = hearingObject.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final UUID masterDefendantId = randomUUID();

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearingObject.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonObject payload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("defendantId", defendantId.toString())
                .add("masterDefendantId", masterDefendantId.toString())
                .build();

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(HEARING_COMMAND), payload);

        handler.addMasterDefendantIdToDefendant(jsonEnvelope);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), MasterDefendantIdAdded.class), isBean(MasterDefendantIdAdded.class)
                .with(MasterDefendantIdAdded::getHearingId, is(hearingId))
                .with(MasterDefendantIdAdded::getProsecutionCaseId, is(prosecutionCaseId))
                .with(MasterDefendantIdAdded::getDefendantId, is(defendantId))
                .with(MasterDefendantIdAdded::getMasterDefendantId, is(masterDefendantId))
        );
    }
}