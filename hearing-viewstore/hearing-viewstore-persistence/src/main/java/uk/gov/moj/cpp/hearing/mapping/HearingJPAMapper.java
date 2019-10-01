package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.deltaspike.core.util.CollectionUtils.isEmpty;
import static uk.gov.justice.core.courts.ApplicationStatus.EJECTED;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationOutcome;
import uk.gov.justice.core.courts.CourtApplicationResponse;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@SuppressWarnings({"squid:S00107", "squid:S3655"})
@ApplicationScoped
public class HearingJPAMapper {

    private CourtCentreJPAMapper courtCentreJPAMapper;
    private HearingDefenceCounselJPAMapper hearingDefenceCounselJPAMapper;
    private DefendantAttendanceJPAMapper defendantAttendanceJPAMapper;
    private DefendantReferralReasonJPAMapper defendantReferralReasonsJPAMapper;
    private HearingCaseNoteJPAMapper hearingCaseNoteJPAMapper;
    private HearingDayJPAMapper hearingDayJPAMapper;
    private JudicialRoleJPAMapper judicialRoleJPAMapper;
    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper;
    private HearingProsecutionCounselJPAMapper hearingProsecutionCounselJPAMapper;
    private HearingTypeJPAMapper hearingTypeJPAMapper;
    private CourtApplicationsSerializer courtApplicationsSerializer;
    private HearingRespondentCounselJPAMapper hearingRespondentCounselJPAMapper;
    private HearingApplicantCounselJPAMapper hearingApplicantCounselJPAMapper;
    private HearingInterpreterIntermediaryJPAMapper hearingInterpreterIntermediaryJPAMapper;
    private HearingCompanyRepresentativeJPAMapper hearingCompanyRepresentativeJPAMapper;

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
                            final HearingTypeJPAMapper hearingTypeJPAMapper,
                            final CourtApplicationsSerializer courtApplicationsSerializer,
                            final HearingRespondentCounselJPAMapper hearingRespondentCounselJPAMapper,
                            final HearingApplicantCounselJPAMapper hearingApplicantCounselJPAMapper,
                            final HearingInterpreterIntermediaryJPAMapper hearingInterpreterIntermediaryJPAMapper,
                            final HearingCompanyRepresentativeJPAMapper hearingCompanyRepresentativeJPAMapper) {
        this.courtCentreJPAMapper = courtCentreJPAMapper;
        this.hearingDefenceCounselJPAMapper = hearingDefenceCounselJPAMapper;
        this.defendantAttendanceJPAMapper = defendantAttendanceJPAMapper;
        this.defendantReferralReasonsJPAMapper = defendantReferralReasonsJPAMapper;
        this.hearingCaseNoteJPAMapper = hearingCaseNoteJPAMapper;
        this.hearingDayJPAMapper = hearingDayJPAMapper;
        this.judicialRoleJPAMapper = judicialRoleJPAMapper;
        this.prosecutionCaseJPAMapper = prosecutionCaseJPAMapper;
        this.hearingProsecutionCounselJPAMapper = hearingProsecutionCounselJPAMapper;
        this.hearingTypeJPAMapper = hearingTypeJPAMapper;
        this.courtApplicationsSerializer = courtApplicationsSerializer;
        this.hearingRespondentCounselJPAMapper = hearingRespondentCounselJPAMapper;
        this.hearingApplicantCounselJPAMapper = hearingApplicantCounselJPAMapper;
        this.hearingInterpreterIntermediaryJPAMapper = hearingInterpreterIntermediaryJPAMapper;
        this.hearingCompanyRepresentativeJPAMapper = hearingCompanyRepresentativeJPAMapper;
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
        hearing.setHearingInterpreterIntermediaries(hearingInterpreterIntermediaryJPAMapper.toJPA(hearing, pojo.getIntermediaries()));
        hearing.setReportingRestrictionReason(pojo.getReportingRestrictionReason());
        hearing.setHearingType(hearingTypeJPAMapper.toJPA(pojo.getType()));
        hearing.setCourtApplicationsJson(courtApplicationsSerializer.json(pojo.getCourtApplications()));
        return hearing;
    }

    public uk.gov.justice.core.courts.Hearing fromJPA(final Hearing entity) {
        if (null == entity) {
            return null;
        }
        List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(entity.getCourtApplicationsJson());
        if(!isEmpty(courtApplications)){
            courtApplications = courtApplications.stream().filter(ca -> !EJECTED.equals(ca.getApplicationStatus())).collect(toList());
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
                .withType(hearingTypeJPAMapper.fromJPA(entity.getHearingType()))
                .withDefendantAttendance(defendantAttendanceJPAMapper.fromJPA(entity.getDefendantAttendance()))
                .withCourtApplications(courtApplications == null || courtApplications.isEmpty() ? null : courtApplications)
                .withRespondentCounsels(hearingRespondentCounselJPAMapper.fromJPA(entity.getRespondentCounsels()))
                .withApplicantCounsels(hearingApplicantCounselJPAMapper.fromJPA(entity.getApplicantCounsels()))
                .withIntermediaries(hearingInterpreterIntermediaryJPAMapper.fromJPA(entity.getHearingInterpreterIntermediaries()))
                .withCompanyRepresentatives(hearingCompanyRepresentativeJPAMapper.fromJPA(entity.getCompanyRepresentatives()))
                .build();
    }

    public String addOrUpdateCourtApplication(final String courtApplicationsJson, final CourtApplication courtApplicationUpdate) {
        List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        if (courtApplications == null) {
            courtApplications = emptyList();
        }
        courtApplications = courtApplications.stream().filter(
                ca -> !ca.getId().equals(courtApplicationUpdate.getId())
        ).collect(toList());
        courtApplications.add(courtApplicationUpdate);
        return courtApplicationsSerializer.json(courtApplications);
    }

    public String saveApplicationResponse(final String courtApplicationsJson, final CourtApplicationResponse courtApplicationResponse, final UUID applicationPartyId) {
        List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        if (courtApplications == null) {
            courtApplications = emptyList();
        }

        Optional<CourtApplication> courtApplication = courtApplications.stream().filter(
                ca -> ca.getId().equals(courtApplicationResponse.getApplicationId())
        ).findFirst();

        if (courtApplication.isPresent()) {
            //The Admitted / denied flag should be only passed once for each application.
            courtApplication.get().getRespondents().stream().filter(r -> r.getPartyDetails().getId().equals(applicationPartyId)).findFirst().get().setApplicationResponse(courtApplicationResponse);
        }
        return courtApplicationsSerializer.json(courtApplications);
    }

    public String saveApplicationOutcome(final String courtApplicationsJson, final CourtApplicationOutcome courtApplicationOutcome) {
        List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        if (courtApplications == null) {
            courtApplications = emptyList();
        }

        Optional<CourtApplication> courtApplication = courtApplications.stream().filter(
                ca -> ca.getId().equals(courtApplicationOutcome.getApplicationId())
        ).findFirst();

        courtApplication.ifPresent(application -> application.setApplicationOutcome(courtApplicationOutcome));

        return courtApplicationsSerializer.json(courtApplications);
    }

    public String updateLinkedApplicationStatus(final String courtApplicationsJson, final UUID prosecutionCaseId, final ApplicationStatus status) {
        List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        if (courtApplications == null) {
            courtApplications = emptyList();
        }
        courtApplications.stream().filter(
                ca -> prosecutionCaseId.equals(ca.getLinkedCaseId())).forEach(ca -> ca.setApplicationStatus(status));
        return courtApplicationsSerializer.json(courtApplications);
    }

    public String updateStandaloneApplicationStatus(final String courtApplicationsJson, final UUID applicationId, final ApplicationStatus status) {
        List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        if (courtApplications == null) {
            courtApplications = emptyList();
        }
        courtApplications.stream().filter(
                ca -> ca.getId().equals(applicationId) ||
                        applicationId.equals(ca.getParentApplicationId())).forEach(ca -> ca.setApplicationStatus(status));
        return courtApplicationsSerializer.json(courtApplications);
    }
}
