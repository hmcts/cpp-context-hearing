package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.core.courts.Target.target;

import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NewTargetToLegacyTargetConverter {


    public List<Target> convert(final List<Target> newTargets) {

        final List<Target> legacyTargets = new ArrayList<>();

        newTargets.stream().forEach(target ->
            target.getResultLines().stream().forEach(resultLine ->
                legacyTargets.add(createTargetInLegacyFormat(target,resultLine))
            )
        );


        return legacyTargets;
    }

    private Target createTargetInLegacyFormat(final Target target, final ResultLine resultLine) {
                    return  target()
                    .withApplicationId(resultLine.getApplicationId())
                    .withCaseId(target.getCaseId())
                    .withDefendantId(resultLine.getDefendantId())
                    .withMasterDefendantId(resultLine.getMasterDefendantId())
                    .withHearingId(target.getHearingId())
                    .withOffenceId(resultLine.getOffenceId())
                    .withResultLines(Arrays.asList(resultLine))
                    .withTargetId(UUID.randomUUID())
                    .withHearingDay(target.getHearingDay())
                    .withShadowListed(target.getShadowListed())
                    .withDraftResult(target.getDraftResult())
                    .build();
    }
}
