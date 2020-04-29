package uk.gov.moj.cpp.hearing.command.enforcement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestOutstandingFines {

    private List<FineRequest> fineRequests = new ArrayList<>();

    @JsonCreator
    public RequestOutstandingFines(@JsonProperty("fineRequests") final List<FineRequest> fineRequests) {
        this.fineRequests = Collections.unmodifiableList(fineRequests);
    }

    public RequestOutstandingFines() {
    }

    public List<FineRequest> getFineRequests() {
        return Collections.unmodifiableList(fineRequests);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RequestOutstandingFines that = (RequestOutstandingFines) o;
        return Objects.equals(fineRequests, that.fineRequests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fineRequests);
    }
}