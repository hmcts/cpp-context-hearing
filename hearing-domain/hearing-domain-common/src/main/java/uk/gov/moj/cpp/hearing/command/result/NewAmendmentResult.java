package uk.gov.moj.cpp.hearing.command.result;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewAmendmentResult implements Serializable {

    private UUID id;
    private ZonedDateTime amendmentDateTime;

    @JsonCreator
    public NewAmendmentResult(@JsonProperty("id") final UUID id,
                              @JsonProperty("amendmentDateTime") final ZonedDateTime amendmentDateTime) {
        this.id = id;
        this.amendmentDateTime = amendmentDateTime;
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public ZonedDateTime getAmendmentDateTime() {
        return amendmentDateTime;
    }

    public void setAmendmentDateTime(final ZonedDateTime amendmentDateTime) {
        this.amendmentDateTime = amendmentDateTime;
    }
}
