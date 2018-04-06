package uk.gov.moj.cpp.hearing.query.view.service;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.getHearing;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import uk.gov.moj.cpp.hearing.persist.HearingCaseRepository;
import uk.gov.moj.cpp.hearing.persist.HearingJudgeRepository;
import uk.gov.moj.cpp.hearing.persist.HearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.HearingCase;
import uk.gov.moj.cpp.hearing.persist.entity.HearingJudge;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ex.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Defendant;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.DefenceCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

/**
 * Unit tests for the HearingServiceTest class.
 */
@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingCaseRepository hearingCaseRepository;

    @Mock
    private HearingJudgeRepository hearingJudgeRepository;

    @Mock
    private AhearingRepository ahearingRepository;

    @InjectMocks
    private HearingService caseHearingService;

    @Test
    public void shouldFindHearingByIdTest() throws IOException {

        final Optional<Hearing> hearing = getHearing();

        final HearingCase arbitraryCase1 = new HearingCase(randomUUID(), hearing.get().getHearingId(), randomUUID());
        final HearingCase arbitraryCase2 = new HearingCase(randomUUID(), hearing.get().getHearingId(), randomUUID());
        final HearingJudge hearingJudge = new HearingJudge(hearing.get().getHearingId(), STRING.next(), STRING.next(), STRING.next(), STRING.next());

        final List<HearingCase> cases = new ArrayList<>();
        cases.add(arbitraryCase1);
        cases.add(arbitraryCase2);

        when(this.hearingRepository.getByHearingId(hearing.get().getHearingId())).thenReturn(hearing);
        when(this.hearingCaseRepository.findByHearingId(hearing.get().getHearingId())).thenReturn(cases);
        when(this.hearingJudgeRepository.findBy(hearing.get().getHearingId())).thenReturn(hearingJudge);

        HearingView hearingView = this.caseHearingService.getHearingById(hearing.get().getHearingId());
        assertEquals(HearingTestUtils.startDate, hearingView.getStartDate());
        assertEquals(hearingJudge.getId(), hearingView.getJudge().getId());
        assertEquals(hearingJudge.getFirstName(), hearingView.getJudge().getFirstName());
        assertEquals(hearingJudge.getLastName(), hearingView.getJudge().getLastName());
        assertEquals(hearingJudge.getTitle(), hearingView.getJudge().getTitle());
    }

    @Test
    public void shouldFindHearingListByStartDate() throws Exception {
        // 1. creating the mocked inputs and outputs objects
        //-----------------------------------------------------------------------
        ZonedDateTime startDateStartOfDay = HearingTestUtils.START_DATE_1.toLocalDate().atStartOfDay(ZoneOffset.systemDefault());
        final List<Ahearing> hearingList = HearingTestUtils.buildHearingList();

        // 2. setting the repository mock return
        //-----------------------------------------------------------------------
        when(ahearingRepository.findByStartDate(startDateStartOfDay)).thenReturn(hearingList);

        // 3. invoking the service with the given input mock value
        //-----------------------------------------------------------------------
        final HearingListResponse response = caseHearingService.getHearingByStartDateV2(HearingTestUtils.START_DATE_1.toLocalDate());

        // 4. performing the assertions
        //-----------------------------------------------------------------------
        assertEquals(hearingList.get(0).getId().toString(), response.getHearings().get(0).getHearingId());
        assertEquals(hearingList.get(0).getHearingType(), response.getHearings().get(0).getHearingType());
        assertEquals(hearingList.get(0).getDefendants().get(0).getOffences().stream()
                .map(o -> o.getLegalCase().getCaseurn()).collect(Collectors.toList()),
                response.getHearings().get(0).getCaseUrn());
        assertEquals(
                hearingList.get(0).getDefendants().stream().map(d -> d.getFirstName() + " " + d.getLastName())
                        .collect(Collectors.toList()),
                response.getHearings().get(0).getDefendants().stream()
                        .map(d -> d.getFirstName() + " " + d.getLastName()).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindHearingDetailsById() throws Exception {
        // 1. creating the mocked inputs and outputs objects
        //-----------------------------------------------------------------------
        final Ahearing hearing = HearingTestUtils.buildHearingList().get(0);

        // 2. setting the repository mock return
        //-----------------------------------------------------------------------
        when(ahearingRepository.findById(HearingTestUtils.HEARING_ID_1)).thenReturn(hearing);

        // 3. invoking the service with the given input mock value
        //-----------------------------------------------------------------------
        final HearingDetailsResponse response = caseHearingService.getHearingByIdV2(HearingTestUtils.HEARING_ID_1);

        // 4. performing assertions for hearing object attributes
        //-----------------------------------------------------------------------
        assertEquals(hearing.getId().toString(), response.getHearingId());
        assertEquals(hearing.getHearingType(), response.getHearingType());
        assertEquals(hearing.getStartDateTime().format(ISO_LOCAL_DATE), response.getStartDate());
        assertEquals(hearing.getStartDateTime().format(ISO_LOCAL_TIME), response.getStartTime());
        assertEquals(hearing.getCourtCentreId().toString(), response.getCourtCentreId());
        assertEquals(hearing.getCourtCentreName(), response.getCourtCentreName());
        assertEquals(hearing.getRoomId().toString(), response.getRoomId());
        assertEquals(hearing.getRoomName(), response.getRoomName());

        final List<ProsecutionCounsel> prosecutionCounsels = response.getAttendees().getProsecutionCounsels();
        final List<DefenceCounsel> defenceCounsels = response.getAttendees().getDefenceCounsels();
        
        final AtomicInteger judgeCounter = new AtomicInteger();
        final AtomicInteger procecutionCounter = new AtomicInteger();
        final AtomicInteger defenceCounter = new AtomicInteger();

        // 5. performing assertions for judge, prosecutionCounsels and defenceCounsels objects attributes
        //-----------------------------------------------------------------------
        hearing.getAttendees().forEach(attendee -> {

            if (attendee instanceof Judge) {

                final Judge entityBean = (Judge) attendee;
                assertEquals(entityBean.getId().getId().toString(), response.getJudge().getId());
                assertEquals(entityBean.getTitle(), response.getJudge().getTitle());
                assertEquals(entityBean.getFirstName(), response.getJudge().getFirstName());
                assertEquals(entityBean.getLastName(), response.getJudge().getLastName());
                judgeCounter.getAndIncrement();

            } else {

                if (attendee instanceof ProsecutionAdvocate) {
                    
                    final ProsecutionAdvocate entityBean = (ProsecutionAdvocate) attendee;

                    prosecutionCounsels.forEach(prosecutionCounsel -> {
                        if (entityBean.getId().getId().toString().equals(prosecutionCounsel.getAttendeeId())) {
                            assertEquals(entityBean.getTitle(), prosecutionCounsel.getTitle());
                            assertEquals(entityBean.getFirstName(), prosecutionCounsel.getFirstName());
                            assertEquals(entityBean.getLastName(), prosecutionCounsel.getLastName());
                            assertEquals(entityBean.getStatus(), prosecutionCounsel.getStatus());
                            procecutionCounter.getAndIncrement();
                        }
                    });

                } else if (attendee instanceof DefenceAdvocate) {
                    
                    final DefenceAdvocate entityBean = (DefenceAdvocate) attendee;
        
                    defenceCounsels.forEach(responseObject -> {
                        if (entityBean.getId().getId().toString().equals(responseObject.getAttendeeId())) {
                            assertEquals(entityBean.getTitle(), responseObject.getTitle());
                            assertEquals(entityBean.getFirstName(), responseObject.getFirstName());
                            assertEquals(entityBean.getLastName(), responseObject.getLastName());
                            assertEquals(entityBean.getStatus(), responseObject.getStatus());
                            defenceCounter.getAndIncrement();
                        }
                    });
                }
            }
        });
        
        assertEquals(1, judgeCounter.get());
        assertEquals(prosecutionCounsels.size(), procecutionCounter.get());
        assertEquals(defenceCounsels.size(), defenceCounter.get());

        // 6. performing assertions for legalCase objects attributes
        //-----------------------------------------------------------------------
        final Set<LegalCase> legalCases = hearing.getDefendants().stream().flatMap(d -> d.getOffences().stream().map(o -> o.getLegalCase())).collect(Collectors.toSet());
        assertEquals(legalCases.size(), response.getCases().size());
        
        legalCases.stream().forEach(entityBean -> {
            
            response.getCases().forEach(responseObject -> {
                
                if (entityBean.getId().toString().equals(responseObject.getCaseId())) {
                    assertEquals(entityBean.getCaseurn(), responseObject.getCaseUrn());
                }
            });
            
        });
        
        // 7. performing assertions for defendant objects attributes
        final Set<Defendant> defendants = response.getCases().stream().flatMap(c -> c.getDefendants().stream()).collect(Collectors.toSet());
        assertEquals(defendants.size(), hearing.getDefendants().size());
        
        defendants.forEach(responseObject -> {
            
            hearing.getDefendants().forEach(entityBean -> {
                
                if (entityBean.getId().getId().toString().equals(responseObject.getId())) {
                    assertEquals(entityBean.getPersonId().toString(), responseObject.getId());
                    assertEquals(entityBean.getFirstName(), responseObject.getFirstName());
                    assertEquals(entityBean.getLastName(), responseObject.getLastName());
                    assertEquals(entityBean.getHomeTelephone(), responseObject.getHomeTelephone());
                    assertEquals(entityBean.getMobileTelephone(), responseObject.getMobile());
                    assertEquals(entityBean.getFax(), responseObject.getFax());
                    assertEquals(entityBean.getEmail(), responseObject.getEmail());
                    assertEquals(entityBean.getDateOfBirth().format(ISO_LOCAL_DATE), responseObject.getDateOfBirth());
                    
                    final Address address = entityBean.getAddress();
                    assertNotNull(address);
                    assertEquals(address.getAddress1(), responseObject.getAddress().getAddress1());
                    assertEquals(address.getAddress2(), responseObject.getAddress().getAddress2());
                    assertEquals(address.getAddress3(), responseObject.getAddress().getAddress3());
                    assertEquals(address.getAddress4(), responseObject.getAddress().getAddress4());
                    assertEquals(address.getPostCode(), responseObject.getAddress().getPostCode());
                    assertEquals(format(address.getAddress1(),address.getAddress2(), address.getAddress3(), address.getAddress4(), address.getPostCode()), responseObject.getAddress().getFormattedAddress());
                }
                
            });
            
        });
    }
    
    /**
     * @param vals
     * @return
     */
    private static String format(final String... vals) {
        return join(vals, ' ').trim();
    }
}
