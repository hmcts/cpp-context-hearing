package uk.gov.moj.cpp.hearing.persist;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.LONG;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.HearingEventDefinition;

import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class HearingEventDefinitionRepositoryTest extends BaseTransactionalTest {

    private static final String RECORDED_LABEL = STRING.next();
    private static final String ACTION_LABEL = STRING.next();
    private static final Integer SEQUENCE_NUMBER = 1;

    private static final String RECORDED_LABEL_2 = STRING.next();
    private static final String ACTION_LABEL_2 = STRING.next();
    private static final Integer SEQUENCE_NUMBER_2 = 2;
    private static final String CASE_ATTRIBUTE_2 = STRING.next();

    private static final String RECORDED_LABEL_3 = STRING.next();
    private static final String ACTION_LABEL_3 = STRING.next();
    private static final Integer SEQUENCE_NUMBER_3 = 3;

    private static final String RECORDED_LABEL_4 = STRING.next();
    private static final String ACTION_LABEL_4 = "judge ruling: Contempt of court";

    private static final String RECORDED_LABEL_5 = STRING.next();
    private static final String ACTION_LABEL_5 = "Prosecution challenges defence application";

    private static final String RECORDED_LABEL_6 = STRING.next();
    private static final String ACTION_LABEL_6 = STRING.next();

    private static final String RECORDED_LABEL_7 = STRING.next();
    private static final String ACTION_LABEL_7 = STRING.next();

    private static final String RECORDED_LABEL_8 = STRING.next();
    private static final String ACTION_LABEL_8 = STRING.next();

    private static final String RECORDED_LABEL_9 = STRING.next();
    private static final String ACTION_LABEL_9 = STRING.next();

    private static final String SEQUENCE_TYPE_SENTENCE = "SENTENCING";
    private static final String SEQUENCE_TYPE_PAUSE_RESUME = "PAUSE_RESUME";
    private static final String SEQUENCE_TYPE_RANDOM = STRING.next();

    private static final String GROUP_LABEL = STRING.next();
    private static final String ACTION_LABEL_EXTENSION = STRING.next();

    @Inject
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;

    @Test
    public void shouldSaveAndRetrieveAHearingEventDefinition() {
        final HearingEventDefinition eventDefinition = new HearingEventDefinition(RECORDED_LABEL, ACTION_LABEL,
                SEQUENCE_NUMBER, SEQUENCE_TYPE_SENTENCE, null, GROUP_LABEL, ACTION_LABEL_EXTENSION);

        hearingEventDefinitionRepository.save(eventDefinition);

        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions.get(0).getActionLabel(), is(ACTION_LABEL));
        assertThat(actualEventDefinitions.get(0).getRecordedLabel(), is(RECORDED_LABEL));
        assertThat(actualEventDefinitions.get(0).getSequenceNumber(), is(SEQUENCE_NUMBER));
        assertThat(actualEventDefinitions.get(0).getCaseAttribute(), is(nullValue()));
        assertThat(actualEventDefinitions.get(0).getGroupLabel(), is(GROUP_LABEL));
        assertThat(actualEventDefinitions.get(0).getActionLabelExtension(), is(ACTION_LABEL_EXTENSION));
    }

    @Test
    public void shouldGetHearingEventDefinitionsInSequentialOrder() {
        givenSequencedHearingEventDefinitionsExistInRandomOrder();

        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions, hasSize(3));

        assertThat(actualEventDefinitions.get(0).getSequenceNumber(), is(1));
        assertThat(actualEventDefinitions.get(1).getSequenceNumber(), is(2));
        assertThat(actualEventDefinitions.get(2).getSequenceNumber(), is(3));
    }

    @Test
    public void shouldGetHearingEventDefinitionsOrderedBySequenceNumberAndActionLabel() {
        givenSequencedAndNonSequencedHearingEventDefinitionsExistInRandomOrder();

        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions, hasSize(5));

        assertThat(actualEventDefinitions.get(0).getSequenceNumber(), is(1));
        assertThat(actualEventDefinitions.get(1).getSequenceNumber(), is(2));
        assertThat(actualEventDefinitions.get(2).getSequenceNumber(), is(3));
        assertThat(actualEventDefinitions.get(3).getActionLabel(), is(ACTION_LABEL_4));
        assertThat(actualEventDefinitions.get(4).getActionLabel(), is(ACTION_LABEL_5));
    }

    @Test
    public void shouldGetHearingEventDefinitionsOrderedBySequenceTypeSequenceNumberAndActionLabel() {
        givenHearingEventDefinitionsExistWithMultipleSequenceTypesInRandomOrder();

        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions, hasSize(7));

        assertThat(actualEventDefinitions.get(0).getActionLabel(), is(ACTION_LABEL));
        assertThat(actualEventDefinitions.get(1).getActionLabel(), is(ACTION_LABEL_2));
        assertThat(actualEventDefinitions.get(2).getActionLabel(), is(ACTION_LABEL_3));
        assertThat(actualEventDefinitions.get(3).getActionLabel(), is(ACTION_LABEL_6));
        assertThat(actualEventDefinitions.get(4).getActionLabel(), is(ACTION_LABEL_7));
        assertThat(actualEventDefinitions.get(5).getActionLabel(), is(ACTION_LABEL_4));
        assertThat(actualEventDefinitions.get(6).getActionLabel(), is(ACTION_LABEL_5));
    }

    @Test
    public void shouldGetHearingEventDefinitionsWithUndefinedSequenceTypesBeforeNulls() {
        givenHearingEventDefinitionsExistWithUndefinedSequenceTypesInRandomOrder();

        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions, hasSize(9));

        assertThat(actualEventDefinitions.get(0).getActionLabel(), is(ACTION_LABEL));
        assertThat(actualEventDefinitions.get(1).getActionLabel(), is(ACTION_LABEL_2));
        assertThat(actualEventDefinitions.get(2).getActionLabel(), is(ACTION_LABEL_3));
        assertThat(actualEventDefinitions.get(3).getActionLabel(), is(ACTION_LABEL_6));
        assertThat(actualEventDefinitions.get(4).getActionLabel(), is(ACTION_LABEL_7));
        assertThat(actualEventDefinitions.get(5).getActionLabel(), is(ACTION_LABEL_8));
        assertThat(actualEventDefinitions.get(6).getActionLabel(), is(ACTION_LABEL_9));
        assertThat(actualEventDefinitions.get(7).getActionLabel(), is(ACTION_LABEL_4));
        assertThat(actualEventDefinitions.get(8).getActionLabel(), is(ACTION_LABEL_5));
    }

    @Test
    public void shouldRemoveAllHearingEventDefinitions() {
        givenSequencedHearingEventDefinitionsExistInRandomOrder();

        hearingEventDefinitionRepository.deleteAll();

        assertThat(hearingEventDefinitionRepository.findAll(), hasSize(0));
    }

    private void givenSequencedHearingEventDefinitionsExistInRandomOrder() {
        final List<HearingEventDefinition> hearingEventDefinitions = newArrayList(
            new HearingEventDefinition(RECORDED_LABEL, ACTION_LABEL, SEQUENCE_NUMBER, SEQUENCE_TYPE_SENTENCE, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_NUMBER_2, SEQUENCE_TYPE_SENTENCE, CASE_ATTRIBUTE_2, GROUP_LABEL, ACTION_LABEL_EXTENSION),
            new HearingEventDefinition(RECORDED_LABEL_3, ACTION_LABEL_3, SEQUENCE_NUMBER_3, SEQUENCE_TYPE_SENTENCE, null, null, null)
        );

        saveHearingEventDefinitionsInRandomOrder(hearingEventDefinitions);
    }

    private void givenSequencedAndNonSequencedHearingEventDefinitionsExistInRandomOrder() {
        final List<HearingEventDefinition> hearingEventDefinitions = newArrayList(
            new HearingEventDefinition(RECORDED_LABEL, ACTION_LABEL, SEQUENCE_NUMBER, SEQUENCE_TYPE_SENTENCE, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_NUMBER_2, SEQUENCE_TYPE_SENTENCE, CASE_ATTRIBUTE_2, GROUP_LABEL, ACTION_LABEL_EXTENSION),
            new HearingEventDefinition(RECORDED_LABEL_3, ACTION_LABEL_3, SEQUENCE_NUMBER_3, SEQUENCE_TYPE_SENTENCE, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_4, ACTION_LABEL_4, null, null, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_5, ACTION_LABEL_5, null, null, null, null, null)
        );

        saveHearingEventDefinitionsInRandomOrder(hearingEventDefinitions);
    }

    private void givenHearingEventDefinitionsExistWithMultipleSequenceTypesInRandomOrder() {
        final List<HearingEventDefinition> hearingEventDefinitions = newArrayList(
            new HearingEventDefinition(RECORDED_LABEL, ACTION_LABEL, SEQUENCE_NUMBER, SEQUENCE_TYPE_SENTENCE, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_NUMBER_2, SEQUENCE_TYPE_SENTENCE, CASE_ATTRIBUTE_2, GROUP_LABEL, ACTION_LABEL_EXTENSION),
            new HearingEventDefinition(RECORDED_LABEL_3, ACTION_LABEL_3, SEQUENCE_NUMBER_3, SEQUENCE_TYPE_SENTENCE, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_4, ACTION_LABEL_4, null, null, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_5, ACTION_LABEL_5, null, null, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_6, ACTION_LABEL_6, SEQUENCE_NUMBER, SEQUENCE_TYPE_PAUSE_RESUME, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_7, ACTION_LABEL_7, SEQUENCE_NUMBER_2, SEQUENCE_TYPE_PAUSE_RESUME, null, null, null)
        );

        saveHearingEventDefinitionsInRandomOrder(hearingEventDefinitions);
    }

    private void givenHearingEventDefinitionsExistWithUndefinedSequenceTypesInRandomOrder() {
        final List<HearingEventDefinition> hearingEventDefinitions = newArrayList(
            new HearingEventDefinition(RECORDED_LABEL, ACTION_LABEL, SEQUENCE_NUMBER, SEQUENCE_TYPE_SENTENCE, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_2, ACTION_LABEL_2, SEQUENCE_NUMBER_2, SEQUENCE_TYPE_SENTENCE, CASE_ATTRIBUTE_2, GROUP_LABEL, ACTION_LABEL_EXTENSION),
            new HearingEventDefinition(RECORDED_LABEL_3, ACTION_LABEL_3, SEQUENCE_NUMBER_3, SEQUENCE_TYPE_SENTENCE, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_4, ACTION_LABEL_4, null, null, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_5, ACTION_LABEL_5, null, null, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_6, ACTION_LABEL_6, SEQUENCE_NUMBER, SEQUENCE_TYPE_PAUSE_RESUME, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_7, ACTION_LABEL_7, SEQUENCE_NUMBER_2, SEQUENCE_TYPE_PAUSE_RESUME, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_8, ACTION_LABEL_8, SEQUENCE_NUMBER, SEQUENCE_TYPE_RANDOM, null, null, null),
            new HearingEventDefinition(RECORDED_LABEL_9, ACTION_LABEL_9, SEQUENCE_NUMBER_2, SEQUENCE_TYPE_RANDOM, null, null, null)
        );

        saveHearingEventDefinitionsInRandomOrder(hearingEventDefinitions);
    }

    private void saveHearingEventDefinitionsInRandomOrder(final List<HearingEventDefinition> hearingEventDefinitions) {
        shuffle(hearingEventDefinitions, new Random(LONG.next()));

        hearingEventDefinitions.forEach(hearingEventDefinition -> hearingEventDefinitionRepository.save(hearingEventDefinition));

        assertThat(hearingEventDefinitionRepository.findAll(), hasSize(hearingEventDefinitions.size()));
    }

}