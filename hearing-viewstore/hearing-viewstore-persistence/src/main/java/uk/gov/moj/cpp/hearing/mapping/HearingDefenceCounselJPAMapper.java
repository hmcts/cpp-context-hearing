package uk.gov.moj.cpp.hearing.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class HearingDefenceCounselJPAMapper {

    public static final String MIDDLE_NAME = "middleName";
    public static final String USER_ID = "userId";
    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
    private final Function<String, LocalDate> stringToLocalDate = LocalDate::parse;

    public HearingDefenceCounsel toJPA(final Hearing hearing, final uk.gov.justice.core.courts.DefenceCounsel pojo) {
        if (null == pojo) {
            return null;
        }
        final HearingDefenceCounsel defenceCounsel = new HearingDefenceCounsel();
        defenceCounsel.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        defenceCounsel.setHearing(hearing);

        final JsonArrayBuilder defendants = createArrayBuilder();
        pojo.getDefendants().forEach(defendantId -> defendants.add(defendantId.toString()));

        final JsonArrayBuilder attendanceDays = createArrayBuilder();
        pojo.getAttendanceDays().forEach(localDate -> attendanceDays.add(localDate.toString()));

        final JsonObjectBuilder payLoad = createObjectBuilder()
                .add("attendanceDays", attendanceDays)
                .add("firstName", pojo.getFirstName())
                .add("lastName", pojo.getLastName())
                .add("defendants", defendants)
                .add("status", pojo.getStatus())
                .add("id", pojo.getId().toString())
                .add("title", pojo.getTitle());

        if (pojo.getUserId() != null ) {
            payLoad.add(USER_ID, pojo.getUserId().toString());
        }
        if (pojo.getMiddleName() != null) {
            payLoad.add(MIDDLE_NAME, pojo.getMiddleName());
        }
        final JsonNode jsonNode = mapper.valueToTree(payLoad.build());
        defenceCounsel.setPayload(jsonNode);
        return defenceCounsel;
    }

    public uk.gov.justice.core.courts.DefenceCounsel fromJPA(final HearingDefenceCounsel entity) {
        if (null == entity) {
            return null;
        }

        final JsonObject entityPayload = jsonFromString(entity.getPayload().toString());


        final uk.gov.justice.core.courts.DefenceCounsel.Builder defenceCounselBuilder =
                uk.gov.justice.core.courts.DefenceCounsel.defenceCounsel()
                        .withId(entity.getId().getId())
                        .withFirstName(entityPayload.getString("firstName"))
                        .withLastName(entityPayload.getString("lastName"))
                        .withMiddleName(entityPayload.getString(MIDDLE_NAME, null))
                        .withStatus(entityPayload.getString("status"))
                        .withTitle(entityPayload.getString("title"))
                        .withDefendants(entityPayload.getJsonArray("defendants")
                                .stream()
                                .map(e -> fromString(((JsonString) e).getString()))
                                .collect(toList()))
                        .withAttendanceDays(entityPayload.getJsonArray("attendanceDays")
                                .stream()
                                .map(e -> stringToLocalDate.apply(((JsonString) e).getString()))
                                .collect(toList()));

        if (entityPayload.getString(USER_ID, null) != null && !entityPayload.getString(USER_ID).isEmpty()) {
            defenceCounselBuilder.withUserId(UUID.fromString(entityPayload.getString(USER_ID)));

        }
        return defenceCounselBuilder.build();
    }

    public Set<HearingDefenceCounsel> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.DefenceCounsel> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.DefenceCounsel> fromJPA(Set<HearingDefenceCounsel> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().filter(pc -> !pc.isDeleted()).map(this::fromJPA).collect(toList());
    }

    private JsonObject jsonFromString(String jsonObjectStr) {

        JsonObject object;
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr))) {
            object = jsonReader.readObject();
        }

        return object;
    }
}