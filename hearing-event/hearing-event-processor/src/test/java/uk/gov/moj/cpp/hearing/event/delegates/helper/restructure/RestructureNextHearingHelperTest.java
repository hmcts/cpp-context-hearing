package uk.gov.moj.cpp.hearing.event.delegates.helper.restructure;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_JSON;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.NEXT_HEARING_ID;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.shared.RestructuringConstants.NEXT_HEARING_IN_CROWN_COURT_ID;


import java.time.LocalDate;
import org.mockito.Mockito;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class RestructureNextHearingHelperTest extends AbstractRestructuringTest {

    private ResultTreeBuilder resultTreeBuilder;

    @Before
    public void setUp() throws IOException {
        ResultTextConfHelper resultTextConfHelper = Mockito.mock(ResultTextConfHelper.class);
        when(resultTextConfHelper.isOldResultDefinition(any(LocalDate.class))).thenReturn(false);
        super.setUp();
        resultTreeBuilder = new ResultTreeBuilder(referenceDataService, nextHearingHelper, resultLineHelper, resultTextConfHelper);
    }

    @Test
    public void restructureNextHearing_single_defendant_and_offence() throws IOException {
        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_JSON, ResultsShared.class);
        final List<TreeNode<ResultLine>> results = DeDupeNextHearingHelper.deDupNextHearing(resultTreeBuilder.build(dummyEnvelope, resultsShared));
        final List<TreeNode<ResultLine>> nextHearingInCrownCourtResults = results
                .stream()
                .filter(r -> NEXT_HEARING_IN_CROWN_COURT_ID.equals(r.getResultDefinitionId().toString()))
                .collect(toList());

        assertThat(nextHearingInCrownCourtResults, hasSize(5));

        final List<TreeNode<ResultLine>> treeResults = filterBy(nextHearingInCrownCourtResults, r -> !(r.getParents().size() == 0 && r.getChildren().size() == 0));

        assertThat(treeResults, hasSize(5));
        assertThat(treeResults.get(0).getId().toString(), is("53efa5f1-9e2f-40ea-aba9-5145d1bec83c"));

        final List<TreeNode<ResultLine>> standAloneResults = nextHearingInCrownCourtResults
                .stream()
                .filter(r -> r.getParents().isEmpty() && r.getChildren().isEmpty())
                .collect(toList());

        assertTrue(standAloneResults.isEmpty());

        final List<TreeNode<ResultLine>> nextHearingResult = filterBy(results, r -> NEXT_HEARING_ID.equals(r.getResultDefinitionId().toString()));

        assertThat(nextHearingResult.get(0).getChildren(), hasSize(5));
    }

    @Test
    public void restructureNextHearing_multiple_defendant_and_offences() throws IOException {

        final ResultsShared resultsShared = fileResourceObjectMapper.convertFromFile(HEARING_RESULTS_SHARED_MULTIPLE_DEFENDANT_JSON, ResultsShared.class);
        final List<TreeNode<ResultLine>> results = DeDupeNextHearingHelper.deDupNextHearing(resultTreeBuilder.build(dummyEnvelope, resultsShared));
        final List<TreeNode<ResultLine>> nextHearingInCrownCourtResults = filterBy(results, r -> NEXT_HEARING_IN_CROWN_COURT_ID.equals(r.getResultDefinitionId().toString()));

        assertThat(nextHearingInCrownCourtResults, hasSize(6));

        final List<TreeNode<ResultLine>> treeResults = filterBy(nextHearingInCrownCourtResults, r -> !(r.getParents().size() == 0 && r.getChildren().size() == 0));

        assertThat(treeResults, hasSize(3));

        final List<TreeNode<ResultLine>> standAloneResults = nextHearingInCrownCourtResults
                .stream()
                .filter(r -> r.getParents().isEmpty() && r.getChildren().isEmpty())
                .collect(toList());

        assertThat(standAloneResults, hasSize(3));

        final List<TreeNode<ResultLine>> nextHearingResult = filterBy(results, r -> NEXT_HEARING_ID.equals(r.getResultDefinitionId().toString()));

        assertThat(nextHearingResult.get(0).getChildren(), hasSize(1));

        List<TreeNode<ResultLine>> restructuredNextHearing = RestructureNextHearingHelper.restructureNextHearing(results);

        final List<TreeNode<ResultLine>> filteredNextHearing = restructuredNextHearing
                .stream()
                .filter(r -> NEXT_HEARING_ID.equals(r.getResultDefinitionId().toString()))
                .collect(toList());

        assertTrue(filteredNextHearing.isEmpty());
    }
}
