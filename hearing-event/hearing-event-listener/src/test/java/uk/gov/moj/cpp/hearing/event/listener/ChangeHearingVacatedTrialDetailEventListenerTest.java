package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.HearingVacatedTrialDetailUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ChangeHearingVacatedTrialDetailEventListenerTest {

    private static final UUID HEARING_ID = randomUUID();
    private static final UUID VACATED_REASON_ID = randomUUID();

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private HearingVacatedTrialDetailChangeEventListener changeHearingVacatedTrialDetailEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Captor
    private ArgumentCaptor<Hearing> ahearingArgumentCaptor;

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldChangeMandatoryHearingDetails() {

        final Hearing hearing = new Hearing();
        hearing.setId(HEARING_ID);

        when(this.hearingRepository.findBy(HEARING_ID)).thenReturn(hearing);

        HearingVacatedTrialDetailUpdated hearingVacatedTrialDetailChanged = HearingVacatedTrialDetailUpdated.hearingVacatedTrialDetailChanged()
                .setHearingId(HEARING_ID)
                .setIsVacated(true)
                .setVacatedTrialReasonId(VACATED_REASON_ID);

        changeHearingVacatedTrialDetailEventListener.handleVacatedTrialDetailChangedHearing(envelopeFrom(metadataWithRandomUUID("hearing.event.vacated-trial-detail-updated"),
                objectToJsonObjectConverter.convert(hearingVacatedTrialDetailChanged)));

        verify(this.hearingRepository).save(ahearingArgumentCaptor.capture());

        final Hearing toBePersisted = ahearingArgumentCaptor.getValue();

        assertThat(hearingVacatedTrialDetailChanged.getHearingId(), equalTo(toBePersisted.getId()));
        assertThat(hearingVacatedTrialDetailChanged.getIsVacated(), equalTo(toBePersisted.getIsVacatedTrial()));
        assertThat(hearingVacatedTrialDetailChanged.getVacatedTrialReasonId(), equalTo(toBePersisted.getVacatedTrialReasonId()));

    }
}