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
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class HearingAdjournIT extends AbstractIT {
    private static final String DD_MM_YYYY = "dd/MM/yyyy";
    private static final String YYYY_MM_DD = "yyyy-MM-dd";

    @Test
    public void shouldRaiseHearingAdjournedEvent() {

        LocalDate orderedDate = PAST_LOCAL_DATE.next();
        UUID resultLineId = randomUUID();

        stubReferenceData(orderedDate, randomUUID(), randomUUID());

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
                                .setResultDefinitionId(UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"))
                                .setOrderedDate(orderedDate)
                                .setPrompts(asList(
                                        prompt().withId(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"))
                                                .withLabel("Date of hearing")
                                                .withValue("02/07/2018")
                                                .build(),
                                        prompt().withId(UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22"))
                                                .withLabel("Hearing type")
                                                .withValue("Plea & Trial Preparation")
                                                .build(),
                                        prompt().withId(UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac"))
                                                .withLabel("Estimated duration")
                                                .withValue("59 Minutes")
                                                .build(),
                                        prompt().withId(UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362"))
                                                .withLabel("Remand Status")
                                                .withValue("remand in custody")
                                                .build(),
                                        prompt().withId(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"))
                                                .withLabel("Time Of Hearing")
                                                .withValue("10.30")
                                                .build()
                                ));
                    })));

        }));

        Utilities.EventListener publicHearingAdjourned = listenFor("public.hearing.adjourned")
                .withFilter(isJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(hearingOne.getFirstCase().getId().toString())),
                        withJsonPath("$.urn", is(hearingOne.getFirstCase().getProsecutionCaseIdentifier().getCaseURN())),
                        withJsonPath("$.hearings[0].type", is("Plea & Trial Preparation")),
                        withJsonPath("$.hearings[0].startDate", is(convertDate("02/07/2018"))),
                        withJsonPath("$.hearings[0].startTime", is("10.30")),
                        withJsonPath("$.hearings[0].estimateMinutes", is(59))
                )));


        UseCases.shareResults(requestSpec, hearingOne.getHearingId(), standardShareResultsCommandTemplate(hearingOne.getHearingId()));

        publicHearingAdjourned.waitFor();


        UseCases.saveDraftResults(requestSpec, with(saveDraftResultCommand, saveDraftCommand -> {

            saveDraftCommand.getTarget()

                    .setResultLines(asList(with(resultLine(resultLineId), resultLine -> {
                        resultLine.setResultLabel("Next Hearing")
                                .setResultDefinitionId(UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"))
                                .setOrderedDate(orderedDate)
                                .setPrompts(asList(
                                        prompt().withId(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"))
                                                .withLabel("Date of hearing")
                                                .withValue("02/08/2018")
                                                .build(),
                                        prompt().withId(UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22"))
                                                .withLabel("Hearing type")
                                                .withValue("Sentencing")
                                                .build(),
                                        prompt().withId(UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac"))
                                                .withLabel("Estimated duration")
                                                .withValue("30 Minutes")
                                                .build(),
                                        prompt().withId(UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362"))
                                                .withLabel("Remand Status")
                                                .withValue("remand in custody")
                                                .build(),
                                        prompt().withId(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"))
                                                .withLabel("Time Of Hearing")
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
                                .setResultDefinitions(singletonList(

                                        ResultDefinitions.resultDefinitions()
                                                .setId(primaryResultDefinitionId)
                                                .setMandatory(true)
                                                .setPrimary(true)
                                ))
                ));

        ReferenceDataStub.stubGetAllNowsMetaData(referenceDate, allNows);
        final String userGroup1 = "DefenseCounsel";
        AllResultDefinitions allResultDefinitions = AllResultDefinitions.allResultDefinitions().setResultDefinitions(
                singletonList(ResultDefinition.resultDefinition()
                        .setId(primaryResultDefinitionId)
                        .setUserGroups(singletonList(userGroup1))
                        .setPrompts(
                                singletonList(
                                        Prompt.prompt().setId(mandatoryPromptId)
                                                .setMandatory(true)
                                                .setLabel("label1")
                                                .setUserGroups(singletonList(userGroup1))
                                )
                        )
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
