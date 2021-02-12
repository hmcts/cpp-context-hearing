package uk.gov.moj.cpp.hearing.query.view.convertor;

import uk.gov.moj.cpp.hearing.common.ReusableInformation;
import uk.gov.moj.cpp.hearing.common.ReusableInformationConverterType;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class ReusableInformationFixlmConverter extends ReusableInformationBaseConverter<List<String>> {

    private static final String DELIMITER = "###";

    public ReusableInformationFixlmConverter() {
        type = ReusableInformationConverterType.FIXLM;
    }

    public JsonObject toJsonObject(final ReusableInformation<List<String>> reusableInformation) {
        if (Objects.isNull(reusableInformation)) {
            return null;
        }
        final JsonObjectBuilder jsonObjectBuilder = super.convert(reusableInformation);
        final String fixlmValue = reusableInformation.getValue().stream().collect(Collectors.joining(DELIMITER));
        return jsonObjectBuilder
                .add(VALUE_LABEL, fixlmValue)
                .build();
    }
}
