package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.CO_HEARING_EVENT_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.DIRS_HEARING_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.IMP_TIMP_HEARING_RESULTS_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.RESULT_TEXT_SPLIT_REGEX;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.SCENARIO_1_SHORT_CODE_SEND_TO_CCON_CB_JSON;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultQualifier;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

public class RestructuringHelperTest extends AbstractRestructuringTest {

    private ResultTreeBuilder resultTreeBuilder;
    private RestructuringHelper target;

    @Before
    public void setUp() throws IOException {
        super.setUp();
        resultTreeBuilder = new ResultTreeBuilder(referenceDataService, nextHearingHelper, resultLineHelper);
        target = new RestructuringHelper(resultTreeBuilder);
    }

    @Test
    public void shouldRestructureSuccessfullyWhenSingleDefendantSingleOffenceOneImpOneTimpHearingResultShared() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(IMP_TIMP_HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> resultLinesTree = resultTreeBuilder.build(envelope, resultsShared);
        final List<TreeNode<ResultLine>> topLevelResultLineParents = filterBy(resultLinesTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);
        final List<TreeNode<ResultLine>> restructuredTree = target.restructure(envelope, resultsShared);

        assertThat(restructuredTree.size(), is(1));

        final JudicialResult judicialResult = restructuredTree.get(0).getJudicialResult();

        assertThat(judicialResult.getJudicialResultPrompts().size(), is(5));
        assertNull(judicialResult.getDelegatedPowers());
        assertThat(judicialResult.getResultText().split(RESULT_TEXT_SPLIT_REGEX).length, is(6));
        assertThat(judicialResult.getJudicialResultTypeId(), is(topLevelResultLineParents.get(0).getResultDefinitionId()));
        assertTrue(judicialResult.getJudicialResultPrompts().stream().allMatch(jrp -> nonNull(jrp.getJudicialResultPromptTypeId())));
        assertTrue(judicialResult.getTerminatesOffenceProceedings());
        assertFalse(judicialResult.getLifeDuration());
        assertFalse(judicialResult.getPublishedAsAPrompt());
        assertFalse(judicialResult.getAlwaysPublished());
        assertFalse(judicialResult.getExcludedFromResults());
        assertFalse(judicialResult.getUrgent());
        assertFalse(judicialResult.getD20());
        assertThat(judicialResult.getJudicialResultPrompts().stream().filter(jrp -> jrp.getJudicialResultPromptTypeId().equals(fromString("76f15753-1706-42fb-b922-0d56d01e5706"))).findFirst().get().getCourtExtract(), is("Y"));
        assertThat(judicialResult.getJudicialResultPrompts().stream().filter(jrp -> jrp.getJudicialResultPromptTypeId().equals(fromString("266a2bbe-b6b5-4b24-830d-70ceff3e2cac"))).findFirst().get().getCourtExtract(), is("N"));
    }

    @Test
    public void shouldRestructureSuccessfullyWhenScenario1() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(SCENARIO_1_SHORT_CODE_SEND_TO_CCON_CB_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> restructuredTree = target.restructure(envelope, resultsShared);

        assertThat(restructuredTree.size(), is(2));

        final List<TreeNode<ResultLine>> topLevelResultLineRestructuredParents = filterBy(restructuredTree, r -> r.getParents().isEmpty() && r.getChildren().size() > 0);

        assertThat((int) restructuredTree.stream().filter(TreeNode::isStandalone).count(), is(2));
        assertThat(topLevelResultLineRestructuredParents.size(), is(0));

        restructuredTree.forEach(rl -> {
            List<JudicialResultPrompt> judicialResultPrompts = rl.getJudicialResult().getJudicialResultPrompts();
            if (judicialResultPrompts != null && !judicialResultPrompts.isEmpty()) {
                assertTrue(judicialResultPrompts.stream()
                        .filter(jrp -> StringUtils.isNotEmpty(jrp.getValue()))
                        .noneMatch(jrp -> jrp.getValue().contains(ResultQualifier.SEPARATOR)));
            }
        });
    }

    @Test
    public void shouldRestructureSuccessfullyWhenDirsHearingResultShared() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(DIRS_HEARING_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> restructuredTree = target.restructure(envelope, resultsShared);
        final List<TreeNode<ResultLine>> topLevelResultLineRestructuredParents = filterBy(restructuredTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

        assertThat(restructuredTree.stream().filter(node -> node.isStandalone()).collect(toList()).size(), is(1));
        assertThat(topLevelResultLineRestructuredParents.size(), is(0));
    }

    @Test
    public void shouldRestructureSuccessfullyWhenCoHearingEvent() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(CO_HEARING_EVENT_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> restructuredTree = target.restructure(envelope, resultsShared);
        final List<TreeNode<ResultLine>> topLevelResultLineRestructuredParents = filterBy(restructuredTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

        assertThat(restructuredTree.stream().filter(node -> node.isStandalone()).collect(toList()).size(), CoreMatchers.is(2));
        assertThat(topLevelResultLineRestructuredParents.size(), CoreMatchers.is(0));
    }
}
