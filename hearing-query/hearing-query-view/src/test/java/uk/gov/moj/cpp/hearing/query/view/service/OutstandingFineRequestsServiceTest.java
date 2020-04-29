package uk.gov.moj.cpp.hearing.query.view.service;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequests;
import uk.gov.moj.cpp.hearing.domain.DefendantOutstandingFineRequestsResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.query.view.HearingTestUtils;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.time.LocalDate;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.query.view.HearingTestUtils.helper;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;


@RunWith(MockitoJUnitRunner.class)
public class OutstandingFineRequestsServiceTest {


    @Mock
    private HearingRepository hearingRepository;


    @InjectMocks
    private OutstandingFineRequestsService outstandingFineRequestsService;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void shouldFindHearingList() {

        final LocalDate startDateStartOfDay = HearingTestUtils.START_DATE_1.toLocalDate();
        final HearingTestUtils.HearingHelper hearingHelper = helper(HearingTestUtils.buildHearing());
        final Hearing hearingEntity = hearingHelper.it();


        when(hearingRepository.findByHearingDate(startDateStartOfDay)).thenReturn(asList(hearingEntity));

        final DefendantOutstandingFineRequestsResult response = outstandingFineRequestsService.getDefendantOutstandingFineRequestsByHearingDate(HearingTestUtils.START_DATE_1.toLocalDate());

        assertThat(response.getDefendantDetails().size() ,greaterThan( 0));

    }

    @Test
    public void shouldFindHearingEmptyList() {

        final LocalDate startDateStartOfDay = HearingTestUtils.START_DATE_1.toLocalDate();


        when(hearingRepository.findByHearingDate(startDateStartOfDay)).thenReturn(emptyList());

        final DefendantOutstandingFineRequestsResult response = outstandingFineRequestsService.getDefendantOutstandingFineRequestsByHearingDate(HearingTestUtils.START_DATE_1.toLocalDate());

        assertThat(response.getDefendantDetails().size() ,is(0) );

    }

    @Test
    public void shouldFindHearingListWithHearingDate() {

        final LocalDate hearingDate = HearingTestUtils.START_DATE_1.toLocalDate();
        final HearingTestUtils.HearingHelper hearingHelper = helper(HearingTestUtils.buildHearingWithRandomDefendants());
        final Hearing hearingEntity = hearingHelper.it();

        final HearingTestUtils.HearingHelper hearingHelper2 = helper(HearingTestUtils.buildHearingWithRandomDefendants());
        final Hearing hearingEntity2 = hearingHelper2.it();

        when(hearingRepository.findByHearingDate(hearingDate)).thenReturn(asList(hearingEntity, hearingEntity2));

        final DefendantOutstandingFineRequestsResult response = outstandingFineRequestsService.getDefendantOutstandingFineRequestsByHearingDate(HearingTestUtils.START_DATE_1.toLocalDate());

        assertTrue(response.getDefendantDetails().size() > 0);
        assertThat(response.getDefendantDetails().size(), is(4));
        assertThat(response.getDefendantDetails(), containsInAnyOrder(
                Stream.concat(hearingEntity.getProsecutionCases().stream(),hearingEntity2.getProsecutionCases().stream())
                        .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                        .map(defendant -> isBean(DefendantOutstandingFineRequests.class).with(DefendantOutstandingFineRequests::getDefendantId, Matchers.is(defendant.getId().getId())))
                        .toArray((IntFunction<Matcher<DefendantOutstandingFineRequests>[]>)Matcher[]::new)
        ));

    }
}
