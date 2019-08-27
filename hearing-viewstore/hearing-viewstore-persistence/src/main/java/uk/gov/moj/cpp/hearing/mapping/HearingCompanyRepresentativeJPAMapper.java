package uk.gov.moj.cpp.hearing.mapping;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.justice.hearing.courts.Position;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCompanyRepresentative;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class HearingCompanyRepresentativeJPAMapper {

    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
    private final Function<String, LocalDate> stringToLocalDate = LocalDate::parse;

    public HearingCompanyRepresentative toJPA(final Hearing hearing, final uk.gov.justice.core.courts.CompanyRepresentative pojo) {
        if (null == pojo) {
            return null;
        }
        final HearingCompanyRepresentative hearingCompanyRepresentative = new HearingCompanyRepresentative();
        hearingCompanyRepresentative.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        hearingCompanyRepresentative.setHearing(hearing);

        final JsonArrayBuilder defendants = createArrayBuilder();
        pojo.getDefendants().forEach(defendantId -> defendants.add(defendantId.toString()));

        final JsonArrayBuilder attendanceDays = createArrayBuilder();
        pojo.getAttendanceDays().forEach(localDate -> attendanceDays.add(localDate.toString()));

        final JsonObjectBuilder payLoad = createObjectBuilder()
                .add("id", pojo.getId().toString())
                .add("title", pojo.getTitle())
                .add("firstName", pojo.getFirstName())
                .add("lastName", pojo.getLastName())
                .add("position", pojo.getPosition().toString())
                .add("defendants", defendants)
                .add("attendanceDays", attendanceDays);

        final JsonNode jsonNode = mapper.valueToTree(payLoad.build());
        hearingCompanyRepresentative.setPayload(jsonNode);
        return hearingCompanyRepresentative;
    }

    public uk.gov.justice.core.courts.CompanyRepresentative fromJPA(final HearingCompanyRepresentative entity) {
        if (null == entity) {
            return null;
        }

        final JsonObject entityPayload = jsonFromString(entity.getPayload().toString());
        return uk.gov.justice.core.courts.CompanyRepresentative.companyRepresentative()
                .withId(entity.getId().getId())
                .withTitle(entityPayload.getString("title"))
                .withFirstName(entityPayload.getString("firstName"))
                .withLastName(entityPayload.getString("lastName"))
                .withPosition(Position.valueOf(entityPayload.getString("position")))
                .withDefendants(entityPayload.getJsonArray("defendants")
                        .stream()
                        .map(e -> fromString(((JsonString) e).getString()))
                        .collect(toList()))
                .withAttendanceDays(entityPayload.getJsonArray("attendanceDays")
                        .stream()
                        .map(e -> stringToLocalDate.apply(((JsonString) e).getString()))
                        .collect(toList()))
                .build();
    }

    public Set<HearingCompanyRepresentative> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.CompanyRepresentative> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.CompanyRepresentative> fromJPA(Set<HearingCompanyRepresentative> entities) {
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