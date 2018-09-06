package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.JurorsJPAMapperTest.whenJurors;
import static uk.gov.moj.cpp.hearing.mapping.LesserOrAlternativeOffenceJPAMapperTest.whenLesserOrAlternativeOffence;
import static uk.gov.moj.cpp.hearing.mapping.VerdictTypeJPAMapperTest.whenVerdictType;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import java.util.UUID;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.Jurors;
import uk.gov.justice.json.schemas.core.LesserOrAlternativeOffence;
import uk.gov.justice.json.schemas.core.Verdict;
import uk.gov.justice.json.schemas.core.VerdictType;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class VerdictJPAMapperTest {

    private VerdictJPAMapper verdictJPAMapper = JPACompositeMappers.VERDICT_JPA_MAPPER;

    @Test
    public void testFromJPA() {
        final UUID offenceId = UUID.randomUUID();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict addressEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict.class);
        assertThat(verdictJPAMapper.fromJPA(offenceId, addressEntity), whenVerdict(isBean(Verdict.class), offenceId, addressEntity));
    }

    @Test
    public void testToJPA() {
        final Verdict addressPojo = aNewEnhancedRandom().nextObject(Verdict.class);
        assertThat(verdictJPAMapper.toJPA(addressPojo), whenVerdict(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict.class), addressPojo));
    }

    public static BeanMatcher<Verdict> whenVerdict(final BeanMatcher<Verdict> m, final UUID offenceId,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict entity) {

        return m.with(Verdict::getJurors, whenJurors(
                        isBean(Jurors.class), entity.getJurors()))

                .with(Verdict::getLesserOrAlternativeOffence, whenLesserOrAlternativeOffence(
                        isBean(LesserOrAlternativeOffence.class), entity.getLesserOrAlternativeOffence()))

                .with(Verdict::getOffenceId, is(offenceId))
                .with(Verdict::getVerdictDate, is(entity.getVerdictDate()))

                .with(Verdict::getVerdictType, whenVerdictType(
                        isBean(VerdictType.class), entity.getVerdictType()));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict> whenVerdict(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict> m, final Verdict entity) {

        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict::getJurors, whenJurors(
                isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors.class), entity.getJurors()))

        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict::getLesserOrAlternativeOffence, 
                whenLesserOrAlternativeOffence(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.LesserOrAlternativeOffence.class), entity.getLesserOrAlternativeOffence()))

        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict::getVerdictDate, is(entity.getVerdictDate()))

        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict::getVerdictType, 
                whenVerdictType(
                        isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.VerdictType.class), entity.getVerdictType()));
    }
}