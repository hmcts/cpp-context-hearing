package uk.gov.moj.cpp.hearing.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefendantOutstandingFineRequestsResult {

    private List<DefendantOutstandingFineRequests> defendantDetails = new ArrayList<>();

    @JsonCreator
    public DefendantOutstandingFineRequestsResult(@JsonProperty("defendantDetails") final List<DefendantOutstandingFineRequests> defendantDetails) {
        this.defendantDetails = Collections.unmodifiableList(defendantDetails != null ? defendantDetails : Collections.emptyList());
    }

    public DefendantOutstandingFineRequestsResult() {
    }

    public List<DefendantOutstandingFineRequests> getDefendantDetails() {
        return Collections.unmodifiableList(defendantDetails);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefendantOutstandingFineRequestsResult that = (DefendantOutstandingFineRequestsResult) o;
        return Objects.equals(defendantDetails, that.defendantDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(defendantDetails);
    }
}