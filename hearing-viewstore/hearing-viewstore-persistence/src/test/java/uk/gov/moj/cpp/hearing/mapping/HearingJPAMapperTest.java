package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.CourtCentreJPAMapperTest.whenCourtCentre;
import static uk.gov.moj.cpp.hearing.mapping.DefendantReferralReasonJPAMapperTest.whenFirstReferralReason;
import static uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapperTest.whenFirstHearingDay;
import static uk.gov.moj.cpp.hearing.mapping.HearingTypeJPAMapperTest.whenHearingType;
import static uk.gov.moj.cpp.hearing.mapping.JudicialRoleJPAMapperTest.whenFirstJudicialRole;
import static uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseJPAMapperTest.whenFirstProsecutionCase;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.justice.json.schemas.core.Hearing;
import uk.gov.justice.json.schemas.core.HearingDay;
import uk.gov.justice.json.schemas.core.HearingType;
import uk.gov.justice.json.schemas.core.JudicialRole;
import uk.gov.justice.json.schemas.core.ProsecutionCase;
import uk.gov.justice.json.schemas.core.ReferralReason;

public class HearingJPAMapperTest {

    private HearingJPAMapper hearingJPAMapper = JPACompositeMappers.HEARING_JPA_MAPPER;

    @Test
    public void testFromJPA() throws Exception {

        final uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();

        assertThat(hearingJPAMapper.fromJPA(hearingEntity), isBean(Hearing.class)
                .with(Hearing::getCourtCentre, whenCourtCentre(
                        isBean(CourtCentre.class), hearingEntity.getCourtCentre()))
                .with(Hearing::getDefendantReferralReasons, whenFirstReferralReason(
                        isBean(ReferralReason.class), hearingEntity.getDefendantReferralReasons().get(0)))
                .with(Hearing::getHasSharedResults, is(hearingEntity.getHasSharedResults()))
                .with(Hearing::getHearingDays , whenFirstHearingDay(
                        isBean(HearingDay.class), hearingEntity.getHearingDays().get(0)))
                .with(Hearing::getHearingLanguage, is(hearingEntity.getHearingLanguage()))
                .with(Hearing::getId, is(hearingEntity.getId()))
                .with(Hearing::getJudiciary, whenFirstJudicialRole(
                        isBean(JudicialRole.class), hearingEntity.getJudicialRoles().get(0)))
                .with(Hearing::getJurisdictionType, is(hearingEntity.getJurisdictionType()))
                .with(Hearing::getProsecutionCases, whenFirstProsecutionCase(
                        isBean(ProsecutionCase.class), hearingEntity.getProsecutionCases().get(0)))
                .with(Hearing::getReportingRestrictionReason, is(hearingEntity.getReportingRestrictionReason()))
                .with(Hearing::getType, whenHearingType(
                        isBean(HearingType.class), hearingEntity.getHearingType())));
    }

    @Test
    public void testToJPA() throws Exception {

        final Hearing hearingPojo = hearingJPAMapper.fromJPA(aNewHearingJPADataTemplate().getHearing());

        assertThat(hearingJPAMapper.toJPA(hearingPojo), isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing.class)
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getCourtCentre, whenCourtCentre(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre.class), hearingPojo.getCourtCentre()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getDefendantReferralReasons, whenFirstReferralReason(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantReferralReason.class), hearingPojo.getDefendantReferralReasons().get(0)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHasSharedResults, is(hearingPojo.getHasSharedResults()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingDays , whenFirstHearingDay(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class), hearingPojo.getHearingDays().get(0)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingLanguage, is(hearingPojo.getHearingLanguage()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getId, is(hearingPojo.getId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getJudicialRoles, whenFirstJudicialRole(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole.class), hearingPojo.getJudiciary().get(0)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getJurisdictionType, is(hearingPojo.getJurisdictionType()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getProsecutionCases, whenFirstProsecutionCase(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase.class), hearingPojo.getProsecutionCases().get(0)))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getReportingRestrictionReason, is(hearingPojo.getReportingRestrictionReason()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing::getHearingType, whenHearingType(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType.class), hearingPojo.getType())));
    }
}