package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.core.courts.DefenceCounsel;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.OffencesRemovedFromExistingHearing;
import uk.gov.moj.cpp.hearing.mapping.HearingDefenceCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingDefenceCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UpdateOffencesAndDefenceCounselsForDefendantEventListenerTest {

    public static final String HEARING = "Hearing";

    @InjectMocks
    private UpdateOffencesForDefendantEventListener updateOffencesForDefendantEventListener;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingDefenceCounselRepository hearingDefenceCounselRepository;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Mock
    private HearingDefenceCounselJPAMapper hearingDefenceCounselJPAMapper;

    @Spy
    private UpdateOffencesForDefendantService updateOffencesForDefendantService;

    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();


    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldRemoveDefendantOffenceDefenceCounselsOnRemovingOffenceFromExistingHearingWhenOffenceToBeRemoved() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionId = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID defenceCounselId1 = randomUUID();
        final UUID defenceCounselId2 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();

        final List<UUID> offenceIds = Collections.singletonList(offenceId1);
        final List<UUID> defendantIds = Collections.singletonList(defendantId1);

        final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing = new OffencesRemovedFromExistingHearing(hearingId, new ArrayList<>(), defendantIds, offenceIds, HEARING);
        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offencesRemovedFromExistingHearing));
        // Defendants
        final Defendant defendant1 = getDefendant(hearingId, defendantId1, Collections.singletonList(offenceId1));
        final Defendant defendant2 = getDefendant(hearingId, defendantId2, Collections.singletonList(offenceId2));

        // POJO Defence Counsels
        final DefenceCounsel defenceCounsel1 = getDefenceCounsel(defenceCounselId1, Arrays.asList(defendantId1));
        final DefenceCounsel defenceCounsel2 = getDefenceCounsel(defenceCounselId2, Arrays.asList(defendantId2));

        // Defence Counsels
        final HearingDefenceCounsel hearingDefenceCounsel1 = getHearingDefenceCounsel(defenceCounselId1, Arrays.asList(defendantId1), hearingId);
        final HearingDefenceCounsel hearingDefenceCounsel2 = getHearingDefenceCounsel(defenceCounselId2, Arrays.asList(defendantId2), hearingId);

        final Hearing hearingDBEntity = getHearing(hearingId, prosecutionId, Arrays.asList(defendant1, defendant2), Arrays.asList(hearingDefenceCounsel1, hearingDefenceCounsel2));

        when(hearingRepository.findBy(hearingId)).thenReturn(hearingDBEntity);
        when(hearingDefenceCounselJPAMapper.fromJPA(hearingDefenceCounsel1)).thenReturn(defenceCounsel1);
        when(hearingDefenceCounselJPAMapper.fromJPA(hearingDefenceCounsel2)).thenReturn(defenceCounsel2);
        when(hearingDefenceCounselJPAMapper.toJPA(hearingDBEntity, defenceCounsel1)).thenReturn(hearingDefenceCounsel1);
        when(hearingDefenceCounselJPAMapper.toJPA(hearingDBEntity, defenceCounsel2)).thenReturn(hearingDefenceCounsel2);

        updateOffencesForDefendantEventListener.removeOffencesFromExistingAllocatedHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        final ArgumentCaptor<HearingDefenceCounsel> hearingDefenceCounselArgumentCaptor = ArgumentCaptor.forClass(HearingDefenceCounsel.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());
        verify(hearingDefenceCounselRepository).saveAndFlush(hearingDefenceCounselArgumentCaptor.capture());

        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        assertThat(hearingOut, isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getProsecutionCases, hasSize(1))
                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                        .with(ProsecutionCase::getDefendants, hasSize(1))
                        )
                )
        );

        final HearingDefenceCounsel hearingDefenceCounselOut = hearingDefenceCounselArgumentCaptor.getValue();

        assertThat(hearingDefenceCounselOut, isBean(HearingDefenceCounsel.class)
                .with(HearingDefenceCounsel::getId, is(hearingDefenceCounsel1.getId()))
                .with(HearingDefenceCounsel::isDeleted, is(Boolean.TRUE))
        );

        assertTrue(hearingDefenceCounselOut.getPayload().toString().contains(defendantId1.toString()));
    }

    @Test
    public void shouldUpdateDefenceCounselsOnRemovingDefendantFromExistingHearingWhenOffenceToBeRemoved() {

        final UUID hearingId = randomUUID();
        final UUID prosecutionId = randomUUID();
        final UUID defendantId1 = randomUUID();
        final UUID defendantId2 = randomUUID();
        final UUID defendantId3 = randomUUID();
        final UUID defenceCounselId1 = randomUUID();
        final UUID defenceCounselId2 = randomUUID();
        final UUID offenceId1 = randomUUID();
        final UUID offenceId2 = randomUUID();
        final UUID offenceId3 = randomUUID();

        final List<UUID> offenceIds = Arrays.asList(offenceId2);
        final List<UUID> defendantIds = Arrays.asList(defendantId2);

        final OffencesRemovedFromExistingHearing offencesRemovedFromExistingHearing = new OffencesRemovedFromExistingHearing(hearingId, new ArrayList<>(), defendantIds, offenceIds, HEARING);
        final JsonEnvelope envelope = envelopeFrom((Metadata) null, objectToJsonObjectConverter.convert(offencesRemovedFromExistingHearing));
        // Defendants
        final Defendant defendant1 = getDefendant(hearingId, defendantId1, Collections.singletonList(offenceId1));
        final Defendant defendant2 = getDefendant(hearingId, defendantId2, Collections.singletonList(offenceId2));
        final Defendant defendant3 = getDefendant(hearingId, defendantId3, Collections.singletonList(offenceId3));

        // POJO Defence Counsels
        final DefenceCounsel defenceCounsel1 = getDefenceCounsel(defenceCounselId1, Arrays.asList(defendantId1));
        final DefenceCounsel defenceCounsel2 = getDefenceCounsel(defenceCounselId2, Arrays.asList(defendantId2, defendantId3));

        final DefenceCounsel defenceCounsel2Updated = getDefenceCounsel(defenceCounselId2, Arrays.asList(defendantId3));

        // Defence Counsels
        final HearingDefenceCounsel hearingDefenceCounsel1 = getHearingDefenceCounsel(defenceCounselId1, Arrays.asList(defendantId1), hearingId);
        final HearingDefenceCounsel hearingDefenceCounsel2 = getHearingDefenceCounsel(defenceCounselId2, Arrays.asList(defendantId2, defendantId3), hearingId);
        final HearingDefenceCounsel hearingDefenceCounsel2Updated = getHearingDefenceCounsel(defenceCounselId2, Arrays.asList(defendantId3), hearingId);

        final Hearing hearingDBEntity = getHearing(hearingId, prosecutionId, Arrays.asList(defendant1, defendant2, defendant3), Arrays.asList(hearingDefenceCounsel1, hearingDefenceCounsel2));

        when(hearingRepository.findBy(hearingId)).thenReturn(hearingDBEntity);
        when(hearingDefenceCounselJPAMapper.fromJPA(hearingDefenceCounsel1)).thenReturn(defenceCounsel1);
        when(hearingDefenceCounselJPAMapper.fromJPA(hearingDefenceCounsel2)).thenReturn(defenceCounsel2);
        when(hearingDefenceCounselJPAMapper.toJPA(hearingDBEntity, defenceCounsel1)).thenReturn(hearingDefenceCounsel1);
        when(hearingDefenceCounselJPAMapper.toJPA(hearingDBEntity, defenceCounsel2)).thenReturn(hearingDefenceCounsel2);
        when(hearingDefenceCounselJPAMapper.toJPA(hearingDBEntity, defenceCounsel2Updated)).thenReturn(hearingDefenceCounsel2Updated);

        updateOffencesForDefendantEventListener.removeOffencesFromExistingAllocatedHearing(envelope);

        final ArgumentCaptor<Hearing> hearingArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);
        final ArgumentCaptor<HearingDefenceCounsel> hearingDefenceCounselArgumentCaptor = ArgumentCaptor.forClass(HearingDefenceCounsel.class);

        verify(hearingRepository).save(hearingArgumentCaptor.capture());
        verify(hearingDefenceCounselRepository).saveAndFlush(hearingDefenceCounselArgumentCaptor.capture());

        final Hearing hearingOut = hearingArgumentCaptor.getValue();

        assertThat(hearingOut, isBean(Hearing.class)
                .with(Hearing::getId, is(hearingId))
                .with(Hearing::getProsecutionCases, hasSize(1))
                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, hasSize(2))
                        )
                )
        );

        final HearingDefenceCounsel hearingDefenceCounselOut = hearingDefenceCounselArgumentCaptor.getValue();

        assertThat(hearingDefenceCounselOut, isBean(HearingDefenceCounsel.class)
                .with(HearingDefenceCounsel::getId, is(hearingDefenceCounsel2.getId()))
                .with(HearingDefenceCounsel::isDeleted, is(Boolean.FALSE))
        );

        assertTrue(hearingDefenceCounselOut.getPayload().toString().contains(defendantId3.toString()));
    }


    private DefenceCounsel getDefenceCounsel(final UUID defenceCounselId, final List<UUID> defendants) {
        return DefenceCounsel.defenceCounsel()
                .withId(defenceCounselId)
                .withDefendants(new ArrayList<>(defendants))
                .build();
    }

    private Hearing getHearing(final UUID hearingId, final UUID prosecutionId, final List<Defendant> defendants, final List<HearingDefenceCounsel> hearingDefenceCounsels) {
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(prosecutionId, hearingId));
        prosecutionCase.getDefendants().addAll(defendants);

        final Hearing hearingDBEntity = new Hearing();
        hearingDBEntity.setId(hearingId);
        hearingDBEntity.getProsecutionCases().add(prosecutionCase);
        hearingDBEntity.getDefenceCounsels().addAll(hearingDefenceCounsels);
        return hearingDBEntity;
    }

    private HearingDefenceCounsel getHearingDefenceCounsel(final UUID defenceCounselId, final List<UUID> defendantIds, final UUID hearingId) {
        final HearingDefenceCounsel defenceCounsel = new HearingDefenceCounsel();
        defenceCounsel.setId(new HearingSnapshotKey(defenceCounselId, hearingId));

        final JsonArrayBuilder defendants = createArrayBuilder();
        defendantIds.forEach(defendantId -> defendants.add(defendantId.toString()));

        final JsonObjectBuilder payLoad = createObjectBuilder()
                .add("defendants", defendants)
                .add("id", defenceCounselId.toString());

        final JsonNode jsonNode = mapper.valueToTree(payLoad.build());
        defenceCounsel.setPayload(jsonNode);
        return defenceCounsel;
    }

    private Defendant getDefendant(final UUID hearingId, final UUID defendantId, final List<UUID> offenceIds) {
        final Defendant defendant = new Defendant();
        offenceIds.forEach(offenceId -> {
            final Offence offence = getOffence(hearingId, defendantId, defendant, offenceId);
            defendant.getOffences().add(offence);
        });
        return defendant;
    }

    private Offence getOffence(final UUID hearingId, final UUID defendantId, final Defendant defendant, final UUID offenceId) {
        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        return offence;
    }
}
