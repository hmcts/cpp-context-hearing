package uk.gov.moj.cpp.hearing.domain.aggregate;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import uk.gov.moj.cpp.hearing.command.plea.HearingPlea;
import uk.gov.moj.cpp.hearing.command.plea.Plea;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaAdded;
import uk.gov.moj.cpp.hearing.domain.event.HearingPleaChanged;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingAggregateTest {

    private UUID caseId;
    private UUID hearingId;
    private UUID defendantId;
    private UUID offenceId;
    private UUID personId;
    private UUID pleaId;
    private LocalDate pleaDate;

    @Before
    public void setUp() {
        caseId = randomUUID();
        hearingId = randomUUID();
        defendantId = randomUUID();
        offenceId = randomUUID();
        personId = randomUUID();
        pleaId = randomUUID();
        pleaDate = LocalDate.now();
    }

    @InjectMocks
    HearingAggregate hearingAggregate;

    @Ignore("GPE-3032")
    @Test
    public void testAddPleaForGuiltyPlea() {
        final String pleaValue = "GUILTY";
        final Plea plea = new Plea(pleaId, pleaValue, LocalDate.now());
        final HearingPlea hearingPlea = new HearingPlea(caseId, hearingId, defendantId, personId, offenceId, plea);
        final Stream<Object> events = hearingAggregate.addPlea(hearingPlea);
        final List<Object> eventsList = events.collect(Collectors.toList());
        assertEquals(2, eventsList.size());
        Object event = eventsList.get(0);
        assertEquals(ConvictionDateAdded.class, event.getClass());
        assertConvictionDateAddedEventValues((ConvictionDateAdded) event);
        event = eventsList.get(1);
        assertEquals(HearingPleaAdded.class, event.getClass());
        assertHearingPleaAddedEventValue((HearingPleaAdded) event, plea);
    }

    @Ignore("GPE-3032")
    @Test
    public void testChangePleaForGuiltyPlea() {
        final String pleaValue = "GUILTY";
        final Plea plea = new Plea(pleaId, pleaValue, LocalDate.now());
        final HearingPlea hearingPlea = new HearingPlea(caseId, hearingId, defendantId, personId, offenceId, plea);
        final Stream<Object> events = hearingAggregate.changePlea(hearingPlea);
        final List<Object> eventsList = events.collect(Collectors.toList());
        assertEquals(2, eventsList.size());
        Object event = eventsList.get(0);
        assertEquals(ConvictionDateAdded.class, event.getClass());
        assertConvictionDateAddedEventValues((ConvictionDateAdded) event);
        event = eventsList.get(1);
        assertEquals(HearingPleaChanged.class, event.getClass());
        assertHearingPleaChangedEventValue((HearingPleaChanged) event, plea);
    }

    @Ignore("GPE-3032")
    @Test
    public void testAddPleaForNotGuiltyPlea() {
        final String pleaValue = "NOT GUILTY";
        final Plea plea = new Plea(pleaId, pleaValue, LocalDate.now());
        final HearingPlea hearingPlea = new HearingPlea(caseId, hearingId, defendantId, personId, offenceId, plea);
        final Stream<Object> events = hearingAggregate.addPlea(hearingPlea);
        final List<Object> eventsList = events.collect(Collectors.toList());
        assertEquals(2, eventsList.size());
        Object event = eventsList.get(0);
        assertEquals(ConvictionDateRemoved.class, event.getClass());
        assertConvictionDateRemovedEventValues((ConvictionDateRemoved) event);
        event = eventsList.get(1);
        assertEquals(HearingPleaAdded.class, event.getClass());
        assertHearingPleaAddedEventValue((HearingPleaAdded) event, plea);
    }

    @Ignore("GPE-3032")
    @Test
    public void testChangePleaForNotGuiltyPlea() {
        final String pleaValue = "NOT GUILTY";
        final Plea plea = new Plea(pleaId, pleaValue, LocalDate.now());
        final HearingPlea hearingPlea = new HearingPlea(caseId, hearingId, defendantId, personId, offenceId, plea);
        final Stream<Object> events = hearingAggregate.changePlea(hearingPlea);
        final List<Object> eventsList = events.collect(Collectors.toList());
        assertEquals(2, eventsList.size());
        Object event = eventsList.get(0);
        assertEquals(ConvictionDateRemoved.class, event.getClass());
        assertConvictionDateRemovedEventValues((ConvictionDateRemoved) event);
        event = eventsList.get(1);
        assertEquals(HearingPleaChanged.class, event.getClass());
        assertHearingPleaChangedEventValue((HearingPleaChanged) event, plea);
    }


    private void assertConvictionDateAddedEventValues(final ConvictionDateAdded event) {
        assertEquals(offenceId, event.getOffenceId());
        assertEquals(pleaDate, event.getConvictionDate());
    }

    private void assertConvictionDateRemovedEventValues(final ConvictionDateRemoved event) {
        assertEquals(offenceId, event.getOffenceId());

    }

    private void assertHearingPleaAddedEventValue(final HearingPleaAdded event, final Plea plea) {
        assertEquals(caseId, event.getCaseId());
        assertEquals(hearingId, event.getHearingId());
        assertEquals(defendantId, event.getDefendantId());
        assertEquals(offenceId, event.getOffenceId());
        assertEquals(personId, event.getPersonId());
        assertEquals(plea, event.getPlea());
    }

    private void assertHearingPleaChangedEventValue(final HearingPleaChanged event, final Plea plea) {
        assertEquals(caseId, event.getCaseId());
        assertEquals(hearingId, event.getHearingId());
        assertEquals(defendantId, event.getDefendantId());
        assertEquals(offenceId, event.getOffenceId());
        assertEquals(personId, event.getPersonId());
        assertEquals(plea, event.getPlea());
    }
}
