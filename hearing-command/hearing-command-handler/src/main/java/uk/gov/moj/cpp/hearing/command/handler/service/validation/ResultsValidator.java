package uk.gov.moj.cpp.hearing.command.handler.service.validation;

public interface ResultsValidator {

    ValidationResponse validate(ValidationRequest request, String userId);
}
