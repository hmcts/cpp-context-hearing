package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.services.integrationtest.utils.jms.JmsMessageConsumerClientProvider.newPublicJmsMessageConsumerClientProvider;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResultsPerDay;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandV2Template;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataResultDefinitionsWithResultTexts;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.integrationtest.utils.jms.JmsMessageConsumerClient;
import uk.gov.justice.services.integrationtest.utils.jms.JmsResourceManagementExtension;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.TestUtilities;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;

@ExtendWith(JmsResourceManagementExtension.class)
public class ResultTextIT extends AbstractIT {

    private static final long RETRIEVE_TIMEOUT = 90000;

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    protected  final JsonObjectToObjectConverter jsonToObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    @Spy
    private StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    private final static LocalDate orderedDate = LocalDate.of(2000, 04, 02);
    private final static LocalDate orderedDate2 = LocalDate.of(2000, 02, 02);


    final JmsMessageConsumerClient jmsMessageConsumerClient = newPublicJmsMessageConsumerClientProvider()
            .withEventNames("public.events.hearing.hearing-resulted").getMessageConsumerClient(); //No need to call startConsumer() explicitly


    @BeforeAll
    public static void setupBeforeClass() {
        stubGetReferenceDataResultDefinitionsWithResultTexts(orderedDate);
        stubGetReferenceDataResultDefinitionsWithResultTexts(orderedDate2);

    }

    @AfterEach
    public void clear(){
        jmsMessageConsumerClient.clearMessages();
    }

    @Test
    public void shouldSetResultTextForPrompts() throws IOException {

        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        stubCourtRoom(hearing);

        final String eventPayloadString = getStringFromResource("hearing.share-result-with-target-with-all-prompts-result.json")
                .replaceAll("HEARING_ID", hearing.getId().toString())
                .replaceAll("CASE_ID", hearing.getProsecutionCases().get(0).getId().toString())
                .replaceAll("ORDERED_DATE", orderedDate.toString())
                .replaceAll("OFFENCE_ID", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString());

        final Target target = jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(eventPayloadString), Target.class);
        final List<Target> targets = new ArrayList<>();
        targets.add(target);


        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        final Optional<JsonObject> message = jmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(RETRIEVE_TIMEOUT).map(JsonEnvelope::payloadAsJsonObject);
        final var publicHearingResultedV2 = jsonToObjectConverter.convert(message.get(), PublicHearingResultedV2.class);

        assertThat(publicHearingResultedV2.getHearing().getId(), is(hearing.getId()));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getResultText(), is("SUSPS - Suspended sentence order - imprisonment\nCommitted to prison for 7 Months Concurrent consecutive to offence of2121 which is on case number tfl3434 suspended. Reason: failure to express willingness to comply with a proposed requirement for a community order. Reason for custody: the defendant has a flagrant disregard for court orders, the defendant has a flagrant disregard for people and their property. The defendant must comply with the requirements (shown below) within the supervision period. This order is to be reviewed weekly starting on 28/12/2022 at 16:00 at Wycombe Magistrates' Court.  In the event of activation of sentence: 20 bail remand days to count. Total custodial period 7 Months suspended for 5 Months . "));
    }

    @Test
    public void shouldSetResultTextForSimpleResult() throws IOException {

        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        stubCourtRoom(hearing);

        final String eventPayloadString = getStringFromResource("hearing.share-result-with-target-with-simple-result.json")
                .replaceAll("HEARING_ID", hearing.getId().toString())
                .replaceAll("CASE_ID", hearing.getProsecutionCases().get(0).getId().toString())
                .replaceAll("ORDERED_DATE", orderedDate.toString())
                .replaceAll("OFFENCE_ID", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString());

        final Target target = jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(eventPayloadString), Target.class);
        final List<Target> targets = new ArrayList<>();
        targets.add(target);

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        final Optional<JsonObject> message = jmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(RETRIEVE_TIMEOUT).map(JsonEnvelope::payloadAsJsonObject);
        final var publicHearingResultedV2 = jsonToObjectConverter.convert(message.get(), PublicHearingResultedV2.class);

        assertThat(publicHearingResultedV2.getHearing().getId(), is(hearing.getId()));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getResultText(), is("ABTR - Anti-social behaviour injunction transferred\nAnti-social behaviour injunction proceedings transferred to Bexley Court on 02/02/2022 at 10:00. Reasons: reason for test"));
    }

    @Test
    public void shouldSetResultTextForResultLabel() throws IOException {

        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        stubCourtRoom(hearing);

        final String eventPayloadString = getStringFromResource("hearing.share-result-with-target-with-result-label.json")
                .replaceAll("HEARING_ID", hearing.getId().toString())
                .replaceAll("CASE_ID", hearing.getProsecutionCases().get(0).getId().toString())
                .replaceAll("ORDERED_DATE", orderedDate.toString())
                .replaceAll("OFFENCE_ID", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString());

        final Target target = jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(eventPayloadString), Target.class);
        final List<Target> targets = new ArrayList<>();
        targets.add(target);

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        final Optional<JsonObject> message = jmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(RETRIEVE_TIMEOUT).map(JsonEnvelope::payloadAsJsonObject);
        final var publicHearingResultedV2 = jsonToObjectConverter.convert(message.get(), PublicHearingResultedV2.class);

        assertThat(publicHearingResultedV2.getHearing().getId(), is(hearing.getId()));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getResultText(), is("ABTR - Anti-social behaviour injunction transferred\nAnti-social behaviour injunction proceedings transferred to Bexley Court on 02/02/2022 at 10:00. Reasons: reason for test"));

    }

    @Test
    public void shouldSetResultTextForChildResult() throws IOException {

        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        stubCourtRoom(hearing);

         final String eventPayloadString = getStringFromResource("hearing.share-result-with-target-with-child-result.json")
                .replaceAll("HEARING_ID", hearing.getId().toString())
                .replaceAll("CASE_ID", hearing.getProsecutionCases().get(0).getId().toString())
                .replaceAll("ORDERED_DATE", orderedDate.toString())
                .replaceAll("OFFENCE_ID", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString());

        final Target target = jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(eventPayloadString), Target.class);
        final List<Target> targets = new ArrayList<>();
        targets.add(target);

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        final Optional<JsonObject> message = jmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(RETRIEVE_TIMEOUT).map(JsonEnvelope::payloadAsJsonObject);
        final var publicHearingResultedV2 = jsonToObjectConverter.convert(message.get(), PublicHearingResultedV2.class);

        assertThat(publicHearingResultedV2.getHearing().getId(), is(hearing.getId()));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getResultText(), is("HMRCFP - Committal to prison further postponed (civil debt)\nFor a debt and costs of £200 or in default to serve 1 Concurrent consecutive to Bexley Court which is on case number Case 13 further postponed. The term of imprisonment is postponed on condition that Term 2. . Reason for the finding: reason for HMRCFP. Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG. An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC.      "));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).getResultText(), is("An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(2).getResultText(), is("Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG."));

    }

    @Test
    public void shouldSetResultTextForAllChildResult() throws IOException {

        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        stubCourtRoom(hearing);

        final String eventPayloadString = getStringFromResource("hearing.share-result-with-target-with-all-child-result.json")
                .replaceAll("HEARING_ID", hearing.getId().toString())
                .replaceAll("CASE_ID", hearing.getProsecutionCases().get(0).getId().toString())
                .replaceAll("ORDERED_DATE", orderedDate.toString())
                .replaceAll("OFFENCE_ID", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString());

        final Target target = jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(eventPayloadString), Target.class);
        final List<Target> targets = new ArrayList<>();
        targets.add(target);

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        final Optional<JsonObject> message = jmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(RETRIEVE_TIMEOUT).map(JsonEnvelope::payloadAsJsonObject);
        final var publicHearingResultedV2 = jsonToObjectConverter.convert(message.get(), PublicHearingResultedV2.class);

        assertThat(publicHearingResultedV2.getHearing().getId(), is(hearing.getId()));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(2).getResultText(), is("An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getResultText(), is("HMRCFPP - Committal to prison further postponed (civil debt)\nTaking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG., An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(3).getResultText(), is("Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG."));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).getResultText(), is("TCGG - Taking control of goods (warrant of control)\nTaking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCGG."));
    }

    @Test
    public void shouldSetResultTextForTwoOffences() throws IOException {

        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        stubCourtRoom(hearing);

        final String eventPayloadString = getStringFromResource("hearing.share-result-with-target-with-two-offences-result.json")
                .replaceAll("HEARING_ID", hearing.getId().toString())
                .replaceAll("CASE_ID", hearing.getProsecutionCases().get(0).getId().toString())
                .replaceAll("ORDERED_DATE", orderedDate.toString())
                .replaceAll("OFFENCE_ID1", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString())
                .replaceAll("OFFENCE_ID2", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getId().toString());

        final JsonObject targetObject = stringToJsonObjectConverter.convert(eventPayloadString);
        final List<Target> targets = targetObject.getJsonArray("targets").stream()
                .map(target -> (JsonObject) target)
                .map(target -> jsonObjectToObjectConverter.convert(target, Target.class))
                .collect(toList());

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        final Optional<JsonObject> message = jmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(RETRIEVE_TIMEOUT).map(JsonEnvelope::payloadAsJsonObject);
        final var publicHearingResultedV2 = jsonToObjectConverter.convert(message.get(), PublicHearingResultedV2.class);

        assertThat(publicHearingResultedV2.getHearing().getId(), is(hearing.getId()));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getResultText(), is("co - Community order England / Wales\nCommunity order made. The defendant must comply with the requirements (shown below) by 12/12/2022. judgeReservesBreachProceedings}. "));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).getResultText(), is("CURE - Curfew with electronic monitoring\nCurfew Requirement with Electronic Monitoring: Be under a curfew for 3 Weeks with electronic monitoring. Start date 12/12/2022. Start time 19:00. End date 12/12/2024. End time 07:00. Defendant to remain at 102PF,London. Curfew details: Everyday. "));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(2).getResultText(), is("UPWR - Unpaid work\nUnpaid Work Requirement: Carry out unpaid work for 100 Hours within the next twelve months. This work will be supervised by the responsible officer. "));

        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(0).getResultText(), is("co - Community order England / Wales\nCommunity order made. The defendant must comply with the requirements (shown below) by 12/12/2022. judgeReservesBreachProceedings}. "));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(1).getResultText(), is("CURE - Curfew with electronic monitoring\nCurfew Requirement with Electronic Monitoring: Be under a curfew for 3 Weeks with electronic monitoring. Start date 12/12/2022. Start time 19:00. End date 12/12/2024. End time 07:00. Defendant to remain at 102PF,London. Curfew details: Everyday. "));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(2).getResultText(), is("UPWR - Unpaid work\nUnpaid Work Requirement: Carry out unpaid work for 100 Hours within the next twelve months. This work will be supervised by the responsible officer. "));
    }

    @Test
    public void shouldSetResultTextForEachOrderedDate() throws IOException {

        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        stubCourtRoom(hearing);

        final String eventPayloadString = getStringFromResource("hearing.share-result-with-target-with-multiple-targets.json")
                .replaceAll("HEARING_ID", hearing.getId().toString())
                .replaceAll("CASE_ID", hearing.getProsecutionCases().get(0).getId().toString())
                .replaceAll("ORDERED_DATE1", orderedDate.toString())
                .replaceAll("ORDERED_DATE2", orderedDate2.toString())
                .replaceAll("OFFENCE_ID1", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId().toString())
                .replaceAll("OFFENCE_ID2", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getId().toString())
                .replaceAll("OFFENCE_ID3", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getId().toString())
                .replaceAll("OFFENCE_ID4", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(3).getId().toString())
                .replaceAll("OFFENCE_ID5", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(4).getId().toString())
                .replaceAll("OFFENCE_ID6", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(5).getId().toString())
                .replaceAll("OFFENCE_ID7", hearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(6).getId().toString());

        JsonObject jsonTargets = stringToJsonObjectConverter.convert(eventPayloadString);
        final List<Target> targets = jsonTargets.getJsonArray("targets").stream().map(jsonTarget -> jsonObjectToObjectConverter.convert(stringToJsonObjectConverter.convert(jsonTarget.toString()), Target.class))
                .collect(toList());

        shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

        final Optional<JsonObject> message = jmsMessageConsumerClient.retrieveMessageAsJsonEnvelope(RETRIEVE_TIMEOUT).map(JsonEnvelope::payloadAsJsonObject);
        final var publicHearingResultedV2 = jsonToObjectConverter.convert(message.get(), PublicHearingResultedV2.class);

        assertThat(publicHearingResultedV2.getHearing().getId(), is(hearing.getId()));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(0).getResultText(), is("CCSU - Committed to Crown Court for sentence on unconditional bail\nCommitted for sentence (Section 14 of the Sentencing Act 2020) for hearing on 18/05/2023 at 10:30, Chester Crown Court. Bail remand days to count (tagged days): 0. No indication given re victim personal statement. PSR ordered"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults().get(1).getResultText(), is("hearing on 18/05/2023 at 10:30, Chester Crown Court"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(0).getResultText(), is("CCSU - Committed to Crown Court for sentence on unconditional bail\nCommitted for sentence (Section 14 of the Sentencing Act 2020) for hearing on 18/05/2023 at 10:30, Chester Crown Court. Bail remand days to count (tagged days): 0. No indication given re victim personal statement. PSR ordered"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(1).getJudicialResults().get(1).getResultText(), is("hearing on 18/05/2023 at 10:30, Chester Crown Court"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getJudicialResults().get(0).getResultText(), is("CCSU - Committed to Crown Court for sentence on unconditional bail\nCommitted for sentence (Section 14 of the Sentencing Act 2020) for hearing on 18/05/2023 at 10:30, Chester Crown Court. Bail remand days to count (tagged days): 0. No indication given re victim personal statement. PSR ordered"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(2).getJudicialResults().get(1).getResultText(), is("hearing on 18/05/2023 at 10:30, Chester Crown Court"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(3).getJudicialResults().get(0).getResultText(), is("Entered in error\n"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(4).getJudicialResults().get(0).getResultText(), is("CCSU - Committed to Crown Court for sentence on unconditional bail\nCommitted for sentence (Section 14 of the Sentencing Act 2020) for hearing on 18/05/2023 at 10:30, Chester Crown Court. Bail remand days to count (tagged days): 0. No indication given re victim personal statement. PSR ordered"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(4).getJudicialResults().get(1).getResultText(), is("hearing on 18/05/2023 at 10:30, Chester Crown Court"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(5).getJudicialResults().get(0).getResultText(), is("Entered in error\n"));
        assertThat(publicHearingResultedV2.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(6).getJudicialResults().get(0).getResultText(), is("Entered in error\n"));
    }


    private CommandHelpers.InitiateHearingCommandHelper getHearingCommand(final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure) {
        return h(UseCases.initiateHearingWithNewJMS(getRequestSpec(),
                InitiateHearingCommand.initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(
                                defaultArguments().setStructure(caseStructure)
                                        .setDefendantType(PERSON)
                                        .setHearingLanguage(ENGLISH)
                                        .setJurisdictionType(CROWN)
                        ).build()), true, true, true, false, false));
    }

    private HashMap<UUID, Map<UUID, List<UUID>>> getUuidMapForMultipleCaseStructure() {
        HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        Map<UUID, List<UUID>> value = new HashMap<>();
        value.put(randomUUID(), TestUtilities.asList(randomUUID(), randomUUID(),randomUUID(), randomUUID(),randomUUID(), randomUUID(),randomUUID()));

        caseStructure.put(randomUUID(), value);
        return caseStructure;
    }

    private void shareDaysResultWithCourtClerk(final Hearing hearing, final List<Target> targets, final LocalDate hearingDay) {
        final DelegatedPowers courtClerk1 = DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();
        ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
        shareDaysResultsCommand.setHearingDay(hearingDay);
        shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                shareDaysResultsCommand,
                command -> command.setCourtClerk(courtClerk1)
        ), targets);
    }
}
