package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.NotifiedPlea;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class NotifiedPleaJPAMapperTest {

    private NotifiedPleaJPAMapper notifiedPleaJPAMapper = new NotifiedPleaJPAMapper();

    public static BeanMatcher<NotifiedPlea> whenNotifiedPlea(final BeanMatcher<NotifiedPlea> m, final UUID offenceId,
                                                             final uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea entity) {
        return m.with(NotifiedPlea::getNotifiedPleaDate, is(entity.getNotifiedPleaDate()))
                .with(NotifiedPlea::getNotifiedPleaValue, is(entity.getNotifiedPleaValue()))
                .with(NotifiedPlea::getOffenceId, is(offenceId));
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea> whenNotifiedPlea(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea> m, final NotifiedPlea pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea::getNotifiedPleaDate, is(pojo.getNotifiedPleaDate()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea::getNotifiedPleaValue, is(pojo.getNotifiedPleaValue()));
    }

    @Test
    public void testFromJPA() {
        final UUID offenceId = UUID.randomUUID();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea notifiedPleaEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea.class);
        assertThat(notifiedPleaJPAMapper.fromJPA(offenceId, notifiedPleaEntity), whenNotifiedPlea(isBean(NotifiedPlea.class), offenceId, notifiedPleaEntity));
    }

    @Test
    public void testToJPA() {
        final NotifiedPlea notifiedPleaPojo = aNewEnhancedRandom().nextObject(NotifiedPlea.class);
        assertThat(notifiedPleaJPAMapper.toJPA(notifiedPleaPojo), whenNotifiedPlea(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.NotifiedPlea.class), notifiedPleaPojo));
    }
}