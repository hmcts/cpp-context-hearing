package uk.gov.moj.cpp.hearing.mapping;

import static io.github.benas.randombeans.EnhancedRandomBuilder.aNewEnhancedRandom;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.mapping.JPACompositeMappers.COURT_INDICATED_SENTENCE_JPA_MAPPER;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.CourtIndicatedSentence;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import org.junit.jupiter.api.Test;

public class CourtIndicatedSentenceJPAMapperTest {

    private CourtIndicatedSentenceJPAMapper courtIndicatedSentenceJPAMapper = COURT_INDICATED_SENTENCE_JPA_MAPPER;

    @Test
    public void testToJPA() {

        final CourtIndicatedSentence courtIndicatedSentencePojo = aNewEnhancedRandom().nextObject(CourtIndicatedSentence.class);
        assertThat(courtIndicatedSentenceJPAMapper.toJPA(courtIndicatedSentencePojo), whenCourtIndicatedSentence(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence.class), courtIndicatedSentencePojo));
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence courtIndicatedSentenceEntity =
                aNewEnhancedRandom().nextObject(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence.class);

        assertThat(courtIndicatedSentenceJPAMapper.fromJPA(courtIndicatedSentenceEntity),
                whenCourtIndicatedSentence(isBean(CourtIndicatedSentence.class), courtIndicatedSentenceEntity));

    }

    public static final BeanMatcher<CourtIndicatedSentence> whenCourtIndicatedSentence(final BeanMatcher<CourtIndicatedSentence> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence entity) {
        return m.with(CourtIndicatedSentence::getCourtIndicatedSentenceTypeId, is(entity.getCourtIndicatedSentenceTypeId()))
                .with(CourtIndicatedSentence::getCourtIndicatedSentenceDescription, is(entity.getCourtIndicatedSentenceDescription()));

    }

    public static final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence> whenCourtIndicatedSentence(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence> m, final CourtIndicatedSentence pojo) {

        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence::getCourtIndicatedSentenceTypeId, is(pojo.getCourtIndicatedSentenceTypeId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence::getCourtIndicatedSentenceDescription, is(pojo.getCourtIndicatedSentenceDescription()));
    }

}