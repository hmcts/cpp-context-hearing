package uk.gov.moj.cpp.hearing.event.relist;


import static java.util.UUID.fromString;
import static uk.gov.moj.cpp.hearing.event.NowsTemplates.resultsSharedTemplate;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
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
    String DATE_OF_HEARING_LABEL = "Date of hearing";
    String HEARING_TYPE_LABEL = "Hearing type";
    String ESTIMATED_DURATION_LABEL = "Estimated duration";
    String REMAND_STATUS_LABEL = "Remand Status";
    String TIME_OF_HEARING_LABEL = "Time Of Hearing";
    String COURT_CENTRE_LABEL = "Courthouse name";
    String COURT_ROOM_LABEL = "Courtroom";

    UUID HTIME_PROMPT_ID= UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d");
    UUID HTYPE_PROMPT_ID= UUID.fromString("c1116d12-dd35-4171-807a-2cb845357d22");
    UUID HEST_PROMPT_ID = UUID.fromString("d85cc2d7-66c8-471e-b6ff-c1bc60c6cdac");
    UUID HCROOM_PROMPT_ID =UUID.fromString("5f507153-6dc9-4ec0-94db-c821eff333f1");
    UUID HCHOUSE_PROMPT_ID =UUID.fromString("7746831a-d5dd-4fa8-ac13-528573948c8a");



    static Map<UUID, NextHearingResultDefinition> arbitraryNextHearingMetaData() {
        NextHearingResultDefinition nextHearingResultDefinition = new NextHearingResultDefinition(UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"),
                new NextHearingPrompt(UUID.fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283"), NextHearingPromptReference.HDATE.name()));
        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(HTIME_PROMPT_ID, NextHearingPromptReference.HTIME.name()));

        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(HTYPE_PROMPT_ID, NextHearingPromptReference.HTYPE.name()));
        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(HEST_PROMPT_ID, NextHearingPromptReference.HEST.name()));
//        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(UUID.fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d"),
//                NextHearingPromptReference.HTIME.name()));
        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(HCROOM_PROMPT_ID, NextHearingPromptReference.HCROOM.name()));
        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(HCHOUSE_PROMPT_ID, NextHearingPromptReference.HCHOUSE.name()));
        return ImmutableMap.of(UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"), nextHearingResultDefinition);
    }



    static ResultsShared getArbitrarySharedResultWithNextHearingResult() {
        ResultsShared ARBITRARY_RESULT_SHARED = resultsSharedTemplate();
        Prompt dateOfHearing = Prompt.prompt().withId(fromString("d27a5d86-d51f-4c6e-914b-cb4b0abc4283")).withLabel(DATE_OF_HEARING_LABEL).withValue("12/12/2018").build();
        Prompt hearingType = Prompt.prompt().withId(HTYPE_PROMPT_ID).withLabel(HEARING_TYPE_LABEL).withValue("Trial").build();
        Prompt estimatedDuration = Prompt.prompt().withId(HEST_PROMPT_ID).withLabel(ESTIMATED_DURATION_LABEL).withValue("1 weeks,2 days").build();
        Prompt courtRoom = Prompt.prompt().withId(HCROOM_PROMPT_ID).withLabel(COURT_ROOM_LABEL).withValue("Room A").build();
        Prompt courtHouse = Prompt.prompt().withId(HCHOUSE_PROMPT_ID).withLabel(COURT_CENTRE_LABEL).withValue("Wimbledon Magistractes").build();
        Prompt remandStatus = Prompt.prompt().withId(fromString("9403f0d7-90b5-4377-84b4-f06a77811362")).withLabel(REMAND_STATUS_LABEL).withValue("remand in custody").build();
        Prompt startTime = Prompt.prompt().withId(fromString("dfac671c-5b85-42a1-bb66-9aeee388a08d")).withLabel(TIME_OF_HEARING_LABEL).withValue("10:45").build();

        ResultLine nextHearingResult = ResultLine.resultLine()
                .withResultLineId(UUID.randomUUID())
                .withResultDefinitionId(fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"))
                .withResultLabel("Next Hearing")
                .withPrompts(Arrays.asList(dateOfHearing, hearingType, estimatedDuration, remandStatus, startTime, courtHouse, courtRoom))
                .build();
        ARBITRARY_RESULT_SHARED.getTargets().get(0).getResultLines().add(nextHearingResult);
        return ARBITRARY_RESULT_SHARED;
    }


    static ResultsShared getArbitrarySharedResult() {
        return resultsSharedTemplate();
    }

}
