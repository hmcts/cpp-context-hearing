package uk.gov.moj.cpp.hearing.repository;

import static org.junit.Assert.assertEquals;
import static uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCaseTest.buildLegalCase1;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.gov.moj.cpp.hearing.persist.entity.ha.AttendeeHearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;

@RunWith(CdiTestRunner.class)
public class AttendeeHearingDateRepositoryTest {

    private static final List<Hearing> hearings = new ArrayList<>();
    private static final LegalCase legalCase1 = buildLegalCase1();
 
    @Inject
    private HearingRepository hearingRepository;
    @Inject
    private LegalCaseRepository legalCaseRepository;
    @Inject
    private AttendeeHearingDateRespository attendeeHearingDateRespository;

    @BeforeClass
    public static void create() {
        final Hearing hearing = HearingRepositoryTestUtils.buildHearing(legalCase1);
        hearings.add(hearing);
    }

    @Before
    public void setup() {
        legalCaseRepository.save(legalCase1);
        hearings.forEach(hearing -> {
            hearingRepository.save(hearing);
            hearing.getAttendees().forEach(attendee -> {
                attendeeHearingDateRespository.save(AttendeeHearingDate.builder()
                        .withId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()))
                        .withHearingDateId(hearing.getHearingDays().get(0).getId().getId())
                        .withAttendeeId(attendee.getId().getId())
                        .build());
            });
        });
    }

    @After
    public void teardown() {
        hearings.forEach(hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getId())));
        legalCaseRepository.attachAndRemove(legalCaseRepository.findBy(legalCase1.getId()));
    }

    @Test
    public void shouldDeleteByHearingIdAndAttendeeIdAndHearingDate() throws Exception {
        final AtomicLong deleteCounter = new AtomicLong(0);
        hearings.forEach(hearing -> {
            hearing.getAttendees().forEach(attendee -> {
                assertEquals(1, attendeeHearingDateRespository.delete(hearing.getId(), attendee.getId().getId(), hearing.getHearingDays().get(0).getId().getId()));
                deleteCounter.incrementAndGet();
            });
        });
        assertEquals(hearings.stream().flatMap(h -> h.getAttendees().stream()).count(), deleteCounter.get());
    }
}