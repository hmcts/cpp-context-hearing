package uk.gov.moj.cpp.hearing.query.view.service;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.helper;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.targetTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingTypeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseIdentifierJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.NowsRepository;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Nows;
import uk.gov.moj.cpp.hearing.persist.entity.ha.NowsMaterial;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCaseIdentifier;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.persist.entity.not.Document;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponse;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponseDefendant;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingListResponseHearing;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCase;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.repository.DocumentRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.NowsMaterialRepository;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.json.JsonString;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


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
    private TargetJPAMapper targetJPAMapper;

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
        final Hearing hearing = HearingTestUtils.buildHearing();
        when(hearingRepository.findByFilters(sittingDate, hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId())).thenReturn(asList(hearing));

        String startTime = "09:15";
        String endTime = "10:29";

        final HearingListResponse response = caseHearingService.getHearingByDateV2(sittingDate, startTime, endTime, hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId());
        assertEquals(0, response.getHearings().size());
    }

    @Test
    public void shouldNotFindHearingListWhenStartDateAndEndDateAreAfterSittingDate() {
        LocalDate sittingDate = HearingTestUtils.START_DATE_1.toLocalDate(); //2018-02-22T10:30:00
        final Hearing hearing = HearingTestUtils.buildHearing();
        when(hearingRepository.findByFilters(sittingDate, hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId())).thenReturn(asList(hearing));

        String startTime = "10:31";
        String endTime = "11:30";

        final HearingListResponse response = caseHearingService.getHearingByDateV2(sittingDate, startTime, endTime, hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId());
        assertEquals(0, response.getHearings().size());
    }

    @Test
    public void shouldFindHearingListWhenStartDateIsBeforeAndEndDateIsAfterSittingDate() {

        uk.gov.justice.core.courts.ProsecutionCaseIdentifier prosecutionCaseIdentifier = uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier()
                .withCaseURN("8C720B32E45B")
                .withProsecutionAuthorityCode("AUTH CODE")
                .withProsecutionAuthorityId(UUID.fromString("1dbab0cf-3822-46ff-b3ea-ddcf99e71ab9"))
                .withProsecutionAuthorityReference("AUTH REF")
                .build();

        uk.gov.justice.core.courts.HearingType hearingType = uk.gov.justice.core.courts.HearingType.hearingType()
                .withId(UUID.fromString("019556b2-a25e-4ea7-b3f1-8c89d14b02e0"))
                .withDescription("TRIAL")
                .build();

        uk.gov.justice.core.courts.HearingDay hearingDay = uk.gov.justice.core.courts.HearingDay.hearingDay()
                .withSittingDay(HearingTestUtils.START_DATE_1)
                .withListedDurationMinutes(2)
                .withListingSequence(5)
                .build();

        LocalDate startDateStartOfDay = HearingTestUtils.START_DATE_1.toLocalDate();
        final HearingTestUtils.HearingHelper hearing = helper(HearingTestUtils.buildHearing());

        when(hearingRepository.findByFilters(startDateStartOfDay, hearing.it().getCourtCentre().getId(), hearing.it().getCourtCentre().getRoomId())).thenReturn(asList(hearing.it()));
        when(prosecutionCaseIdentifierJPAMapper.fromJPA(any(ProsecutionCaseIdentifier.class))).thenReturn(prosecutionCaseIdentifier);
        when(hearingTypeJPAMapper.fromJPA(any(HearingType.class))).thenReturn(hearingType);
        when(hearingDayJPAMapper.fromJPA(any(HearingDay.class))).thenReturn(hearingDay);

        final HearingListResponse response = caseHearingService.getHearingByDateV2(HearingTestUtils.START_DATE_1.toLocalDate(),
                "10:15", "14:30", hearing.it().getCourtCentre().getId(), hearing.it().getCourtCentre().getRoomId());


        assertThat(response, isBean(HearingListResponse.class)
                .with(HearingListResponse::getHearings, first(isBean(HearingListResponseHearing.class)
                        .with(HearingListResponseHearing::getId, is(hearing.it().getId()))
                        .with(HearingListResponseHearing::getType, isBean(uk.gov.justice.core.courts.HearingType.class)
                                .with(uk.gov.justice.core.courts.HearingType::getId, is(hearing.it().getHearingType().getId()))
                                .with(uk.gov.justice.core.courts.HearingType::getDescription, is(hearing.it().getHearingType().getDescription()))
                        )
                        .with(HearingListResponseHearing::getJurisdictionType, is(hearing.it().getJurisdictionType()))
                        .with(HearingListResponseHearing::getReportingRestrictionReason, is(hearing.it().getReportingRestrictionReason()))
                        .with(HearingListResponseHearing::getHearingLanguage, is(hearing.it().getHearingLanguage().toString()))
                        .with(HearingListResponseHearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getId, is(hearing.getFirstProsecutionCase().getId().getId()))
                                .with(ProsecutionCase::getProsecutionCaseIdentifier, isBean(uk.gov.justice.core.courts.ProsecutionCaseIdentifier.class)
                                        .with(uk.gov.justice.core.courts.ProsecutionCaseIdentifier::getCaseURN, is(hearing.getFirstProsecutionCase().getProsecutionCaseIdentifier().getCaseURN()))
                                        .with(uk.gov.justice.core.courts.ProsecutionCaseIdentifier::getProsecutionAuthorityCode, is(hearing.getFirstProsecutionCase().getProsecutionCaseIdentifier().getProsecutionAuthorityCode()))
                                        .with(uk.gov.justice.core.courts.ProsecutionCaseIdentifier::getProsecutionAuthorityReference, is(hearing.getFirstProsecutionCase().getProsecutionCaseIdentifier().getProsecutionAuthorityReference()))
                                        .with(uk.gov.justice.core.courts.ProsecutionCaseIdentifier::getProsecutionAuthorityId, is(hearing.getFirstProsecutionCase().getProsecutionCaseIdentifier().getProsecutionAuthorityId()))
                                )
                                .with(ProsecutionCase::getDefendants, first(isBean(HearingListResponseDefendant.class)
                                        .with(HearingListResponseDefendant::getId, is(hearing.getFirstDefendant().getId().getId()))
                                        .with(HearingListResponseDefendant::getName, is(String.format("%s %s %s",
                                                hearing.getFirstDefendantPersonDetails().getFirstName(),
                                                hearing.getFirstDefendantPersonDetails().getMiddleName(),
                                                hearing.getFirstDefendantPersonDetails().getLastName()
                                        )))
                                ))

                        ))

                ))
        );
    }

    @Test
    public void shouldFindHearingDetailsById() throws Exception {

        Hearing entity = mock(Hearing.class);

        uk.gov.justice.core.courts.Hearing pojo = mock(uk.gov.justice.core.courts.Hearing.class);

        UUID hearingId = randomUUID();

        when(hearingRepository.findBy(hearingId)).thenReturn(entity);

        when(hearingJPAMapper.fromJPA(entity)).thenReturn(pojo);

        HearingDetailsResponse response = caseHearingService.getHearingById(hearingId);

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

        final JsonObject response = caseHearingService.getNowsRepository(nowMaterialId.toString());
        assertThat(response.getJsonArray("allowedUserGroups").getValuesAs(JsonString.class).stream().map(JsonString::getString).collect(Collectors.toList()), containsInAnyOrder(nowsMaterial.getUserGroups().toArray()));
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

        assertThat(response.toString(), is("{}"));
    }

    @Test
    public void shouldReturnEmptyResponseWhenNowTypeIdNotFound() {

        final String referenceDate = "15012018";

        final Document document = buildDocument();

        final String nowTypeId = randomUUID().toString();

        when(documentRepository.findAllByOrderByStartDateAsc()).thenReturn(asList(document));

        final JsonObject response = caseHearingService.getSubscriptions(referenceDate, nowTypeId);

        assertThat(response.toString(), is("{\"subscriptions\":[]}"));
    }

    @Test
    public void shouldReturnEmptyResponseWhenTargetNotAdded() {

        final Hearing hearing = new Hearing();

        hearing.setId(randomUUID());

        when(hearingRepository.findBy(any())).thenReturn(hearing);

        when(targetJPAMapper.fromJPA(anySet())).thenReturn(new ArrayList());

        final TargetListResponse targetListResponse = caseHearingService.getTargets(hearing.getId());

        assertThat(targetListResponse.getTargets().isEmpty(), is(true));
    }


    @Test
    public void shouldReturnResponseWhenTargetIsAdded() {

        final Hearing hearing = new Hearing();

        hearing.setId(randomUUID());
        hearing.setTargets(asSet(new Target()));

        final List<uk.gov.justice.core.courts.Target> targets = asList(
                targetTemplate(),
                targetTemplate());

        when(hearingRepository.findBy(any())).thenReturn(hearing);

        when(targetJPAMapper.fromJPA(anySet())).thenReturn(targets);

        final TargetListResponse targetListResponse = caseHearingService.getTargets(hearing.getId());

        final uk.gov.justice.core.courts.Target targetIn = targets.get(0);

        final ResultLine resultLine = targetIn.getResultLines().get(0);

        final Prompt prompt = resultLine.getPrompts().get(0);

        assertThat(targetListResponse, isBean(TargetListResponse.class)
                .with(t -> t.getTargets().isEmpty(), is(false))
                .with(TargetListResponse::getTargets, first(isBean(uk.gov.justice.core.courts.Target.class)
                        .with(uk.gov.justice.core.courts.Target::getTargetId, is(targetIn.getTargetId()))
                        .with(uk.gov.justice.core.courts.Target::getDefendantId, is(targetIn.getDefendantId()))
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
}
