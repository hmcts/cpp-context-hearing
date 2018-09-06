package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.StringUtils.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingTypeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseIdentifierJPAMapper;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.nowresponse.NowsResponse;
import uk.gov.moj.cpp.hearing.repository.DocumentRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper;

    @Mock
    private HearingTypeJPAMapper hearingTypeJPAMapper;

    @Mock
    private HearingDayJPAMapper hearingDayJPAMapper;

    @Mock
    private NowsRepository nowsRepository;

    @Mock
    private NowsMaterialRepository nowsMaterialRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @InjectMocks
    private HearingService caseHearingService;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldNotFindHearingListWhenStartDateAndEndDateAreBeforeSittingDate() {
        LocalDate sittingDate = HearingTestUtils.START_DATE_1.toLocalDate(); //2018-02-22T10:30:00
        final List<Hearing> hearingList = HearingTestUtils.buildHearingList();
        when(hearingRepository.findByFilters(sittingDate, hearingList.get(0).getCourtCentre().getId(), hearingList.get(0).getCourtCentre().getRoomId())).thenReturn(hearingList);

        String startTime = "09:15";
        String endTime = "10:29";

        final HearingListResponse response = caseHearingService.getHearingByDateV2(sittingDate, startTime, endTime, hearingList.get(0).getCourtCentre().getId(), hearingList.get(0).getCourtCentre().getRoomId());
        assertEquals(0, response.getHearings().size());
    }

    @Test
    public void shouldNotFindHearingListWhenStartDateAndEndDateAreAfterSittingDate() {
        LocalDate sittingDate = HearingTestUtils.START_DATE_1.toLocalDate(); //2018-02-22T10:30:00
        final List<Hearing> hearingList = HearingTestUtils.buildHearingList();
        when(hearingRepository.findByFilters(sittingDate, hearingList.get(0).getCourtCentre().getId(), hearingList.get(0).getCourtCentre().getRoomId())).thenReturn(hearingList);

        String startTime = "10:31";
        String endTime = "11:30";

        final HearingListResponse response = caseHearingService.getHearingByDateV2(sittingDate, startTime, endTime, hearingList.get(0).getCourtCentre().getId(), hearingList.get(0).getCourtCentre().getRoomId());
        assertEquals(0, response.getHearings().size());
    }

    @Test
    public void shouldFindHearingListWhenStartDateIsBeforeAndEndDateIsAfterSittingDate() {

        uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier prosecutionCaseIdentifier = uk.gov.justice.json.schemas.core.ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withCaseURN("8C720B32E45B")
                .withProsecutionAuthorityCode("AUTH CODE")
                .withProsecutionAuthorityId(UUID.fromString("1dbab0cf-3822-46ff-b3ea-ddcf99e71ab9"))
                .withProsecutionAuthorityReference("AUTH REF")
                .build();

        uk.gov.justice.json.schemas.core.HearingType hearingType = uk.gov.justice.json.schemas.core.HearingType.hearingType()
                .withId(UUID.fromString("019556b2-a25e-4ea7-b3f1-8c89d14b02e0"))
                .withDescription("TRIAL")
                .build();

        uk.gov.justice.json.schemas.core.HearingDay hearingDay = uk.gov.justice.json.schemas.core.HearingDay.hearingDay()
                .withSittingDay(HearingTestUtils.START_DATE_1)
                .withListedDurationMinutes(2)
                .withListingSequence(5)
                .build();

        LocalDate startDateStartOfDay = HearingTestUtils.START_DATE_1.toLocalDate();
        final List<Hearing> hearingList = HearingTestUtils.buildHearingList();

        when(hearingRepository.findByFilters(startDateStartOfDay, hearingList.get(0).getCourtCentre().getId(), hearingList.get(0).getCourtCentre().getRoomId())).thenReturn(hearingList);
        when(prosecutionCaseIdentifierJPAMapper.fromJPA(Mockito.any(ProsecutionCaseIdentifier.class))).thenReturn(prosecutionCaseIdentifier);
        when(hearingTypeJPAMapper.fromJPA(Mockito.any(HearingType.class))).thenReturn(hearingType);
        when(hearingDayJPAMapper.fromJPA(Mockito.any(HearingDay.class))).thenReturn(hearingDay);

        final HearingListResponse response = caseHearingService.getHearingByDateV2(HearingTestUtils.START_DATE_1.toLocalDate(), "10:15", "14:30", hearingList.get(0).getCourtCentre().getId(), hearingList.get(0).getCourtCentre().getRoomId());

        assertEquals(hearingList.get(0).getId(), response.getHearings().get(0).getId());
        assertEquals(hearingList.get(0).getHearingType().getId(), response.getHearings().get(0).getType().getId());
        assertEquals(hearingList.get(0).getHearingType().getDescription(), response.getHearings().get(0).getType().getDescription());
        assertEquals(hearingList.get(0).getJurisdictionType(), response.getHearings().get(0).getJurisdictionType());
        assertEquals(hearingList.get(0).getReportingRestrictionReason(), response.getHearings().get(0).getReportingRestrictionReason());
        assertEquals(hearingList.get(0).getHearingLanguage().name(), response.getHearings().get(0).getHearingLanguage());
        assertEquals(hearingList.get(0).getHearingDays().get(0).getSittingDay(), hearingDay.getSittingDay());
        assertEquals(hearingList.get(0).getHearingDays().get(0).getListedDurationMinutes(), hearingDay.getListedDurationMinutes());
        assertEquals(hearingList.get(0).getHearingDays().get(0).getListingSequence(), hearingDay.getListingSequence());
        assertThat(response.getHearings().get(0).getProsecutionCases().get(0).getId(), is(hearingList.get(0).getProsecutionCases().get(0).getId().getId()));
        assertThat(response.getHearings().get(0).getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN(), is(hearingList.get(0).getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN()));
        assertThat(response.getHearings().get(0).getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityCode(), is(hearingList.get(0).getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityCode()));
        assertThat(response.getHearings().get(0).getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId(), is(hearingList.get(0).getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityId()));
        assertThat(response.getHearings().get(0).getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference(), is(hearingList.get(0).getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference()));
        assertEquals(
                hearingList.get(0).getProsecutionCases().get(0).getDefendants().stream().map(d -> d.getPersonDefendant().getPersonDetails().getFirstName() + " " + d.getPersonDefendant().getPersonDetails().getMiddleName() + " " + d.getPersonDefendant().getPersonDetails().getLastName())
                        .collect(Collectors.toList()),
                response.getHearings().get(0).getProsecutionCases().get(0).getDefendants().stream()
                        .map(d -> d.getName()).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindHearingDetailsById() throws Exception {

        Hearing entity = mock(Hearing.class);

        uk.gov.justice.json.schemas.core.Hearing pojo = mock(uk.gov.justice.json.schemas.core.Hearing.class);

        UUID hearingId = randomUUID();

        when(hearingRepository.findBy(hearingId)).thenReturn(entity);

        when(hearingJPAMapper.fromJPA(entity)).thenReturn(pojo);

        HearingDetailsResponse response = caseHearingService.getHearingById(hearingId);

        assertThat(response, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, is(pojo))
        );

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
        nowsMaterial.setStatus("generated");
        nowsMaterial.setUserGroups(asList("LO", "GA"));
        nowsMaterial.setLanguage(language);
        nows.getMaterial().add(nowsMaterial);

        final NowsResult nowsResult = new NowsResult();
        nowsResult.setSequence(1);
        nowsResult.setSharedResultId(sharedResultId);
        nowsResult.setNowsMaterial(nowsMaterial);
        nowsMaterial.getNowResult().add(nowsResult);

        final List<Nows> nowsList = new ArrayList<>();
        nowsList.add(nows);
        when(nowsRepository.findByHearingId(hearingId)).thenReturn(nowsList);

        final NowsResponse response = caseHearingService.getNows(hearingId);
        assertThat(response.getNows().get(0).getId(), is(id.toString()));
        assertThat(response.getNows().get(0).getDefendantId(), is(defendantId.toString()));
        assertThat(response.getNows().get(0).getNowsTypeId(), is(nowsTypeId.toString()));
        assertThat(response.getNows().get(0).getMaterial().get(0).getId(), is(nowsMaterial.getId().toString()));
        assertThat(response.getNows().get(0).getMaterial().get(0).getStatus(), is("generated"));
        assertThat(response.getNows().get(0).getMaterial().get(0).getLanguage(), is(language));
        assertThat(response.getNows().get(0).getMaterial().get(0).getNowResult().get(0).getSharedResultId(), is(sharedResultId.toString()));
        assertThat(response.getNows().get(0).getMaterial().get(0).getNowResult().get(0).getSequence(), is(1));

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
        nowsMaterial.setStatus("generated");
        nowsMaterial.setUserGroups(asList("Lx", "GA"));
        nowsMaterial.setLanguage(language);
        nows.getMaterial().add(nowsMaterial);


        when(nowsMaterialRepository.findBy(nowMaterialId)).thenReturn(nowsMaterial);

        final JsonObject response = caseHearingService.getNowsRepository(nowMaterialId.toString());
        assertThat(response.getJsonArray("allowedUserGroups").getValuesAs(JsonString.class).stream().map(jsonString -> jsonString.getString()).collect(Collectors.toList()), is(nowsMaterial.getUserGroups()));
    }

    @Test
    public void shouldNotFindUserGroupsByMaterialId() throws Exception {
        final UUID nowMaterialId = randomUUID();
        when(nowsMaterialRepository.findBy(nowMaterialId)).thenReturn(null);
        final JsonObject response = caseHearingService.getNowsRepository(nowMaterialId.toString());
        assertThat(response.getJsonArray("allowedUserGroups").size(), is(0));
    }

    @Test
    public void shouldFindSubscriptionByNowTypeId() {

        final String referenceDate = "15012018";

        final Document document = buildDocument();

        final String nowTypeId = document.getSubscriptions().get(0).getNowTypeIds().get(0).toString();

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(document));

        final JsonObject response = caseHearingService.getSubscriptions(referenceDate, nowTypeId);

        assertThat(response.getJsonArray("subscriptions").size(), is(1));
    }

    @Test
    public void shouldReturnEmptyWhenReferenceDateIsInvalid() {

        final String referenceDate = "15132018";

        final Document document = buildDocument();

        final String nowTypeId = document.getSubscriptions().get(0).getNowTypeIds().get(0).toString();

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(document));

        final JsonObject response = caseHearingService.getSubscriptions(referenceDate, nowTypeId);

        System.out.println(response.toString());

        assertThat(response.toString(), is("{}"));
    }

    @Test
    public void shouldReturnEmptyResponseWhenNowTypeIdNotFound() {

        final String referenceDate = "15012018";

        final Document document = buildDocument();

        final String nowTypeId = randomUUID().toString();

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(document));

        final JsonObject response = caseHearingService.getSubscriptions(referenceDate, nowTypeId);

        System.out.println(response.toString());

        assertThat(response.toString(), is("{\"subscriptions\":[]}"));
    }


    private Document buildDocument() {

        final Document document = new Document();
        document.setStartDate(LocalDate.of(2018, Month.JANUARY, 1));
        document.setId(randomUUID());
        document.setSubscriptions(asList(buildSubscription(), buildSubscription()));

        return document;
    }

    private uk.gov.moj.cpp.hearing.persist.entity.not.Subscription buildSubscription() {

        uk.gov.moj.cpp.hearing.persist.entity.not.Subscription subscription = new uk.gov.moj.cpp.hearing.persist.entity.not.Subscription();
        subscription.setId(randomUUID());
        subscription.setChannel(STRING.next());
        subscription.setDestination(STRING.next());

        final Map<String, String> properties = new HashMap<>();
        properties.put(STRING.next(), STRING.next());
        properties.put(STRING.next(), STRING.next());
        properties.put(STRING.next(), STRING.next());
        subscription.setChannelProperties(properties);

        subscription.setUserGroups(asList(STRING.next(), STRING.next()));
        subscription.setNowTypeIds(asList(randomUUID(), randomUUID()));
        subscription.setCourtCentreIds(asList(randomUUID(), randomUUID()));

        return subscription;
    }

    private static String format(final String... vals) {
        return join(vals, ' ').trim();
    }
}
