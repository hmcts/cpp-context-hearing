package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.ApplicantCounsel;
import uk.gov.justice.core.courts.CompanyRepresentative;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.core.courts.DefendantAttendance;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingCaseNote;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.ReferralReason;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingJPAMapperTest {

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

    @Captor
    private ArgumentCaptor<List<CourtApplication>> courtApplicationCaptor;

    @InjectMocks
    private HearingJPAMapper hearingJPAMapper;

    @Test
    public void testInsertCourtApplications() {
        final CourtApplication courtApplicationUpdate = CourtApplication.courtApplication().withId(UUID.randomUUID()).build();
        final List<CourtApplication> existingCourtApplications = asList(
                CourtApplication.courtApplication().withId(UUID.randomUUID()).build(),
                CourtApplication.courtApplication().withId(UUID.randomUUID()).build()
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
        final CourtApplication courtApplicationUpdate = CourtApplication.courtApplication().withId(UUID.randomUUID()).build();
        final List<CourtApplication> existingCourtApplications = asList(
                CourtApplication.courtApplication().withId(courtApplicationUpdate.getId()).build(),
                CourtApplication.courtApplication().withId(UUID.randomUUID()).build()
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
    public void testFromJPA() {


        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearingEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing();
        hearingEntity.setId(UUID.randomUUID());
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

        Target targetMock = mock(Target.class);
        when(targetJPAMapper.fromJPA(hearingEntity.getTargets(), emptySet())).thenReturn(asList(targetMock));

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
                .withValue(Hearing::getCourtApplications, null)
        );
    }

    @Test
    public void testToJPA() {

        Hearing hearing = Hearing.hearing()
                .withId(UUID.randomUUID())
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
        );
    }

}
