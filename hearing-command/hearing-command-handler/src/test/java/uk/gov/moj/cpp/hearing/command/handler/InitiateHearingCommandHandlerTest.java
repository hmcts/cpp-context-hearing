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
import uk.gov.moj.cpp.hearing.command.initiate.UpdateHearingWithInheritedPleaCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstOffence;
import uk.gov.moj.cpp.hearing.domain.event.DefenceWitnessAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.FoundWitnessesForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.FoundPleaForHearingToInherit;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
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
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

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

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();

        final UUID caseId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        setupMockedEventStream(hearingId, this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.hearingCommandHandler.initiate(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.events.initiated"),

                        payloadIsJson(allOf(

                                withJsonPath("$.cases.[0].caseId", equalTo(caseId.toString())),
                                withJsonPath("$.cases.[0].urn", is(initiateHearingCommand.getCases().get(0).getUrn())),
                                withJsonPath("$.hearing.id", is(hearingId.toString())),
                                withJsonPath("$.hearing.type", is(initiateHearingCommand.getHearing().getType())),

                                withJsonPath("$.hearing.courtCentreId", is(initiateHearingCommand.getHearing().getCourtCentreId().toString())),
                                withJsonPath("$.hearing.courtCentreName", is(initiateHearingCommand.getHearing().getCourtCentreName())),

                                withJsonPath("$.hearing.courtRoomId", is(initiateHearingCommand.getHearing().getCourtRoomId().toString())),
                                withJsonPath("$.hearing.courtRoomName", is(initiateHearingCommand.getHearing().getCourtRoomName())),

                                withJsonPath("$.hearing.judge.id", is(initiateHearingCommand.getHearing().getJudge().getId().toString())),
                                withJsonPath("$.hearing.judge.title", is(initiateHearingCommand.getHearing().getJudge().getTitle())),
                                withJsonPath("$.hearing.judge.firstName", is(initiateHearingCommand.getHearing().getJudge().getFirstName())),
                                withJsonPath("$.hearing.judge.lastName", is(initiateHearingCommand.getHearing().getJudge().getLastName())),

                                withJsonPath("$.hearing.defendants.[0].id", is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString())),
                                withJsonPath("$.hearing.defendants.[0].personId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getPersonId().toString())),
                                withJsonPath("$.hearing.defendants.[0].firstName", is(initiateHearingCommand.getHearing().getDefendants().get(0).getFirstName())),
                                withJsonPath("$.hearing.defendants.[0].lastName", is(initiateHearingCommand.getHearing().getDefendants().get(0).getLastName())),
                                withJsonPath("$.hearing.defendants.[0].nationality", is(initiateHearingCommand.getHearing().getDefendants().get(0).getNationality())),
                                withJsonPath("$.hearing.defendants.[0].gender", is(initiateHearingCommand.getHearing().getDefendants().get(0).getGender())),

                                withJsonPath("$.hearing.defendants.[0].address.address1", is(initiateHearingCommand.getHearing().getDefendants().get(0).getAddress().getAddress1())),
                                withJsonPath("$.hearing.defendants.[0].address.address2", is(initiateHearingCommand.getHearing().getDefendants().get(0).getAddress().getAddress2())),
                                withJsonPath("$.hearing.defendants.[0].address.address3", is(initiateHearingCommand.getHearing().getDefendants().get(0).getAddress().getAddress3())),
                                withJsonPath("$.hearing.defendants.[0].address.address4", is(initiateHearingCommand.getHearing().getDefendants().get(0).getAddress().getAddress4())),
                                withJsonPath("$.hearing.defendants.[0].address.postCode", is(initiateHearingCommand.getHearing().getDefendants().get(0).getAddress().getPostCode())),
                                withJsonPath("$.hearing.defendants.[0].dateOfBirth", is(initiateHearingCommand.getHearing().getDefendants().get(0).getDateOfBirth().toString())),
                                withJsonPath("$.hearing.defendants.[0].defenceOrganisation", is(initiateHearingCommand.getHearing().getDefendants().get(0).getDefenceOrganisation())),
                                withJsonPath("$.hearing.defendants.[0].interpreter.needed", is(initiateHearingCommand.getHearing().getDefendants().get(0).getInterpreter().isNeeded())),
                                withJsonPath("$.hearing.defendants.[0].interpreter.language", is(initiateHearingCommand.getHearing().getDefendants().get(0).getInterpreter().getLanguage())),

                                withJsonPath("$.hearing.defendants.[0].defendantCases.[0].caseId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0).getCaseId().toString())),
                                withJsonPath("$.hearing.defendants.[0].defendantCases.[0].bailStatus", is(initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0).getBailStatus())),
                                withJsonPath("$.hearing.defendants.[0].defendantCases.[0].custodyTimeLimitDate", is(
                                        initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0).getCustodyTimeLimitDate().toString()
                                )),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].id", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].offenceCode", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getOffenceCode())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].wording", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getWording())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].section", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getSection())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].startDate", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getStartDate().toString())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].orderIndex", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getOrderIndex())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].count", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCount())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].convictionDate", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getConvictionDate().toString()))

                        ))).thatMatchesSchema()
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