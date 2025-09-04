package uk.gov.moj.cpp.hearing.mapping;

import static java.lang.Boolean.TRUE;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;

import java.util.List;
import java.util.UUID;

public class ApplicationAmendmentAllowedResolver {

    public static boolean isAmendmentAllowed(final UUID hearingId, final List<HearingApplication> applicationHearings) {
        if (isNotEmpty(applicationHearings)) {
            final boolean isApplicationFinalisedInOneOfTheHearings = applicationHearings.stream()
                    .map(HearingApplication::getHearing)
                    .filter(h -> isNotEmpty(h.getTargets()))
                    .flatMap(h -> h.getTargets().stream())
                    .anyMatch(t -> TRUE.equals(t.getApplicationFinalised()));
            final boolean isApplicationFinalisedInThisHearing = applicationHearings.stream()
                    .map(HearingApplication::getHearing)
                    .filter(h -> hearingId.equals(h.getId()))
                    .filter(h -> isNotEmpty(h.getTargets()))
                    .flatMap(h -> h.getTargets().stream())
                    .anyMatch(t -> TRUE.equals(t.getApplicationFinalised()));

            //allowed when applicationStatus not finalised in any of the hearings OR applicationStatus finalised in this hearing; false otherwise
            return !isApplicationFinalisedInOneOfTheHearings || isApplicationFinalisedInThisHearing;
        }

        return true;
    }
}
