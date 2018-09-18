package uk.gov.moj.cpp.hearing.command.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.test.ObjectConverters.asPojo;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.json.schemas.core.AttendanceDay;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.defendant.UpdateDefendantAttendanceCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDefendantAttendanceCommandHandlerTest {

    @InjectMocks
    private UpdateDefendantAttendanceCommandHandler commandHandler;

    @Mock
    private EventStream hearingAggregateEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(HearingInitiated.class,
            DefendantAttendanceUpdated.class);

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void testUpdateDefendantAttendanceCommand() throws Throwable {

        CommandHelpers.InitiateHearingCommandHelper hearing = CommandHelpers.h(standardInitiateHearingTemplate());

        final UUID hearingId = hearing.getHearingId();
        final UUID defendantId = hearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final LocalDate dateOfAttendance = hearing.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        UpdateDefendantAttendanceCommand updateDefendantAttendanceCommand = UpdateDefendantAttendanceCommand.updateDefendantAttendanceCommand()
                .setHearingId(hearingId)
                .setDefendantId(defendantId)
                .setAttendanceDay(AttendanceDay.attendanceDay()
                        .withDay(dateOfAttendance)
                        .withIsInAttendance(true)
                        .build());

        final HearingAggregate hearingAggregate = new HearingAggregate() {{
            apply(new HearingInitiated(hearing.getHearing()));
        }};

        when(this.eventSource.getStreamById(hearing.getHearingId())).thenReturn(this.hearingAggregateEventStream);
        when(this.aggregateService.get(this.hearingAggregateEventStream, HearingAggregate.class)).thenReturn(hearingAggregate);

        final JsonEnvelope jsonEnvelop = envelopeFrom(metadataWithRandomUUID("hearing.update-defendant-attendance-on-hearing-day"), objectToJsonObjectConverter.convert(updateDefendantAttendanceCommand));

        this.commandHandler.updateDefendantAttendance(jsonEnvelop);

        final List<JsonEnvelope> events = verifyAppendAndGetArgumentFrom(this.hearingAggregateEventStream).collect(Collectors.toList());

        assertThat(asPojo(events.get(0), DefendantAttendanceUpdated.class), isBean(DefendantAttendanceUpdated.class)
                .with(DefendantAttendanceUpdated::getAttendanceDay, isBean(AttendanceDay.class)
                        .with(AttendanceDay::getDay, Matchers.is(dateOfAttendance))
                        .with(AttendanceDay::getIsInAttendance, Matchers.is(true)))
                .with(DefendantAttendanceUpdated::getDefendantId, Matchers.is(defendantId))
                .with(DefendantAttendanceUpdated::getHearingId, Matchers.is(hearingId)));


    }
}
