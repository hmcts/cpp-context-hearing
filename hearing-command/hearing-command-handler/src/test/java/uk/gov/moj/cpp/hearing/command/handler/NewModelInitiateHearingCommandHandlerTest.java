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
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Address;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.Hearing;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.initiate.Plea;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
import uk.gov.moj.cpp.hearing.domain.event.CaseCreated;
import uk.gov.moj.cpp.hearing.domain.event.CaseHearingAdded;
import uk.gov.moj.cpp.hearing.domain.event.CaseOffenceAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.CourtAssigned;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjournDateUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.HearingVerdictUpdated;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.JudgeAssigned;
import uk.gov.moj.cpp.hearing.domain.event.OffenceCreated;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.PleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ResultAmended;
import uk.gov.moj.cpp.hearing.domain.event.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;
import uk.gov.moj.cpp.hearing.domain.event.VerdictAdded;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
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
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(MockitoJUnitRunner.class)
public class NewModelInitiateHearingCommandHandlerTest {

    @Mock
    private EventStream caseEventStream;

    @Mock
    private EventStream caseEventStream2;

    @Mock
    private EventStream offenceEventStream;

    @Mock
    private EventStream offenceEventStream2;

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
            //new events.
            Initiated.class,
            CaseCreated.class,
            CaseOffenceAdded.class,
            CaseHearingAdded.class,
            OffencePleaUpdated.class,

            OffenceCreated.class,
            //TODO - GPE-3032 CLEANUP - remove old events.
            DraftResultSaved.class, HearingInitiated.class, CaseAssociated.class, CourtAssigned.class,
            RoomBooked.class, ProsecutionCounselAdded.class, DefenceCounselAdded.class,
            HearingAdjournDateUpdated.class, ResultsShared.class, ResultAmended.class, PleaAdded.class, PleaChanged.class,
            HearingPleaAdded.class, HearingPleaChanged.class, HearingPleaUpdated.class, JudgeAssigned.class,
            VerdictAdded.class, ConvictionDateAdded.class, HearingVerdictUpdated.class, ConvictionDateRemoved.class);

    @Mock
    private HearingCommandHandler oldHandler;

    @InjectMocks
    private NewModelInitiateHearingCommandHandler hearingCommandHandler;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void givenNoPreviousCaseId_initiateHearing() throws Throwable {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        final UUID caseId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId();
        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        final UUID defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        setupMockedEventStream(flip(caseId), this.caseEventStream, new CaseAggregate());
        setupMockedEventStream(offenceId, this.offenceEventStream, new OffenceAggregate());
        setupMockedEventStream(hearingId, this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.hearingCommandHandler.initiate(command);

        List<JsonEnvelope> caseEvents = verifyAppendAndGetArgumentFrom(this.caseEventStream).collect(Collectors.toList());

        assertThat(caseEvents.size(), is(3));

        assertThat(caseEvents.get(0), jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName("hearing.case-created"),
                payloadIsJson(allOf(
                        withJsonPath("$.caseId", is(caseId.toString())),
                        withJsonPath("$.urn", is(initiateHearingCommand.getCases().get(0).getUrn()))
                ))).thatMatchesSchema()
        );

        assertThat(caseEvents.get(1), jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName("hearing.case-offence-added"),
                payloadIsJson(allOf(
                        withJsonPath("$.offenceId", is(offenceId.toString())),
                        withJsonPath("$.caseId", is(caseId.toString()))
                ))).thatMatchesSchema()
        );

        assertThat(caseEvents.get(2), jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName("hearing.case-hearing-added"),
                payloadIsJson(allOf(
                        withJsonPath("$.caseId", is(caseId.toString())),
                        withJsonPath("$.hearingId", is(hearingId.toString()))
                ))).thatMatchesSchema()
        );

        List<JsonEnvelope> offenceEvents = verifyAppendAndGetArgumentFrom(this.offenceEventStream).collect(Collectors.toList());

        assertThat(offenceEvents.size(), is(1));

        assertThat(offenceEvents.get(0), jsonEnvelope(
                withMetadataEnvelopedFrom(command)
                        .withName("hearing.offence-created"),
                payloadIsJson(allOf(
                        withJsonPath("$.offenceId", is(offenceId.toString())),
                        withJsonPath("$.caseId", is(caseId.toString())),
                        withJsonPath("$.defendantId", is(defendantId.toString()))
                ))).thatMatchesSchema()
        );

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.initiated"),
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
                                withJsonPath("$.hearing.defendants.[0].defendantCases.[0].custodyTimeLimitDate", is(initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0).getCustodyTimeLimitDate().toString())),


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
    public void givenAPreviousCaseId_initiateHearing_shouldGenerateOnlyOffenceAddedEvent() throws Throwable {

        InitiateHearingCommand initiateHearingCommand = with(initiateHearingCommandTemplate(), builder ->
                builder.getHearing().getDefendants().get(0).getOffences().get(0)
                        .withPlea(Plea.builder()
                                .withId(randomUUID())
                                .withPleaDate(PAST_LOCAL_DATE.next())
                                .withValue("GUILTY")
                        )
        ).build();

        final UUID caseId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId();
        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        setupMockedEventStream(flip(caseId), this.caseEventStream, new CaseAggregate(), aggregate -> {
            aggregate.apply(new CaseCreated(caseId, initiateHearingCommand.getCases().get(0).getUrn()));
        });
        setupMockedEventStream(offenceId, this.offenceEventStream, new OffenceAggregate());
        setupMockedEventStream(hearingId, this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.hearingCommandHandler.initiate(command);

        List<JsonEnvelope> results = verifyAppendAndGetArgumentFrom(this.caseEventStream).collect(Collectors.toList());

        assertThat(results.size(), is(2));

        assertThat(results.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.case-offence-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.caseId", equalTo(caseId.toString()))
                        ))).thatMatchesSchema()

        );

        assertThat(results.get(1),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.case-hearing-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.caseId", equalTo(caseId.toString()))
                        ))).thatMatchesSchema()

        );
    }

    @Test
    public void givenAPreviousCaseIdAndOffence_initiateHearing_shouldNotGenerateAnyCaseEvents() throws Throwable {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        final UUID caseId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId();
        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        final UUID defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        setupMockedEventStream(flip(caseId), this.caseEventStream, new CaseAggregate(), aggregate -> {
            aggregate.apply(new CaseCreated(caseId, initiateHearingCommand.getCases().get(0).getUrn()));
            aggregate.apply(new CaseOffenceAdded(offenceId, caseId));
        });
        setupMockedEventStream(offenceId, this.offenceEventStream, new OffenceAggregate(), offenceAggregate -> {
            offenceAggregate.apply(new OffenceCreated(offenceId, caseId, defendantId));
        });
        setupMockedEventStream(hearingId, this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.hearingCommandHandler.initiate(command);

        List<JsonEnvelope> results = verifyAppendAndGetArgumentFrom(this.caseEventStream).collect(Collectors.toList());

        assertThat(results.size(), is(1));

        assertThat(results.get(0),
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.case-hearing-added"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.caseId", equalTo(caseId.toString()))
                        ))).thatMatchesSchema()

        );

        List<JsonEnvelope> offenceEvents = verifyAppendAndGetArgumentFrom(this.offenceEventStream).collect(Collectors.toList());

        assertThat(offenceEvents.size(), is(0));
    }

    @Test
    public void givenAPreviousPlea_initiateHearing_shouldContainThePlea() throws Throwable {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        Plea plea = Plea.builder()
                .withId(randomUUID())
                .withOriginalHearingId(randomUUID())
                .withPleaDate(PAST_LOCAL_DATE.next())
                .withValue("GUILTY")
                .build();

        final UUID caseId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId();
        final UUID offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        final UUID defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();

        setupMockedEventStream(flip(caseId), this.caseEventStream, new CaseAggregate());
        setupMockedEventStream(offenceId, this.offenceEventStream, new OffenceAggregate(), offenceAggregate -> {
            offenceAggregate.apply(new OffenceCreated(offenceId, caseId, defendantId));
            offenceAggregate.apply(new OffencePleaUpdated(caseId, plea.getOriginalHearingId(), offenceId, plea.getId(), plea.getPleaDate(), plea.getValue()));
        });
        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.hearingCommandHandler.initiate(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.initiated"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearing.defendants.[0].offences.[0].plea.id", is(plea.getId().toString())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].plea.originalHearingId", is(plea.getOriginalHearingId().toString())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].plea.value", is(plea.getValue())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].plea.pleaDate", is(plea.getPleaDate().toString()))

                        ))).thatMatchesSchema()
        ));
    }

    @Test
    public void givenMultipleCases_initiateHearing_shouldHandle() throws Throwable {
        final InitiateHearingCommand initiateHearingCommand = with(initiateHearingCommandTemplate(), template -> {

            template.getCases().add(Case.builder().withCaseId(randomUUID()).withUrn(STRING.next()));


            template.getHearing().getDefendants().get(0).getOffences().add(
                    Offence.builder()
                            .withId(randomUUID())
                            .withCaseId(template.getCases().get(1).getCaseId())
                            .withOffenceCode(STRING.next())
                            .withWording(STRING.next())
                            .withSection(STRING.next())
                            .withStartDate(PAST_LOCAL_DATE.next())
                            .withEndDate(PAST_LOCAL_DATE.next())
                            .withOrderIndex(INTEGER.next())
                            .withCount(INTEGER.next())
                            .withConvictionDate(PAST_LOCAL_DATE.next())
            );
        }).build();

        setupMockedEventStream(flip(initiateHearingCommand.getCases().get(0).getCaseId()), this.caseEventStream, new CaseAggregate());
        setupMockedEventStream(flip(initiateHearingCommand.getCases().get(1).getCaseId()), this.caseEventStream2, new CaseAggregate());
        setupMockedEventStream(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId(), this.offenceEventStream, new OffenceAggregate());
        setupMockedEventStream(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId(), this.offenceEventStream2, new OffenceAggregate());
        setupMockedEventStream(initiateHearingCommand.getHearing().getId(), this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.hearingCommandHandler.initiate(command);

        with(verifyAppendAndGetArgumentFrom(this.caseEventStream), stream -> {
            List<JsonEnvelope> caseEvents = stream.collect(Collectors.toList());

            assertThat(caseEvents.size(), is(3));

            assertThat(caseEvents.get(0), jsonEnvelope(
                    withMetadataEnvelopedFrom(command)
                            .withName("hearing.case-created"),
                    payloadIsJson(allOf(
                            withJsonPath("$.caseId", is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                            withJsonPath("$.urn", is(initiateHearingCommand.getCases().get(0).getUrn()))
                    ))).thatMatchesSchema()
            );

            assertThat(caseEvents.get(1), jsonEnvelope(
                    withMetadataEnvelopedFrom(command)
                            .withName("hearing.case-offence-added"),
                    payloadIsJson(allOf(
                            withJsonPath("$.offenceId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                            withJsonPath("$.caseId", is(initiateHearingCommand.getCases().get(0).getCaseId().toString()))
                    ))).thatMatchesSchema()
            );

            assertThat(caseEvents.get(2), jsonEnvelope(
                    withMetadataEnvelopedFrom(command)
                            .withName("hearing.case-hearing-added"),
                    payloadIsJson(allOf(
                            withJsonPath("$.caseId", is(initiateHearingCommand.getCases().get(0).getCaseId().toString())),
                            withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))
                    ))).thatMatchesSchema()
            );
        });

        with(verifyAppendAndGetArgumentFrom(this.caseEventStream2), stream -> {
            List<JsonEnvelope> caseEvents = stream.collect(Collectors.toList());

            assertThat(caseEvents.size(), is(3));

            assertThat(caseEvents.get(0), jsonEnvelope(
                    withMetadataEnvelopedFrom(command)
                            .withName("hearing.case-created"),
                    payloadIsJson(allOf(
                            withJsonPath("$.caseId", is(initiateHearingCommand.getCases().get(1).getCaseId().toString())),
                            withJsonPath("$.urn", is(initiateHearingCommand.getCases().get(1).getUrn()))
                    ))).thatMatchesSchema()
            );

            assertThat(caseEvents.get(1), jsonEnvelope(
                    withMetadataEnvelopedFrom(command)
                            .withName("hearing.case-offence-added"),
                    payloadIsJson(allOf(
                            withJsonPath("$.offenceId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString())),
                            withJsonPath("$.caseId", is(initiateHearingCommand.getCases().get(1).getCaseId().toString()))
                    ))).thatMatchesSchema()
            );

            assertThat(caseEvents.get(2), jsonEnvelope(
                    withMetadataEnvelopedFrom(command)
                            .withName("hearing.case-hearing-added"),
                    payloadIsJson(allOf(
                            withJsonPath("$.caseId", is(initiateHearingCommand.getCases().get(1).getCaseId().toString())),
                            withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))
                    ))).thatMatchesSchema()
            );
        });


        with(verifyAppendAndGetArgumentFrom(this.offenceEventStream), stream -> {
            List<JsonEnvelope> offenceEvents = stream.collect(Collectors.toList());

            assertThat(offenceEvents.size(), is(1));

            assertThat(offenceEvents.get(0), jsonEnvelope(
                    withMetadataEnvelopedFrom(command)
                            .withName("hearing.offence-created"),
                    payloadIsJson(allOf(
                            withJsonPath("$.offenceId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                            withJsonPath("$.caseId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId().toString())),
                            withJsonPath("$.defendantId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString()))
                    ))).thatMatchesSchema()
            );
        });

        with(verifyAppendAndGetArgumentFrom(this.offenceEventStream2), stream -> {
            List<JsonEnvelope> offenceEvents = stream.collect(Collectors.toList());

            assertThat(offenceEvents.size(), is(1));

            assertThat(offenceEvents.get(0), jsonEnvelope(
                    withMetadataEnvelopedFrom(command)
                            .withName("hearing.offence-created"),
                    payloadIsJson(allOf(
                            withJsonPath("$.offenceId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getId().toString())),
                            withJsonPath("$.caseId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(1).getCaseId().toString())),
                            withJsonPath("$.defendantId", is(initiateHearingCommand.getHearing().getDefendants().get(0).getId().toString()))
                    ))).thatMatchesSchema()
            );
        });


    }

    private UUID flip(UUID id) {
        //TODO - GPE-3032 CLEANUP - get rid of this method.
        return new UUID(id.getLeastSignificantBits(), id.getMostSignificantBits());
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

    public static InitiateHearingCommand.Builder initiateHearingCommandTemplate() {
        UUID caseId = randomUUID();
        return InitiateHearingCommand.builder()
                .addCase(Case.builder()
                        .withCaseId(caseId)
                        .withUrn(STRING.next())
                )
                .withHearing(Hearing.builder()
                        .withId(randomUUID())
                        .withType(STRING.next())
                        .withCourtCentreId(randomUUID())
                        .withCourtCentreName(STRING.next())
                        .withCourtRoomId(randomUUID())
                        .withCourtRoomName(STRING.next())
                        .withJudge(
                                Judge.builder()
                                        .withId(randomUUID())
                                        .withTitle(STRING.next())
                                        .withFirstName(STRING.next())
                                        .withLastName(STRING.next())
                        )
                        .withStartDateTime(FUTURE_LOCAL_DATE.next())
                        .withNotBefore(false)
                        .withEstimateMinutes(INTEGER.next())
                        .addDefendant(Defendant.builder()
                                .withId(randomUUID())
                                .withPersonId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .withNationality(STRING.next())
                                .withGender(STRING.next())
                                .withAddress(
                                        Address.builder()
                                                .withAddress1(STRING.next())
                                                .withAddress2(STRING.next())
                                                .withAddress3(STRING.next())
                                                .withAddress4(STRING.next())
                                                .withPostCode(STRING.next())
                                )
                                .withDateOfBirth(PAST_LOCAL_DATE.next())
                                .withDefenceOrganisation(STRING.next())
                                .withInterpreter(
                                        Interpreter.builder()
                                                .withNeeded(false)
                                                .withLanguage(STRING.next())
                                )
                                .addDefendantCase(
                                        DefendantCase.builder()
                                                .withCaseId(caseId)
                                                .withBailStatus(STRING.next())
                                                .withCustodyTimeLimitDate(FUTURE_LOCAL_DATE.next())
                                )
                                .addOffence(
                                        Offence.builder()
                                                .withId(randomUUID())
                                                .withCaseId(caseId)
                                                .withOffenceCode(STRING.next())
                                                .withWording(STRING.next())
                                                .withSection(STRING.next())
                                                .withStartDate(PAST_LOCAL_DATE.next())
                                                .withEndDate(PAST_LOCAL_DATE.next())
                                                .withOrderIndex(INTEGER.next())
                                                .withCount(INTEGER.next())
                                                .withConvictionDate(PAST_LOCAL_DATE.next())
                                )
                        )
                );
    }
}