package uk.gov.moj.cpp.hearing.command.defendant;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;
import java.util.UUID;

@SuppressWarnings({"squid:S2384"})
public class DefendantsWithWelshTranslationsCommand {

    private final UUID hearingId;
    private final List<DefendantWelshInfo> defendantsWelshList;

    @JsonCreator
    public DefendantsWithWelshTranslationsCommand(final UUID hearingId, final List<DefendantWelshInfo> defendantsWelshList) {
        this.hearingId = hearingId;
        this.defendantsWelshList = defendantsWelshList;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<DefendantWelshInfo> getDefendantsWelshList() {
        return defendantsWelshList;
    }

}
