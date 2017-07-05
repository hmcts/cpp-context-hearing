package uk.gov.moj.cpp.hearing.steps.data;

import java.util.Map;
import java.util.UUID;

public class DefenceCounselData {

    private final UUID personId;
    private final String personName;
    private final UUID attendeeId;
    private final Map<UUID, String> mapOfDefendantIdToNames;
    private final String status;

    public DefenceCounselData(final UUID personId, final String personName, final UUID attendeeId,
                              final Map<UUID, String> defendantIdToNames, final String status) {
        this.personId = personId;
        this.personName = personName;
        this.attendeeId = attendeeId;
        this.mapOfDefendantIdToNames = defendantIdToNames;
        this.status = status;
    }

    public UUID getPersonId() {
        return personId;
    }

    public String getPersonName() {
        return personName;
    }

    public UUID getAttendeeId() {
        return attendeeId;
    }

    public Map<UUID, String> getMapOfDefendantIdToNames() {
        return mapOfDefendantIdToNames;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "DefenceCounselData{" +
                "personId=" + personId +
                ", personName='" + personName + '\'' +
                ", attendeeId=" + attendeeId +
                ", mapOfDefendantIdToNames=" + mapOfDefendantIdToNames +
                ", status='" + status + '\'' +
                '}';
    }
}
