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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.DefenceCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.HearingDetailsResponse.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.HearingView;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

/**
 * Unit tests for the HearingServiceTest class.
 */
@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    public static final String EXPECTED_HEARING_LIST_RESPONSE = "{\"hearings\":[{\"hearingId\":\"23ef34ec-63e5-422e-8071-9b3753008c10\",\"hearingType\":\"TRIAL\",\"caseUrn\":[\"8C720B32E45B\"],\"defendants\":[{\"firstName\":\"Ken\",\"lastName\":\"Thompson\"},{\"firstName\":\"William Nelson\",\"lastName\":\"Joy\"}]}]}";
    public static final String EXPECTED_HEARING_DETAILS_RESPONSE = "{\"hearingId\":\"23ef34ec-63e5-422e-8071-9b3753008c10\",\"startDate\":\"2018-02-22\",\"startTime\":\"10:30:00\",\"roomName\":\"3-1\",\"hearingType\":\"TRIAL\",\"courtCentreName\":\"Liverpool Crown Court\",\"judge\":{\"id\":\"a38d0d5f-a26c-436b-9b5e-4dc58f28878d\",\"title\":\"HHJ\",\"firstName\":\"Alan\",\"lastName\":\"Mathison Turing\"},\"roomId\":\"e7721a38-546d-4b56-9992-ebdd772a561b\",\"courtCentreId\":\"e8821a38-546d-4b56-9992-ebdd772a561f\",\"attendees\":{\"prosecutionCounsels\":[{\"attendeeId\":\"841164f6-13bc-46ff-8634-63cf9ae85d36\",\"status\":\"QC\",\"title\":\"MR\",\"firstName\":\"Brian J.\",\"lastName\":\"Fox\"}],\"defenceCounsels\":[{\"attendeeId\":\"743d333a-b270-4de6-a598-61abb64a8027\",\"status\":\"QC\",\"title\":\"MR\",\"firstName\":\"Mark\",\"lastName\":\"Zuckerberg\"},{\"attendeeId\":\"cdc14b89-6b4d-4e98-9641-826c355c51b8\",\"status\":\"QC\",\"title\":\"MR\",\"firstName\":\"Sean\",\"lastName\":\"Parker\"}]},\"cases\":[{\"caseId\":\"9b70743c-69b3-4ac2-a362-8c720b32e45b\",\"caseUrn\":\"8C720B32E45B\",\"defendants\":[{\"defendantId\":\"841164f6-13bc-46ff-8634-63cf9ae85d36\",\"personId\":\"5a6e2001-91ed-4af2-99af-f30ddc9ef5af\",\"firstName\":\"Ken\",\"lastName\":\"Thompson\",\"homeTelephone\":\"02070101010\",\"mobile\":\"07422263910\",\"fax\":\"021111111\",\"email\":\"ken.thompson@acme.me\",\"address\":{\"formatedAddress\":\"222 Furze Road Exeter Lorem Ipsum Solor Porro Quisquam CR0 1XG\",\"address1\":\"222 Furze Road Exeter\",\"address2\":\"Lorem Ipsum\",\"address3\":\"Solor\",\"address4\":\"Porro Quisquam\",\"postCode\":\"CR0 1XG\"},\"dateOfBirth\":\"1943-02-04\",\"offences\":[{\"id\":\"4b1318e4-1517-4e4f-a89d-6af0eafa5058\",\"wording\":\"on 01/08/2009 at the County public house, unlawfully and maliciously wounded, John Smith\",\"count\":1,\"title\":\"Wound / inflict grievous bodily harm without intent\",\"legislation\":\"Contrary to section 20 of the Offences Against the Person Act 1861.\",\"plea\":{\"pleaId\":\"0161a828-cfd1-4608-8616-d92870baba3d\",\"pleaDate\":\"2016-06-08\"},\"verdict\":{\"verdictId\":\"0161a828-cfd1-4608-8616-d92870bada3d\",\"value\":{\"id\":\"0161a828-cfd1-4608-8616-d92870bada3d\",\"category\":\"GUILTY\",\"code\":\"A1\",\"description\":\"Guilty By Jury On Judges Direction\"},\"verdictDate\":\"2018-02-21\",\"numberOfSplitJurors\":2,\"numberOfJurors\":10,\"unanimous\":false}}]},{\"defendantId\":\"3739b4e3-1f81-4d12-a99d-ad27ae672566\",\"personId\":\"98583be4-8d4a-4552-9252-ceccd61d32db\",\"firstName\":\"William Nelson\",\"lastName\":\"Joy\",\"homeTelephone\":\"02070101010\",\"mobile\":\"07422263910\",\"fax\":\"021111111\",\"email\":\"william-nelson.joy@acme.me\",\"address\":{\"formatedAddress\":\"222 Furze Road Exeter Lorem Ipsum Solor Porro Quisquam CR0 1XG\",\"address1\":\"222 Furze Road Exeter\",\"address2\":\"Lorem Ipsum\",\"address3\":\"Solor\",\"address4\":\"Porro Quisquam\",\"postCode\":\"CR0 1XG\"},\"dateOfBirth\":\"1954-11-08\",\"offences\":[{\"id\":\"4b1318e4-1517-4e4f-a89d-6af0eafa5058\",\"wording\":\"on 01/08/2009 at the County public house, unlawfully and maliciously wounded, John Smith\",\"count\":1,\"title\":\"Wound / inflict grievous bodily harm without intent\",\"legislation\":\"Contrary to section 20 of the Offences Against the Person Act 1861.\",\"plea\":{\"pleaId\":\"0161a828-cfd1-4608-8616-d92870baba3d\",\"pleaDate\":\"2016-06-08\"},\"verdict\":{\"verdictId\":\"0161a828-cfd1-4608-8616-d92870bada3d\",\"value\":{\"id\":\"0161a828-cfd1-4608-8616-d92870bada3d\",\"category\":\"GUILTY\",\"code\":\"A1\",\"description\":\"Guilty By Jury On Judges Direction\"},\"verdictDate\":\"2018-02-21\",\"numberOfSplitJurors\":2,\"numberOfJurors\":10,\"unanimous\":false}}]}]}]}";

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
        final LocalDate startDate = HearingTestUtils.START_DATE_1.toLocalDate();
        final List<Ahearing> hearingList = HearingTestUtils.buildHearingList();

        // 2. setting the repository mock return
        //-----------------------------------------------------------------------
        when(ahearingRepository.findByStartDateTime(startDate.atStartOfDay())).thenReturn(hearingList);

        // 3. invoking the service with the given input mock value
        //-----------------------------------------------------------------------
        final HearingListResponse response = caseHearingService.getHearingByStartDateV2(startDate);

        // 4. performing the assertions
        //-----------------------------------------------------------------------
        final String json = new ObjectMapper().writeValueAsString(response);
        assertEquals(EXPECTED_HEARING_LIST_RESPONSE, json);
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

    @Ignore
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
        final String json = new ObjectMapper().writeValueAsString(response);
        System.out.println(json);
        assertEquals(EXPECTED_HEARING_DETAILS_RESPONSE, json);
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
        final Set<HearingDetailsResponse.Defendant> defendants = response.getCases().stream().flatMap(c -> c.getDefendants().stream()).collect(Collectors.toSet());
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
                    assertEquals(format(address.getAddress1(),address.getAddress2(), address.getAddress3(), address.getAddress4(), address.getPostCode()), responseObject.getAddress().getformattedAddress());
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
