package uk.gov.moj.cpp.hearing.domain.aggregate;

import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.domain.event.OffenceCreated;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.domain.event.OffenceVerdictUpdated;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

public class OffenceAggregate implements Aggregate {

    private UUID offenceId;

    private UUID caseId;

    private UUID defendantId;

    private Plea plea;

    private Verdict verdict;

    @Override
    public Object apply(Object event) {
        return match(event).with(
                when(OffenceCreated.class).apply(this::onOffenceCreated),
                when(OffencePleaUpdated.class).apply(this::onOffencePleaUpdated),
                when(OffenceVerdictUpdated.class).apply(this::onOffenceVerdictUpdated),
                otherwiseDoNothing()
        );
    }

    public Stream<Object> initiateHearing(UUID defendantId, uk.gov.moj.cpp.hearing.command.initiate.Offence offence) {

        if (offenceId == null) {
            return apply(Stream.of(new OffenceCreated(offence.getId(), offence.getCaseId(), defendantId)));
        }
        return apply(Stream.empty());
    }

    public Stream<Object> updatePlea(UUID originHearingId, uk.gov.moj.cpp.hearing.command.plea.Plea plea) {

        return apply(Stream.of(
                new OffencePleaUpdated(
                        caseId, //TODO - offenceId is unique within case, so do we need this?
                        originHearingId,
                        offenceId,
                        plea.getId(),//TODO - do we need pleaId?
                        plea.getPleaDate(),
                        plea.getValue())
        ));
    }

    public Stream<Object> updateVerdict(UUID hearingId, uk.gov.moj.cpp.hearing.command.verdict.Verdict verdict) {

        return apply(Stream.of(
                new OffenceVerdictUpdated(
                        caseId, //TODO - offenceId is unique within case, so do we need this?
                        hearingId,
                        offenceId,
                        verdict.getId(), //TODO - do we need verdictId
                        verdict.getValue().getId(),
                        verdict.getValue().getCategory(),
                        verdict.getValue().getCode(),
                        verdict.getValue().getDescription()
                )
        ));
    }

    private void onOffenceCreated(OffenceCreated offenceCreated) {
        this.offenceId = offenceCreated.getOffenceId();
        this.caseId = offenceCreated.getCaseId();
        this.defendantId = offenceCreated.getDefendantId();
    }

    private void onOffencePleaUpdated(OffencePleaUpdated offencePleaUpdated) {
        this.plea = new Plea(
                offencePleaUpdated.getPleaId(),
                offencePleaUpdated.getOriginHearingId(),
                offencePleaUpdated.getValue(),
                offencePleaUpdated.getPleaDate()
        );
    }

    private void onOffenceVerdictUpdated(OffenceVerdictUpdated offenceVerdictUpdated) {
        this.verdict = new Verdict(
                offenceVerdictUpdated.getVerdictId(),
                offenceVerdictUpdated.getOriginHearingId(),
                offenceVerdictUpdated.getOffenceId(),
                offenceVerdictUpdated.getVerdictId(),
                offenceVerdictUpdated.getVerdictValueId(),
                offenceVerdictUpdated.getCategory(),
                offenceVerdictUpdated.getCode(),
                offenceVerdictUpdated.getDescription()
        );
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public Plea getPlea() {
        return plea;
    }

    public Verdict getVerdict() {
        return verdict;
    }


    public static class Plea {
        private UUID id;
        private UUID originHearingId;
        private String value;
        private LocalDate pleaDate;

        Plea(UUID id, UUID originHearingId, String value, LocalDate pleaDate) {
            this.id = id;
            this.originHearingId = originHearingId;
            this.value = value;
            this.pleaDate = pleaDate;
        }

        public UUID getId() {
            return id;
        }

        public UUID getOriginHearingId() {
            return originHearingId;
        }

        public String getValue() {
            return value;
        }

        public LocalDate getPleaDate() {
            return pleaDate;
        }
    }

    public static class Verdict {

        private UUID caseId;
        private UUID originHearingId;
        private UUID offenceId;
        private UUID verdictId;
        private UUID verdictValueId;
        private String category;
        private String code;
        private String description;

        public Verdict(UUID caseId, UUID originHearingId, UUID offenceId, UUID verdictId, UUID verdictValueId, String category, String code, String description) {
            this.caseId = caseId;
            this.originHearingId = originHearingId;
            this.offenceId = offenceId;
            this.verdictId = verdictId;
            this.verdictValueId = verdictValueId;
            this.category = category;
            this.code = code;
            this.description = description;
        }

        public UUID getCaseId() {
            return caseId;
        }

        public UUID getOriginHearingId() {
            return originHearingId;
        }

        public UUID getOffenceId() {
            return offenceId;
        }

        public UUID getVerdictId() {
            return verdictId;
        }

        public UUID getVerdictValueId() {
            return verdictValueId;
        }

        public String getCategory() {
            return category;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
