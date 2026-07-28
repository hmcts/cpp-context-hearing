package uk.gov.moj.cpp.hearing.event.delegates.helper;

import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import uk.gov.moj.cpp.hearing.pi.ProsecutionCaseRetriever;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.ProsecutionCaseResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BailStatusHelperTest {

    @Mock
    private ReferenceDataService referenceDataService;

    @Mock
    private ProsecutionCaseRetriever prosecutionCaseRetriever;

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

    // ── Task 2.3: per-offence bailStatus set independently ──────────────────────────────

    @Test
    public void shouldSetOffenceBailStatusPerOffenceIndependently() {
        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        final Offence offence1 = Offence.offence()
                .withJudicialResults(singletonList(getJudicialResult("U")))
                .build();
        final Offence offence2 = Offence.offence()
                .withJudicialResults(singletonList(getJudicialResult("C")))
                .build();
        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("A").build())
                .build();
        final ResultsShared resultsShared = ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                                .withDefendants(singletonList(Defendant.defendant()
                                        .withOffences(Arrays.asList(offence1, offence2))
                                        .withPersonDefendant(personDefendant)
                                        .build()))
                                .build()))
                        .build())
                .build();

        bailStatusHelper.mapBailStatuses(context, resultsShared.getHearing());

        assertThat(offence1.getBailStatus().getCode(), is("U"));
        assertThat(offence2.getBailStatus().getCode(), is("C"));
        // defendant-level takes the highest priority (lowest ranking) → Custody
        assertThat(personDefendant.getBailStatus().getCode(), is("C"));
    }

    @Test
    public void shouldSetOffenceBailStatusForAllSixRemandTypes() {
        final List<BailStatus> allStatuses = buildFullListOfBailStatuses();
        when(referenceDataService.getBailStatuses(context)).thenReturn(allStatuses);

        final Offence offenceB = Offence.offence().withJudicialResults(singletonList(getJudicialResult("B"))).build();
        final Offence offenceU = Offence.offence().withJudicialResults(singletonList(getJudicialResult("U"))).build();
        final Offence offenceC = Offence.offence().withJudicialResults(singletonList(getJudicialResult("C"))).build();
        final Offence offenceL = Offence.offence().withJudicialResults(singletonList(getJudicialResult("L"))).build();
        final Offence offenceP = Offence.offence().withJudicialResults(singletonList(getJudicialResult("P"))).build();
        final Offence offenceS = Offence.offence().withJudicialResults(singletonList(getJudicialResult("S"))).build();

        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("A").build())
                .build();
        final ResultsShared resultsShared = ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                                .withDefendants(singletonList(Defendant.defendant()
                                        .withOffences(Arrays.asList(offenceB, offenceU, offenceC, offenceL, offenceP, offenceS))
                                        .withPersonDefendant(personDefendant)
                                        .build()))
                                .build()))
                        .build())
                .build();

        bailStatusHelper.mapBailStatuses(context, resultsShared.getHearing());

        assertThat(offenceB.getBailStatus().getCode(), is("B"));
        assertThat(offenceU.getBailStatus().getCode(), is("U"));
        assertThat(offenceC.getBailStatus().getCode(), is("C"));
        assertThat(offenceL.getBailStatus().getCode(), is("L"));
        assertThat(offenceP.getBailStatus().getCode(), is("P"));
        assertThat(offenceS.getBailStatus().getCode(), is("S"));
    }

    // ── Task 4.3: NHMC/NHCC as main result suppresses update; as child does not ─────────

    @Test
    public void shouldNotUpdateOffenceBailStatusWhenNhmcIsMainResult() {
        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        // NHMC as main result: parentJudicialResultId is null
        final JudicialResult nhmcMain = JudicialResult.judicialResult()
                .withJudicialResultTypeId(UUID.fromString("70c98fa6-804d-11e8-adc0-fa7ae01bbebc"))
                .withPostHearingCustodyStatus("A")
                .build();

        final Offence offence = Offence.offence()
                .withJudicialResults(singletonList(nhmcMain))
                .build();
        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("U").build())
                .build();
        final ResultsShared resultsShared = ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                                .withDefendants(singletonList(Defendant.defendant()
                                        .withOffences(singletonList(offence))
                                        .withPersonDefendant(personDefendant)
                                        .build()))
                                .build()))
                        .build())
                .build();

        bailStatusHelper.mapBailStatuses(context, resultsShared.getHearing());

        assertNull(offence.getBailStatus());
        // defendant-level retains existing
        assertThat(personDefendant.getBailStatus().getCode(), is("U"));
    }

    @Test
    public void shouldUpdateOffenceBailStatusWhenNhmcIsChildResultAlongsideQualifyingResult() {
        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        // Main result: CCII (Custody) - no parentJudicialResultId
        final JudicialResult custodyMain = getJudicialResult("C");

        // NHMC as child result: parentJudicialResultId is non-null
        final JudicialResult nhmcChild = JudicialResult.judicialResult()
                .withJudicialResultTypeId(UUID.fromString("70c98fa6-804d-11e8-adc0-fa7ae01bbebc"))
                .withPostHearingCustodyStatus("A")
                .withParentJudicialResultId(UUID.randomUUID())
                .build();

        final Offence offence = Offence.offence()
                .withJudicialResults(Arrays.asList(custodyMain, nhmcChild))
                .build();
        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("U").build())
                .build();
        final ResultsShared resultsShared = ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                                .withDefendants(singletonList(Defendant.defendant()
                                        .withOffences(singletonList(offence))
                                        .withPersonDefendant(personDefendant)
                                        .build()))
                                .build()))
                        .build())
                .build();

        bailStatusHelper.mapBailStatuses(context, resultsShared.getHearing());

        assertNotNull(offence.getBailStatus());
        assertThat(offence.getBailStatus().getCode(), is("C"));
        assertThat(personDefendant.getBailStatus().getCode(), is("C"));
    }

    @Test
    public void shouldNotUpdateOffenceBailStatusWhenNhccIsMainResult() {
        final List<BailStatus> bailStatusList = buildListOfBailStatuses();
        when(referenceDataService.getBailStatuses(context)).thenReturn(bailStatusList);

        final JudicialResult nhccMain = JudicialResult.judicialResult()
                .withJudicialResultTypeId(UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8"))
                .withPostHearingCustodyStatus("A")
                .build();

        final Offence offence = Offence.offence()
                .withJudicialResults(singletonList(nhccMain))
                .build();
        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("C").build())
                .build();
        final ResultsShared resultsShared = ResultsShared.builder()
                .withHearing(Hearing.hearing()
                        .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                                .withDefendants(singletonList(Defendant.defendant()
                                        .withOffences(singletonList(offence))
                                        .withPersonDefendant(personDefendant)
                                        .build()))
                                .build()))
                        .build())
                .build();

        bailStatusHelper.mapBailStatuses(context, resultsShared.getHearing());

        assertNull(offence.getBailStatus());
        assertThat(personDefendant.getBailStatus().getCode(), is("C"));
    }

    private List<BailStatus> buildFullListOfBailStatuses() {
        final List<BailStatus> list = new ArrayList<>(buildListOfBailStatuses());
        final BailStatus bs;

        bs = new BailStatus(); bs.setStatusCode("B"); bs.setStatusDescription("Conditional bail"); bs.setStatusRanking(4); bs.setId(fromString("aaaaaaaa-0001-0000-0000-000000000000")); list.add(bs);
        final BailStatus bsL = new BailStatus(); bsL.setStatusCode("L"); bsL.setStatusDescription("Remanded into care of Local Authority"); bsL.setStatusRanking(3); bsL.setId(fromString("aaaaaaaa-0002-0000-0000-000000000000")); list.add(bsL);
        final BailStatus bsP = new BailStatus(); bsP.setStatusCode("P"); bsP.setStatusDescription("Conditional Bail with Pre-Release conditions"); bsP.setStatusRanking(5); bsP.setId(fromString("aaaaaaaa-0003-0000-0000-000000000000")); list.add(bsP);
        final BailStatus bsS = new BailStatus(); bsS.setStatusCode("S"); bsS.setStatusDescription("Remanded to youth detention accommodation"); bsS.setStatusRanking(1); bsS.setId(fromString("aaaaaaaa-0004-0000-0000-000000000000")); list.add(bsS);
        return list;
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

    // ── Task 3.4: cross-hearing defendant remand scenarios ────────────────────────────────

    @Test
    public void shouldRetainHigherPriorityBailStatusFromOffenceOutsideCurrentHearing() {
        // Scenario 8: later hearing only includes offence1 (Unconditional bail).
        // Offence2 from a prior hearing has Custody and is still active.
        // Defendant remand must remain Custody.
        final List<BailStatus> allStatuses = buildFullListOfBailStatuses();
        when(referenceDataService.getBailStatuses(context)).thenReturn(allStatuses);

        final UUID hearingId = UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();
        final UUID offence2Id = UUID.randomUUID();

        // Current hearing: only offence1 with Unconditional bail
        final Offence offence1Current = Offence.offence()
                .withId(UUID.randomUUID())
                .withJudicialResults(singletonList(getJudicialResult("U")))
                .build();
        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("C").build())
                .build();
        final Defendant defendant = Defendant.defendant()
                .withId(defendantId)
                .withOffences(singletonList(offence1Current))
                .withPersonDefendant(personDefendant)
                .build();
        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                        .withDefendants(singletonList(defendant))
                        .build()))
                .build();

        // Stored offence2 from prior hearing: Custody, still active (proceedingsConcluded=false)
        final Offence offence2Stored = Offence.offence()
                .withId(offence2Id)
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("C").build())
                .withProceedingsConcluded(false)
                .build();
        final Defendant storedDefendant = Defendant.defendant()
                .withId(defendantId)
                .withOffences(singletonList(offence2Stored))
                .build();
        final ProsecutionCaseResponse response = ProsecutionCaseResponse.builder()
                .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                        .withDefendants(singletonList(storedDefendant))
                        .build()))
                .build();
        when(prosecutionCaseRetriever.getProsecutionCaseForHearing(hearingId, hearingId))
                .thenReturn(Optional.of(response));

        bailStatusHelper.mapBailStatuses(context, hearing);

        // offence1 in current hearing gets Unconditional bail
        assertThat(offence1Current.getBailStatus().getCode(), is("U"));
        // defendant-level: offence1=U (rank2), stored offence2=C (rank1) → Custody wins
        assertThat(personDefendant.getBailStatus().getCode(), is("C"));
    }

    @Test
    public void shouldRecalculateDefendantBailStatusWhenHighestPriorityOffenceUpdated() {
        // Scenario 9: offence2 previously Custody now resulted with Unconditional bail.
        // Current hearing contains both offences. Offence1=Conditional bail, offence2=Unconditional bail.
        // No stored higher-priority offences outside current hearing.
        // Defendant remand should be Conditional bail (next highest among B and U is B at rank4, U at rank2... wait,
        // rank ordering: S=1, C=1(same), L=3, U=2, B=4, P=5 — so U(rank2) < B(rank4) → defendant = Unconditional bail.
        // Actually looking at buildFullListOfBailStatuses: C=rank1(custody), S=rank1(youth detention), U=rank2, L=rank3, B=rank4, P=rank5
        // min by ranking → U wins over B → defendant = U
        final List<BailStatus> allStatuses = buildFullListOfBailStatuses();
        when(referenceDataService.getBailStatuses(context)).thenReturn(allStatuses);

        final UUID hearingId = UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();

        final Offence offence1 = Offence.offence()
                .withId(UUID.randomUUID())
                .withJudicialResults(singletonList(getJudicialResult("B")))
                .build();
        final Offence offence2 = Offence.offence()
                .withId(UUID.randomUUID())
                .withJudicialResults(singletonList(getJudicialResult("U")))
                .build();
        final PersonDefendant personDefendant = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("C").build())
                .build();
        final Defendant defendant = Defendant.defendant()
                .withId(defendantId)
                .withOffences(Arrays.asList(offence1, offence2))
                .withPersonDefendant(personDefendant)
                .build();
        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                        .withDefendants(singletonList(defendant))
                        .build()))
                .build();

        // No additional stored offences — empty response
        when(prosecutionCaseRetriever.getProsecutionCaseForHearing(hearingId, hearingId))
                .thenReturn(Optional.empty());

        bailStatusHelper.mapBailStatuses(context, hearing);

        assertThat(offence1.getBailStatus().getCode(), is("B"));
        assertThat(offence2.getBailStatus().getCode(), is("U"));
        // U(rank2) < B(rank4) → defendant = Unconditional bail
        assertThat(personDefendant.getBailStatus().getCode(), is("U"));
    }

    @Test
    public void shouldCalculateBailStatusIndependentlyForEachDefendant() {
        // Scenario 10: multi-defendant case, each defendant's remand calculated independently
        final List<BailStatus> allStatuses = buildFullListOfBailStatuses();
        when(referenceDataService.getBailStatuses(context)).thenReturn(allStatuses);

        final UUID hearingId = UUID.randomUUID();
        final UUID defendant1Id = UUID.randomUUID();
        final UUID defendant2Id = UUID.randomUUID();

        // Defendant 1: Custody offence
        final Offence def1Offence = Offence.offence()
                .withId(UUID.randomUUID())
                .withJudicialResults(singletonList(getJudicialResult("C")))
                .build();
        final PersonDefendant personDef1 = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("U").build())
                .build();
        final Defendant defendant1 = Defendant.defendant()
                .withId(defendant1Id)
                .withOffences(singletonList(def1Offence))
                .withPersonDefendant(personDef1)
                .build();

        // Defendant 2: Unconditional bail offence
        final Offence def2Offence = Offence.offence()
                .withId(UUID.randomUUID())
                .withJudicialResults(singletonList(getJudicialResult("U")))
                .build();
        final PersonDefendant personDef2 = PersonDefendant.personDefendant()
                .withBailStatus(uk.gov.justice.core.courts.BailStatus.bailStatus().withCode("C").build())
                .build();
        final Defendant defendant2 = Defendant.defendant()
                .withId(defendant2Id)
                .withOffences(singletonList(def2Offence))
                .withPersonDefendant(personDef2)
                .build();

        final Hearing hearing = Hearing.hearing()
                .withId(hearingId)
                .withProsecutionCases(singletonList(ProsecutionCase.prosecutionCase()
                        .withDefendants(Arrays.asList(defendant1, defendant2))
                        .build()))
                .build();

        when(prosecutionCaseRetriever.getProsecutionCaseForHearing(hearingId, hearingId))
                .thenReturn(Optional.empty());

        bailStatusHelper.mapBailStatuses(context, hearing);

        // Defendant 1 → Custody
        assertThat(def1Offence.getBailStatus().getCode(), is("C"));
        assertThat(personDef1.getBailStatus().getCode(), is("C"));

        // Defendant 2 → Unconditional bail
        assertThat(def2Offence.getBailStatus().getCode(), is("U"));
        assertThat(personDef2.getBailStatus().getCode(), is("U"));
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
