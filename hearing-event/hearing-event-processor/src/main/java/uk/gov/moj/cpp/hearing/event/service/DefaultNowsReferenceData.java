package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.ResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DefaultNowsReferenceData {

    public static final UUID nowId1 = UUID.fromString("6b2bc725-324b-420f-b603-8d7963065b39");
    public static final UUID resultDefinitionId1 = UUID.fromString("87631590-bd78-49b2-bd6f-ad7030904e73");
    public static final String PROMPT_LABEL1_1 = "promptLabel1_1Label";
    public static final String DEFENCE_COUNSEL_USER_GROUP = "defenceCounsel";

    public AllNows defaultAllNows() {
        return new AllNows().setNows(
                Arrays.asList(
                        new NowDefinition().setId(nowId1).setResultDefinitions(
                                Arrays.asList(
                                        new ResultDefinitions().setId(resultDefinitionId1)
                                                .setPrimary(true)
                                                .setMandatory(true)
                                )
                        )
                )
        );
    }

    public final ResultDefinition defaultResultDefinition1() {
        return new ResultDefinition().setId(resultDefinitionId1)
                .setPrompts(
                        Arrays.asList(
                                new Prompt().setLabel(PROMPT_LABEL1_1)
                                        .setUserGroups(Arrays.asList(DEFENCE_COUNSEL_USER_GROUP))
                        )
                );
    }

    public Map<UUID, ResultDefinition> defaultId2ResultDefinition() {
        HashMap result = new HashMap<>();
        ResultDefinition def1 = defaultResultDefinition1();
        result.put(def1.getId(), def1);
        return result;
    }


}
