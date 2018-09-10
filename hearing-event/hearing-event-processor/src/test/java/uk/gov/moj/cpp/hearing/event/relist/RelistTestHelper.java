package uk.gov.moj.cpp.hearing.event.relist;


import static java.util.UUID.fromString;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;

import uk.gov.justice.json.schemas.core.Defendant;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLine;
import uk.gov.moj.cpp.hearing.command.result.ResultPrompt;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPrompt;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPromptReference;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;


public interface RelistTestHelper {

    List<UUID> ARBITRARY_WITHDRAWN_META_DATA = Arrays.asList(
            fromString("6feb0f2e-8d1e-40c7-af2c-05b28c69e5fc"),
            fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8"),
            fromString("c8326b9e-56eb-406c-b74b-9f90c772b657"),
            fromString("eaecff82-32da-4cc1-b530-b55195485cc7"));


    static Map<UUID, NextHearingResultDefinition> arbitraryNextHearingMetaData() {
        NextHearingResultDefinition nextHearingResultDefinition = new NextHearingResultDefinition(UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"),
                new NextHearingPrompt(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"), NextHearingPromptReference.HDATE.name()));
        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22"), NextHearingPromptReference.HTYPE.name()));
        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac"), NextHearingPromptReference.HEST.name()));
        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"), NextHearingPromptReference.HTIME.name()));
        return ImmutableMap.of(UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"), nextHearingResultDefinition);
    }

    static ResultsShared getArbitrarySharedResultWithNextHearingResult() {
        ResultsShared ARBITRARY_RESULT_SHARED = resultsSharedTemplate();
        Defendant firstDefendant = ARBITRARY_RESULT_SHARED.getHearing().getProsecutionCases().get(0).getDefendants().get(0);
        ResultPrompt dateOfHearing = ResultPrompt.builder().withId(fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283")).withLabel("Date of hearing").withValue("07/072018").build();
        ResultPrompt hearingType = ResultPrompt.builder().withId(fromString("c1116d12-dd35-4171-807a-2cb845357d22")).withLabel("Hearing type").withValue("Trial").build();
        ResultPrompt estimatedDuration = ResultPrompt.builder().withId(fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac")).withLabel("Estimated duration").withValue("1 weeks,2 days").build();
        ResultPrompt remandStatus = ResultPrompt.builder().withId(fromString("9403f0d7-90b5-4377-84b4-f06a77811362")).withLabel("Remand Status").withValue("remand in custody").build();
        ResultPrompt startTime = ResultPrompt.builder().withId(fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d")).withLabel("Time Of Hearing").withValue("10:45").build();
        CompletedResultLine nextHearingResult = CompletedResultLine.builder()
                .withId(UUID.randomUUID())
                .withDefendantId(firstDefendant.getId())
                .withResultDefinitionId(fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"))
                .withResultLabel("Next Hearing")
                .withResultPrompts(Arrays.asList(dateOfHearing, hearingType, estimatedDuration, remandStatus, startTime))
                .withOffenceId(firstDefendant.getOffences().get(0).getId())
                .build();
        ARBITRARY_RESULT_SHARED.getCompletedResultLines().add(nextHearingResult);
        return ARBITRARY_RESULT_SHARED;
    }

    static ResultsShared getArbitrarySharedResult() {
        return resultsSharedTemplate();
    }

}
