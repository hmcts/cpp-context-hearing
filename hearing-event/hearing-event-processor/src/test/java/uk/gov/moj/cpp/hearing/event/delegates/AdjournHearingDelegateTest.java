package uk.gov.moj.cpp.hearing.event.delegates;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloper;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.event.relist.RelistTestHelper.getArbitrarySharedResult;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.NextHearing;
import uk.gov.justice.core.courts.Target;
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
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.util.Map;
import java.util.UUID;

import javax.json.Json;

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

    private static final Boolean PROSECUTION_CASE_VALID = true;
    private static final Boolean PROSECUTION_CASE_INVALID = false;
    private static final Boolean APPLICATION_VALID = true;
    private static final Boolean APPLICATION_INVALID = false;

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

    @Captor
    private ArgumentCaptor<JsonEnvelope> transformerEnvelopeArgumentCaptor;

    @Captor
    private ArgumentCaptor<ResultsShared> transformerResultSharedCaptor;

    @Captor
    private ArgumentCaptor<Map<UUID, NextHearingResultDefinition>> transformerNextHearingResultDefinitionsCaptor;


    @Before
    public void setup() {
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
        setField(this.jsonObjectToObjectConvertor, "objectMapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void executeCaseOnlyAdjournment() {
        execute(PROSECUTION_CASE_VALID, APPLICATION_INVALID);
    }

    @Test
    public void executeApplicationOnly() {
        execute(PROSECUTION_CASE_INVALID, APPLICATION_VALID);
    }

    private void execute(final boolean validProsecutionCase, final boolean validApplication) {
        final JsonEnvelope event = envelopeFrom(metadataWithRandomUUID("hearing.adjourn-hearing"), Json.createObjectBuilder().build());
        final ResultsShared resultsShared = getArbitrarySharedResult();
        if (validApplication) {
            final Target targetForCase = resultsShared.getTargets().get(0);
            final Target target = Target
                    .target()
                    .withApplicationId(UUID.randomUUID())
                    .withDefendantId(targetForCase.getDefendantId())
                    .withDraftResult(targetForCase.getDraftResult())
                    .withHearingId(targetForCase.getHearingId())
                    .withOffenceId(targetForCase.getOffenceId())
                    .withResultLines(targetForCase.getResultLines())
                    .withTargetId(targetForCase.getTargetId())
                    .build();
            resultsShared.getTargets().add(0,target);

        }
        when(relistReferenceDataService.getNextHearingResultDefinitions(any(), eq(resultsShared.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate()))).thenReturn(emptyMap());
        when(relistReferenceDataService.getWithdrawnResultDefinitionUuids(any(), eq(resultsShared.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate()))).thenReturn(emptyList());
        when(hearingAdjournValidator.validateProsecutionCase(any(), any(), any())).thenReturn(validProsecutionCase);
        when(hearingAdjournValidator.validateApplication(any(), any())).thenReturn(validApplication);
        final HearingAdjourned hearingAdjourned = new HearingAdjourned(UUID.randomUUID(),
                singletonList(NextHearing.nextHearing().withCourtCentre(CourtCentre.courtCentre().withId(UUID.randomUUID()).build()).build()));
        when(hearingAdjournTransformer.transform2Adjournment(any(), any(), any())).thenReturn(hearingAdjourned);

        testObj.execute(resultsShared, event);

        final int expectedSendCount = 1;

        verify(hearingAdjournTransformer, times(expectedSendCount)).transform2Adjournment(transformerEnvelopeArgumentCaptor.capture(), transformerResultSharedCaptor.capture(), transformerNextHearingResultDefinitionsCaptor.capture());
        if (validProsecutionCase) {
            //check that a filtered resultsShared is used to create adjournment message
            final ResultsShared filtered = transformerResultSharedCaptor.getAllValues().get(0);
            final long applicationTargetCount = filtered.getTargets().stream().filter(t -> t.getApplicationId() != null).count();
            assertEquals(0, applicationTargetCount);
        }
        if (validApplication) {
            //check that a filtered resultsShared is used to create adjournment message
            final ResultsShared filtered = transformerResultSharedCaptor.getAllValues().get(expectedSendCount - 1);
            final long prosecutionCaseCount = filtered.getTargets().stream().filter(t -> t.getApplicationId() == null).count();
            assertEquals(0, prosecutionCaseCount);
        }
    }
}