package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Optional.ofNullable;

import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@SuppressWarnings("squid:S00107")
@ApplicationScoped
public class HearingJPAMapper {

    private CourtCentreJPAMapper courtCentreJPAMapper;
    private HearingDefenceCounselJPAMapper hearingDefenceCounselJPAMapper ;
    private DefendantAttendanceJPAMapper defendantAttendanceJPAMapper;
    private DefendantReferralReasonJPAMapper defendantReferralReasonsJPAMapper;
    private HearingCaseNoteJPAMapper hearingCaseNoteJPAMapper;
    private HearingDayJPAMapper hearingDayJPAMapper;
    private JudicialRoleJPAMapper judicialRoleJPAMapper;
    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper;
    private HearingProsecutionCounselJPAMapper hearingProsecutionCounselJPAMapper;
    private TargetJPAMapper targetJPAMapper;
    private HearingTypeJPAMapper hearingTypeJPAMapper;

    @Inject
    public HearingJPAMapper(final CourtCentreJPAMapper courtCentreJPAMapper,
                            final HearingDefenceCounselJPAMapper hearingDefenceCounselJPAMapper,
                            final DefendantAttendanceJPAMapper defendantAttendanceJPAMapper,
                            final DefendantReferralReasonJPAMapper defendantReferralReasonsJPAMapper,
                            final HearingCaseNoteJPAMapper hearingCaseNoteJPAMapper,
                            final HearingDayJPAMapper hearingDayJPAMapper,
                            final JudicialRoleJPAMapper judicialRoleJPAMapper,
                            final ProsecutionCaseJPAMapper prosecutionCaseJPAMapper,
                            final HearingProsecutionCounselJPAMapper hearingProsecutionCounselJPAMapper,
                            final TargetJPAMapper targetJPAMapper,
                            final HearingTypeJPAMapper hearingTypeJPAMapper) {
        this.courtCentreJPAMapper = courtCentreJPAMapper;
        this.hearingDefenceCounselJPAMapper = hearingDefenceCounselJPAMapper ;
        this.defendantAttendanceJPAMapper = defendantAttendanceJPAMapper;
        this.defendantReferralReasonsJPAMapper = defendantReferralReasonsJPAMapper;
        this.hearingCaseNoteJPAMapper = hearingCaseNoteJPAMapper;
        this.hearingDayJPAMapper = hearingDayJPAMapper;
        this.judicialRoleJPAMapper = judicialRoleJPAMapper;
        this.prosecutionCaseJPAMapper = prosecutionCaseJPAMapper;
        this.hearingProsecutionCounselJPAMapper = hearingProsecutionCounselJPAMapper;
        this.targetJPAMapper = targetJPAMapper;
        this.hearingTypeJPAMapper = hearingTypeJPAMapper;
    }

    //to keep cditester happy
    public HearingJPAMapper() {
    }

    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Hearing toJPA(final uk.gov.justice.core.courts.Hearing pojo) {
        if (null == pojo) {
            return null;
        }
        final Hearing hearing = new Hearing();
        hearing.setId(pojo.getId());
        hearing.setCourtCentre(courtCentreJPAMapper.toJPA(pojo.getCourtCentre()));
        hearing.setDefenceCounsels(hearingDefenceCounselJPAMapper.toJPA(hearing, pojo.getDefenceCounsels()));
        hearing.setDefendantAttendance(defendantAttendanceJPAMapper.toJPA(pojo.getDefendantAttendance()));
        hearing.setDefendantReferralReasons(defendantReferralReasonsJPAMapper.toJPA(hearing, pojo.getDefendantReferralReasons()));
        hearing.setHasSharedResults(pojo.getHasSharedResults());
        hearing.setHearingCaseNotes(hearingCaseNoteJPAMapper.toJPA(hearing, pojo.getHearingCaseNotes()));
        hearing.setHearingDays(hearingDayJPAMapper.toJPA(hearing, pojo.getHearingDays()));
        hearing.setHearingLanguage(ofNullable(pojo.getHearingLanguage()).orElse(HearingLanguage.ENGLISH));
        hearing.setJudicialRoles(judicialRoleJPAMapper.toJPA(hearing, pojo.getJudiciary()));
        hearing.setJurisdictionType(pojo.getJurisdictionType());
        hearing.setProsecutionCases(prosecutionCaseJPAMapper.toJPA(hearing, pojo.getProsecutionCases()));
        hearing.setProsecutionCounsels(hearingProsecutionCounselJPAMapper.toJPA(hearing, pojo.getProsecutionCounsels()));
        hearing.setReportingRestrictionReason(pojo.getReportingRestrictionReason());
        hearing.setTargets(targetJPAMapper.toJPA(hearing, pojo.getTargets()));
        hearing.setHearingType(hearingTypeJPAMapper.toJPA(pojo.getType()));
        return hearing;
    }

    public uk.gov.justice.core.courts.Hearing fromJPA(final Hearing entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.Hearing.hearing()
                .withId(entity.getId())
                .withCourtCentre(courtCentreJPAMapper.fromJPA(entity.getCourtCentre()))
                .withDefenceCounsels(hearingDefenceCounselJPAMapper.fromJPA(entity.getDefenceCounsels()))
                .withDefendantAttendance(defendantAttendanceJPAMapper.fromJPA(entity.getDefendantAttendance()))
                .withDefendantReferralReasons(defendantReferralReasonsJPAMapper.fromJPA(entity.getDefendantReferralReasons()))
                .withHasSharedResults(entity.getHasSharedResults())
                .withHearingCaseNotes(hearingCaseNoteJPAMapper.fromJPA(entity.getHearingCaseNotes()))
                .withHearingDays(hearingDayJPAMapper.fromJPA(entity.getHearingDays()))
                .withHearingLanguage(entity.getHearingLanguage())
                .withJudiciary(judicialRoleJPAMapper.fromJPA(entity.getJudicialRoles()))
                .withJurisdictionType(entity.getJurisdictionType())
                .withProsecutionCases(prosecutionCaseJPAMapper.fromJPA(entity.getProsecutionCases()))
                .withProsecutionCounsels(hearingProsecutionCounselJPAMapper.fromJPA(entity.getProsecutionCounsels()))
                .withReportingRestrictionReason(entity.getReportingRestrictionReason())
                .withTargets(targetJPAMapper.fromJPA(entity.getTargets()))
                .withType(hearingTypeJPAMapper.fromJPA(entity.getHearingType()))
                .withDefendantAttendance(defendantAttendanceJPAMapper.fromJPA(entity.getDefendantAttendance()))
                .build();
    }
}