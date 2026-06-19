package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType;

import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class ReusableInformationObjectTypeConverter extends ReusableInformationBaseConverter<Object> {

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    public JsonObject toJsonObject(final ReusableInformation<Object> reusableInformation,
                                   final ReusableInformationConverterType type) {
        if (isNull(reusableInformation)) {
            return null;
        }
        this.type = type;

        final JsonObjectBuilder jsonObjectBuilder = super.convert(reusableInformation);

        final Object objectReusableInformation = reusableInformation.getValue();

        if (nonNull(objectReusableInformation)) {
            jsonObjectBuilder
                    .add(VALUE_LABEL, objectToJsonObjectConverter.convert(objectReusableInformation));
        }

        return jsonObjectBuilder.build();

    }
}
