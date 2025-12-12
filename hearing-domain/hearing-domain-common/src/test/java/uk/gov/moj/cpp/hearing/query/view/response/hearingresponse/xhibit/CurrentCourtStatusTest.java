package uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.xhibit;


import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import java.io.StringReader;

import javax.json.Json;

import org.junit.jupiter.api.Test;


public class CurrentCourtStatusTest {

    private static final String TEST_JSON = "{\"court\":{\"courtName\":\"gK2BZfbntC\",\"courtSites\":[{\"id\":\"7b223ad0-4468-11ea-b77f-2e728ce88125\",\"courtRooms\":[{\"courtRoomId\":\"5d5d7b5e-7833-4dc1-b94a-d1a5c635c623\",\"courtRoomName\":\"CourtRoom 2\",\"cases\":{\"casesDetails\":[]},\"hearingEvent\":{\"alterable\":false,\"defenceCounselId\":\"9dc824b3-8a61-472f-8b55-e532ceabb403\",\"deleted\":false,\"eventDate\":\"2022-02-22\",\"eventTime\":\"2022-02-22T10:59:33.896Z\",\"hearingEventDefinitionId\":\"e9060336-4821-4f46-969c-e08b33b48071\",\"hearingId\":\"b4d48568-b720-4542-a885-0d2d0595f162\",\"id\":\"da86ef47-f4f5-4e07-bad2-bb07b43d20b7\",\"lastModifiedTime\":\"2018-01-25T08:55:17.163Z\",\"recordedLabel\":\"yWef8t8JIL\"},\"defenceCounsel\":{\"attendanceDays\":[\"2022-02-22\"],\"defendants\":[\"82832c5b-3d60-42d1-a7ee-b077ea4d7d11\"],\"firstName\":\"John\",\"id\":\"9dc824b3-8a61-472f-8b55-e532ceabb403\",\"lastName\":\"Jones\",\"status\":\"OPEN\",\"title\":\"Mr\"},\"linkedCaseIds\":[]}],\"courtSiteName\":\"DUMMYCOURTNAME\"}]}}";
    private JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter(new ObjectMapperProducer().objectMapper());

    @Test
    public void testConversion() {
        final CurrentCourtStatus currentCourtStatus = jsonObjectToObjectConverter.convert(Json.createReader(new StringReader(TEST_JSON)).readObject(), CurrentCourtStatus.class);
        assertNotNull(currentCourtStatus);
    }

}