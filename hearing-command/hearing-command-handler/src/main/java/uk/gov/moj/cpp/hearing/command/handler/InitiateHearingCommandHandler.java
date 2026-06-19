package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.hearing.details.UpdateRelatedHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommandV2;
import uk.gov.moj.cpp.hearing.domain.aggregate.ApplicationAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class InitiateHearingCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(InitiateHearingCommandHandler.class.getName());

    private static final String PROGRESSION_QUERY_PROSECUTION_CASE = "progression.query.prosecutioncase";
    private static final String CASE_ID = "caseId";
    private static final String PROSECUTION_CASE = "prosecutionCase";

    @Inject
    private Requester requester;

    @Handles("hearing.initiate")
    public void initiate(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.initiate event received {}", envelope.toObfuscatedDebugString());
        }
        final InitiateHearingCommand command = convertToObject(envelope, InitiateHearingCommand.class);

        aggregate(HearingAggregate.class, command.getHearing().getId(), envelope, a -> a.initiate(command.getHearing()));

        final Hearing hearing = command.getHearing();
        final List<CourtApplication> courtApplications = hearing.getCourtApplications();
        if (courtApplications != null) {
            for (final CourtApplication courtApplication : courtApplications) {
                aggregate(ApplicationAggregate.class, courtApplication.getId(), envelope, a -> a.registerHearingId(courtApplication.getId(), hearing.getId()));
            }
        }
    }

    /**
     * This command is called from HearingExtendedEventProcessor to update the existing hearing.
     * This method call creates an event HearingExtend. For better command name and event name refer
     * {@link InitiateHearingCommandHandler#updateRelatedHearing(JsonEnvelope)}
     *
     * @param envelope
     * @throws EventStreamException
     */
    @Handles("hearing.command.extend-hearing")
    public void extendHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.extend-hearing received {}", envelope.toObfuscatedDebugString());
        }

        final ExtendHearingCommand command = convertToObject(envelope, ExtendHearingCommand.class);
        final UUID hearingId = command.getHearingId();

        // Extended hearing: an application listed onto an existing hearing carries its offences under
        // courtApplicationCases. Active offences (proceedingsConcluded != true) belong on the prosecution
        // side, so attach them under their owning defendant here (fetched from progression) before the
        // aggregate shapes the application/prosecution split.
        final List<ProsecutionCase> prosecutionCases = enrichWithActiveApplicationOffences(envelope, command.getCourtApplication(), command.getProsecutionCases());

        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.extend(hearingId,
                command.getHearingDays(), command.getCourtCentre(), command.getJurisdictionType(),
                command.getCourtApplication(), prosecutionCases, command.getShadowListedOffences()));

        if (nonNull(command.getCourtApplication())) {
            final UUID applicationId = command.getCourtApplication().getId();
            aggregate(ApplicationAggregate.class, applicationId, envelope, a -> a.registerHearingId(applicationId, hearingId));
        }

        if (nonNull(prosecutionCases)) {
            final List<Defendant> defendants = prosecutionCases.stream()
                    .filter(pc -> nonNull(pc.getDefendants()))
                    .flatMap(pc -> pc.getDefendants().stream())
                    .collect(toList());

            for (final Defendant defendant : defendants) {
                aggregate(DefendantAggregate.class, defendant.getId(), envelope, a -> a.registerHearing(defendant.getId(), hearingId));
            }
        }
    }

    /**
     * Builds prosecution-case entries for the active offences carried by an extended hearing's
     * application, attaching each active offence under its owning defendant (resolved from the
     * prosecution case held in progression). The aggregate then strips those moved offences from
     * the application side. Offences already present under {@code existingProsecutionCases} are
     * skipped (no lookup needed). An offence whose owning case/defendant cannot be resolved is left
     * untouched on the application (no data loss).
     */
    private List<ProsecutionCase> enrichWithActiveApplicationOffences(final JsonEnvelope envelope,
                                                                      final CourtApplication courtApplication,
                                                                      final List<ProsecutionCase> existingProsecutionCases) {
        if (isNull(courtApplication) || isEmpty(courtApplication.getCourtApplicationCases())) {
            return existingProsecutionCases;
        }

        // Offences already attached to the incoming prosecution cases are where they should be, so
        // there is no need to look them up again from progression.
        final Set<UUID> alreadyPresentOffenceIds = collectProsecutionOffenceIds(existingProsecutionCases);

        final Map<UUID, List<Offence>> activeOffencesByCaseId = new LinkedHashMap<>();
        courtApplication.getCourtApplicationCases().stream()
                .filter(courtApplicationCase -> nonNull(courtApplicationCase.getProsecutionCaseId()) && isNotEmpty(courtApplicationCase.getOffences()))
                .forEach(courtApplicationCase -> {
                    final List<Offence> offencesToResolve = courtApplicationCase.getOffences().stream()
                            .filter(offence -> !isProceedingsConcluded(offence))
                            .filter(offence -> !alreadyPresentOffenceIds.contains(offence.getId()))
                            .toList();
                    if (!offencesToResolve.isEmpty()) {
                        activeOffencesByCaseId.computeIfAbsent(courtApplicationCase.getProsecutionCaseId(), key -> new ArrayList<>()).addAll(offencesToResolve);
                    }
                });

        if (activeOffencesByCaseId.isEmpty()) {
            return existingProsecutionCases;
        }

        final List<ProsecutionCase> enriched = new ArrayList<>(isNull(existingProsecutionCases) ? new ArrayList<>() : existingProsecutionCases);
        activeOffencesByCaseId.forEach((caseId, activeOffences) ->
                fetchProsecutionCase(envelope, caseId)
                        .map(ownerCase -> buildMovedProsecutionCase(ownerCase, activeOffences))
                        .ifPresentOrElse(
                                movedCase -> mergeMovedCase(enriched, movedCase),
                                () -> LOGGER.warn("Could not resolve prosecution case {} for active application offences; leaving them on the application side", caseId)));
        return enriched;
    }

    /**
     * Merges a moved case into the working list. If the case is already present (same id), its
     * defendants/offences are merged into the existing entry rather than adding a duplicate case;
     * otherwise the case is added.
     */
    private void mergeMovedCase(final List<ProsecutionCase> prosecutionCases, final ProsecutionCase movedCase) {
        for (int index = 0; index < prosecutionCases.size(); index++) {
            final ProsecutionCase existingCase = prosecutionCases.get(index);
            if (movedCase.getId().equals(existingCase.getId())) {
                prosecutionCases.set(index, ProsecutionCase.prosecutionCase()
                        .withValuesFrom(existingCase)
                        .withDefendants(mergeDefendants(existingCase.getDefendants(), movedCase.getDefendants()))
                        .build());
                return;
            }
        }
        prosecutionCases.add(movedCase);
    }

    private List<Defendant> mergeDefendants(final List<Defendant> existingDefendants, final List<Defendant> movedDefendants) {
        final List<Defendant> merged = new ArrayList<>(isNull(existingDefendants) ? new ArrayList<>() : existingDefendants);
        for (final Defendant movedDefendant : movedDefendants) {
            final int index = indexOfDefendant(merged, movedDefendant.getId());
            if (index < 0) {
                merged.add(movedDefendant);
            } else {
                final Defendant existingDefendant = merged.get(index);
                merged.set(index, Defendant.defendant()
                        .withValuesFrom(existingDefendant)
                        .withOffences(mergeOffences(existingDefendant.getOffences(), movedDefendant.getOffences()))
                        .build());
            }
        }
        return merged;
    }

    private List<Offence> mergeOffences(final List<Offence> existingOffences, final List<Offence> movedOffences) {
        final List<Offence> merged = new ArrayList<>(isNull(existingOffences) ? new ArrayList<>() : existingOffences);
        final Set<UUID> existingOffenceIds = merged.stream().map(Offence::getId).collect(toSet());
        movedOffences.stream()
                .filter(offence -> !existingOffenceIds.contains(offence.getId()))
                .forEach(merged::add);
        return merged;
    }

    private static int indexOfDefendant(final List<Defendant> defendants, final UUID defendantId) {
        for (int index = 0; index < defendants.size(); index++) {
            if (defendantId.equals(defendants.get(index).getId())) {
                return index;
            }
        }
        return -1;
    }

    private static Set<UUID> collectProsecutionOffenceIds(final List<ProsecutionCase> prosecutionCases) {
        if (isEmpty(prosecutionCases)) {
            return Set.of();
        }
        return prosecutionCases.stream()
                .filter(prosecutionCase -> nonNull(prosecutionCase.getDefendants()))
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(defendant -> nonNull(defendant.getOffences()))
                .flatMap(defendant -> defendant.getOffences().stream())
                .map(Offence::getId)
                .collect(toSet());
    }

    private ProsecutionCase buildMovedProsecutionCase(final ProsecutionCase ownerCase, final List<Offence> activeOffences) {
        if (isEmpty(ownerCase.getDefendants())) {
            return null;
        }
        final List<Defendant> movedDefendants = ownerCase.getDefendants().stream()
                .filter(defendant -> nonNull(defendant.getOffences()))
                .map(defendant -> {
                    final Set<UUID> ownerOffenceIds = defendant.getOffences().stream().map(Offence::getId).collect(toSet());
                    final List<Offence> offencesForDefendant = activeOffences.stream()
                            .filter(offence -> ownerOffenceIds.contains(offence.getId()))
                            .collect(toList());
                    return offencesForDefendant.isEmpty() ? null
                            : Defendant.defendant().withValuesFrom(defendant).withOffences(offencesForDefendant).build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(toList());

        return movedDefendants.isEmpty() ? null
                : ProsecutionCase.prosecutionCase().withValuesFrom(ownerCase).withDefendants(movedDefendants).build();
    }

    private Optional<ProsecutionCase> fetchProsecutionCase(final JsonEnvelope envelope, final UUID caseId) {
        try {
            final JsonObject query = createObjectBuilder().add(CASE_ID, caseId.toString()).build();

            final Envelope<JsonObject> request = Enveloper.envelop(query).withName(PROGRESSION_QUERY_PROSECUTION_CASE).withMetadataFrom(envelope);
            final Envelope<JsonObject> response = requester.requestAsAdmin(request, JsonObject.class);

            if (isNull(response) || isNull(response.payload()) || !response.payload().containsKey(PROSECUTION_CASE)) {
                return Optional.empty();
            }
            return Optional.of(convertToObject(response.payload().getJsonObject(PROSECUTION_CASE), ProsecutionCase.class));
        } catch (final RuntimeException e) {
            LOGGER.warn("Unable to fetch prosecution case {} from progression: {}", caseId, e.getMessage());
            return Optional.empty();
        }
    }

    private static boolean isProceedingsConcluded(final Offence offence) {
        return Boolean.TRUE.equals(offence.getProceedingsConcluded());
    }

    /**
     * This is the new command handler which updates the existing hearing like
     * {@link InitiateHearingCommandHandler#extendHearing(JsonEnvelope)} and does the same
     * functionality.* But this command is created to have a meaningful command name and event
     * name(ExistingHearingUpdated).
     *
     * @param envelope
     * @throws EventStreamException
     */
    @Handles("hearing.command.update-related-hearing")
    public void updateRelatedHearing(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.update-related-hearing received {}", envelope.toObfuscatedDebugString());
        }

        final UpdateRelatedHearingCommand command = convertToObject(envelope, UpdateRelatedHearingCommand.class);
        final UUID hearingId = command.getHearingId();
        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.updateExistingHearing(hearingId, command.getProsecutionCases(), command.getShadowListedOffences()));

    }


    @Handles("hearing.command.register-hearing-against-offence")
    public void initiateHearingOffence(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.register-hearing-against-offence event received {}", envelope.toObfuscatedDebugString());
        }
        final RegisterHearingAgainstOffenceCommand command = convertToObject(envelope, RegisterHearingAgainstOffenceCommand.class);
        aggregate(OffenceAggregate.class, command.getOffenceId(), envelope, a -> a.lookupOffenceForHearing(command.getHearingId(), command.getOffenceId()));
    }

    @Handles("hearing.command.register-hearing-against-offence-v2")
    public void initiateHearingOffenceV2(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.register-hearing-against-offence-v2 event received {}", envelope.toObfuscatedDebugString());
        }
        final RegisterHearingAgainstOffenceCommandV2 command = convertToObject(envelope, RegisterHearingAgainstOffenceCommandV2.class);
        aggregate(OffenceAggregate.class, command.getOffenceId(), envelope, a -> a.lookupOffenceForHearingV2(command.getHearingIds(), command.getOffenceId()));
    }

    @Handles("hearing.command.register-hearing-against-defendant")
    public void recordHearingDefendant(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.register-hearing-against-defendant event received {}", envelope.toObfuscatedDebugString());
        }
        final RegisterHearingAgainstDefendantCommand command = convertToObject(envelope, RegisterHearingAgainstDefendantCommand.class);
        aggregate(DefendantAggregate.class, command.getDefendantId(), envelope, defendantAggregate -> defendantAggregate.registerHearing(command.getDefendantId(), command.getHearingId()));
    }

    @Handles("hearing.command.register-hearing-against-case")
    public void registerHearingAgainstCase(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.register-hearing-against-case event received {}", envelope.toObfuscatedDebugString());
        }
        final RegisterHearingAgainstCaseCommand command = convertToObject(envelope, RegisterHearingAgainstCaseCommand.class);
        aggregate(CaseAggregate.class, command.getCaseId(), envelope, caseAggregate -> caseAggregate.registerHearingId(command.getCaseId(), command.getHearingId()));
    }
}
