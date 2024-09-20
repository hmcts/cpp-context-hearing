package uk.gov.moj.cpp.hearing.event;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.JsonEnvelopeBuilder.envelope;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.domain.event.result.HearingVacatedRequested;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.service.CrackedIneffectiveVacatedTrialTypesReverseLookup;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.util.Arrays;
import java.util.UUID;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings({"unchecked", "unused"})
@ExtendWith(MockitoExtension.class)
public class HearingVacatedEventProcessorTest {

    public static final String PUBLIC_HEARING_DRAFT_RESULT_SAVED = "hearing.command.set-trial-type";
    private static final String DRAFT_RESULT_SAVED_PRIVATE_EVENT = "hearing.hearing-vacated-requested";
    private static final UUID HEARING_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();

    @Spy
    private final Enveloper enveloper = createEnveloper();
    @Spy
    private final ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();
    @Spy
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
    @Spy
    private final ObjectToJsonValueConverter objectToJsonValueConverter = new ObjectToJsonValueConverter(this.objectMapper);
    @InjectMocks
    private HearingVacatedEventProcessor hearingVacatedEventProcessor;
    @Mock
    CrackedIneffectiveVacatedTrialTypesReverseLookup crackedIneffectiveVacatedTrialTypesReverseLookup;
    @Mock
    private Sender sender;
    @Mock
    private Requester requester;
    @Mock
    private JsonEnvelope responseEnvelope;
    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;
    @Spy
    private final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();

    @BeforeEach
    public void initMocks() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());

    }

    @Test
    public void shouldPublishDraftResultSavedPublicEvent() {

        final String SHORT_DESCRIPTION = "short desc";
        final UUID ID = UUID.randomUUID();
        final UUID REASON_ID = UUID.randomUUID();
        CrackedIneffectiveVacatedTrialType crackedIneffectiveVacatedTrialType =  new CrackedIneffectiveVacatedTrialType(REASON_ID,null,null,SHORT_DESCRIPTION,null,null);
        CrackedIneffectiveVacatedTrialTypes crackedIneffectiveVacatedTrialTypes = new CrackedIneffectiveVacatedTrialTypes();
        crackedIneffectiveVacatedTrialTypes.setCrackedIneffectiveVacatedTrialTypes(Arrays.asList(crackedIneffectiveVacatedTrialType));
        when(crackedIneffectiveVacatedTrialTypesReverseLookup.getCrackedIneffectiveVacatedTrialType(any())).thenReturn(crackedIneffectiveVacatedTrialTypes);

        final String draftResult = "some random text";
        final HearingVacatedRequested hearingVacatedRequested = CoreTestTemplates.hearingVacatedRequested(ID,SHORT_DESCRIPTION).build();


        final JsonObjectBuilder result = createObjectBuilder()
                .add("hearingIdToBeVacated", ID.toString())
                .add("vacatedTrialReasonShortDesc", SHORT_DESCRIPTION.toString());
        final JsonEnvelope eventIn =  envelopeFrom(metadataWithRandomUUID(DRAFT_RESULT_SAVED_PRIVATE_EVENT), result.build());


        final InOrder inOrder = inOrder(sender);
        this.hearingVacatedEventProcessor.hearingVacatedRequested(eventIn);

        inOrder.verify(this.sender, times(1)).send(this.envelopeArgumentCaptor.capture());

        final JsonEnvelope envelopeOut = this.envelopeArgumentCaptor.getValue();
        assertThat(envelopeOut.metadata().name(), is(PUBLIC_HEARING_DRAFT_RESULT_SAVED));

        final TrialType trialType = jsonObjectToObjectConverter
                .convert(envelopeOut.payloadAsJsonObject(), TrialType.class);

        assertThat(trialType.getHearingId(), is(ID));
        assertThat(trialType.getVacatedTrialReasonId(), is(REASON_ID));


    }

    private JsonEnvelope createHearingVacatedRequested(final HearingVacatedRequested hearingVacatedRequested) {
        final JsonObject jsonObject = this.objectToJsonObjectConverter.convert(hearingVacatedRequested);
        return envelope().withPayloadOf(jsonObject, "target").with(metadataWithRandomUUID(DRAFT_RESULT_SAVED_PRIVATE_EVENT).withUserId(USER_ID.toString())).build();
    }
}