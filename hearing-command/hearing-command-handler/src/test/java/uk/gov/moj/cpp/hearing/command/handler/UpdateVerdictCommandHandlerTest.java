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
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Defendant;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Offence;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictValue;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

@RunWith(MockitoJUnitRunner.class)
public class UpdateVerdictCommandHandlerTest {

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
            HearingInitiated.class,
            VerdictUpsert.class,
            ConvictionDateAdded.class,
            ConvictionDateRemoved.class
    );

    @InjectMocks
    private UpdateVerdictCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void updateVerdict_toGuilty() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        initiateHearingCommand.getHearing().getDefendants().stream()
                .flatMap(d -> d.getOffences().stream())
                .findFirst()
                .get()
                .setConvictionDate(null);

        HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .addDefendant(Defendant.builder()
                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                        .withPersonId(randomUUID())
                        .addOffence(Offence.builder()
                                .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId())
                                .withVerdict(Verdict.builder()
                                        .withId(randomUUID())
                                        .withValue(
                                                VerdictValue.builder()
                                                        .withId(randomUUID())
                                                        .withCategory("Guilty")
                                                        .withCategoryType("GUILTY")
                                                        .withCode("A1")
                                                        .withDescription(STRING.next())

                                        )
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(integer(0, 3).next())
                                        .withVerdictDate(PAST_LOCAL_DATE.next())
                                        .withUnanimous(BOOLEAN.next())
                                )
                        )
                )
                .build();

        NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        setupMockedEventStream(hearingUpdateVerdictCommand.getHearingId(), this.hearingEventStream, newModelHearingAggregate);

        final JsonEnvelope addVerdictCommand = envelopeFrom(metadataWithRandomUUID("hearing.command.update-verdict"),
                objectToJsonObjectConverter.convert(hearingUpdateVerdictCommand));

        this.hearingCommandHandler.updateVerdict(addVerdictCommand);

        List<Object> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();
        assertThat((JsonEnvelope) events.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.offence-verdict-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(hearingUpdateVerdictCommand.getCaseId().toString())),
                                withJsonPath("$.hearingId", is(hearingUpdateVerdictCommand.getHearingId().toString())),
                                withJsonPath("$.offenceId", is(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.verdictId", is(verdict.getId().toString())),
                                withJsonPath("$.verdictValueId", is(verdict.getValue().getId().toString())),
                                withJsonPath("$.category", is(verdict.getValue().getCategory())),
                                withJsonPath("$.code", is(verdict.getValue().getCode())),
                                withJsonPath("$.description", is(verdict.getValue().getDescription())),
                                withJsonPath("$.numberOfJurors", is(verdict.getNumberOfJurors())),
                                withJsonPath("$.numberOfSplitJurors", is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.unanimous", is(verdict.getUnanimous())),
                                withJsonPath("$.verdictDate", is(verdict.getVerdictDate().toString()))
                        )))
        );

        assertThat((JsonEnvelope) events.get(1),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.conviction-date-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", equalTo(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.convictionDate", equalTo(verdict.getVerdictDate().toString()))

                        )))
        );
    }

    @Test
    public void updateVerdict_toNotGuilty() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.builder()
                .withCaseId(initiateHearingCommand.getCases().get(0).getCaseId())
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .addDefendant(Defendant.builder()
                        .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getId())
                        .withPersonId(randomUUID())
                        .addOffence(Offence.builder()
                                .withId(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId())
                                .withVerdict(Verdict.builder()
                                        .withId(randomUUID())
                                        .withValue(
                                                VerdictValue.builder()
                                                        .withId(randomUUID())
                                                        .withCategory("Not Guilty")
                                                        .withCategoryType("NOT_GUILTY")
                                                        .withCode("A1")
                                                        .withDescription(STRING.next())

                                        )
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(integer(0, 3).next())
                                        .withVerdictDate(PAST_LOCAL_DATE.next())
                                        .withUnanimous(BOOLEAN.next())
                                )
                        )
                )
                .build();

        NewModelHearingAggregate newModelHearingAggregate = new NewModelHearingAggregate();
        newModelHearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));

        setupMockedEventStream(hearingUpdateVerdictCommand.getHearingId(), this.hearingEventStream, newModelHearingAggregate);

        final JsonEnvelope addVerdictCommand = envelopeFrom(metadataWithRandomUUID("hearing.command.update-verdict"),
                objectToJsonObjectConverter.convert(hearingUpdateVerdictCommand));

        this.hearingCommandHandler.updateVerdict(addVerdictCommand);

        List<Object> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());

        Verdict verdict = hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getVerdict();
        assertThat((JsonEnvelope) events.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.offence-verdict-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(hearingUpdateVerdictCommand.getCaseId().toString())),
                                withJsonPath("$.hearingId", is(hearingUpdateVerdictCommand.getHearingId().toString())),
                                withJsonPath("$.offenceId", is(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.verdictId", is(verdict.getId().toString())),
                                withJsonPath("$.verdictValueId", is(verdict.getValue().getId().toString())),
                                withJsonPath("$.category", is(verdict.getValue().getCategory())),
                                withJsonPath("$.code", is(verdict.getValue().getCode())),
                                withJsonPath("$.description", is(verdict.getValue().getDescription())),
                                withJsonPath("$.numberOfJurors", is(verdict.getNumberOfJurors())),
                                withJsonPath("$.numberOfSplitJurors", is(verdict.getNumberOfSplitJurors())),
                                withJsonPath("$.unanimous", is(verdict.getUnanimous())),
                                withJsonPath("$.verdictDate", is(verdict.getVerdictDate().toString()))
                        )))
        );

        assertThat((JsonEnvelope) events.get(1),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.conviction-date-removed"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", equalTo(hearingUpdateVerdictCommand.getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withoutJsonPath("$.convictionDate")
                        )))
        );
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        setupMockedEventStream(id, eventStream, aggregate, a -> {
        });
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate, Consumer<T> consumer) {
        consumer.accept(aggregate);
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }

    public static <T> T with(T object, Consumer<T> consumer) {
        consumer.accept(object);
        return object;
    }

}
