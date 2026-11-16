package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.deltaspike.core.util.CollectionUtils.isEmpty;
import static uk.gov.moj.cpp.hearing.event.helper.HearingHelper.getOffencesFromApplication;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OffenceBailStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.BailStatus;
import uk.gov.moj.cpp.hearing.event.service.ProgressionService;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

public class BailStatusHelper {

    private final ReferenceDataService referenceDataService;
    private final ProgressionService progressionService;

    private static final String NHCCS_RESULT_DEFINITION_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";
    private static final String NHMC_RESULT_DEFINITION_ID = "70c98fa6-804d-11e8-adc0-fa7ae01bbebc";

    @Inject
    public BailStatusHelper(final ReferenceDataService referenceDataService,
                            final ProgressionService progressionService) {
        this.referenceDataService = referenceDataService;
        this.progressionService = progressionService;
    }

    public void mapBailStatuses(final JsonEnvelope context, final Hearing hearing) {
        final List<BailStatus> bailStatusesFromRefData = referenceDataService.getBailStatuses(context);

        mapProsecutionCaseBailStatuses(context, hearing.getProsecutionCases(), bailStatusesFromRefData);

        ofNullable(hearing.getCourtApplications()).stream().flatMap(Collection::stream)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    updateDefendantWithBailStatus(ca.getSubject().getMasterDefendant(), bailStatusesFromRefData, offences);
                });
    }

    public void mapBailStatuses(final JsonEnvelope context, final ResultsShared resultsShared) {
        final List<BailStatus> bailStatusesFromRefData = referenceDataService.getBailStatuses(context);

        mapProsecutionCaseBailStatuses(context, resultsShared.getHearing().getProsecutionCases(), bailStatusesFromRefData);

        ofNullable(resultsShared.getHearing().getCourtApplications()).stream().flatMap(Collection::stream)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    updateDefendantWithBailStatus(ca.getSubject().getMasterDefendant(), bailStatusesFromRefData, offences);
                });
    }

    /**
     * Progression is queried at most once per prosecution case; the defendants of that case are
     * then filtered from the same response.
     */
    private void mapProsecutionCaseBailStatuses(final JsonEnvelope context, final List<ProsecutionCase> prosecutionCases, final List<BailStatus> bailStatusesFromRefData) {
        final Map<UUID, Optional<ProsecutionCase>> progressionCases = new HashMap<>();

        ofNullable(prosecutionCases).stream().flatMap(Collection::stream)
                .forEach(prosecutionCase -> prosecutionCase.getDefendants().stream()
                        .filter(d -> nonNull(d.getPersonDefendant()))
                        .forEach(defendant -> updateDefendantWithBailStatus(defendant, bailStatusesFromRefData,
                                getProgressionCase(context, prosecutionCase.getId(), progressionCases))));
    }

    private Optional<ProsecutionCase> getProgressionCase(final JsonEnvelope context, final UUID caseId, final Map<UUID, Optional<ProsecutionCase>> progressionCases) {
        if (isNull(caseId)) {
            return empty();
        }
        return progressionCases.computeIfAbsent(caseId, id -> progressionService.getProsecutionCaseDetails(context, id));
    }

    private void updateDefendantWithBailStatus(final Defendant defendant, final List<BailStatus> bailStatusesFromRefData, final Optional<ProsecutionCase> progressionCase) {
        setOffenceRemandStatuses(defendant.getOffences(), bailStatusesFromRefData);

        final List<OffenceBailStatus> storedOffenceBailStatuses = progressionCase
                .map(prosecutionCase -> fetchStoredOffencesBailStatusForDefendant(prosecutionCase, defendant.getId()))
                .orElse(List.of());
        final List<OffenceBailStatus> allActiveOffenceBailStatuses = buildAllActiveOffenceBailStatuses(defendant.getOffences(), storedOffenceBailStatuses);

        final uk.gov.justice.core.courts.BailStatus existingBailStatus = defendant.getPersonDefendant().getBailStatus();
        final Optional<BailStatus> bailStatusOptional = getHighestPriorityBailStatus(allActiveOffenceBailStatuses, bailStatusesFromRefData);
        if (bailStatusOptional.isPresent()) {
            defendant.getPersonDefendant().setBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus()
                    .withCode(bailStatusOptional.get().getStatusCode())
                    .withDescription(bailStatusOptional.get().getStatusDescription())
                    .withId(bailStatusOptional.get().getId())
                    .build());
        } else {
            defendant.getPersonDefendant().setBailStatus(existingBailStatus);
        }
    }

    private void updateDefendantWithBailStatus(final MasterDefendant defendant, final List<BailStatus> bailStatusesFromRefData, final List<Offence> offences) {
        setOffenceRemandStatuses(offences, bailStatusesFromRefData);

        // No stored offences are looked up for an application subject: only the application's own offences are considered.
        final List<OffenceBailStatus> allActiveOffenceBailStatuses = buildAllActiveOffenceBailStatuses(offences, List.of());
        final Optional<BailStatus> bailStatusOptional = getHighestPriorityBailStatus(allActiveOffenceBailStatuses, bailStatusesFromRefData);
        bailStatusOptional.ifPresent(bailStatusResult ->
                defendant.getPersonDefendant().setBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus()
                        .withCode(bailStatusResult.getStatusCode())
                        .withDescription(bailStatusResult.getStatusDescription())
                        .withId(bailStatusResult.getId())
                        .build())
        );
    }

    private List<OffenceBailStatus> buildAllActiveOffenceBailStatuses(final List<Offence> currentHearingOffences, final List<OffenceBailStatus> storedOffenceBailStatuses) {
        final Map<UUID, OffenceBailStatus> currentActiveById = currentHearingOffences.stream()
                .filter(o -> nonNull(o.getId()) && isActiveOffence(o))
                .collect(toMap(Offence::getId, BailStatusHelper::toOffenceBailStatus, (a, b) -> a));

        final List<OffenceBailStatus> merged = currentHearingOffences.stream()
                .map(BailStatusHelper::toOffenceBailStatus)
                .collect(toCollection(ArrayList::new));

        storedOffenceBailStatuses.stream()
                .filter(stored -> nonNull(stored.getOffenceId()))
                .filter(stored -> !currentActiveById.containsKey(stored.getOffenceId()))
                .forEach(merged::add);

        return merged;
    }

    private static boolean isActiveOffence(final Offence stored) {
        return !Boolean.TRUE.equals(stored.getProceedingsConcluded());
    }

    private static OffenceBailStatus toOffenceBailStatus(final Offence offence) {
        final uk.gov.justice.core.courts.BailStatus bailStatus = offence.getBailStatus();
        return new OffenceBailStatus(
                offence.getId(),
                bailStatus == null ? null : bailStatus.getId(),
                bailStatus == null ? null : bailStatus.getCode(),
                bailStatus == null ? null : bailStatus.getDescription());
    }

    /**
     * Bail status of each active offence of the defendant as currently held by progression, falling
     * back to the defendant's own bail status when the offence has none.
     */
    private static List<OffenceBailStatus> fetchStoredOffencesBailStatusForDefendant(final ProsecutionCase progressionCase, final UUID defendantId) {
        if (defendantId == null) {
            return List.of();
        }

        return ofNullable(progressionCase.getDefendants()).stream().flatMap(Collection::stream)
                .filter(defendant -> defendantId.equals(defendant.getId()))
                .flatMap(defendant -> ofNullable(defendant.getOffences()).stream().flatMap(Collection::stream)
                        .filter(BailStatusHelper::isActiveOffence)
                        .map(offence -> toStoredOffenceBailStatus(offence, defendant)))
                .toList();
    }

    private static OffenceBailStatus toStoredOffenceBailStatus(final Offence offence, final Defendant defendant) {
        final Optional<uk.gov.justice.core.courts.BailStatus> bailStatus = ofNullable(offence.getBailStatus())
                .or(() -> ofNullable(defendant.getPersonDefendant()).map(PersonDefendant::getBailStatus));
        return new OffenceBailStatus(
                offence.getId(),
                bailStatus.map(uk.gov.justice.core.courts.BailStatus::getId).orElse(null),
                bailStatus.map(uk.gov.justice.core.courts.BailStatus::getCode).orElse(null),
                bailStatus.map(uk.gov.justice.core.courts.BailStatus::getDescription).orElse(null));
    }

    /**
     * Sets offence.bailStatus on each individual offence based on that offence's own main judicial
     * result. NHMC/NHCC suppress the update only when used as the main result
     * (parentJudicialResultId == null).
     */
    private void setOffenceRemandStatuses(final List<Offence> offences, final List<BailStatus> bailStatusesFromRefData) {
        if (isEmpty(offences)) {
            return;
        }
        offences.forEach(offence -> {
            final List<JudicialResult> offenceResults = ofNullable(offence.getJudicialResults()).orElse(List.of());
            final Optional<BailStatus> offenceBailStatus = resolveOffenceRemandStatus(offenceResults, bailStatusesFromRefData);
            offenceBailStatus.ifPresent(bs ->
                    offence.setBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus()
                            .withCode(bs.getStatusCode())
                            .withDescription(bs.getStatusDescription())
                            .withId(bs.getId())
                            .build())
            );
        });
    }

    /**
     * Derives the remand status for a single offence from its judicial results. Returns empty if
     * all qualifying results are NHMC/NHCC used as main result.
     */
    private Optional<BailStatus> resolveOffenceRemandStatus(final List<JudicialResult> judicialResults, final List<BailStatus> bailStatusesFromRefData) {
        if (isEmpty(judicialResults)) {
            return empty();
        }

        final List<JudicialResult> effectiveResults = judicialResults.stream()
                .filter(jr -> nonNull(jr.getPostHearingCustodyStatus()))
                .filter(jr -> !isExcludedMainResult(jr))
                .toList();

        if (effectiveResults.isEmpty()) {
            return empty();
        }

        return effectiveResults.stream()
                .map(jr -> buildRankFromJudicialResults(bailStatusesFromRefData, jr.getPostHearingCustodyStatus()))
                .filter(Objects::nonNull)
                .min(comparing(BailStatus::getStatusRanking));
    }

    /**
     * Returns true when the result is NHMC or NHCC used as a main result (parentJudicialResultId is
     * null). When used as a child result (parentJudicialResultId is non-null), the exclusion does
     * not apply.
     */
    private boolean isExcludedMainResult(final JudicialResult judicialResult) {
        if (judicialResult.getJudicialResultTypeId() == null) {
            return false;
        }
        final String typeId = judicialResult.getJudicialResultTypeId().toString();
        final boolean isExcludedType = NHMC_RESULT_DEFINITION_ID.equals(typeId) || NHCCS_RESULT_DEFINITION_ID.equals(typeId);
        final boolean isMainResult = judicialResult.getParentJudicialResultId() == null;
        return isExcludedType && isMainResult;
    }

    /**
     * Selects the highest-priority bail status from the supplied offence bail statuses. Entries
     * with a null bail status code are skipped (no remand status recorded yet).
     */
    private Optional<BailStatus> getHighestPriorityBailStatus(final List<OffenceBailStatus> offenceBailStatuses, final List<BailStatus> bailStatusesFromRefData) {
        if (isEmpty(offenceBailStatuses)) {
            return empty();
        }

        return offenceBailStatuses.stream()
                .map(OffenceBailStatus::getBailStatusCode)
                .filter(Objects::nonNull)
                .map(code -> bailStatusesFromRefData.stream()
                        .filter(ref -> ref.getStatusCode().equalsIgnoreCase(code))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .min(comparing(BailStatus::getStatusRanking));
    }

    private BailStatus buildRankFromJudicialResults(final List<BailStatus> bailStatusesFromRefData, final String postHearingCustodyStatus) {
        Optional<BailStatus> bailStatusOptional = empty();
        if (isNotEmpty(postHearingCustodyStatus)) {
            bailStatusOptional = bailStatusesFromRefData.stream()
                    .filter(bailStatus -> bailStatus.getStatusCode().equalsIgnoreCase(postHearingCustodyStatus))
                    .findFirst();
        }
        return bailStatusOptional.orElse(null);
    }
}
