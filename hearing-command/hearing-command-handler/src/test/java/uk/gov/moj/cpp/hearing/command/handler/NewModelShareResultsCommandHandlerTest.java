package uk.gov.moj.cpp.hearing.command.handler;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataOf;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.withMetadataEnvelopedFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeStreamMatcher.streamContaining;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.command.result.ResultLine;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.Initiated;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

@SuppressWarnings({"serial","unchecked"})
@RunWith(MockitoJUnitRunner.class)
public class NewModelShareResultsCommandHandlerTest {

    @InjectMocks 
    private NewModelShareResultsCommandHandler shareResultsCommandHandler;

    @Mock
    private EventStream caseEventStream;

    @Mock
    private EventStream hearingEventStream;

    @Mock
    private EventSource eventSource;

    @Mock
    private AggregateService aggregateService;

    @Mock
    private Clock clock;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(DraftResultSaved.class, ResultsShared.class);

    private static InitiateHearingCommand initiateHearingCommand;
    private static ProsecutionCounselUpsert prosecutionCounselUpsert;
    private static DefenceCounselUpsert defenceCounselUpsert;
    private static UUID hearingId;
    private static UUID defendantId;
    private static UUID targetId;
    private static UUID offenceId;
    private static UUID medatadaId;
    private static UUID resultDefinitionId;
    private static UUID resultLineId1;
    private static UUID personId;
    private static UUID caseId;
    private static String draftResult;
    private static String resultLabel;
    private static String promptLabel1;
    private static String promptValue1;
    private static String promptLabel2;
    private static String promptValue2;
    private static Level level;
    private static ZonedDateTime sharedTime;

    @BeforeClass
    public static void init() {
        initiateHearingCommand = TestTemplates.initiateHearingCommandTemplate().build();
        hearingId = initiateHearingCommand.getHearing().getId();
        defendantId = initiateHearingCommand.getHearing().getDefendants().get(0).getId();
        targetId = UUID.randomUUID();
        offenceId = initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId();
        medatadaId = UUID.randomUUID();
        resultDefinitionId = UUID.randomUUID();
        resultLineId1 = UUID.randomUUID();
        personId = initiateHearingCommand.getHearing().getDefendants().get(0).getPersonId();
        caseId = initiateHearingCommand.getCases().get(0).getCaseId();
        draftResult = RandomGenerator.STRING.next();
        resultLabel = RandomGenerator.STRING.next();
        promptLabel1 = RandomGenerator.STRING.next();
        promptValue1 = RandomGenerator.STRING.next();
        promptLabel2 = RandomGenerator.STRING.next();
        promptValue2 = RandomGenerator.STRING.next();
        level = RandomGenerator.values(Level.CASE, Level.DEFENDANT, Level.OFFENCE).next();
        sharedTime = new UtcClock().now();
        prosecutionCounselUpsert = ProsecutionCounselUpsert.builder()
            .withHearingId(hearingId)
            .withPersonId(randomUUID())
            .withAttendeeId(randomUUID())
            .withTitle(STRING.next())
            .withFirstName(STRING.next())
            .withLastName(STRING.next())
            .withStatus(STRING.next())
            .build();
        defenceCounselUpsert = DefenceCounselUpsert.builder()
            .withHearingId(hearingId)
            .withPersonId(randomUUID())
            .withAttendeeId(randomUUID())
            .withTitle(STRING.next())
            .withFirstName(STRING.next())
            .withLastName(STRING.next())
            .withStatus(STRING.next())
            .withDefendantIds(asList(defendantId))
            .build();
    }

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        when(this.eventSource.getStreamById(hearingId)).thenReturn(this.hearingEventStream);
        when(this.clock.now()).thenReturn(sharedTime);
    }

    @Test
    public void shouldRaiseDraftResultSaved() throws Exception {

        final NewModelHearingAggregate aggregate = new NewModelHearingAggregate() {{
            apply(Stream.of(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing())));
            apply(Stream.of(prosecutionCounselUpsert));
            apply(Stream.of(defenceCounselUpsert));
         }};

        when(this.aggregateService.get(this.hearingEventStream, NewModelHearingAggregate.class)).thenReturn(aggregate);

        final SaveDraftResultCommand saveDraftResultCommand = SaveDraftResultCommand.builder()
                .withHearingId(hearingId)
                .withDefendantId(defendantId)
                .withOffenceId(offenceId)
                .withTargetId(targetId)
                .withDraftResult(draftResult)
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataOf(medatadaId, "hearing.save-draft-result"), objectToJsonObjectConverter.convert(saveDraftResultCommand));

        this.shareResultsCommandHandler.saveDraftResult(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(envelope)
                                .withName("hearing.draft-result-saved"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.defendantId", is(defendantId.toString())),
                                withJsonPath("$.targetId", is(targetId.toString())),
                                withJsonPath("$.offenceId", is(offenceId.toString())),
                                withJsonPath("$.draftResult", is(draftResult))
                        ))
                )
        ));
    }

    @Test
    public void shouldRaiseResultsSharedEventIfSharingForTheFirstTime() throws Exception {

        final NewModelHearingAggregate aggregate = new NewModelHearingAggregate() {{
            apply(Stream.of(new Initiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing())));
            apply(Stream.of(prosecutionCounselUpsert));
            apply(Stream.of(defenceCounselUpsert)); 
        }};

        when(this.aggregateService.get(this.hearingEventStream, NewModelHearingAggregate.class)).thenReturn(aggregate);

        final ShareResultsCommand shareResultsCommand = ShareResultsCommand.builder()
                .withHearingId(hearingId)
                .withResultLines(asList(ResultLine.builder()
                            .withId(resultLineId1)
                            .withResultDefinitionId(resultDefinitionId)
                            .withPersonId(personId)
                            .withOffenceId(offenceId)
                            .withCaseId(caseId)
                            .withLevel(level)
                            .withResultLabel(resultLabel)
                            .withComplete(true)
                            .withPrompts(asList(ResultPrompt.builder()
                                        .withLabel(promptLabel1)
                                        .withValue(promptValue1)
                                        .build(),
                                    ResultPrompt.builder()
                                        .withLabel(promptLabel2)
                                        .withValue(promptValue2)
                                        .build()))
                            .build(),
                         ResultLine.builder()
                            .withId(UUID.randomUUID())
                            .withResultDefinitionId(resultDefinitionId)
                            .withPersonId(UUID.randomUUID())
                            .withOffenceId(UUID.randomUUID())
                            .withCaseId(UUID.randomUUID())
                            .withLevel(level)
                            .withResultLabel(resultLabel)
                            .withComplete(false)
                            .withPrompts(asList(ResultPrompt.builder()
                                        .withLabel(promptLabel1)
                                        .withValue(promptValue1)
                                        .build(),
                                    ResultPrompt.builder()
                                        .withLabel(promptLabel2)
                                        .withValue(promptValue2)
                                        .build()))
                            .build()))
                .build();

        final JsonEnvelope envelope = envelopeFrom(metadataOf(medatadaId, "hearing.share-results"), objectToJsonObjectConverter.convert(shareResultsCommand));

        this.shareResultsCommandHandler.shareResult(envelope);

        assertThat(verifyAppendAndGetArgumentFrom(this.hearingEventStream), streamContaining(
                jsonEnvelope(
                        withMetadataEnvelopedFrom(envelope)
                                .withName("hearing.results-shared"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearingId", is(hearingId.toString())),
                                withJsonPath("$.sharedTime", is(ZonedDateTimes.toString(sharedTime))),
                                withJsonPath("$.resultLines[0].id", is(resultLineId1.toString())),
                                withoutJsonPath("$.resultLines[0].lastSharedResultId"),
                                withJsonPath("$.resultLines[0].personId", is(personId.toString())),
                                withJsonPath("$.resultLines[0].offenceId", is(offenceId.toString())),
                                withJsonPath("$.resultLines[0].caseId", is(caseId.toString())),
                                withJsonPath("$.resultLines[0].level", is(level.name())),
                                withJsonPath("$.resultLines[0].resultLabel", is(resultLabel)),
                                withJsonPath("$.resultLines[0].prompts[0].label", is(promptLabel1)),
                                withJsonPath("$.resultLines[0].prompts[0].value", is(promptValue1)),
                                withJsonPath("$.resultLines[0].prompts[1].label", is(promptLabel2)),
                                withJsonPath("$.resultLines[0].prompts[1].value", is(promptValue2)),
                                
                                withJsonPath("$.cases.[0].caseId", is(caseId.toString())),
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
                                withJsonPath("$.hearing.defendants.[0].defendantCases.[0].custodyTimeLimitDate", is(initiateHearingCommand.getHearing().getDefendants().get(0).getDefendantCases().get(0).getCustodyTimeLimitDate().toLocalDateTime().atZone(ZoneId.of("Z")).toString())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].id", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getId().toString())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].offenceCode", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getOffenceCode())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].wording", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getWording())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].section", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getSection())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].startDate", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getStartDate().toString())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].orderIndex", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getOrderIndex())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].count", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getCount())),
                                withJsonPath("$.hearing.defendants.[0].offences.[0].convictionDate", is(initiateHearingCommand.getHearing().getDefendants().get(0).getOffences().get(0).getConvictionDate().toString())),

                                withJsonPath("$.hearing.witnesses.[0].type", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getType())),
                                withJsonPath("$.hearing.witnesses.[0].classification", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getClassification())),
                                withJsonPath("$.hearing.witnesses.[0].caseId", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getCaseId().toString())),
                                withJsonPath("$.hearing.witnesses.[0].personId", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getPersonId().toString())),
                                withJsonPath("$.hearing.witnesses.[0].title", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getTitle())),
                                withJsonPath("$.hearing.witnesses.[0].firstName", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getFirstName())),
                                withJsonPath("$.hearing.witnesses.[0].lastName", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getLastName())),
                                withJsonPath("$.hearing.witnesses.[0].nationality", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getNationality())),
                                withJsonPath("$.hearing.witnesses.[0].gender", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getGender())),
                                withJsonPath("$.hearing.witnesses.[0].dateOfBirth", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getDateOfBirth().toString())),
                                withJsonPath("$.hearing.witnesses.[0].homeTelephone", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getHomeTelephone())),
                                withJsonPath("$.hearing.witnesses.[0].workTelephone", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getWorkTelephone())),
                                withJsonPath("$.hearing.witnesses.[0].fax", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getFax())),
                                withJsonPath("$.hearing.witnesses.[0].mobile", is(initiateHearingCommand.getHearing().getWitnesses().get(0).getMobile())),

                                withJsonPath("$.prosecutionCounsels."+ prosecutionCounselUpsert.getAttendeeId() +".hearingId", is(prosecutionCounselUpsert.getHearingId().toString())),
                                withJsonPath("$.prosecutionCounsels."+ prosecutionCounselUpsert.getAttendeeId() +".personId", is(prosecutionCounselUpsert.getPersonId().toString())),
                                withJsonPath("$.prosecutionCounsels."+ prosecutionCounselUpsert.getAttendeeId() +".attendeeId", is(prosecutionCounselUpsert.getAttendeeId().toString())),
                                withJsonPath("$.prosecutionCounsels."+ prosecutionCounselUpsert.getAttendeeId() +".title", is(prosecutionCounselUpsert.getTitle())),
                                withJsonPath("$.prosecutionCounsels."+ prosecutionCounselUpsert.getAttendeeId() +".firstName", is(prosecutionCounselUpsert.getFirstName())),
                                withJsonPath("$.prosecutionCounsels."+ prosecutionCounselUpsert.getAttendeeId() +".lastName", is(prosecutionCounselUpsert.getLastName())),
                                withJsonPath("$.prosecutionCounsels."+ prosecutionCounselUpsert.getAttendeeId() +".status", is(prosecutionCounselUpsert.getStatus())),

                                withJsonPath("$.defenceCounsels." + defenceCounselUpsert.getAttendeeId() + ".hearingId", is(defenceCounselUpsert.getHearingId().toString())),
                                withJsonPath("$.defenceCounsels." + defenceCounselUpsert.getAttendeeId() + ".personId", is(defenceCounselUpsert.getPersonId().toString())),
                                withJsonPath("$.defenceCounsels." + defenceCounselUpsert.getAttendeeId() + ".attendeeId", is(defenceCounselUpsert.getAttendeeId().toString())),
                                withJsonPath("$.defenceCounsels." + defenceCounselUpsert.getAttendeeId() + ".title", is(defenceCounselUpsert.getTitle())),
                                withJsonPath("$.defenceCounsels." + defenceCounselUpsert.getAttendeeId() + ".firstName", is(defenceCounselUpsert.getFirstName())),
                                withJsonPath("$.defenceCounsels." + defenceCounselUpsert.getAttendeeId() + ".lastName", is(defenceCounselUpsert.getLastName())),
                                withJsonPath("$.defenceCounsels." + defenceCounselUpsert.getAttendeeId() + ".status", is(defenceCounselUpsert.getStatus())),
                                withJsonPath("$.defenceCounsels." + defenceCounselUpsert.getAttendeeId() + ".defendantIds.[0]", is(defenceCounselUpsert.getDefendantIds().get(0).toString()))
                        ))
                )
        ));
    }
}