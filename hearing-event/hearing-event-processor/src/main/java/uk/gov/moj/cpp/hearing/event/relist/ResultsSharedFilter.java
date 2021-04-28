package uk.gov.moj.cpp.hearing.event.relist;

import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("squid:S1612")
public class ResultsSharedFilter {
    public ResultsShared filterTargets(final ResultsShared resultsSharedUnfiltered, final Predicate<Target> include) {
        return ResultsShared.builder()
                .withTargets(resultsSharedUnfiltered.getTargets().stream().filter(t -> include.test(t)).filter(Objects::nonNull).collect(Collectors.toList()))
                .withCompletedResultLinesStatus(resultsSharedUnfiltered.getCompletedResultLinesStatus())
                .withSharedTime(resultsSharedUnfiltered.getSharedTime())
                .withCourtClerk(resultsSharedUnfiltered.getCourtClerk())
                .withHearing(resultsSharedUnfiltered.getHearing())
                .withHearingId(resultsSharedUnfiltered.getHearingId())
                .withVariantDirectory(resultsSharedUnfiltered.getVariantDirectory())
                .withSavedTargets(resultsSharedUnfiltered.getSavedTargets())
                .build();
    }

    public ResultsSharedV2 filterTargets(final ResultsSharedV2 resultsSharedUnfiltered, final Predicate<Target> include) {
        return ResultsSharedV2.builder()
                .withTargets(resultsSharedUnfiltered.getTargets().stream().filter(t -> include.test(t)).filter(Objects::nonNull).collect(Collectors.toList()))
                .withCompletedResultLinesStatus(resultsSharedUnfiltered.getCompletedResultLinesStatus())
                .withSharedTime(resultsSharedUnfiltered.getSharedTime())
                .withCourtClerk(resultsSharedUnfiltered.getCourtClerk())
                .withHearing(resultsSharedUnfiltered.getHearing())
                .withHearingId(resultsSharedUnfiltered.getHearingId())
                .withVariantDirectory(resultsSharedUnfiltered.getVariantDirectory())
                .withSavedTargets(resultsSharedUnfiltered.getSavedTargets())
                .build();
    }
}
