package uk.gov.moj.cpp.hearing.domain;


import java.util.Optional;

public enum HearingState {
    INITIALISED("INITIALISED"),
    SHARED("SHARED"),
    SHARED_AMEND_LOCKED_ADMIN_ERROR("SHARED_AMEND_LOCKED_ADMIN_ERROR"),
    SHARED_AMEND_LOCKED_USER_ERROR("SHARED_AMEND_LOCKED_USER_ERROR"),
    VALIDATED("VALIDATED"),
    APPROVAL_REQUESTED("APPROVAL_REQUESTED");


    private final String value;

    HearingState(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static Optional<HearingState> valueFor(final String value) {
        if(INITIALISED.value.equals(value)) {
            return Optional.of(INITIALISED);
        }
        if(SHARED.value.equals(value)) {
            return Optional.of(SHARED);
        }
        if(SHARED_AMEND_LOCKED_ADMIN_ERROR.value.equals(value)) {
            return Optional.of(SHARED_AMEND_LOCKED_ADMIN_ERROR);
        }
        if(SHARED_AMEND_LOCKED_USER_ERROR.value.equals(value)) {
            return Optional.of(SHARED_AMEND_LOCKED_USER_ERROR);
        }
        if(VALIDATED.value.equals(value)) {
            return Optional.of(VALIDATED);
        }
        if(APPROVAL_REQUESTED.value.equals(value)) {
            return Optional.of(APPROVAL_REQUESTED);
        }
        return Optional.empty();
    }

}
