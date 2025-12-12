package uk.gov.moj.cpp.hearing.query.api.service.referencedata;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.external.domain.referencedata.PIEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.PIEventMappingsList;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMapping;
import uk.gov.moj.cpp.external.domain.referencedata.XhibitEventMappingsList;
import uk.gov.moj.cpp.hearing.domain.referencedata.HearingTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.json.JsonObject;

import com.google.common.cache.LoadingCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }


    @Test
    public void shouldRequestCrackedInEffectiveTrialTypes() {
        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class))).thenReturn(crackedInEffectiveTrialTypesResponseEnvelope());
        final CrackedIneffectiveVacatedTrialTypes trialTypes = referenceDataService.listAllCrackedIneffectiveVacatedTrialTypes();
        assertEquals(2, trialTypes.getCrackedIneffectiveVacatedTrialTypes().size());
    }

    @Test
    public void shouldRequestAllHearingTypes() {
        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class))).thenReturn(hearingTypesResponseEnvelope());
        final HearingTypes hearingTypes = referenceDataService.getAllHearingTypes();
        assertEquals(2, hearingTypes.getHearingTypes().size());
    }

    @Test
    public void shouldGetCacheableResultDefinitions() {
        final String promptRef = "promptRef";
        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class))).thenReturn(cacheableResultDefinitionsEnvelope(promptRef));
        final List<Prompt> resultPrompts = referenceDataService.getCacheableResultPrompts(Optional.empty());
        assertThat(resultPrompts.size(), is(1));
        assertThat(resultPrompts.get(0).getReference(), is(promptRef));
    }


    @Test
    public void shouldNotGetCacheableResultDefinitionsAndThrowException() throws ExecutionException {
        final LoadingCache<LocalDate, List<Prompt>> promptsCacheLoader = mock(LoadingCache.class);
        setField(referenceDataService, "promptsCacheLoader", promptsCacheLoader);
        when(promptsCacheLoader.get(any())).thenThrow(ExecutionException.class);
        final List<Prompt> resultPrompts = referenceDataService.getCacheableResultPrompts(Optional.empty());
        assertThat(resultPrompts.size(), is(0));
    }

    @Test
    public void shouldGetCountryCodesMap(){
        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class))).thenReturn(countryCodeResponseEnvelope());
        final Map<String, String> countryCodesMap = referenceDataService.getCountryCodesMap();
        assertThat(countryCodesMap.get("isoCode"), is("countryName"));
    }

    @Test
    public void shouldListAllEventMappings() {
        final String firstXhibitEventCode = "exhibitEventCode_1";
        final String secondXhibitEventCode = "exhibitEventCode_2";
        final UUID cpHearingEventId_1 = randomUUID();
        final UUID cpHearingEventId_2 = randomUUID();
        final XhibitEventMapping xhibitEventMapping1 = new XhibitEventMapping(cpHearingEventId_1, firstXhibitEventCode, EMPTY, EMPTY, EMPTY);
        final XhibitEventMapping xhibitEventMapping2 = new XhibitEventMapping(cpHearingEventId_2, secondXhibitEventCode, EMPTY, EMPTY, EMPTY);

        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class)).payload()).thenReturn(new XhibitEventMappingsList(asList(xhibitEventMapping1,xhibitEventMapping2)));
        XhibitEventMappingsList list = referenceDataService.listAllEventMappings();
        assertEquals(2,list.getCpXhibitHearingEventMappings().size());
        assertEquals(cpHearingEventId_1,list.getCpXhibitHearingEventMappings().get(0).getCpHearingEventId());
        assertEquals(cpHearingEventId_2,list.getCpXhibitHearingEventMappings().get(1).getCpHearingEventId());
    }

    @Test
    public void shouldListAllPIEventMappings() {
        final String eventCode_1 = "eventCode_1";
        final String eventCode_2 = "eventCode_2";
        final UUID eventId_1 = randomUUID();
        final UUID eventId_2 = randomUUID();
        final String eventDescription_1 = "eventDescription_1";
        final String eventDescription_2 = "eventDescription_2";
        final PIEventMapping eventMapping1 = new PIEventMapping(eventId_1, eventCode_1, eventDescription_1);
        final PIEventMapping eventMapping2 = new PIEventMapping(eventId_2, eventCode_2, eventDescription_2);

        when(requester.requestAsAdmin(any(JsonEnvelope.class), any(Class.class)).payload()).thenReturn(new PIEventMappingsList(asList(eventMapping1,eventMapping2)));
        PIEventMappingsList list = referenceDataService.listAllPIEventMappings();
        assertEquals(2,list.getCpPIHearingEventMappings().size());
        assertEquals(eventId_1,list.getCpPIHearingEventMappings().get(0).getCpHearingEventId());
        assertEquals(eventId_2,list.getCpPIHearingEventMappings().get(1).getCpHearingEventId());
    }

    @Test
    public void shouldGetAllCourtRooms() {
        when(requester.requestAsAdmin(any(), any(Class.class))).thenReturn(courtRoomsResponseEnvelope());
        JsonObject obj = referenceDataService.getAllCourtRooms(courtRoomsRequestEnvelope());
        assertEquals(1, obj.getJsonArray("organisationunits").size());
        assertEquals("oucodeL3Name", obj.getJsonArray("organisationunits").getJsonObject(0).getString("oucodeL3Name"));
    }

    private Envelope cacheableResultDefinitionsEnvelope(final String promptRef) {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.get-all-result-definitions").
                        withId(randomUUID()),
                createObjectBuilder().add("resultDefinitions",
                        createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", randomUUID().toString())
                                        .add("prompts", createArrayBuilder()
                                                .add(createObjectBuilder()
                                                        .add("id", randomUUID().toString())
                                                        .add("reference", promptRef)
                                                        .add("cacheable", 1)).build()))
                                .add(createObjectBuilder()
                                        .add("id", randomUUID().toString())
                                        .add("prompts", createArrayBuilder()
                                                .add(createObjectBuilder()
                                                        .add("id", randomUUID().toString())
                                                        .add("reference", promptRef)
                                                        .add("cacheable", 1)).build()))
                                .build())
        );
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

    private JsonEnvelope judiciaryResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.judiciaries").
                        withId(randomUUID()),
                createReader(getClass().getClassLoader().
                        getResourceAsStream("referencedata.query.judiciaries.json")).
                        readObject()
        );
    }

    private JsonEnvelope emptyJudiciaryResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.judiciaries").
                        withId(randomUUID()),
                createObjectBuilder().build()
        );
    }

    private JsonEnvelope courtRoomsRequestEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.courtrooms").
                        withId(randomUUID()),
                createObjectBuilder().build()
        );
    }

    private JsonEnvelope courtRoomsResponseEnvelope() {
        return envelopeFrom(
                metadataBuilder().
                        withName("referencedata.query.courtrooms").
                        withId(randomUUID()),
                createObjectBuilder().add("organisationunits",
                        createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("id", randomUUID().toString())
                                        .add("oucodeL3Name", "oucodeL3Name"))
                                .build())
        );
    }

    private JsonEnvelope countryCodeResponseEnvelope(){
        return envelopeFrom(metadataBuilder()
                .withName("referencedata.query.country-nationality")
                .withId(randomUUID()),
                createObjectBuilder().add("countryNationality",
                        createArrayBuilder()
                                .add(createObjectBuilder()
                                        .add("isoCode","isoCode")
                                        .add("countryName","countryName")
                                        .build()).build()).build());
    }
}
