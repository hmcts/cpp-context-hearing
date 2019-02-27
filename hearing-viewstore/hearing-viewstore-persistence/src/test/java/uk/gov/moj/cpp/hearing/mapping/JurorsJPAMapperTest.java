package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.Jurors;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.Test;

public class JurorsJPAMapperTest {

    private JurorsJPAMapper jurorsJPAMapper = new JurorsJPAMapper();

    public static BeanMatcher<Jurors> whenJurors(final BeanMatcher<Jurors> m,
                                                 final uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors entity) {
        return m.with(Jurors::getNumberOfJurors, is(entity.getNumberOfJurors()))
                .with(Jurors::getNumberOfSplitJurors, is(entity.getNumberOfSplitJurors()))
                .with(Jurors::getUnanimous, is(entity.getUnanimous()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors> whenJurors(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors> m, final Jurors pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors::getNumberOfJurors, is(pojo.getNumberOfJurors()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors::getNumberOfSplitJurors, is(pojo.getNumberOfSplitJurors()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors::getUnanimous, is(pojo.getUnanimous()));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors addressEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors.class);
        assertThat(jurorsJPAMapper.fromJPA(addressEntity), whenJurors(isBean(Jurors.class), addressEntity));
    }

    @Test
    public void testToJPA() {
        final Jurors addressPojo = aNewEnhancedRandom().nextObject(Jurors.class);
        assertThat(jurorsJPAMapper.toJPA(addressPojo),
                whenJurors(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors.class), addressPojo));
    }
}