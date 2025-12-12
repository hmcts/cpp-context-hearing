package uk.gov.moj.cpp.hearing.domain.aggregate.util;

import static com.google.common.collect.Lists.asList;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.CourtApplication.courtApplication;
import static uk.gov.justice.core.courts.CourtApplicationCase.courtApplicationCase;
import static uk.gov.justice.core.courts.CourtOrder.courtOrder;
import static uk.gov.justice.core.courts.CourtOrderOffence.courtOrderOffence;
import static uk.gov.justice.core.courts.DefendantJudicialResult.defendantJudicialResult;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.Offence.offence;
import static uk.gov.moj.cpp.hearing.domain.aggregate.util.HearingResultsCleanerUtil.removeResultsFromHearing;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantJudicialResult;
import uk.gov.justice.core.courts.FutureSummonsHearing;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class HearingResultsCleanerUtilTest {

    @Test
    public void shouldRemoveDefendantLevelResultsFromHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels(false);

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());
        assertThat(processedHearing.getDefendantJudicialResults(), nullValue());
    }

    @Test
    public void shouldRemoveCaseLevelResultsFromHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels(false);

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());
        assertThat(processedHearing.getProsecutionCases().get(0).getDefendants().get(0).getDefendantCaseJudicialResults(), nullValue());
    }

    @Test
    public void shouldRemoveOffenceLevelResultsFromHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels(false);

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());
        assertThat(processedHearing.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getJudicialResults(), nullValue());
    }

    @Test
    public void shouldRemoveApplicationLevelResultsFromHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels(false);

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());

        assertThat(processedHearing.getCourtApplications().get(0).getFutureSummonsHearing(), nullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getJudicialResults(), nullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults(), nullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults(), nullValue());
    }

    @Test
    public void shouldNotRemoveFutureSummonsHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels(true);

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());

        assertThat(processedHearing.getCourtApplications().get(0).getFutureSummonsHearing(), notNullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getJudicialResults(), nullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults(), nullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults(), nullValue());
    }

    @Test
    public void shouldRemoveFutureSummonsHearing() {
        final Hearing sourceHearing = getHearingWithResultsAtAllLevels(null);

        final Hearing processedHearing = removeResultsFromHearing(sourceHearing);
        assertThat(processedHearing, notNullValue());

        assertThat(processedHearing.getCourtApplications().get(0).getFutureSummonsHearing(), nullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getJudicialResults(), nullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getJudicialResults(), nullValue());
        assertThat(processedHearing.getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getJudicialResults(), nullValue());
    }


    private Hearing getHearingWithResultsAtAllLevels(Boolean boxHearing) {
        final Offence offence = offence().withJudicialResults(newArrayList(judicialResult().withJudicialResultId(randomUUID()).build())).build();
        final Defendant defendant = Defendant.defendant().withOffences(newArrayList(offence)).withDefendantCaseJudicialResults(newArrayList(judicialResult().withJudicialResultId(randomUUID()).build())).build();
        final ProsecutionCase prosecutionCase = ProsecutionCase.prosecutionCase().withDefendants(newArrayList(defendant)).build();
        final DefendantJudicialResult defendantJudicialResult = defendantJudicialResult()
                .withMasterDefendantId(randomUUID())
                .withJudicialResult(judicialResult()
                        .withJudicialResultId(randomUUID())
                        .build())
                .build();
        final CourtApplication courtApplication = courtApplication()
                .withFutureSummonsHearing(FutureSummonsHearing.futureSummonsHearing().build())
                .withCourtOrder(courtOrder().withCourtOrderOffences(singletonList(courtOrderOffence().withOffence(offence().withJudicialResults(singletonList(judicialResult().build())).build()).build())).build())
                .withCourtApplicationCases(singletonList(courtApplicationCase().withOffences(singletonList(offence().withJudicialResults(singletonList(judicialResult().build())).build())).build()))
                .withJudicialResults(singletonList(judicialResult().build()))
                .build();
        return Hearing.hearing().
                withDefendantJudicialResults(newArrayList(defendantJudicialResult))
                .withProsecutionCases(newArrayList(prosecutionCase))
                .withCourtApplications(newArrayList(courtApplication))
                .withIsBoxHearing(boxHearing)
                .build();

    }

}