package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResult;

import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournTransformer;
import uk.gov.moj.cpp.hearing.event.relist.HearingAdjournValidator;
import uk.gov.moj.cpp.hearing.event.relist.RelistReferenceDataService;

import javax.json.Json;

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

    @InjectMocks
    AdjournHearingDelegate testObj;

    @Mock
    private Sender sender;

    @Captor
    private ArgumentCaptor<JsonEnvelope> envelopeArgumentCaptor;

    @Test
    public void execute() throws Exception {
        //Given
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.adjourn-hearing"), Json.createObjectBuilder().build());
        when(relistReferenceDataService.getNextHearingResultDefinitions(any(), eq(getArbitrarySharedResult().getHearing().getHearingDays().get(0).toLocalDate()))).thenReturn(emptyMap());
        when(relistReferenceDataService.getWithdrawnResultDefinitionUuids(any(), eq(getArbitrarySharedResult().getHearing().getHearingDays().get(0).toLocalDate()))).thenReturn(emptyList());
        when(hearingAdjournValidator.validate(any(), any(), any())).thenReturn(true);

        //when
        testObj.execute(getArbitrarySharedResult(), event);

        //then
        verify(this.sender).send(this.envelopeArgumentCaptor.capture());
    }

}