package uk.gov.moj.cpp.external.domain.referencedata;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;

import javax.json.JsonArray;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

public class XhibitEventMappingsListTest {

    private ObjectMapper objectMapper = new ObjectMapperProducer().objectMapper();

    @Test
    public void convertToObject() {

        final JsonArray eventMappings = createArrayBuilder().add(
                        createObjectBuilder()
                                .add("cpHearingEventId", randomUUID().toString())
                                .add("xhibitHearingEventCode", randomAlphabetic(10))
                                .add("xhibitHearingEventDescription", randomAlphabetic(10))
                                .add("validFrom", randomAlphabetic(10))
                                .add("validTo", randomAlphabetic(10))
                                .build())
                .build();

        final XhibitEventMappingsList xhibitEventMappingsList = this.objectMapper.convertValue(createObjectBuilder().add("cpXhibitHearingEventMappings", eventMappings).build(), XhibitEventMappingsList.class);
        assertNotNull(xhibitEventMappingsList);
        assertThat(xhibitEventMappingsList.getCpXhibitHearingEventMappings(), Matchers.hasSize(1));

    }
}