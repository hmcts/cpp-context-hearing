package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import uk.gov.justice.json.schemas.core.HearingDay;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JudicialRole;
import uk.gov.justice.json.schemas.core.JudicialRoleType;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventIgnored;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HearingDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public HearingDelegate(HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleHearingInitiated(HearingInitiated hearingInitiated) {
        this.momento.setHearing(hearingInitiated.getHearing());
    }

    public void handleHearingInitiated(HearingDetailChanged hearingDetailChanged) {

        if (hearingDetailChanged.getJudge() != null) {
            final JudicialRole judge = this.momento.getHearing().getJudiciary().stream().filter(j ->
                    j.getJudicialRoleType() == JudicialRoleType.DISTRICT_JUDGE || j.getJudicialRoleType() == JudicialRoleType.CIRCUIT_JUDGE
            )
                    .findAny()
                    .orElseThrow(() -> new IllegalArgumentException("No judge found"));

            judge.setFirstName(hearingDetailChanged.getJudge().getFirstName());
            judge.setLastName(hearingDetailChanged.getJudge().getLastName());
            judge.setTitle(hearingDetailChanged.getJudge().getTitle());
            judge.setJudicialId(hearingDetailChanged.getJudge().getId());
        }

        if (!hearingDetailChanged.getHearingDays().isEmpty()) {
            this.momento.getHearing().setHearingDays(hearingDetailChanged.getHearingDays().stream()
                    .map(zdt -> HearingDay.hearingDay().withSittingDay(zdt).build())
                    .collect(Collectors.toList())
            );
        }

        this.momento.getHearing().getCourtCentre().setRoomId(hearingDetailChanged.getCourtRoomId());
        this.momento.getHearing().getCourtCentre().setRoomName(hearingDetailChanged.getCourtRoomName());
        this.momento.getHearing().setType(HearingType.hearingType().withDescription(hearingDetailChanged.getType()).build());
    }

    public Stream<Object> initiate(final InitiateHearingCommand initiateHearingCommand) {
        // initiate Hearing command must not contain verdict and plea
        initiateHearingCommand.getHearing().getProsecutionCases().stream()
                .flatMap(p-> p.getDefendants().stream())
                .collect(Collectors.toList())
                .stream()
                .flatMap(d -> d.getOffences().stream())
                .forEach(o -> {
                    o.setVerdict(null);
                    o.setPlea(null);
                });
        return Stream.of(new HearingInitiated(initiateHearingCommand.getHearing()));
    }

    public Stream<Object> updateHearingDetails(final UUID id, final String type, final UUID courtRoomId, final String courtRoomName, final uk.gov.moj.cpp.hearing.command.hearingDetails.Judge judge, final List<ZonedDateTime> hearingDays) {

        if (this.momento.getHearing() == null) {
            return Stream.of(generateHearingIgnoredMessage("Rejecting 'hearing.change-hearing-detail' event as hearing not found", id));
        }

        return Stream.of(new HearingDetailChanged(id, type, courtRoomId, courtRoomName, judge, hearingDays));
    }

    private HearingEventIgnored generateHearingIgnoredMessage(final String reason, final UUID hearingId) {
        return new HearingEventIgnored(hearingId, reason);
    }
}
