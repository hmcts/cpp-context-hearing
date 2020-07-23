package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.BailStatus;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class BailStatusHelper {

    private final ReferenceDataService referenceDataService;


    @Inject
    public BailStatusHelper(final ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public void mapBailStatuses(final JsonEnvelope context, final ResultsShared resultsShared) {
        final List<BailStatus> bailStatusesFromRefData = referenceDataService.getBailStatuses(context);
        final List<Defendant> defendants = resultsShared.getHearing().getProsecutionCases().stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream()).collect(Collectors.toList());

        for (final Defendant defendant : defendants) {
            if (nonNull(defendant.getPersonDefendant())) {
                final Optional<BailStatus> bailStatusOptional = getPostHearingCustodyStatusBasedOnRank(defendant, bailStatusesFromRefData);
                bailStatusOptional.ifPresent(s -> {
                    final BailStatus bailStatusResult = bailStatusOptional.get();
                    defendant.getPersonDefendant()
                            .setBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode(bailStatusResult.getStatusCode()).withDescription(bailStatusResult.getStatusDescription()).withId(bailStatusResult.getId()).build());
                });
            }
        }
    }

    private Optional<BailStatus> getPostHearingCustodyStatusBasedOnRank(final Defendant defendant, final List<BailStatus> bailStatusesFromRefData) {
        final List<JudicialResult> judicialResults = defendant.getOffences().stream()
                .filter(offence -> nonNull(offence.getJudicialResults()))
                .map(Offence::getJudicialResults)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (isNotEmpty(judicialResults)) {
            Set<BailStatus> collect = judicialResults.stream()
                    .filter(j -> nonNull(j.getPostHearingCustodyStatus()))
                    .map(judicialResult -> buildRankFromJudicialResults(bailStatusesFromRefData, judicialResult.getPostHearingCustodyStatus()))
                    .collect(Collectors.toSet());
            return collect
                    .stream()
                    .filter(entry -> nonNull(entry))
                    .sorted(comparing(BailStatus::getStatusRanking))
                    .findFirst();
        }
        return empty();
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


    private class PostHearingCustodyStatus {

        private final String hearingCustodyStatus;

        private final Integer rank;

        PostHearingCustodyStatus(final String hearingCustodyStatus, final Integer rank) {
            this.hearingCustodyStatus = hearingCustodyStatus;
            this.rank = rank;
        }

        String getHearingCustodyStatus() {
            return hearingCustodyStatus;
        }

        Integer getRank() {
            return rank;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final PostHearingCustodyStatus that = (PostHearingCustodyStatus) o;

            return Objects.equals(hearingCustodyStatus, that.hearingCustodyStatus) && Objects.equals(rank, that.rank);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hearingCustodyStatus, rank);
        }
    }

}
