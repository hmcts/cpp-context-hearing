package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.JudicialResult.judicialResult;
import static uk.gov.justice.core.courts.JudicialResultPrompt.judicialResultPrompt;
import static uk.gov.justice.core.courts.Offence.offence;
import static uk.gov.justice.core.courts.PersonDefendant.personDefendant;
import static uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared.builder;

import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResultPrompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class BailStatusReasonHelperTest {


    @Test
    public void testMapBailStatusReason() {
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate();
        new BailStatusReasonHelper().setReason(resultsSharedTemplate);

        final String bailReasonResult = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailReasons();
        assertNotNull(bailReasonResult);
        assertThat(bailReasonResult, is("123" + System.lineSeparator() + "675"));
    }

    private ResultsShared buildResultsSharedTemplate() {
        return builder()
                .withHearing(hearing()
                        .withHearingDays(asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                                .withDefendants(singletonList(Defendant.defendant()
                                        .withOffences(asList(offence()
                                                .withJudicialResults(asList(judicialResult()
                                                        .withPostHearingCustodyStatus("CONDITIONAL")
                                                        .withJudicialResultPrompts(getJudicialResultPromptList("123", "BAILCONDREAS"))
                                                        .build(), judicialResult()
                                                        .withPostHearingCustodyStatus("UNCONDITIONAL")
                                                        .build()))
                                                .build(), offence()
                                                .withJudicialResults(asList(judicialResult()
                                                        .withPostHearingCustodyStatus("CONDITIONAL")
                                                        .build(), judicialResult()
                                                        .withJudicialResultPrompts(getJudicialResultPromptList("675", "BAILEXCEPTREAS"))
                                                        .withPostHearingCustodyStatus("UNCONDITIONAL")
                                                        .build()))
                                                .build()))
                                        .withPersonDefendant(personDefendant().build())
                                        .build())
                                )
                                .build()))
                        .build())
                .build();
    }

    private ImmutableList<JudicialResultPrompt> getJudicialResultPromptList(final String value, final String promptReference) {
        return of(judicialResultPrompt().withValue(value).withPromptReference(promptReference).build());
    }

}
