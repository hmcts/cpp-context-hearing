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
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceWitnessAdded;
import uk.gov.moj.cpp.hearing.domain.event.FoundPleaForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.FoundWitnessesForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
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
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MappedToBeanMatcher.convertTo;

@RunWith(MockitoJUnitRunner.class)
public class InitiateHearingCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            HearingInitiated.class,
            FoundPleaForHearingToInherit.class,
            InheritedPlea.class,
            FoundWitnessesForHearingToInherit.class,
            RegisteredHearingAgainstDefendant.class,
            RegisteredHearingAgainstOffence.class
    );
    @Mock
    private EventStream hearingEventStream;
    @Mock
    private EventStream offenceEventStream;
    @Mock
    private EventStream defendantEventStream;
    @Mock
    private EventSource eventSource;
    @Mock
    private AggregateService aggregateService;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @InjectMocks
    private InitiateHearingCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void initiateHearing() throws Throwable {

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());

        setupMockedEventStream(hearingOne.getHearingId(), this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(hearingOne.it()));

        this.hearingCommandHandler.initiate(command);

        JsonEnvelope jsonEnvelope = verifyAppendAndGetArgumentFrom(this.hearingEventStream).findAny().get();

        assertThat(jsonEnvelope, convertTo(HearingInitiated.class, isBean(HearingInitiated.class)
                .with(HearingInitiated::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingOne.getHearingId()))
                        .with(Hearing::getType, is(hearingOne.it().getHearing().getType()))
                        .with(Hearing::getCourtCentreId, is(hearingOne.it().getHearing().getCourtCentreId()))
                        .with(Hearing::getCourtCentreName, is(hearingOne.it().getHearing().getCourtCentreName()))
                        .with(Hearing::getCourtRoomId, is(hearingOne.it().getHearing().getCourtRoomId()))
                        .with(Hearing::getCourtRoomName, is(hearingOne.it().getHearing().getCourtRoomName()))
                        .with(Hearing::getJudge, isBean(Judge.class)
                                .with(Judge::getId, is(hearingOne.getJudge().getId()))
                                .with(Judge::getTitle, is(hearingOne.getJudge().getTitle()))
                                .with(Judge::getFirstName, is(hearingOne.getJudge().getFirstName()))
                                .with(Judge::getLastName, is(hearingOne.getJudge().getLastName()))
                        )
                        .with(Hearing::getDefendants, first(
                                isBean(Defendant.class)
                                        .with(Defendant::getId, is(hearingOne.getFirstDefendantId()))
                                        .with(Defendant::getPersonId, is(hearingOne.getFirstDefendant().getPersonId()))
                                        .with(Defendant::getFirstName, is(hearingOne.getFirstDefendant().getFirstName()))
                                        .with(Defendant::getLastName, is(hearingOne.getFirstDefendant().getLastName()))
                                        .with(Defendant::getNationality, is(hearingOne.getFirstDefendant().getNationality()))
                                        .with(Defendant::getGender, is(hearingOne.getFirstDefendant().getGender()))
                                        .with(Defendant::getDateOfBirth, is(hearingOne.getFirstDefendant().getDateOfBirth()))
                                        .with(Defendant::getDefenceOrganisation, is(hearingOne.getFirstDefendant().getDefenceOrganisation()))
                                        .with(Defendant::getAddress, isBean(Address.class)
                                                .with(Address::getAddress1, is(hearingOne.getFirstDefendant().getAddress().getAddress1()))
                                                .with(Address::getAddress2, is(hearingOne.getFirstDefendant().getAddress().getAddress2()))
                                                .with(Address::getAddress3, is(hearingOne.getFirstDefendant().getAddress().getAddress3()))
                                                .with(Address::getAddress4, is(hearingOne.getFirstDefendant().getAddress().getAddress4()))
                                                .with(Address::getPostCode, is(hearingOne.getFirstDefendant().getAddress().getPostCode()))
                                        )
                                        .with(Defendant::getInterpreter, isBean(Interpreter.class)
                                                .with(Interpreter::getLanguage, is(hearingOne.getFirstDefendant().getInterpreter().getLanguage()))
                                                .with(Interpreter::isNeeded, is(hearingOne.getFirstDefendant().getInterpreter().isNeeded()))
                                        )
                                        .with(Defendant::getDefendantCases, first(
                                                isBean(DefendantCase.class)
                                                        .with(DefendantCase::getCaseId, is(hearingOne.getFirstCaseForFirstDefendant().getCaseId()))
                                                        .with(DefendantCase::getBailStatus, is(hearingOne.getFirstCaseForFirstDefendant().getBailStatus()))
                                                        .with(DefendantCase::getCustodyTimeLimitDate, is(hearingOne.getFirstCaseForFirstDefendant().getCustodyTimeLimitDate()))
                                                )
                                        )
                                        .with(Defendant::getOffences, first(
                                                isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getFirstOffenceIdForFirstDefendant()))
                                                        .with(Offence::getCaseId, is(hearingOne.getFirstCaseId()))
                                                        .with(Offence::getTitle, is(hearingOne.getFirstOffence().getTitle()))
                                                        .with(Offence::getOffenceCode, is(hearingOne.getFirstOffence().getOffenceCode()))
                                                        .with(Offence::getWording, is(hearingOne.getFirstOffence().getWording()))
                                                        .with(Offence::getLegislation, is(hearingOne.getFirstOffence().getLegislation()))
                                                        .with(Offence::getOrderIndex, is(hearingOne.getFirstOffence().getOrderIndex()))
                                                        .with(Offence::getCount, is(hearingOne.getFirstOffence().getCount()))
                                                        .with(Offence::getSection, is(hearingOne.getFirstOffence().getSection()))
                                                        .with(Offence::getStartDate, is(hearingOne.getFirstOffence().getStartDate()))
                                                        .with(Offence::getEndDate, is(hearingOne.getFirstOffence().getEndDate()))
                                                        .with(Offence::getConvictionDate, is(hearingOne.getFirstOffence().getConvictionDate()))
                                                        .with(Offence::getPlea, is(nullValue()))
                                        ))
                                )
                        )
                )
        ));
    }


    @Test
    public void initiateHearingOffence() throws Throwable {

        final UUID offenceId = randomUUID();
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID caseId = randomUUID();

        final UUID originHearingId = randomUUID();
        final LocalDate pleaDate = PAST_LOCAL_DATE.next();
        final String value = "GUILTY";

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), createObjectBuilder()
                .add("offenceId", offenceId.toString())
                .add("defendantId", defendantId.toString())
                .add("hearingId", hearingId.toString())
                .add("caseId", caseId.toString())
                .build());

        final OffenceAggregate offenceAggregate = new OffenceAggregate();
        offenceAggregate.apply(new OffencePleaUpdated(originHearingId, offenceId, pleaDate, value));
        setupMockedEventStream(offenceId, this.offenceEventStream, offenceAggregate);

        this.hearingCommandHandler.initiateHearingOffence(command);

        List<Object> events = verifyAppendAndGetArgumentFrom(this.offenceEventStream).collect(Collectors.toList());

        assertThat((JsonEnvelope) events.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.events.registered-hearing-against-offence"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString()))
                        ))).thatMatchesSchema()
        );

        assertThat((JsonEnvelope) events.get(1),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.events.found-plea-for-hearing-to-inherit"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.defendantId", is(defendantId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.caseId", is(caseId.toString())),
                                withJsonPath("$.originHearingId", is(originHearingId.toString())),
                                withJsonPath("$.pleaDate", is(pleaDate.toString())),
                                withJsonPath("$.value", is(value))
                        ))).thatMatchesSchema()
        );

    }

    @Test
    public void initiateHearingOffencePlea() throws Throwable {

        final UpdateHearingWithInheritedPleaCommand input =
                new UpdateHearingWithInheritedPleaCommand(randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), PAST_LOCAL_DATE.next(), "GUILTY");

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(input));

        setupMockedEventStream(input.getHearingId(), this.hearingEventStream, new NewModelHearingAggregate());

        this.hearingCommandHandler.initiateHearingOffencePlea(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.events.inherited-plea"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(input.getOffenceId().toString())),
                                withJsonPath("$.defendantId", is(input.getDefendantId().toString())),
                                withJsonPath("$.hearingId", is(input.getHearingId().toString())),
                                withJsonPath("$.caseId", is(input.getCaseId().toString())),
                                withJsonPath("$.originHearingId", is(input.getOriginHearingId().toString())),
                                withJsonPath("$.pleaDate", is(input.getPleaDate().toString())),
                                withJsonPath("$.value", is(input.getValue()))
                        ))).thatMatchesSchema()

        ));
    }

    @Test
    public void testEnrichDefenceWitness() throws EventStreamException {
        final UUID defendantId = randomUUID();
        final UUID hearingId = randomUUID();
        final UUID witnessId = randomUUID();
        final JsonEnvelope command = envelopeFrom(
                metadataWithRandomUUID(
                        "hearing.command.initiate-hearing-defence-witness-enrich"),
                createObjectBuilder().add("defendantId", defendantId.toString())
                        .add("hearingId", hearingId.toString()).build());
        final DefendantAggregate defendantAggregate = new DefendantAggregate();
        setupMockedEventStream(defendantId, this.defendantEventStream, defendantAggregate);
        defendantAggregate.apply(new DefenceWitnessAdded(witnessId, defendantId, hearingId,
                "Defence", "Expert", "Mr", "John", "Smith"));
        this.hearingCommandHandler.initiateHearingDefenceWitness(command);
        assertThat(verifyAppendAndGetArgumentFrom(this.defendantEventStream),
                streamContaining(jsonEnvelope(withMetadataEnvelopedFrom(command)
                                .withName("hearing.events.found-witnesses-for-hearing-to-inherit"),
                        payloadIsJson(allOf(
                                withJsonPath("$.id",
                                        is(witnessId.toString())),
                                withJsonPath("$.defendantId",
                                        is(defendantId.toString())),
                                withJsonPath("$.hearingId",
                                        is(hearingId.toString())),
                                withJsonPath("$.title", is("Mr")),
                                withJsonPath("$.firstName", is("John")),
                                withJsonPath("$.lastName", is("Smith")),
                                withJsonPath("$.type", is("Defence")),
                                withJsonPath("$.classification", is(
                                        "Expert")))))
                        .thatMatchesSchema()
                ));
    }

    @Test
    public void recordHearingDefendant() throws EventStreamException {

        RegisterHearingAgainstDefendantCommand command = RegisterHearingAgainstDefendantCommand.builder()
                .withDefendantId(randomUUID())
                .withHearingId(randomUUID())
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataWithRandomUUID("hearing.command.register-defendant-with-hearing"), objectToJsonObjectConverter.convert(command));

        setupMockedEventStream(command.getDefendantId(), defendantEventStream, new DefendantAggregate());

        hearingCommandHandler.recordHearingDefendant(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(defendantEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(envelope).withName("hearing.events.registered-hearing-against-defendant"),
                        payloadIsJson(allOf(
                                withJsonPath("$.defendantId", is(command.getDefendantId().toString())),
                                withJsonPath("$.hearingId", is(command.getHearingId().toString()))
                        ))).thatMatchesSchema()));


    }

    private <T extends Aggregate> void setupMockedEventStream(final UUID id, final EventStream eventStream, final T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        final Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
    }
}