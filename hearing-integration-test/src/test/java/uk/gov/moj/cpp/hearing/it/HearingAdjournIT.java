package uk.gov.moj.cpp.hearing.it;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.json.schemas.core.Prompt.prompt;
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

import org.junit.Test;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.moj.cpp.external.domain.progression.relist.Hearing;
import uk.gov.moj.cpp.external.domain.progression.relist.Offence;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.utils.DocumentGeneratorStub;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HearingAdjournIT extends AbstractIT {
    private static final String DD_MM_YYYY = "dd/MM/yyyy";
    private static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DATE_OF_HEARING_LABEL = "Date of hearing";
    public static final String HEARING_TYPE_LABEL = "Hearing type";
    public static final String ESTIMATED_DURATION_LABEL = "Estimated duration";
    public static final String REMAND_STATUS_LABEL = "Remand Status";
    public static final String TIME_OF_HEARING_LABEL = "Time Of Hearing";

    @Test
    public void shouldRaiseHearingAdjournedEvent() {

        LocalDate orderedDate = PAST_LOCAL_DATE.next();
        UUID resultLineId = randomUUID();

        final UUID primaryResultDefinitionId = UUID.fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");
        stubReferenceData(orderedDate, primaryResultDefinitionId, randomUUID());
        DocumentGeneratorStub.stubDocumentCreate("N/A");

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));
        hearingOne.getHearing().setHearingLanguage(HearingLanguage.ENGLISH);

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        SaveDraftResultCommand saveDraftResultCommand = UseCases.saveDraftResults(requestSpec, with(standardSaveDraftTemplate(hearingOne.getHearingId(),
                hearingOne.getFirstDefendantForFirstCase().getId(),
                hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                resultLineId
        ), saveDraftCommand -> {

            saveDraftCommand.getTarget()

                    .setResultLines(asList(with(resultLine(resultLineId), resultLine -> {
                        resultLine.setResultLabel("Next Hearing")
                                .setResultDefinitionId(primaryResultDefinitionId)
                                .setOrderedDate(orderedDate)
                                .setPrompts(asList(
                                        prompt().withId(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"))
                                                .withLabel(DATE_OF_HEARING_LABEL)
                                                .withValue("02/07/2018")
                                                .build(),
                                        prompt().withId(UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22"))
                                                .withLabel(HEARING_TYPE_LABEL)
                                                .withValue("Plea & Trial Preparation")
                                                .build(),
                                        prompt().withId(UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac"))
                                                .withLabel(ESTIMATED_DURATION_LABEL)
                                                .withValue("59 Minutes")
                                                .build(),
                                        prompt().withId(UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362"))
                                                .withLabel(REMAND_STATUS_LABEL)
                                                .withValue("remand in custody")
                                                .build(),
                                        prompt().withId(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"))
                                                .withLabel(TIME_OF_HEARING_LABEL)
                                                .withValue("10.30")
                                                .build()
                                ));
                    })));

        }));

        hearingOne.getHearing();
        final Utilities.EventListener publicHearingAdjourned = listenFor("public.hearing.adjourned", 30000)
                .withFilter(convertStringTo(HearingAdjourned.class, isBean(HearingAdjourned.class)
                        .with(HearingAdjourned::getAdjournedHearing, is(hearingOne.getHearingId()))
                        .with(HearingAdjourned::getNextHearings, first(isBean(Hearing.class)
                                .with(Hearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getDescription, is("Plea & Trial Preparation")))
                                .with(Hearing::getJurisdictionType, is(hearingOne.getHearing().getJurisdictionType()))
                                .with(Hearing::getReportingRestrictionReason, is(hearingOne.getHearing().getReportingRestrictionReason()))
                                .with(Hearing::getHearingLanguage, is(hearingOne.getHearing().getHearingLanguage()))
                                .with(Hearing::getEstimatedMinutes, is(59))
                                .with(Hearing::getCourtCentre, isBean(uk.gov.moj.cpp.external.domain.progression.relist.CourtCentre.class)
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.CourtCentre::getId, is(hearingOne.getHearing().getCourtCentre().getId()))
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.CourtCentre::getRoomId, is(hearingOne.getHearing().getCourtCentre().getRoomId())))
                                .with(Hearing::getJudiciary, first(isBean(uk.gov.moj.cpp.external.domain.progression.relist.JudicialRole.class)
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.JudicialRole::getJudicialId, is(hearingOne.getHearing().getJudiciary().get(0).getJudicialId()))
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.JudicialRole::getJudicialRoleType, is(hearingOne.getHearing().getJudiciary().get(0).getJudicialRoleType()))))
                                .with(Hearing::getProsecutionCases, first(isBean(uk.gov.moj.cpp.external.domain.progression.relist.ProsecutionCase.class)
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.ProsecutionCase::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getId()))
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.ProsecutionCase::getDefendants, first(isBean(uk.gov.moj.cpp.external.domain.progression.relist.Defendant.class)
                                                .with(uk.gov.moj.cpp.external.domain.progression.relist.Defendant::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId()))
                                                .with(uk.gov.moj.cpp.external.domain.progression.relist.Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId()))))))))
                        ))));

        ShareResultsCommand shareResultsCommand = standardShareResultsCommandTemplate(hearingOne.getHearingId());

        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), shareResultsCommand);

        publicHearingAdjourned.waitFor();


        UseCases.saveDraftResults(requestSpec, with(saveDraftResultCommand, saveDraftCommand -> {

            saveDraftCommand.getTarget()

                    .setResultLines(asList(with(resultLine(resultLineId), resultLine -> {
                        resultLine.setResultLabel("Next Hearing")
                                .setResultDefinitionId(primaryResultDefinitionId)
                                .setOrderedDate(orderedDate)
                                .setPrompts(asList(
                                        prompt().withId(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"))
                                                .withLabel(DATE_OF_HEARING_LABEL)
                                                .withValue("02/08/2018")
                                                .build(),
                                        prompt().withId(UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22"))
                                                .withLabel(HEARING_TYPE_LABEL)
                                                .withValue("Sentencing")
                                                .build(),
                                        prompt().withId(UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac"))
                                                .withLabel(ESTIMATED_DURATION_LABEL)
                                                .withValue("30 Minutes")
                                                .build(),
                                        prompt().withId(UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362"))
                                                .withLabel(REMAND_STATUS_LABEL)
                                                .withValue("remand in custody")
                                                .build(),
                                        prompt().withId(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"))
                                                .withLabel(TIME_OF_HEARING_LABEL)
                                                .withValue("11.30")
                                                .build()
                                ));
                    })));

        }));

        final Utilities.EventListener publicHearingAdjourned2 = listenFor("public.hearing.adjourned", 30000)
                .withFilter(convertStringTo(HearingAdjourned.class, isBean(HearingAdjourned.class)
                        .with(HearingAdjourned::getAdjournedHearing, is(hearingOne.getHearingId()))
                        .with(HearingAdjourned::getNextHearings, first(isBean(Hearing.class)
                                .with(Hearing::getType, isBean(HearingType.class)
                                        .with(HearingType::getDescription, is("Sentencing")))
                                .with(Hearing::getJurisdictionType, is(hearingOne.getHearing().getJurisdictionType()))
                                .with(Hearing::getReportingRestrictionReason, is(hearingOne.getHearing().getReportingRestrictionReason()))
                                .with(Hearing::getHearingLanguage, is(hearingOne.getHearing().getHearingLanguage()))
                                .with(Hearing::getEstimatedMinutes, is(30))
                                .with(Hearing::getCourtCentre, isBean(uk.gov.moj.cpp.external.domain.progression.relist.CourtCentre.class)
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.CourtCentre::getId, is(hearingOne.getHearing().getCourtCentre().getId()))
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.CourtCentre::getRoomId, is(hearingOne.getHearing().getCourtCentre().getRoomId())))
                                .with(Hearing::getJudiciary, first(isBean(uk.gov.moj.cpp.external.domain.progression.relist.JudicialRole.class)
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.JudicialRole::getJudicialId, is(hearingOne.getHearing().getJudiciary().get(0).getJudicialId()))
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.JudicialRole::getJudicialRoleType, is(hearingOne.getHearing().getJudiciary().get(0).getJudicialRoleType()))))
                                .with(Hearing::getProsecutionCases, first(isBean(uk.gov.moj.cpp.external.domain.progression.relist.ProsecutionCase.class)
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.ProsecutionCase::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getId()))
                                        .with(uk.gov.moj.cpp.external.domain.progression.relist.ProsecutionCase::getDefendants, first(isBean(uk.gov.moj.cpp.external.domain.progression.relist.Defendant.class)
                                                .with(uk.gov.moj.cpp.external.domain.progression.relist.Defendant::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId()))
                                                .with(uk.gov.moj.cpp.external.domain.progression.relist.Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getId, is(hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getId()))))))))
                        ))));


        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), standardShareResultsCommandTemplate(hearingOne.getHearingId()));

        publicHearingAdjourned2.waitFor();
    }

    private void stubReferenceData(final LocalDate referenceDate, final UUID primaryResultDefinitionId, final UUID mandatoryPromptId) {
        AllNows allNows = AllNows.allNows()
                .setNows(singletonList(
                        NowDefinition.now()
                                .setId(randomUUID())
                                .setTemplateName("nowsTemplateName0")
                                .setResultDefinitions(singletonList(
                                        ResultDefinitions.resultDefinitions()
                                                .setId(primaryResultDefinitionId)
                                                .setMandatory(true)
                                                .setPrimary(true)
                                ))
                ));

        System.out.println("stubbing now " + allNows.getNows().get(0).getId() + " at " + referenceDate);

        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows);
        final String userGroup1 = "DefenseCounsel";

        List<Prompt> promptDefs = asList(DATE_OF_HEARING_LABEL, HEARING_TYPE_LABEL, ESTIMATED_DURATION_LABEL, REMAND_STATUS_LABEL, TIME_OF_HEARING_LABEL).stream()
                .map(label -> Prompt.prompt()
                        .setMandatory(false)
                        .setId(UUID.randomUUID())
                        .setLabel(label)
                        .setUserGroups(singletonList(userGroup1))).collect(Collectors.toList());
        promptDefs.get(0).setMandatory(true);
        promptDefs.get(0).setId(mandatoryPromptId);

        AllResultDefinitions allResultDefinitions = AllResultDefinitions.allResultDefinitions().setResultDefinitions(
                singletonList(ResultDefinition.resultDefinition()
                        .setId(primaryResultDefinitionId)
                        .setUserGroups(singletonList(userGroup1))
                        .setPrompts(promptDefs)
                )
        );

        ReferenceDataStub.stubGetAllResultDefinitions(referenceDate, allResultDefinitions);
        ReferenceDataStub.stubRelistReferenceDataResults();
    }
}
