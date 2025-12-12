package uk.gov.moj.cpp.hearing.nows.events;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.events.enforcement-error")
public class EnforcementError {

    private final UUID requestId;

    private final String errorCode;

    private final String errorMessage;

    public EnforcementError(final UUID requestId, final String errorCode, final String errorMessage) {
        this.requestId = requestId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
