package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

public class ConvictionDateDelegateTest {

    private final UUID offenceId = UUID.randomUUID();
    private final UUID offenceIdUnderApplication = UUID.randomUUID();
    private final UUID caseId = UUID.randomUUID();
    private final UUID courtApplicationId = UUID.randomUUID();

    @Test
    public void shouldHandleConvictionDateAdded_whenHearingHasMultipleCases() {
        //given
        final LocalDate convictionDate = LocalDate.now();
        final HearingAggregateMomento momento = createHearingAggregateMomento();
        final ConvictionDateAdded convictionDateAdded = ConvictionDateAdded.convictionDateAdded()
                .setOffenceId(offenceId)
                .setCaseId(caseId)
                .setConvictionDate(convictionDate);
        final ConvictionDateDelegate convictionDateDelegate = new ConvictionDateDelegate(momento);

        //when
        convictionDateDelegate.handleConvictionDateAdded(convictionDateAdded);

        //then
        assertEquals(convictionDate, momento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getConvictionDate());
    }

    @Test
    public void shouldHandleConvictionDateAddedToOffenceUnderCourtApplication_whenHearingHasMultipleCases() {
        //given
        final LocalDate convictionDate = LocalDate.now();
        final HearingAggregateMomento momento = createHearingAggregateMomento();
        final ConvictionDateAdded convictionDateAdded = ConvictionDateAdded.convictionDateAdded()
                .setOffenceId(offenceIdUnderApplication)
                .setCourtApplicationId(courtApplicationId)
                .setConvictionDate(convictionDate);
        final ConvictionDateDelegate convictionDateDelegate = new ConvictionDateDelegate(momento);

        //when
        convictionDateDelegate.handleConvictionDateAdded(convictionDateAdded);

        //then
        assertEquals(convictionDate, momento.getHearing().getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getConvictionDate());
    }

    @Test
    public void shouldHandleConvictionDateRemoved_whenHearingHasMultipleCases() {
        //given
        final HearingAggregateMomento momento = createHearingAggregateMomento();
        final ConvictionDateRemoved convictionDateRemoved = ConvictionDateRemoved.convictionDateRemoved()
                .setOffenceId(offenceId)
                .setCaseId(caseId);
        final ConvictionDateDelegate convictionDateDelegate = new ConvictionDateDelegate(momento);

        //when
        convictionDateDelegate.handleConvictionDateRemoved(convictionDateRemoved);

        //then
        assertNull(momento.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getConvictionDate());
    }

    @Test
    public void shouldHandleConvictionDateRemovedFromOffenceUnderApplication_whenHearingHasMultipleCases() {
        //given
        final HearingAggregateMomento momento = createHearingAggregateMomento();
        final ConvictionDateRemoved convictionDateRemoved = ConvictionDateRemoved.convictionDateRemoved()
                .setOffenceId(offenceId)
                .setCourtApplicationId(courtApplicationId);
        final ConvictionDateDelegate convictionDateDelegate = new ConvictionDateDelegate(momento);

        //when
        convictionDateDelegate.handleConvictionDateRemoved(convictionDateRemoved);

        //then
        assertNull(momento.getHearing().getCourtApplications().get(0).getCourtApplicationCases().get(0).getOffences().get(0).getConvictionDate());
    }

    private HearingAggregateMomento createHearingAggregateMomento() {
        final HearingAggregateMomento momento = new HearingAggregateMomento();
        momento.setHearing(Hearing.hearing()
                .withProsecutionCases(Arrays.asList(
                        ProsecutionCase.prosecutionCase()
                                .withId(caseId)
                                .withDefendants(Arrays.asList(Defendant.defendant()
                                        .withId(UUID.randomUUID())
                                        .withOffences(Arrays.asList(Offence.offence()
                                                .withId(offenceId)
                                                .build()))
                                        .build()))
                                .build(),
                        ProsecutionCase.prosecutionCase()
                                .withId(UUID.randomUUID())
                                .withDefendants(Arrays.asList(Defendant.defendant()
                                        .withId(UUID.randomUUID())
                                        .withOffences(Arrays.asList(Offence.offence()
                                                .withId(UUID.randomUUID())
                                                .build()))
                                        .build()))
                                .build()
                ))
                .withCourtApplications(singletonList(CourtApplication.courtApplication()
                        .withId(courtApplicationId)
                        .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                                .withIsSJP(false)
                                .withCaseStatus("ACTIVE")
                                .withOffences(Arrays.asList(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(offenceIdUnderApplication).build(), uk.gov.justice.core.courts.Offence.offence()
                                        .withId(randomUUID()).build()))
                                .build()))
                        .build()))
                .build());

        return momento;
    }
}
