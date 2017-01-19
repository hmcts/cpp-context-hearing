package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.lang.String.valueOf;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.List;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class ProsecutionCounselListConverter implements Converter<List<ProsecutionCounsel>, JsonObject> {

    @Override
    public JsonObject convert(List<ProsecutionCounsel> prosecutionCounsels) {
        final JsonArrayBuilder jsonArrayBuilder = createArrayBuilder();
        prosecutionCounsels.forEach(prosecutionCounsel -> {
            jsonArrayBuilder.add(createObjectBuilder()
                    .add("id", valueOf(prosecutionCounsel.getId()))
                    .add("personId", valueOf(prosecutionCounsel.getPersonId()))
                    .add("status", prosecutionCounsel.getStatus())
                    .build());
        });

        return createObjectBuilder()
                .add("prosecution-counsels", jsonArrayBuilder.build())
                .build();
    }
}
