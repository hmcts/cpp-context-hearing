package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResultsPerDay;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.DefendantType.PERSON;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.defaultArguments;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandV2Template;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataResultDefinitionsWithResultTexts;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.path.json.JsonPath;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.json.JsonObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Spy;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.TestUtilities;
import uk.gov.moj.cpp.platform.test.feature.toggle.FeatureStubber;

public class ResultTextIT extends AbstractIT {

    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    private StringToJsonObjectConverter stringToJsonObjectConverter = new StringToJsonObjectConverter();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter(objectMapper);

    private final static LocalDate orderedDate = PAST_LOCAL_DATE.next();

    @BeforeClass
    public static void setupBeforeClass() {
        stubGetReferenceDataResultDefinitionsWithResultTexts(orderedDate);

        final ImmutableMap<String, Boolean> features = ImmutableMap.of("amendReshare", true);
        FeatureStubber.stubFeaturesFor(HEARING_CONTEXT, features);
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

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("SUSPS - Suspended sentence order - imprisonment\nCommitted to prison for 7 Months Concurrent consecutive to offence of2121 which is on case number tfl3434 suspended. Reason: failure to express willingness to comply with a proposed requirement for a community order. Reason for custody: the defendant has a flagrant disregard for court orders, the defendant has a flagrant disregard for people and their property. The defendant must comply with the requirements (shown below) within the supervision period. This order is to be reviewed weekly starting on 28/12/2022 at 16:00 at Wycombe Magistrates' Court.  In the event of activation of sentence: 20 bail remand days to count. Total custodial period 7 Months suspended for 5 Months . "));
        }
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

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("ABTR - Anti-social behaviour injunction transferred\nAnti-social behaviour injunction proceedings transferred to Bexley Court on 02/02/2022 at 10:00. Reasons: reason for test"));
        }

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

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("ABTR - Anti-social behaviour injunction transferred\nAnti-social behaviour injunction proceedings transferred to Bexley Court on 02/02/2022 at 10:00. Reasons: reason for test"));
        }

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

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("HMRCFP - Committal to prison further postponed (civil debt)\nFor a debt and costs of £200 or in default to serve 1 Concurrent consecutive to Bexley Court which is on case number Case 13 further postponed. The term of imprisonment is postponed on condition that Term 2. . Reason for the finding: reason for HMRCFP. Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG. An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC.      "));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[1].resultText"), is("An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[2].resultText"), is("Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG."));
        }


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

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[2].resultText"), is("An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("HMRCFPP - Committal to prison further postponed (civil debt)\nTaking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG., An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[3].resultText"), is("Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[1].resultText"), is("TCGG - Taking control of goods (warrant of control)\nTaking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCGG."));
        }
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

        try (final Utilities.EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            shareDaysResultWithCourtClerk(hearing, targets, hearingDay);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("co - Community order England / Wales\nCommunity order made. The defendant must comply with the requirements (shown below) by 12/12/2022. judgeReservesBreachProceedings}. "));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[1].resultText"), is("UPWR - Unpaid work\nUnpaid Work Requirement: Carry out unpaid work for 100 Hours within the next twelve months. This work will be supervised by the responsible officer. "));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[2].resultText"), is("CURE - Curfew with electronic monitoring\nCurfew Requirement with Electronic Monitoring: Be under a curfew for 3 Weeks with electronic monitoring. Start date 12/12/2022. Start time 19:00. End date 12/12/2024. End time 07:00. Defendant to remain at 102PF,London. Curfew details: Everyday. "));

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[1].judicialResults[0].resultText"), is("co - Community order England / Wales\nCommunity order made. The defendant must comply with the requirements (shown below) by 12/12/2022. judgeReservesBreachProceedings}. "));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[1].judicialResults[1].resultText"), is("UPWR - Unpaid work\nUnpaid Work Requirement: Carry out unpaid work for 100 Hours within the next twelve months. This work will be supervised by the responsible officer. "));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[1].judicialResults[2].resultText"), is("CURE - Curfew with electronic monitoring\nCurfew Requirement with Electronic Monitoring: Be under a curfew for 3 Weeks with electronic monitoring. Start date 12/12/2022. Start time 19:00. End date 12/12/2024. End time 07:00. Defendant to remain at 102PF,London. Curfew details: Everyday. "));

        }
    }


    private CommandHelpers.InitiateHearingCommandHelper getHearingCommand(final HashMap<UUID, Map<UUID, List<UUID>>> caseStructure) {
        return h(UseCases.initiateHearing(getRequestSpec(),
                InitiateHearingCommand.initiateHearingCommand()
                        .setHearing(CoreTestTemplates.hearing(
                                defaultArguments().setStructure(caseStructure)
                                        .setDefendantType(PERSON)
                                        .setHearingLanguage(ENGLISH)
                                        .setJurisdictionType(CROWN)
                        ).build())));
    }

    private HashMap<UUID, Map<UUID, List<UUID>>> getUuidMapForMultipleCaseStructure() {
        HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        Map<UUID, List<UUID>> value = new HashMap<>();
        value.put(randomUUID(), TestUtilities.asList(randomUUID(), randomUUID()));
        value.put(randomUUID(), TestUtilities.asList(randomUUID(), randomUUID()));
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
