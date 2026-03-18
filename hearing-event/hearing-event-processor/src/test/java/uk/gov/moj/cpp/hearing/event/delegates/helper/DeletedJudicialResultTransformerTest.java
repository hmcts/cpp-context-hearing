package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.CourtApplication.courtApplication;
import static uk.gov.moj.cpp.hearing.event.delegates.helper.DeletedJudicialResultTransformer.toDeletedResults;

import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantJudicialResult;
import uk.gov.justice.core.courts.DeletedJudicialResults;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class DeletedJudicialResultTransformerTest {

    @Test
    public void givenDeletedDefendantLevelJrs_whenTransformedToDeletedJudicialResults_shouldIncludeInProsecutionCaseResults() {
        final UUID defendantId = randomUUID();
        final UUID resultLineId = randomUUID();
        final TreeNode mockNode = mock(TreeNode.class);
        final List<TreeNode<ResultLine2>> restructuredResults = List.of(mockNode);

        final Hearing hearing = Hearing.hearing().withDefendantJudicialResults(List.of(DefendantJudicialResult.defendantJudicialResult()
                .withDefendantId(defendantId).build())).build();
        when(mockNode.getDefendantId()).thenReturn(defendantId);
        when(mockNode.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId).withIsNewAmendment(TRUE).build());

        final DeletedJudicialResults deletedResults = toDeletedResults(restructuredResults, hearing);

        assertThat(deletedResults.getProsecutionCaseResults().size(), is(1));
        assertThat(deletedResults.getProsecutionCaseResults().get(0).getDefendantId(), is(defendantId));
        assertThat(deletedResults.getProsecutionCaseResults().get(0).getJudicialResult(), is(notNullValue()));
    }

    @Test
    public void givenDeletedDefendantLevelJrs_whenTransformedToDeletedJudicialResults_andNoMatchFound_shouldReturnNullProsecutionCaseResults() {
        final UUID defendantId = randomUUID();
        final UUID resultLineId = randomUUID();
        final TreeNode mockNode = mock(TreeNode.class);
        final List<TreeNode<ResultLine2>> restructuredResults = List.of(mockNode);

        final Hearing hearing = Hearing.hearing().withDefendantJudicialResults(List.of(DefendantJudicialResult.defendantJudicialResult()
                .withDefendantId(defendantId).build())).build();
        when(mockNode.getDefendantId()).thenReturn(randomUUID());
        when(mockNode.getApplicationId()).thenReturn(randomUUID());
        when(mockNode.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId).withIsNewAmendment(TRUE).build());

        final DeletedJudicialResults deletedResults = toDeletedResults(restructuredResults, hearing);

        assertThat(deletedResults.getProsecutionCaseResults(), is(nullValue()));
    }

    @Test
    public void givenDeletedDefendantCaseLevelJrs_whenTransformedToDeletedJudicialResults_shouldIncludeInProsecutionCaseResults() {
        final UUID defendantId = randomUUID();
        final UUID resultLineId = randomUUID();
        final TreeNode mockNode = mock(TreeNode.class);
        final List<TreeNode<ResultLine2>> restructuredResults = List.of(mockNode);

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(
                        ProsecutionCase.prosecutionCase().withDefendants(List.of(Defendant.defendant()
                                .withId(defendantId)
                                .build())).build()
                ))
                .build();
        when(mockNode.getDefendantId()).thenReturn(defendantId);
        when(mockNode.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId).withIsNewAmendment(TRUE).build());

        final DeletedJudicialResults deletedResults = toDeletedResults(restructuredResults, hearing);

        assertThat(deletedResults.getProsecutionCaseResults().size(), is(1));
        assertThat(deletedResults.getProsecutionCaseResults().get(0).getDefendantId(), is(defendantId));
        assertThat(deletedResults.getProsecutionCaseResults().get(0).getJudicialResult(), is(notNullValue()));
    }

    @Test
    public void givenOffenceLevelJrs_whenTransformedToDeletedJudicialResults_shouldIncludeInProsecutionCaseResults() {
        final UUID defendantId = randomUUID();
        final UUID resultLineId = randomUUID();
        final UUID offenceId = randomUUID();
        final TreeNode mockNode = mock(TreeNode.class);
        final List<TreeNode<ResultLine2>> restructuredResults = List.of(mockNode);

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(
                        ProsecutionCase.prosecutionCase().withDefendants(List.of(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(List.of(Offence.offence().withId(offenceId)
                                        .build()))
                                .build())).build()
                ))
                .build();
        when(mockNode.getDefendantId()).thenReturn(defendantId);
        when(mockNode.getOffenceId()).thenReturn(offenceId);
        when(mockNode.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId).withIsNewAmendment(TRUE).build());

        final DeletedJudicialResults deletedResults = toDeletedResults(restructuredResults, hearing);

        assertThat(deletedResults.getProsecutionCaseResults().size(), is(1));
        assertThat(deletedResults.getProsecutionCaseResults().get(0).getDefendantId(), is(defendantId));
        assertThat(deletedResults.getProsecutionCaseResults().get(0).getJudicialResult(), is(notNullValue()));
    }

    @Test
    public void givenMultipleOffenceLevelJrs_whenTransformedToDeletedJudicialResults_shouldIncludeInProsecutionCaseResults() {
        final UUID defendantId = randomUUID();
        final UUID resultLineId1 = randomUUID();
        final UUID resultLineId2 = randomUUID();
        final UUID offenceId = randomUUID();
        final TreeNode mockNode = mock(TreeNode.class);
        final TreeNode mockNode2 = mock(TreeNode.class);
        final List<TreeNode<ResultLine2>> restructuredResults = List.of(mockNode, mockNode2);

        final Hearing hearing = Hearing.hearing()
                .withProsecutionCases(List.of(
                        ProsecutionCase.prosecutionCase().withDefendants(List.of(Defendant.defendant()
                                .withId(defendantId)
                                .withOffences(List.of(Offence.offence().withId(offenceId)
                                        .build()))
                                .build())).build()
                ))
                .build();
        when(mockNode.getDefendantId()).thenReturn(defendantId);
        when(mockNode.getOffenceId()).thenReturn(offenceId);
        when(mockNode.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId1).withIsNewAmendment(TRUE).build());

        when(mockNode2.getDefendantId()).thenReturn(defendantId);
        when(mockNode2.getOffenceId()).thenReturn(offenceId);
        when(mockNode2.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId2).withIsNewAmendment(TRUE).build());

        final DeletedJudicialResults deletedResults = toDeletedResults(restructuredResults, hearing);

        assertThat(deletedResults.getProsecutionCaseResults().size(), is(2));
        assertThat(deletedResults.getProsecutionCaseResults().stream().allMatch(pc -> pc.getDefendantId().equals(defendantId)), is(true));
        assertThat(deletedResults.getProsecutionCaseResults().stream().allMatch(pc -> pc.getOffenceId().equals(offenceId)), is(true));
        assertThat(deletedResults.getProsecutionCaseResults().stream().map(pc -> pc.getJudicialResult().getJudicialResultId()).toList(), containsInAnyOrder(resultLineId1, resultLineId2));
    }

    @Test
    public void givenApplicationJrs_whenTransformedToDeletedJudicialResults_shouldIncludeInApplicationResults() {
        final UUID applicationId = randomUUID();
        final UUID resultLineId = randomUUID();
        final TreeNode mockNode = mock(TreeNode.class);
        final List<TreeNode<ResultLine2>> restructuredResults = List.of(mockNode);

        final Hearing hearing = Hearing.hearing()
                .withCourtApplications(List.of(courtApplication().withId(applicationId).build()))
                .build();
        when(mockNode.getApplicationId()).thenReturn(applicationId);
        when(mockNode.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId).withIsNewAmendment(TRUE).build());

        final DeletedJudicialResults deletedResults = toDeletedResults(restructuredResults, hearing);

        assertThat(deletedResults.getApplicationResults().size(), is(1));
        assertThat(deletedResults.getApplicationResults().get(0).getApplicationId(), is(applicationId));
        assertThat(deletedResults.getApplicationResults().get(0).getJudicialResult(), is(notNullValue()));
    }

    @Test
    public void givenApplicationCaseJrs_whenTransformedToDeletedJudicialResults_shouldIncludeInApplicationResults() {
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID resultLineId = randomUUID();
        final TreeNode mockNode = mock(TreeNode.class);
        final List<TreeNode<ResultLine2>> restructuredResults = List.of(mockNode);

        final Hearing hearing = Hearing.hearing()
                .withCourtApplications(List.of(courtApplication().withId(applicationId)
                        .withCourtApplicationCases(List.of(CourtApplicationCase.courtApplicationCase()
                                .withOffences(List.of(Offence.offence()
                                        .withId(offenceId).build()))
                                .build())).build()))
                .build();
        when(mockNode.getApplicationId()).thenReturn(applicationId);
        when(mockNode.getOffenceId()).thenReturn(offenceId);
        when(mockNode.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId).withIsNewAmendment(TRUE).build());

        final DeletedJudicialResults deletedResults = toDeletedResults(restructuredResults, hearing);

        assertThat(deletedResults.getApplicationCaseResults().size(), is(1));
        assertThat(deletedResults.getApplicationCaseResults().get(0).getApplicationId(), is(applicationId));
        assertThat(deletedResults.getApplicationCaseResults().get(0).getJudicialResult(), is(notNullValue()));
    }

    @Test
    public void givenApplicationCourtOrderJrs_whenTransformedToDeletedJudicialResults_shouldIncludeInApplicationResults() {
        final UUID applicationId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID resultLineId = randomUUID();
        final TreeNode mockNode = mock(TreeNode.class);
        final List<TreeNode<ResultLine2>> restructuredResults = List.of(mockNode);

        final Hearing hearing = Hearing.hearing()
                .withCourtApplications(List.of(courtApplication().withId(applicationId)
                        .withCourtOrder(CourtOrder.courtOrder()
                                .withCourtOrderOffences(List.of(CourtOrderOffence.courtOrderOffence()
                                        .withOffence(Offence.offence().withId(offenceId)
                                                .build())
                                        .build()))
                                .build())

                        .build()))
                .build();
        when(mockNode.getApplicationId()).thenReturn(applicationId);
        when(mockNode.getOffenceId()).thenReturn(offenceId);
        when(mockNode.getJudicialResult()).thenReturn(JudicialResult.judicialResult().withJudicialResultId(resultLineId).withIsNewAmendment(TRUE).build());

        final DeletedJudicialResults deletedResults = toDeletedResults(restructuredResults, hearing);

        assertThat(deletedResults.getApplicationCourtOrderResults().size(), is(1));
        assertThat(deletedResults.getApplicationCourtOrderResults().get(0).getApplicationId(), is(applicationId));
        assertThat(deletedResults.getApplicationCourtOrderResults().get(0).getJudicialResult(), is(notNullValue()));
    }
}