package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.System.lineSeparator;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;


import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class ResultTextHelperTest {

    @Test
    public void shouldSetResultTextForPromptDirective(){
        final List<TreeNode<ResultLine>> treeNodeList = singletonList(new TreeNode<>(randomUUID(), null));

        treeNodeList.get(0).setJudicialResult(JudicialResult.judicialResult()
                .withResultTextTemplate("Exclusion Requirement: Not to enter {placeArea} between {fromDate} and {untilDate}. This exclusion requirement lasts until {endDateOfExclusion}. {additionalInformation}")
                .withJudicialResultPrompts(Arrays.asList(judicialResultPrompt()
                        .withPromptReference("placeArea")
                        .withValue("area1")
                        .withType("TXT")
                        .build(), judicialResultPrompt()
                        .withPromptReference("fromDate")
                        .withType("DATE")
                        .withValue("01-01-2012")
                        .build(), judicialResultPrompt()
                        .withPromptReference("untilDate")
                        .withType("DATE")
                        .withValue("01-01-2013")
                        .build(), judicialResultPrompt()
                        .withPromptReference("endDateOfExclusion")
                        .withType("DATE")
                        .withValue("01-01-2014")
                        .build(), judicialResultPrompt()
                        .withPromptReference("additionalInformation")
                        .withType("TXT")
                        .withValue("text prompt")
                        .build())
                )
                .build());


        ResultTextHelper.setResultText(treeNodeList);

        assertThat(treeNodeList.get(0).getJudicialResult().getResultText(), CoreMatchers.is ("Exclusion Requirement: Not to enter area1 between 01-01-2012 and 01-01-2013. This exclusion requirement lasts until 01-01-2014. text prompt"));
    }
}