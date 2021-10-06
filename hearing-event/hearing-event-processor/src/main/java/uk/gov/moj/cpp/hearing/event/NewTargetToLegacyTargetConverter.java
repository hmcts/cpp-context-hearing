package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.core.courts.Target2.target2;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.core.courts.Target2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class NewTargetToLegacyTargetConverter {


    public List<Target2> convert(final List<Target2> newTargets) {

        final List<Target2> legacyTargets = new ArrayList<>();

        newTargets.stream().forEach(target ->
            target.getResultLines().stream().forEach(resultLine ->
                legacyTargets.add(createTargetInLegacyFormat(target,resultLine))
            )
        );


        return legacyTargets;
    }

    private Target2 createTargetInLegacyFormat(final Target2 target, final ResultLine2 resultLine) {
                    return  target2()
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
