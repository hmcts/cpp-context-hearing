package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.util.Objects.isNull;

import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ReusableInformationINTCConverter extends ReusableInformationBaseConverter<String> {

    public ReusableInformationINTCConverter() {
        type = ReusableInformationConverterType.INTC;
    }

    public JsonObject toJsonObject(final ReusableInformation<String> reusableInformation) {
        if (isNull(reusableInformation)) {
            return null;
        }
        final JsonObjectBuilder jsonObjectBuilder = super.convert(reusableInformation);

        return jsonObjectBuilder
                .add(VALUE_LABEL, reusableInformation.getValue())
                .build();
    }


}
