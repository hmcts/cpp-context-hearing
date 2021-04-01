package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.deltaspike.core.util.CollectionUtils.isEmpty;
import static uk.gov.justice.core.courts.ApplicationStatus.EJECTED;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.CourtOrder;
import uk.gov.justice.core.courts.CourtOrderOffence;
import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Plea;
import uk.gov.justice.core.courts.Verdict;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@SuppressWarnings({"squid:S00107", "squid:S3655", "squid:CommentedOutCodeLine", "squid:S1172"})
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
    private ApprovalRequestedJPAMapper approvalRequestedJPAMapper;

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
                            final HearingCompanyRepresentativeJPAMapper hearingCompanyRepresentativeJPAMapper,
                            final ApprovalRequestedJPAMapper approvalRequestedJPAMapper) {
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
        this.approvalRequestedJPAMapper = approvalRequestedJPAMapper;
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
        hearing.setIsBoxHearing(pojo.getIsBoxHearing());
        hearing.setIsVacatedTrial(pojo.getIsVacatedTrial());
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
        hearing.setApprovalsRequested(approvalRequestedJPAMapper.toJPA(pojo.getApprovalsRequested()));
        return hearing;
    }

    public uk.gov.justice.core.courts.Hearing fromJPA(final Hearing entity) {
        if (null == entity) {
            return null;
        }
        List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(entity.getCourtApplicationsJson());
        if (!isEmpty(courtApplications)) {
            courtApplications = courtApplications.stream().filter(ca -> !EJECTED.equals(ca.getApplicationStatus())).collect(toList());
        }
        return uk.gov.justice.core.courts.Hearing.hearing()
                .withId(entity.getId())
                .withCourtCentre(courtCentreJPAMapper.fromJPA(entity.getCourtCentre()))
                .withDefenceCounsels(hearingDefenceCounselJPAMapper.fromJPA(entity.getDefenceCounsels()))
                .withDefendantAttendance(defendantAttendanceJPAMapper.fromJPA(entity.getDefendantAttendance()))
                .withDefendantReferralReasons(defendantReferralReasonsJPAMapper.fromJPA(entity.getDefendantReferralReasons()))
                .withHasSharedResults(entity.getHasSharedResults())
                .withIsBoxHearing(entity.getIsBoxHearing())
                .withIsVacatedTrial(entity.getIsVacatedTrial())
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
                .withApprovalsRequested(approvalRequestedJPAMapper.fromJPA(entity.getApprovalsRequested()))
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


    public String updateLinkedApplicationStatus(final String courtApplicationsJson, final UUID prosecutionCaseId, final ApplicationStatus status) {
        List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        if (courtApplications == null) {
            courtApplications = emptyList();
        }
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

    public String updateConvictedDateOnOffencesInCourtApplication(final String courtApplicationsJson,final UUID courtApplicationId, final UUID offenceId, final LocalDate convictedDate){
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        final List<CourtApplication> updatedCourtApplications = courtApplications.stream()
                .map(application -> ! application.getId().equals(courtApplicationId)  ? application :
                        CourtApplication.courtApplication().withValuesFrom(application)
                        .withCourtApplicationCases(getCourtApplicationCasesWithConvictionDate(offenceId, convictedDate, application))
                        .withCourtOrder(getCourtOrderWithConvictionDate(offenceId, convictedDate, application))
                        .build())
                .collect(toList());
        return courtApplicationsSerializer.json(updatedCourtApplications);
    }


    private CourtOrder getCourtOrderWithConvictionDate(final UUID offenceId, final LocalDate convictedDate, final CourtApplication application) {
        return application.getCourtOrder() == null ? null : of(application.getCourtOrder())
                .map(co -> CourtOrder.courtOrder().withValuesFrom(co)
                        .withCourtOrderOffences(co.getCourtOrderOffences().stream()
                                .map(o -> getCourtOrderOffenceWithConvictionDate(offenceId, convictedDate, o)).collect(toList())).build()).orElse(null);
    }

    private List<CourtApplicationCase> getCourtApplicationCasesWithConvictionDate(final UUID offenceId, final LocalDate convictedDate, final CourtApplication application) {
        return application.getCourtApplicationCases() == null ? null : application.getCourtApplicationCases().stream()
                .map(applicationCase -> CourtApplicationCase.courtApplicationCase().withValuesFrom(applicationCase)
                        .withOffences(applicationCase.getOffences().stream()
                                .map(courtApplicationOffence -> getCourtApplicationOffenceWithConvictionDate(offenceId, convictedDate, courtApplicationOffence))
                                .collect(toList()))
                        .build())
                .collect(toList());
    }

    private Offence getCourtApplicationOffenceWithConvictionDate(final UUID offenceId, final LocalDate convictedDate, final Offence offence) {
        return !offence.getId().equals(offenceId) ? offence :
                Offence.offence().withValuesFrom(offence)
                        .withConvictionDate(convictedDate)
                        .build();
    }

    private CourtOrderOffence getCourtOrderOffenceWithConvictionDate(final UUID offenceId, final LocalDate convictedDate, final CourtOrderOffence o) {
        return !o.getOffence().getId().equals(offenceId) ? o : CourtOrderOffence.courtOrderOffence().withValuesFrom(o)
                .withOffence(Offence.offence().withValuesFrom(o.getOffence())
                        .withConvictionDate(convictedDate)
                        .build())
                .build();
    }

    public String updatePleaOnOffencesInCourtApplication(final String courtApplicationsJson, final Plea plea) {
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        final CourtApplication courtApplication = getCourtApplication(courtApplications, plea.getOffenceId());
        final int index = courtApplications.indexOf(courtApplication);

        final CourtApplication updatedCourtApplication = CourtApplication.courtApplication().withValuesFrom(courtApplication)
                                .withCourtApplicationCases(courtApplication.getCourtApplicationCases() == null ? null : courtApplication.getCourtApplicationCases().stream()
                                        .map(applicationCase -> CourtApplicationCase.courtApplicationCase().withValuesFrom(applicationCase)
                                                .withOffences(applicationCase.getOffences().stream()
                                                        .map(courtApplicationOffence -> getCourtApplicationOffenceWithPlea(plea, courtApplicationOffence))
                                                        .collect(toList()))
                                                .build())
                                        .collect(toList()))
                                .withCourtOrder(courtApplication.getCourtOrder() == null ? null : of(courtApplication.getCourtOrder())
                                        .map(co -> CourtOrder.courtOrder().withValuesFrom(co)
                                                .withCourtOrderOffences(co.getCourtOrderOffences().stream()
                                                        .map(o -> getCourtOrderOffenceWithPlea(plea, o)).collect(toList())).build()).orElse(null))
                                .build();

        courtApplications.set(index, updatedCourtApplication);
        return courtApplicationsSerializer.json(courtApplications);
    }

    private CourtOrderOffence getCourtOrderOffenceWithPlea(final Plea plea, final CourtOrderOffence o) {
        return !o.getOffence().getId().equals(plea.getOffenceId()) ? o : CourtOrderOffence.courtOrderOffence().withValuesFrom(o)
                .withOffence(Offence.offence().withValuesFrom(o.getOffence())
                        .withPlea(plea)
                        .build())
                .build();
    }

    private Offence getCourtApplicationOffenceWithPlea(final Plea plea, final Offence offence) {
        return !offence.getId().equals(plea.getOffenceId()) ? offence :
                Offence.offence().withValuesFrom(offence)
                        .withPlea(plea)
                        .build();
    }

    public String updateVerdictOnOffencesInCourtApplication(final String courtApplicationsJson, final Verdict verdict) {
        final List<CourtApplication> courtApplications = courtApplicationsSerializer.courtApplications(courtApplicationsJson);
        final CourtApplication courtApplication = getCourtApplication(courtApplications, verdict.getOffenceId());
        final int index = courtApplications.indexOf(courtApplication);

        final CourtApplication updatedCourtApplication = CourtApplication.courtApplication().withValuesFrom(courtApplication)
                .withCourtApplicationCases(courtApplication.getCourtApplicationCases() == null ? null : courtApplication.getCourtApplicationCases().stream()
                        .map(applicationCase -> CourtApplicationCase.courtApplicationCase().withValuesFrom(applicationCase)
                                .withOffences(applicationCase.getOffences().stream()
                                        .map(courtApplicationOffence -> getCourtApplicationOffenceWithVerdict(verdict, courtApplicationOffence))
                                        .collect(toList()))
                                .build())
                        .collect(toList()))
                .withCourtOrder(courtApplication.getCourtOrder() == null ? null : of(courtApplication.getCourtOrder())
                        .map(co -> CourtOrder.courtOrder().withValuesFrom(co)
                                .withCourtOrderOffences(co.getCourtOrderOffences().stream()
                                        .map(o -> getCourtOrderOffenceWithVerdict(verdict, o)).collect(toList())).build()).orElse(null))
                .build();

        courtApplications.set(index, updatedCourtApplication);
        return courtApplicationsSerializer.json(courtApplications);
    }

    private CourtOrderOffence getCourtOrderOffenceWithVerdict(final Verdict verdict, final CourtOrderOffence o) {
        return !o.getOffence().getId().equals(verdict.getOffenceId()) ? o : CourtOrderOffence.courtOrderOffence().withValuesFrom(o)
                .withOffence(Offence.offence().withValuesFrom(o.getOffence())
                        .withVerdict(verdict)
                        .build())
                .build();
    }

    private Offence getCourtApplicationOffenceWithVerdict(final Verdict verdict, final Offence offence) {
        return !offence.getId().equals(verdict.getOffenceId()) ? offence :
                Offence.offence().withValuesFrom(offence)
                        .withVerdict(verdict)
                        .build();
    }

    private CourtApplication getCourtApplication(final List<CourtApplication> courtApplications, UUID offenceID){
        return courtApplications.stream()
                .filter(ca -> ofNullable(ca.getCourtApplicationCases()).orElse(emptyList()).stream()
                        .flatMap(cac -> cac.getOffences().stream())
                        .anyMatch(o -> o.getId().equals(offenceID)) ||
                        (ca.getCourtOrder() != null &&
                                ca.getCourtOrder().getCourtOrderOffences().stream().anyMatch(co -> co.getOffence().getId().equals(offenceID)))
                )
                .findFirst().get();
    }
}
