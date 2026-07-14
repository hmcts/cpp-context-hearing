package uk.gov.moj.cpp.hearing.command.handler.service.validation;

import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationRequest;
import uk.gov.moj.cpp.hearing.domain.common.resultsvalidator.DraftValidationResponse;

public interface ResultsValidator {

    DraftValidationResponse validate(DraftValidationRequest request, String userId);
}
