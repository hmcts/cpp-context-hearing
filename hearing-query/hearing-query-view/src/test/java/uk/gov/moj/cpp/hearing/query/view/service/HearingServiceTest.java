package uk.gov.moj.cpp.hearing.query.view.service;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.DefendantDetail;
import uk.gov.moj.cpp.hearing.domain.DefendantInfoQueryResult;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingTypeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseIdentifierJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingEvent;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.helper.TimelineHearingSummaryHelper;
import uk.gov.moj.cpp.hearing.query.view.response.Timeline;
import uk.gov.moj.cpp.hearing.query.view.response.TimelineHearingSummary;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTarget;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ApplicationTargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus;
import uk.gov.moj.cpp.hearing.repository.DocumentRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventDefinitionRepository;
import uk.gov.moj.cpp.hearing.repository.HearingEventPojo;
import uk.gov.moj.cpp.hearing.repository.HearingEventRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

import javax.json.JsonObject;
import javax.json.JsonString;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.math.BigInteger.valueOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.END_DATE_1;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.START_DATE_1;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.buildDefendant1;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.buildDefendant2;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.buildHearing;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.buildHearing1;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.buildHearingAndHearingDays;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.buildLegalCase1;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.buildRandomDefendant;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.buildRandomDefendantLegalOrganisation;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.helper;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CaseDetail.caseDetail;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Cases.cases;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Court.court;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtRoom.courtRoom;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CourtSite.courtSite;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.CurrentCourtStatus.currentCourtStatus;
import static uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit.Defendant.defendant;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.targetTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Mock
    private  List<UUID> prosecutionCasesIdsWithAccess;

    @Mock
    private FilterHearingsBasedOnPermissions filterHearingsBasedOnPermissions;

    @Mock
    private uk.gov.justice.core.courts.HearingEvent hearingEvent;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingEventRepository hearingEventRepository;

    @Mock
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;

    @Mock
    private ProsecutionCaseIdentifierJPAMapper prosecutionCaseIdentifierJPAMapper;

    @Mock
    private HearingTypeJPAMapper hearingTypeJPAMapper;

    @Mock
    private HearingDayJPAMapper hearingDayJPAMapper;

    @Mock
    private TargetJPAMapper targetJPAMapper;

    @Mock
    private NowsRepository nowsRepository;

    @Mock
    private NowsMaterialRepository nowsMaterialRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @Mock
    private GetHearingsTransformer getHearingsTransformer;

    @Mock
    private HearingListXhibitResponseTransformer hearingListXhibitResponseTransformer;

    @InjectMocks
    private HearingService hearingService;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private TimelineHearingSummaryHelper timelineHearingSummaryHelperMock;

    @Before
    public void setup() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldNotFindHearingListWhenStartDateAndEndDateAreBeforeSittingDate() {
        final LocalDate sittingDate = START_DATE_1.toLocalDate(); //2018-02-22T10:30:00
        final Hearing hearing = HearingTestUtils.buildHearing();
        final List<Hearing> hearings = asList(hearing);
        when(hearingRepository.findByFilters(sittingDate, hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId())).thenReturn(hearings);

        final String startTime = "09:15";
        final String endTime = "10:29";

        when(filterHearingsBasedOnPermissions.filterHearings(hearings, prosecutionCasesIdsWithAccess)).thenReturn(hearings);

        final GetHearings response = hearingService.getHearings(sittingDate, startTime, endTime, hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), prosecutionCasesIdsWithAccess, false);
        assertThat(response.getHearingSummaries(), is(emptyCollectionOf(HearingSummaries.class)));
    }

    @Test
    public void shouldNotFindHearingListWhenStartDateAndEndDateAreAfterSittingDate() {
        final LocalDate sittingDate = START_DATE_1.toLocalDate(); //2018-02-22T10:30:00
        final Hearing hearing = HearingTestUtils.buildHearing();
        final List<Hearing> hearings = asList(hearing);
        when(hearingRepository.findByFilters(sittingDate, hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId())).thenReturn(hearings);

        final String startTime = "10:31";
        final String endTime = "11:30";

        when(filterHearingsBasedOnPermissions.filterHearings(hearings, prosecutionCasesIdsWithAccess)).thenReturn(hearings);

        final GetHearings response = hearingService.getHearings(sittingDate, startTime, endTime, hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), prosecutionCasesIdsWithAccess, false);
        assertThat(response.getHearingSummaries(), is(emptyCollectionOf(HearingSummaries.class)));
    }

    @Test
    public void shouldNotFindHearingListWhenHearingIsEnded() {
        /*
         start time is :10:30
         hearing duration is 2 min
         so if query at 10:31 it should return hearing
         */
        final uk.gov.justice.core.courts.ProsecutionCaseIdentifier prosecutionCaseIdentifier = uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withCaseURN("8C720B32E45B")
                .withProsecutionAuthorityCode("AUTH CODE")
                .withProsecutionAuthorityId(UUID.fromString("1dbab0cf-3822-46ff-b3ea-ddcf99e71ab9"))
                .withProsecutionAuthorityReference("AUTH REF")
                .build();

        final uk.gov.justice.core.courts.HearingType hearingType = uk.gov.justice.core.courts.HearingType.hearingType()
                .withId(UUID.fromString("019556b2-a25e-4ea7-b3f1-8c89d14b02e0"))
                .withDescription("TRIAL")
                .build();

        final uk.gov.justice.core.courts.HearingDay hearingDay = uk.gov.justice.core.courts.HearingDay.hearingDay()
                .withSittingDay(START_DATE_1) //2018-02-22T10:30:00
                .withListedDurationMinutes(2)
                .withListingSequence(5)
                .build();

        final LocalDate startDateStartOfDay = START_DATE_1.toLocalDate();
        final HearingTestUtils.HearingHelper hearingHelper = helper(HearingTestUtils.buildHearing());
        final Hearing hearingEntity = hearingHelper.it();
        final uk.gov.justice.core.courts.Hearing hearingPojo = uk.gov.justice.core.courts.Hearing.hearing().withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase().build())).build();
        final UUID hearingSummaryId = randomUUID();
        final HearingSummaries.Builder hearingSummariesBuilder = HearingSummaries.hearingSummaries().withId(hearingSummaryId);
        final UUID hearingEventId = randomUUID();
        final HearingEvent hearingEvent = HearingEvent.hearingEvent()
                .setId(hearingEventId)
                .setHearingId(hearingEntity.getId())
                .setRecordedLabel("Hearing ended");


        final List<Hearing> hearings = asList(hearingEntity);
        when(hearingRepository.findHearings(startDateStartOfDay, hearingEntity.getCourtCentre().getId())).thenReturn(hearings);
        when(hearingEventRepository.findHearingEvents(hearingEntity.getId(), "Hearing ended")).thenReturn(asList(hearingEvent));
        when(hearingJPAMapper.fromJPA(hearingEntity)).thenReturn(hearingPojo);
        when(getHearingsTransformer.summary(hearingPojo)).thenReturn(hearingSummariesBuilder);
        when(filterHearingsBasedOnPermissions.filterHearings(hearings, prosecutionCasesIdsWithAccess)).thenReturn(hearings);

        final GetHearings response = hearingService.getHearings(START_DATE_1.toLocalDate(),
                "10:30", "14:30", hearingEntity.getCourtCentre().getId(), null, prosecutionCasesIdsWithAccess,false);

        assertThat(response.getHearingSummaries(), is(emptyCollectionOf(HearingSummaries.class)));
    }

    @Test
    public void shouldFindHearingListWhenStartDateIsBeforeAndEndDateIsAfterSittingDate() {

        final uk.gov.justice.core.courts.ProsecutionCaseIdentifier prosecutionCaseIdentifier = uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withCaseURN("8C720B32E45B")
                .withProsecutionAuthorityCode("AUTH CODE")
                .withProsecutionAuthorityId(UUID.fromString("1dbab0cf-3822-46ff-b3ea-ddcf99e71ab9"))
                .withProsecutionAuthorityReference("AUTH REF")
                .build();

        final uk.gov.justice.core.courts.HearingType hearingType = uk.gov.justice.core.courts.HearingType.hearingType()
                .withId(UUID.fromString("019556b2-a25e-4ea7-b3f1-8c89d14b02e0"))
                .withDescription("TRIAL")
                .build();

        final uk.gov.justice.core.courts.HearingDay hearingDay = uk.gov.justice.core.courts.HearingDay.hearingDay()
                .withSittingDay(START_DATE_1)
                .withListedDurationMinutes(2)
                .withListingSequence(5)
                .build();

        final LocalDate startDateStartOfDay = START_DATE_1.toLocalDate();
        final HearingTestUtils.HearingHelper hearingHelper = helper(HearingTestUtils.buildHearing());
        final Hearing hearingEntity = hearingHelper.it();
        final uk.gov.justice.core.courts.Hearing hearingPojo = uk.gov.justice.core.courts.Hearing.hearing().withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase().build())).build();
        final UUID hearingSummaryId = randomUUID();
        final HearingSummaries.Builder hearingSummariesBuilder = HearingSummaries.hearingSummaries().withId(hearingSummaryId);

        final List<Hearing> hearings = asList(hearingEntity);
        when(hearingRepository.findByFilters(startDateStartOfDay, hearingEntity.getCourtCentre().getId(), hearingEntity.getCourtCentre().getRoomId())).thenReturn(hearings);
        when(hearingJPAMapper.fromJPA(hearingEntity)).thenReturn(hearingPojo);
        when(getHearingsTransformer.summary(hearingPojo)).thenReturn(hearingSummariesBuilder);

        when(filterHearingsBasedOnPermissions.filterHearings(hearings, prosecutionCasesIdsWithAccess)).thenReturn(hearings);

        final GetHearings response = hearingService.getHearings(START_DATE_1.toLocalDate(),
                "10:15", "14:30", hearingEntity.getCourtCentre().getId(), hearingEntity.getCourtCentre().getRoomId(), prosecutionCasesIdsWithAccess, false);

        assertThat(response.getHearingSummaries().get(0).getId(), is(hearingSummaryId));
    }

    @Test
    public void shouldFindHearingListWithSingleCourtRoom() {

        final LocalDate startDateStartOfDay = START_DATE_1.toLocalDate();
        final HearingTestUtils.HearingHelper hearingHelper = helper(HearingTestUtils.buildHearing());
        final Hearing hearingEntity = hearingHelper.it();

        when(hearingRepository.findByFilters(startDateStartOfDay, hearingEntity.getCourtCentre().getId(), Arrays.asList(hearingEntity.getCourtCentre().getRoomId()))).thenReturn(asList(hearingEntity));

        final DefendantInfoQueryResult response = hearingService.getHearingsByCourtRoomList(START_DATE_1.toLocalDate(), hearingEntity.getCourtCentre().getId(), Arrays.asList(hearingEntity.getCourtCentre().getRoomId()));

        assertThat(response, isBean(DefendantInfoQueryResult.class)
                .with(DefendantInfoQueryResult::getCourtRooms, allOf(
                        iterableWithSize(1),
                        hasItems(
                                isBean(uk.gov.moj.cpp.hearing.domain.CourtRoom.class)
                                        .withValue(uk.gov.moj.cpp.hearing.domain.CourtRoom::getCourtRoomName, hearingEntity.getCourtCentre().getRoomName())
                                        .with(uk.gov.moj.cpp.hearing.domain.CourtRoom::getDefendantDetails, hasItems(
                                                isBean(DefendantDetail.class)
                                                        .withValue(DefendantDetail::getDefendantId, hearingHelper.getFirstDefendant().getId().getId())
                                                        .withValue(DefendantDetail::getFirstName, hearingHelper.getFirstDefendant().getPersonDefendant().getPersonDetails().getFirstName())
                                        ))
                        )
                ))
        );

    }

    @Test
    public void shouldFindHearingListWithCourtRoomList() {

        final LocalDate hearingDate = START_DATE_1.toLocalDate();
        Defendant organizationDefendant = buildRandomDefendantLegalOrganisation();
        Defendant personDefendant = buildRandomDefendant(randomUUID());
        final HearingTestUtils.HearingHelper hearingHelper = helper(HearingTestUtils.buildHearingWithRandomDefendants(personDefendant, organizationDefendant));
        final Hearing hearingEntity = hearingHelper.it();

        final HearingTestUtils.HearingHelper hearingHelper2 = helper(HearingTestUtils.buildHearingWithRandomDefendants());
        final Hearing hearingEntity2 = hearingHelper2.it();

        when(hearingRepository.findByFilters(hearingDate, hearingEntity.getCourtCentre().getId(), Arrays.asList(hearingEntity.getCourtCentre().getRoomId())))
                .thenReturn(asList(hearingEntity, hearingEntity2));

        final DefendantInfoQueryResult response = hearingService.getHearingsByCourtRoomList(START_DATE_1.toLocalDate(), hearingEntity.getCourtCentre().getId(), Arrays.asList(hearingEntity.getCourtCentre().getRoomId()));

        assertTrue(response.getCourtRooms().size() > 0);


        assertThat(response, isBean(DefendantInfoQueryResult.class)
                .with(DefendantInfoQueryResult::getCourtRooms, allOf(
                        iterableWithSize(2),
                        hasItems(
                                isBean(uk.gov.moj.cpp.hearing.domain.CourtRoom.class)
                                        .withValue(uk.gov.moj.cpp.hearing.domain.CourtRoom::getCourtRoomName, hearingEntity.getCourtCentre().getRoomName())
                                        .with(uk.gov.moj.cpp.hearing.domain.CourtRoom::getDefendantDetails, hasItems(
                                                isBean(DefendantDetail.class)
                                                        .withValue(DefendantDetail::getDefendantId, personDefendant.getId().getId())
                                                        .withValue(DefendantDetail::getFirstName, personDefendant.getPersonDefendant().getPersonDetails().getFirstName()),
                                                isBean(DefendantDetail.class)
                                                        .withValue(DefendantDetail::getDefendantId, organizationDefendant.getId().getId())
                                                        .withValue(DefendantDetail::getLegalEntityOrganizationName, organizationDefendant.getLegalEntityOrganisation().getName())
                                        )),
                                isBean(uk.gov.moj.cpp.hearing.domain.CourtRoom.class)
                                        .withValue(uk.gov.moj.cpp.hearing.domain.CourtRoom::getCourtRoomName, hearingEntity2.getCourtCentre().getRoomName())
                                        .with(uk.gov.moj.cpp.hearing.domain.CourtRoom::getDefendantDetails, hasSize(2))
                        )
                ))
        );

    }

    @Test
    public void shouldFindHearingDetailsById() throws Exception {

        final Hearing entity = mock(Hearing.class);

        final uk.gov.justice.core.courts.Hearing pojo = mock(uk.gov.justice.core.courts.Hearing.class);

        final UUID hearingId = randomUUID();

        when(hearingRepository.findBy(hearingId)).thenReturn(entity);

        when(hearingJPAMapper.fromJPA(entity)).thenReturn(pojo);

        when(filterHearingsBasedOnPermissions.filterHearings(Arrays.asList(entity), prosecutionCasesIdsWithAccess)).thenReturn(Arrays.asList(entity));

        final UUID trialTypeId = randomUUID();
        final HearingDetailsResponse response = hearingService.getHearingDetailsResponseById(hearingId, buildCrackedIneffectiveVacatedTrialTypes(trialTypeId), prosecutionCasesIdsWithAccess, false);

        assertThat(response, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, is(pojo))
        );
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
        nowsMaterial.setUserGroups(asSet("Lx", "GA"));
        nowsMaterial.setLanguage(language);
        nows.getMaterial().add(nowsMaterial);


        when(nowsMaterialRepository.findBy(nowMaterialId)).thenReturn(nowsMaterial);

        final JsonObject response = hearingService.getNowsRepository(nowMaterialId.toString());
        assertThat(response.getJsonArray("allowedUserGroups").getValuesAs(JsonString.class).stream().map(JsonString::getString).collect(toList()), containsInAnyOrder(nowsMaterial.getUserGroups().toArray()));
    }

    @Test
    public void shouldNotFindUserGroupsByMaterialId() throws Exception {
        final UUID nowMaterialId = randomUUID();
        when(nowsMaterialRepository.findBy(nowMaterialId)).thenReturn(null);
        final JsonObject response = hearingService.getNowsRepository(nowMaterialId.toString());
        assertThat(response.getJsonArray("allowedUserGroups").size(), is(0));
    }

    @Test
    public void shouldFindSubscriptionByNowTypeId() {

        final String referenceDate = "15012018";

        final Document document = buildDocument();

        final String nowTypeId = document.getSubscriptions().get(0).getNowTypeIds().get(0).toString();

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(document));

        final JsonObject response = hearingService.getSubscriptions(referenceDate, nowTypeId);

        assertThat(response.getJsonArray("subscriptions").size(), is(1));
    }

    @Test
    public void shouldReturnEmptyWhenReferenceDateIsInvalid() {

        final String referenceDate = "15132018";

        final Document document = buildDocument();

        final String nowTypeId = document.getSubscriptions().get(0).getNowTypeIds().get(0).toString();

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(document));

        final JsonObject response = hearingService.getSubscriptions(referenceDate, nowTypeId);

        assertThat(response.toString(), is("{}"));
    }

    @Test
    public void shouldReturnEmptyResponseWhenNowTypeIdNotFound() {

        final String referenceDate = "15012018";

        final Document document = buildDocument();

        final String nowTypeId = randomUUID().toString();

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(document));

        final JsonObject response = hearingService.getSubscriptions(referenceDate, nowTypeId);

        assertThat(response.toString(), is("{\"subscriptions\":[]}"));
    }

    @Test
    public void shouldReturnEmptyResponseWhenTargetNotAdded() {

        final Hearing hearing = new Hearing();

        hearing.setId(randomUUID());
        hearing.setProsecutionCases(createProsecutionCases());

        when(hearingRepository.findBy(any())).thenReturn(hearing);

        when(targetJPAMapper.fromJPA(anySet(), anySet())).thenReturn(new ArrayList());

        final TargetListResponse targetListResponse = hearingService.getTargets(hearing.getId());

        assertThat(targetListResponse.getTargets().isEmpty(), is(true));
    }


    @Test
    public void shouldReturnResponseWhenTargetIsAdded() {

        final Hearing hearing = new Hearing();
        hearing.setProsecutionCases(createProsecutionCases());
        hearing.setId(randomUUID());
        hearing.setTargets(asSet(new Target()));

        final List<uk.gov.justice.core.courts.Target> targets = asList(
                targetTemplate(),
                targetTemplate());

        when(hearingRepository.findTargetsByHearingId(hearing.getId()))
                .thenReturn(Lists.newArrayList(hearing.getTargets()));
        when(hearingRepository.findProsecutionCasesByHearingId(hearing.getId()))
                .thenReturn(Lists.newArrayList(hearing.getProsecutionCases()));
        when(targetJPAMapper.fromJPA(anySet(), anySet())).thenReturn(targets);


        final TargetListResponse targetListResponse = hearingService.getTargets(hearing.getId());

        final uk.gov.justice.core.courts.Target targetIn = targets.get(0);

        final ResultLine resultLine = targetIn.getResultLines().get(0);

        final Prompt prompt = resultLine.getPrompts().get(0);

        verify(hearingRepository)
                .findTargetsByHearingId(hearing.getId());
        verify(hearingRepository)
                .findProsecutionCasesByHearingId(hearing.getId());

        assertThat(targetListResponse, isBean(TargetListResponse.class)
                .with(t -> t.getTargets().isEmpty(), is(false))
                .with(TargetListResponse::getTargets, first(isBean(uk.gov.justice.core.courts.Target.class)
                        .with(uk.gov.justice.core.courts.Target::getTargetId, is(targetIn.getTargetId()))
                        .with(uk.gov.justice.core.courts.Target::getDefendantId, is(targetIn.getDefendantId()))
                        .with(uk.gov.justice.core.courts.Target::getMasterDefendantId, is(targetIn.getMasterDefendantId()))
                        .with(uk.gov.justice.core.courts.Target::getDraftResult, is(targetIn.getDraftResult()))
                        .with(uk.gov.justice.core.courts.Target::getHearingId, is(targetIn.getHearingId()))
                        .with(uk.gov.justice.core.courts.Target::getOffenceId, is(targetIn.getOffenceId()))
                        .with(t -> t.getResultLines().size(), is(targetIn.getResultLines().size()))
                        .with(uk.gov.justice.core.courts.Target::getResultLines, first(isBean(ResultLine.class)
                                .with(ResultLine::getIsModified, is(resultLine.getIsModified()))
                                .with(ResultLine::getIsComplete, is(resultLine.getIsComplete()))
                                .with(ResultLine::getLevel, is(resultLine.getLevel()))
                                .with(ResultLine::getResultLineId, is(resultLine.getResultLineId()))
                                .with(ResultLine::getResultLabel, is(resultLine.getResultLabel()))
                                .with(ResultLine::getSharedDate, is(resultLine.getSharedDate()))
                                .with(ResultLine::getOrderedDate, is(resultLine.getOrderedDate()))
                                .with(ResultLine::getDelegatedPowers, isBean(DelegatedPowers.class)
                                        .with(DelegatedPowers::getUserId, is(resultLine.getDelegatedPowers().getUserId()))
                                        .with(DelegatedPowers::getFirstName, is(resultLine.getDelegatedPowers().getFirstName()))
                                        .with(DelegatedPowers::getLastName, is(resultLine.getDelegatedPowers().getLastName())))
                                .with(r -> r.getPrompts().size(), is(resultLine.getPrompts().size()))
                                .with(ResultLine::getPrompts, first(isBean(Prompt.class)
                                        .with(Prompt::getId, is(prompt.getId()))
                                        .with(Prompt::getLabel, is(prompt.getLabel()))
                                        .with(Prompt::getFixedListCode, is(prompt.getFixedListCode()))
                                        .with(Prompt::getValue, is(prompt.getValue()))
                                        .with(Prompt::getWelshValue, is(prompt.getWelshValue()))
                                )))))));

    }

    @Test
    public void shouldReturnResponseWhenApplicationTargetIsAdded() {
        final ApplicationDraftResult applicationDraftResult = ApplicationDraftResult.applicationDraftResult()
                .setApplicationId(randomUUID()).setDraftResult("result").setId(randomUUID());

        when(hearingRepository.findApplicationDraftResultsByHearingId(any())).thenReturn(asList(applicationDraftResult));

        final ApplicationTargetListResponse targetListResponse = hearingService.getApplicationTargets(randomUUID());

        assertThat(targetListResponse, isBean(ApplicationTargetListResponse.class)
                .with(t -> t.getTargets().isEmpty(), is(false))
                .with(ApplicationTargetListResponse::getTargets, first(isBean(ApplicationTarget.class)
                        .with(ApplicationTarget::getTargetId, is(applicationDraftResult.getId()))
                        .with(ApplicationTarget::getApplicationId, is(applicationDraftResult.getApplicationId()))
                        .with(ApplicationTarget::getDraftResult, is(applicationDraftResult.getDraftResult())))));

    }

    @Test
    public void shouldFindHearingListWhenStartDateAndEndDateAreAfterSittingDateWithSortedListingSequence() {

        final uk.gov.justice.core.courts.ProsecutionCaseIdentifier prosecutionCaseIdentifier = uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withCaseURN("8C720B32E45B")
                .withProsecutionAuthorityCode("AUTH CODE")
                .withProsecutionAuthorityId(UUID.fromString("1dbab0cf-3822-46ff-b3ea-ddcf99e71ab9"))
                .withProsecutionAuthorityReference("AUTH REF")
                .build();

        final uk.gov.justice.core.courts.HearingType hearingType = uk.gov.justice.core.courts.HearingType.hearingType()
                .withId(UUID.fromString("019556b2-a25e-4ea7-b3f1-8c89d14b02e0"))
                .withDescription("TRIAL")
                .build();

        final uk.gov.justice.core.courts.HearingDay hearingDay = uk.gov.justice.core.courts.HearingDay.hearingDay()
                .withSittingDay(START_DATE_1)
                .withListedDurationMinutes(2)
                .withListingSequence(5)
                .build();

        final LocalDate startDateStartOfDay = LocalDate.of(2019, 7, 4);

        //  final HearingTestUtils.HearingHelper hearingHelper = helper(HearingTestUtils.buildHearing());
        final Hearing hearingEntity = HearingTestUtils.buildHearing();
        final uk.gov.justice.core.courts.Hearing hearingPojo = uk.gov.justice.core.courts.Hearing.hearing().withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase().build())).build();
        final UUID hearingSummaryId = randomUUID();
        final HearingSummaries.Builder hearingSummariesBuilder = HearingSummaries.hearingSummaries().withId(hearingSummaryId);

        final List<Hearing> hearings = buildHearingAndHearingDays();
        when(hearingRepository.findByFilters(startDateStartOfDay, hearingEntity.getCourtCentre().getId(),
                hearingEntity.getCourtCentre().getRoomId())).thenReturn(hearings);
        when(hearingJPAMapper.fromJPA(Mockito.any(Hearing.class))).thenReturn(hearingPojo);
        when(getHearingsTransformer.summary(hearingPojo)).thenReturn(hearingSummariesBuilder);

        when(filterHearingsBasedOnPermissions.filterHearings(hearings, prosecutionCasesIdsWithAccess)).thenReturn(hearings);

        final GetHearings response = hearingService.getHearings(startDateStartOfDay,
                "09:00", "17:00", hearingEntity.getCourtCentre().getId(), hearingEntity.getCourtCentre().getRoomId(), prosecutionCasesIdsWithAccess, false);
        assertTrue("response is empty", !response.getHearingSummaries().isEmpty());
        assertThat(response.getHearingSummaries().get(0).getId(), is(hearingSummaryId));
    }

    @Test
    public void shouldFindHearingListWhenJudicialUserIsMatched() {

        final LocalDate startDateStartOfDay = LocalDate.of(2019, 7, 4);

        final Hearing hearingEntity = HearingTestUtils.buildHearing();
        final uk.gov.justice.core.courts.Hearing hearingPojo = uk.gov.justice.core.courts.Hearing.hearing().withProsecutionCases(Collections.singletonList(ProsecutionCase.prosecutionCase().build())).build();
        final UUID hearingSummaryId = randomUUID();
        final HearingSummaries.Builder hearingSummariesBuilder = HearingSummaries.hearingSummaries().withId(hearingSummaryId);

        final List<Hearing> hearingList = new ArrayList<>();
        hearingList.add(hearingEntity);

        when(hearingRepository.findByUserFilters(startDateStartOfDay, hearingEntity.getJudicialRoles().stream().findFirst().get().getUserId())).thenReturn(hearingList);
        when(hearingJPAMapper.fromJPA(Mockito.any(Hearing.class))).thenReturn(hearingPojo);
        when(getHearingsTransformer.summaryForHearingsForToday(hearingPojo)).thenReturn(hearingSummariesBuilder);

        final GetHearings response = hearingService.getHearingsForToday(startDateStartOfDay,
                hearingEntity.getJudicialRoles().stream().findFirst().get().getUserId());

        assertTrue("response is empty", !response.getHearingSummaries().isEmpty());
        assertThat(response.getHearingSummaries().get(0).getId(), is(hearingSummaryId));
    }

    @Test
    public void shouldFilterHearingForDDJ() {
        final UUID hearingId1 = randomUUID();
        final UUID hearingId2 = randomUUID();
        final Defendant defendant1 = buildDefendant1(hearingId1);
        final Defendant defendant2 = buildDefendant2(hearingId2);
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCase1 = buildLegalCase1(hearingId1, asSet(defendant1));
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCase2 = buildLegalCase1(hearingId2, asSet(defendant2));
        final Hearing hearing1 = buildHearing1(hearingId1, START_DATE_1, END_DATE_1, asSet(prosecutionCase1, prosecutionCase2));
        final List<UUID> accessibleCasesId = Arrays.asList(prosecutionCase1.getId().getId());
        final Hearing hearing2 = buildHearing1(hearingId2, START_DATE_1, END_DATE_1, asSet(prosecutionCase1));

        final uk.gov.justice.core.courts.Hearing hearingPojo = uk.gov.justice.core.courts.Hearing.hearing().withProsecutionCases(singletonList(
                ProsecutionCase.prosecutionCase()
                        .withId(prosecutionCase1.getId().getId())
                        .withDefendants(asList(uk.gov.justice.core.courts.Defendant.defendant()
                                .withId(defendant1.getId().getId())
                                .build()))
                        .build())).build();

        when(hearingRepository.findBy(hearingId1)).thenReturn(hearing1);

        when(hearingJPAMapper.fromJPA(hearing2)).thenReturn(hearingPojo);

        when(filterHearingsBasedOnPermissions.filterHearings(Arrays.asList(hearing1), accessibleCasesId)).thenReturn(Arrays.asList(hearing2));

        final UUID trialTypeId = randomUUID();


        final HearingDetailsResponse response = hearingService.getHearingDetailsResponseById(hearingId1, buildCrackedIneffectiveVacatedTrialTypes(trialTypeId), accessibleCasesId, true);

        assertThat(response, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, is(hearingPojo))
        );


    }

    @Test
    public void shouldNotFindHearingListWhenJudicialUserIsNotMatched() {

        final LocalDate startDateStartOfDay = LocalDate.of(2019, 7, 4);

        final Hearing hearingEntity = HearingTestUtils.buildHearing();
        final uk.gov.justice.core.courts.Hearing hearingPojo = uk.gov.justice.core.courts.Hearing.hearing().withProsecutionCases(Collections.singletonList(ProsecutionCase.prosecutionCase().build())).build();
        final UUID hearingSummaryId = randomUUID();
        final HearingSummaries.Builder hearingSummariesBuilder = HearingSummaries.hearingSummaries().withId(hearingSummaryId);

        when(hearingRepository.findByUserFilters(startDateStartOfDay, hearingEntity.getJudicialRoles().stream().findFirst().get().getUserId())).thenReturn(buildHearingAndHearingDays());
        when(hearingJPAMapper.fromJPA(Mockito.any(Hearing.class))).thenReturn(hearingPojo);
        when(getHearingsTransformer.summary(hearingPojo)).thenReturn(hearingSummariesBuilder);

        final GetHearings response = hearingService.getHearingsForToday(startDateStartOfDay,
                randomUUID());
        assertNull("response is empty", response.getHearingSummaries());
    }

    @Test
    public void shouldFindHearingWithInEffectiveTrailType() {

        final Hearing entity = mock(Hearing.class);

        final uk.gov.justice.core.courts.Hearing pojo = mock(uk.gov.justice.core.courts.Hearing.class);

        final UUID hearingId = randomUUID();
        final UUID trialTypeId = randomUUID();
        entity.setTrialTypeId(trialTypeId);

        when(hearingRepository.findBy(hearingId)).thenReturn(entity);

        when(hearingJPAMapper.fromJPA(entity)).thenReturn(pojo);

        final HearingDetailsResponse response = hearingService.getHearingDetailsResponseById(hearingId, buildCrackedIneffectiveVacatedTrialTypes(trialTypeId), prosecutionCasesIdsWithAccess, false);

        assertThat(response, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, is(pojo))
        );
    }

    @Test
    public void shouldFindHearingWithVacateTrailType() {

        final Hearing entity = mock(Hearing.class);

        final uk.gov.justice.core.courts.Hearing pojo = mock(uk.gov.justice.core.courts.Hearing.class);

        final UUID hearingId = randomUUID();
        final UUID vacatedTrialReasonId = randomUUID();
        entity.setvacatedTrialReasonId(vacatedTrialReasonId);
        entity.setIsVacatedTrial(true);

        when(hearingRepository.findBy(hearingId)).thenReturn(entity);

        when(hearingJPAMapper.fromJPA(entity)).thenReturn(pojo);

        final HearingDetailsResponse response = hearingService.getHearingDetailsResponseById(hearingId, buildVacatedTrialTypes(vacatedTrialReasonId), prosecutionCasesIdsWithAccess, false);

        assertThat(response, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, is(pojo))
        );
    }

    @Test
    public void shouldFindHearingWithEffectiveTrailType() {

        final Hearing entity = mock(Hearing.class);

        final uk.gov.justice.core.courts.Hearing pojo = mock(uk.gov.justice.core.courts.Hearing.class);

        final UUID hearingId = randomUUID();
        entity.setIsEffectiveTrial(true);

        when(hearingRepository.findBy(hearingId)).thenReturn(entity);

        when(hearingJPAMapper.fromJPA(entity)).thenReturn(pojo);

        final UUID trialTypeId = randomUUID();
        final HearingDetailsResponse response = hearingService.getHearingDetailsResponseById(hearingId, buildCrackedIneffectiveVacatedTrialTypes(trialTypeId), prosecutionCasesIdsWithAccess, false);

        assertThat(response, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, is(pojo))
        );
    }

    @Test
    public void shouldFindHearingTimeLineByCaseId() {

        final Hearing entity = mock(Hearing.class);
        final List<Hearing> hearings = new ArrayList<>();
        hearings.add(entity);
        final HearingDay hearingDayMock = mock(HearingDay.class);
        final Set<HearingDay> hearingDays = new HashSet<>();
        hearingDays.add(hearingDayMock);
        final JsonObject allCourtRooms = mock(JsonObject.class);

        final UUID caseId = randomUUID();
        when(hearingRepository.findByCaseId(caseId)).thenReturn(hearings);
        when(entity.getHearingDays()).thenReturn(hearingDays);
        final TimelineHearingSummary hearingSummary = mock(TimelineHearingSummary.class);
        when(timelineHearingSummaryHelperMock.createTimeLineHearingSummary(any(), any(), any(), any())).thenReturn(hearingSummary);
        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypesMock = mock(CrackedIneffectiveVacatedTrialTypes.class);
        when(crackedIneffectiveVacatedTrialTypesMock.getCrackedIneffectiveVacatedTrialTypes()).thenReturn(emptyList());

        final UUID trialTypeId = randomUUID();
        final Timeline response = hearingService.getTimeLineByCaseId(caseId, buildCrackedIneffectiveVacatedTrialTypes(trialTypeId), allCourtRooms);

        assertThat(response, instanceOf(Timeline.class));
        assertThat(response.getHearingSummaries().get(0), is(hearingSummary));

    }

    @Test
    public void shouldFindHearingTimeLineByApplicationId() {

        final Hearing entity = mock(Hearing.class);
        entity.getCourtApplicationsJson();

        final List<Hearing> hearings = new ArrayList<>();
        hearings.add(entity);

        final HearingDay hearingDayMock = mock(HearingDay.class);
        final Set<HearingDay> hearingDays = new HashSet<>();
        hearingDays.add(hearingDayMock);

        final JsonObject allCourtRooms = mock(JsonObject.class);

        final UUID applicationId = randomUUID();
        when(hearingRepository.findAllHearingsByApplicationId(applicationId)).thenReturn(hearings);
        when(entity.getHearingDays()).thenReturn(hearingDays);

        final TimelineHearingSummary hearingSummary = mock(TimelineHearingSummary.class);
        when(timelineHearingSummaryHelperMock.createTimeLineHearingSummary(any(), any(), any(), any(), any())).thenReturn(hearingSummary);

        final CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypesMock = mock(CrackedIneffectiveVacatedTrialTypes.class);
        when(crackedIneffectiveVacatedTrialTypesMock.getCrackedIneffectiveVacatedTrialTypes()).thenReturn(emptyList());

        final UUID trialTypeId = randomUUID();
        final Timeline response = hearingService.getTimeLineByApplicationId(applicationId, buildCrackedIneffectiveVacatedTrialTypes(trialTypeId), allCourtRooms);

        assertThat(response, instanceOf(Timeline.class));
        assertThat(response.getHearingSummaries().get(0), is(hearingSummary));

    }

    @Test
    public void shouldReturnLatestHearingByCourtCentreIdsAndLatestModifiedTime() {
        final LocalDate now = LocalDate.now();
        final List<UUID> courtCentreIds = new ArrayList();
        courtCentreIds.add(randomUUID());

        final HearingEventPojo hearingEvent = new HearingEventPojo(randomUUID(), false, LocalDate.now(), ZonedDateTime.now(), randomUUID(), randomUUID(), randomUUID(), ZonedDateTime.now(), "");

        final List<HearingEventPojo> hearingEventList = asList(hearingEvent);
        final Hearing hearing = buildHearing();

        final uk.gov.justice.core.courts.Hearing hearinPojo = mock(uk.gov.justice.core.courts.Hearing.class);

        final CurrentCourtStatus expectedCurrentCourtStatus = currentCourtStatus().withPageName("hello").build();

        final Set<UUID> hearingEventRequiredDefinitionsIds = new HashSet();
        hearingEventRequiredDefinitionsIds.add(randomUUID());
        hearingEventRequiredDefinitionsIds.add(randomUUID());

        when(hearingEventRepository.findLatestHearingsForThatDay(courtCentreIds, now, hearingEventRequiredDefinitionsIds)).thenReturn(hearingEventList);
        when(hearingRepository.findBy(hearingEvent.getHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.fromJPA(hearing)).thenReturn(hearinPojo);
        when(hearingListXhibitResponseTransformer.transformFrom(any(HearingEventsToHearingMapper.class))).thenReturn(expectedCurrentCourtStatus);

        final Optional<CurrentCourtStatus> response = hearingService.getHearingsForWebPage(courtCentreIds, now, hearingEventRequiredDefinitionsIds);

        assertThat(response.get().getPageName(), is(expectedCurrentCourtStatus.getPageName()));
    }

    @Test
    public void shouldReturnHearingsByDate() {
        final LocalDate now = LocalDate.now();
        final List<UUID> courtCentreIds = new ArrayList();
        courtCentreIds.add(randomUUID());

        final HearingEventPojo hearingEventPojo = new HearingEventPojo(randomUUID(), false, LocalDate.now(), ZonedDateTime.now(), randomUUID(), randomUUID(), randomUUID(), ZonedDateTime.now(), "");
        final List<HearingEventPojo> hearingEventList = asList(hearingEventPojo);
        final Hearing hearing = buildHearing();

        final uk.gov.justice.core.courts.Hearing hearingPojo = mock(uk.gov.justice.core.courts.Hearing.class);
        final Set<UUID> activeHearingIds = new HashSet<>();
        activeHearingIds.add(randomUUID());

        final List<Hearing> hearings = new ArrayList<>();
        hearings.add(hearing);
        final Set<UUID> hearingEventRequiredDefinitionsIds = new HashSet<>();
        hearingEventRequiredDefinitionsIds.add(randomUUID());
        hearingEventRequiredDefinitionsIds.add(randomUUID());

        when(hearingRepository.findHearingsByDateAndCourtCentreList(now, courtCentreIds)).thenReturn(hearings);
        when(hearingEventRepository.findLatestHearingsForThatDay(courtCentreIds, now, hearingEventRequiredDefinitionsIds)).thenReturn(hearingEventList);
        when(hearingRepository.findBy(hearingEventPojo.getHearingId())).thenReturn(hearing);
        when(hearingJPAMapper.fromJPA(hearing)).thenReturn(hearingPojo);

        final CurrentCourtStatus expectedCurrentCourtStatus = getCurrentCourtStatusWithMultipleCases(hearingEvent);
        when(hearingListXhibitResponseTransformer.transformFrom(any(HearingEventsToHearingMapper.class))).thenReturn(expectedCurrentCourtStatus);

        final Optional<CurrentCourtStatus> response = hearingService.getHearingsByDate(courtCentreIds, now, hearingEventRequiredDefinitionsIds);
        assertCurrentCourtStatus(response.get(), expectedCurrentCourtStatus);
        assertThat(response.get().getPageName(), is(expectedCurrentCourtStatus.getPageName()));
    }

    @Test
    public void shouldGetHearingById() {
        final Hearing hearingStub = new Hearing();
        when(hearingRepository.findBy(Mockito.any(UUID.class))).thenReturn(hearingStub);
        final Optional<Hearing> optionalHearing = hearingService.getHearingById(randomUUID());

        verify(hearingRepository).findBy(Mockito.any(UUID.class));
        assertThat(optionalHearing.isPresent(), is(true));
        assertThat(hearingStub, is(optionalHearing.get()));
    }

    @Test
    public void shouldNotGetHearingByNonExistingId() {
        when(hearingRepository.findBy(Mockito.any(UUID.class))).thenReturn(null);
        final Optional<Hearing> optionalHearing = hearingService.getHearingById(randomUUID());

        verify(hearingRepository).findBy(Mockito.any(UUID.class));
        assertThat(optionalHearing.isPresent(), is(false));
    }

    @Test
    public void shouldGetHearingEvents() {
        final List<HearingEvent> hearingEventsStub = Arrays.asList(new HearingEvent());
        when(hearingEventRepository.findByHearingIdOrderByEventTimeAsc(Mockito.any(UUID.class), Mockito.any(LocalDate.class))).thenReturn(hearingEventsStub);
        when(hearingEventRepository.findHearingEvents(Mockito.any(UUID.class), Mockito.any(UUID.class), Mockito.any(LocalDate.class))).thenReturn(hearingEventsStub);
        final List<HearingEvent> firstHearingEventsList = hearingService.getHearingEvents(randomUUID(), LocalDate.now());
        final List<HearingEvent> secondHearingEventsList = hearingService.getHearingEvents(randomUUID(), randomUUID(), LocalDate.now());

        verify(hearingEventRepository).findByHearingIdOrderByEventTimeAsc(Mockito.any(UUID.class), Mockito.any(LocalDate.class));
        verify(hearingEventRepository).findHearingEvents(Mockito.any(UUID.class), Mockito.any(UUID.class), Mockito.any(LocalDate.class));
        assertThat(firstHearingEventsList, containsInAnyOrder(hearingEventsStub.toArray()));
        assertThat(secondHearingEventsList, containsInAnyOrder(hearingEventsStub.toArray()));
    }

    @Test
    public void shouldGetHearingEventsAsEmptyListByNonExistingHearingId() {
        when(hearingEventRepository.findByHearingIdOrderByEventTimeAsc(Mockito.any(UUID.class), Mockito.any(LocalDate.class))).thenReturn(Collections.emptyList());
        when(hearingEventRepository.findHearingEvents(Mockito.any(UUID.class), Mockito.any(UUID.class), Mockito.any(LocalDate.class))).thenReturn(Collections.emptyList());
        final List<HearingEvent> firstHearingEventsList = hearingService.getHearingEvents(randomUUID(), LocalDate.now());
        final List<HearingEvent> secondHearingEventsList = hearingService.getHearingEvents(randomUUID(), randomUUID(), LocalDate.now());

        verify(hearingEventRepository).findByHearingIdOrderByEventTimeAsc(Mockito.any(UUID.class), Mockito.any(LocalDate.class));
        verify(hearingEventRepository).findHearingEvents(Mockito.any(UUID.class), Mockito.any(UUID.class), Mockito.any(LocalDate.class));
        assertThat(firstHearingEventsList.isEmpty(), is(true));
        assertThat(secondHearingEventsList.isEmpty(), is(true));
    }

    @Test
    public void shouldGetHearingEventDefinition() {
        final HearingEventDefinition hearingEventDefinitionStub = new HearingEventDefinition();
        when(hearingEventDefinitionRepository.findBy(Mockito.any(UUID.class))).thenReturn(hearingEventDefinitionStub);
        final Optional<HearingEventDefinition> optionalHearingEventDefinition = hearingService.getHearingEventDefinition(randomUUID());

        verify(hearingEventDefinitionRepository).findBy(Mockito.any(UUID.class));
        assertThat(optionalHearingEventDefinition.isPresent(), is(true));
        assertThat(hearingEventDefinitionStub, is(optionalHearingEventDefinition.get()));
    }

    @Test
    public void shouldNotGetHearingEventDefinitionByNonExistingHearingId() {
        when(hearingEventDefinitionRepository.findBy(Mockito.any(UUID.class))).thenReturn(null);
        final Optional<HearingEventDefinition> optionalHearingEventDefinition = hearingService.getHearingEventDefinition(randomUUID());

        verify(hearingEventDefinitionRepository).findBy(Mockito.any(UUID.class));
        assertThat(optionalHearingEventDefinition.isPresent(), is(false));
    }

    @Test
    public void shouldGetHearingEventDefinitions() {
        final List<HearingEventDefinition> hearingEventDefinitionListStub = Arrays.asList(new HearingEventDefinition());
        when(hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel()).thenReturn(hearingEventDefinitionListStub);
        final List<HearingEventDefinition> hearingEventDefinitionList = hearingService.getHearingEventDefinitions();

        verify(hearingEventDefinitionRepository).findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();
        assertThat(hearingEventDefinitionList, containsInAnyOrder(hearingEventDefinitionListStub.toArray()));
    }

    @Test
    public void shouldGetHearingEventDefinitionsAsEmptyList() {
        when(hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel()).thenReturn(Collections.emptyList());
        final List<HearingEventDefinition> hearingEventDefinitionList = hearingService.getHearingEventDefinitions();

        verify(hearingEventDefinitionRepository).findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();
        assertThat(hearingEventDefinitionList.isEmpty(), is(true));
    }

    @Test
    public void shouldGetCourtCenterByHearingId() {
        final CourtCentre courtCentreStub = new CourtCentre();
        when(hearingRepository.findCourtCenterByHearingId(Mockito.any(UUID.class))).thenReturn(courtCentreStub);
        final Optional<CourtCentre> optionalCourtCentre = hearingService.getCourtCenterByHearingId(randomUUID());

        verify(hearingRepository).findCourtCenterByHearingId(Mockito.any(UUID.class));
        assertTrue(optionalCourtCentre.isPresent());
        assertThat(courtCentreStub, is(optionalCourtCentre.get()));
    }

    @Test
    public void shouldNotGetCourtCenterByNonExistingHearingId() {
        when(hearingRepository.findCourtCenterByHearingId(Mockito.any(UUID.class))).thenReturn(null);
        final Optional<CourtCentre> optionalCourtCentre = hearingService.getCourtCenterByHearingId(randomUUID());

        verify(hearingRepository).findCourtCenterByHearingId(Mockito.any(UUID.class));
        assertThat(optionalCourtCentre.isPresent(), is(false));
    }

    private void assertCurrentCourtStatus(final CurrentCourtStatus actual, final CurrentCourtStatus expected) {
        final Court actualCourt = actual.getCourt();
        final Court expectedCourt = expected.getCourt();
        assertCourt(actualCourt, expectedCourt);
    }

    private void assertCourt(final Court actualCourt, final Court expectedCourt) {
        assertThat(actualCourt.getCourtName(), is(expectedCourt.getCourtName()));
        final List<CourtSite> actualCourtCourtSites = actualCourt.getCourtSites();
        final List<CourtSite> expectedCourtSites = expectedCourt.getCourtSites();
        for (int i = 0; i < expectedCourtSites.size(); i++) {
            final CourtSite actualCourtSite = actualCourtCourtSites.get(i);
            final CourtSite expectedCourtSite = expectedCourtSites.get(i);
            assertThat(actualCourtSite.getCourtSiteName(), is(expectedCourtSite.getCourtSiteName()));
            assertThat(actualCourtSite.getId(), is(expectedCourtSite.getId()));
            assertCourtRooms(actualCourtSite, expectedCourtSite);
        }
    }

    private void assertCourtRooms(final CourtSite actualCourtSite, CourtSite expectedCourtSite) {
        final List<CourtRoom> actualCourtRooms = actualCourtSite.getCourtRooms();
        final List<CourtRoom> expectedCourtRooms = expectedCourtSite.getCourtRooms();
        for (int i = 0; i < expectedCourtRooms.size(); i++) {
            final CourtRoom actualCourtRoom = actualCourtRooms.get(i);
            final CourtRoom expectedCourtRoom = expectedCourtRooms.get(i);
            assertThat(actualCourtRoom.getCourtRoomName(), is(expectedCourtRoom.getCourtRoomName()));
            assertThat(actualCourtRoom.getCourtRoomId(), is(expectedCourtRoom.getCourtRoomId()));
            assertCourtCases(actualCourtRoom, expectedCourtRoom);
        }
    }

    private void assertCourtCases(final CourtRoom actualCourtRoom, final CourtRoom expectedCourtRoom) {
        final List<CaseDetail> actualCaseDetails = actualCourtRoom.getCases().getCasesDetails();
        final List<CaseDetail> expectedCaseDetails = expectedCourtRoom.getCases().getCasesDetails();
        for (int i = 0; i < expectedCaseDetails.size(); i++) {
            final CaseDetail actualCaseDetail = actualCaseDetails.get(i);
            final CaseDetail expectedCaseDetail = expectedCaseDetails.get(i);
            assertThat(actualCaseDetail.getCaseType(), is(expectedCaseDetail.getCaseType()));
            assertThat(actualCaseDetail.getCppUrn(), is(expectedCaseDetail.getCppUrn()));
            assertThat(actualCaseDetail.getHearingType(), is(expectedCaseDetail.getHearingType()));
            assertThat(actualCaseDetail.getCaseNumber(), is(expectedCaseDetail.getCaseNumber()));
            assertThat(actualCaseDetail.getActivecase(), is(expectedCaseDetail.getActivecase()));
            assertThat(actualCaseDetail.getDefenceCounsel(), is(expectedCaseDetail.getDefenceCounsel()));
            assertThat(actualCaseDetail.getNotBeforeTime(), is(expectedCaseDetail.getNotBeforeTime()));
            assertThat(actualCaseDetail.getHearingEvent(), is(expectedCaseDetail.getHearingEvent()));
            assertThat(actualCaseDetail.getLinkedCaseIds(), is(expectedCaseDetail.getLinkedCaseIds()));
            assertThat(actualCaseDetail.getDefendants(), is(expectedCaseDetail.getDefendants()));
        }
    }

    private Document buildDocument() {

        final Document document = new Document();
        document.setStartDate(LocalDate.of(2018, Month.JANUARY, 1));
        document.setId(randomUUID());
        document.setSubscriptions(asList(buildSubscription(), buildSubscription()));

        return document;
    }

    private uk.gov.moj.cpp.hearing.persist.entity.not.Subscription buildSubscription() {

        final uk.gov.moj.cpp.hearing.persist.entity.not.Subscription subscription = new uk.gov.moj.cpp.hearing.persist.entity.not.Subscription();
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

    private CrackedIneffectiveVacatedTrialTypes buildCrackedIneffectiveVacatedTrialTypes(final UUID trialTypeId) {
        final List<CrackedIneffectiveVacatedTrialType> trialList = new ArrayList<>();
        trialList.add(new CrackedIneffectiveVacatedTrialType(trialTypeId, "code", "InEffective", "fullDescription",null));

        return new CrackedIneffectiveVacatedTrialTypes().setCrackedIneffectiveVacatedTrialTypes(trialList);
    }
    private CrackedIneffectiveVacatedTrialTypes buildVacatedTrialTypes(final UUID vacatedTrialReasonId) {
        final List<CrackedIneffectiveVacatedTrialType> trialList = new ArrayList<>();
        trialList.add(new CrackedIneffectiveVacatedTrialType(vacatedTrialReasonId, "code", "Vacated", "fullDescription",null));

        return new CrackedIneffectiveVacatedTrialTypes().setCrackedIneffectiveVacatedTrialTypes(trialList);
    }

    private CurrentCourtStatus getCurrentCourtStatusWithMultipleCases(uk.gov.justice.core.courts.HearingEvent hearingEvent) {
        return currentCourtStatus()
                .withCourt(court()
                        .withCourtName("testCourtName")
                        .withCourtSites(Arrays.asList(courtSite()
                                .withId(randomUUID())
                                .withCourtSiteName("testCourtSiteName")
                                .withCourtRooms(Arrays.asList(courtRoom()
                                        .withCourtRoomName("courtRoomName")
                                        .withHearingEvent(hearingEvent)
                                        .withCases(cases().withCasesDetails(Arrays.asList(caseDetail2(), caseDetail3()))
                                                .build())
                                        .build()))
                                .build()))
                        .build()).build();
    }

    private CaseDetail caseDetail2() {
        return caseDetail()
                .withActivecase(valueOf(0))
                .withCaseNumber("1")
                .withCaseType("caseType")
                .withCppUrn("234")
                .withHearingType("hearingType")
                .withDefendants(Arrays.asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                .withJudgeName("Mr Lampard")
                .withNotBeforeTime("2020-02-09T15:00Z[UTC]")
                .build();
    }

    private CaseDetail caseDetail3() {
        return caseDetail()
                .withActivecase(valueOf(1))
                .withCaseNumber("1")
                .withCaseType("caseType")
                .withCppUrn("235")
                .withHearingType("hearingType")
                .withDefendants(Arrays.asList(defendant().withFirstName("Alexander").withMiddleName("de").withLastName("Jong").build()))
                .withJudgeName("Mr Lampard")
                .withNotBeforeTime("2020-02-09T15:00Z[UTC]")
                .build();
    }

    private Set<uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase> createProsecutionCases() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCase = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        final Defendant defendant = new Defendant();
        final Set<Defendant> defendants = asSet(defendant);
        prosecutionCase.setDefendants(defendants);
        return asSet(prosecutionCase);
    }
}
