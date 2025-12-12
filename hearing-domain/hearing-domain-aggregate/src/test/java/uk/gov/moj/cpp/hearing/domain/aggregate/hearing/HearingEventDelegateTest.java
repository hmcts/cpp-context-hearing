package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class HearingEventDelegateTest {

    private HearingAggregateMomento momento = new HearingAggregateMomento();
    private HearingEventDelegate hearingDelegate = new HearingEventDelegate(momento);
    private String courtApplicationReference = "TEST-APPLICATION-REFERENCE";
    private String caseURN = "39GD9692220";


    @Test
    public void handleMomentoHearingWhenCourtApplicationsAreEmpty() {
        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of())
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), caseURN, "REF12345")))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is(caseURN));
    }

    @Test
    public void handleMomentoHearingWhenCourtApplicationsArePresented() {
        final CourtApplication courtApplication = createCourtApplication(UUID.randomUUID());

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of(courtApplication))
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), caseURN, "REF12345")))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is(caseURN));
    }


    @Test
    public void handleMomentoHearingWhenCourtApplicationsArePresentedCaseURNIsEmpty() {
        final CourtApplication courtApplication = createCourtApplication(UUID.randomUUID());

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of(courtApplication))
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), null, "REF12345")))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is("REF12345"));
    }

    @Test
    public void handleMomentoHearingWhenCourtApplicationsEmptyCaseURNIsEmpty() {

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of())
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), null, "REF12345")))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is("REF12345"));
    }

    @Test
    public void handleMomentoHearingWhenCourtApplicationsCaseURNAndReferenceIsEmpty() {
        final CourtApplication courtApplication = createCourtApplication(UUID.randomUUID());

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of(courtApplication))
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), null, null)))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is(courtApplicationReference));
    }

    @Test
    public void handleMomentoHearingWhenCourtApplicationsEmptyCaseURNAndReferenceIsEmpty() {
        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of())
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), null, null)))
                .build());

        String reference = hearingDelegate.getReference();
        Assert.assertNull(reference);
    }


    private CourtApplication createCourtApplication(final UUID id) {
        return new CourtApplication.Builder()
                .withId(id)
                .withApplicationReference(courtApplicationReference)
                .build();
    }

    private ProsecutionCase createProsecutionCase(final UUID id, final String caseURN, final String reference) {
        return new ProsecutionCase.Builder()
                .withId(id)
                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier().withCaseURN(caseURN).withProsecutionAuthorityReference(reference).build())
                .build();
    }
}
