package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.stream.Stream.empty;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.match;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.otherwiseDoNothing;
import static uk.gov.justice.domain.aggregate.matcher.EventSwitcher.when;

import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.domain.aggregate.Aggregate;
import uk.gov.moj.cpp.hearing.command.defendant.Defendant;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.DefendantAggregateMomento;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.NCESNotificationDecisionDelegate;
import uk.gov.moj.cpp.hearing.domain.event.CaseDefendantDetailsWithHearings;
import uk.gov.moj.cpp.hearing.domain.event.DefendantCaseWithdrawnOrDismissed;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.DefendantOffenceResultsUpdated;
import uk.gov.moj.cpp.hearing.domain.event.FoundHearingsForNewOffence;
import uk.gov.moj.cpp.hearing.domain.event.HearingDeletedForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.HearingMarkedAsDuplicateForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.HearingRemovedForDefendant;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstDefendant;
import uk.gov.moj.cpp.hearing.nces.ApplicationDetailsForDefendant;
import uk.gov.moj.cpp.hearing.nces.DefendantUpdateWithApplicationDetails;
import uk.gov.moj.cpp.hearing.nces.DefendantUpdateWithFinancialOrderDetails;
import uk.gov.moj.cpp.hearing.nces.FinancialOrderForDefendant;
import uk.gov.moj.cpp.hearing.nces.RemoveGrantedApplicationDetailsForDefendant;
import uk.gov.moj.cpp.hearing.nces.UpdateDefendantWithApplicationDetails;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S00107", "squid:S1948"})
public class DefendantAggregate implements Aggregate {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefendantAggregate.class);
    private static final long serialVersionUID = 3L;

    private final Set<UUID> hearingIds = new HashSet<>();

    private DefendantAggregateMomento momento = new DefendantAggregateMomento();

    private NCESNotificationDecisionDelegate ncesNotificationDecisionDelegate = new NCESNotificationDecisionDelegate(momento);

    private final Map<UUID, OffenceResult> offenceResults = new HashMap<>();

    @SuppressWarnings("squid:S2250")
    @Override
    public Object apply(Object event) {

        return match(event)
                .with(
                        when(RegisteredHearingAgainstDefendant.class).apply(defendant -> hearingIds.add(defendant.getHearingId())),
                        when(DefendantUpdateWithFinancialOrderDetails.class).apply(this::handleUpdateDefendantWithFinancialOrder),
                        when(DefendantUpdateWithApplicationDetails.class).apply(this::handleUpdateDefendantWithApplicationDetails),
                        when(RemoveGrantedApplicationDetailsForDefendant.class).apply(this::handleRemoveGrantedApplicationDetailsForDefendant),
                        when(DefendantOffenceResultsUpdated.class).apply(e ->
                                updateOffenceResults(this.offenceResults, e.getOffenceIds(), e.getResultedOffences())
                        ),
                        when(DefendantCaseWithdrawnOrDismissed.class).apply(e -> {
                        }),
                        when(DefendantLegalAidStatusUpdated.class).apply(e -> {
                        }),
                        when(HearingMarkedAsDuplicateForDefendant.class).apply(e -> hearingIds.remove(e.getHearingId())),
                        when(HearingDeletedForDefendant.class).apply(e -> hearingIds.remove(e.getHearingId())),
                        when(HearingRemovedForDefendant.class).apply(e -> hearingIds.remove(e.getHearingId())),
                        otherwiseDoNothing()
                );
    }

    public Stream<Object> registerHearing(final UUID defendantId, final UUID hearingId) {
        return apply(Stream.of(
                RegisteredHearingAgainstDefendant.builder()
                        .withDefendantId(defendantId)
                        .withHearingId(hearingId)
                        .build()
        ));
    }

    public Stream<Object> enrichCaseDefendantDetailsWithHearingIds(final Defendant defendant) {
        if (hearingIds.isEmpty()) {
            return Stream.empty();
        }

        final CaseDefendantDetailsWithHearings caseDefendantDetailsWithHearings =
                CaseDefendantDetailsWithHearings.caseDefendantDetailsWithHearings()
                        .setDefendant(defendant)
                        .setHearingIds(new ArrayList<>(hearingIds));

        return apply(Stream.of(caseDefendantDetailsWithHearings));
    }

    public Stream<Object> lookupHearingsForNewOffenceOnDefendant(final UUID defendantId, final UUID prosecutionCaseId, final Offence offence) {
        return hearingIds.isEmpty() ? empty() : apply(Stream.of(FoundHearingsForNewOffence.foundHearingsForNewOffence()
                .withDefendantId(defendantId)
                .withProsecutionCaseId(prosecutionCaseId)
                .withOffence(offence)
                .withHearingIds(new ArrayList<>(hearingIds))
        ));
    }

    public Stream<Object> updateDefendantLegalAidStatus(final UUID defendantId, final String legalAidStatus) {
        if (!hearingIds.isEmpty()) {
            return apply(Stream.of(DefendantLegalAidStatusUpdated.defendantLegalAidStatusUpdatedBuilder()
                    .withDefendantId(defendantId)
                    .withLegalAidStatus(legalAidStatus)
                    .withHearingIds(new ArrayList<>(hearingIds))
                    .build()

            ));
        } else {
            return empty();
        }
    }

    public Stream<Object> updateDefendantWithFinancialOrder(final FinancialOrderForDefendant financialOrderForDefendant) {
        LOGGER.info("Nces-notification : updateDefendantWithFinancialOrder.............");
        return apply(ncesNotificationDecisionDelegate.updateDefendantWithFinancialOrder(financialOrderForDefendant));
    }

    public Stream<Object> updateDefendantWithApplicationDetails(final UpdateDefendantWithApplicationDetails updateDefendantWithApplicationDetails) {
        LOGGER.info("Nces-notification : updateDefendantWithApplicationDetails...........");
        return apply(ncesNotificationDecisionDelegate.updateDefendantWithApplicationDetails(ApplicationDetailsForDefendant.newBuilder()
                .withApplicationTypeId(updateDefendantWithApplicationDetails.getApplicationTypeId())
                .build()));
    }

    public Stream<Object> updateOffenceResults(final UUID defendantId, final UUID caseId, final List<UUID> offenceIds, final Map<UUID, OffenceResult> updatedResults) {
        final Map<UUID, OffenceResult> offenceResultsCopy = new HashMap(this.offenceResults);
        updateOffenceResults(offenceResultsCopy, offenceIds, updatedResults);

        final Stream.Builder<Object> builder = Stream.builder();
        builder.add(
                DefendantOffenceResultsUpdated.newBuilder()
                        .withDefendantId(defendantId)
                        .withOffenceIds(offenceIds)
                        .withResultedOffences(updatedResults)
                        .build()
        );

        if (computeAllOffencesWithdrawnOrDismissed(offenceResultsCopy)) {
            builder.add(
                    DefendantCaseWithdrawnOrDismissed.newBuilder()
                            .withDefendantId(defendantId)
                            .withCaseId(caseId)
                            .withResultedOffences(offenceResultsCopy)
                            .build());
        }

        return apply(builder.build());
    }

    public Stream<Object> markHearingAsDuplicate(final UUID defendantId, final UUID hearingId) {
        return apply(Stream.of(new HearingMarkedAsDuplicateForDefendant(defendantId, hearingId)));
    }

    public void handleUpdateDefendantWithFinancialOrder(final DefendantUpdateWithFinancialOrderDetails defendantUpdateWithFinancialOrderDetails) {
        this.momento.setFinancialOrderForDefendant(defendantUpdateWithFinancialOrderDetails.getFinancialOrderForDefendant());
    }

    public void handleUpdateDefendantWithApplicationDetails(final DefendantUpdateWithApplicationDetails defendantWithApplicationDetails) {
        this.momento.setApplicationDetailsForDefendant(defendantWithApplicationDetails.getApplicationDetailsForDefendant());
    }

    @SuppressWarnings({"squid:S1172"})
    private void handleRemoveGrantedApplicationDetailsForDefendant(final RemoveGrantedApplicationDetailsForDefendant removeGrantedApplicationDetailsForDefendant) {
        this.momento.setApplicationDetailsForDefendant(null);
    }

    public void setMomento(final DefendantAggregateMomento momento) {
        this.momento = momento;
    }


    private void updateOffenceResults(final Map<UUID, OffenceResult> offenceResultsCopy, final List<UUID> offenceIds, final Map<UUID, OffenceResult> results) {
        offenceIds.stream().forEach(offenceId -> offenceResultsCopy.putIfAbsent(offenceId, OffenceResult.ADJOURNED));
        results.entrySet().stream().forEach(p -> offenceResultsCopy.put(p.getKey(), p.getValue()));
    }

    public static boolean computeAllOffencesWithdrawnOrDismissed(final Map<UUID, OffenceResult> offenceResults) {
        if (offenceResults.isEmpty()) {
            return false;
        }
        return offenceResults.entrySet().stream().noneMatch(entry -> entry.getValue().equals(OffenceResult.ADJOURNED) || entry.getValue().equals(OffenceResult.GUILTY));
    }

    public Stream<Object> deleteHearingForDefendant(final UUID defendantId, final UUID hearingId) {
        return apply(Stream.of(new HearingDeletedForDefendant(defendantId, hearingId)));
    }

    public Stream<Object> removeHearingForDefendant(final UUID defendantId, final UUID hearingId) {
        return apply(Stream.of(new HearingRemovedForDefendant(defendantId, hearingId)));
    }
}