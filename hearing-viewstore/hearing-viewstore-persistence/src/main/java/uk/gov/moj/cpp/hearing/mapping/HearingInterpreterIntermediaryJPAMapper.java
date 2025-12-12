package uk.gov.moj.cpp.hearing.mapping;

import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.core.courts.Attendant;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingInterpreterIntermediary;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonString;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class HearingInterpreterIntermediaryJPAMapper {

    public static final String ATTENDANT_TYPE = "attendantType";
    public static final String ATTENDANT = "attendant";
    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
    private final Function<String, LocalDate> stringToLocalDate = LocalDate::parse;

    public HearingInterpreterIntermediary toJPA(final Hearing hearing, final uk.gov.justice.core.courts.InterpreterIntermediary pojo) {
        if (null == pojo) {
            return null;
        }

        final HearingInterpreterIntermediary hearingInterpreterIntermediary = new HearingInterpreterIntermediary();
        hearingInterpreterIntermediary.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        hearingInterpreterIntermediary.setHearing(hearing);

        final JsonObjectBuilder attendant = createObjectBuilder();
        if(pojo.getAttendant().getAttendantType() == uk.gov.justice.hearing.courts.AttendantType.DEFENDANTS) {
            attendant.add(ATTENDANT_TYPE, pojo.getAttendant().getAttendantType().toString());
            attendant.add("defendantId", pojo.getAttendant().getDefendantId().toString());
        }

        if(pojo.getAttendant().getAttendantType() == uk.gov.justice.hearing.courts.AttendantType.WITNESS) {
            attendant.add(ATTENDANT_TYPE, pojo.getAttendant().getAttendantType().toString());
            attendant.add("name", pojo.getAttendant().getName());
        }

        final JsonArrayBuilder attendanceDays = createArrayBuilder();
        pojo.getAttendanceDays().forEach(localDate -> attendanceDays.add(localDate.toString()));

        final JsonObjectBuilder payLoad = createObjectBuilder()
                .add("attendanceDays", attendanceDays)
                .add(ATTENDANT, attendant)
                .add("firstName", pojo.getFirstName())
                .add("lastName", pojo.getLastName())
                .add("id", pojo.getId().toString())
                .add("role", pojo.getRole().toString());

        final JsonNode jsonNode = mapper.valueToTree(payLoad.build());
        hearingInterpreterIntermediary.setPayload(jsonNode);
        return hearingInterpreterIntermediary;
    }

    public uk.gov.justice.core.courts.InterpreterIntermediary fromJPA(final HearingInterpreterIntermediary entity) {
        if (null == entity) {
            return null;
        }

        final JsonObject entityPayload = jsonFromString(entity.getPayload().toString());

        final uk.gov.justice.hearing.courts.AttendantType attendantType = uk.gov.justice.hearing.courts.AttendantType.valueOf(entityPayload.getJsonObject(ATTENDANT).getString(ATTENDANT_TYPE));

        Attendant.Builder attendantBuilder = Attendant.attendant().withAttendantType(attendantType);


        if(uk.gov.justice.hearing.courts.AttendantType.DEFENDANTS == attendantType) {
            attendantBuilder = attendantBuilder.withDefendantId(UUID.fromString(entityPayload.getJsonObject(ATTENDANT).getString("defendantId")));
        }

        if(uk.gov.justice.hearing.courts.AttendantType.WITNESS == attendantType) {
            attendantBuilder = attendantBuilder.withName(entityPayload.getJsonObject(ATTENDANT).getString("name"));
        }

        return uk.gov.justice.core.courts.InterpreterIntermediary.interpreterIntermediary()
                .withId(entity.getId().getId())
                .withFirstName(entityPayload.getString("firstName"))
                .withLastName(entityPayload.getString("lastName"))
                .withRole(uk.gov.justice.hearing.courts.Role.valueOf(entityPayload.getString("role")))
                .withAttendant(attendantBuilder.build())
                .withAttendanceDays(entityPayload.getJsonArray("attendanceDays")
                        .stream()
                        .map(e -> stringToLocalDate.apply(((JsonString) e).getString()))
                        .collect(toList()))
                .build();
    }

    public Set<HearingInterpreterIntermediary> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.InterpreterIntermediary> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.InterpreterIntermediary> fromJPA(Set<HearingInterpreterIntermediary> entities) {
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