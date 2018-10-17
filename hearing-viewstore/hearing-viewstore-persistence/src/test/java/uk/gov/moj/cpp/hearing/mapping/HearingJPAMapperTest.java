package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.justice.json.schemas.core.DefendantAttendance;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.HearingLanguage;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JurisdictionType;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.json.schemas.core.ReferralReason;
import uk.gov.justice.json.schemas.core.Target;
import uk.gov.justice.json.schemas.core.HearingCaseNote;
import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class HearingJPAMapperTest {

    @Mock
    private CourtCentreJPAMapper courtCentreJPAMapper;
    @Mock
    private DefenceCounselJPAMapper defenceCounselJPAMapper;
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
    private ProsecutionCounselJPAMapper prosecutionCounselJPAMapper;
    @Mock
    private TargetJPAMapper targetJPAMapper;
    @Mock
    private HearingTypeJPAMapper hearingTypeJPAMapper;

    @InjectMocks
    private HearingJPAMapper hearingJPAMapper;

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
        hearingEntity.setDefendantAttendance(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance.class)));
        hearingEntity.setHearingCaseNotes(asSet(mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote.class)));

        CourtCentre courtCentreMock = mock(CourtCentre.class);
        when(courtCentreJPAMapper.fromJPA(hearingEntity.getCourtCentre())).thenReturn(courtCentreMock);

        ReferralReason referralReasonMock = mock(ReferralReason.class);
        when(defendantReferralReasonsJPAMapper.fromJPA(hearingEntity.getDefendantReferralReasons())).thenReturn(asList(referralReasonMock));

        uk.gov.justice.json.schemas.core.HearingDay hearingDayMock = mock(uk.gov.justice.json.schemas.core.HearingDay.class);
        when(hearingDayJPAMapper.fromJPA(hearingEntity.getHearingDays())).thenReturn(asList(hearingDayMock));

        uk.gov.justice.json.schemas.core.JudicialRole judicialRoleMock = mock(uk.gov.justice.json.schemas.core.JudicialRole.class);
        when(judicialRoleJPAMapper.fromJPA(hearingEntity.getJudicialRoles())).thenReturn(asList(judicialRoleMock));

        ProsecutionCase prosecutionCaseMock = mock(ProsecutionCase.class);
        when(prosecutionCaseJPAMapper.fromJPA(hearingEntity.getProsecutionCases())).thenReturn(asList(prosecutionCaseMock));

        Target targetMock = mock(Target.class);
        when(targetJPAMapper.fromJPA(hearingEntity.getTargets())).thenReturn(asList(targetMock));

        HearingType hearingTypeMock = mock(HearingType.class);
        when(hearingTypeJPAMapper.fromJPA(hearingEntity.getHearingType())).thenReturn(hearingTypeMock);

        DefendantAttendance defendantAttendanceMock = mock(DefendantAttendance.class);
        when(defendantAttendanceJPAMapper.fromJPA(hearingEntity.getDefendantAttendance())).thenReturn(asList(defendantAttendanceMock));

        HearingCaseNote hearingCaseNoteMock = mock(HearingCaseNote.class);
        when(hearingCaseNoteJPAMapper.fromJPA(hearingEntity.getHearingCaseNotes())).thenReturn(asList(hearingCaseNoteMock));

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
                .with(Hearing::getTargets, first(is(targetMock)))
                .with(Hearing::getType, is(hearingTypeMock))
                .with(Hearing::getDefendantAttendance, first(is(defendantAttendanceMock)))
                .with(Hearing::getHearingCaseNotes, first(is(hearingCaseNoteMock)))
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
                .withHearingDays(asList(mock(uk.gov.justice.json.schemas.core.HearingDay.class)))
                .withHearingLanguage(RandomGenerator.values(HearingLanguage.values()).next())
                .withJudiciary(asList(mock(uk.gov.justice.json.schemas.core.JudicialRole.class)))
                .withJurisdictionType(RandomGenerator.values(JurisdictionType.values()).next())
                .withProsecutionCases(asList(mock(ProsecutionCase.class)))
                .withReportingRestrictionReason(RandomGenerator.STRING.next())
                .withTargets(asList(mock(Target.class)))
                .withType(mock(HearingType.class))
                .withDefendantAttendance(asList(mock(DefendantAttendance.class)))
                .withHearingCaseNotes(asList(mock(HearingCaseNote.class)))
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

        uk.gov.moj.cpp.hearing.persist.entity.ha.Target targetMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.Target.class);
        when(targetJPAMapper.toJPA(any(), eq(hearing.getTargets()))).thenReturn(asSet(targetMock));

        uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType hearingTypeMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType.class);
        when(hearingTypeJPAMapper.toJPA(hearing.getType())).thenReturn(hearingTypeMock);

        uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance defendantAttendanceMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance.class);
        when(defendantAttendanceJPAMapper.toJPA(hearing.getDefendantAttendance())).thenReturn(asSet(defendantAttendanceMock));

        uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote hearingCaseNoteMock = mock(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote.class);
        when(hearingCaseNoteJPAMapper.toJPA(any(), eq(hearing.getHearingCaseNotes()))).thenReturn(asSet(hearingCaseNoteMock));

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
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getTargets, first(is(targetMock)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingType, is(hearingTypeMock))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getDefendantAttendance, first(is(defendantAttendanceMock)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingCaseNotes, first(is(hearingCaseNoteMock)))
        );
    }

}