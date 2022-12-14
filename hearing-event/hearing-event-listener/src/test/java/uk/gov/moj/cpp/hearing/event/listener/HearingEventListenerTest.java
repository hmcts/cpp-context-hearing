package uk.gov.moj.cpp.hearing.event.listener;

import static com.google.common.io.Resources.getResource;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.Charset.defaultCharset;
import static java.time.LocalDate.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithDefaults;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.BOOLEAN;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.integer;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.domain.HearingState.SHARED_AMEND_LOCKED_USER_ERROR;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.targetTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.result.CompletedResultLineStatus;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateChanged;
import uk.gov.moj.cpp.hearing.domain.event.EarliestNextHearingDateCleared;
import uk.gov.moj.cpp.hearing.domain.event.HearingAmended;
import uk.gov.moj.cpp.hearing.domain.event.HearingDaysCancelled;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.domain.event.RegisteredHearingAgainstApplication;
import uk.gov.moj.cpp.hearing.domain.event.TargetRemoved;
import uk.gov.moj.cpp.hearing.domain.event.VerdictUpsert;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSavedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;
import uk.gov.moj.cpp.hearing.domain.event.result.UpdateDraftResultSaved;
import uk.gov.moj.cpp.hearing.mapping.ApplicationDraftResultJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.TargetJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.repository.ApprovalRequestedRepository;
import uk.gov.moj.cpp.hearing.repository.DraftResultRepository;
import uk.gov.moj.cpp.hearing.repository.HearingApplicationRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HearingEventListenerTest {
    private static final UUID RI_UNKNOWN_RESULT_DEFINITON_ID = UUID.randomUUID();

    private static final UUID RI_C_RESULT_DEFINITON_ID = fromString("d0a369c9-5a28-40ec-99cb-da7943550b18");
    private static final UUID RILA_L_RESULT_DEFINITON_ID = fromString("903b3e90-f185-40d3-92dd-6f81b73c4bb2");
    private static final UUID CCIIYDA_S_RESULT_DEFINITON_ID = fromString("d271def7-14a1-4a92-a40b-b6ee5d4654ff");
    private static final UUID RIB_P_RESULT_DEFINITON_ID = fromString("e26940b7-2534-42f2-9c44-c70072bf6ad2");
    private static final UUID REMCBY_B_RESULT_DEFINITON_ID = fromString("0536dbd2-b922-4899-9bc9-cad08429a889");
    private static final UUID CCIU_U_RESULT_DEFINITON_ID = fromString("705140dc-833a-4aa0-a872-839009fc4494");

    private static final UUID BAIL_STATUS_C_REF_DATA_ID = fromString("12e69486-4d01-3403-a50a-7419ca040635");
    private static final String BAIL_STATUS_C_REF_DATA_CODE = "C";
    private static final String BAIL_STATUS_C_REF_DATA_DESC = "Custody";

    private static final UUID BAIL_STATUS_P_REF_DATA_ID = fromString("34443c87-fa6f-34c0-897f-0cce45773df5");
    private static final String BAIL_STATUS_P_REF_DATA_CODE = "P";
    private static final String BAIL_STATUS_P_REF_DATA_DESC = "Conditional Bail with Pre-Release conditions";

    private static final UUID BAIL_STATUS_L_REF_DATA_ID = fromString("4dc146db-9d89-30bf-93b3-b22bc072d666");
    private static final String BAIL_STATUS_L_REF_DATA_CODE = "L";
    private static final String BAIL_STATUS_L_REF_DATA_DESC = "Remanded into care of Local Authority";

    private static final UUID BAIL_STATUS_S_REF_DATA_ID = fromString("549336f9-2a07-3767-960f-107da761a698");
    private static final String BAIL_STATUS_S_REF_DATA_CODE = "S";
    private static final String BAIL_STATUS_S_REF_DATA_DESC = "Remanded to youth detention accommodation";

    private static final UUID BAIL_STATUS_B_REF_DATA_ID = fromString("a5e5df07-c729-3f95-bf12-957c018eb526");
    private static final String BAIL_STATUS_B_REF_DATA_CODE = "B";
    private static final String BAIL_STATUS_B_REF_DATA_DESC = "Conditional Bail";

    private static final UUID BAIL_STATUS_U_REF_DATA_ID = fromString("4cfa861d-2931-30a6-a505-ddb91c95ab74");
    private static final String BAIL_STATUS_U_REF_DATA_CODE = "U";
    private static final String BAIL_STATUS_U_REF_DATA_DESC = "Unconditional Bail";

    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Captor
    ArgumentCaptor<Hearing> saveHearingCaptor;
    @Captor
    ArgumentCaptor<DraftResult> draftResultCaptor;
    @Captor
    ArgumentCaptor<HearingApplication> saveHearingApplicationCaptor;
    @Spy
    private StringToJsonObjectConverter stringToJsonObjectConverter;
    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Spy
    @InjectMocks
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @InjectMocks
    private HearingEventListener hearingEventListener;
    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private DraftResultRepository draftResultRepository;
    @Mock
    private TargetJPAMapper targetJPAMapper;
    @Mock
    private HearingJPAMapper hearingJPAMapper;
    @Mock
    private ApplicationDraftResultJPAMapper applicationDraftResultJPAMapper;
    @Mock
    private HearingApplicationRepository hearingApplicationRepository;
    @Mock
    private ApprovalRequestedRepository approvalRequestedRepository;
    @Spy
    private BailStatusProducer bailStatusProducer;

    @Mock
    private OffenceRepository offenceRepository;

    @Captor
    private ArgumentCaptor<Offence> offenceArgumentCaptor;

    @Captor
    private ArgumentCaptor<HearingSnapshotKey> hearingSnapshotKeyArgumentCaptor;

    private static final String LAST_SHARED_DATE = "lastSharedDate";
    private static final String DIRTY = "dirty";
    private static final String RESULTS = "results";
    private static final String CHILD_RESULT_LINES = "childResultLines";

    @Before
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void draftResultSaved_shouldStoreForCustodyStatusNull() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final uk.gov.justice.core.courts.Target.Builder target = CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID());
        final JsonObject result = createObjectBuilder().add("resultCode", RI_UNKNOWN_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = createArrayBuilder()
                .add(result)
                .build();
        target.withDraftResult(createObjectBuilder().add("results", results).build().toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(target.build(), HearingState.INITIALISED, randomUUID());

        draftResultSaved.getTarget().getResultLines().remove(0);
        draftResultSaved.getTarget().getResultLines().add(CoreTestTemplates.resultLine(RI_C_RESULT_DEFINITON_ID, UUID.randomUUID(), false));

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);
        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );

        verifyNoMoreInteractions(offenceRepository);

    }

    @Test
    public void draftResultSaved_shouldNotSaveHearingOffenceBailStatusForApplicationsAsOffenceNotFound() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final uk.gov.justice.core.courts.Target.Builder target = CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID());
        final JsonObject result = Json.createObjectBuilder().add("resultCode", RILA_L_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = Json.createArrayBuilder()
                .add(result)
                .build();
        target.withDraftResult(Json.createObjectBuilder().add("results", results).build().toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(target.build(), HearingState.INITIALISED, randomUUID());

        draftResultSaved.getTarget().getResultLines().remove(0);
        draftResultSaved.getTarget().getResultLines().add(CoreTestTemplates.resultLine(RI_C_RESULT_DEFINITON_ID, UUID.randomUUID(), false));

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);
        final UUID offenceId = draftResultSaved.getTarget().getOffenceId();
        when(offenceRepository.findBy(new HearingSnapshotKey(offenceId, hearingId))).thenReturn(null);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );

        verify(offenceRepository).findBy(hearingSnapshotKeyArgumentCaptor.capture());
        assertThat(hearingSnapshotKeyArgumentCaptor.getValue().getId(),is(offenceId));
        assertThat(hearingSnapshotKeyArgumentCaptor.getValue().getHearingId(),is(hearingId));
        verifyNoMoreInteractions(this.offenceRepository);
    }


    @Test
    public void draftResultSaved_shouldStoreForCustodyStatusC() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final uk.gov.justice.core.courts.Target.Builder target = CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID());
        final JsonObject result = createObjectBuilder().add("resultCode", RI_C_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = createArrayBuilder()
                .add(result)
                .build();
        target.withDraftResult(createObjectBuilder().add("results", results).build().toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(target.build(), HearingState.INITIALISED, randomUUID());

        draftResultSaved.getTarget().getResultLines().remove(0);
        draftResultSaved.getTarget().getResultLines().add(CoreTestTemplates.resultLine(RI_C_RESULT_DEFINITON_ID, UUID.randomUUID(), false));

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);
        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );

        verify(this.offenceRepository).save(offenceArgumentCaptor.capture());

        assertThat(offenceArgumentCaptor.getValue(), isBean(Offence.class)
                .with(Offence::getBailStatusCode, is(BAIL_STATUS_C_REF_DATA_CODE))
                .with(Offence::getBailStatusId, is(BAIL_STATUS_C_REF_DATA_ID))
                .with(Offence::getBailStatusDescription, is(BAIL_STATUS_C_REF_DATA_DESC)));
    }

    @Test
    public void draftResultSaved_shouldStoreForCustodyStatusS() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());

        final JsonObject result1 = createObjectBuilder().add("resultCode", CCIIYDA_S_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = createArrayBuilder().add(result1).build();

        draftResultSaved.getTarget().setDraftResult(createObjectBuilder().add("results", results).build().toString());

        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
        verify(this.offenceRepository).save(offenceArgumentCaptor.capture());

        assertThat(offenceArgumentCaptor.getValue(), isBean(Offence.class)
                .with(Offence::getBailStatusCode, is(BAIL_STATUS_S_REF_DATA_CODE))
                .with(Offence::getBailStatusId, is(BAIL_STATUS_S_REF_DATA_ID))
                .with(Offence::getBailStatusDescription, is(BAIL_STATUS_S_REF_DATA_DESC)));
    }

    @Test
    public void draftResultSaved_shouldStoreForCustodyStatusL() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());

        final JsonObject result1 = createObjectBuilder().add("resultCode", RILA_L_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = createArrayBuilder().add(result1).build();

        draftResultSaved.getTarget().setDraftResult(createObjectBuilder().add("results", results).build().toString());

        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
        verify(this.offenceRepository).save(offenceArgumentCaptor.capture());

        assertThat(offenceArgumentCaptor.getValue(), isBean(Offence.class)
                .with(Offence::getBailStatusCode, is(BAIL_STATUS_L_REF_DATA_CODE))
                .with(Offence::getBailStatusId, is(BAIL_STATUS_L_REF_DATA_ID))
                .with(Offence::getBailStatusDescription, is(BAIL_STATUS_L_REF_DATA_DESC)));
    }

    @Test
    public void draftResultSaved_shouldStoreForCustodyStatusP() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());

        final JsonObject result1 = createObjectBuilder().add("resultCode", RIB_P_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = createArrayBuilder().add(result1).build();

        draftResultSaved.getTarget().setDraftResult(createObjectBuilder().add("results", results).build().toString());

        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
        verify(this.offenceRepository).save(offenceArgumentCaptor.capture());

        assertThat(offenceArgumentCaptor.getValue(), isBean(Offence.class)
                .with(Offence::getBailStatusCode, is(BAIL_STATUS_P_REF_DATA_CODE))
                .with(Offence::getBailStatusId, is(BAIL_STATUS_P_REF_DATA_ID))
                .with(Offence::getBailStatusDescription, is(BAIL_STATUS_P_REF_DATA_DESC)));
    }

    @Test
    public void draftResultSaved_shouldStoreForCustodyStatusU() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());

        final JsonObject result1 = createObjectBuilder().add("resultCode", CCIU_U_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = createArrayBuilder().add(result1).build();

        draftResultSaved.getTarget().setDraftResult(createObjectBuilder().add("results", results).build().toString());

        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
        verify(this.offenceRepository).save(offenceArgumentCaptor.capture());

        assertThat(offenceArgumentCaptor.getValue(), isBean(Offence.class)
                .with(Offence::getBailStatusCode, is(BAIL_STATUS_U_REF_DATA_CODE))
                .with(Offence::getBailStatusId, is(BAIL_STATUS_U_REF_DATA_ID))
                .with(Offence::getBailStatusDescription, is(BAIL_STATUS_U_REF_DATA_DESC)));
    }

    @Test
    public void draftResultSaved_shouldStoreForCustodyStatusB() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());

        final JsonObject result1 = createObjectBuilder().add("resultCode", REMCBY_B_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = createArrayBuilder().add(result1).build();

        draftResultSaved.getTarget().setDraftResult(createObjectBuilder().add("results", results).build().toString());

        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
        verify(this.offenceRepository).save(offenceArgumentCaptor.capture());

        assertThat(offenceArgumentCaptor.getValue(), isBean(Offence.class)
                .with(Offence::getBailStatusCode, is(BAIL_STATUS_B_REF_DATA_CODE))
                .with(Offence::getBailStatusId, is(BAIL_STATUS_B_REF_DATA_ID))
                .with(Offence::getBailStatusDescription, is(BAIL_STATUS_B_REF_DATA_DESC)));
    }

    @Test
    public void draftResultSaved_shouldNotStoreBailStatusIfAllResultLinesDeleted() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());

        final JsonObject result1 = createObjectBuilder().add("resultCode", RI_C_RESULT_DEFINITON_ID.toString()).add("isDeleted", true).build();
        final JsonObject result2 = createObjectBuilder().add("resultCode", RI_C_RESULT_DEFINITON_ID.toString()).add("isDeleted", true).build();
        final JsonObject result3 = createObjectBuilder().add("resultCode", RI_C_RESULT_DEFINITON_ID.toString()).add("isDeleted", true).build();
        final JsonObject result4 = createObjectBuilder().add("resultCode", RI_C_RESULT_DEFINITON_ID.toString()).add("isDeleted", true).build();
        final JsonObject result5 = createObjectBuilder().add("resultCode", RI_C_RESULT_DEFINITON_ID.toString()).add("isDeleted", true).build();
        final JsonArray results = createArrayBuilder()
                .add(result1).add(result2).add(result3).add(result4).add(result5)
                .build();
        draftResultSaved.getTarget().setDraftResult(createObjectBuilder().add("results", results).build().toString());

        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
        verify(this.offenceRepository).save(offenceArgumentCaptor.capture());

        assertThat(offenceArgumentCaptor.getValue(), isBean(Offence.class)
                .with(Offence::getBailStatusCode, is(nullValue()))
                .with(Offence::getBailStatusId, is(nullValue()))
                .with(Offence::getBailStatusDescription, is(nullValue())));
    }

    @Test
    public void draftResultSaved_shouldNotClearBailStatusIfNoResults() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());

        final JsonArray emptyResults = createArrayBuilder().build();
        draftResultSaved.getTarget().setDraftResult(createObjectBuilder().add("results", emptyResults).build().toString());

        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(null);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );

        verify(this.offenceRepository, never()).save(Mockito.any());
    }

    @Test
    public void draftResultSaved_shouldPersist_with_hasSharedResults_false() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());
        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
        verifyNoMoreInteractions(offenceRepository);
    }


    @Test
    public void draftResultSaved_shouldPersist_with_hasSharedResults_no_change() {

        final UUID hearingId = randomUUID();
        final Target targetOut = new Target();
        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED,
                randomUUID()
        );
        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(false)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );

        verifyNoMoreInteractions(offenceRepository);
    }

    @Test
    public void shouldCreateNewDraftResultSavedV2WhenDoesntExists() {

        final UUID hearingId = randomUUID();
        final UUID amendedByUser = randomUUID();
        final LocalDate hearingDay = LocalDate.now();
        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{}");
        final DraftResultSavedV2 draftResultSavedV2 = new DraftResultSavedV2(hearingId, hearingDay, someJsonObject, amendedByUser);

        when(draftResultRepository.findBy(hearingId.toString() + hearingDay)).thenReturn(null);

        hearingEventListener.draftResultSavedV2(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved-v2"),
                objectToJsonObjectConverter.convert(draftResultSavedV2)
        ));

        verify(this.draftResultRepository).save(draftResultCaptor.capture());

        assertThat(draftResultCaptor.getValue(), isBean(DraftResult.class)
                .with(DraftResult::getHearingId, is(hearingId))
                .with(DraftResult::getHearingDay, is(hearingDay.toString()))
                .with(DraftResult::getAmendedByUserId, is(amendedByUser))
                .with(DraftResult::getDraftResultPayload, is(objectMapper.convertValue(someJsonObject, JsonNode.class)))
        );
    }

    @Test
    public void shouldPatchWhenUpdateDraftResultRecieved() throws IOException {
        final UUID hearingId = randomUUID();
        final UUID amendedByUser = randomUUID();
        final LocalDate hearingDay = LocalDate.now();
        final UUID resultId = randomUUID();
        final DraftResult initialDraft = new DraftResult();
        initialDraft.setHearingId(hearingId);
        initialDraft.setHearingDay(hearingDay.toString());
        initialDraft.setAmendedByUserId(amendedByUser);

        final JsonNode initialDraftPayload = toJsonNode(new StringToJsonObjectConverter().convert(getDraftResultFromResource("initial_draft_result.json").replaceAll("HEARING_ID", hearingId.toString()).replaceAll("HEARING_DAY", hearingDay.toString())));
        initialDraft.setDraftResultPayload(initialDraftPayload);
        initialDraft.setDraftResultId(resultId.toString());

        final JsonObject someJsonObject = new StringToJsonObjectConverter().convert("{\"operations\": [{\"op\": \"remove\", \"path\": \"/draftResult/resultLines/1b8005ca-47d1-42be-8d06-7d0aa8254221\"}, {\"op\": \"remove\", \"path\": \"/draftResult/relations/0\"} ] }");
        final UpdateDraftResultSaved updateDraftResultSaved = new UpdateDraftResultSaved(hearingId, hearingDay, someJsonObject, amendedByUser);

        when(draftResultRepository.findBy(hearingId.toString() + hearingDay)).thenReturn((initialDraft));

        hearingEventListener.draftResultUpdated(envelopeFrom(metadataWithRandomUUID("hearing.update-draft-result-saved"),
                objectToJsonObjectConverter.convert(updateDraftResultSaved)
        ));

        verify(this.draftResultRepository).save(draftResultCaptor.capture());

        assertThat(draftResultCaptor.getValue(), isBean(DraftResult.class)
                .with(DraftResult::getHearingId, is(hearingId))
                .with(DraftResult::getHearingDay, is(hearingDay.toString()))
                .with(DraftResult::getAmendedByUserId, is(amendedByUser))
                .with(DraftResult::getDraftResultPayload, is(toJsonNode(new StringToJsonObjectConverter().convert(getDraftResultFromResource("hearing_patch_1_removed.json").replaceAll("HEARING_ID", hearingId.toString()).replaceAll("HEARING_DAY", hearingDay.toString()))))
                ));
    }

    @Test
    public void resultsShared_shouldPersist_with_hasSharedResults_true() {
        final ResultsShared resultsShared = resultsSharedTemplate();
        final List<uk.gov.justice.core.courts.Target> targets = asList(targetTemplate());
        final Target target = new Target()
                .setId(randomUUID());
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        final Defendant defendant = new Defendant();
        final Set<Defendant> defendants = asSet(defendant);
        prosecutionCase.setDefendants(defendants);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(false)
                .setTargets(asSet(target))
                .setProsecutionCases(asSet(prosecutionCase))
                .setId(resultsShared.getHearingId()
                );

        when(hearingRepository.findBy(resultsShared.getHearingId())).thenReturn(dbHearing);
        when(hearingRepository.findTargetsByHearingId(resultsShared.getHearingId())).thenReturn(asList(target));
        when(hearingRepository.findProsecutionCasesByHearingId(dbHearing.getId()))
                .thenReturn(Lists.newArrayList(dbHearing.getProsecutionCases()));
        when(targetJPAMapper.fromJPA(asSet(target), asSet(prosecutionCase))).thenReturn(targets);
        hearingEventListener.resultsShared(envelopeFrom(metadataWithRandomUUID("hearing.results-shared"),
                objectToJsonObjectConverter.convert(resultsShared)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());
        verify(this.approvalRequestedRepository).removeAllRequestApprovals(resultsShared.getHearingId());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(true))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getId, is(resultsShared.getHearingId()))
        );

        verifyNoMoreInteractions(offenceRepository);
    }

    @Test
    public void resultsSharedV2_shouldPersist_with_hasSharedResults_true() {
        final LocalDate today = now();
        final LocalDate tomorrow = now().plusDays(1);

        final ResultsSharedV2 resultsShared = resultsSharedV2Template(today);

        final uk.gov.justice.core.courts.Target resultForToday = targetTemplate(today);
        final uk.gov.justice.core.courts.Target resultForTomorrow = targetTemplate(tomorrow);
        final List<uk.gov.justice.core.courts.Target> targets = asList(resultForToday, resultForTomorrow);
        final Target target = new Target()
                .setId(randomUUID())
                .setHearingDay(today.toString());
        final Target target2 = new Target()
                .setId(randomUUID())
                .setHearingDay(tomorrow.toString());

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        final Defendant defendant = new Defendant();
        final Set<Defendant> defendants = asSet(defendant);
        prosecutionCase.setDefendants(defendants);

        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay hearingDay = new uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay();
        hearingDay.setDate(today);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(false)
                .setHearingDays(asSet(hearingDay))
                .setTargets(asSet(target, target2))
                .setProsecutionCases(asSet(prosecutionCase))
                .setId(resultsShared.getHearingId()
                );

        when(hearingRepository.findBy(resultsShared.getHearingId())).thenReturn(dbHearing);
        when(hearingRepository.findTargetsByHearingId(resultsShared.getHearingId())).thenReturn(asList(target, target2));
        when(hearingRepository.findProsecutionCasesByHearingId(dbHearing.getId()))
                .thenReturn(Lists.newArrayList(dbHearing.getProsecutionCases()));
        when(targetJPAMapper.fromJPA(asSet(target, target2), asSet(prosecutionCase))).thenReturn(targets);
        when(targetJPAMapper.toJPA(Mockito.eq(dbHearing), Mockito.eq(resultForToday))).thenReturn(target);
        when(targetJPAMapper.toJPA(Mockito.eq(dbHearing), Mockito.eq(resultForTomorrow))).thenReturn(target2);

        hearingEventListener.resultsSharedV2(envelopeFrom(metadataWithRandomUUID("hearing.results-shared-v2"),
                objectToJsonObjectConverter.convert(resultsShared)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());
        verify(this.approvalRequestedRepository).removeAllRequestApprovals(resultsShared.getHearingId());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(true))
                .with(Hearing::getId, is(resultsShared.getHearingId()))
                .with(Hearing::getTargets, hasSize(2))
                .with(Hearing::getHearingDays, first(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class)
                                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getHasSharedResults, is(true)
                                )
                        )
                )
        );

        verifyNoMoreInteractions(offenceRepository);
    }

    @Test
    public void resultsSharedV3_shouldPersist_with_hasSharedResults_true_and_draft_result_does_not_exist() throws IOException {
        final LocalDate today = now();
        final LocalDate tomorrow = now().plusDays(1);

        final ResultsSharedV3 resultsShared = resultsSharedV3Template(today);

        final uk.gov.justice.core.courts.Target resultForToday = targetTemplate(today);
        final uk.gov.justice.core.courts.Target resultForTomorrow = targetTemplate(tomorrow);
        final List<uk.gov.justice.core.courts.Target> targets = asList(resultForToday, resultForTomorrow);
        final Target target = new Target()
                .setId(randomUUID())
                .setHearingDay(today.toString());
        final Target target2 = new Target()
                .setId(randomUUID())
                .setHearingDay(tomorrow.toString());

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        final Defendant defendant = new Defendant();
        final Set<Defendant> defendants = asSet(defendant);
        prosecutionCase.setDefendants(defendants);

        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay hearingDay = new uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay();
        hearingDay.setDate(today);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(false)
                .setHearingDays(asSet(hearingDay))
                .setTargets(asSet(target, target2))
                .setProsecutionCases(asSet(prosecutionCase))
                .setId(resultsShared.getHearingId()
                );

        DraftResult draftResult = mock(DraftResult.class);
        JsonObject draftResultJsonObject = Json.createObjectBuilder()
                .add("__metadata__", Json.createObjectBuilder()
                        .add("version","1")
                        .build())
                .build();

        JsonNode draftResultPayload = toJsonNode(draftResultJsonObject);

        when(draftResult.getDraftResultPayload()).thenReturn(draftResultPayload);

        final String draftResultPK = resultsShared.getHearingId().toString()+resultsShared.getHearingDay().toString();
        when(draftResultRepository.findBy(draftResultPK)).thenReturn(null);
        when(hearingRepository.findBy(resultsShared.getHearingId())).thenReturn(dbHearing);
        when(hearingRepository.findTargetsByHearingId(resultsShared.getHearingId())).thenReturn(asList(target, target2));
        when(hearingRepository.findProsecutionCasesByHearingId(dbHearing.getId()))
                .thenReturn(Lists.newArrayList(dbHearing.getProsecutionCases()));
        when(targetJPAMapper.setMasterDefandantId(anyObject(), anyObject())).thenReturn(targets);
        when(targetJPAMapper.toJPA(Mockito.eq(dbHearing), Mockito.eq(resultForToday))).thenReturn(target);
        when(targetJPAMapper.toJPA(Mockito.eq(dbHearing), Mockito.eq(resultForTomorrow))).thenReturn(target2);

        hearingEventListener.resultsSharedV3(envelopeFrom(metadataWithRandomUUID("hearing.results-shared-v3"),
                objectToJsonObjectConverter.convert(resultsShared)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());
        verify(this.approvalRequestedRepository).removeAllRequestApprovals(resultsShared.getHearingId());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(true))
                .with(Hearing::getId, is(resultsShared.getHearingId()))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getHearingDays, first(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class)
                                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getHasSharedResults, is(true)
                                )
                        )
                )
        );

        verify(this.draftResultRepository,never()).save(any());
        verifyNoMoreInteractions(offenceRepository);
    }

    @Test
    public void resultsSharedV3_shouldPersist_with_hasSharedResults_true() throws IOException {
        final LocalDate today = now();
        final LocalDate tomorrow = now().plusDays(1);

        final ResultsSharedV3 resultsShared = resultsSharedV3Template(today);

        final uk.gov.justice.core.courts.Target resultForToday = targetTemplate(today);
        final uk.gov.justice.core.courts.Target resultForTomorrow = targetTemplate(tomorrow);
        final List<uk.gov.justice.core.courts.Target> targets = asList(resultForToday, resultForTomorrow);
        final Target target = new Target()
                .setId(randomUUID())
                .setHearingDay(today.toString());
        final Target target2 = new Target()
                .setId(randomUUID())
                .setHearingDay(tomorrow.toString());

        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        final Defendant defendant = new Defendant();
        final Set<Defendant> defendants = asSet(defendant);
        prosecutionCase.setDefendants(defendants);

        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay hearingDay = new uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay();
        hearingDay.setDate(today);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(false)
                .setHearingDays(asSet(hearingDay))
                .setTargets(asSet(target, target2))
                .setProsecutionCases(asSet(prosecutionCase))
                .setId(resultsShared.getHearingId()
                );

        DraftResult draftResult = mock(DraftResult.class);
        JsonObject draftResultJsonObject = Json.createObjectBuilder()
                .add("__metadata__", Json.createObjectBuilder()
                        .add("version","1")
                        .build())
                .build();

        JsonNode draftResultPayload = toJsonNode(draftResultJsonObject);

        when(draftResult.getDraftResultPayload()).thenReturn(draftResultPayload);

        final String draftResultPK = resultsShared.getHearingId().toString()+resultsShared.getHearingDay().toString();
        when(draftResultRepository.findBy(draftResultPK)).thenReturn(draftResult);
        when(hearingRepository.findBy(resultsShared.getHearingId())).thenReturn(dbHearing);
        when(hearingRepository.findTargetsByHearingId(resultsShared.getHearingId())).thenReturn(asList(target, target2));
        when(hearingRepository.findProsecutionCasesByHearingId(dbHearing.getId()))
                .thenReturn(Lists.newArrayList(dbHearing.getProsecutionCases()));
        when(targetJPAMapper.setMasterDefandantId(anyObject(), anyObject())).thenReturn(targets);
        when(targetJPAMapper.toJPA(Mockito.eq(dbHearing), Mockito.eq(resultForToday))).thenReturn(target);
        when(targetJPAMapper.toJPA(Mockito.eq(dbHearing), Mockito.eq(resultForTomorrow))).thenReturn(target2);

        hearingEventListener.resultsSharedV3(envelopeFrom(metadataWithRandomUUID("hearing.results-shared-v3"),
                objectToJsonObjectConverter.convert(resultsShared)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());
        verify(this.approvalRequestedRepository).removeAllRequestApprovals(resultsShared.getHearingId());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(true))
                .with(Hearing::getId, is(resultsShared.getHearingId()))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getHearingDays, first(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class)
                                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getHasSharedResults, is(true)
                                )
                        )
                )
        );

        assertThat(draftResult.getDraftResultPayload().get("__metadata__").get("lastSharedTime").textValue(), not(isEmptyOrNullString()));
        verify(this.draftResultRepository,times(1)).save(any());
        verifyNoMoreInteractions(offenceRepository);
    }

    public JsonNode toJsonNode(JsonObject jsonObj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(jsonObj.toString());
    }

    @Test
    public void shouldRegisterHearingAgainstApplication() {
        final UUID hearingId = randomUUID();
        final UUID applicationId = randomUUID();
        final RegisteredHearingAgainstApplication registeredHearingAgainstApplication = new RegisteredHearingAgainstApplication(applicationId, hearingId);

        hearingEventListener.registerHearingAgainstApplication(envelopeFrom(metadataWithRandomUUID("hearing.events.registered-hearing-against-application"),
                objectToJsonObjectConverter.convert(registeredHearingAgainstApplication)
        ));

        verify(this.hearingApplicationRepository).save(saveHearingApplicationCaptor.capture());

        assertThat(saveHearingApplicationCaptor.getValue().getId().getApplicationId(), is(applicationId));
        assertThat(saveHearingApplicationCaptor.getValue().getId().getHearingId(), is(hearingId));
    }

    @Test
    public void shouldHandleHearingMarkedAsDuplicate() {
        final UUID hearingId = randomUUID();
        final Hearing hearing = Mockito.mock(Hearing.class);
        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        hearingEventListener.handleHearingMarkedAsDuplicate(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(hearingRepository).remove(hearing);

        verifyNoMoreInteractions(offenceRepository);
    }

    @Test
    public void shouldHandleHearingMarkedAsDuplicateWhenExistingHearingNotFound() {
        final UUID hearingId = randomUUID();
        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        hearingEventListener.handleHearingMarkedAsDuplicate(envelopeFrom(metadataWithDefaults().build(), createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .build()));

        verify(hearingRepository, never()).remove(Mockito.any(Hearing.class));

        verifyNoMoreInteractions(offenceRepository);

    }

    private ResultsShared resultsSharedTemplate() {

        CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        UUID completedResultLineId = randomUUID();
        hearingOne.it().getHearing().setHasSharedResults(true);

        final VerdictUpsert verdictUpsert = VerdictUpsert.verdictUpsert()
                .setHearingId(hearingOne.getFirstOffenceIdForFirstDefendant())
                .setVerdict(uk.gov.justice.core.courts.Verdict.verdict()
                        .withVerdictDate(PAST_LOCAL_DATE.next())
                        .withOffenceId(hearingOne.getFirstOffenceIdForFirstDefendant())
                        .withOriginatingHearingId(randomUUID())
                        .withJurors(
                                uk.gov.justice.core.courts.Jurors.jurors()
                                        .withNumberOfJurors(integer(9, 12).next())
                                        .withNumberOfSplitJurors(integer(0, 3).next())
                                        .withUnanimous(BOOLEAN.next())
                                        .build())
                        .withVerdictType(
                                uk.gov.justice.core.courts.VerdictType.verdictType()
                                        .withId(randomUUID())
                                        .withCategoryType(STRING.next())
                                        .withCategory(STRING.next())
                                        .withDescription(STRING.next())
                                        .withSequence(INTEGER.next())
                                        .build())
                        .withLesserOrAlternativeOffence(uk.gov.justice.core.courts.LesserOrAlternativeOffence.lesserOrAlternativeOffence()
                                .withOffenceLegislationWelsh(STRING.next())
                                .withOffenceLegislation(STRING.next())
                                .withOffenceTitleWelsh(STRING.next())
                                .withOffenceTitle(STRING.next())
                                .withOffenceCode(STRING.next())
                                .withOffenceDefinitionId(randomUUID())
                                .build())
                        .build());

        return ResultsShared.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(new ArrayList<>(singletonList(
                        CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
                )))
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .build();
    }

    private ResultsSharedV2 resultsSharedV2Template(final LocalDate hearingDay) {

        CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        UUID completedResultLineId = randomUUID();
        hearingOne.it().getHearing().setHasSharedResults(true);

        return ResultsSharedV2.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(new ArrayList<>(singletonList(
                        CoreTestTemplates.target(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
                )))
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .withHearingDay(hearingDay)
                .build();
    }

    private ResultsSharedV3 resultsSharedV3Template(final LocalDate hearingDay) {

        CommandHelpers.InitiateHearingCommandHelper hearingOne = h(standardInitiateHearingTemplate());
        UUID completedResultLineId = randomUUID();
        hearingOne.it().getHearing().setHasSharedResults(true);

        return ResultsSharedV3.builder()
                .withHearingId(hearingOne.getHearingId())
                .withTargets(new ArrayList<>(singletonList(
                        CoreTestTemplates.target2(hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId(), hearingOne.getFirstOffenceIdForFirstDefendant(), completedResultLineId).build()
                )))
                .withSharedTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                .withHearing(hearingOne.getHearing())
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(randomUUID())
                        .withFirstName(STRING.next())
                        .withLastName(STRING.next())
                        .build())
                .withCompletedResultLinesStatus(ImmutableMap.of(completedResultLineId, CompletedResultLineStatus.builder()
                        .withCourtClerk(DelegatedPowers.delegatedPowers()
                                .withUserId(randomUUID())
                                .withFirstName(STRING.next())
                                .withLastName(STRING.next())
                                .build())
                        .withId(completedResultLineId)
                        .withLastSharedDateTime(PAST_ZONED_DATE_TIME.next().withZoneSameInstant(ZoneId.of("UTC")))
                        .build()
                ))
                .withVariantDirectory(singletonList(
                        standardVariantTemplate(randomUUID(), hearingOne.getHearingId(), hearingOne.getFirstDefendantForFirstCase().getId())
                ))
                .withHearingDay(hearingDay)
                .build();
    }

    @Test
    public void setInEffectiveTrialType_shouldPersist_with_hearing() {

        final UUID hearingId = randomUUID();
        final UUID trialTypeId = randomUUID();
        final Hearing hearingEntity = new Hearing()
                .setId(hearingId);
        final HearingTrialType hearingTrialType = new HearingTrialType(hearingId, trialTypeId, "A", "Effective", "full description");
        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);

        hearingEventListener.setHearingTrialType(envelopeFrom(metadataWithRandomUUID("hearing.hearing-trial-type-set"),
                objectToJsonObjectConverter.convert(hearingTrialType)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTrialTypeId, is(trialTypeId))
        );
    }

    @Test
    public void setEffectiveTrialType_shouldPersist_with_hearing() {

        final UUID hearingId = randomUUID();
        Hearing hearingEntity = new Hearing()
                .setId(hearingId);

        final HearingEffectiveTrial hearingEffectiveTrial = new HearingEffectiveTrial(hearingId, TRUE);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);

        hearingEventListener.setHearingEffectiveTrial(envelopeFrom(metadataWithRandomUUID("hearing.hearing-effective-trial-set"),
                objectToJsonObjectConverter.convert(hearingEffectiveTrial)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getIsEffectiveTrial, is(true))
        );
    }

    @Test
    public void setInVacateTrialType_shouldPersist_with_hearing() {

        final UUID hearingId = randomUUID();
        final UUID vacateTrialTypeId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final Hearing hearingEntity = new Hearing()
                .setId(hearingId);
        final HearingTrialVacated hearingTrialVacated = new HearingTrialVacated(hearingId, vacateTrialTypeId, "A", "Vacated", "full description", courtCentreId);
        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);

        hearingEventListener.setHearingVacateTrialType(envelopeFrom(metadataWithRandomUUID("hearing.trial-vacated"),
                objectToJsonObjectConverter.convert(hearingTrialVacated)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getVacatedTrialReasonId, is(vacateTrialTypeId))
        );
    }

    @Test
    public void shouldCancelHearingDaysWhenHearingNotNull() {
        final ZonedDateTime sittingDay = ZonedDateTime.now();
        final UUID hearingId = randomUUID();
        final Hearing hearing = new Hearing().setId(hearingId);
        final List<HearingDay> hearingDayList = Arrays.asList(new HearingDay.Builder().withSittingDay(sittingDay).withIsCancelled(TRUE).build());
        final HearingDaysCancelled hearingDaysCancelled = new HearingDaysCancelled(hearingId, hearingDayList);
        final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay hearingDayEntity = new uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay();

        hearingDayEntity.setSittingDay(sittingDay);
        hearingDayEntity.setIsCancelled(null);
        hearing.setHearingDays(new HashSet<>(Arrays.asList(hearingDayEntity)));

        when(hearingRepository.findBy(hearingId)).thenReturn(hearing);

        hearingEventListener.cancelHearingDays(envelopeFrom(metadataWithRandomUUID("hearing.hearing-days-cancelled"),
                objectToJsonObjectConverter.convert(hearingDaysCancelled)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());
        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getHearingDays, first(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay.class)
                        .with(uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay::getIsCancelled, is(TRUE))))
        );
    }

    @Test
    public void shouldNotCancelHearingDaysWhenHearingNull() {
        final UUID hearingId = randomUUID();
        final List<HearingDay> hearingDayList = new ArrayList<>();
        final HearingDaysCancelled hearingDaysCancelled = new HearingDaysCancelled(hearingId, hearingDayList);

        when(hearingRepository.findBy(hearingId)).thenReturn(null);

        hearingEventListener.cancelHearingDays(envelopeFrom(metadataWithRandomUUID("hearing.hearing-days-cancelled"),
                objectToJsonObjectConverter.convert(hearingDaysCancelled)
        ));

        verify(this.hearingRepository, never()).save(any());
    }

    @Test
    public void draftResultRemoved_shouldPersist_with_hasSharedResults_false() {

        final UUID hearingId = randomUUID();
        final Target targetOut = new Target();
        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED,
                randomUUID()
        );
        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getHasSharedResults, is(false))
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, hasSize(1))
                .with(Hearing::getTargets, first(is(targetOut)))
        );
    }

    @Test
    public void targetRemoved_IfPresent() {

        final UUID hearingId = randomUUID();
        final UUID targetId = randomUUID();
        final TargetRemoved targetRemoved = new TargetRemoved(hearingId, targetId);
        final Hearing dbHearing = new Hearing()
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(targetId)
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);

        hearingEventListener.targetRemoved(envelopeFrom(metadataWithRandomUUID("hearing.target-removed"),
                objectToJsonObjectConverter.convert(targetRemoved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getTargets, is(empty()))
        );
    }

    @Test
    public void targetIgnoredForRemoval_IfNotPresent() {
        // this is a specific test for production data scenario (as entries have been removed from viewstore manuallu)
        final UUID hearingId = randomUUID();
        final UUID unknownTargetId = randomUUID();
        final TargetRemoved targetRemoved = new TargetRemoved(hearingId, unknownTargetId);
        final Hearing dbHearing = new Hearing()
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(randomUUID())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);

        hearingEventListener.targetRemoved(envelopeFrom(metadataWithRandomUUID("hearing.target-removed"),
                objectToJsonObjectConverter.convert(targetRemoved)
        ));

        verify(this.hearingRepository, never()).save(saveHearingCaptor.capture());

    }

    @Test
    public void testDraftResults() throws IOException {
        final ZonedDateTime sharedTime = ZonedDateTime.now(UTC);
        final String draftResults = getDraftResultFromResource("hearing.draft-result.json");
        final String enrichedDraftResultAsString = hearingEventListener.enrichDraftResult(draftResults, sharedTime);
        final JsonObject enrichedDraftResultJson = new StringToJsonObjectConverter().convert(enrichedDraftResultAsString);
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getString(LAST_SHARED_DATE), is(sharedTime.toLocalDate().toString()));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getBoolean(DIRTY), is(false));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getString(LAST_SHARED_DATE), is(sharedTime.toLocalDate().toString()));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getBoolean(DIRTY), is(false));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getString(LAST_SHARED_DATE), is(sharedTime.toLocalDate().toString()));
        assertThat(enrichedDraftResultJson.getJsonArray(RESULTS).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getJsonArray(CHILD_RESULT_LINES).getJsonObject(0).getBoolean(DIRTY), is(false));
    }


    @Test
    public void shouldAmendHearing() {

        final UUID hearingId = randomUUID();
        final UUID userId = randomUUID();
        final Hearing hearingEntity = new Hearing()
                .setId(hearingId);

        final HearingAmended hearingAmended = new HearingAmended(hearingId, userId, SHARED_AMEND_LOCKED_USER_ERROR);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);

        hearingEventListener.amendHearing(envelopeFrom(metadataWithRandomUUID("hearing.event.amended"),
                objectToJsonObjectConverter.convert(hearingAmended)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getHearingState, is(hearingAmended.getNewHearingState()))
                .with(Hearing::getAmendedByUserId, is(hearingAmended.getUserId()))
        );
    }

    @Test
    public void shouldDeleteDraftResultV2() {

        final UUID hearingId = randomUUID();
        final UUID userId = randomUUID();
        final DraftResult draftResultEntity = new DraftResult();


        final DraftResultDeletedV2 draftResultDeletedV2 = new DraftResultDeletedV2(hearingId, now(), userId);

        when(draftResultRepository.findBy(any())).thenReturn(draftResultEntity);

        hearingEventListener.draftResultDeletedV2(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-deleted-v2"),
                objectToJsonObjectConverter.convert(draftResultDeletedV2)
        ));

        verify(this.draftResultRepository).removeAndFlush(draftResultCaptor.capture());
    }

    @Test
    public void shouldUpdateEarliestNextHearingDate() {

        final UUID hearingId = randomUUID();
        final UUID seedingHearingId = randomUUID();
        final ZonedDateTime earliestNextHearingDate = ZonedDateTime.now(ZoneId.of("UTC"));

        final Hearing hearingEntity = new Hearing()
                .setId(hearingId);

        final EarliestNextHearingDateChanged earliestNextHearingDateChanged = new EarliestNextHearingDateChanged(hearingId, seedingHearingId, earliestNextHearingDate);

        when(hearingRepository.findBy(seedingHearingId)).thenReturn(hearingEntity);

        hearingEventListener.changeEarliestNextHearingDate(envelopeFrom(metadataWithRandomUUID("hearing.events.earliest-next-hearing-date-changed"),
                objectToJsonObjectConverter.convert(earliestNextHearingDateChanged)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getEarliestNextHearingDate, is(earliestNextHearingDate))
        );
    }

    @Test
    public void shouldRemoveNextHearingsStartDate() {

        final UUID hearingId = randomUUID();

        final Hearing hearingEntity = new Hearing()
                .setId(hearingId)
                .setEarliestNextHearingDate(ZonedDateTime.now());

        final EarliestNextHearingDateCleared earliestNextHearingDateCleared = new EarliestNextHearingDateCleared(hearingId);

        when(hearingRepository.findBy(hearingId)).thenReturn(hearingEntity);

        hearingEventListener.removeNextHearingsStartDate(envelopeFrom(metadataWithRandomUUID("hearing.events.next-hearings-start-date-removed"),
                objectToJsonObjectConverter.convert(earliestNextHearingDateCleared)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());

        assertThat(saveHearingCaptor.getValue(), isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getEarliestNextHearingDate, nullValue())
        );
    }

    @Test
    public void draftResultSaved_withBailStatus() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = now();
        final Target targetOut = new Target().setHearingDay(hearingDay.toString());

        final DraftResultSaved draftResultSaved = new DraftResultSaved(
                CoreTestTemplates.target(hearingId, hearingDay, randomUUID(), randomUUID(), randomUUID()).build(),
                HearingState.INITIALISED, randomUUID());

        final JsonObject result1 = createObjectBuilder().add("resultCode", RIB_P_RESULT_DEFINITON_ID.toString()).add("isDeleted", false).build();
        final JsonArray results = createArrayBuilder().add(result1).build();

        draftResultSaved.getTarget().setDraftResult(createObjectBuilder().add("results", results).build().toString());

        final Offence offence = new Offence();
        when(offenceRepository.findBy(new HearingSnapshotKey(draftResultSaved.getTarget().getOffenceId(), hearingId))).thenReturn(offence);

        final Hearing dbHearing = new Hearing()
                .setHasSharedResults(true)
                .setId(hearingId)
                .setTargets(asSet(new Target()
                        .setId(draftResultSaved.getTarget().getTargetId())
                ));

        when(hearingRepository.findBy(hearingId)).thenReturn(dbHearing);
        when(targetJPAMapper.toJPA(dbHearing, draftResultSaved.getTarget())).thenReturn(targetOut);

        hearingEventListener.draftResultSaved(envelopeFrom(metadataWithRandomUUID("hearing.draft-result-saved"),
                objectToJsonObjectConverter.convert(draftResultSaved)
        ));

        verify(this.hearingRepository).save(saveHearingCaptor.capture());
        verify(this.offenceRepository).save(offenceArgumentCaptor.capture());

        assertThat(offenceArgumentCaptor.getValue(), isBean(Offence.class)
                .with(Offence::getBailStatusCode, is(BAIL_STATUS_P_REF_DATA_CODE))
                .with(Offence::getBailStatusId, is(BAIL_STATUS_P_REF_DATA_ID))
                .with(Offence::getBailStatusDescription, is(BAIL_STATUS_P_REF_DATA_DESC)));
    }

    private String getDraftResultFromResource(final String path) throws IOException {
        return Resources.toString(getResource(path), defaultCharset());
    }
}
