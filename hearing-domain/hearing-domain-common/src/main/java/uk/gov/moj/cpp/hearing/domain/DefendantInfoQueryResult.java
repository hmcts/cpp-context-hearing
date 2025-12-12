package uk.gov.moj.cpp.hearing.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefendantInfoQueryResult {

    private List<CourtRoom> courtRooms = new ArrayList<>();

    @JsonCreator
    public DefendantInfoQueryResult(@JsonProperty("courtRooms") final List<CourtRoom> courtRooms) {
        this.courtRooms = courtRooms;
    }

    public DefendantInfoQueryResult() {
    }

    public List<CourtRoom> getCourtRooms() {
        return courtRooms;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefendantInfoQueryResult that = (DefendantInfoQueryResult) o;
        return Objects.equals(courtRooms, that.courtRooms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courtRooms);
    }
}