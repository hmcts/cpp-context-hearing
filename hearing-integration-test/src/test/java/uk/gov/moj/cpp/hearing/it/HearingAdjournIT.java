package uk.gov.moj.cpp.hearing.it;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
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

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
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
import java.time.format.DateTimeFormatter;
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

        Utilities.EventListener publicHearingAdjourned = listenFor("public.hearing.adjourned")
                .withFilter(isJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString()))/*,
                        withJsonPath("$.urn", is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getCaseURN())),
                        withJsonPath("$.hearings[0].type", is("Plea & Trial Preparation")),
                        withJsonPath("$.hearings[0].startDate", is(convertDate("02/07/2018"))),
                        withJsonPath("$.hearings[0].startTime", is("10.30")),
                        withJsonPath("$.hearings[0].estimateMinutes", is(59))*/
                )));


        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), standardShareResultsCommandTemplate(hearingOne.getHearingId()));

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

        Utilities.EventListener publicHearingAdjourned2 = listenFor("public.hearing.adjourned")
                .withFilter(isJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())),
                        withJsonPath("$.urn", is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getCaseURN())),
                        withJsonPath("$.hearings[0].type", is("Sentencing")),
                        withJsonPath("$.hearings[0].startDate", is(convertDate("02/08/2018"))),
                        withJsonPath("$.hearings[0].startTime", is("11.30")),
                        withJsonPath("$.hearings[0].estimateMinutes", is(30))
                )));

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
                .map(label->Prompt.prompt()
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

    private String convertDate(final String date) {
        DateTimeFormatter from = DateTimeFormatter.ofPattern(DD_MM_YYYY);
        DateTimeFormatter to = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return LocalDate.parse(date, from).format(to);
    }
}
