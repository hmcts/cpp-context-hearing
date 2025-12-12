package uk.gov.moj.cpp.hearing.mapping;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.ApplicationAmendmentAllowedResolver.isAmendmentAllowed;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class ApplicationAmendmentAllowedResolverTest {

    @Test
    public void givenApplicationHasNoAssociatedHearings_thenAmendmentAllowedResolved_toTrue() {
        final UUID hearingId = randomUUID();
        final boolean amendmentAllowed = isAmendmentAllowed(hearingId, List.of());

        assertThat(amendmentAllowed, is(true));
    }

    @Test
    public void givenApplicationHasHearings_andApplicationStatusNotFinalisedInAnyHearings_thenAmendmentAllowedResolved_toTrue() {
        final UUID hearingId = randomUUID();
        final HearingApplication hearingApplication = new HearingApplication();
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        final Target target = new Target();
        target.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        target.setApplicationId(randomUUID());
        hearing.setTargets(Set.of(target));
        hearingApplication.setHearing(hearing);

        final boolean amendmentAllowed = isAmendmentAllowed(hearingId, List.of(hearingApplication));

        assertThat(amendmentAllowed, is(true));
    }

    @Test
    public void givenApplicationHasHearings_andApplicationStatusFinalisedInADifferentHearing_thenAmendmentAllowedResolved_toFalse() {
        final UUID hearingId = randomUUID();
        final UUID otherHearingId = randomUUID();
        final HearingApplication hearingApplication = new HearingApplication();
        final Hearing hearing = new Hearing();
        hearing.setId(otherHearingId);
        final Target target = new Target();
        target.setId(new HearingSnapshotKey(randomUUID(), otherHearingId));
        target.setApplicationId(randomUUID());
        target.setApplicationFinalised(TRUE);
        hearing.setTargets(Set.of(target));
        hearingApplication.setHearing(hearing);

        final boolean amendmentAllowed = isAmendmentAllowed(hearingId, List.of(hearingApplication));

        assertThat(amendmentAllowed, is(false));
    }

    @Test
    public void givenApplicationHasHearings_andApplicationStatusFinalisedInThisHearing_thenAmendmentAllowedResolved_toTrue() {
        final UUID hearingId = randomUUID();
        final HearingApplication hearingApplication = new HearingApplication();
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        final Target target = new Target();
        target.setId(new HearingSnapshotKey(randomUUID(), hearingId));
        target.setApplicationId(randomUUID());
        target.setApplicationFinalised(TRUE);
        hearing.setTargets(Set.of(target));
        hearingApplication.setHearing(hearing);

        final boolean amendmentAllowed = isAmendmentAllowed(hearingId, List.of(hearingApplication));

        assertThat(amendmentAllowed, is(true));
    }
}