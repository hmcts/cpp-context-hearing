package uk.gov.moj.cpp.hearing.query.view.convertor;


import static java.lang.String.valueOf;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.common.converter.Converter;
import uk.gov.moj.cpp.hearing.persist.entity.HearingOutcome;
import uk.gov.moj.cpp.hearing.persist.entity.ProsecutionCounsel;

import java.util.List;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

public class HearingOutcomesConverter implements Converter<List<HearingOutcome>, JsonObject> {

    @Override
    public JsonObject convert(final List<HearingOutcome> hearingOutcomes) {
        final JsonArrayBuilder jsonArrayBuilder = createArrayBuilder();
        hearingOutcomes.forEach(hearingOutcome -> {
            jsonArrayBuilder.add(createObjectBuilder()
                    .add("targetId", valueOf(hearingOutcome.getId()))
                    .add("offenceId", valueOf(hearingOutcome.getOffenceId()))
                    .add("defendantId", valueOf(hearingOutcome.getDefendantId()))
                    .add("draftResult", hearingOutcome.getDraftResult())
                    .build());
        });

        return createObjectBuilder()
                .add("targets", jsonArrayBuilder.build())
                .build();
    }
}