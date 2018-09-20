package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.IndicatedPleaJPAMapperTest.whenIndicatedPlea;
import static uk.gov.moj.cpp.hearing.mapping.NotifiedPleaJPAMapperTest.whenNotifiedPlea;
import static uk.gov.moj.cpp.hearing.mapping.OffenceFactsJPAMapperTest.whenOffenceFacts;
import static uk.gov.moj.cpp.hearing.mapping.PleaJPAMapperTest.whenPlea;
import static uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapperTest.whenVerdict;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.IndicatedPlea;
import uk.gov.justice.json.schemas.core.NotifiedPlea;
import uk.gov.justice.json.schemas.core.Offence;
import uk.gov.justice.json.schemas.core.OffenceFacts;
import uk.gov.justice.json.schemas.core.Plea;
import uk.gov.justice.json.schemas.core.Verdict;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

public class OffenceJPAMapperTest {

    private OffenceJPAMapper offenceFactsJPAMapper = JPACompositeMappers.OFFENCE_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Offence offenceEntity = aNewHearingJPADataTemplate().getHearing().getProsecutionCases().iterator().next().getDefendants().iterator().next().getOffences().iterator().next();
        assertThat(offenceFactsJPAMapper.fromJPA(offenceEntity), whenOffence(isBean(Offence.class), offenceEntity));
    }

    @Test
    public void testToJPA() {
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final Defendant defendantEntity = hearingEntity.getProsecutionCases().iterator().next().getDefendants().iterator().next();
        final Offence offencePojo = offenceFactsJPAMapper.fromJPA(defendantEntity.getOffences().iterator().next());
        assertThat(offenceFactsJPAMapper.toJPA(hearingEntity, defendantEntity.getId().getId(), offencePojo), 
                whenOffence(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence.class), offencePojo));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstOffence(final BeanMatcher<?> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.Offence entity) {
        return ElementAtListMatcher.first(whenOffence((BeanMatcher<Offence>) m, entity));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstOffence(final BeanMatcher<?> m, final Offence pojo) {
        return ElementAtListMatcher.first(whenOffence((BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Offence>) m, pojo));
    }

    public static BeanMatcher<Offence> whenOffence(final BeanMatcher<Offence> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.Offence entity) {

        return m.with(Offence::getArrestDate, is(entity.getArrestDate()))
                .with(Offence::getChargeDate, is(entity.getChargeDate()))
                .with(Offence::getConvictionDate, is(entity.getConvictionDate()))
                .with(Offence::getCount, is(entity.getCount()))
                .with(Offence::getEndDate, is(entity.getEndDate()))
                .with(Offence::getId, is(entity.getId().getId()))

                .with(Offence::getIndicatedPlea, 
                        whenIndicatedPlea(isBean(IndicatedPlea.class), entity.getId().getId(), entity.getIndicatedPlea()))

                .with(Offence::getModeOfTrial, is(entity.getModeOfTrial()))

                .with(Offence::getNotifiedPlea, 
                        whenNotifiedPlea(isBean(NotifiedPlea.class), entity.getId().getId(), entity.getNotifiedPlea()))

                .with(Offence::getOffenceCode, is(entity.getOffenceCode()))
                .with(Offence::getOffenceDefinitionId, is(entity.getOffenceDefinitionId()))

                .with(Offence::getOffenceFacts, 
                        whenOffenceFacts(isBean(OffenceFacts.class), entity.getOffenceFacts()))

                .with(Offence::getOffenceLegislation, is(entity.getOffenceLegislation()))
                .with(Offence::getOffenceLegislationWelsh, is(entity.getOffenceLegislationWelsh()))
                .with(Offence::getOffenceTitle, is(entity.getOffenceTitle()))
                .with(Offence::getOffenceTitleWelsh, is(entity.getOffenceTitleWelsh()))
                .with(Offence::getOrderIndex, is(entity.getOrderIndex()))

                .with(Offence::getPlea, 
                        whenPlea(isBean(Plea.class), entity.getId().getId(), entity.getPlea()))

                .with(Offence::getStartDate, is(entity.getStartDate()))
                .with(Offence::getWording, is(entity.getWording()))
                .with(Offence::getWordingWelsh, is(entity.getWordingWelsh()))
                
                .with(Offence::getVerdict, 
                        whenVerdict(isBean(Verdict.class), entity.getId().getId(), entity.getVerdict()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Offence> whenOffence(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Offence> m, final Offence pojo) {

        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getArrestDate, is(pojo.getArrestDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getChargeDate, is(pojo.getChargeDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getConvictionDate, is(pojo.getConvictionDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getCount, is(pojo.getCount()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getEndDate, is(pojo.getEndDate()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getId, isBean(HearingSnapshotKey.class)
                        .with(HearingSnapshotKey::getId, is(pojo.getId())))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getIndicatedPlea, whenIndicatedPlea(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.IndicatedPlea.class), pojo.getIndicatedPlea()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getModeOfTrial, is(pojo.getModeOfTrial()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getNotifiedPlea, whenNotifiedPlea(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea.class), pojo.getNotifiedPlea()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getOffenceCode, is(pojo.getOffenceCode()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getOffenceDefinitionId, is(pojo.getOffenceDefinitionId()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getOffenceFacts,  whenOffenceFacts(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.OffenceFacts.class), pojo.getOffenceFacts()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getOffenceLegislation, is(pojo.getOffenceLegislation()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getOffenceLegislationWelsh, is(pojo.getOffenceLegislationWelsh()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getOffenceTitle, is(pojo.getOffenceTitle()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getOffenceTitleWelsh, is(pojo.getOffenceTitleWelsh()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getOrderIndex, is(pojo.getOrderIndex()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getPlea, whenPlea(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Plea.class), pojo.getPlea()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getStartDate, is(pojo.getStartDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getWording, is(pojo.getWording()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Offence::getWordingWelsh, is(pojo.getWordingWelsh()));
    }
}