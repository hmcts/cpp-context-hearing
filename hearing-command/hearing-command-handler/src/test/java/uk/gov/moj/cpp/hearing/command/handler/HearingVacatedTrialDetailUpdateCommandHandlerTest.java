package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
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
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingVacatedTrialDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacatedTrialDetailUpdated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingVacatedTrialDetailUpdateCommandHandlerTest {

    private static final String PRIVATE_HEARING_COMMAND_UPDATE_VACATED_TRIAL_DETAIL = "hearing.update-vacated-trial-detail";

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingVacatedTrialDetailUpdated.class,
            HearingEventIgnored.class
    );

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private HearingAggregateMomento hearingAggregateMomento;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private HearingVacatedTrialDetailUpdateCommandHandler handler;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void eventHearingDetailChangedShouldBeCreated() throws Exception {

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());
        final UUID hearingId = hearing.getHearingId();
        final UUID vacatedTrialReasonId = randomUUID();


        HearingVacatedTrialDetailsUpdateCommand hearingVacateTrialDetailsUpdateCommand = new HearingVacatedTrialDetailsUpdateCommand(hearingId,vacatedTrialReasonId,true,true);

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingVacatedTrialDetailUpdated(hearingId, true, vacatedTrialReasonId ));
        }};
        when(hearingAggregateMomento.getHearing()).thenReturn(hearing.getHearing());
        setField(hearingAggregate, "momento", hearingAggregateMomento);

        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingEventStream);
        when(this.aggregateService.get(this.hearingEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelope = envelopeFrom(metadataWithRandomUUID(PRIVATE_HEARING_COMMAND_UPDATE_VACATED_TRIAL_DETAIL), objectToJsonObjectConverter.convert(hearingVacateTrialDetailsUpdateCommand));

        handler.changeHearingVacateTrialDetail(jsonEnvelope);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), HearingVacatedTrialDetailUpdated.class), isBean(HearingVacatedTrialDetailUpdated.class)
                .with(HearingVacatedTrialDetailUpdated::getHearingId, Matchers.is(hearingId))
                .with(HearingVacatedTrialDetailUpdated::getVacatedTrialReasonId, Matchers.is(vacatedTrialReasonId))
                .with(HearingVacatedTrialDetailUpdated::getIsVacated, Matchers.is(true)));
    }
}