package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

public class ResultTreeBuilderTest extends AbstractRestructuringTest {

    private ResultTreeBuilder target;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        target = new ResultTreeBuilder(referenceDataService, nextHearingHelper);
    }

    @Test
    public void shouldBuildSimpleTwoLayerTree() {
        final List<ResultDefinition> resultDefinitionList = resultDefinitions.stream().filter(resultDefinition ->
                REMAND_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_TO_HOSPITAL_ID.equals(resultDefinition.getId().toString())).collect(toList());

        final ResultsShared resultsShared = getResultsShared(resultDefinitionList);
        final List<TreeNode<ResultLine>> results = target.build(dummyEnvelope, resultsShared);

        assertThat(results.stream().filter(jr -> fromString(REMAND_IN_CUSTODY_ID).equals(jr.getResultDefinitionId())).findAny().get().getChildren().size(), is(0));
        assertThat(results.size(), is(3));
    }

    @Test
    public void shouldBuildSimplePromptWithValues() {
        final List<ResultDefinition> resultDefinitionList = resultDefinitions.stream().filter(resultDefinition ->
                REMAND_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_TO_HOSPITAL_ID.equals(resultDefinition.getId().toString())).collect(toList());
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
        final List<ResultDefinition> resultDefinitionList = resultDefinitions.stream().filter(resultDefinition ->
                REMAND_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_ID.equals(resultDefinition.getId().toString()) ||
                        REMANDED_IN_CUSTODY_TO_HOSPITAL_ID.equals(resultDefinition.getId().toString())).collect(toList());
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
    public void shouldGetRootResultLineFromGrandChild() {
        final ResultLine parent = buildResultLine(null);
        final ResultLine child = buildResultLine(parent.getResultLineId());
        final ResultLine grandChild = buildResultLine(child.getResultLineId());
        final List<ResultLine> resultLineList = asList(parent, child, grandChild);
        final ResultLine rootResultLine = target.getRootResultLine(resultLineList, grandChild);
        assertThat(rootResultLine, is(notNullValue()));
        assertThat(rootResultLine.getResultLineId(), is(parent.getResultLineId()));
        assertThat(rootResultLine.getResultDefinitionId(), is(parent.getResultDefinitionId()));
    }

    @Test
    public void shouldGetRootResultLineFromChild() {
        final ResultLine parent = buildResultLine(null);
        final ResultLine child = buildResultLine(parent.getResultLineId());
        final List<ResultLine> resultLineList = asList(parent, child);
        final ResultLine rootResultLine = target.getRootResultLine(resultLineList, child);
        assertThat(rootResultLine, is(notNullValue()));
        assertThat(rootResultLine.getResultLineId(), is(parent.getResultLineId()));
        assertThat(rootResultLine.getResultDefinitionId(), is(parent.getResultDefinitionId()));
    }

    @Test
    public void shouldGetRootResultLineFromParent() {
        final ResultLine parent = buildResultLine(null);
        final List<ResultLine> resultLineList = asList(parent);
        final ResultLine rootResultLine = target.getRootResultLine(resultLineList, parent);
        assertThat(rootResultLine, is(notNullValue()));
        assertThat(rootResultLine.getResultLineId(), is(parent.getResultLineId()));
        assertThat(rootResultLine.getResultDefinitionId(), is(parent.getResultDefinitionId()));
    }

    private ResultLine buildResultLine(UUID parentResultId) {
        final ResultLine.Builder resultLineBuilder = ResultLine.resultLine()
                .withResultLineId(randomUUID())
                .withResultDefinitionId(randomUUID());

        if (nonNull(parentResultId)) {
            resultLineBuilder.withParentResultLineIds(singletonList(parentResultId));
        }

        return resultLineBuilder.build();
    }
}
