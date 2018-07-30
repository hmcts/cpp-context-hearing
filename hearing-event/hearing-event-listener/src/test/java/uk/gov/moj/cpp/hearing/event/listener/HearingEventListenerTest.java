package uk.gov.moj.cpp.hearing.event.listener;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUIDAndName;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ui.HearingOutcome;
import uk.gov.moj.cpp.hearing.repository.HearingOutcomeRepository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventListenerTest {

    private static final String FIELD_ORDERED_DATE = "orderedDate";
    private static final String FIELD_HEARING_ID = "hearingId";
    private static final String FIELD_NUMBER_OF_JURORS = "numberOfJurors";
    private static final String FIELD_NUMBER_OF_SPLIT_JURORS = "numberOfSplitJurors";
    private static final String FIELD_UNANIMOUS = "unanimous";

    private static final String FIELD_CASE_ID = "caseId";

    private static final String FIELD_PERSON_ID = "personId";
    private static final String FIELD_DEFENDANT_ID = "defendantId";

    private static final String FIELD_TARGET_ID = "targetId";
    private static final String FIELD_OFFENCE_ID = "offenceId";
    private static final String FIELD_DRAFT_RESULT = "draftResult";

    private static final String FIELD_GENERIC_ID = "id";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_COMPLETED_RESULT_LINES = "completedResultLines";
    private static final String FIELD_RESULT_LABEL = "resultLabel";
    private static final String FIELD_PROMPTS = "prompts";
    private static final String FIELD_PROMPT_ID = "id";
    private static final String FIELD_PROMPT_LABEL = "label";
    private static final String FIELD_PROMPT_VALUE = "value";
    private static final String FIELD_SHARED_TIME = "sharedTime";

    private static final String FIELD_VALUE_ID = "id";
    private static final String FIELD_VALUE_CATEGORY = "category";
    private static final String FIELD_VALUE_CODE = "code";
    private static final String FIELD_VALUE_DESCRIPTION = "description";

    private static final UUID HEARING_ID = randomUUID();

    private static final UUID DEFENDANT_ID = randomUUID();
    private static final UUID TARGET_ID = randomUUID();
    private static final UUID OFFENCE_ID = randomUUID();

    private static final LocalDate ORDERED_DATE = LocalDate.now();

    private static final UUID OFFENCE_ID_2 = randomUUID();
    private static final UUID OFFENCE_ID_3 = randomUUID();
    private static final UUID TARGET_ID_2 = randomUUID();
    private static final UUID TARGET_ID_3 = randomUUID();
    private static final UUID TARGET_ID_4 = randomUUID();

    private static final ZonedDateTime START_DATE_TIME = PAST_ZONED_DATE_TIME.next();
    private static final LocalDate START_DATE = START_DATE_TIME.toLocalDate();

    private static final int NUMBER_OF_JURORS = 10;
    private static final int NUMBER_OF_SPLIT_JURORS = 2;
    private static final boolean UNANIMOUS = false;
    private static final UUID CASE_ID = randomUUID();

    private static final UUID PERSON_ID = randomUUID();
    private static final UUID DEFENDANT_ID_2 = randomUUID();

    private static final String LEVEL = "OFFENCE";
    private static final String RESULT_LABEL = "Imprisonment";
    private static final UUID PROMPT_ID_1 = randomUUID();
    private static final String PROMPT_LABEL_1 = "Imprisonment duration";
    private static final String PROMPT_VALUE_1 = "1 year 6 months";
    private static final UUID PROMPT_ID_2 = randomUUID();
    private static final String PROMPT_LABEL_2 = "Prison";
    private static final String PROMPT_VALUE_2 = "Wormwood Scrubs";
    private static final ZonedDateTime SHARED_TIME = PAST_ZONED_DATE_TIME.next();

    private static final UUID RESULT_LINE_ID = randomUUID();
    private static final UUID RESULT_LINE_ID_2 = randomUUID();
    private static final UUID RESULT_LINE_ID_3 = randomUUID();
    private static final UUID RESULT_LINE_ID_4 = randomUUID();

    private static final String DRAFT_RESULT = "{\"targetId\":\"" + TARGET_ID + "\",\"caseId\":\"" + CASE_ID + "\",\"defendantId\":\"" + DEFENDANT_ID + "\",\"offenceId\":\"" + OFFENCE_ID + "\",\"offenceNum\":1,\"showDefendantName\":true,\"addMoreResults\":false,\"results\":[{\"resultLineId\":\"" + RESULT_LINE_ID + "\",\"originalText\":\"vs£500\",\"resultCode\":\"12dc713a-04dc-4613-8af0-9d962c08af0d\",\"resultLevel\":\"C\",\"isCompleted\":true,\"parts\":[{\"value\":\"Surcharge\",\"type\":\"RESULT\",\"state\":\"RESOLVED\",\"resultChoices\":[]},{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"value\":\"£500\",\"type\":\"CURR\",\"state\":\"RESOLVED\",\"resultChoices\":[]}],\"choices\":[{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"type\":\"CURR\",\"required\":true}]}]}";
    private static final String UPDATED_DRAFT_RESULT = "{\"targetId\":\"" + TARGET_ID + "\",\"caseId\":\"" + CASE_ID + "\",\"defendantId\":\"" + DEFENDANT_ID + "\",\"offenceId\":\"" + OFFENCE_ID + "\",\"offenceNum\":1,\"showDefendantName\":true,\"addMoreResults\":false,\"results\":[{\"resultLineId\":\"" + RESULT_LINE_ID + "\",\"originalText\":\"vs£500\",\"resultCode\":\"12dc713a-04dc-4613-8af0-9d962c08af0d\",\"resultLevel\":\"C\",\"isCompleted\":true,\"parts\":[{\"value\":\"Surcharge\",\"type\":\"RESULT\",\"state\":\"RESOLVED\",\"resultChoices\":[]},{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"value\":\"£500\",\"type\":\"CURR\",\"state\":\"RESOLVED\",\"resultChoices\":[]}],\"choices\":[{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"type\":\"CURR\",\"required\":true}]}]}";
    private static final String DRAFT_RESULT_2 = "{\"targetId\":\"" + TARGET_ID_2 + "\",\"caseId\":\"" + CASE_ID + "\",\"defendantId\":\"" + DEFENDANT_ID_2 + "\",\"offenceId\":\"" + OFFENCE_ID_2 + "\",\"offenceNum\":1,\"showDefendantName\":true,\"addMoreResults\":false,\"results\":[{\"resultLineId\":\"" + RESULT_LINE_ID_2 + "\",\"originalText\":\"vs£500\",\"resultCode\":\"12dc713a-04dc-4613-8af0-9d962c08af0d\",\"resultLevel\":\"C\",\"isCompleted\":false,\"parts\":[{\"value\":\"Surcharge\",\"type\":\"RESULT\",\"state\":\"RESOLVED\",\"resultChoices\":[]},{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"value\":\"£500\",\"type\":\"CURR\",\"state\":\"RESOLVED\",\"resultChoices\":[]}],\"choices\":[{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"type\":\"CURR\",\"required\":true}]}]}";
    private static final String UPDATED_DRAFT_RESULT_2 = "{\"targetId\":\"" + TARGET_ID_2 + "\",\"caseId\":\"" + CASE_ID + "\",\"defendantId\":\"" + DEFENDANT_ID_2 + "\",\"offenceId\":\"" + OFFENCE_ID_2 + "\",\"offenceNum\":1,\"showDefendantName\":true,\"addMoreResults\":false,\"results\":[{\"resultLineId\":\"" + RESULT_LINE_ID_2 + "\",\"originalText\":\"vs£500\",\"resultCode\":\"12dc713a-04dc-4613-8af0-9d962c08af0d\",\"resultLevel\":\"C\",\"isCompleted\":false,\"parts\":[{\"value\":\"Surcharge\",\"type\":\"RESULT\",\"state\":\"RESOLVED\",\"resultChoices\":[]},{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"value\":\"£500\",\"type\":\"CURR\",\"state\":\"RESOLVED\",\"resultChoices\":[]}],\"choices\":[{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"type\":\"CURR\",\"required\":true}]}]}";
    private static final String DRAFT_RESULT_3 = "{\"targetId\":\"" + TARGET_ID_3 + "\",\"caseId\":\"" + CASE_ID + "\",\"defendantId\":\"" + DEFENDANT_ID_2 + "\",\"offenceId\":\"" + OFFENCE_ID_3 + "\",\"offenceNum\":1,\"showDefendantName\":true,\"addMoreResults\":false,\"results\":[{\"resultLineId\":\"" + RESULT_LINE_ID_3 + "\",\"originalText\":\"vs£500\",\"resultCode\":\"12dc713a-04dc-4613-8af0-9d962c08af0d\",\"resultLevel\":\"C\",\"isCompleted\":true,\"parts\":[{\"value\":\"Surcharge\",\"type\":\"RESULT\",\"state\":\"RESOLVED\",\"resultChoices\":[]},{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"value\":\"£500\",\"type\":\"CURR\",\"state\":\"RESOLVED\",\"resultChoices\":[]}],\"choices\":[{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"type\":\"CURR\",\"required\":true}]}]}";
    private static final String UPDATED_DRAFT_RESULT_3 = "{\"targetId\":\"" + TARGET_ID_3 + "\",\"caseId\":\"" + CASE_ID + "\",\"defendantId\":\"" + DEFENDANT_ID_2 + "\",\"offenceId\":\"" + OFFENCE_ID_3 + "\",\"offenceNum\":1,\"showDefendantName\":true,\"addMoreResults\":false,\"results\":[{\"resultLineId\":\"" + RESULT_LINE_ID_3 + "\",\"originalText\":\"vs£500\",\"resultCode\":\"12dc713a-04dc-4613-8af0-9d962c08af0d\",\"resultLevel\":\"C\",\"isCompleted\":true,\"parts\":[{\"value\":\"Surcharge\",\"type\":\"RESULT\",\"state\":\"RESOLVED\",\"resultChoices\":[]},{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"value\":\"£500\",\"type\":\"CURR\",\"state\":\"RESOLVED\",\"resultChoices\":[]}],\"choices\":[{\"code\":\"8bfc5e44-ca2f-45e3-8b5f-fcbe397f913f\",\"label\":\"Amount of surcharge\",\"type\":\"CURR\",\"required\":true}]}]}";

    private static final UUID PLEA_ID = randomUUID();
    private static final UUID VERDICT_ID = randomUUID();
    private static final UUID VERDICT_VALUE_ID = randomUUID();
    private static final String VERDICT_VALUE_CATEGORY = STRING.next();
    private static final String VERDICT_VALUE_CODE = STRING.next();
    private static final String VERDICT_VALUE_DESCRIPTION = STRING.next();
    private static final String FIELD_VERDICT_DATE = "verdictDate";
    private static final String PLEA_VALUE = "GUILTY";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_PLEA_DATE = "pleaDate";

    @Mock
    private HearingOutcomeRepository hearingOutcomeRepository;

    @Captor
    private ArgumentCaptor<HearingOutcome> hearingOutcomeArgumentCaptor;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private HearingEventListener hearingEventListener;

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "mapper",
                new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldPersistHearingDraftResult() {
        final JsonEnvelope event = getSaveDraftResultJsonEnvelope();

        this.hearingEventListener.draftResultSaved(event);

        verify(this.hearingOutcomeRepository).save(this.hearingOutcomeArgumentCaptor.capture());
        assertThat(this.hearingOutcomeArgumentCaptor.getValue().getId(), is(TARGET_ID));
        assertThat(this.hearingOutcomeArgumentCaptor.getValue().getHearingId(), is(HEARING_ID));
        assertThat(this.hearingOutcomeArgumentCaptor.getValue().getDraftResult(), is(DRAFT_RESULT));
        assertThat(this.hearingOutcomeArgumentCaptor.getValue().getDefendantId(), is(DEFENDANT_ID));
        assertThat(this.hearingOutcomeArgumentCaptor.getValue().getOffenceId(), is(OFFENCE_ID));
    }

    @Test
    public void shouldUpdateDraftResultWithLastSharedResultIdsWhenResultsAreShared() {
        final JsonEnvelope event = getResultsSharedJsonEnvelope();
        when(this.hearingOutcomeRepository.findByHearingId(HEARING_ID)).thenReturn(getHearingOutcomesForSharedResults());

        this.hearingEventListener.updateDraftResultWithFromSharedResults(event);

        verify(this.hearingOutcomeRepository, times(3)).save(this.hearingOutcomeArgumentCaptor.capture());
        final List<HearingOutcome> expectedHearingOutcomes = this.hearingOutcomeArgumentCaptor.getAllValues();
        assertThat(expectedHearingOutcomes, hasSize(3));
        assertThat(expectedHearingOutcomes.get(0).getId(), is(TARGET_ID));
        assertThat(expectedHearingOutcomes.get(0).getHearingId(), is(HEARING_ID));
        assertThat(expectedHearingOutcomes.get(0).getDefendantId(), is(DEFENDANT_ID));
        assertThat(expectedHearingOutcomes.get(0).getOffenceId(), is(OFFENCE_ID));
        assertThat(expectedHearingOutcomes.get(0).getDraftResult(), is(UPDATED_DRAFT_RESULT));

        assertThat(expectedHearingOutcomes.get(1).getId(), is(TARGET_ID_2));
        assertThat(expectedHearingOutcomes.get(1).getHearingId(), is(HEARING_ID));
        assertThat(expectedHearingOutcomes.get(1).getDefendantId(), is(DEFENDANT_ID_2));
        assertThat(expectedHearingOutcomes.get(1).getOffenceId(), is(OFFENCE_ID_2));
        assertThat(expectedHearingOutcomes.get(1).getDraftResult(), is(UPDATED_DRAFT_RESULT_2));

        assertThat(expectedHearingOutcomes.get(2).getId(), is(TARGET_ID_3));
        assertThat(expectedHearingOutcomes.get(2).getHearingId(), is(HEARING_ID));
        assertThat(expectedHearingOutcomes.get(2).getDefendantId(), is(DEFENDANT_ID_2));
        assertThat(expectedHearingOutcomes.get(2).getOffenceId(), is(OFFENCE_ID_3));
        assertThat(expectedHearingOutcomes.get(2).getDraftResult(), is(UPDATED_DRAFT_RESULT_3));
    }


    //TODO: GPE-3390  shouldUpdateDraftResultWithLastSharedResultIdsWhenResultIsAmended

    private List<HearingOutcome> getHearingOutcomesForSharedResults() {
        return newArrayList(
                new HearingOutcome(OFFENCE_ID, HEARING_ID, DEFENDANT_ID, TARGET_ID, DRAFT_RESULT),
                new HearingOutcome(OFFENCE_ID_2, HEARING_ID, DEFENDANT_ID_2, TARGET_ID_2, DRAFT_RESULT_2),
                new HearingOutcome(OFFENCE_ID_3, HEARING_ID, DEFENDANT_ID_2, TARGET_ID_3, DRAFT_RESULT_3)
        );
    }

    private JsonEnvelope getSaveDraftResultJsonEnvelope() {
        return envelope()
                .withPayloadOf(HEARING_ID, FIELD_HEARING_ID)
                .withPayloadOf(DEFENDANT_ID, FIELD_DEFENDANT_ID)
                .withPayloadOf(TARGET_ID, FIELD_TARGET_ID)
                .withPayloadOf(OFFENCE_ID, FIELD_OFFENCE_ID)
                .withPayloadOf(DRAFT_RESULT, FIELD_DRAFT_RESULT)
                .build();
    }

    private JsonEnvelope getResultsSharedJsonEnvelope() {
        final JsonArrayBuilder completedResultLines = createArrayBuilder()
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, RESULT_LINE_ID.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                        .add(FIELD_ORDERED_DATE, ORDERED_DATE.toString())
                        .add(FIELD_LEVEL, LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                        .add(FIELD_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_ID, PROMPT_ID_1.toString())
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_1)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_1))
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_ID, PROMPT_ID_2.toString())
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_2)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_2))))
                .add(createObjectBuilder()
                        .add(FIELD_GENERIC_ID, RESULT_LINE_ID_3.toString())
                        .add(FIELD_PERSON_ID, PERSON_ID.toString())
                        .add(FIELD_CASE_ID, CASE_ID.toString())
                        .add(FIELD_OFFENCE_ID, OFFENCE_ID_3.toString())
                        .add(FIELD_LEVEL, LEVEL)
                        .add(FIELD_RESULT_LABEL, RESULT_LABEL)
                        .add(FIELD_PROMPTS, createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_1)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_1))
                                .add(createObjectBuilder()
                                        .add(FIELD_PROMPT_LABEL, PROMPT_LABEL_2)
                                        .add(FIELD_PROMPT_VALUE, PROMPT_VALUE_2))));

        final JsonObject resultsShared = createObjectBuilder()
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_SHARED_TIME, ZonedDateTimes.toString(SHARED_TIME))
                .add(FIELD_COMPLETED_RESULT_LINES, completedResultLines)
                .build();

        return envelopeFrom(metadataWithRandomUUIDAndName(), resultsShared);
    }

    public JsonEnvelope getHearingPleaEnvelope() {
        final JsonObject pleaObject = createObjectBuilder().add(FIELD_GENERIC_ID, PLEA_ID.toString()).add(FIELD_VALUE, PLEA_VALUE).add(FIELD_PLEA_DATE, START_DATE.toString()).build();
        final JsonObject hearingPlea = createObjectBuilder().add(FIELD_CASE_ID, CASE_ID.toString())
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_PERSON_ID, PERSON_ID.toString())
                .add(FIELD_DEFENDANT_ID, DEFENDANT_ID.toString())
                .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                .add("plea", pleaObject).build();
        return envelopeFrom(metadataWithRandomUUIDAndName(), hearingPlea);
    }

    public JsonEnvelope getHearingVerdictEnvelope() {
        final JsonObject verdictValueObject = createObjectBuilder()
                .add(FIELD_VALUE_ID, VERDICT_VALUE_ID.toString())
                .add(FIELD_VALUE_CATEGORY, VERDICT_VALUE_CATEGORY)
                .add(FIELD_VALUE_CODE, VERDICT_VALUE_CODE)
                .add(FIELD_VALUE_DESCRIPTION, VERDICT_VALUE_DESCRIPTION)
                .build();

        final JsonObject verdictObject = createObjectBuilder().add(FIELD_GENERIC_ID, VERDICT_ID.toString())
                .add(FIELD_VALUE, verdictValueObject)
                .add(FIELD_VERDICT_DATE, START_DATE.toString())
                .add(FIELD_NUMBER_OF_JURORS, NUMBER_OF_JURORS)
                .add(FIELD_NUMBER_OF_SPLIT_JURORS, NUMBER_OF_SPLIT_JURORS)
                .add(FIELD_UNANIMOUS, UNANIMOUS)
                .build();

        final JsonObject hearingVerdict = createObjectBuilder().add(FIELD_CASE_ID, CASE_ID.toString())
                .add(FIELD_HEARING_ID, HEARING_ID.toString())
                .add(FIELD_PERSON_ID, PERSON_ID.toString())
                .add(FIELD_DEFENDANT_ID, DEFENDANT_ID.toString())
                .add(FIELD_OFFENCE_ID, OFFENCE_ID.toString())
                .add("verdict", verdictObject).build();
        return envelopeFrom(metadataWithRandomUUIDAndName(), hearingVerdict);
    }
}
