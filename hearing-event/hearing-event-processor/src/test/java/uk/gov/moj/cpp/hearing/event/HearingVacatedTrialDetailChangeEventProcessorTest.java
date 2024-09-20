package uk.gov.moj.cpp.hearing.event;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.matchers.JsonEnvelopePayloadMatcher.payloadIsJson;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HearingVacatedTrialDetailChangeEventProcessorTest {
    private static final String PUBLIC_EVENT_HEARING_VACATE_TRIAL_DETAIL_CHANGED = "public.listing.vacated-trial-updated";
    private static final String PRIVATE_HEARING_COMMAND_HEARING_VACATED_TRIAL_DETAIL_CHANGE = "hearing.update-vacated-trial-detail";
    private static final String HEARING_ID = UUID.randomUUID().toString();
    private static final String VACATED_TRIAL_REASON_ID = UUID.randomUUID().toString();

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter = new JsonObjectConvertersFactory().objectToJsonObjectConverter();

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> senderJsonEnvelopeCaptor;

    @InjectMocks
    private HearingVacatedTrialDetailChangeEventProcessor hearingVacatedTrialDetailChangeEventProcessor;

    @Test
    public void shouldPublishHearingVacatedTrialDetailUpdatedPublicEvent() {
        //Given
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID(PUBLIC_EVENT_HEARING_VACATE_TRIAL_DETAIL_CHANGED),
                publicHearingVacatedTrialDetailUpdatedEvent());

        //when
        hearingVacatedTrialDetailChangeEventProcessor.handleListingVacatedTrialUpdate(event);

        //then
        verify(sender).send(senderJsonEnvelopeCaptor.capture());

        assertThat(senderJsonEnvelopeCaptor.getValue().metadata().name(), is(PRIVATE_HEARING_COMMAND_HEARING_VACATED_TRIAL_DETAIL_CHANGE));
        assertThat(senderJsonEnvelopeCaptor.getValue().payload(), is(payloadIsJson(allOf(
                withJsonPath("$.hearingId", equalTo(HEARING_ID)),
                withJsonPath("$.vacatedTrialReasonId", equalTo(VACATED_TRIAL_REASON_ID)),
                withJsonPath("$.isVacated", equalTo(true)),
                withJsonPath("$.allocated", equalTo(true))
                )))
        );

    }

    private JsonObject publicHearingVacatedTrialDetailUpdatedEvent() {
        return Json.createObjectBuilder()
                .add("hearingId", HEARING_ID)
                .add("vacatedTrialReasonId", VACATED_TRIAL_REASON_ID)
                .add("isVacated", true)
                .add("allocated", true)
                .build();

    }


}