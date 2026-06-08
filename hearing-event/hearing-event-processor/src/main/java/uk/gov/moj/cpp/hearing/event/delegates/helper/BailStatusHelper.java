package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.deltaspike.core.util.CollectionUtils.isEmpty;
import static uk.gov.moj.cpp.hearing.event.helper.HearingHelper.getOffencesFromApplication;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.BailStatus;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;
import uk.gov.moj.cpp.hearing.pi.ProsecutionCaseRetriever;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCaseResponse;

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
import java.util.stream.Stream;

import javax.inject.Inject;

public class BailStatusHelper {

    private final ReferenceDataService referenceDataService;
    private final ProsecutionCaseRetriever prosecutionCaseRetriever;

    private static final String NHCCS_RESULT_DEFINITION_ID = "fbed768b-ee95-4434-87c8-e81cbc8d24c8";
    private static final String NHMC_RESULT_DEFINITION_ID = "70c98fa6-804d-11e8-adc0-fa7ae01bbebc";

    @Inject
    public BailStatusHelper(final ReferenceDataService referenceDataService,
                            final ProsecutionCaseRetriever prosecutionCaseRetriever) {
        this.referenceDataService = referenceDataService;
        this.prosecutionCaseRetriever = prosecutionCaseRetriever;
    }

    public void mapBailStatuses(final JsonEnvelope context, final Hearing hearing) {
        final List<BailStatus> bailStatusesFromRefData = referenceDataService.getBailStatuses(context);
        final UUID hearingId = hearing.getId();

        ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(d -> nonNull(d.getPersonDefendant()))
                .forEach(defendant -> updateDefendantWithBailStatus(defendant, bailStatusesFromRefData, hearingId));

        ofNullable(hearing.getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    updateDefendantWithBailStatus(ca.getSubject().getMasterDefendant(), bailStatusesFromRefData, offences, hearingId);
                });
    }

    public void mapBailStatuses(final JsonEnvelope context, final ResultsShared resultsShared) {
        final List<BailStatus> bailStatusesFromRefData = referenceDataService.getBailStatuses(context);
        final UUID hearingId = resultsShared.getHearingId();

        ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(d -> nonNull(d.getPersonDefendant()))
                .forEach(defendant -> updateDefendantWithBailStatus(defendant, bailStatusesFromRefData, hearingId));

        ofNullable(resultsShared.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    updateDefendantWithBailStatus(ca.getSubject().getMasterDefendant(), bailStatusesFromRefData, offences, hearingId);
                });
    }

    private void updateDefendantWithBailStatus(final Defendant defendant, final List<BailStatus> bailStatusesFromRefData, final UUID hearingId) {
        // Step 1: set per-offence bail status from this hearing's judicial results
        setOffenceRemandStatuses(defendant.getOffences(), bailStatusesFromRefData);

        // Step 2: build a complete view of all active offences across all hearings for this case
        final List<Offence> allActiveOffences = buildAllActiveOffences(defendant.getOffences(), hearingId, defendant.getId());

        // Step 3: derive defendant-level bail status from highest-priority active offence
        final uk.gov.justice.core.courts.BailStatus existingBailStatus = defendant.getPersonDefendant().getBailStatus();
        final Optional<BailStatus> bailStatusOptional = getHighestPriorityBailStatusFromOffences(allActiveOffences, bailStatusesFromRefData);
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

    private void updateDefendantWithBailStatus(final MasterDefendant defendant, final List<BailStatus> bailStatusesFromRefData, final List<Offence> offences, final UUID hearingId) {
        setOffenceRemandStatuses(offences, bailStatusesFromRefData);

        final List<Offence> allActiveOffences = buildAllActiveOffences(offences, hearingId, null);
        final Optional<BailStatus> bailStatusOptional = getHighestPriorityBailStatusFromOffences(allActiveOffences, bailStatusesFromRefData);
        bailStatusOptional.ifPresent(bailStatusResult ->
                defendant.getPersonDefendant().setBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus()
                        .withCode(bailStatusResult.getStatusCode())
                        .withDescription(bailStatusResult.getStatusDescription())
                        .withId(bailStatusResult.getId())
                        .build())
        );
    }

    /**
     * Merges the current hearing's offences (with freshly-computed bail statuses) with offences
     * stored in the viewstore from prior hearings. Current hearing offences take precedence.
     * Only active offences (proceedingsConcluded != true) are returned.
     */
    private List<Offence> buildAllActiveOffences(final List<Offence> currentHearingOffences, final UUID hearingId, final UUID defendantId) {
        // Index current hearing offences by offence ID (these have the freshest bail status)
        final Map<UUID, Offence> currentById = currentHearingOffences.stream()
                .filter(o -> nonNull(o.getId()))
                .collect(toMap(Offence::getId, o -> o, (a, b) -> a));

        // Retrieve stored offences from the viewstore for the full prosecution case
        final List<Offence> storedOffences = fetchStoredOffencesForDefendant(hearingId, defendantId);

        // Build merged list: for offences present in current hearing, use current; else use stored
        final List<Offence> merged = new ArrayList<>(currentHearingOffences);
        storedOffences.stream()
                .filter(stored -> nonNull(stored.getId()))
                .filter(stored -> !currentById.containsKey(stored.getId()))
                .forEach(merged::add);

        // Return only active offences
        return merged.stream()
                .filter(o -> !Boolean.TRUE.equals(o.getProceedingsConcluded()))
                .collect(toList());
    }

    private List<Offence> fetchStoredOffencesForDefendant(final UUID hearingId, final UUID defendantId) {
        if (hearingId == null) {
            return List.of();
        }
        return prosecutionCaseRetriever.getProsecutionCaseForHearing(hearingId, hearingId)
                .map(ProsecutionCaseResponse::getProsecutionCases)
                .orElse(List.of())
                .stream()
                .flatMap(pc -> ofNullable(pc.getDefendants()).map(Collection::stream).orElseGet(Stream::empty))
                .filter(d -> defendantId == null || defendantId.equals(d.getId()))
                .flatMap(d -> ofNullable(d.getOffences()).map(Collection::stream).orElseGet(Stream::empty))
                .collect(toList());
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
                .collect(toList());

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
     * Selects the highest-priority bail status from the supplied offences.
     * Offences with a null bailStatus are skipped (no remand status recorded yet).
     */
    private Optional<BailStatus> getHighestPriorityBailStatusFromOffences(final List<Offence> offences, final List<BailStatus> bailStatusesFromRefData) {
        if (isEmpty(offences)) {
            return empty();
        }

        return offences.stream()
                .map(Offence::getBailStatus)
                .filter(Objects::nonNull)
                .map(bs -> bailStatusesFromRefData.stream()
                        .filter(ref -> ref.getStatusCode().equalsIgnoreCase(bs.getCode()))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .min(comparing(BailStatus::getStatusRanking));
    }

    private Optional<BailStatus> getPostHearingCustodyStatusBasedOnRank(final Defendant defendant, final List<BailStatus> bailStatusesFromRefData) {
        final List<JudicialResult> judicialResults = defendant.getOffences().stream()
                .map(Offence::getJudicialResults)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return getBailStatusByJudicialResults(judicialResults, bailStatusesFromRefData);
    }

    private Optional<BailStatus> getPostHearingCustodyStatusBasedOnRank(final List<BailStatus> bailStatusesFromRefData, final List<Offence> offences) {
        final List<JudicialResult> judicialResults = offences.stream()
                .map(Offence::getJudicialResults)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(toList());

        return getBailStatusByJudicialResults(judicialResults, bailStatusesFromRefData);
    }

    private Optional<BailStatus> getBailStatusByJudicialResults(final List<JudicialResult> judicialResults, final List<BailStatus> bailStatusesFromRefData) {
        if (isEmpty(judicialResults) || (judicialResults.stream().allMatch(s -> s.getPostHearingCustodyStatus() != null && "A".equals(s.getPostHearingCustodyStatus()))
                && judicialResults.stream().anyMatch(s -> Arrays.asList(NHCCS_RESULT_DEFINITION_ID, NHMC_RESULT_DEFINITION_ID).contains(s.getJudicialResultTypeId().toString())))) {
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
