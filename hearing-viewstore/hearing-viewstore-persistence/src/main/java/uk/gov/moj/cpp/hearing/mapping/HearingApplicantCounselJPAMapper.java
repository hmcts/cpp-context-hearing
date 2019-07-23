package uk.gov.moj.cpp.hearing.mapping;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicantCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class HearingApplicantCounselJPAMapper {

    public static final String MIDDLE_NAME = "middleName";
    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
    private final Function<String, LocalDate> stringToLocalDate = s -> LocalDate.parse(s);

    public HearingApplicantCounsel toJPA(final Hearing hearing, final uk.gov.justice.core.courts.ApplicantCounsel pojo) {
        if (null == pojo) {
            return null;
        }
        final HearingApplicantCounsel hearingApplicantCounsel = new HearingApplicantCounsel();
        hearingApplicantCounsel.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        hearingApplicantCounsel.setHearing(hearing);

        final JsonArrayBuilder applicants = createArrayBuilder();
        pojo.getApplicants().forEach(applicantId -> applicants.add(applicantId.toString()));

        final JsonArrayBuilder attendanceDays = createArrayBuilder();
        pojo.getAttendanceDays().forEach(localDate -> attendanceDays.add(localDate.toString()));

        final JsonObjectBuilder payLoad = createObjectBuilder()
                .add("attendanceDays", attendanceDays)
                .add("firstName", pojo.getFirstName())
                .add("lastName", pojo.getLastName())
                .add("applicants", applicants)
                .add("status", pojo.getStatus())
                .add("id", pojo.getId().toString())
                .add("title", pojo.getTitle());
        if (pojo.getMiddleName() != null) {
            payLoad.add(MIDDLE_NAME, pojo.getMiddleName());
        }
        final JsonNode jsonNode = mapper.valueToTree(payLoad.build());
        hearingApplicantCounsel.setPayload(jsonNode);
        return hearingApplicantCounsel;
    }

    public uk.gov.justice.core.courts.ApplicantCounsel fromJPA(final HearingApplicantCounsel entity) {
        if (null == entity) {
            return null;
        }

        final JsonObject entityPayload = jsonFromString(entity.getPayload().toString());
        return uk.gov.justice.core.courts.ApplicantCounsel.applicantCounsel()
                .withId(entity.getId().getId())
                .withFirstName(entityPayload.getString("firstName"))
                .withLastName(entityPayload.getString("lastName"))
                .withMiddleName(entityPayload.getString(MIDDLE_NAME, null))
                .withStatus(entityPayload.getString("status"))
                .withTitle(entityPayload.getString("title"))
                .withApplicants(entityPayload.getJsonArray("applicants")
                        .stream()
                        .map(e -> fromString(((JsonString) e).getString()))
                        .collect(toList()))
                .withAttendanceDays(entityPayload.getJsonArray("attendanceDays")
                        .stream()
                        .map(e -> stringToLocalDate.apply(((JsonString) e).getString()))
                        .collect(toList()))
                .build();
    }

    public Set<HearingApplicantCounsel> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.ApplicantCounsel> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.ApplicantCounsel> fromJPA(Set<HearingApplicantCounsel> entities) {
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