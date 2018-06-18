package uk.gov.moj.cpp.hearing.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.command.result.CourtClerk;
import uk.gov.moj.cpp.hearing.command.result.Level;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.Plea;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.GenerateNowsCommand;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Material;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.NowResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Nows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.PromptRefs;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;

import java.util.List;
import java.util.UUID;

import static com.jayway.jsonassert.impl.matcher.IsCollectionWithSize.hasSize;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMatcher.jsonEnvelope;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopeMetadataMatcher.metadata;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

public class PublishResultsEventProcessorTest {

    public static final String ARBITRARY_HEARING_START_DATE = "2016-06-01T10:00:00Z";
    @Mock
    private Sender sender;

    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Spy
    @InjectMocks
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();

    @Spy
    @InjectMocks
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);

    @Spy
    @InjectMocks
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @Mock
    private NowsDataProcessor nowsDataProcessor;

    @InjectMocks
    private PublishResultsEventProcessor publishResultsEventProcessor;


    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void resultsShared() {

        ResultsShared resultsShared = resultsSharedTemplate();
        Defendant defendant = resultsShared.getHearing().getDefendants().get(0);
        Offence offence = resultsShared.getHearing().getDefendants().get(0).getOffences().get(0);
        DefenceCounselUpsert defenceCounselUpsert = resultsShared.getDefenceCounsels().values().iterator().next();
        ProsecutionCounselUpsert prosecutionCounselUpsert = resultsShared.getProsecutionCounsels().values().iterator().next();
        Plea plea = resultsShared.getPleas().values().iterator().next();
        VerdictUpsert verdict = resultsShared.getVerdicts().values().iterator().next();
        CompletedResultLineStatus completedResultLineStatus = resultsShared.getCompletedResultLinesStatus().values().iterator().next();

        final List<Nows> nows = asList(
                Nows.nows()
                        .setDefendantId(UUID.randomUUID().toString())
                        .setId(UUID.randomUUID())
                        .setMaterial(
                                asList(
                                        Material.material()
                                                .setId(UUID.randomUUID())
                                                .setLanguage("Welsh")
                                                .setNowResult(
                                                        asList(
                                                                NowResult.nowResult()
                                                                        .setSharedResultId(UUID.randomUUID())
                                                                        .setSequence(123)
                                                                        .setPromptRefs(
                                                                                asList(
                                                                                        PromptRefs.promptRefs()
                                                                                                .setLabel("label1"))
                                                                        )
                                                        )
                                                )
                                )
                        )
                        .setNowsTypeId(UUID.randomUUID().toString())
        );
        uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing hearing = new uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows.Hearing().setId(UUID.randomUUID());
        Mockito.when(nowsDataProcessor.createNows(Mockito.any())).thenReturn(nows);
        Mockito.when(nowsDataProcessor.translateReferenceData(resultsShared)).thenReturn(hearing);

        CompletedResultLine resultLine = resultsShared.getCompletedResultLines().get(0);

        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared));
        Mockito.when(jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ResultsShared.class)).thenReturn(resultsShared);

        publishResultsEventProcessor.resultsShared(event);

        verify(this.sender, times(3)).send(this.envelopeArgumentCaptor.capture());

        List<JsonEnvelope> outgoingMessages = envelopeArgumentCaptor.getAllValues();

        JsonEnvelope createNowsMessage = outgoingMessages.get(0);
        assertThat(createNowsMessage, jsonEnvelope(
                metadata().withName("hearing.command.generate-nows.v2"),
                payloadIsJson(allOf(
                        withJsonPath("$.hearing.id", is(hearing.getId().toString()))))
        ));

        JsonEnvelope updateResultLineStatusMessage = outgoingMessages.get(1);
        assertThat(updateResultLineStatusMessage, jsonEnvelope(
                metadata().withName("hearing.command.update-result-lines-status"),
                payloadIsJson(allOf(
                        withJsonPath("$.sharedResultLines", hasSize(0))))
        ));

        //deserialise
        GenerateNowsCommand generateNowsCommandOut = jsonObjectToObjectConverter.convert(createNowsMessage.payloadAsJsonObject(), GenerateNowsCommand.class);
        assertThat(generateNowsCommandOut.getHearing().getId(), is(hearing.getId()));
        final Nows nowsIn = nows.get(0);
        final Nows nowsOut = generateNowsCommandOut.getHearing().getNows().get(0);
        assertThat(nowsOut.getId(), is(nowsIn.getId()));
        assertThat(nowsOut.getDefendantId(), is(nowsIn.getDefendantId()));
        assertThat(nowsOut.getNowsTypeId(), is(nowsIn.getNowsTypeId()));
        final Material materialIn = nowsIn.getMaterial().get(0);
        final Material materialOut = nowsOut.getMaterial().get(0);
        assertThat(materialIn.getId(), is(materialOut.getId()));
        assertThat(materialIn.getLanguage(), is(materialOut.getLanguage()));
        assertThat(materialIn.getNowResult().get(0).getSharedResultId(), is(materialOut.getNowResult().get(0).getSharedResultId()));
        assertThat(materialIn.getNowResult().get(0).getPromptRefs().get(0).getLabel(), is(materialOut.getNowResult().get(0).getPromptRefs().get(0).getLabel()));
        final JsonEnvelope shareMessage = outgoingMessages.get(2);



        assertThat(
                shareMessage, jsonEnvelope(
                        metadata().withName("public.hearing.resulted"),
                        payloadIsJson(allOf(
                                withJsonPath("$.hearing.id", is(resultsShared.getHearingId().toString())),
                                withJsonPath("$.hearing.hearingType", is(resultsShared.getHearing().getType())),
                                withJsonPath("$.hearing.courtCentre.courtCentreId", is(resultsShared.getHearing().getCourtCentreId().toString())),
                                withJsonPath("$.hearing.courtCentre.courtCentreName", is(resultsShared.getHearing().getCourtCentreName())),
                                withJsonPath("$.hearing.courtCentre.courtRoomId", is(resultsShared.getHearing().getCourtRoomId().toString())),
                                withJsonPath("$.hearing.courtCentre.courtRoomName", is(resultsShared.getHearing().getCourtRoomName())),
                                withJsonPath("$.hearing.startDateTime", is(ZonedDateTimes.toString(resultsShared.getHearing().getHearingDays().get(0)))),

                                withJsonPath("$.hearing.attendees[0].personId", is(resultsShared.getCourtClerk().getId().toString())),
                                withJsonPath("$.hearing.attendees[0].type", is("COURTCLERK")),
                                withJsonPath("$.hearing.attendees[0].firstName", is(resultsShared.getCourtClerk().getFirstName())),
                                withJsonPath("$.hearing.attendees[0].lastName", is(resultsShared.getCourtClerk().getLastName())),

                                withJsonPath("$.hearing.attendees[1].type", is("JUDGE")),

                                withJsonPath("$.hearing.attendees[1].personId", is(resultsShared.getHearing().getJudge().getId().toString())),
                                withJsonPath("$.hearing.attendees[1].firstName", is(resultsShared.getHearing().getJudge().getFirstName())),
                                withJsonPath("$.hearing.attendees[1].lastName", is(resultsShared.getHearing().getJudge().getLastName())),
                                withJsonPath("$.hearing.attendees[1].title", is(resultsShared.getHearing().getJudge().getTitle())),

                                withJsonPath("$.hearing.attendees[2].type", is("DEFENCEADVOCATE")),
                                withJsonPath("$.hearing.attendees[2].firstName", is(defenceCounselUpsert.getFirstName())),
                                withJsonPath("$.hearing.attendees[2].lastName", is(defenceCounselUpsert.getLastName())),
                                withJsonPath("$.hearing.attendees[2].title", is(defenceCounselUpsert.getTitle())),

                                withJsonPath("$.hearing.attendees[3].type", is("PROSECUTIONADVOCATE")),
                                withJsonPath("$.hearing.attendees[3].personId", is(prosecutionCounselUpsert.getPersonId().toString())),
                                withJsonPath("$.hearing.attendees[3].firstName", is(prosecutionCounselUpsert.getFirstName())),
                                withJsonPath("$.hearing.attendees[3].lastName", is(prosecutionCounselUpsert.getLastName())),
                                withJsonPath("$.hearing.attendees[3].title", is(prosecutionCounselUpsert.getTitle())),

                                withJsonPath("$.hearing.defendants[0].id", is(defendant.getId().toString())),
                                withJsonPath("$.hearing.defendants[0].person.id", is(defendant.getPersonId().toString())),
                                withJsonPath("$.hearing.defendants[0].person.firstName", is(defendant.getFirstName())),
                                withJsonPath("$.hearing.defendants[0].person.lastName", is(defendant.getLastName())),
                                withJsonPath("$.hearing.defendants[0].person.dateOfBirth", is(defendant.getDateOfBirth().toString())),
                                withJsonPath("$.hearing.defendants[0].person.address.address1", is(defendant.getAddress().getAddress1())),
                                withJsonPath("$.hearing.defendants[0].person.address.address2", is(defendant.getAddress().getAddress2())),
                                withJsonPath("$.hearing.defendants[0].person.address.address3", is(defendant.getAddress().getAddress3())),
                                withJsonPath("$.hearing.defendants[0].person.address.address4", is(defendant.getAddress().getAddress4())),
                                withJsonPath("$.hearing.defendants[0].person.address.postCode", is(defendant.getAddress().getPostCode())),
                                withJsonPath("$.hearing.defendants[0].person.nationality", is(defendant.getNationality())),
                                withJsonPath("$.hearing.defendants[0].person.gender", is(defendant.getGender())),
                                withJsonPath("$.hearing.defendants[0].person.homeTelephone", is("")),
                                withJsonPath("$.hearing.defendants[0].person.workTelephone", is("")),
                                withJsonPath("$.hearing.defendants[0].person.mobile", is("")),
                                withJsonPath("$.hearing.defendants[0].person.fax", is("")),
                                withJsonPath("$.hearing.defendants[0].person.email", is("")),
                                withJsonPath("$.hearing.defendants[0].defenceOrganisation", is(defendant.getDefenceOrganisation())),
                                withJsonPath("$.hearing.defendants[0].interpreter.language", is(defendant.getInterpreter().getLanguage())),
                                withJsonPath("$.hearing.defendants[0].cases[0].id", is(defendant.getDefendantCases().get(0).getCaseId().toString())),
                                withJsonPath("$.hearing.defendants[0].cases[0].bailStatus", is(defendant.getDefendantCases().get(0).getBailStatus())),
                                withJsonPath("$.hearing.defendants[0].cases[0].urn", is(resultsShared.getCases().get(0).getUrn())),

                                withJsonPath("$.hearing.defendants[0].cases[0].custodyTimeLimitDate", is(
                                        defendant.getDefendantCases().get(0).getCustodyTimeLimitDate().toString()
                                )),

                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].id", is(offence.getId().toString())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].code", is(offence.getOffenceCode())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].convictionDate", is(offence.getConvictionDate().toString())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].wording", is(offence.getWording())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].startDate", is(offence.getStartDate().toString())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].endDate", is(offence.getEndDate().toString())),

                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].plea.id", is(plea.getOffenceId().toString())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].plea.date", is(plea.getPleaDate().toString())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].plea.value", is(plea.getValue())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].plea.enteredHearingId", is(plea.getOriginHearingId().toString())),

                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.verdictDescription", is(verdict.getDescription())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.verdictCategory", is(verdict.getCategory())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.numberOfSplitJurors", is(
                                        format("%s-%s", verdict.getNumberOfJurors() - verdict.getNumberOfSplitJurors(), verdict.getNumberOfSplitJurors())
                                )),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.numberOfJurors", is(verdict.getNumberOfJurors())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.unanimous", is(verdict.getUnanimous())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.enteredHearingId", is(verdict.getHearingId().toString())),
                                withJsonPath("$.hearing.defendants[0].cases[0].offences[0].verdict.verdictDate", is(verdict.getVerdictDate().toString())),

                                withJsonPath("$.hearing.sharedResultLines[0].id", is(resultLine.getId().toString())),
                                withJsonPath("$.hearing.sharedResultLines[0].caseId", is(resultLine.getCaseId().toString())),
                                withJsonPath("$.hearing.sharedResultLines[0].defendantId", is(resultLine.getDefendantId().toString())),
                                withJsonPath("$.hearing.sharedResultLines[0].offenceId", is(resultLine.getOffenceId().toString())),
                                withJsonPath("$.hearing.sharedResultLines[0].level", is(resultLine.getLevel().toString())),
                                withJsonPath("$.hearing.sharedResultLines[0].label", is(resultLine.getResultLabel())),
                                withJsonPath("$.hearing.sharedResultLines[0].prompts[0].label", is(resultLine.getPrompts().get(0).getLabel())),
                                withJsonPath("$.hearing.sharedResultLines[0].prompts[0].value", is(resultLine.getPrompts().get(0).getValue())),
                                withJsonPath("$.hearing.sharedResultLines[0].courtClerk.id", is(completedResultLineStatus.getCourtClerk().getId().toString())),
                                withJsonPath("$.hearing.sharedResultLines[0].courtClerk.firstName", is(completedResultLineStatus.getCourtClerk().getFirstName())),
                                withJsonPath("$.hearing.sharedResultLines[0].courtClerk.lastName", is(completedResultLineStatus.getCourtClerk().getLastName()))

                                )
                        )
                )
        );
    }

    private ResultsShared resultsSharedTemplate() {

        InitiateHearingCommandHelper hearingOne = new InitiateHearingCommandHelper(standardInitiateHearingTemplate());
        UUID completedResultLineId = randomUUID();
        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withSharedTime(PAST_ZONED_DATE_TIME.next())
                .withHearing(hearingOne.it().getHearing())
                .withCases(hearingOne.it().getCases())
                .withDefenceCounsels(ImmutableMap.of(randomUUID(), DefenceCounselUpsert.builder()
                        .withHearingId(hearingOne.getHearingId())
                        .withPersonId(randomUUID())
                        .withAttendeeId(randomUUID())
                        .withDefendantIds(asList(randomUUID()))
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .withStatus(STRING.next())
                        .withTitle(STRING.next())
                        .build()
                ))
                .withProsecutionCounsels(ImmutableMap.of(randomUUID(), ProsecutionCounselUpsert.builder()
                        .withAttendeeId(randomUUID())
                        .withHearingId(hearingOne.getHearingId())
                        .withPersonId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .withStatus(STRING.next())
                        .withTitle(STRING.next())
                        .build()
                ))
                .withPleas(ImmutableMap.of(randomUUID(), Plea.plea()
                        .setOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .setOriginHearingId(hearingOne.getHearingId())
                        .setPleaDate(PAST_LOCAL_DATE.next())
                        .setValue(STRING.next())
                ))
                .withVerdicts(ImmutableMap.of(randomUUID(), VerdictUpsert.builder()
                        .withVerdictId(randomUUID())
                        .withCategory(STRING.next())
                        .withCode(STRING.next())
                        .withDescription(STRING.next())
                        .withNumberOfJurors(RandomGenerator.integer(9, 12).next())
                        .withNumberOfSplitJurors(RandomGenerator.integer(0, 3).next())
                        .withVerdictDate(PAST_LOCAL_DATE.next())
                        .withVerdictValueId(randomUUID())
                        .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .withUnanimous(BOOLEAN.next())
                        .withHearingId(hearingOne.getHearingId())
                        .build()))
                .withCourtClerk(CourtClerk.builder()
                        .withId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLines(asList(
                        CompletedResultLine.builder()
                                .withId(completedResultLineId)
                                .withCaseId(hearingOne.getFirstCaseId())
                                .withDefendantId(hearingOne.getFirstDefendantId())
                                .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                                .withLevel(Level.CASE)
                                .withResultDefinitionId(randomUUID())
                                .withResultLabel(STRING.next())
                                .withResultPrompts(asList(ResultPrompt.builder()
                                        .withLabel(STRING.next())
                                        .withValue(STRING.next())
                                        .build()))
                                .build()
                ))
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(CourtClerk.builder()
                                .withId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next())
                        .build()
                ))
                .build();
    }
}