package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;

@Event("hearing.company-representative-change-ignored")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyRepresentativeChangeIgnored implements Serializable {
    private static final long serialVersionUID = 361999875647163505L;

    private final String reason;

    @JsonCreator
    public CompanyRepresentativeChangeIgnored(@JsonProperty("reason") final String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
