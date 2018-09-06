package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.Collections.singletonList;
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
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

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
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.command.verdict.Jurors;
import uk.gov.moj.cpp.hearing.command.verdict.LesserOffence;
import uk.gov.moj.cpp.hearing.command.verdict.Verdict;
import uk.gov.moj.cpp.hearing.command.verdict.VerdictType;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final UUID caseId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId();

        final UUID offenceId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId();

        final boolean unanimous = BOOLEAN.next();

        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.hearingUpdateVerdictCommand()
                .withHearingId(hearingId)
                .withVerdicts(singletonList(Verdict.verdict()
                        .setOffenceId(offenceId)
                        .setVerdictType(VerdictType.verdictType()
                                .setId(randomUUID())
                                .setCategory("GUILTY")
                                .setCategoryType("GUILTY"))
                        .setLesserOffence(LesserOffence.lesserOffence()
                                .setOffenceDefinitionId(randomUUID())
                                .setOffenceCode("A1")
                                .setTitle(STRING.next())
                                .setLegislation(STRING.next()))
                        .setJurors(Jurors.jurors()
                                .setNumberOfJurors(integer(9, 12).next())
                                .setNumberOfSplitJurors(numberOfSplitJurors)
                                .setUnanimous(unanimous))
                        .setVerdictDate(PAST_LOCAL_DATE.next())
                ));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(hearingUpdateVerdictCommand.getHearingId(), this.hearingEventStream, hearingAggregate);

        final JsonEnvelope addVerdictCommand = envelopeFrom(metadataWithRandomUUID("hearing.command.update-verdict"),
                objectToJsonObjectConverter.convert(hearingUpdateVerdictCommand));

        this.hearingCommandHandler.updateVerdict(addVerdictCommand);

        final List<?> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());
        final Verdict verdict = hearingUpdateVerdictCommand.getVerdicts().get(0);

        assertThat((JsonEnvelope) events.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.offence-verdict-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(caseId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.verdictDate", is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.verdictTypeId", is(verdict.getVerdictType().getId().toString())),
                                withJsonPath("$.category", is(verdict.getVerdictType().getCategory())),
                                withJsonPath("$.categoryType", is(verdict.getVerdictType().getCategoryType())),
                                withJsonPath("$.offenceDefinitionId", is(verdict.getLesserOffence().getOffenceDefinitionId().toString())),
                                withJsonPath("$.offenceCode", is(verdict.getLesserOffence().getOffenceCode())),
                                withJsonPath("$.title", is(verdict.getLesserOffence().getTitle())),
                                withJsonPath("$.legislation", is(verdict.getLesserOffence().getLegislation())),
                                withJsonPath("$.numberOfJurors", is(verdict.getJurors().getNumberOfJurors())),
                                withJsonPath("$.numberOfSplitJurors", is(verdict.getJurors().getNumberOfSplitJurors())),
                                withJsonPath("$.unanimous", is(verdict.getJurors().getUnanimous()))
                        )))
        );

        assertThat((JsonEnvelope) events.get(1),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.conviction-date-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(caseId.toString())),
                                withJsonPath("$.offenceId", equalTo(offenceId.toString())),
                                withJsonPath("$.convictionDate", equalTo(verdict.getVerdictDate().toString()))

                        )))
        );
    }

    @Test
    public void updateVerdict_toNotGuilty() throws EventStreamException {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingTemplateForMagistrates();

        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        final UUID caseId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getId();

        final UUID offenceId = initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId();

        final boolean unanimous = BOOLEAN.next();

        final int numberOfSplitJurors = unanimous ? 0 : integer(1, 3).next();

        final HearingUpdateVerdictCommand hearingUpdateVerdictCommand = HearingUpdateVerdictCommand.hearingUpdateVerdictCommand()
                .withHearingId(initiateHearingCommand.getHearing().getId())
                .withVerdicts(Arrays.asList(Verdict.verdict()
                        .setOffenceId(offenceId)
                        .setVerdictType(VerdictType.verdictType()
                                .setId(randomUUID())
                                .setCategory("Not GUILTY")
                                .setCategoryType("NOT_GUILTY"))
                        .setLesserOffence(LesserOffence.lesserOffence()
                                .setOffenceDefinitionId(randomUUID())
                                .setOffenceCode("A1")
                                .setTitle(STRING.next())
                                .setLegislation(STRING.next()))
                        .setJurors(Jurors.jurors()
                                .setNumberOfJurors(integer(9, 12).next())
                                .setNumberOfSplitJurors(numberOfSplitJurors)
                                .setUnanimous(unanimous))
                        .setVerdictDate(PAST_LOCAL_DATE.next())
                ));

        final HearingAggregate hearingAggregate = new HearingAggregate();

        hearingAggregate.apply(new HearingInitiated(initiateHearingCommand.getHearing()));

        setupMockedEventStream(hearingUpdateVerdictCommand.getHearingId(), this.hearingEventStream, hearingAggregate);

        final JsonEnvelope addVerdictCommand = envelopeFrom(metadataWithRandomUUID("hearing.command.update-verdict"),
                objectToJsonObjectConverter.convert(hearingUpdateVerdictCommand));

        this.hearingCommandHandler.updateVerdict(addVerdictCommand);

        final List<?> events = verifyAppendAndGetArgumentFrom(this.hearingEventStream).collect(Collectors.toList());
        final Verdict verdict = hearingUpdateVerdictCommand.getVerdicts().get(0);

        assertThat((JsonEnvelope) events.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.offence-verdict-updated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(caseId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.verdictDate", is(verdict.getVerdictDate().toString())),
                                withJsonPath("$.verdictTypeId", is(verdict.getVerdictType().getId().toString())),
                                withJsonPath("$.category", is(verdict.getVerdictType().getCategory())),
                                withJsonPath("$.categoryType", is(verdict.getVerdictType().getCategoryType())),
                                withJsonPath("$.offenceDefinitionId", is(verdict.getLesserOffence().getOffenceDefinitionId().toString())),
                                withJsonPath("$.offenceCode", is(verdict.getLesserOffence().getOffenceCode())),
                                withJsonPath("$.title", is(verdict.getLesserOffence().getTitle())),
                                withJsonPath("$.legislation", is(verdict.getLesserOffence().getLegislation())),
                                withJsonPath("$.numberOfJurors", is(verdict.getJurors().getNumberOfJurors())),
                                withJsonPath("$.numberOfSplitJurors", is(verdict.getJurors().getNumberOfSplitJurors())),
                                withJsonPath("$.unanimous", is(verdict.getJurors().getUnanimous()))
                        )))
        );

        assertThat((JsonEnvelope) events.get(1),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(addVerdictCommand)
                                .withName("hearing.conviction-date-removed"),
                        payloadIsJson(allOf(
                                withJsonPath("$.caseId", is(caseId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withoutJsonPath("$.convictionDate")
                        )))
        );
    }

    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        setupMockedEventStream(id, eventStream, aggregate, a -> {
        });
    }

    @SuppressWarnings("unchecked")
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
