package uk.gov.moj.cpp.hearing.listing.common.xhibit;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.moj.cpp.hearing.listing.common.xhibit.exception.InvalidReferenceDataException;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.junit.jupiter.api.Test;

public class XhibitReferenceDataValidatorTest {

    private static final String LAST_NAME = "lastName";

    private XhibitReferenceDataValidator xhibitReferenceDataValidator = new XhibitReferenceDataValidator();

    // Warning - this suppression of warnings is solely to get sonar to pass so the junit5
    // upgrade can be merged. The @SuppressWarnings needs to be removed and the test refactored
    @SuppressWarnings("java:S5778")
    @Test
    public void shouldThrowErrorWhenValidatePayload() {


        final JsonObject payload = Json.createObjectBuilder()
                .add("firstName", "Joe")
                .add(LAST_NAME, "")
                .build();

        final InvalidReferenceDataException invalidReferenceDataException = assertThrows(
                InvalidReferenceDataException.class,
                () -> xhibitReferenceDataValidator.validate(LAST_NAME, payload.getString(LAST_NAME), payload));

        assertThat(invalidReferenceDataException.getMessage(), is("Invalid value '' for 'lastName' in '{\"firstName\":\"Joe\",\"lastName\":\"\"}'"));
    }


    @Test
    public void shouldThrowErrorWhenValidate() {
        assertThrows(InvalidReferenceDataException.class, () -> xhibitReferenceDataValidator.validate(LAST_NAME, ""));
    }


    @Test
    public void shouldValidateWhenDataExist() {


        xhibitReferenceDataValidator.validate(LAST_NAME, "Smith");
    }


    @Test
    public void shouldThrowErrorWhenValidateJsonArrayIsEmpty() {
        assertThrows(InvalidReferenceDataException.class, () -> xhibitReferenceDataValidator.validateJsonArray(LAST_NAME, Json.createArrayBuilder().build()));
    }

    @Test
    public void shouldValidateJsonArray() {

        final JsonArray jsonArray = Json.createArrayBuilder()
                .add(Json.createObjectBuilder()
                        .add("firstName", "Joe")
                        .add(LAST_NAME, "Smith")

                        .build())
                .build();

        xhibitReferenceDataValidator.validateJsonArray(LAST_NAME, jsonArray);
    }
}
