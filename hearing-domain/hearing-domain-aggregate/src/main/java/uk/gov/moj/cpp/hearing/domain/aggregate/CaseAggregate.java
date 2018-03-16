package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.initiate.Case;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.CaseCreated;
import uk.gov.moj.cpp.hearing.domain.event.CaseHearingAdded;
import uk.gov.moj.cpp.hearing.domain.event.CaseOffenceAdded;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

public class CaseAggregate implements Aggregate {

    private UUID caseId;

    private String urn;

    private Map<UUID, Offence> offences = new HashMap<>();

    private List<UUID> hearingIds = new ArrayList<>();

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(CaseCreated.class).apply(this::onCaseCreated),
                when(CaseOffenceAdded.class).apply(this::onCaseOffenceAdded),
                when(CaseHearingAdded.class).apply(this::onCaseHearingAdded),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiateHearing(UUID caseId, InitiateHearingCommand initiateHearingCommand) {

        List<Object> events = new ArrayList<>();

        if (this.caseId == null) {
            Case legalCase = initiateHearingCommand.getCases()
                    .stream()
                    .filter(c -> c.getCaseId().equals(caseId))
                    .findFirst()
                    .get();

            events.add(new CaseCreated(legalCase.getCaseId(), legalCase.getUrn()));
        }

        events.addAll(initiateHearingCommand.getHearing()
                .getDefendants()
                .stream()
                .flatMap(defendant -> defendant.getOffences().stream())
                .filter(offence -> offence.getCaseId().equals(caseId))
                .filter(offence -> !offences.containsKey(offence.getId()))
                .map(offence -> new CaseOffenceAdded(offence.getId(), caseId))
                .collect(Collectors.toList()));

        events.add(new CaseHearingAdded(caseId, initiateHearingCommand.getHearing().getId()));

        return apply(events.stream());
    }

    private void onCaseCreated(final CaseCreated event) {
        this.caseId = event.getCaseId();
        this.urn = event.getUrn();
    }

    private void onCaseOffenceAdded(CaseOffenceAdded caseOffenceAdded) {
        this.offences.put(caseOffenceAdded.getOffenceId(), new Offence(caseOffenceAdded.getCaseId(), caseOffenceAdded.getOffenceId()));
    }

    private void onCaseHearingAdded(CaseHearingAdded caseHearingAdded) {
        hearingIds.add(caseHearingAdded.getHearingId());
    }

    public UUID getCaseId() {
        return caseId;
    }

    public String getUrn() {
        return urn;
    }

    public Offence getOffence(UUID offenceId) {
        return this.offences.get(offenceId);
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public static class Offence {
        private UUID caseId;

        private UUID offenceId;

        public Offence(UUID caseId, UUID offenceId) {
            this.caseId = caseId;
            this.offenceId = offenceId;
        }

        public UUID getCaseId() {
            return caseId;
        }

        public UUID getOffenceId() {
            return offenceId;
        }
    }
}
