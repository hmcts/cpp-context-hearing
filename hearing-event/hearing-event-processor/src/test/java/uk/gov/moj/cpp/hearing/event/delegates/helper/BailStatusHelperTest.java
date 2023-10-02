package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.BailStatus;
import uk.gov.moj.cpp.hearing.event.service.ReferenceDataService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BailStatusHelperTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @Mock
    private JsonEnvelope context;

    @InjectMocks
    private BailStatusHelper bailStatusHelper;

    @Test
    public void testMapBailStatuses() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("U", Lists.newArrayList("C", "U", "A"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate.getHearing());

        uk.gov.justice.core.courts.BailStatus bailStatus = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailStatus();
        assertNotNull(bailStatus);
        assertThat(bailStatus.getId().toString(), is("fab947a3-c50c-4dbb-accf-b2758b1d2d6d"));
        assertThat(bailStatus.getCode(), is("C"));
        assertThat(bailStatus.getDescription(), is("Remanded into Custody"));

    }

    @Test
    public void testMapBailStatusesWithPostHearingStatusForNEXH() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("U", Lists.newArrayList("A", "A", "A"),"70c98fa6-804d-11e8-adc0-fa7ae01bbebc");
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate.getHearing());

        uk.gov.justice.core.courts.BailStatus bailStatus = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailStatus();
        assertNotNull(bailStatus);
        assertThat(bailStatus.getCode(), is("U"));

    }

    @Test
    public void testMapBailStatuses_ShouldNotUpdateIfApplicableStatusNotFound() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplate("U", Lists.newArrayList("W", "Z"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate.getHearing());

        uk.gov.justice.core.courts.BailStatus bailStatus = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant().getBailStatus();
        assertNotNull(bailStatus);
        assertThat(bailStatus.getCode(), is("U"));

    }

    @Test
    public void shouldMapBailStatusesWithoutPersonDefendant() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithoutPersonDefendant(Lists.newArrayList("C", "U", "A"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate.getHearing());

        PersonDefendant personDefendant = resultsSharedTemplate.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getPersonDefendant();
        assertNull(personDefendant);

    }

    @Test
    public void testMapBailStatuses_ShouldUpdateWhenCourtApplicationCasesExist() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCase("U", Lists.newArrayList("C", "U", "A"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate);

        uk.gov.justice.core.courts.BailStatus bailStatus = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailStatus();
        assertNotNull(bailStatus);
        assertThat(bailStatus.getId().toString(), is("fab947a3-c50c-4dbb-accf-b2758b1d2d6d"));
        assertThat(bailStatus.getCode(), is("C"));
        assertThat(bailStatus.getDescription(), is("Remanded into Custody"));



    }

    @Test
    public void testMapBailStatuses_ShouldNotUpdateIfApplicableStatusNotFoundWhenCourtApplicationCasesExist() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCase("U", Lists.newArrayList("W", "Z"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate);

        uk.gov.justice.core.courts.BailStatus bailStatus = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailStatus();
        assertNotNull(bailStatus);
        assertThat(bailStatus.getCode(), is("U"));

    }

    @Test
    public void shouldMapBailStatusesWithoutPersonDefendantWhenCourtApplicationCasesExist() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithoutPersonDefendantWithCourtApplicationCase(Lists.newArrayList("C", "U", "A"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate);

        PersonDefendant personDefendant = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant();
        assertNull(personDefendant);

    }

    @Test
    public void testMapBailStatuses_ShouldUpdateWhenCourtApplicationCourtOrderExists() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder("U", Lists.newArrayList("C", "U", "A"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate);

        uk.gov.justice.core.courts.BailStatus bailStatus = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailStatus();
        assertNotNull(bailStatus);
        assertThat(bailStatus.getId().toString(), is("fab947a3-c50c-4dbb-accf-b2758b1d2d6d"));
        assertThat(bailStatus.getCode(), is("C"));
        assertThat(bailStatus.getDescription(), is("Remanded into Custody"));
    }

    @Test
    public void testMapBailStatuses_ShouldNotUpdateIfApplicableStatusNotFoundWhenCourtApplicationCourtOrderExists() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithCourtApplicationCourtOrder("U", Lists.newArrayList("W", "Z"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate);

        uk.gov.justice.core.courts.BailStatus bailStatus = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant().getBailStatus();
        assertNotNull(bailStatus);
        assertThat(bailStatus.getCode(), is("U"));

    }

    @Test
    public void shouldMapBailStatusesWithoutPersonDefendantWhenCourtApplicationCourtOrderExists() {

        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        final ResultsShared resultsSharedTemplate = buildResultsSharedTemplateWithoutPersonDefendantWithCourtApplicationCourtOrder(Lists.newArrayList("C", "U", "A"));
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        bailStatusHelper.mapBailStatuses(context, resultsSharedTemplate);

        PersonDefendant personDefendant = resultsSharedTemplate.getHearing().getCourtApplications().get(0).getSubject().getMasterDefendant().getPersonDefendant();
        assertNull(personDefendant);

    }
    private ResultsShared buildResultsSharedTemplate(final String defendantBailStatusCode, final List<String> postHearingCustodyStatuses, final String judicialResultTypeId) {
        final List<JudicialResult> offenceJudicialResults = postHearingCustodyStatuses.stream().map(s -> getJudicialResult(s,judicialResultTypeId)).collect(Collectors.toList());

        final PersonDefendant personDefendant = PersonDefendant
                .personDefendant().withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode(defendantBailStatusCode).build())
                .build();

        return ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                                .withDefendants(Arrays.asList(Defendant.defendant()
                                        .withOffences(Arrays.asList(Offence.offence()
                                                .withJudicialResults(offenceJudicialResults)
                                                .build(), Offence.offence()
                                                .withJudicialResults(offenceJudicialResults)
                                                .build()))
                                        .withPersonDefendant(personDefendant)
                                        .build())
                                )
                                .build()))
                        .build())
                .build();
    }
    private ResultsShared buildResultsSharedTemplate(final String defendantBailStatusCode, final List<String> postHearingCustodyStatuses) {
        final List<JudicialResult> offenceJudicialResults = postHearingCustodyStatuses.stream().map(s -> getJudicialResult(s)).collect(Collectors.toList());

        final PersonDefendant personDefendant = PersonDefendant
                .personDefendant().withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode(defendantBailStatusCode).build())
                .build();
        return ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                                .withDefendants(Arrays.asList(Defendant.defendant()
                                        .withOffences(Arrays.asList(Offence.offence()
                                                .withJudicialResults(offenceJudicialResults)
                                                .build(), Offence.offence()
                                                .withJudicialResults(offenceJudicialResults)
                                                .build()))
                                        .withPersonDefendant(personDefendant)
                                        .build())
                                )
                                .build()))
                        .build())
                .build();
    }

    private ResultsShared buildResultsSharedTemplateWithoutPersonDefendant(final List<String> postHearingCustodyStatuses) {
        final List<JudicialResult> offenceJudicialResults = postHearingCustodyStatuses.stream().map(s -> getJudicialResult(s)).collect(Collectors.toList());

        return ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withProsecutionCases(Arrays.asList(ProsecutionCase.prosecutionCase()
                                .withDefendants(Arrays.asList(Defendant.defendant()
                                        .withOffences(Arrays.asList(Offence.offence()
                                                .withJudicialResults(offenceJudicialResults)
                                                .build(), Offence.offence()
                                                .withJudicialResults(offenceJudicialResults)
                                                .build()))
                                        .build())
                                )
                                .build()))
                        .build())
                .build();
    }

    private ResultsShared buildResultsSharedTemplateWithCourtApplicationCase(final String defendantBailStatusCode, final List<String> postHearingCustodyStatuses) {
        final List<JudicialResult> offenceJudicialResults = postHearingCustodyStatuses.stream().map(s -> getJudicialResult(s)).collect(Collectors.toList());

        final PersonDefendant personDefendant = PersonDefendant
                .personDefendant().withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode(defendantBailStatusCode).build())
                .build();
        return ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withCourtApplications(singletonList(CourtApplication.courtApplication()
                                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                                        .withOffences(singletonList(Offence.offence()
                                                        .withJudicialResults(offenceJudicialResults)
                                                        .build()))
                                        .withCaseStatus("ACTIVE")
                                        .build()))
                                .withSubject(CourtApplicationParty.courtApplicationParty()
                                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                                .withPersonDefendant(personDefendant)
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    private ResultsShared buildResultsSharedTemplateWithCourtApplicationCourtOrder(final String defendantBailStatusCode, final List<String> postHearingCustodyStatuses) {
        final List<JudicialResult> offenceJudicialResults = postHearingCustodyStatuses.stream().map(s -> getJudicialResult(s)).collect(Collectors.toList());

        final PersonDefendant personDefendant = PersonDefendant
                .personDefendant().withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode(defendantBailStatusCode).build())
                .build();
        return ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withCourtApplications(singletonList(CourtApplication.courtApplication()
                                .withCourtOrder(CourtOrder.courtOrder()
                                        .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                                .withOffence(Offence.offence()
                                                        .withJudicialResults(offenceJudicialResults)
                                                        .build())
                                                .build())).build())
                                .withSubject(CourtApplicationParty.courtApplicationParty()
                                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                                .withPersonDefendant(personDefendant)
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    private ResultsShared buildResultsSharedTemplateWithoutPersonDefendantWithCourtApplicationCase(final List<String> postHearingCustodyStatuses) {
        final List<JudicialResult> offenceJudicialResults = postHearingCustodyStatuses.stream().map(s -> getJudicialResult(s)).collect(Collectors.toList());

        return ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withCourtApplications(singletonList(CourtApplication.courtApplication()
                                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                                        .withOffences(singletonList(Offence.offence()
                                                        .withJudicialResults(offenceJudicialResults)
                                                        .build()))
                                        .withCaseStatus("ACTIVE")
                                        .build()))
                                .withSubject(CourtApplicationParty.courtApplicationParty()
                                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    private ResultsShared buildResultsSharedTemplateWithoutPersonDefendantWithCourtApplicationCourtOrder(final List<String> postHearingCustodyStatuses) {
        final List<JudicialResult> offenceJudicialResults = postHearingCustodyStatuses.stream().map(s -> getJudicialResult(s)).collect(Collectors.toList());

        return ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 5, 2), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build(), HearingDay.hearingDay()
                                .withSittingDay(ZonedDateTime.of(LocalDate.of(2018, 6, 4), LocalTime.of(12, 1, 1), ZoneId.systemDefault()))
                                .build()))
                        .withCourtApplications(singletonList(CourtApplication.courtApplication()
                                .withCourtOrder(CourtOrder.courtOrder()
                                        .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                                .withOffence(Offence.offence()
                                                        .withJudicialResults(offenceJudicialResults)
                                                        .build())
                                                .build())).build())
                                .withSubject(CourtApplicationParty.courtApplicationParty()
                                        .withMasterDefendant(MasterDefendant.masterDefendant()
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    private JudicialResult getJudicialResult(final String postHearingCustodyStatus) {
        return JudicialResult.judicialResult().withPostHearingCustodyStatus(postHearingCustodyStatus).build();
    }

    private JudicialResult getJudicialResult(final String postHearingCustodyStatus,final  String judicialResultTypeId) {
        return JudicialResult.judicialResult().withPostHearingCustodyStatus(postHearingCustodyStatus).withJudicialResultTypeId(UUID.fromString(judicialResultTypeId)).build();
    }

    private List<BailStatus> buildListOfBailStatuses() {
        final List<BailStatus> bailStatusList = new ArrayList<>();
        final BailStatus bailStatus = new BailStatus();
        bailStatus.setStatusDescription("CONDITIONAL");
        bailStatus.setStatusRanking(1);
        bailStatus.setId(fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6d"));
        bailStatus.setStatusCode("C");
        bailStatus.setStatusDescription("Remanded into Custody");
        final BailStatus bailStatus2 = new BailStatus();
        bailStatus2.setStatusDescription("UNCONDITIONAL");
        bailStatus2.setStatusRanking(2);
        bailStatus2.setId(fromString("fab947a3-c50d-4dbb-accf-b2758b1d2d6d"));
        bailStatus2.setStatusCode("U");
        bailStatus2.setStatusDescription("Unconditional Bail");
        final BailStatus bailStatus3 = new BailStatus();
        bailStatus3.setStatusDescription("CONDITIONAL");
        bailStatus3.setStatusRanking(3);
        bailStatus3.setId(fromString("fab947a3-c50c-4dbb-accf-b2758b1d2d6f"));
        bailStatus3.setStatusCode("A");
        bailStatus3.setStatusDescription("Not applicable");

        bailStatusList.add(bailStatus);
        bailStatusList.add(bailStatus2);
        bailStatusList.add(bailStatus3);
        return bailStatusList;
    }

}
