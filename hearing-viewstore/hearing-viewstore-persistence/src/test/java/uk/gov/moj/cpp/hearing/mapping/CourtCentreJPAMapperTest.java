package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import org.junit.Test;

import uk.gov.justice.json.schemas.core.CourtCentre;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

public class CourtCentreJPAMapperTest {

    private CourtCentreJPAMapper courtCentreJPAMapper = new CourtCentreJPAMapper();

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre courtCentreEntity = aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre.class);
        assertThat(courtCentreJPAMapper.fromJPA(courtCentreEntity), whenCourtCentre(isBean(CourtCentre.class), courtCentreEntity));
    }

    @Test
    public void testToJPA() {
        final CourtCentre courtCentrePojo = aNewEnhancedRandom().nextObject(CourtCentre.class);
        assertThat(courtCentreJPAMapper.toJPA(courtCentrePojo), whenCourtCentre(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre.class), courtCentrePojo));
    }

    public static BeanMatcher<CourtCentre> whenCourtCentre(final BeanMatcher<CourtCentre> m,
            final uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre entity) {
        return m.with(CourtCentre::getId, is(entity.getId()))
                .with(CourtCentre::getName, is(entity.getName()))
                .with(CourtCentre::getRoomId, is(entity.getRoomId()))
                .with(CourtCentre::getRoomName, is(entity.getRoomName()))
                .with(CourtCentre::getWelshName, is(entity.getWelshName()))
                .with(CourtCentre::getWelshRoomName, is(entity.getWelshRoomName()));

    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre> whenCourtCentre(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre> m, final CourtCentre pojo) {
        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre::getId, is(pojo.getId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre::getName, is(pojo.getName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre::getRoomId, is(pojo.getRoomId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre::getRoomName, is(pojo.getRoomName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre::getWelshName, is(pojo.getWelshName()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre::getWelshRoomName, is(pojo.getWelshRoomName()));
    }
}