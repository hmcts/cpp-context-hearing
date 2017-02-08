package uk.gov.moj.cpp.hearing.persist;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.LONG;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinitions;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class HearingEventDefinitionsRepositoryTest extends BaseTransactionalTest {

    private static final String RECORDED_LABEL = STRING.next();
    private static final String ACTION_LABEL = STRING.next();
    private static final Integer SEQUENCE_NUMBER = 1;
    private static final String CASE_ATTRIBUTE = STRING.next();

    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final String ACTION_LABEL_2 = STRING.next();
    private static final Integer SEQUENCE_NUMBER_2 = 2;
    private static final String CASE_ATTRIBUTE_2 = STRING.next();

    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final String ACTION_LABEL_3 = STRING.next();
    private static final Integer SEQUENCE_NUMBER_3 = 3;
    private static final String CASE_ATTRIBUTE_3 = STRING.next();

    @Inject
    private HearingEventDefinitionsRepository hearingEventDefinitionsRepository;

    @Test
    public void shouldGetHearingEventDefinitionsInSequentialOrder() {
        givenHearingEventDefinitionsExistInRandomOrder();

        final List<HearingEventDefinitions> actualEventDefinitions = hearingEventDefinitionsRepository.findAll();

        assertThat(actualEventDefinitions, hasSize(3));

        assertThat(actualEventDefinitions.get(0).getSequenceNumber(), is(1));
        assertThat(actualEventDefinitions.get(1).getSequenceNumber(), is(2));
        assertThat(actualEventDefinitions.get(2).getSequenceNumber(), is(3));
    }

    @Test
    public void shouldRemoveAllHearingEventDefinitions() {
        givenHearingEventDefinitionsExistInRandomOrder();

        hearingEventDefinitionsRepository.deleteAll();

        assertThat(hearingEventDefinitionsRepository.findAll(), hasSize(0));
    }

    private void givenHearingEventDefinitionsExistInRandomOrder() {
        final List<HearingEventDefinitions> hearingEventDefinitions = newArrayList(
            new HearingEventDefinitions(RECORDED_LABEL, ACTION_LABEL, SEQUENCE_NUMBER, CASE_ATTRIBUTE),
            new HearingEventDefinitions(RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_NUMBER_2, CASE_ATTRIBUTE_2),
            new HearingEventDefinitions(RECORDED_LABEL_3, ACTION_LABEL_3, SEQUENCE_NUMBER_3, CASE_ATTRIBUTE_3)
        );

        shuffle(hearingEventDefinitions, new Random(LONG.next()));

        hearingEventDefinitions.forEach(hearingEventDefinition -> hearingEventDefinitionsRepository.save(hearingEventDefinition));

        assertThat(hearingEventDefinitionsRepository.findAll(), hasSize(3));
    }

}