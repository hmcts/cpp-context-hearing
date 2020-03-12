package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createReader;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.referencedata.HearingTypeMapping;
import uk.gov.moj.cpp.external.domain.referencedata.HearingTypeMappingList;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ReferenceDataServiceTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Spy
    private UtcClock utcClock;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private Requester requester;


    @InjectMocks
    private ReferenceDataService referenceDataService;

    @Before
    public void setup() {

        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void shouldRequestCrackedInEffectiveTrialTypes() {
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(crackedInEffectiveTrialTypesResponseEnvelope());
        final CrackedIneffectiveVacatedTrialTypes trialTypes = referenceDataService.getCrackedIneffectiveVacatedTrialTypes();
        assertEquals(2, trialTypes.getCrackedIneffectiveVacatedTrialTypes().size());
    }

    @Test
    public void shouldRequestXhibitHearingTypes(){
        final UUID hearingTypeId1 = UUID.randomUUID();
        final UUID hearingTypeId2 = UUID.randomUUID();
        final String exhibitHearingTypeDescription1 = "Plea and Trial Preparation";
        final String exhibitHearingTypeDescription2= "Committal for Sentence";
        final HearingTypeMapping hearingTypeMapping1 = new HearingTypeMapping(hearingTypeId1, 0, EMPTY, EMPTY, EMPTY, 0, EMPTY, EMPTY, EMPTY, exhibitHearingTypeDescription1);
        final HearingTypeMapping hearingTypeMapping2 = new HearingTypeMapping(hearingTypeId2, 0, EMPTY, EMPTY, EMPTY, 0, EMPTY, EMPTY, EMPTY, exhibitHearingTypeDescription2);

        final HearingTypeMappingList hearingTypeMappingList = new HearingTypeMappingList(asList(hearingTypeMapping1, hearingTypeMapping2));

        final JsonEnvelope value = hearingTypesResponseEnvelope();
        when(requester.requestAsAdmin(any(JsonEnvelope.class))).thenReturn(value);

        when(jsonObjectToObjectConverter.convert(value.payloadAsJsonObject(), HearingTypeMappingList.class)).thenReturn(hearingTypeMappingList);

        final HearingTypeMappingList hearingTypeMappings = referenceDataService.getXhibitHearingType();
        assertEquals(2, hearingTypeMappings.getHearingTypes().size());
    }

    private JsonEnvelope crackedInEffectiveTrialTypesResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.cracked-ineffective-vacated-trial-types").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("cracked-ineffective-trial-types-ref-data.json")).
                        readObject()
        );
    }

    private JsonEnvelope hearingTypesResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.hearing-types").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("hearing-types.json")).
                        readObject()
        );
    }
}
