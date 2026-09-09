package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationRequest;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationResponse;

public interface ResultsValidator {

    DraftValidationResponse validate(DraftValidationRequest request, String userId);

    /**
     * Whether a validation failure should block the share.
     * Driven by the JNDI env-entry {@code resultsvalidator.share.blocking}:
     * {@code enabled} blocks; absent or {@code disabled} means log-only.
     */
    boolean isShareBlockingEnabled();
}
