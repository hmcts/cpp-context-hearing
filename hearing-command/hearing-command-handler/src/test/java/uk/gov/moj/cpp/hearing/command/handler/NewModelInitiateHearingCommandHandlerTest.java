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
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Interpreter;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;
import uk.gov.moj.cpp.hearing.domain.event.CaseAssociated;
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
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffenceEnriched;
import uk.gov.moj.cpp.hearing.domain.event.InitiateHearingOffencePlead;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.JudgeAssigned;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.PleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.PleaChanged;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ResultAmended;
import uk.gov.moj.cpp.hearing.domain.event.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.RoomBooked;
import uk.gov.moj.cpp.hearing.domain.event.VerdictAdded;

import java.time.LocalDate;
import java.util.UUID;

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
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.FUTURE_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

@RunWith(MockitoJUnitRunner.class)
public class NewModelInitiateHearingCommandHandlerTest {

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventStream offenceEventStream;

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
            InitiateHearingOffenceEnriched.class,
            InitiateHearingOffencePlead.class,

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
    public void initiateHearing() throws Throwable {

        InitiateHearingCommand initiateHearingCommand = initiateHearingCommandTemplate().build();

        final UUID caseId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCaseId();
        final UUID hearingId = initiateHearingCommand.getHearing().getId();

        setupMockedEventStream(hearingId, this.hearingEventStream, new NewModelHearingAggregate());

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(initiateHearingCommand));

        this.hearingCommandHandler.initiate(command);

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

        OffenceAggregate offenceAggregate = new OffenceAggregate();
        offenceAggregate.apply(new OffencePleaUpdated(originHearingId, offenceId, pleaDate, value));
        setupMockedEventStream(offenceId, this.offenceEventStream, offenceAggregate);

        this.hearingCommandHandler.initiateHearingOffence(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.offenceEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.initiate-hearing-offence-enriched"),
                        payloadIsJson(allOf(
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.defendantId", is(defendantId.toString())),
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.caseId", is(caseId.toString())),
                                withJsonPath("$.originHearingId", is(originHearingId.toString())),
                                withJsonPath("$.pleaDate", is(pleaDate.toString())),
                                withJsonPath("$.value", is(value))
                        ))).thatMatchesSchema()

        ));
    }

    @Test
    public void initiateHearingOffencePlea() throws Throwable {

        InitiateHearingOffencePleaCommand input =
                new InitiateHearingOffencePleaCommand(randomUUID(), randomUUID(), randomUUID(), randomUUID(), randomUUID(), PAST_LOCAL_DATE.next(), "GUILTY");

        final JsonEnvelope command = envelopeFrom(metadataWithRandomUUID("hearing.initiate"), objectToJsonObjectConverter.convert(input));

        setupMockedEventStream(input.getHearingId(), this.hearingEventStream, new NewModelHearingAggregate());

        this.hearingCommandHandler.initiateHearingOffencePlea(command);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(command)
                                .withName("hearing.initiate-hearing-offence-plead"),
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


    private <T extends Aggregate> void setupMockedEventStream(UUID id, EventStream eventStream, T aggregate) {
        when(this.eventSource.getStreamById(id)).thenReturn(eventStream);
        Class<T> clz = (Class<T>) aggregate.getClass();
        when(this.aggregateService.get(eventStream, clz)).thenReturn(aggregate);
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