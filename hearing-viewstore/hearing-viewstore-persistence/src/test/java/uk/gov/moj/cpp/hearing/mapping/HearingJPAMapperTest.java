package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.ApprovalRequest;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DefendantAttendance;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingCaseNote;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.IndicatedPlea;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.PleaModel;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.ReferralReason;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.repository.HearingYouthCourtDefendantsRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingJPAMapperTest {

    public static final int NUMBER_OF_GROUP_CASES = 7;
    @Mock
    private CourtCentreJPAMapper courtCentreJPAMapper;
    @Mock
    private HearingDefenceCounselJPAMapper defenceCounselJPAMapper;
    @Mock
    private DefendantAttendanceJPAMapper defendantAttendanceJPAMapper;
    @Mock
    private DefendantReferralReasonJPAMapper defendantReferralReasonsJPAMapper;
    @Mock
    private HearingCaseNoteJPAMapper hearingCaseNoteJPAMapper;
    @Mock
    private HearingDayJPAMapper hearingDayJPAMapper;
    @Mock
    private JudicialRoleJPAMapper judicialRoleJPAMapper;
    @Mock
    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper;
    @Mock
    private HearingProsecutionCounselJPAMapper hearingProsecutionCounselJPAMapper;
    @Mock
    private TargetJPAMapper targetJPAMapper;
    @Mock
    private HearingTypeJPAMapper hearingTypeJPAMapper;
    @Mock
    private CourtApplicationsSerializer courtApplicationsSerializer;
    @Mock
    private HearingRespondentCounselJPAMapper hearingRespondentCounselJPAMapper;
    @Mock
    private HearingApplicantCounselJPAMapper hearingApplicantCounselJPAMapper;
    @Mock
    private HearingInterpreterIntermediaryJPAMapper hearingInterpreterIntermediaryJPAMapper;
    @Mock
    private HearingCompanyRepresentativeJPAMapper hearingCompanyRepresentativeJPAMapper;
    @Mock
    private ApprovalRequestedJPAMapper approvalRequestedJPAMapper;

    @Mock
    private HearingYouthCourtDefendantsRepository hearingYouthCourtDefendantsRepository;

    @Captor
    private ArgumentCaptor<List<CourtApplication>> courtApplicationCaptor;

    @InjectMocks
    private HearingJPAMapper hearingJPAMapper;

    @Test
    public void testInsertCourtApplications() {
        final CourtApplication courtApplicationUpdate = CourtApplication.courtApplication().withId(randomUUID()).build();
        final List<CourtApplication> existingCourtApplications = asList(
                CourtApplication.courtApplication().withId(randomUUID()).build(),
                CourtApplication.courtApplication().withId(randomUUID()).build()
        );

        final List<CourtApplication> courtApplicationsOut = testAddOrUpdateCourtApplications(existingCourtApplications, courtApplicationUpdate);

        assertThat(courtApplicationsOut.size(), is(3));
        Set<UUID> expectedUuids = new HashSet<>();
        expectedUuids.add(existingCourtApplications.get(0).getId());
        expectedUuids.add(existingCourtApplications.get(1).getId());
        expectedUuids.add(courtApplicationUpdate.getId());

        assertThat(courtApplicationsOut.stream().map(CourtApplication::getId).collect(Collectors.toSet()), is(expectedUuids));
    }

    @Test
    public void testUpdateCourtApplications() {
        final CourtApplication courtApplicationUpdate = CourtApplication.courtApplication().withId(randomUUID()).build();
        final List<CourtApplication> existingCourtApplications = asList(
                CourtApplication.courtApplication().withId(courtApplicationUpdate.getId()).build(),
                CourtApplication.courtApplication().withId(randomUUID()).build()
        );

        final List<CourtApplication> courtApplicationsOut = testAddOrUpdateCourtApplications(existingCourtApplications, courtApplicationUpdate);

        assertThat(courtApplicationsOut.size(), is(2));
        Set<UUID> expectedUuids = new HashSet<>();
        expectedUuids.add(existingCourtApplications.get(1).getId());
        expectedUuids.add(courtApplicationUpdate.getId());

        assertThat(courtApplicationsOut.stream().map(CourtApplication::getId).collect(Collectors.toSet()), is(expectedUuids));
    }


    private List<CourtApplication> testAddOrUpdateCourtApplications(final List<CourtApplication> existingCourtApplications, final CourtApplication courtApplicationUpdate) {

        final String existingCourtApplicationsJson = "xyz";
        final String expectedCourtApplicationsJson = "abc";

        when(courtApplicationsSerializer.courtApplications(existingCourtApplicationsJson)).thenReturn(existingCourtApplications);
        when(courtApplicationsSerializer.json(Mockito.anyList())).thenReturn(expectedCourtApplicationsJson);
        final String strResult = hearingJPAMapper.addOrUpdateCourtApplication(existingCourtApplicationsJson, courtApplicationUpdate);
        assertThat(strResult, is(expectedCourtApplicationsJson));
        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        return courtApplicationCaptor.getValue();

    }

    @Test
    public void ShouldUpdateConvictedDateForOffenceUnderCourtApplicationCase(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(Arrays.asList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(offenceId).build(), uk.gov.justice.core.courts.Offence.offence()
                                .withId(randomUUID()).build()))
                        .build()))
                .build());
        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(singletonList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(randomUUID()).build()))
                        .build()))
                .build());

        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final LocalDate convictedDate =  LocalDate.now();

        final String strResult = hearingJPAMapper.updateConvictedDateOnOffencesInCourtApplication("",courtAppId, offenceId,  convictedDate );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getConvictionDate(), is(convictedDate));

        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getConvictionDate(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getConvictionDate(), nullValue());
    }

    @Test
    public void ShouldUpdateConvictedDateForOffenceUnderCourtApplicationCourtOrder(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(Arrays.asList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(offenceId).build())
                                .build(),CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(randomUUID()).build())
                                .build()))
                        .build())
                .build());

        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(offenceId).build())
                                .build()))
                        .build())
                .build());
        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final LocalDate convictedDate =  LocalDate.now();

        final String strResult = hearingJPAMapper.updateConvictedDateOnOffencesInCourtApplication("",courtAppId, offenceId,  convictedDate );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId(), is(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getConvictionDate(), is(convictedDate));

        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getId(), is(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getConvictionDate(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId(), is(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId()));
        assertThat(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getConvictionDate(), nullValue());
    }


    @Test
    public void ShouldUpdatePleaForOffenceUnderCourtApplicationCase(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(Arrays.asList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(offenceId).build(), uk.gov.justice.core.courts.Offence.offence()
                                .withId(randomUUID()).build()))
                        .build()))
                .build());
        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(singletonList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(randomUUID()).build()))
                        .build()))
                .build());

        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final PleaModel plea = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withApplicationId(courtAppId)
                .withPlea(Plea.plea()
                        .withPleaDate(LocalDate.now())
                        .withPleaValue("GUILTY")
                        .withOffenceId(offenceId).build())
                .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                        .withOffenceId(offenceId)
                        .withIndicatedPleaValue(IndicatedPleaValue.INDICATED_GUILTY)
                        .build())
                .withAllocationDecision(AllocationDecision.allocationDecision().build())
                .build();

        final String strResult = hearingJPAMapper.updatePleaOnOffencesInCourtApplication("",  plea );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getPlea().getPleaValue(), is("GUILTY"));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getIndicatedPlea().getIndicatedPleaValue(), is(IndicatedPleaValue.INDICATED_GUILTY));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getAllocationDecision(), notNullValue());

        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getPlea(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getPlea(), nullValue());
    }

    @Test
    public void ShouldRemovePleaForOffenceUnderCourtApplicationCase(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(Arrays.asList(Offence.offence()
                                .withId(offenceId)
                                .withPlea(Plea.plea().build())
                                .withIndicatedPlea(IndicatedPlea.indicatedPlea().build())
                                .withAllocationDecision(AllocationDecision.allocationDecision().build())
                                .build(), Offence.offence()
                                .withId(randomUUID())
                               .build()))
                        .build()))
                .build());
        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(singletonList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(randomUUID()).build()))
                        .build()))
                .build());

        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final PleaModel plea = PleaModel.pleaModel()
                .withApplicationId(courtAppId)
                .withOffenceId(offenceId).build();

        final String strResult = hearingJPAMapper.updatePleaOnOffencesInCourtApplication("",  plea );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getPlea(), is(nullValue()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getIndicatedPlea(), is(nullValue()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getAllocationDecision(), is(nullValue()));

        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getPlea(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getPlea(), nullValue());
    }

    @Test
    public void ShouldUpdatePleaForOffenceUnderCourtApplicationCourtOrders(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(Arrays.asList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(offenceId).build())
                                .build(),CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(randomUUID()).build())
                                .build()))
                        .build())
                .build());

        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(offenceId).build())
                                .build()))
                        .build())
                .build());

        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final PleaModel pleaModel = PleaModel.pleaModel()
                .withOffenceId(offenceId)
                .withApplicationId(courtAppId)
                .withPlea(Plea.plea()
                        .withPleaDate(LocalDate.now())
                        .withPleaValue("GUILTY")
                        .withOffenceId(offenceId).build())
                .withIndicatedPlea(IndicatedPlea.indicatedPlea()
                        .withOffenceId(offenceId)
                        .withIndicatedPleaValue(IndicatedPleaValue.INDICATED_GUILTY)
                        .build())
                .withAllocationDecision(AllocationDecision.allocationDecision().build())
                .build();

        final String strResult = hearingJPAMapper.updatePleaOnOffencesInCourtApplication("",  pleaModel );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId(), is(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getPlea().getPleaValue(), is("GUILTY"));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getIndicatedPlea().getIndicatedPleaValue(), is(IndicatedPleaValue.INDICATED_GUILTY));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getAllocationDecision(), notNullValue());

        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getId(), is(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getPlea(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId(), is(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId()));
        assertThat(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getPlea(), nullValue());
    }

    @Test
    public void ShouldRemovePleaForOffenceUnderCourtApplicationCourtOrders(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(Arrays.asList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(Offence.offence()
                                        .withId(offenceId)
                                        .withPlea(Plea.plea().build())
                                        .withIndicatedPlea(IndicatedPlea.indicatedPlea().build())
                                        .withAllocationDecision(AllocationDecision.allocationDecision().build())
                                        .build())
                                .build(),CourtOrderOffence.courtOrderOffence()
                                .withOffence(Offence.offence()
                                        .withId(randomUUID()).build())
                                .build()))
                        .build())
                .build());

        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(offenceId).build())
                                .build()))
                        .build())
                .build());

        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final PleaModel pleaModel =  PleaModel.pleaModel()
                .withApplicationId(courtAppId)
                .withOffenceId(offenceId).build();

        final String strResult = hearingJPAMapper.updatePleaOnOffencesInCourtApplication("",  pleaModel );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId(), is(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getPlea(), nullValue());
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getIndicatedPlea(), nullValue());
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getAllocationDecision(), nullValue());

        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getId(), is(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getPlea(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId(), is(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId()));
        assertThat(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getPlea(), nullValue());
    }

    @Test
    public void ShouldUpdateVerdictForOffenceUnderCourtApplicationCase(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        final LocalDate verdictDate = LocalDate.now();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(Arrays.asList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(offenceId).build(), uk.gov.justice.core.courts.Offence.offence()
                                .withId(randomUUID()).build()))
                        .build()))
                .build());
        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(singletonList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(randomUUID()).build()))
                        .build()))
                .build());

        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final Verdict verdict =  Verdict.verdict()
                .withApplicationId(courtAppId)
                .withOffenceId(offenceId)
                .withVerdictDate(verdictDate)
                .withVerdictType(VerdictType.verdictType().withId(randomUUID()).withCategoryType("test").withCategory("testCategory").build()).build();

        final String strResult = hearingJPAMapper.updateVerdictOnOffencesInCourtApplication("",  verdict );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getVerdict().getVerdictDate(), is(verdictDate));

        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getVerdict(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getVerdict(), nullValue());
    }

    @Test
    public void ShouldUpdateClearVerdictForOffenceUnderCourtApplicationCase(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID offenceId1 = randomUUID();
        final LocalDate verdictDate = LocalDate.now();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(Arrays.asList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(offenceId).build(), uk.gov.justice.core.courts.Offence.offence()
                                .withId(randomUUID()).build()))
                        .build()))
                .build());
        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtApplicationCases(singletonList(CourtApplicationCase.courtApplicationCase()
                        .withIsSJP(false)
                        .withCaseStatus("ACTIVE")
                        .withOffences(singletonList(uk.gov.justice.core.courts.Offence.offence()
                                .withId(offenceId1).build()))
                        .build()))
                .build());

        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final Verdict verdict =  Verdict.verdict()
                .withApplicationId(courtAppId)
                .withOffenceId(offenceId)
                .withVerdictDate(verdictDate)
                .withVerdictType(VerdictType.verdictType().withId(randomUUID()).withCategory("category").withCategoryType("category type").build())
                .withIsDeleted(true)
                .build();

        final String strResult = hearingJPAMapper.updateVerdictOnOffencesInCourtApplication("",  verdict );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(0).getVerdict(), nullValue());

        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId(), is(courtApplications.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getId()));
        assertThat(applicationList.get(0).getCourtApplicationCases().get(0).getOffences().get(1).getVerdict(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getIsSJP(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getIsSJP()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId(), is(courtApplications.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getId()));
        assertThat(applicationList.get(1).getCourtApplicationCases().get(0).getOffences().get(0).getVerdict(), nullValue());

    }

    @Test
    public void ShouldUpdateVerdictForOffenceUnderCourtApplicationCourtOrders(){
        final UUID courtAppId = randomUUID();
        final UUID offenceId = randomUUID();
        final LocalDate verdictDate = LocalDate.now();
        List<CourtApplication> courtApplications = new ArrayList<>();
        courtApplications.add(CourtApplication.courtApplication()
                .withId(courtAppId)
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(Arrays.asList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(offenceId).build())
                                .build(),CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(randomUUID()).build())
                                .build()))
                        .build())
                .build());

        courtApplications.add(CourtApplication.courtApplication()
                .withId(randomUUID())
                .withCourtOrder(CourtOrder.courtOrder()
                        .withCourtOrderOffences(singletonList(CourtOrderOffence.courtOrderOffence()
                                .withOffence(uk.gov.justice.core.courts.Offence.offence()
                                        .withId(offenceId).build())
                                .build()))
                        .build())
                .build());

        when(courtApplicationsSerializer.courtApplications(any(String.class))).thenReturn(courtApplications);
        when(courtApplicationsSerializer.json(any())).thenReturn("EFG");
        final Verdict verdict =  Verdict.verdict()
                .withApplicationId(courtAppId)
                .withOffenceId(offenceId)
                .withVerdictDate(verdictDate)
                .withVerdictType(VerdictType.verdictType().withId(randomUUID()).withCategoryType("test").withCategory("testCategory").build()).build();

        final String strResult = hearingJPAMapper.updateVerdictOnOffencesInCourtApplication("",  verdict );

        verify(courtApplicationsSerializer, times(1)).json(courtApplicationCaptor.capture());
        List<CourtApplication> applicationList = courtApplicationCaptor.getValue();

        assertThat(strResult, is("EFG"));
        assertThat(applicationList.get(0).getId(), is(courtApplications.get(0).getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId(), is(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getVerdict().getVerdictDate(), is(verdictDate));

        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getId(), is(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getId()));
        assertThat(applicationList.get(0).getCourtOrder().getCourtOrderOffences().get(1).getOffence().getVerdict(), nullValue());

        assertThat(applicationList.get(1).getId(), is(courtApplications.get(1).getId()));
        assertThat(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId(), is(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getId()));
        assertThat(applicationList.get(1).getCourtOrder().getCourtOrderOffences().get(0).getOffence().getVerdict(), nullValue());
    }


    @Test
    public void testFromJPA() {


        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearingEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing();
        hearingEntity.setId(randomUUID());
        hearingEntity.setCourtCentre(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre.class));
        hearingEntity.setDefendantAttendance(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance.class)));
        hearingEntity.setDefendantReferralReasons(asSet(mock(DefendantReferralReason.class)));
        hearingEntity.setHasSharedResults(RandomGenerator.BOOLEAN.next());
        hearingEntity.setHearingDays(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class)));
        hearingEntity.setHearingLanguage(RandomGenerator.values(HearingLanguage.values()).next());
        hearingEntity.setJudicialRoles(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole.class)));
        hearingEntity.setJurisdictionType(RandomGenerator.values(JurisdictionType.values()).next());
        hearingEntity.setProsecutionCases(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase.class)));
        hearingEntity.setReportingRestrictionReason(RandomGenerator.STRING.next());
        hearingEntity.setTargets(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.Target.class)));
        hearingEntity.setHearingType(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType.class));
        hearingEntity.setHearingCaseNotes(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote.class)));
        hearingEntity.setIsVacatedTrial(Boolean.TRUE);
        hearingEntity.setNumberOfGroupCases(NUMBER_OF_GROUP_CASES);
        hearingEntity.setApprovalsRequested(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested.class)));
        hearingEntity.setIsGroupProceedings(Boolean.TRUE);

        CourtCentre courtCentreMock = mock(CourtCentre.class);
        when(courtCentreJPAMapper.fromJPA(hearingEntity.getCourtCentre())).thenReturn(courtCentreMock);

        ReferralReason referralReasonMock = mock(ReferralReason.class);
        when(defendantReferralReasonsJPAMapper.fromJPA(hearingEntity.getDefendantReferralReasons())).thenReturn(asList(referralReasonMock));

        uk.gov.justice.core.courts.HearingDay hearingDayMock = mock(uk.gov.justice.core.courts.HearingDay.class);
        when(hearingDayJPAMapper.fromJPA(hearingEntity.getHearingDays())).thenReturn(asList(hearingDayMock));

        uk.gov.justice.core.courts.JudicialRole judicialRoleMock = mock(uk.gov.justice.core.courts.JudicialRole.class);
        when(judicialRoleJPAMapper.fromJPA(hearingEntity.getJudicialRoles())).thenReturn(asList(judicialRoleMock));

        ProsecutionCase prosecutionCaseMock = mock(ProsecutionCase.class);
        when(prosecutionCaseJPAMapper.fromJPA(hearingEntity.getProsecutionCases())).thenReturn(asList(prosecutionCaseMock));

        HearingType hearingTypeMock = mock(HearingType.class);
        when(hearingTypeJPAMapper.fromJPA(hearingEntity.getHearingType())).thenReturn(hearingTypeMock);

        DefendantAttendance defendantAttendanceMock = mock(DefendantAttendance.class);
        when(defendantAttendanceJPAMapper.fromJPA(hearingEntity.getDefendantAttendance())).thenReturn(asList(defendantAttendanceMock));

        HearingCaseNote hearingCaseNoteMock = mock(HearingCaseNote.class);
        when(hearingCaseNoteJPAMapper.fromJPA(hearingEntity.getHearingCaseNotes())).thenReturn(asList(hearingCaseNoteMock));

        ProsecutionCounsel prosecutionCounselMock = mock(ProsecutionCounsel.class);
        when(hearingProsecutionCounselJPAMapper.fromJPA(hearingEntity.getProsecutionCounsels())).thenReturn(asList(prosecutionCounselMock));

        RespondentCounsel respondentCounselMock = mock(RespondentCounsel.class);
        when(hearingRespondentCounselJPAMapper.fromJPA(hearingEntity.getRespondentCounsels())).thenReturn(asList(respondentCounselMock));

        ApplicantCounsel applicantCounselMock = mock(ApplicantCounsel.class);
        when(hearingApplicantCounselJPAMapper.fromJPA(hearingEntity.getApplicantCounsels())).thenReturn(asList(applicantCounselMock));

        DefenceCounsel defenceCounselMock = mock(DefenceCounsel.class);
        when(defenceCounselJPAMapper.fromJPA(hearingEntity.getDefenceCounsels())).thenReturn(asList(defenceCounselMock));

        InterpreterIntermediary interpreterIntermediaryMock = mock(InterpreterIntermediary.class);
        when(hearingInterpreterIntermediaryJPAMapper.fromJPA(hearingEntity.getHearingInterpreterIntermediaries())).thenReturn(asList(interpreterIntermediaryMock));

        CompanyRepresentative companyRepresentativeMock = mock(CompanyRepresentative.class);
        when(hearingCompanyRepresentativeJPAMapper.fromJPA(hearingEntity.getCompanyRepresentatives())).thenReturn(asList(companyRepresentativeMock));

        ApprovalRequest approvalRequestMock = mock(ApprovalRequest.class);
        when(approvalRequestedJPAMapper.fromJPA(hearingEntity.getApprovalsRequested())).thenReturn(asList(approvalRequestMock));

        final List<CourtApplication> expectedCourtApplications = asList();
        when(courtApplicationsSerializer.courtApplications(hearingEntity.getCourtApplicationsJson())).thenReturn(expectedCourtApplications);

        assertThat(hearingJPAMapper.fromJPA(hearingEntity), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingEntity.getId()))
                .with(Hearing::getCourtCentre, is(courtCentreMock))
                .with(Hearing::getDefendantReferralReasons, first(is(referralReasonMock)))
                .with(Hearing::getHasSharedResults, is(hearingEntity.getHasSharedResults()))
                .with(Hearing::getHearingDays, first(is(hearingDayMock)))
                .with(Hearing::getHearingLanguage, is(hearingEntity.getHearingLanguage()))
                .with(Hearing::getJudiciary, first(is(judicialRoleMock)))
                .with(Hearing::getJurisdictionType, is(hearingEntity.getJurisdictionType()))
                .with(Hearing::getProsecutionCases, first(is(prosecutionCaseMock)))
                .with(Hearing::getReportingRestrictionReason, is(hearingEntity.getReportingRestrictionReason()))
                .with(Hearing::getType, is(hearingTypeMock))
                .with(Hearing::getDefendantAttendance, first(is(defendantAttendanceMock)))
                .with(Hearing::getHearingCaseNotes, first(is(hearingCaseNoteMock)))
                .with(Hearing::getApprovalsRequested, first(is(approvalRequestMock)))

                .withValue(Hearing::getCourtApplications, null)
                .with(Hearing::getIsVacatedTrial, is(Boolean.TRUE))
                .with(Hearing::getIsGroupProceedings, is(Boolean.TRUE))
                .with(Hearing::getNumberOfGroupCases, is(NUMBER_OF_GROUP_CASES))
        );
    }

    @Test
    public void testToJPA() {

        Hearing hearing = Hearing.hearing()
                .withId(randomUUID())
                .withCourtCentre(mock(CourtCentre.class))
                .withDefendantAttendance(asList(mock(DefendantAttendance.class)))
                .withDefendantReferralReasons(asList(mock(ReferralReason.class)))
                .withHasSharedResults(RandomGenerator.BOOLEAN.next())
                .withHearingDays(asList(mock(uk.gov.justice.core.courts.HearingDay.class)))
                .withHearingLanguage(RandomGenerator.values(HearingLanguage.values()).next())
                .withJudiciary(asList(mock(uk.gov.justice.core.courts.JudicialRole.class)))
                .withJurisdictionType(RandomGenerator.values(JurisdictionType.values()).next())
                .withProsecutionCases(asList(mock(ProsecutionCase.class)))
                .withReportingRestrictionReason(RandomGenerator.STRING.next())
                .withType(mock(HearingType.class))
                .withDefendantAttendance(asList(mock(DefendantAttendance.class)))
                .withHearingCaseNotes(asList(mock(HearingCaseNote.class)))
                .withCourtApplications(asList())
                .withIsVacatedTrial(Boolean.FALSE)
                .withApprovalsRequested(asList(mock(ApprovalRequest.class)))
                .withIsGroupProceedings(Boolean.TRUE)
                .build();


        uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre courtCentreMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre.class);
        when(courtCentreJPAMapper.toJPA(hearing.getCourtCentre())).thenReturn(courtCentreMock);

        DefendantReferralReason referralReasonMock = mock(DefendantReferralReason.class);
        when(defendantReferralReasonsJPAMapper.toJPA(any(), eq(hearing.getDefendantReferralReasons()))).thenReturn(asSet(referralReasonMock));

        HearingDay hearingDayMock = mock(HearingDay.class);
        when(hearingDayJPAMapper.toJPA(any(), eq(hearing.getHearingDays()))).thenReturn(asSet(hearingDayMock));

        JudicialRole judicialRole = mock(JudicialRole.class);
        when(judicialRoleJPAMapper.toJPA(any(), eq(hearing.getJudiciary()))).thenReturn(asSet(judicialRole));

        uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCaseMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase.class);
        when(prosecutionCaseJPAMapper.toJPA(any(), eq(hearing.getProsecutionCases()))).thenReturn(asSet(prosecutionCaseMock));

        uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel hearingProsecutionCounselCaseMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel.class);
        when(hearingProsecutionCounselJPAMapper.toJPA(any(), eq(hearing.getProsecutionCounsels()))).thenReturn(asSet(hearingProsecutionCounselCaseMock));

        uk.gov.moj.cpp.hearing.persist.entity.ha.Target targetMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.Target.class);

        uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType hearingTypeMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType.class);
        when(hearingTypeJPAMapper.toJPA(hearing.getType())).thenReturn(hearingTypeMock);

        uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance defendantAttendanceMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance.class);
        when(defendantAttendanceJPAMapper.toJPA(hearing.getDefendantAttendance())).thenReturn(asSet(defendantAttendanceMock));

        uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote hearingCaseNoteMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote.class);
        when(hearingCaseNoteJPAMapper.toJPA(any(), eq(hearing.getHearingCaseNotes()))).thenReturn(asSet(hearingCaseNoteMock));

        final String expectedCourtApplicationsJson = "**expectedCourtApplicationsJson**";
        when(courtApplicationsSerializer.json(hearing.getCourtApplications())).thenReturn(expectedCourtApplicationsJson);

        uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested approvalRequestedMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested.class);
        when(approvalRequestedJPAMapper.toJPA(eq(hearing.getApprovalsRequested()))).thenReturn(asSet(approvalRequestedMock));

        assertThat(hearingJPAMapper.toJPA(hearing), isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing.class)
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getId, is(hearing.getId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getCourtCentre, is(courtCentreMock))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getDefendantReferralReasons, first(is(referralReasonMock)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHasSharedResults, is(hearing.getHasSharedResults()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingDays, first(is(hearingDayMock)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingLanguage, is(hearing.getHearingLanguage()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getJudicialRoles, first(is(judicialRole)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getProsecutionCases, first(is(prosecutionCaseMock)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getReportingRestrictionReason, is(hearing.getReportingRestrictionReason()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingType, is(hearingTypeMock))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getDefendantAttendance, first(is(defendantAttendanceMock)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingCaseNotes, first(is(hearingCaseNoteMock)))
                .withValue(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getCourtApplicationsJson, expectedCourtApplicationsJson)
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getIsVacatedTrial, is(Boolean.FALSE))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getIsGroupProceedings, is(Boolean.TRUE))
        );
    }

    @Test
    public void testFromJPAWithCourtListRestrictionsForCase() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearingEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing();
        hearingEntity.setId(randomUUID());
        hearingEntity.setCourtCentre(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre.class));
        hearingEntity.setDefendantAttendance(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance.class)));
        hearingEntity.setHearingDays(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class)));
        hearingEntity.setHearingLanguage(RandomGenerator.values(HearingLanguage.values()).next());
        hearingEntity.setJudicialRoles(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole.class)));
        hearingEntity.setJurisdictionType(RandomGenerator.values(JurisdictionType.values()).next());

        final uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase prosecutionCase = new uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase();
        prosecutionCase.setCourtListRestricted(true);
        hearingEntity.setProsecutionCases(asSet(prosecutionCase));

        hearingEntity.setReportingRestrictionReason(RandomGenerator.STRING.next());
        hearingEntity.setTargets(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.Target.class)));
        hearingEntity.setHearingType(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType.class));
        hearingEntity.setHearingCaseNotes(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote.class)));
        hearingEntity.setIsVacatedTrial(Boolean.TRUE);
        hearingEntity.setApprovalsRequested(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.ApprovalRequested.class)));

        CourtCentre courtCentreMock = mock(CourtCentre.class);
        when(courtCentreJPAMapper.fromJPA(hearingEntity.getCourtCentre())).thenReturn(courtCentreMock);

        ReferralReason referralReasonMock = mock(ReferralReason.class);
        when(defendantReferralReasonsJPAMapper.fromJPA(hearingEntity.getDefendantReferralReasons())).thenReturn(asList(referralReasonMock));

        uk.gov.justice.core.courts.HearingDay hearingDayMock = mock(uk.gov.justice.core.courts.HearingDay.class);
        when(hearingDayJPAMapper.fromJPA(hearingEntity.getHearingDays())).thenReturn(asList(hearingDayMock));

        uk.gov.justice.core.courts.JudicialRole judicialRoleMock = mock(uk.gov.justice.core.courts.JudicialRole.class);
        when(judicialRoleJPAMapper.fromJPA(hearingEntity.getJudicialRoles())).thenReturn(asList(judicialRoleMock));

        HearingType hearingTypeMock = mock(HearingType.class);
        when(hearingTypeJPAMapper.fromJPA(hearingEntity.getHearingType())).thenReturn(hearingTypeMock);

        DefendantAttendance defendantAttendanceMock = mock(DefendantAttendance.class);
        when(defendantAttendanceJPAMapper.fromJPA(hearingEntity.getDefendantAttendance())).thenReturn(asList(defendantAttendanceMock));

        HearingCaseNote hearingCaseNoteMock = mock(HearingCaseNote.class);
        when(hearingCaseNoteJPAMapper.fromJPA(hearingEntity.getHearingCaseNotes())).thenReturn(asList(hearingCaseNoteMock));

        ProsecutionCounsel prosecutionCounselMock = mock(ProsecutionCounsel.class);
        when(hearingProsecutionCounselJPAMapper.fromJPA(hearingEntity.getProsecutionCounsels())).thenReturn(asList(prosecutionCounselMock));

        RespondentCounsel respondentCounselMock = mock(RespondentCounsel.class);
        when(hearingRespondentCounselJPAMapper.fromJPA(hearingEntity.getRespondentCounsels())).thenReturn(asList(respondentCounselMock));

        ApplicantCounsel applicantCounselMock = mock(ApplicantCounsel.class);
        when(hearingApplicantCounselJPAMapper.fromJPA(hearingEntity.getApplicantCounsels())).thenReturn(asList(applicantCounselMock));

        DefenceCounsel defenceCounselMock = mock(DefenceCounsel.class);
        when(defenceCounselJPAMapper.fromJPA(hearingEntity.getDefenceCounsels())).thenReturn(asList(defenceCounselMock));

        InterpreterIntermediary interpreterIntermediaryMock = mock(InterpreterIntermediary.class);
        when(hearingInterpreterIntermediaryJPAMapper.fromJPA(hearingEntity.getHearingInterpreterIntermediaries())).thenReturn(asList(interpreterIntermediaryMock));

        CompanyRepresentative companyRepresentativeMock = mock(CompanyRepresentative.class);
        when(hearingCompanyRepresentativeJPAMapper.fromJPA(hearingEntity.getCompanyRepresentatives())).thenReturn(asList(companyRepresentativeMock));

        ApprovalRequest approvalRequestMock = mock(ApprovalRequest.class);
        when(approvalRequestedJPAMapper.fromJPA(hearingEntity.getApprovalsRequested())).thenReturn(asList(approvalRequestMock));

        final List<CourtApplication> expectedCourtApplications = asList();
        when(courtApplicationsSerializer.courtApplications(hearingEntity.getCourtApplicationsJson())).thenReturn(expectedCourtApplications);

        assertThat(hearingJPAMapper.fromJPAWithCourtListRestrictions(hearingEntity), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingEntity.getId()))
                .with(Hearing::getCourtCentre, is(courtCentreMock))
                .with(Hearing::getDefendantReferralReasons, first(is(referralReasonMock)))
                .with(Hearing::getHasSharedResults, is(hearingEntity.getHasSharedResults()))
                .with(Hearing::getHearingDays, first(is(hearingDayMock)))
                .with(Hearing::getHearingLanguage, is(hearingEntity.getHearingLanguage()))
                .with(Hearing::getJudiciary, first(is(judicialRoleMock)))
                .with(Hearing::getJurisdictionType, is(hearingEntity.getJurisdictionType()))
                .withValue(Hearing::getProsecutionCases, new ArrayList())
                .with(Hearing::getReportingRestrictionReason, is(hearingEntity.getReportingRestrictionReason()))
                .with(Hearing::getType, is(hearingTypeMock))
                .with(Hearing::getDefendantAttendance, first(is(defendantAttendanceMock)))
                .with(Hearing::getHearingCaseNotes, first(is(hearingCaseNoteMock)))
                .with(Hearing::getApprovalsRequested, first(is(approvalRequestMock)))
                .withValue(Hearing::getCourtApplications, null)
                .with(Hearing::getIsVacatedTrial, is(Boolean.TRUE))
        );
    }
}
