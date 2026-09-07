package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.moj.cpp.hearing.event.helper.HearingHelper.getOffencesFromApplication;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OffenceBailStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.BailStatus;
import uk.gov.moj.cpp.hearing.event.service.OffenceService;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

public class BailStatusHelper {

    private final ReferenceDataService referenceDataService;
    private final OffenceService offenceService;

    private static final String NHCCS_RESULT_DEFINITION_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";
    private static final String NHMC_RESULT_DEFINITION_ID = "70c98fa6-804d-11e8-adc0-fa7ae01bbebc";

    @Inject
    public BailStatusHelper(final ReferenceDataService referenceDataService,
                            final OffenceService offenceService) {
        this.referenceDataService = referenceDataService;
        this.offenceService = offenceService;
    }

    public void mapBailStatuses(final JsonEnvelope context, final Hearing hearing) {
        final List<BailStatus> bailStatusesFromRefData = referenceDataService.getBailStatuses(context);

        ofNullable(hearing.getProsecutionCases()).stream().flatMap(Collection::stream)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(d -> nonNull(d.getPersonDefendant()))
                .forEach(defendant -> updateDefendantWithBailStatus(defendant, bailStatusesFromRefData));

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

        ofNullable(resultsShared.getHearing().getProsecutionCases()).stream().flatMap(Collection::stream)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(d -> nonNull(d.getPersonDefendant()))
                .forEach(defendant -> updateDefendantWithBailStatus(defendant, bailStatusesFromRefData));

        ofNullable(resultsShared.getHearing().getCourtApplications()).stream().flatMap(Collection::stream)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    updateDefendantWithBailStatus(ca.getSubject().getMasterDefendant(), bailStatusesFromRefData, offences);
                });
    }

    private void updateDefendantWithBailStatus(final Defendant defendant, final List<BailStatus> bailStatusesFromRefData) {
        setOffenceRemandStatuses(defendant.getOffences(), bailStatusesFromRefData);

        final List<OffenceBailStatus> allActiveOffenceBailStatuses = buildAllActiveOffenceBailStatuses(defendant.getOffences(), defendant.getId());

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

        final List<OffenceBailStatus> allActiveOffenceBailStatuses = buildAllActiveOffenceBailStatuses(offences, defendant.getCpsDefendantId());
        final Optional<BailStatus> bailStatusOptional = getHighestPriorityBailStatus(allActiveOffenceBailStatuses, bailStatusesFromRefData);
        bailStatusOptional.ifPresent(bailStatusResult ->
                defendant.getPersonDefendant().setBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus()
                        .withCode(bailStatusResult.getStatusCode())
                        .withDescription(bailStatusResult.getStatusDescription())
                        .withId(bailStatusResult.getId())
                        .build())
        );
    }

    private List<OffenceBailStatus> buildAllActiveOffenceBailStatuses(final List<Offence> currentHearingOffences, final UUID defendantId) {
        final Map<UUID, OffenceBailStatus> currentActiveById = currentHearingOffences.stream()
                .filter(o -> nonNull(o.getId()) && isActiveOffence(o))
                .collect(toMap(Offence::getId, BailStatusHelper::toOffenceBailStatus, (a, b) -> a));

        final List<OffenceBailStatus> merged = currentHearingOffences.stream()
                .map(BailStatusHelper::toOffenceBailStatus)
                .collect(toCollection(ArrayList::new));

        fetchStoredOffencesBailStatusForDefendant(defendantId).stream()
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

    private List<OffenceBailStatus> fetchStoredOffencesBailStatusForDefendant(final UUID defendantId) {
        if (defendantId == null) {
            return List.of();
        }

        return offenceService.getOffenceBailStatus(defendantId);
    }

    /**
     * Sets offence.bailStatus on each individual offence based on that offence's own main judicial result.
     * NHMC/NHCC suppress the update only when used as the main result (parentJudicialResultId == null).
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
     * Derives the remand status for a single offence from its judicial results.
     * Returns empty if all qualifying results are NHMC/NHCC used as main result.
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
     * Returns true when the result is NHMC or NHCC used as a main result (parentJudicialResultId is null).
     * When used as a child result (parentJudicialResultId is non-null), the exclusion does not apply.
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
     * Selects the highest-priority bail status from the supplied offence bail statuses.
     * Entries with a null bail status code are skipped (no remand status recorded yet).
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

    private Optional<BailStatus> getBailStatusByJudicialResults(final List<JudicialResult> judicialResults, final List<BailStatus> bailStatusesFromRefData) {
        if ((judicialResults == null || judicialResults.isEmpty()) || (judicialResults.stream().allMatch(s -> s.getPostHearingCustodyStatus() != null && "A".equals(s.getPostHearingCustodyStatus()))
                && judicialResults.stream().anyMatch(s -> Arrays.asList(NHCCS_RESULT_DEFINITION_ID,NHMC_RESULT_DEFINITION_ID).contains(s.getJudicialResultTypeId().toString())))) {
            return empty();
        }
        Set<BailStatus> collect = judicialResults.stream()
                .filter(j -> nonNull(j.getPostHearingCustodyStatus()))
                .map(judicialResult -> buildRankFromJudicialResults(bailStatusesFromRefData, judicialResult.getPostHearingCustodyStatus()))
                .collect(Collectors.toSet());
        return collect
                .stream()
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
