package uk.gov.moj.cpp.hearing.mapping;

import static javax.json.Json.createObjectBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CaseMarker;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("squid:S1186")
@ApplicationScoped
public class CaseMarkerJPAMapper {

    private static final String MARKER_TYPE_ID = "markerTypeid";
    private static final String MARKER_TYPE_CODE = "markerTypeCode";
    private static final String MARKER_TYPE_DESCRIPTION = "markerTypeDescription";
    private static final String ID = "id";
    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    public CaseMarkerJPAMapper() {}

    public Set<CaseMarker> toJPA(Hearing hearing, final ProsecutionCase prosecutionCase, final List<uk.gov.justice.core.courts.Marker> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, prosecutionCase, pojo)).collect(Collectors.toSet());
    }

    public CaseMarker toJPA(final Hearing hearing, final ProsecutionCase prosecutionCase, final uk.gov.justice.core.courts.Marker pojo) {
        if (null == pojo) {
            return null;
        }
        final CaseMarker caseMarker = new CaseMarker();
        caseMarker.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        caseMarker.setProsecutionCase(prosecutionCase);
        caseMarker.setProsecutionCaseId(prosecutionCase.getId().getId());

        final JsonObject payload = createObjectBuilder()
                .add(ID, pojo.getId().toString())
                .add(MARKER_TYPE_ID, pojo.getMarkerTypeid().toString())
                .add(MARKER_TYPE_CODE, pojo.getMarkerTypeCode())
                .add(MARKER_TYPE_DESCRIPTION, pojo.getMarkerTypeDescription())
                .build();
        final JsonNode jsonNode = mapper.valueToTree(payload);

        caseMarker.setPayload(jsonNode);
        return caseMarker;
    }

    public List<Marker> fromJPA(final Set<CaseMarker> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(entity -> fromJPA(entity)).collect(Collectors.toList());
    }

    public Marker fromJPA(final CaseMarker entity) {
        if (null == entity) {
            return null;
        }
        final JsonObject entityPayload = jsonFromString(entity.getPayload().toString());
        return Marker.marker()
                .withId(entity.getId().getId())
                .withMarkerTypeid(UUID.fromString(entityPayload.getString(MARKER_TYPE_ID)))
                .withMarkerTypeCode(entityPayload.getString(MARKER_TYPE_CODE))
                .withMarkerTypeDescription(entityPayload.getString("markerTypeDescription"))
                .build();
    }

    private JsonObject jsonFromString(String jsonObjectStr) {
        JsonObject object;
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr))) {
            object = jsonReader.readObject();
        }
        return object;
    }
}
