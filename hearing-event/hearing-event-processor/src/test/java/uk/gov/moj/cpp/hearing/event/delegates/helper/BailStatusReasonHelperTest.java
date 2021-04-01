package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.justice.core.courts.Offence.offence;
import static uk.gov.justice.core.courts.PersonDefendant.personDefendant;
import static uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared.builder;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class BailStatusReasonHelperTest {


    @Test
    public void testMapBailStatusReason() {
        final ResultsShared resultsSharedTemplate = buildSharedTemplate(true, true, false, false);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);

        final String bailReasonResult = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailReasons();
        assertNotNull(bailReasonResult);
        assertThat(bailReasonResult, is("123" + System.lineSeparator() + "675"));
    }

    @Test
    public void testMapBailStatusReasonWithCourtApplicationCases() {
        final ResultsShared resultsSharedTemplate = buildSharedTemplate(true, false, true, false);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);

        final String bailReasonResult = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailReasons();
        assertNotNull(bailReasonResult);
        assertThat(bailReasonResult, is("123" + System.lineSeparator() + "675"));
    }

    @Test
    public void testMapBailStatusReasonWithCourtApplicationCourtOrder() {
        final ResultsShared resultsSharedTemplate = buildSharedTemplate(true, false, false , true);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);

        final String bailReasonResult = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailReasons();
        assertNotNull(bailReasonResult);
        assertThat(bailReasonResult, is("123" + System.lineSeparator() + "675"));
    }

    @Test
    public void testNoMapBailStatusReason() {
        final ResultsShared resultsSharedTemplate = buildSharedTemplate(false, true, false, false);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);

        final String bailReasonResult = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailReasons();
        assertNull(bailReasonResult);
    }

    @Test
    public void testNoMapBailStatusReasonWithCourtApplicationCases() {
        final ResultsShared resultsSharedTemplate = buildSharedTemplate(false, false, true, false);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);

        final String bailReasonResult = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailReasons();
        assertNull(bailReasonResult);
    }

    @Test
    public void testNoMapBailStatusReasonWithCourtApplicationCourtOrder() {
        final ResultsShared resultsSharedTemplate = buildSharedTemplate(false, false, false, true);
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);

        final String bailReasonResult = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailReasons();
        assertNull(bailReasonResult);
    }

    private ResultsShared buildSharedTemplate(final boolean withResults, final Boolean withProsecutionCases, final boolean withCourtApplicationCases, final boolean withCourtOrder) {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withProsecutionCases(withProsecutionCases ? getProsecutionCases(withResults) : null)
                        .withCourtApplications(withCourtApplicationCases || withCourtOrder ? singletonList(CourtApplication.courtApplication()
                                .withSubject(CourtApplicationParty.courtApplicationParty()
                                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                                .withPersonDefendant(personDefendant()
                                                        .build())
                                                .build())
                                        .build())
                                .withCourtApplicationCases(withCourtApplicationCases ? getCourtApplicationCases(withResults) : null)
                                .withCourtOrder(withCourtOrder ? getCourtOrder(withResults) : null)
                                .build()) : null)
                        .build())
                .build();
    }

    private List<ProsecutionCase> getProsecutionCases(final boolean withResults) {
        return singletonList(ProsecutionCase.prosecutionCase()
                .withDefendants(singletonList(Defendant.defendant()
                        .withOffences(asList(offence()
                                .withJudicialResults(withResults ? asList(getJudicialResults("123")) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? asList(getJudicialResults("123")) : null)
                                .build(), offence()
                                .withJudicialResults(withResults ? asList(getJudicialResults("675")) : null)
                                .build()))
                        .withPersonDefendant(personDefendant().build())
                        .build())
                )
                .build());
    }

    private CourtOrder getCourtOrder(final boolean withResults) {
        return CourtOrder.courtOrder()
                .withCourtOrderOffences(asList(CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? asList(getJudicialResults("123")) : null)
                                .build())
                        .build(), CourtOrderOffence.courtOrderOffence()
                        .withOffence(offence()
                                .withJudicialResults(withResults ? asList(getJudicialResults("675")) : null)
                                .build())
                        .build()))
                .build();
    }

    private List<CourtApplicationCase> getCourtApplicationCases(final boolean withResults) {
        return singletonList(CourtApplicationCase.courtApplicationCase()
                .withOffences(asList(offence()
                        .withJudicialResults(withResults ? asList(getJudicialResults("123")) : null)
                        .build(), offence()
                        .withJudicialResults(withResults ? asList(getJudicialResults("675")) : null)
                        .build()))
                .withCaseStatus("ACTIVE")
                .build());
    }

    private JudicialResult[] getJudicialResults(final String promptValue) {
        return new JudicialResult[]{judicialResult()
                .withPostHearingCustodyStatus("CONDITIONAL")
                .withJudicialResultPrompts(getJudicialResultPromptList(promptValue, "bailExceptionReason"))
                .build(), judicialResult()
                .withPostHearingCustodyStatus("UNCONDITIONAL")
                .build()};
    }


    private ImmutableList<JudicialResultPrompt> getJudicialResultPromptList(final String value, final String promptReference) {
        return of(judicialResultPrompt().withValue(value).withPromptReference(promptReference).build());
    }

}
