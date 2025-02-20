package uk.gov.moj.cpp.hearing.domain;

public enum ResultsErrorType {

    VERSION_OFF_SEQUENCE(ResultsError.ErrorType.VERSION, "101") {
        @Override
        public ResultsError toError(final String description) {
            return new ResultsError(this.getErrorType(), this.getErrorCode(), description);
        }
    },
    VERSION_MISMATCH(ResultsError.ErrorType.VERSION, "102") {
        @Override
        public ResultsError toError(final String description) {
            return new ResultsError(this.getErrorType(), this.getErrorCode(), description);
        }
    },
    SAVE_RESULTS_NOT_PERMITTED(ResultsError.ErrorType.STATE, "201") {
        @Override
        public ResultsError toError(final String description) {
            return new ResultsError(this.getErrorType(), this.getErrorCode(), description);
        }
    },
    SHARE_NOT_PERMITTED(ResultsError.ErrorType.STATE, "202") {
        @Override
        public ResultsError toError(final String description) {
            return new ResultsError(this.getErrorType(), this.getErrorCode(), description);
        }
    },
    CANCEL_AMENDMENTS_NOT_PERMITTED(ResultsError.ErrorType.STATE, "203") {
        @Override
        public ResultsError toError(final String description) {
            return new ResultsError(this.getErrorType(), this.getErrorCode(), description);
        }
    },

    UNLOCK_HEARING_NOT_PERMITTED(ResultsError.ErrorType.STATE, "204") {
        @Override
        public ResultsError toError(final String description) {
            return new ResultsError(this.getErrorType(), this.getErrorCode(), description);
        }
    },
    VALIDATE_HEARING_NOT_PERMITTED(ResultsError.ErrorType.STATE, "205") {
        @Override
        public ResultsError toError(final String description) {
            return new ResultsError(this.getErrorType(), this.getErrorCode(), description);
        }
    },
    HEARING_LOCKED(ResultsError.ErrorType.STATE, "206") {
        @Override
        public ResultsError toError(final String description) {
            return new ResultsError(this.getErrorType(), this.getErrorCode(), description);
        }
    };

    private final ResultsError.ErrorType errorType;
    private final String errorCode;

    ResultsErrorType(final ResultsError.ErrorType errorType, final String errorCode) {
        this.errorType = errorType;
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ResultsError.ErrorType getErrorType() {
        return errorType;
    }

    public abstract ResultsError toError(final String description);
}

