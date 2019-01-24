package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.PleaValue;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InheritedPlea;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DelegatedPowers;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Plea;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.io.StringReader;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InitiateHearingEventListenerTest {

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    @Mock
    private PleaJPAMapper pleaJPAMapper;

    @Mock
    private OffenceRepository offenceRepository;

    @InjectMocks
    private InitiateHearingEventListener initiateHearingEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldInsertHearingWhenInitiated() {

        final InitiateHearingCommand command = minimumInitiateHearingTemplate();

        final uk.gov.justice.core.courts.Hearing hearing = command.getHearing();

        when(hearingJPAMapper.toJPA(hearing)).thenReturn(new Hearing());

        initiateHearingEventListener.newHearingInitiated(getInitiateHearingJsonEnvelope(hearing));

        final ArgumentCaptor<Hearing> hearingExArgumentCaptor = ArgumentCaptor.forClass(Hearing.class);

        verify(hearingRepository, times(1)).save(hearingExArgumentCaptor.capture());
    }

    @Test
    public void convictionDateUpdated_shouldUpdateTheConvictionDate() {

        final UUID caseId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);
        final ConvictionDateAdded convictionDateAdded = new ConvictionDateAdded(caseId, hearingId, offenceId, PAST_LOCAL_DATE.next());

        final Offence offence = new Offence();
        offence.setId(snapshotKey);

        when(this.offenceRepository.findBy(snapshotKey)).thenReturn(offence);

        initiateHearingEventListener.convictionDateUpdated(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-added"),
                objectToJsonObjectConverter.convert(convictionDateAdded)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(convictionDateAdded.getOffenceId()));
        assertThat(offence.getConvictionDate(), is(convictionDateAdded.getConvictionDate()));
    }

    @Test
    public void convictionDateRemoved_shouldSetConvictionDateToNull() {

        final UUID caseId = randomUUID();
        final UUID offenceId = randomUUID();
        final UUID hearingId = randomUUID();
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(offenceId, hearingId);

        final ConvictionDateRemoved convictionDateRemoved = new ConvictionDateRemoved(caseId, hearingId, offenceId);

        final Offence offence = new Offence();
        offence.setId(snapshotKey);

        when(offenceRepository.findBy(snapshotKey)).thenReturn(offence);

        initiateHearingEventListener.convictionDateRemoved(envelopeFrom(metadataWithRandomUUID("hearing.conviction-date-removed"),
                objectToJsonObjectConverter.convert(convictionDateRemoved)));

        verify(this.offenceRepository).saveAndFlush(offence);

        assertThat(offence.getId().getId(), is(offenceId));
        assertThat(offence.getId().getHearingId(), is(hearingId));
        assertThat(offence.getConvictionDate(), is(nullValue()));
    }

    @Test
    public void testHearingInitiatedPleaData() {

        final uk.gov.justice.core.courts.DelegatedPowers delegatedPowersPojo = uk.gov.justice.core.courts.DelegatedPowers.delegatedPowers()
                .withUserId(randomUUID())
                .withFirstName(STRING.next())
                .withLastName(STRING.next())
                .build();
        final uk.gov.justice.core.courts.Plea pleaPojo = uk.gov.justice.core.courts.Plea.plea()
                .withOffenceId(randomUUID())
                .withOriginatingHearingId(randomUUID())
                .withPleaDate(PAST_LOCAL_DATE.next())
                .withPleaValue(PleaValue.GUILTY)
                .withDelegatedPowers(delegatedPowersPojo)
                .build();

        final InheritedPlea event = new InheritedPlea()
                .setHearingId(randomUUID())
                .setPlea(pleaPojo);

        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(event.getPlea().getOffenceId(), event.getHearingId());

        final Offence offence = new Offence();
        offence.setId(snapshotKey);
        when(offenceRepository.findBy(snapshotKey)).thenReturn(offence);

        final DelegatedPowers delegatedPowers = new DelegatedPowers();
        delegatedPowers.setDelegatedPowersUserId(delegatedPowersPojo.getUserId());
        delegatedPowers.setDelegatedPowersLastName(delegatedPowersPojo.getLastName());
        delegatedPowers.setDelegatedPowersFirstName(delegatedPowersPojo.getFirstName());

        final Plea plea = new Plea();
        plea.setPleaValue(pleaPojo.getPleaValue());
        plea.setPleaDate(pleaPojo.getPleaDate());
        plea.setOriginatingHearingId(pleaPojo.getOriginatingHearingId());
        plea.setDelegatedPowers(delegatedPowers);

        when(pleaJPAMapper.toJPA(Mockito.any())).thenReturn(plea);

        initiateHearingEventListener.hearingInitiatedPleaData(envelopeFrom(metadataWithRandomUUID("hearing.initiate-hearing-offence-plead"),
                objectToJsonObjectConverter.convert(event)));

        verify(this.offenceRepository).save(offence);

        assertThat(offence, isBean(Offence.class)
                .with(Offence::getId, is(snapshotKey))
                .with(Offence::getPlea, isBean(Plea.class)
                        .with(Plea::getOriginatingHearingId, is(event.getPlea().getOriginatingHearingId()))
                        .with(Plea::getPleaDate, is(event.getPlea().getPleaDate()))
                        .with(Plea::getPleaValue, is(event.getPlea().getPleaValue()))
                        .with(Plea::getDelegatedPowers, isBean(DelegatedPowers.class)
                                .with(DelegatedPowers::getDelegatedPowersUserId, is(event.getPlea().getDelegatedPowers().getUserId()))
                                .with(DelegatedPowers::getDelegatedPowersFirstName, is(event.getPlea().getDelegatedPowers().getFirstName()))
                                .with(DelegatedPowers::getDelegatedPowersLastName, is(event.getPlea().getDelegatedPowers().getLastName()))
                        )
                )
        );
    }

    private JsonEnvelope getInitiateHearingJsonEnvelope(final uk.gov.justice.core.courts.Hearing hearing) {

        final InitiateHearingCommand document = new InitiateHearingCommand(hearing);

        final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

        String strJsonDocument;
        try {
            strJsonDocument = objectMapper.writer().writeValueAsString(document);
        } catch (final JsonProcessingException jpe) {
            throw new RuntimeException("failed ot serialise " + document, jpe);
        }
        final JsonObject jsonObject = Json.createReader(new StringReader(strJsonDocument)).readObject();

        return envelopeFrom((Metadata) null, jsonObject);
    }
}
