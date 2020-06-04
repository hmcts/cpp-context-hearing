package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObjectBuilder;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SetTrialTypeCommandHandlerTest {

    private static final String HEARING_SET_TRIAL_TYPE = "hearing.command.set-trial-type";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingTrialType.class,
            HearingTrialVacated.class
    );

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private Requester requester;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private SetTrialTypeCommandHandler setTrialTypeCommandHandler;

    @Captor
    private ArgumentCaptor<JsonEnvelope> requesterArgumentCaptor;

    private final UUID trialTypeId = randomUUID();
    private final UUID vacatedTrialReasonId = randomUUID();

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void eventHearingSetTrialTypeShouldBeCreated() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearingObject.getHearingId();


        HearingTrialType trailType = new HearingTrialType(hearingId, trialTypeId, "A", "Effective", "Some Description");
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearingObject.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(HEARING_SET_TRIAL_TYPE), objectToJsonObjectConverter.convert(trailType));

        when(requester.request(any(JsonEnvelope.class))).thenReturn(prepareActiveHearingsResponse());

        setTrialTypeCommandHandler.handleTrialType(jsonEnvelope);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), HearingTrialType.class), isBean(HearingTrialType.class)
                .with(HearingTrialType::getHearingId, Matchers.is(hearingId))
                .with(HearingTrialType::getTrialTypeId, Matchers.is(trialTypeId))
        );
    }


    @Test
    public void eventHearingSetVacateTrialTypeShouldBeCreated() throws EventStreamException {
        CommandHelpers.InitiateHearingCommandHelper hearingObject = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearingObject.getHearingId();

        HearingTrialVacated vacateTrialType = new HearingTrialVacated(hearingId, vacatedTrialReasonId, "A", "Vacated", "Vacated Trial");
        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearingObject.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearingObject.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(HEARING_SET_TRIAL_TYPE), objectToJsonObjectConverter.convert(vacateTrialType));

        when(requester.request(any(JsonEnvelope.class))).thenReturn(prepareVacatedHearingsResponse());

        setTrialTypeCommandHandler.handleTrialType(jsonEnvelope);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), HearingTrialVacated.class), isBean(HearingTrialVacated.class)
                .with(HearingTrialVacated::getHearingId, Matchers.is(hearingId))
                .with(HearingTrialVacated::getVacatedTrialReasonId, Matchers.is(vacatedTrialReasonId))
        );

    }


    private JsonEnvelope prepareActiveHearingsResponse() {
        return envelopeFrom(metadataWithRandomUUIDAndName(), mockCrackedIneffectiveReasonPayload());
    }

    private JsonObjectBuilder mockCrackedIneffectiveReasonPayload() {
        return createObjectBuilder()
                .add("id", trialTypeId.toString())
                .add("code", "A")
                .add("type", "Effective")
                .add("description", "full description");
    }

    private JsonEnvelope prepareVacatedHearingsResponse() {
        return envelopeFrom(metadataWithRandomUUIDAndName(), mockVacatedReasonPayload());
    }

    private JsonObjectBuilder mockVacatedReasonPayload() {
        return createObjectBuilder()
                .add("id", vacatedTrialReasonId.toString())
                .add("code", "A")
                .add("type", "Vacated")
                .add("description", "full description");
    }

}