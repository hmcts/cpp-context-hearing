package uk.gov.moj.cpp.hearing.it;


import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.it.TestUtilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.standardShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
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
import java.util.Arrays;
import java.util.UUID;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class HearingAdjournIT extends AbstractIT {
    private static final String DD_MM_YYYY = "dd/MM/yyyy";
    private static final String YYYY_MM_DD = "yyyy-MM-dd";

    @Test
    public void shouldRaiseHearingAdjournedEvent() {

        stubReferenceData(UUID.randomUUID(), UUID.randomUUID());

        final CommandHelpers.InitiateHearingCommandHelper hearingOne = h(UseCases.initiateHearing(requestSpec, standardInitiateHearingTemplate()));

        givenAUserHasLoggedInAsACourtClerk(USER_ID_VALUE);

        final ArbitraryNextHearingPromptValues arbitraryNextHearingPromptValues = new ArbitraryNextHearingPromptValues("Plea & Trial Preparation", "02/07/2018", "10.30", 59);

        TestUtilities.EventListener publicHearingAdjourned = getPublicHearingAdjournedEventListener(hearingOne, arbitraryNextHearingPromptValues);

        // share result with addition result next hearing completed result
        new CommandHelpers.ShareResultsCommandHelper(
                UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                        standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), UUID.randomUUID(), UUID.randomUUID()),
                        command -> {
                            CompletedResultLine firstRecord = command.getCompletedResultLines().get(0);
                            CompletedResultLine nextHearing = getNextHearingCompletedResultLine(firstRecord, arbitraryNextHearingPromptValues, "59 Minutes");
                            command.getCompletedResultLines().add(1, nextHearing);
                            command.getCompletedResultLines().forEach(rl -> rl.setDefendantId(hearingOne.getFirstDefendantId()));
                        })
                )
        );
        publicHearingAdjourned.waitFor();

        //Update hearing type, start date, start time  and estimated minutes
        final ArbitraryNextHearingPromptValues arbitraryNextHearingPromptUpdatedValues = new ArbitraryNextHearingPromptValues("Sentencing", "02/08/2018", "11.30", 30);
        publicHearingAdjourned = getPublicHearingAdjournedEventListener(hearingOne, arbitraryNextHearingPromptUpdatedValues);

        // update same next hearing  result
        new CommandHelpers.ShareResultsCommandHelper(
                UseCases.shareResults(requestSpec, hearingOne.getHearingId(), with(
                        standardShareResultsCommandTemplate(hearingOne.getFirstDefendantId(), hearingOne.getFirstOffenceIdForFirstDefendant(), hearingOne.getFirstCaseId(), UUID.randomUUID(), UUID.randomUUID()),
                        command -> {
                            CompletedResultLine firstRecord = command.getCompletedResultLines().get(0);
                            CompletedResultLine nextHearing = getNextHearingCompletedResultLine(firstRecord, arbitraryNextHearingPromptUpdatedValues, "30 Minutes");
                            command.getCompletedResultLines().add(1, nextHearing);
                            command.getCompletedResultLines().forEach(rl -> rl.setDefendantId(hearingOne.getFirstDefendantId()));
                        })
                )
        );
        publicHearingAdjourned.waitFor();

    }

    private TestUtilities.EventListener getPublicHearingAdjournedEventListener(final CommandHelpers.InitiateHearingCommandHelper hearingOne, final ArbitraryNextHearingPromptValues arbitraryNextHearingPromptValues) {
        return listenFor("public.hearing.adjourned")
                .withFilter(isJson(CoreMatchers.allOf(
                        withJsonPath("$.caseId", is(hearingOne.getFirstCaseId().toString())),
                        withJsonPath("$.urn", is(hearingOne.getFirstCaseUrn().toString())),
                        withJsonPath("$.hearings[0].type", is(arbitraryNextHearingPromptValues.hearingType)),
                        withJsonPath("$.hearings[0].startDate", is(convertDate(arbitraryNextHearingPromptValues.getStartDate()))),
                        withJsonPath("$.hearings[0].startTime", is(arbitraryNextHearingPromptValues.getStartTime())),
                        withJsonPath("$.hearings[0].estimateMinutes", is(arbitraryNextHearingPromptValues.getEstimateMinutes()))
                )));
    }

    private CompletedResultLine getNextHearingCompletedResultLine(final CompletedResultLine firstRecord, final ArbitraryNextHearingPromptValues arbitraryNextHearingPromptValues, final String durationValue) {
        ResultPrompt dateOfHearing = ResultPrompt.builder().withId(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283")).withLabel("Date of hearing").withValue(arbitraryNextHearingPromptValues.getStartDate()).build();
        ResultPrompt hearingType = ResultPrompt.builder().withId(UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22")).withLabel("Hearing type").withValue(arbitraryNextHearingPromptValues.getHearingType()).build();
        ResultPrompt estimatedDuration = ResultPrompt.builder().withId(UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac")).withLabel("Estimated duration").withValue(durationValue).build();
        ResultPrompt remandStatus = ResultPrompt.builder().withId(UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362")).withLabel("Remand Status").withValue("remand in custody").build();
        ResultPrompt startTime = ResultPrompt.builder().withId(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d")).withLabel("Time Of Hearing").withValue(arbitraryNextHearingPromptValues.startTime).build();
        return CompletedResultLine.builder()
                .withId(UUID.randomUUID())
                .withCaseId(firstRecord.getCaseId())
                .withDefendantId(firstRecord.getDefendantId())
                .withLevel(firstRecord.getLevel())
                .withResultDefinitionId(UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"))
                .withResultLabel("Next Hearing")
                .withResultPrompts(Arrays.asList(dateOfHearing, hearingType, estimatedDuration, remandStatus, startTime))
                .withOffenceId(firstRecord.getOffenceId())
                .withOrderedDate(firstRecord.getOrderedDate())
                .build();
    }

    private void stubReferenceData(final UUID primaryResultDefinitionId, final UUID mandatoryPromptId) {
        AllNows allNows = AllNows.allNows()
                .setNows(singletonList(
                        NowDefinition.now()
                                .setId(UUID.randomUUID())
                                .setResultDefinitions(singletonList(

                                        ResultDefinitions.resultDefinitions()
                                                .setId(primaryResultDefinitionId)
                                                .setMandatory(true)
                                                .setPrimary(true)
                                ))
                ));

        ReferenceDataStub.stubGetAllNowsMetaData(allNows);
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

        ReferenceDataStub.stubGetAllResultDefinitions(allResultDefinitions);
        ReferenceDataStub.stubRelistReferenceDataResults();
    }

    private String convertDate(final String date) {
        DateTimeFormatter from = DateTimeFormatter.ofPattern(DD_MM_YYYY);
        DateTimeFormatter to = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        return LocalDate.parse(date, from).format(to);
    }

    private class ArbitraryNextHearingPromptValues {
        final String hearingType;
        final String startDate;
        final String startTime;
        final int estimateMinutes;

        public ArbitraryNextHearingPromptValues(final String hearingType, final String startDate, final String startTime, final int estimateMinutes) {
            this.hearingType = hearingType;
            this.startDate = startDate;
            this.startTime = startTime;
            this.estimateMinutes = estimateMinutes;
        }

        public String getHearingType() {
            return hearingType;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getStartTime() {
            return startTime;
        }

        public int getEstimateMinutes() {
            return estimateMinutes;
        }
    }
}
