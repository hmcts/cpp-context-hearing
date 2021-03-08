package uk.gov.moj.cpp.hearing.domain.event.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;


@Event("hearing.events.results-amendments-cancellation-failed")
public class ResultAmendmentsCancellationFailed implements Serializable {

    private static final long serialVersionUID = 1L;

    final String reason;

    @JsonCreator
    public ResultAmendmentsCancellationFailed(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
