package uk.gov.moj.cpp.hearing.it;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.HearingLanguage.ENGLISH;
import static uk.gov.justice.core.courts.JurisdictionType.CROWN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResultsPerDay;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Spy;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.TestUtilities;
import uk.gov.moj.cpp.platform.test.feature.toggle.FeatureStubber;

public class ResultTextIT extends AbstractIT {

    public static final String RESULT_PARENT = "co - Community order England / Wales\nEnd Date: 12/10/2024, Responsible officer: a probation officer, Supervising Court: Banbury Magistrates' Court, Probation team to be notified organisation name: London Division NPS, Probation team to be notified email address 1: londonnps.court@justice.gov.uk" + System.lineSeparator() +
            "Number of hours: 100 Hours" + System.lineSeparator() +
            "Offender to comply with any instructions of the responsible officer to attend appointments (with the responsible officer or someone else nominated by them), or to participate in any activity as required by the responsible officer up to a maximum of: 28 Days";
    public static final String RESULT_CHILD_1 = "UPWR - Unpaid work" + System.lineSeparator() +
            "Number of hours: 100 Hours";
    public static final String RESULT_CHILD_2 = "RAR - Rehabilitation activity" + System.lineSeparator() +
            "Offender to comply with any instructions of the responsible officer to attend appointments (with the responsible officer or someone else nominated by them), or to participate in any activity as required by the responsible officer up to a maximum of: 28 Days";
    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
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

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("ABTRR - Anti-social behaviour injunction transferred\nCounty Court where the proceedings have been transferred to organisation name: Bexley Court, County Court where the proceedings have been transferred to address line 1: line 1, Date of hearing: 02/02/2022, Time of hearing: 10:00, Reasons: reason for test"));
        }

        try (final Utilities.EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            makeCommand(getRequestSpec(), "hearing.share-results")
                    .ofType("application/vnd.hearing.share-results+json")
                    .withArgs(hearing.getId())
                    .withPayload(eventPayloadString)
                    .executeSuccessfully();

            final JsonPath publicHearingResulted = publicEventResulted.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("ABTRR - Anti-social behaviour injunction transferred\nCounty Court where the proceedings have been transferred to organisation name: Bexley Court, County Court where the proceedings have been transferred to address line 1: line 1, Date of hearing: 02/02/2022, Time of hearing: 10:00, Reasons: reason for test"));
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

        try (final Utilities.EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            makeCommand(getRequestSpec(), "hearing.share-results")
                    .ofType("application/vnd.hearing.share-results+json")
                    .withArgs(hearing.getId())
                    .withPayload(eventPayloadString)
                    .executeSuccessfully();

            final JsonPath publicHearingResulted = publicEventResulted.waitFor();

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

        try (final Utilities.EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            makeCommand(getRequestSpec(), "hearing.share-results")
                    .ofType("application/vnd.hearing.share-results+json")
                    .withArgs(hearing.getId())
                    .withPayload(eventPayloadString)
                    .executeSuccessfully();

            final JsonPath publicHearingResulted = publicEventResulted.waitFor();

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
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[1].resultText"), is("Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[2].resultText"), is("An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
        }

        try (final Utilities.EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            makeCommand(getRequestSpec(), "hearing.share-results")
                    .ofType("application/vnd.hearing.share-results+json")
                    .withArgs(hearing.getId())
                    .withPayload(eventPayloadString)
                    .executeSuccessfully();

            final JsonPath publicHearingResulted = publicEventResulted.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("HMRCFP - Committal to prison further postponed (civil debt)\nFor a debt and costs of £200 or in default to serve 1 Concurrent consecutive to Bexley Court which is on case number Case 13 further postponed. The term of imprisonment is postponed on condition that Term 2. . Reason for the finding: reason for HMRCFP. Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG. An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC.      "));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[1].resultText"), is("Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[2].resultText"), is("An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
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

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[2].resultText"), is("Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("TCGG - Taking control of goods (warrant of control)" +  System.lineSeparator() + "Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCGG."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[3].resultText"), is("An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[1].resultText"), is("HMRCFPP - Committal to prison further postponed (civil debt)\nTaking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG., An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
        }

        try (final Utilities.EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)))))) {

            makeCommand(getRequestSpec(), "hearing.share-results")
                    .ofType("application/vnd.hearing.share-results+json")
                    .withArgs(hearing.getId())
                    .withPayload(eventPayloadString)
                    .executeSuccessfully();

            final JsonPath publicHearingResulted = publicEventResulted.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[2].resultText"), is("Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is("TCGG - Taking control of goods (warrant of control)" +  System.lineSeparator() + "Taking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCGG."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[3].resultText"), is("An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[1].resultText"), is("HMRCFPP - Committal to prison further postponed (civil debt)\nTaking control of goods (warrant of control) was tried and unsuccessful. Reasons: reasons for TCG., An application to the High Court or County Court was tried and unsuccessful. Reasons: reasons for AHCC."));
        }
    }

    @Test
    public void shouldSetResultTextForDependantResultDefinitionGroup() throws IOException {
        final LocalDate hearingDay = LocalDate.now();

        final CommandHelpers.InitiateHearingCommandHelper hearingCommand = getHearingCommand(getUuidMapForMultipleCaseStructure());
        final Hearing hearing = hearingCommand.getHearing();

        stubCourtRoom(hearing);

        final String eventPayloadString = getStringFromResource("hearing.share-result-with-target-with-dependantResultDefinitionGroup.json")
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

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[2].resultText"), is(RESULT_PARENT));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[0].resultText"), is(RESULT_CHILD_1));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults[1].resultText"), is(RESULT_CHILD_2));
        }

        try (final Utilities.EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId()))
                                .with(Hearing::getProsecutionCases, hasItem(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getDefendants, hasItem(isBean(Defendant.class)
                                                .with(Defendant::getOffences, hasItem(isBean(Offence.class)
                                                        .with(Offence::getJudicialResults, hasItem(isBean(JudicialResult.class)
                                                                .with(JudicialResult::getResultText, is(RESULT_PARENT))))
                                                        .with(Offence::getJudicialResults, hasItem(isBean(JudicialResult.class)
                                                                .with(JudicialResult::getResultText, is(RESULT_CHILD_1))))
                                                        .with(Offence::getJudicialResults, hasItem(isBean(JudicialResult.class)
                                                                .with(JudicialResult::getResultText, is(RESULT_CHILD_2)))))))))))))) {

            makeCommand(getRequestSpec(), "hearing.share-results")
                    .ofType("application/vnd.hearing.share-results+json")
                    .withArgs(hearing.getId())
                    .withPayload(eventPayloadString)
                    .executeSuccessfully();

            final JsonPath publicHearingResulted = publicEventResulted.waitFor();

            assertThat(publicHearingResulted.getInt("hearing.prosecutionCases[0].defendants[0].offences[0].judicialResults.size"), is(3));
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
        value.put(randomUUID(), TestUtilities.asList(randomUUID()));
        caseStructure.put(randomUUID(), value);
        caseStructure.put(randomUUID(), toMap(randomUUID(), TestUtilities.asList(randomUUID(), randomUUID())));
        caseStructure.put(randomUUID(), toMap(randomUUID(), TestUtilities.asList(randomUUID())));
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
