package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.CO_HEARING_EVENT_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.DIRS_HEARING_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.DUMMY_NAME;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.FIXED_LIST_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.IMP_TIMP_HEARING_RESULTS_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.RESULT_DEFINITIONS_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.SCENARIO_1_SHORT_CODE_SEND_TO_CCON_CB_JSON;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.metadataFor;

import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.hearing.courts.referencedata.CourtCentreOrganisationUnit;
import uk.gov.justice.hearing.courts.referencedata.Courtrooms;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.delegates.helper.ResultQualifier;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllFixedList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.FixedList;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class RestructuringHelperTest extends AbstractRestructuringTest {

    private ResultTreeBuilder resultTreeBuilder;
    private RestructuringHelper target;

    @BeforeEach
    public void setUp() throws IOException {
        ResultTextConfHelper resultTextConfHelper = Mockito.mock(ResultTextConfHelper.class);
        when(resultTextConfHelper.isOldResultDefinition(any(LocalDate.class))).thenReturn(false);
        stubResultDefinitionJson();

        resultTreeBuilder = new ResultTreeBuilder(referenceDataService, nextHearingHelper, resultLineHelper, resultTextConfHelper);
        target = new RestructuringHelper(resultTreeBuilder, resultTextConfHelper);
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
        stubFixedListJson();
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
        stubFixedListJson();
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(CO_HEARING_EVENT_JSON, ResultsShared.class);
        final JsonEnvelope envelope = getEnvelope(resultsShared);
        final List<TreeNode<ResultLine>> restructuredTree = target.restructure(envelope, resultsShared);
        final List<TreeNode<ResultLine>> topLevelResultLineRestructuredParents = filterBy(restructuredTree, r -> r.getParents().size() == 0 && r.getChildren().size() > 0);

        assertThat(restructuredTree.stream().filter(node -> node.isStandalone()).collect(toList()).size(), CoreMatchers.is(2));
        assertThat(topLevelResultLineRestructuredParents.size(), CoreMatchers.is(0));
    }
}
