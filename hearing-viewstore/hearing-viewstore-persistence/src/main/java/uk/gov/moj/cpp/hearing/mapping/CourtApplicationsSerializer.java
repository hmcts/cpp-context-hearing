package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;

import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

@SuppressWarnings({"squid:S2384"})
public class CourtApplicationsSerializer {
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    public String json(final List<CourtApplication> courtApplications) {
        final Holder holder = new Holder();
        holder.courtApplications = courtApplications;
        return objectToJsonObjectConverter.convert(holder).toString();
    }

    private static JsonObject jsonFromString(String jsonObjectStr) {
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr))) {
            return jsonReader.readObject();
        }
    }

    public List<CourtApplication> courtApplications(final String json) {
        if (json==null || json.trim().length()==0) {
            return Collections.emptyList();
        }
        final JsonObject jsonObject = jsonFromString(json);
        return jsonObjectToObjectConverter.convert(jsonObject, Holder.class).courtApplications;
    }

    private static class Holder {
        private List<CourtApplication> courtApplications;
        public List<CourtApplication> getCourtApplications() {
            return courtApplications;
        }
    }

}
