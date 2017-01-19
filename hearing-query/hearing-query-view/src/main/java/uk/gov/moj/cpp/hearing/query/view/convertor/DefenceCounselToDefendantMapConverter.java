package uk.gov.moj.cpp.hearing.query.view.convertor;

import static java.lang.String.valueOf;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.DefenceCounselDefendant;

import java.util.List;
import java.util.Map;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class DefenceCounselToDefendantMapConverter implements Converter<Map<DefenceCounsel, List<DefenceCounselDefendant>>, JsonObject> {

    @Override
    public JsonObject convert(Map<DefenceCounsel, List<DefenceCounselDefendant>> defenceCounselsAndDefendantsMap) {
        final JsonArrayBuilder jsonArrayBuilder = createArrayBuilder();
        defenceCounselsAndDefendantsMap.forEach((a,b) -> {
            jsonArrayBuilder.add(createObjectBuilder()
                    .add("id", valueOf(a.getId()))
                    .add("personId", valueOf(a.getPersonId()))
                    .add("status", a.getStatus())
                    .add("defendantIds", convert(b))
                    .build());
        });

        return createObjectBuilder()
                .add("defence-counsels", jsonArrayBuilder.build())
                .build();
    }

    private JsonArrayBuilder convert(List<DefenceCounselDefendant> defenceCounselDefendants){
        final JsonArrayBuilder jsonArrayBuilder = createArrayBuilder();
        defenceCounselDefendants.forEach(defenceCounselDefendant -> {
            jsonArrayBuilder.add(
                    createObjectBuilder().add(
                        "defendantId",
                        valueOf(defenceCounselDefendant.getDefendantId())));

        });
        return jsonArrayBuilder;
    }
}
