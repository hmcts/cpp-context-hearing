package uk.gov.moj.cpp.hearing.repository;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.LONG;

import uk.gov.justice.services.test.utils.persistence.BaseTransactionalTest;
import uk.gov.moj.cpp.hearing.persist.entity.heda.HearingEventDefinition;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("CdiInjectionPointsInspection")
@RunWith(CdiTestRunner.class)
public class HearingEventDefinitionRepositoryTest extends BaseTransactionalTest {
    private static final String ID_1 = "b71e7d2a-d3b3-4a55-a393-6d451767fc05";
    private static final String RECORDED_LABEL_1 = "Hearing Started";
    private static final String ACTION_LABEL_1 = "Start";
    private static final Integer ACTION_SEQUENCE_1 = 1;
    private static final String GROUP_LABEL_1 = "Recording";
    private static final Integer GROUP_SEQUENCE_1 = 1;

    private static final String ID_2 = "0df93f18-0a21-40f5-9fb3-da4749cd70fe";
    private static final String RECORDED_LABEL_2 = "Hearing Ended";
    private static final String ACTION_LABEL_2 = "End";
    private static final Integer ACTION_SEQUENCE_2 = 2;
    private static final String GROUP_LABEL_2 = "Recording";
    private static final Integer GROUP_SEQUENCE_2 = 1;

    private static final String ID_3 = "160ecb51-29ee-4954-bbbf-daab18a24fbb";
    private static final String RECORDED_LABEL_3 = "Hearing Paused";
    private static final String ACTION_LABEL_3 = "Pause";
    private static final Integer ACTION_SEQUENCE_3 = 3;
    private static final String GROUP_LABEL_3 = "Recording";
    private static final Integer GROUP_SEQUENCE_3 = 1;

    private static final String ID_5 = "ffd6bb0d-8702-428c-a7bd-570570fa8d0a";
    private static final String RECORDED_LABEL_5 = "Proceedings in chambers";
    private static final String ACTION_LABEL_5 = "In chambers";
    private static final Integer ACTION_SEQUENCE_5 = 5;
    private static final String GROUP_LABEL_5 = "Recording";
    private static final Integer GROUP_SEQUENCE_5 = 1;

    private static final String ID_6 = "c0b15e38-52ce-4d9d-9ffa-d76c7793cff6";
    private static final String RECORDED_LABEL_6 = "Open Court";
    private static final String ACTION_LABEL_6 = "Open court";
    private static final Integer ACTION_SEQUENCE_6 = 6;
    private static final String GROUP_LABEL_6 = "Recording";
    private static final Integer GROUP_SEQUENCE_6 = 1;

    private static final String ID_7 = "c3edf650-13c4-4ecb-9f85-6100ad8e4ffc";
    private static final String RECORDED_LABEL_7 = "Defendant Arraigned";
    private static final String ACTION_LABEL_7 = "Arraign defendant.name";
    private static final Integer ACTION_SEQUENCE_7 = 1;
    private static final String GROUP_LABEL_7 = "Defendant";
    private static final Integer GROUP_SEQUENCE_7 = 2;

    private static final String ID_8 = "75c8c5eb-c661-40be-a5bf-07b7b8c0463a";
    private static final String RECORDED_LABEL_8 = "Defendant Rearraigned";
    private static final String ACTION_LABEL_8 = "Rearraign defendant.name";
    private static final Integer ACTION_SEQUENCE_8 = 2;
    private static final String GROUP_LABEL_8 = "Defendant";
    private static final Integer GROUP_SEQUENCE_8 = 2;


    private static final boolean ALTERABLE = BOOLEAN.next();

    @Inject
    private HearingEventDefinitionRepository hearingEventDefinitionRepository;

    @Test
    public void shouldSaveAndRetrieveAHearingEventDefinition() {
        final HearingEventDefinition eventDefinition = new HearingEventDefinition(UUID.fromString(ID_1), RECORDED_LABEL_1, ACTION_LABEL_1,
                ACTION_SEQUENCE_1, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, ALTERABLE);

        hearingEventDefinitionRepository.save(eventDefinition);

        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions.get(0).getActionLabel(), is(ACTION_LABEL_1));
        assertThat(actualEventDefinitions.get(0).getRecordedLabel(), is(RECORDED_LABEL_1));
        assertThat(actualEventDefinitions.get(0).getActionSequence(), is(ACTION_SEQUENCE_1));
        assertThat(actualEventDefinitions.get(0).getCaseAttribute(), is(nullValue()));
        assertThat(actualEventDefinitions.get(0).getGroupLabel(), is(GROUP_LABEL_1));
        assertThat(actualEventDefinitions.get(0).getGroupSequence(), is(GROUP_SEQUENCE_1));
    }

    @Test
    public void shouldAlwaysGetHearingEventDefinitionsWhichIsActive() {
        givenSequencedHearingEventDefinitionsExistInRandomOrder();

        List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions, hasSize(7));
        assertThat(actualEventDefinitions.get(0).getId(), is(UUID.fromString(ID_1)));
    }

    @Test
    public void shouldGetHearingEventDefinitionsInSequentialOrder() {
        givenSequencedHearingEventDefinitionsExistInRandomOrder();

        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions, hasSize(7));

        assertThat(actualEventDefinitions.get(0).getActionSequence(), is(1));
        assertThat(actualEventDefinitions.get(1).getActionSequence(), is(2));
        assertThat(actualEventDefinitions.get(6).getActionSequence(), is(2));
    }

    @Test
    public void shouldGetHearingEventDefinitionsOrderedByActionSequenceAndActionLabel() {
        givenSequencedHearingEventDefinitionsExistInRandomOrder();

        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions, hasSize(7));

        assertThat(actualEventDefinitions.get(0).getActionSequence(), is(ACTION_SEQUENCE_1));
        assertThat(actualEventDefinitions.get(1).getActionSequence(), is(ACTION_SEQUENCE_2));
        assertThat(actualEventDefinitions.get(2).getActionSequence(), is(ACTION_SEQUENCE_3));
        assertThat(actualEventDefinitions.get(3).getActionLabel(), is(ACTION_LABEL_5));
        assertThat(actualEventDefinitions.get(4).getActionLabel(), is(ACTION_LABEL_6));
    }

    @Test
    public void shouldGetHearingEventDefinitionsOrderedBySequenceTypeSequenceNumberAndActionLabel() {
        givenSequencedHearingEventDefinitionsExistInRandomOrder();
        final List<HearingEventDefinition> actualEventDefinitions = hearingEventDefinitionRepository.findAllActiveOrderBySequenceTypeSequenceNumberAndActionLabel();

        assertThat(actualEventDefinitions, hasSize(7));

        assertThat(actualEventDefinitions.get(0).getActionLabel(), is(ACTION_LABEL_1));
        assertThat(actualEventDefinitions.get(1).getActionLabel(), is(ACTION_LABEL_2));
        assertThat(actualEventDefinitions.get(2).getActionLabel(), is(ACTION_LABEL_3));
        assertThat(actualEventDefinitions.get(3).getActionLabel(), is(ACTION_LABEL_5));
        assertThat(actualEventDefinitions.get(4).getActionLabel(), is(ACTION_LABEL_6));
        assertThat(actualEventDefinitions.get(5).getActionLabel(), is(ACTION_LABEL_7));
        assertThat(actualEventDefinitions.get(6).getActionLabel(), is(ACTION_LABEL_8));
    }

    private void givenSequencedHearingEventDefinitionsExistInRandomOrder() {
        final List<HearingEventDefinition> hearingEventDefinitions = newArrayList(
                new HearingEventDefinition(UUID.fromString(ID_1), RECORDED_LABEL_1, ACTION_LABEL_1, ACTION_SEQUENCE_1, null, GROUP_LABEL_1, GROUP_SEQUENCE_1, false),
                new HearingEventDefinition(UUID.fromString(ID_2), RECORDED_LABEL_2, ACTION_LABEL_2, ACTION_SEQUENCE_2, null, GROUP_LABEL_2, GROUP_SEQUENCE_2, false),
                new HearingEventDefinition(UUID.fromString(ID_3), RECORDED_LABEL_3, ACTION_LABEL_3, ACTION_SEQUENCE_3, null, GROUP_LABEL_3, GROUP_SEQUENCE_3, false),
                new HearingEventDefinition(UUID.fromString(ID_6), RECORDED_LABEL_6, ACTION_LABEL_6, ACTION_SEQUENCE_6, null, GROUP_LABEL_6, GROUP_SEQUENCE_6, false),
                new HearingEventDefinition(UUID.fromString(ID_5), RECORDED_LABEL_5, ACTION_LABEL_5, ACTION_SEQUENCE_5, null, GROUP_LABEL_5, GROUP_SEQUENCE_5, false),
                new HearingEventDefinition(UUID.fromString(ID_7), RECORDED_LABEL_7, ACTION_LABEL_7, ACTION_SEQUENCE_7, null, GROUP_LABEL_7, GROUP_SEQUENCE_7, false),
                new HearingEventDefinition(UUID.fromString(ID_8), RECORDED_LABEL_8, ACTION_LABEL_8, ACTION_SEQUENCE_8, null, GROUP_LABEL_8, GROUP_SEQUENCE_8, false)
        );

        saveHearingEventDefinitionsInRandomOrder(hearingEventDefinitions);
    }

    private void saveHearingEventDefinitionsInRandomOrder(final List<HearingEventDefinition> hearingEventDefinitions) {
        shuffle(hearingEventDefinitions, new Random(LONG.next()));

        hearingEventDefinitions.forEach(hearingEventDefinition -> hearingEventDefinitionRepository.save(hearingEventDefinition));

        assertThat(hearingEventDefinitionRepository.findAll(), hasSize(hearingEventDefinitions.size()));
    }

}