package uk.gov.moj.cpp.hearing.query.view.convertor;

import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType;

import java.util.Objects;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ReusableInformationIntConverter extends ReusableInformationBaseConverter<String> {

    public ReusableInformationIntConverter() {
        type = ReusableInformationConverterType.INT;
    }

    public JsonObject toJsonObject(final ReusableInformation<String> reusableInformation) {
        if (Objects.isNull(reusableInformation)) {
            return null;
        }
        final JsonObjectBuilder jsonObjectBuilder = super.convert(reusableInformation);

        return jsonObjectBuilder
                .add(VALUE_LABEL, reusableInformation.getValue())
                .build();
    }
}
