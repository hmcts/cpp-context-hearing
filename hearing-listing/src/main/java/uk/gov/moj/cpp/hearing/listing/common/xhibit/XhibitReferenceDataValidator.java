package uk.gov.moj.cpp.hearing.listing.common.xhibit;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

import uk.gov.moj.cpp.hearing.listing.common.xhibit.exception.InvalidReferenceDataException;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.commons.collections.CollectionUtils;

public class XhibitReferenceDataValidator {

    public void validate(final String propertyName, final String propertyValue, final JsonObject parentObject) {
        if (isBlank(propertyValue)) {
            throw new InvalidReferenceDataException(format("Invalid value '%s' for '%s' in '%s'", propertyValue, propertyName, parentObject));
        }
    }

    public void validate(final String propertyName, final String propertyValue) {
        if (isBlank(propertyValue)) {
            throw new InvalidReferenceDataException(format("Invalid value '%s' for '%s'", propertyValue, propertyName));
        }
    }

    public void validateJsonArray(final String propertyName, final JsonArray jsonArray) {
        if (CollectionUtils.isEmpty(jsonArray)) {
            throw new InvalidReferenceDataException(format("Invalid object '%s' for '%s'", propertyName, jsonArray));
        }
    }
}
