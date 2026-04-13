package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.ApplicationCaseResults;
import uk.gov.justice.core.courts.ApplicationCourtOrderResults;
import uk.gov.justice.core.courts.ApplicationResults;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.DeletedJudicialResults;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseResults;
import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DeletedJudicialResultTransformer {

    public static DeletedJudicialResults toDeletedResults(final List<TreeNode<ResultLine2>> restructuredDeletedResults, final Hearing hearing) {

        final List<ProsecutionCaseResults> prosecutionCaseResults = getProsecutionCaseResults(restructuredDeletedResults, hearing.getProsecutionCases());
        final List<ApplicationResults> applicationResults = getApplicationResults(restructuredDeletedResults, hearing.getCourtApplications());
        final List<ApplicationCaseResults> applicationCaseResults = getApplicationCaseResults(restructuredDeletedResults, hearing.getCourtApplications());
        final List<ApplicationCourtOrderResults> applicationCourtOrderResults = getApplicationCourtOrderResults(restructuredDeletedResults, hearing.getCourtApplications());

        return DeletedJudicialResults.deletedJudicialResults()
                .withProsecutionCaseResults(isNotEmpty(prosecutionCaseResults) ? prosecutionCaseResults : null)
                .withApplicationResults(isNotEmpty(applicationResults) ? applicationResults : null)
                .withApplicationCaseResults(isNotEmpty(applicationCaseResults) ? applicationCaseResults : null)
                .withApplicationCourtOrderResults(isNotEmpty(applicationCourtOrderResults) ? applicationCourtOrderResults : null)
                .build();
    }

    private static List<ApplicationCourtOrderResults> getApplicationCourtOrderResults(final List<TreeNode<ResultLine2>> restructuredDeletedResults, final List<CourtApplication> courtApplications) {

        return restructuredDeletedResults.stream()
                .filter(node -> nonNull(node.getJudicialResult()))
                .filter(node -> node.getJudicialResult().getIsNewAmendment())
                .filter(node -> nonNull(node.getOffenceId()) && nonNull(node.getApplicationId()))
                .filter(node -> isCourtOrderResult(node, courtApplications))
                .map(node -> ApplicationCourtOrderResults.applicationCourtOrderResults()
                        .withDefendantId(getDefendantId(node.getDefendantId(), node.getApplicationId(), courtApplications))
                        .withApplicationId(node.getApplicationId())
                        .withOffenceId(node.getOffenceId())
                        .withJudicialResult(node.getJudicialResult())
                        .build())
                .toList();
    }

    private static List<ApplicationCaseResults> getApplicationCaseResults(final List<TreeNode<ResultLine2>> restructuredDeletedResults, final List<CourtApplication> courtApplications) {
        return restructuredDeletedResults.stream()
                .filter(node -> nonNull(node.getJudicialResult()))
                .filter(node -> node.getJudicialResult().getIsNewAmendment())
                .filter(node -> nonNull(node.getOffenceId()) && nonNull(node.getApplicationId()))
                .filter(node -> isApplicationCaseResult(node, courtApplications))
                .map(node -> ApplicationCaseResults.applicationCaseResults()
                        .withDefendantId(getDefendantId(node.getDefendantId(), node.getApplicationId(), courtApplications))
                        .withApplicationId(node.getApplicationId())
                        .withOffenceId(node.getOffenceId())
                        .withJudicialResult(node.getJudicialResult())
                        .build())
                .toList();
    }

    private static List<ApplicationResults> getApplicationResults(final List<TreeNode<ResultLine2>> restructuredDeletedResults, final List<CourtApplication> courtApplications) {
        return restructuredDeletedResults.stream()
                .filter(node -> nonNull(node.getJudicialResult()))
                .filter(node -> node.getJudicialResult().getIsNewAmendment())
                .filter(node -> isNull(node.getOffenceId()))
                .filter(node -> isApplicationResult(node, courtApplications))
                .map(node -> ApplicationResults.applicationResults()
                        .withDefendantId(getDefendantId(node.getDefendantId(), node.getApplicationId(), courtApplications))
                        .withApplicationId(node.getApplicationId())
                        .withJudicialResult(node.getJudicialResult())
                        .build())
                .toList();
    }

    private static List<ProsecutionCaseResults> getProsecutionCaseResults(final List<TreeNode<ResultLine2>> restructuredDeletedResults, final List<ProsecutionCase> prosecutionCases) {
        return restructuredDeletedResults.stream()
                .filter(node -> nonNull(node.getJudicialResult()))
                .filter(node -> node.getJudicialResult().getIsNewAmendment())
                .filter(node -> isNull(node.getApplicationId()))
                .map(node -> ProsecutionCaseResults.prosecutionCaseResults()
                        .withDefendantId(node.getDefendantId())
                        .withOffenceId(node.getOffenceId())
                        .withJudicialResult(node.getJudicialResult())
                        .build())
                .toList();
    }

    private static UUID getDefendantId(final UUID defendantId, final UUID applicationId, final List<CourtApplication> courtApplications) {
        if (nonNull(defendantId)) {
            return defendantId;
        }
        //liked applications
        if (isNotEmpty(courtApplications) && nonNull(applicationId)) {
            final Optional<UUID> defendantOnSubject = fromSubject(applicationId, courtApplications);
            if (defendantOnSubject.isPresent()) {
                return defendantOnSubject.get();
            }
            final Optional<UUID> defendantOnApplicant = fromApplicant(applicationId, courtApplications);
            if (defendantOnApplicant.isPresent()) {
                return defendantOnApplicant.get();
            }
            final Optional<UUID> defendantOnRespondents = fromRespondent(applicationId, courtApplications);
            if (defendantOnRespondents.isPresent()) {
                return defendantOnRespondents.get();
            }
        }
        return null;
    }

    private static Optional<UUID> fromSubject(final UUID applicationId, final List<CourtApplication> courtApplications) {
        return courtApplications.stream()
                .filter(ca -> applicationId.equals(ca.getId()))
                .filter(ca -> nonNull(ca.getSubject()) && nonNull(ca.getSubject().getMasterDefendant())
                        && isNotEmpty(ca.getSubject().getMasterDefendant().getDefendantCase()))
                .map(ca -> ca.getSubject().getMasterDefendant().getDefendantCase().get(0).getDefendantId())
                .findFirst();
    }

    private static Optional<UUID> fromApplicant(final UUID applicationId, final List<CourtApplication> courtApplications) {
        return courtApplications.stream()
                .filter(ca -> applicationId.equals(ca.getId()))
                .filter(ca -> nonNull(ca.getApplicant()) && nonNull(ca.getApplicant().getMasterDefendant())
                        && isNotEmpty(ca.getApplicant().getMasterDefendant().getDefendantCase()))
                .map(ca -> ca.getApplicant().getMasterDefendant().getDefendantCase().get(0).getDefendantId())
                .findFirst();
    }

    private static Optional<UUID> fromRespondent(final UUID applicationId, final List<CourtApplication> courtApplications) {
        return courtApplications.stream()
                .filter(ca -> applicationId.equals(ca.getId()))
                .filter(ca -> nonNull(ca.getRespondents()))
                .flatMap(ca -> ca.getRespondents().stream())
                .filter(resp -> nonNull(resp.getMasterDefendant())
                        && isNotEmpty(resp.getMasterDefendant().getDefendantCase()))
                .map(resp -> resp.getMasterDefendant().getDefendantCase().get(0).getDefendantId())
                .findFirst();
    }

    private static boolean isCourtOrderResult(final TreeNode<ResultLine2> node, final List<CourtApplication> courtApplications) {
        return isNotEmpty(courtApplications) && courtApplications.stream()
                .filter(ca -> ca.getId().equals(node.getApplicationId()))
                .filter(ca -> nonNull(ca.getCourtOrder()) && isNotEmpty(ca.getCourtOrder().getCourtOrderOffences()))
                .flatMap(ca -> ca.getCourtOrder().getCourtOrderOffences().stream())
                .anyMatch(coo -> coo.getOffence().getId().equals(node.getOffenceId()));
    }

    private static boolean isApplicationCaseResult(final TreeNode<ResultLine2> node, final List<CourtApplication> courtApplications) {
        return isNotEmpty(courtApplications) && courtApplications.stream()
                .filter(ca -> ca.getId().equals(node.getApplicationId()))
                .filter(ca -> isNotEmpty(ca.getCourtApplicationCases()))
                .flatMap(ca -> ca.getCourtApplicationCases().stream())
                .flatMap(cac -> cac.getOffences().stream())
                .anyMatch(caco -> caco.getId().equals(node.getOffenceId()));
    }

    private static boolean isApplicationResult(final TreeNode<ResultLine2> node, final List<CourtApplication> courtApplications) {
        return isNotEmpty(courtApplications) && courtApplications.stream()
                .anyMatch(ca -> ca.getId().equals(node.getApplicationId()));
    }
}
