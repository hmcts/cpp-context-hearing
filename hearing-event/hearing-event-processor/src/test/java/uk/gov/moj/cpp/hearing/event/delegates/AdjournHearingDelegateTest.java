package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.common.reflection.ReflectionUtils.setField;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResult;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingAdjourned;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsShared;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournTransformer;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournValidator;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;

import java.util.Arrays;
import java.util.UUID;

import javax.json.Json;

import org.junit.Assert;
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
public class AdjournHearingDelegateTest {
    @Spy
    private final Enveloper enveloper = createEnveloper();

    @Mock
    HearingAdjournValidator hearingAdjournValidator;

    @Mock
    HearingAdjournTransformer hearingAdjournTransformer;

    @Mock
    RelistReferenceDataService relistReferenceDataService;

    @Spy
    ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    JsonObjectToObjectConverter jsonObjectToObjectConvertor;

    @InjectMocks
    AdjournHearingDelegate testObj;

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Before
    public void setup() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.jsonObjectToObjectConvertor, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void execute()  {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.adjourn-hearing"), Json.createObjectBuilder().build());
        final ResultsShared resultsShared = getArbitrarySharedResult();
        when(relistReferenceDataService.getNextHearingResultDefinitions(any(), eq(resultsShared.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate()))).thenReturn(emptyMap());
        when(relistReferenceDataService.getWithdrawnResultDefinitionUuids(any(), eq(resultsShared.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate()))).thenReturn(emptyList());
        when(hearingAdjournValidator.validate(any(), any(), any())).thenReturn(true);
        final HearingAdjourned hearingAdjourned = new HearingAdjourned(UUID.randomUUID(),
                Arrays.asList(NextHearing.nextHearing().withCourtCentre(CourtCentre.courtCentre().withId(UUID.randomUUID()).build()).build()));
        when(hearingAdjournTransformer.transform2Adjournment(any(), any(), any())).thenReturn(hearingAdjourned);

        testObj.execute(resultsShared, event);

        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
        final HearingAdjourned hearingAdjournedOut =  jsonObjectToObjectConvertor.convert(this.envelopeArgumentCaptor.getValue().payloadAsJsonObject(), HearingAdjourned.class);
        Assert.assertEquals(hearingAdjourned.getAdjournedHearing(), hearingAdjournedOut.getAdjournedHearing());
        Assert.assertEquals(hearingAdjourned.getNextHearings().get(0).getCourtCentre().getId(), hearingAdjournedOut.getNextHearings().get(0).getCourtCentre().getId());

    }

}