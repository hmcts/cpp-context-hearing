package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;


import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Judge;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class HearingDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public HearingDelegate(HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleHearingInitiated(HearingInitiated hearingInitiated) {
        this.momento.setCases(hearingInitiated.getCases());
        this.momento.setHearing(hearingInitiated.getHearing());
    }

    public Stream<Object> initiate(InitiateHearingCommand initiateHearingCommand) {
        return Stream.of(new HearingInitiated(initiateHearingCommand.getCases(), initiateHearingCommand.getHearing()));
    }

    public Stream<Object> updateHearingDetails(final UUID id, final String type, final UUID courtRoomId, final String courtRoomName, final Judge judge, final List<ZonedDateTime> hearingDays) {

        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting 'hearing.change-hearing-detail' event as hearing not found", id));
        }

        return Stream.of(
                new HearingDetailChanged(id, type, courtRoomId, courtRoomName, judge, hearingDays)
        );
    }

    private HearingEventIgnored generateHearingIgnoredMessage(final String reason, final UUID hearingId) {
        return new HearingEventIgnored(hearingId, reason);
    }
}
