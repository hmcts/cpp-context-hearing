package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.DefendantJudicialResult.defendantJudicialResult;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.HearingResultsCleanerUtil.removeResultsFromHearing;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantJudicialResult;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;

import org.junit.Test;

public class HearingResultsCleanerUtilTest {

    @Test
    public void shouldRemoveDefendantLevelResultsFromHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels();

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());
        assertThat(processedHearing.getDefendantJudicialResults(), nullValue());
    }

    @Test
    public void shouldRemoveCaseLevelResultsFromHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels();

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());
        assertThat(processedHearing.getProsecutionCases().get(0).getDefendants().get(0).getDefendantCaseJudicialResults(), nullValue());
    }

    @Test
    public void shouldRemoveOffenceLevelResultsFromHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels();

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());
        assertThat(processedHearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults(), nullValue());
    }


    private Hearing getHearingWithResultsAtAllLevels() {
        final Offence offence = Offence.offence().withJudicialResults(newArrayList(judicialResult().withJudicialResultId(randomUUID()).build())).build();
        final Defendant defendant = Defendant.defendant().withOffences(newArrayList(offence)).withDefendantCaseJudicialResults(newArrayList(judicialResult().withJudicialResultId(randomUUID()).build())).build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase().withDefendants(newArrayList(defendant)).build();
        final DefendantJudicialResult defendantJudicialResult = defendantJudicialResult()
                .withMasterDefendantId(randomUUID())
                .withJudicialResult(judicialResult()
                        .withJudicialResultId(randomUUID())
                        .build())
                .build();
        return Hearing.hearing().
                withDefendantJudicialResults(newArrayList(defendantJudicialResult))
                .withProsecutionCases(newArrayList(prosecutionCase))
                .build();

    }

}