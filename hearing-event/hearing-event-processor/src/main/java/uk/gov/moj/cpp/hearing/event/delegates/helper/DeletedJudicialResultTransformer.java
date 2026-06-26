package uk.gov.moj.cpp.hearing.event.delegates.helper;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.moj.cpp.hearing.event.helper.TreeNode;
import uk.gov.justice.core.courts.ResultLine2;

import java.util.List;

/**
 * No-op stub: ApplicationCaseResults, ApplicationCourtOrderResults, ApplicationResults,
 * DeletedJudicialResults, ProsecutionCaseResults and Hearing.setDeletedJudicialResults were
 * removed from coredomain in 25.104.0-M4.
 */
public class DeletedJudicialResultTransformer {

    public static void toDeletedResults(final List<TreeNode<ResultLine2>> restructuredDeletedResults, final Hearing hearing) {
        // No-op: coredomain 25.104.0-M4 removed DeletedJudicialResults and related types.
    }
}
