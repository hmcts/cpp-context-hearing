package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;

import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.moj.cpp.hearing.domain.event.HearingInitiated;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import org.junit.Test;

public class HearingDelegateTest {
    private static final String GUILTY = "GUILTY";

    private HearingAggregateMomento momento = new HearingAggregateMomento();
    private HearingDelegate hearingDelegate = new HearingDelegate(momento);

    @Test
    public void handleHearingInitiated_FirstTimeHearing() {
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        assertThat(momento.getHearing(), is(notNullValue()));
        assertThat(momento.getPleas(), equalTo(Collections.EMPTY_MAP));
        assertThat(momento.getVerdicts(), equalTo(Collections.EMPTY_MAP));
        assertThat(momento.getConvictionDates(), equalTo(Collections.EMPTY_MAP));
    }

    @Test
    public void handleHearingInitiated_AdjournedOrSubsequentHearing() {
        final LocalDate convictionDateForFirstOffence = PAST_LOCAL_DATE.next();
        final CommandHelpers.InitiateHearingCommandHelper hearing = h(standardInitiateHearingTemplate());
        final Offence firstOffenceForFirstDefendantForFirstCase = hearing.getFirstOffenceForFirstDefendantForFirstCase();
        final UUID idOfFirstOffenceForFirstDefendantForFirstCase = firstOffenceForFirstDefendantForFirstCase.getId();
        firstOffenceForFirstDefendantForFirstCase.setPlea(Plea.plea().withPleaValue(GUILTY).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
        firstOffenceForFirstDefendantForFirstCase.setVerdict(Verdict.verdict().withVerdictType(VerdictType.verdictType().withCategoryType("GUILTY").build()).withOffenceId(idOfFirstOffenceForFirstDefendantForFirstCase).build());
        firstOffenceForFirstDefendantForFirstCase.setConvictionDate(convictionDateForFirstOffence);
        hearingDelegate.handleHearingInitiated(new HearingInitiated(hearing.getHearing()));

        assertThat(momento.getHearing(), is(notNullValue()));
        assertThat(momento.getPleas(), hasKey(idOfFirstOffenceForFirstDefendantForFirstCase));
        assertThat(momento.getVerdicts(), hasKey(idOfFirstOffenceForFirstDefendantForFirstCase));
        assertThat(momento.getConvictionDates(), hasEntry(idOfFirstOffenceForFirstDefendantForFirstCase, convictionDateForFirstOffence));
    }


}