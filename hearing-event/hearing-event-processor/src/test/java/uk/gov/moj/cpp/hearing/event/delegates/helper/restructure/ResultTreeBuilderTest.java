package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.IMP_TIMP_HEARING_RESULTS_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.NEXT_HEARING_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.NEXT_HEARING_IN_CROWN_COURT_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.NEXT_HEARING_IN_MAGISTRATE_COURT_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REMANDED_IN_CUSTODY_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REMANDED_IN_CUSTODY_TO_HOSPITAL_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REMANDED_ON_CONDITIONAL_BAIL_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.REMAND_IN_CUSTODY_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.SCENARIO_1_SHORT_CODE_SEND_TO_CCON_CB_JSON;


import java.time.LocalDate;
import org.mockito.Mockito;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.SecondaryCJSCode;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ResultTreeBuilderTest extends AbstractRestructuringTest {

    private ResultTreeBuilder target;
    private List<ResultDefinition> resultDefinitionList;

    @Before
    public void setUp() throws IOException {
        ResultTextConfHelper resultTextConfHelper = Mockito.mock(ResultTextConfHelper.class);
        when(resultTextConfHelper.isOldResultDefinition(any(LocalDate.class))).thenReturn(false);
        super.setUp();
        target = new ResultTreeBuilder(referenceDataService, nextHearingHelper, resultLineHelper, resultTextConfHelper);
        resultDefinitionList = resultDefinitions.stream().filter(resultDefinition ->
                REMAND_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_TO_HOSPITAL_ID.equals(resultDefinition.getId().toString())).collect(toList());
    }

    @Test
    public void shouldBuildSimpleTwoLayerTree() {
        final ResultsShared resultsShared = getResultsShared(resultDefinitionList);
        final List<TreeNode<ResultLine>> results = target.build(dummyEnvelope, resultsShared);
        final TreeNode<ResultLine> firstTreeNode = results.stream().filter(jr -> fromString(REMAND_IN_CUSTODY_ID).equals(jr.getResultDefinitionId())).findAny().get();

        assertThat(firstTreeNode.getChildren().size(), is(0));
        assertThat(results.size(), is(3));

        final TreeNode<ResultLine> secondTreeNode = results.stream().filter(jr -> fromString(REMANDED_IN_CUSTODY_ID).equals(jr.getResultDefinitionId())).findAny().get();
        final JudicialResult judicialResult = secondTreeNode.getJudicialResult();
        final List<SecondaryCJSCode> secondaryCjsCodes = judicialResult.getSecondaryCJSCodes();
        assertThat(secondaryCjsCodes, hasSize(2));
        assertThat(secondaryCjsCodes.get(0).getCjsCode(), notNullValue());
        assertThat(secondaryCjsCodes.get(0).getText(), notNullValue());
        assertThat(secondaryCjsCodes.get(1).getCjsCode(), notNullValue());
        assertThat(secondaryCjsCodes.get(1).getText(), notNullValue());
        assertThat(judicialResult.getDrivingTestStipulation(),notNullValue());
        assertThat(judicialResult.getPointsDisqualificationCode(),notNullValue());
        assertThat(judicialResult.getDvlaCode(), notNullValue());
    }

    @Test
    public void shouldBuildSimplePromptWithValues() {
        final String promptValue = "abc";
        final ResultsShared resultsShared = getResultsSharedWithPromptValue(resultDefinitionList, promptValue);
        final List<TreeNode<ResultLine>> results = target.build(dummyEnvelope, resultsShared);

        assertThat(results.stream().filter(jr -> fromString(REMAND_IN_CUSTODY_ID).equals(jr.getResultDefinitionId())).findAny().get().getChildren().size(), is(0));
        assertThat(results.size(), is(3));
        assertTrue(results.stream().filter(result -> nonNull(result.getJudicialResult()))
                .map(TreeNode::getJudicialResult)
                .filter(judicialResult -> nonNull(judicialResult.getJudicialResultPrompts()))
                .flatMap(judicialResult -> judicialResult.getJudicialResultPrompts().stream())
                .filter(judicialResultPrompt -> nonNull(judicialResultPrompt.getValue()))
                .allMatch(judicialResultPrompt -> judicialResultPrompt.getValue().equals(promptValue)));
    }

    @Test
    public void shouldBuildSimplePromptWithValuesCommaSeparated() {
        final String promptValue = "abc###def";
        final ResultsShared resultsShared = getResultsSharedWithPromptValue(resultDefinitionList, promptValue);
        final List<TreeNode<ResultLine>> results = target.build(dummyEnvelope, resultsShared);

        assertThat(results.stream().filter(jr -> fromString(REMAND_IN_CUSTODY_ID).equals(jr.getResultDefinitionId())).findAny().get().getChildren().size(), is(0));
        assertThat(results.size(), is(3));
        assertTrue(results.stream().filter(result -> nonNull(result.getJudicialResult()))
                .map(TreeNode::getJudicialResult)
                .filter(judicialResult -> nonNull(judicialResult.getJudicialResultPrompts()))
                .flatMap(judicialResult -> judicialResult.getJudicialResultPrompts().stream())
                .filter(judicialResultPrompt -> nonNull(judicialResultPrompt.getValue()))
                .allMatch(judicialResultPrompt -> judicialResultPrompt.getValue().equals("abc, def")));
    }

    @Test
    public void shouldBuildSimplePromptWithWelshValuesCommaSeparated() {
        final String promptValue = "abc###def";
        final ResultsShared resultsShared = getResultsSharedWithWelshPromptValue(resultDefinitionList, promptValue);
        final List<TreeNode<ResultLine>> results = target.build(dummyEnvelope, resultsShared);

        assertThat(results.stream().filter(jr -> fromString(REMAND_IN_CUSTODY_ID).equals(jr.getResultDefinitionId())).findAny().get().getChildren().size(), is(0));
        assertThat(results.size(), is(3));
        assertTrue(results.stream().filter(result -> nonNull(result.getJudicialResult()))
                .map(TreeNode::getJudicialResult)
                .filter(judicialResult -> nonNull(judicialResult.getJudicialResultPrompts()))
                .flatMap(judicialResult -> judicialResult.getJudicialResultPrompts().stream())
                .filter(judicialResultPrompt -> nonNull(judicialResultPrompt.getValue()))
                .allMatch(judicialResultPrompt -> judicialResultPrompt.getWelshValue().equals("abc, def")));
    }

    @Test
    public void shouldBuildMultipleTrees() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.size(), is(16));

        final List<TreeNode<ResultLine>> topLevelParents = filterBy(resultLineTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

        assertThat(topLevelParents.size(), is(4));

        final TreeNode<ResultLine> remandedOnConditionalBailResult = resultLineTree.stream().filter(jr -> fromString(REMANDED_ON_CONDITIONAL_BAIL_ID).equals(jr.getResultDefinitionId())).findAny().get();

        assertThat(remandedOnConditionalBailResult.getResultDefinitionId().toString(), is(REMANDED_ON_CONDITIONAL_BAIL_ID));
        assertThat(remandedOnConditionalBailResult.getChildren().size(), is(1));

        final TreeNode<ResultLine> nextHearingResult = remandedOnConditionalBailResult.getChildren().get(0);
        assertThat(nextHearingResult.getResultDefinitionId().toString(), is(NEXT_HEARING_ID));
        assertThat(nextHearingResult.getChildren().size(), is(1));

        final List<TreeNode<ResultLine>> nextHearingInCrownCourtResults = nextHearingResult.getChildren();

        assertTrue(nextHearingInCrownCourtResults
                .stream()
                .allMatch(r -> NEXT_HEARING_IN_CROWN_COURT_ID.equals(r.getResultDefinitionId().toString()) || NEXT_HEARING_IN_MAGISTRATE_COURT_ID.equals(r.getResultDefinitionId().toString())));
    }

    @Test
    public void shouldBuildWhenOneDefendantAndOneOffenceResultShared() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.size(), is(11));

        final List<TreeNode<ResultLine>> topLevelParents = filterBy(resultLineTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

        assertThat(topLevelParents.size(), is(2));

        final TreeNode<ResultLine> remandedOnConditionalBailResult = resultLineTree.stream().filter(jr -> fromString(REMANDED_ON_CONDITIONAL_BAIL_ID).equals(jr.getResultDefinitionId())).findAny().get();

        assertThat(remandedOnConditionalBailResult.getResultDefinitionId().toString(), is(REMANDED_ON_CONDITIONAL_BAIL_ID));
        assertThat(remandedOnConditionalBailResult.getChildren().size(), is(2));

        final TreeNode<ResultLine> nextHearingResult = remandedOnConditionalBailResult.getChildren().get(0);

        assertThat(nextHearingResult.getResultDefinitionId().toString(), is(NEXT_HEARING_ID));
        assertThat(nextHearingResult.getChildren().size(), is(5));

        final List<TreeNode<ResultLine>> nextHearingInCrownCourtResults = nextHearingResult.getChildren();

        assertTrue(nextHearingInCrownCourtResults
                .stream()
                .allMatch(r -> NEXT_HEARING_IN_CROWN_COURT_ID.equals(r.getResultDefinitionId().toString()) || NEXT_HEARING_IN_MAGISTRATE_COURT_ID.equals(r.getResultDefinitionId().toString())));
    }

    @Test
    public void shouldBuildSuccessfullyWhenDefendantSingleOffenceOneImpOneTimpHearingResultShared() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(IMP_TIMP_HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.size(), is(2));
        assertThat(resultLineTree.get(0).getJudicialResult().getJudicialResultPrompts().size(), is(2));
        assertThat(resultLineTree.get(1).getJudicialResult().getJudicialResultPrompts().size(), is(3));

        final List<TreeNode<ResultLine>> topLevelResultLineParents = filterBy(resultLineTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

        assertThat(topLevelResultLineParents.size(), is(1));
        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(1));
    }

    @Test
    public void shouldBuildSuccessfullyScenario1ShortCodeSendToCCOnCB() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(SCENARIO_1_SHORT_CODE_SEND_TO_CCON_CB_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.size(), is(9));

        final List<TreeNode<ResultLine>> topLevelResultLineParents = filterBy(resultLineTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

        assertThat(topLevelResultLineParents.size(), is(1));
        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(1));
    }

    @Test
    public void shouldBuildSuccessfullyWithJudicialResult() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(SCENARIO_1_SHORT_CODE_SEND_TO_CCON_CB_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.size(), is(9));
        final List<TreeNode<ResultLine>> topLevelResultLineParents = filterBy(resultLineTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);
        assertThat(topLevelResultLineParents.size(), is(1));
        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(1));
    }

    @Test
    public void shouldBuildSuccessfullyWithJudicialResultWhenResultLineIdExistsInNewAmendedResults() throws IOException {
        final ResultsSharedV2 resultsShared = fileResourceObjectMapper.convertFromFile("judicial-result-with-newAmendedResults.json", ResultsSharedV2.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.size(), is(9));
        final List<TreeNode<ResultLine>> topLevelResultLineParents = filterBy(resultLineTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);
        assertThat(topLevelResultLineParents.size(), is(1));
        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(1));
        assertThat(topLevelResultLineParents.get(0).getJudicialResult().getIsNewAmendment(),is(true));
    }

    @Test
    public void shouldBuildSuccessfullyWithJudicialResultWhenResultLineIdNotExistsInNewAmendedResults() throws IOException {
        final ResultsSharedV2 resultsShared = fileResourceObjectMapper.convertFromFile("judicial-result-without-newAmendedResults.json", ResultsSharedV2.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.size(), is(9));
        final List<TreeNode<ResultLine>> topLevelResultLineParents = filterBy(resultLineTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);
        assertThat(topLevelResultLineParents.size(), is(1));
        assertThat(topLevelResultLineParents.get(0).getChildren().size(), is(1));
        assertThat(topLevelResultLineParents.get(0).getJudicialResult().getIsNewAmendment(), is(false));
    }

    @Test
    public void shouldOrderResultsForDependantResultDefinitionGroup() throws IOException {
        final ResultsSharedV2 resultsShared = fileResourceObjectMapper.convertFromFile("judicial-result-for-ordering.json", ResultsSharedV2.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.get(0).getResultDefinition().getData().getShortCode(), is("SUSPS"));
        assertThat(resultLineTree.get(0).getResultDefinition().getData().getDependantResultDefinitionGroup(), is("Community Requirements"));

        assertThat(resultLineTree.get(1).getResultDefinition().getData().getShortCode(), is("CRS"));
        assertThat(resultLineTree.get(1).getResultDefinition().getData().getResultDefinitionGroup(), is("Community Requirements, ABC"));

        assertThat(resultLineTree.get(2).getResultDefinition().getData().getShortCode(), is("STIMP"));
        assertThat(resultLineTree.get(2).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));
        assertThat(resultLineTree.get(2).getResultDefinition().getData().getResultDefinitionGroup(), is("Community Requirements"));

        assertThat(resultLineTree.get(3).getResultDefinition().getData().getShortCode(), is("EMREQ"));
        assertThat(resultLineTree.get(3).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));

        assertThat(resultLineTree.get(4).getResultDefinition().getData().getShortCode(), is("FRHS"));
        assertThat(resultLineTree.get(4).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));

        assertThat(resultLineTree.get(5).getResultDefinition().getData().getShortCode(), is("NORDRC"));
        assertThat(resultLineTree.get(5).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));

        assertThat(resultLineTree.get(6).getResultDefinition().getData().getShortCode(), is("RR"));
        assertThat(resultLineTree.get(6).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));

        assertThat(resultLineTree.get(7).getResultDefinition().getData().getShortCode(), is("UPWR"));
        assertThat(resultLineTree.get(7).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));


    }

    @Test
    public void shouldOrderResultsForDependantResultDefinitionGroupForApplication() throws IOException {
        final ResultsSharedV2 resultsShared = fileResourceObjectMapper.convertFromFile("judicial-result-for-ordering-for-application.json", ResultsSharedV2.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLineTree = target.build(envelope, resultsShared);

        assertThat(resultLineTree.get(0).getResultDefinition().getData().getShortCode(), is("SUSPS"));
        assertThat(resultLineTree.get(0).getResultDefinition().getData().getDependantResultDefinitionGroup(), is("Community Requirements"));

        assertThat(resultLineTree.get(1).getResultDefinition().getData().getShortCode(), is("CRS"));
        assertThat(resultLineTree.get(1).getResultDefinition().getData().getResultDefinitionGroup(), is("Community Requirements, ABC"));

        assertThat(resultLineTree.get(2).getResultDefinition().getData().getShortCode(), is("STIMP"));
        assertThat(resultLineTree.get(2).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));
        assertThat(resultLineTree.get(2).getResultDefinition().getData().getResultDefinitionGroup(), is("Community Requirements"));

        assertThat(resultLineTree.get(3).getResultDefinition().getData().getShortCode(), is("EMREQ"));
        assertThat(resultLineTree.get(3).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));

        assertThat(resultLineTree.get(4).getResultDefinition().getData().getShortCode(), is("FRHS"));
        assertThat(resultLineTree.get(4).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));

        assertThat(resultLineTree.get(5).getResultDefinition().getData().getShortCode(), is("NORDRC"));
        assertThat(resultLineTree.get(5).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));

        assertThat(resultLineTree.get(6).getResultDefinition().getData().getShortCode(), is("RR"));
        assertThat(resultLineTree.get(6).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));

        assertThat(resultLineTree.get(7).getResultDefinition().getData().getShortCode(), is("UPWR"));
        assertThat(resultLineTree.get(7).getResultDefinition().getData().getDependantResultDefinitionGroup(), is(nullValue()));


    }
}
