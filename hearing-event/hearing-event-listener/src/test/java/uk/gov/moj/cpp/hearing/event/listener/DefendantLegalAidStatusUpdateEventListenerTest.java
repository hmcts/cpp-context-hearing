package uk.gov.moj.cpp.hearing.event.listener;


import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import java.util.UUID;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class DefendantLegalAidStatusUpdateEventListenerTest {

    @Mock
    private DefendantRepository defendantRepository;

    @InjectMocks
    private DefendantLegalAidStatusUpdateEventListener defendantLegalAidStatusUpdateEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void updateDefendantLegalAidStatusGranted() {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final String legalAidStatus = "Granted";
        final DefendantLegalAidStatusUpdatedForHearing defendantLegalAidStatusUpdatedForHearing = DefendantLegalAidStatusUpdatedForHearing.defendantLegalaidStatusUpdatedForHearing()
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withLegalAidStatus(legalAidStatus)
                .build();
        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        defendantLegalAidStatusUpdateEventListener.updateDefendantLegalAidStatusForHearing(envelopeFrom(metadataWithRandomUUID("hearing.defendant-legalaid-status-updated-for-hearing"),
                objectToJsonObjectConverter.convert(defendantLegalAidStatusUpdatedForHearing)));


        final ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantexArgumentCaptor.capture());

        final Defendant defendantOut = defendantexArgumentCaptor.getValue();

        assertThat(defendant.getId(), is(defendantOut.getId()));

        assertThat(defendant.getLegalaidStatus(), is("Granted"));
    }



    @Test
    public void updateDefendantLegalAidStatusNoValue() {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final String legalAidStatus = "NO_VALUE";
        final DefendantLegalAidStatusUpdatedForHearing defendantLegalAidStatusUpdatedForHearing = DefendantLegalAidStatusUpdatedForHearing.defendantLegalaidStatusUpdatedForHearing()
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withLegalAidStatus(legalAidStatus)
                .build();
        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        when(defendantRepository.findBy(defendant.getId())).thenReturn(defendant);

        defendantLegalAidStatusUpdateEventListener.updateDefendantLegalAidStatusForHearing(envelopeFrom(metadataWithRandomUUID("hearing.defendant-legalaid-status-updated-for-hearing"),
                objectToJsonObjectConverter.convert(defendantLegalAidStatusUpdatedForHearing)));


        final ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);

        verify(defendantRepository).save(defendantexArgumentCaptor.capture());

        final Defendant defendantOut = defendantexArgumentCaptor.getValue();

        assertThat(defendant.getId(), is(defendantOut.getId()));

        assertThat(defendant.getLegalaidStatus(), is(CoreMatchers.nullValue()));
    }



    @Test
    public void testUpdateDefendantLegalAidStatusWhenDefendantIsNotFoundForCombinationOfHearingIdAndDefendantId() {
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final String legalAidStatus = "Granted";
        final DefendantLegalAidStatusUpdatedForHearing defendantLegalAidStatusUpdatedForHearing = DefendantLegalAidStatusUpdatedForHearing.defendantLegalaidStatusUpdatedForHearing()
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withLegalAidStatus(legalAidStatus)
                .build();
        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        when(defendantRepository.findBy(defendant.getId())).thenReturn(null);

        defendantLegalAidStatusUpdateEventListener.updateDefendantLegalAidStatusForHearing(envelopeFrom(metadataWithRandomUUID("hearing.defendant-legalaid-status-updated-for-hearing"),
                objectToJsonObjectConverter.convert(defendantLegalAidStatusUpdatedForHearing)));


        verify(this.defendantRepository, never()).save(Mockito.any());

        assertThat(defendant.getLegalaidStatus(), is(CoreMatchers.nullValue()));
    }

    @Test
    public void shouldNotUpdateDefendantWhenThereIsNoDefendantAssociatedWithHearing(){
        final UUID hearingId = randomUUID();
        final UUID defendantId = randomUUID();
        final String legalAidStatus = "NO_VALUE";
        final DefendantLegalAidStatusUpdatedForHearing defendantLegalAidStatusUpdatedForHearing = DefendantLegalAidStatusUpdatedForHearing.defendantLegalaidStatusUpdatedForHearing()
                .withDefendantId(defendantId)
                .withHearingId(hearingId)
                .withLegalAidStatus(legalAidStatus)
                .build();
        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        final ArgumentCaptor<Defendant> defendantexArgumentCaptor = ArgumentCaptor.forClass(Defendant.class);
        verify(defendantRepository, never()).save(defendantexArgumentCaptor.capture());
    }

}
