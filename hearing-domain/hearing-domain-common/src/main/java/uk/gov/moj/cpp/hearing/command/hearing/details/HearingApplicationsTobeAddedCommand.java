package uk.gov.moj.cpp.hearing.command.hearing.details;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("squid:S2384")
public class HearingApplicationsTobeAddedCommand {

    private final List<UUID> breachedApplications;

    private final UUID hearingId;

    @JsonCreator
    public HearingApplicationsTobeAddedCommand(List<UUID> breachedApplications, UUID hearingId) {
        this.breachedApplications = breachedApplications;
        this.hearingId = hearingId;
    }

    public List<UUID> getBreachedApplications() {
        return breachedApplications;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}

