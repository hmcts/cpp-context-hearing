package uk.gov.moj.cpp.hearing.it;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.Prompt.prompt;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.resultLine;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardSaveDraftTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ProgressionStub.stubProgressionGenerateNows;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubForReferenceDataResults;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.JudicialRole;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.NextHearingDefendant;
import uk.gov.justice.core.courts.NextHearingOffence;
import uk.gov.justice.core.courts.NextHearingProsecutionCase;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({"squid:S1607"})
public class HearingAdjournIT extends AbstractIT {

    private static final String DATE_OF_HEARING_LABEL = "Date of hearing";
    private static final String HEARING_TYPE_LABEL = "Hearing type";
    private static final String ESTIMATED_DURATION_LABEL = "Estimated duration";
    private static final String REMAND_STATUS_LABEL = "Remand Status";
    private static final String TIME_OF_HEARING_LABEL = "Time Of Hearing";
    private static final String COURT_ROOM_LABEL = "CourtRoom";
    private static final String COURT_CENTRE_LABEL = "Courthouse name";
    public static final UUID WIMBLEDON_COURT_CENTRE_ID = UUID.fromString("80921334-2cf0-4609-8a29-0921bf6b3520");
    public static final UUID WIMBLEDON_ROOM_B_ID = UUID.fromString("2bd3e322-f603-411d-a5ab-2e42ff4b6e00");
    public static final UUID WIMBLEDON_ROOM_A_ID = UUID.fromString("f703dc83-d0e4-42c8-8d44-0352d46e5194");
    public static final String WIMBLEDON_ADDRESS1 = "4 Belmarsh Road";
    public static final String WIMBLEDON_ADDRESS2 = "London";
    public static final String WIMBLEDON_POSTCODE = "SE28 0HA";
    public static final LocalDate START_DATE1 = LocalDate.of(2018, 07, 02);
    public static final LocalDate START_DATE2 = LocalDate.of(2018, 8, 02);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Before
    public void setup() {
        stubProgressionGenerateNows();
        stubForReferenceDataResults();

    }

    @Test
    public void shouldRaiseHearingAdjournedEvent() {

        LocalDate orderedDate = PAST_LOCAL_DATE.next();
        UUID resultLineId = randomUUID();

        final UUID primaryResultDefinitionId = UUID.fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");
        DocumentGeneratorStub.stubDocumentCreate("N/A");

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));
        hearingOne.getHearing().setHearingLanguage(HearingLanguage.ENGLISH);
        stubReferenceData(orderedDate, primaryResultDefinitionId, randomUUID(), hearingOne.getHearing().getCourtCentre().getId());

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final List<Target> targets = new ArrayList<>();

        SaveDraftResultCommand saveDraftResultCommand = UseCases.saveDraftResults(requestSpec, with(standardSaveDraftTemplate(hearingOne.getHearingId(),
                hearingOne.getFirstDefendantForFirstCase().getId(),
                hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                resultLineId
        ), saveDraftCommand -> {

            saveDraftCommand.getTarget()

                    .setResultLines(asList(with(resultLine(resultLineId), resultLine ->
                            resultLine.setResultLabel("Next Hearing")
                                    .setResultDefinitionId(primaryResultDefinitionId)
                                    .setOrderedDate(orderedDate)
                                    .setPrompts(asList(
                                            prompt().withId(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"))
                                                    .withLabel(DATE_OF_HEARING_LABEL)
                                                    .withValue(START_DATE1.format(DATE_TIME_FORMATTER))
                                                    .withWelshValue(START_DATE1.format(DATE_TIME_FORMATTER))
                                                    .build(),
                                            prompt().withId(UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22"))
                                                    .withLabel(HEARING_TYPE_LABEL)
                                                    .withValue("Plea & Trial Preparation")
                                                    .withWelshValue("WPlea & Trial Preparation")
                                                    .build(),
                                            prompt().withId(UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac"))
                                                    .withLabel(ESTIMATED_DURATION_LABEL)
                                                    .withValue("59 Minutes")
                                                    .withWelshValue("W59 Minutes")
                                                    .build(),
                                            prompt().withId(UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362"))
                                                    .withLabel(REMAND_STATUS_LABEL)
                                                    .withValue("remand in custody")
                                                    .withWelshValue("remand in custody")
                                                    .build(),
                                            prompt().withId(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"))
                                                    .withLabel(TIME_OF_HEARING_LABEL)
                                                    .withValue("10:30")
                                                    .withWelshValue("10:30")
                                                    .build(),
                                            prompt().withId(UUID.fromString("5f507153-6dc9-4ec0-94db-c821eff333f1"))
                                                    .withLabel(COURT_ROOM_LABEL)
                                                    .withValue("ROOM A")
                                                    .withWelshValue("ROOM A")
                                                    .build(),
                                            prompt().withId(UUID.fromString("7746831a-d5dd-4fa8-ac13-528573948c8a"))
                                                    .withLabel(COURT_CENTRE_LABEL)
                                                    .withValue("Wimbledon")
                                                    .withWelshValue("Wimbledon Magistrates")
                                                    .build()

                                    ))
                    )));
            targets.add(saveDraftCommand.getTarget());
        }));

        hearingOne.getHearing();
        final Utilities.EventListener publicHearingAdjourned = listenFor("public.hearing.adjourned", 60000)
                .withFilter(convertStringTo(HearingAdjourned.class, isBean(HearingAdjourned.class)
                        .with(HearingAdjourned::getAdjournedHearing, is(hearingOne.getHearingId()))
                        .with(HearingAdjourned::getNextHearings, first(isBean(NextHearing.class)
                                .with(NextHearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getDescription, is("Plea & Trial Preparation")))
                                .with(NextHearing::getJurisdictionType, is(hearingOne.getHearing().getJurisdictionType()))
                                .with(NextHearing::getReportingRestrictionReason, is(hearingOne.getHearing().getReportingRestrictionReason()))
                                .with(NextHearing::getHearingLanguage, is(hearingOne.getHearing().getHearingLanguage()))
                                .with(NextHearing::getEstimatedMinutes, is(59))
                                .withValue(nh->nh.getEarliestStartDateTime().toLocalDate(), START_DATE1)
                                .with(NextHearing::getCourtCentre, isBean(CourtCentre.class)
                                        .withValue(CourtCentre::getId, WIMBLEDON_COURT_CENTRE_ID)
                                        .withValue(CourtCentre::getRoomId, WIMBLEDON_ROOM_A_ID))
                                .with(NextHearing::getJudiciary, first(isBean(JudicialRole.class)
                                        .with(JudicialRole::getJudicialId, is(hearingOne.getHearing().getJudiciary().get(0).getJudicialId()))
                                        .with(JudicialRole::getJudicialRoleType, is(hearingOne.getHearing().getJudiciary().get(0).getJudicialRoleType()))))
                                .with(NextHearing::getNextHearingProsecutionCases, first(isBean(NextHearingProsecutionCase.class)
                                        .with(NextHearingProsecutionCase::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getId()))
                                        .with(NextHearingProsecutionCase::getDefendants, first(isBean(NextHearingDefendant.class)
                                                .with(NextHearingDefendant::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId()))
                                                .with(NextHearingDefendant::getOffences, first(isBean(NextHearingOffence.class)
                                                        .with(NextHearingOffence::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId()))))))))
                        ))
                ));

        ShareResultsCommand shareResultsCommand = standardShareResultsCommandTemplate(hearingOne.getHearingId());

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), standardShareResultsCommandTemplate(hearingOne.getHearingId()), targets);

        publicHearingAdjourned.waitFor();

        UseCases.saveDraftResults(requestSpec, with(saveDraftResultCommand, saveDraftCommand -> saveDraftCommand.getTarget()
                .setResultLines(asList(with(resultLine(resultLineId), resultLine -> {
                    resultLine.setResultLabel("Next Hearing")
                            .setResultDefinitionId(primaryResultDefinitionId)
                            .setOrderedDate(orderedDate)
                            .setPrompts(asList(
                                    prompt().withId(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"))
                                            .withLabel(DATE_OF_HEARING_LABEL)
                                            .withValue(START_DATE2.format(DATE_TIME_FORMATTER))
                                            .withWelshValue(START_DATE2.format(DATE_TIME_FORMATTER))
                                            .build(),
                                    prompt().withId(UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22"))
                                            .withLabel(HEARING_TYPE_LABEL)
                                            .withValue("Sentence")
                                            .withWelshValue("WSentencing")
                                            .build(),
                                    prompt().withId(UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac"))
                                            .withLabel(ESTIMATED_DURATION_LABEL)
                                            .withValue("30 Minutes")
                                            .withWelshValue("30 Minutes")
                                            .build(),
                                    prompt().withId(UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362"))
                                            .withLabel(REMAND_STATUS_LABEL)
                                            .withValue("remand in custody")
                                            .withWelshValue("remand in custody")
                                            .build(),
                                    prompt().withId(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"))
                                            .withLabel(TIME_OF_HEARING_LABEL)
                                            .withValue("11:30")
                                            .withWelshValue("11:30")
                                            .build(),
                                    prompt().withId(UUID.fromString("5f507153-6dc9-4ec0-94db-c821eff333f1"))
                                            .withLabel(COURT_ROOM_LABEL)
                                            .withValue("ROOM B")
                                            .withWelshValue("ROOM B")
                                            .build(),
                                    prompt().withId(UUID.fromString("7746831a-d5dd-4fa8-ac13-528573948c8a"))
                                            .withLabel(COURT_CENTRE_LABEL)
                                            .withValue("Wimbledon")
                                            .withWelshValue("Wimbledon Magistrates")
                                            .build()

                            ));
                })))));

        final Utilities.EventListener publicHearingAdjourned2 = listenFor("public.hearing.adjourned", 30000)
                .withFilter(convertStringTo(HearingAdjourned.class, isBean(HearingAdjourned.class)
                        .with(HearingAdjourned::getAdjournedHearing, is(hearingOne.getHearingId()))
                        .with(HearingAdjourned::getNextHearings, first(isBean(NextHearing.class)
                                .with(NextHearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getDescription, is("Sentence")))
                                .with(NextHearing::getJurisdictionType, is(hearingOne.getHearing().getJurisdictionType()))
                                .with(NextHearing::getReportingRestrictionReason, is(hearingOne.getHearing().getReportingRestrictionReason()))
                                .with(NextHearing::getHearingLanguage, is(hearingOne.getHearing().getHearingLanguage()))
                                .with(NextHearing::getEstimatedMinutes, is(30))
                                .with(NextHearing::getCourtCentre, isBean(CourtCentre.class)
                                        .withValue(CourtCentre::getId, WIMBLEDON_COURT_CENTRE_ID)
                                        .withValue(CourtCentre::getRoomId, WIMBLEDON_ROOM_B_ID)
                                         /*.with(CourtCentre::getAddress, isBean(Address.class)
                                             .withValue(Address::getAddress1, WIMBLEDON_ADDRESS1)
                                                 .withValue(Address::getAddress2, WIMBLEDON_ADDRESS2)
                                                 .withValue(Address::getPostcode, WIMBLEDON_POSTCODE)
                                         )*/
                                )

                                .with(NextHearing::getJudiciary, first(isBean(JudicialRole.class)
                                        .with(JudicialRole::getJudicialId, is(hearingOne.getHearing().getJudiciary().get(0).getJudicialId()))
                                        .with(JudicialRole::getJudicialRoleType, is(hearingOne.getHearing().getJudiciary().get(0).getJudicialRoleType()))))
                                .with(NextHearing::getNextHearingProsecutionCases, first(isBean(NextHearingProsecutionCase.class)
                                        .with(NextHearingProsecutionCase::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getId()))
                                        .with(NextHearingProsecutionCase::getDefendants, first(isBean(NextHearingDefendant.class)
                                                .with(NextHearingDefendant::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId()))
                                                .with(NextHearingDefendant::getOffences, first(isBean(NextHearingOffence.class)
                                                        .with(NextHearingOffence::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId()))))))))
                        ))));


        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), standardShareResultsCommandTemplate(hearingOne.getHearingId()), targets);

        publicHearingAdjourned2.waitFor();
    }

    private void stubReferenceData(final LocalDate referenceDate, final UUID primaryResultDefinitionId, final UUID mandatoryPromptId, final UUID courtCentreId) {
        AllNows allNows = AllNows.allNows()
                .setNows(singletonList(
                        NowDefinition.now()
                                .setId(randomUUID())
                                .setTemplateName("nowsTemplateName0")
                                .setResultDefinitions(singletonList(
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(primaryResultDefinitionId)
                                                .setMandatory(true)
                                                .setPrimary(true)
                                ))
                ));

        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows);
        final String userGroup1 = "DefenseCounsel";

        final Map<String, UUID> labelToUuid = new HashMap<>();
        labelToUuid.put(DATE_OF_HEARING_LABEL, UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"));
        labelToUuid.put(HEARING_TYPE_LABEL, UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22"));
        labelToUuid.put(ESTIMATED_DURATION_LABEL, UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac"));
        labelToUuid.put(REMAND_STATUS_LABEL, UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362"));
        labelToUuid.put(TIME_OF_HEARING_LABEL, UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"));
        labelToUuid.put(COURT_ROOM_LABEL, UUID.fromString("5f507153-6dc9-4ec0-94db-c821eff333f1"));
        labelToUuid.put(COURT_CENTRE_LABEL, UUID.fromString("7746831a-d5dd-4fa8-ac13-528573948c8a"));

        final List<String> usergroups = singletonList(userGroup1);
        List<Prompt> promptDefs = asList(REMAND_STATUS_LABEL, DATE_OF_HEARING_LABEL, HEARING_TYPE_LABEL, ESTIMATED_DURATION_LABEL, TIME_OF_HEARING_LABEL,
                COURT_ROOM_LABEL, COURT_CENTRE_LABEL).stream()
                .map(label -> Prompt.prompt()
                        .setMandatory(false)
                        //this needs to link to the id
                        .setId(labelToUuid.get(label))
                        .setLabel(label)
                        .setUserGroups(usergroups))
                .collect(Collectors.toList());
        promptDefs.add(Prompt.prompt()
                .setLabel("mandatory")
                .setMandatory(true)
                .setId(mandatoryPromptId)
                .setUserGroups(usergroups)
        );

        AllResultDefinitions allResultDefinitions = AllResultDefinitions.allResultDefinitions().setResultDefinitions(
                singletonList(ResultDefinition.resultDefinition()
                        .setId(primaryResultDefinitionId)
                        .setUserGroups(singletonList(userGroup1))
                        .setFinancial("Y")
                        .setPrompts(promptDefs)
                )
        );

        ReferenceDataStub.stubGetAllResultDefinitions(referenceDate, allResultDefinitions);
        ReferenceDataStub.stubRelistReferenceDataResults();

        stubLjaDetails(courtCentreId);

    }

}
