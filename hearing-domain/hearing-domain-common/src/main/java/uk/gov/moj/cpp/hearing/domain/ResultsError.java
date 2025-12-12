package uk.gov.moj.cpp.hearing.domain;

import java.io.Serializable;

public class ResultsError implements Serializable {
    private ErrorType type;
    private String code;
    private String reason;

    public ResultsError(final ErrorType type, final String code, final String reason) {
        this.type = type;
        this.code = code;
        this.reason = reason;
    }

    public ErrorType getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    public enum ErrorType {
        STATE, VERSION;
    }

}
