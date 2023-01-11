package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BailStatusHelper {

    private final ReferenceDataService referenceDataService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BailStatusHelper.class.getName());

    @Inject
    public BailStatusHelper(final ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    public void mapBailStatuses(final JsonEnvelope context, final Hearing hearing) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final List<BailStatus> bailStatusesFromRefData = referenceDataService.getBailStatuses(context);

        ofNullable(hearing.getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(d -> nonNull(d.getPersonDefendant()))
                .forEach(defendant -> updateDefendantWithBailStatus(defendant, bailStatusesFromRefData));

        ofNullable(hearing.getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    updateDefendantWithBailStatus(ca.getSubject().getMasterDefendant(), bailStatusesFromRefData, offences);
                });

        stopWatch.stop();
        if (LOGGER.isErrorEnabled()) {
            LOGGER.error("id is {} mapBailStatuses took {} milliseconds", nonNull(context.metadata()) ? context.metadata().id() : "", stopWatch.getTime());
        }

    }

    public void mapBailStatuses(final JsonEnvelope context, final ResultsShared resultsShared) {
        final List<BailStatus> bailStatusesFromRefData = referenceDataService.getBailStatuses(context);

        ofNullable(resultsShared.getHearing().getProsecutionCases()).map(Collection::stream).orElseGet(Stream::empty)
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .filter(d -> nonNull(d.getPersonDefendant()))
                .forEach(defendant -> updateDefendantWithBailStatus(defendant, bailStatusesFromRefData));

        ofNullable(resultsShared.getHearing().getCourtApplications()).map(Collection::stream).orElseGet(Stream::empty)
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant()))
                .filter(ca -> nonNull(ca.getSubject().getMasterDefendant().getPersonDefendant()))
                .forEach(ca -> {
                    final List<Offence> offences = getOffencesFromApplication(ca);
                    updateDefendantWithBailStatus(ca.getSubject().getMasterDefendant(), bailStatusesFromRefData, offences);
                });
    }


    private void updateDefendantWithBailStatus(Defendant defendant, final List<BailStatus> bailStatusesFromRefData) {
        final Optional<BailStatus> bailStatusOptional = getPostHearingCustodyStatusBasedOnRank(defendant, bailStatusesFromRefData);
        bailStatusOptional.ifPresent(bailStatusResult ->
                defendant.getPersonDefendant().setBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus()
                        .withCode(bailStatusResult.getStatusCode()).
                        withDescription(bailStatusResult.getStatusDescription())
                        .withId(bailStatusResult.getId())
                        .build())
        );
    }

    private void updateDefendantWithBailStatus(final MasterDefendant defendant, final List<BailStatus> bailStatusesFromRefData, final List<Offence> offences) {
        final Optional<BailStatus> bailStatusOptional = getPostHearingCustodyStatusBasedOnRank(bailStatusesFromRefData, offences);
        bailStatusOptional.ifPresent(bailStatusResult ->
                defendant.getPersonDefendant().setBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus()
                        .withCode(bailStatusResult.getStatusCode())
                        .withDescription(bailStatusResult.getStatusDescription())
                        .withId(bailStatusResult.getId())
                        .build())
        );
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
        if (isNotEmpty(judicialResults)) {
            Set<BailStatus> collect = judicialResults.stream()
                    .filter(j -> nonNull(j.getPostHearingCustodyStatus()))
                    .map(judicialResult -> buildRankFromJudicialResults(bailStatusesFromRefData, judicialResult.getPostHearingCustodyStatus()))
                    .collect(Collectors.toSet());
            return collect
                    .stream()
                    .filter(Objects::nonNull)
                    .min(comparing(BailStatus::getStatusRanking));
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
}
