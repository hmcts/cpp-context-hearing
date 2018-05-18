package uk.gov.moj.cpp.hearing.query.view.service;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Address;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Judge;
import uk.gov.moj.cpp.hearing.persist.entity.ha.LegalCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterialStatus;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.DefenceCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.Defendant;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingResponse.ProsecutionCounsel;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.persistence.NoResultException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private NowsRepository nowsRepository;

    @Mock
    private NowsMaterialRepository nowsMaterialRepository;

    @InjectMocks
    private HearingService caseHearingService;

    @Test
    public void shouldFindHearingListByStartDate() throws Exception {
        ZonedDateTime startDateStartOfDay = HearingTestUtils.START_DATE_1.toLocalDate().atStartOfDay(ZoneOffset.systemDefault());
        final List<Hearing> hearingList = HearingTestUtils.buildHearingList();

        when(hearingRepository.findByDate(startDateStartOfDay)).thenReturn(hearingList);

        final HearingListResponse response = caseHearingService.getHearingByDateV2(HearingTestUtils.START_DATE_1.toLocalDate());

        assertEquals(hearingList.get(0).getId().toString(), response.getHearings().get(0).getHearingId());
        assertEquals(hearingList.get(0).getHearingType(), response.getHearings().get(0).getHearingType());
        assertThat(response.getHearings().get(0).getCases().get(0).getId(), is(hearingList.get(0).getDefendants().get(0).getOffences().get(0).getLegalCase().getId()));
        assertThat(response.getHearings().get(0).getCases().get(0).getUrn(), is(hearingList.get(0).getDefendants().get(0).getOffences().get(0).getLegalCase().getCaseUrn()));

        assertEquals(
                hearingList.get(0).getDefendants().stream().map(d -> d.getFirstName() + " " + d.getLastName())
                        .collect(Collectors.toList()),
                response.getHearings().get(0).getDefendants().stream()
                        .map(d -> d.getFirstName() + " " + d.getLastName()).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindHearingDetailsById() throws Exception {
        final Hearing hearing = HearingTestUtils.buildHearingList().get(0);

        when(hearingRepository.findById(HearingTestUtils.HEARING_ID_1)).thenReturn(hearing);

        final HearingDetailsResponse response = caseHearingService.getHearingByIdV2(HearingTestUtils.HEARING_ID_1);

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

        final Set<LegalCase> legalCases = hearing.getDefendants().stream().flatMap(d -> d.getOffences().stream().map(o -> o.getLegalCase())).collect(Collectors.toSet());
        assertEquals(legalCases.size(), response.getCases().size());

        legalCases.stream().forEach(entityBean -> {

            response.getCases().forEach(responseObject -> {

                if (entityBean.getId().toString().equals(responseObject.getCaseId())) {
                    assertEquals(entityBean.getCaseUrn(), responseObject.getCaseUrn());
                }
            });

        });

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
                    assertEquals(format(address.getAddress1(), address.getAddress2(), address.getAddress3(), address.getAddress4(), address.getPostCode()), responseObject.getAddress().getFormattedAddress());
                }

            });

        });
    }


    @Test
    public void shouldFindNowsByHearingId() throws Exception {
        final UUID hearingId = randomUUID();
        final UUID id = randomUUID();
        final UUID defendantId = randomUUID();

        final UUID nowsTypeId = randomUUID();
        final UUID nowMaterialId = randomUUID();
        final UUID sharedResultId = randomUUID();
        final String language = "wales";

        final Nows nows = new Nows();
        nows.setId(id);
        nows.setDefendantId(defendantId);
        nows.setHearingId(hearingId);
        nows.setNowsTypeId(nowsTypeId);

        final NowsMaterial nowsMaterial = new NowsMaterial();
        nowsMaterial.setId(nowMaterialId);
        nowsMaterial.setNows(nows);
        nowsMaterial.setStatus(NowsMaterialStatus.GENERATED);
        nowsMaterial.setUserGroups(Arrays.asList("LO", "GA"));
        nowsMaterial.setLanguage(language);
        nows.getMaterial().add(nowsMaterial);

        final NowsResult nowsResult = new NowsResult();
        nowsResult.setSequence(1);
        nowsResult.setSharedResultId(sharedResultId);
        nowsResult.setNows(nows);
        nows.getNowResult().add(nowsResult);

        final List<Nows> nowsList = new ArrayList<>();
        nowsList.add(nows);
        when(nowsRepository.findByHearingId(hearingId)).thenReturn(nowsList);

        final NowsResponse response = caseHearingService.getNows(hearingId);
        assertThat(response.getNows().get(0).getId(), is(id.toString()));
        assertThat(response.getNows().get(0).getDefendantId(), is(defendantId.toString()));
        assertThat(response.getNows().get(0).getNowsTypeId(), is(nowsTypeId.toString()));
        assertThat(response.getNows().get(0).getMaterial().get(0).getId(), is(nowsMaterial.getId().toString()));
        assertThat(response.getNows().get(0).getMaterial().get(0).getStatus(), is(NowsMaterialStatus.GENERATED.getDescription()));
        assertThat(response.getNows().get(0).getMaterial().get(0).getLanguage(), is(language));
        assertThat(response.getNows().get(0).getNowResult().get(0).getSharedResultId(), is(sharedResultId.toString()));
        assertThat(response.getNows().get(0).getNowResult().get(0).getSequence(), is(1));

    }



    @Test
    public void shouldFindUserGroupsByMaterialId() throws Exception {
        final UUID hearingId = randomUUID();
        final UUID id = randomUUID();
        final UUID defendantId = randomUUID();

        final UUID nowsTypeId = randomUUID();
        final UUID nowMaterialId = randomUUID();
        final String language = "wales";

        final Nows nows = new Nows();
        nows.setId(id);
        nows.setDefendantId(defendantId);
        nows.setHearingId(hearingId);
        nows.setNowsTypeId(nowsTypeId);

        final NowsMaterial nowsMaterial = new NowsMaterial();
        nowsMaterial.setId(nowMaterialId);
        nowsMaterial.setNows(nows);
        nowsMaterial.setStatus(NowsMaterialStatus.GENERATED);
        nowsMaterial.setUserGroups(Arrays.asList("Lx", "GA"));
        nowsMaterial.setLanguage(language);
        nows.getMaterial().add(nowsMaterial);


        when(nowsMaterialRepository.findBy(nowMaterialId)).thenReturn(nowsMaterial);

        final JsonObject response = caseHearingService.getNowsRepository(nowMaterialId.toString());
        assertThat(response.getJsonArray("allowedUserGroups").getValuesAs(JsonString.class).stream().map(jsonString -> jsonString.getString() ).collect(Collectors.toList()), is(nowsMaterial.getUserGroups()));
    }

    @Test
    public void shouldNotFindUserGroupsByMaterialId() throws Exception {
        final UUID nowMaterialId = randomUUID();
        when(nowsMaterialRepository.findBy(nowMaterialId)).thenReturn(null);
        final JsonObject response = caseHearingService.getNowsRepository(nowMaterialId.toString());
        assertThat(response.getJsonArray("allowedUserGroups").size(), is(0));
    }

    private static String format(final String... vals) {
        return join(vals, ' ').trim();
    }
}
