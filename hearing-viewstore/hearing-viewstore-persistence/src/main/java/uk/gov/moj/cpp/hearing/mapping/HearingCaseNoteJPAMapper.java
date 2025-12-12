package uk.gov.moj.cpp.hearing.mapping;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.NoteType;
import uk.gov.justice.services.common.converter.ZonedDateTimes;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
public class HearingCaseNoteJPAMapper {

    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    public HearingCaseNote toJPA(final Hearing hearing, final uk.gov.justice.core.courts.HearingCaseNote pojo) {
        if (null == pojo) {
            return null;
        }
        final HearingCaseNote hearingCaseNote = new HearingCaseNote();
        //GPE-7318
        hearingCaseNote.setId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()));
        hearingCaseNote.setHearing(hearing);

        final JsonArrayBuilder prosecutionCases = createArrayBuilder();
        pojo.getProsecutionCases().forEach(caseId -> prosecutionCases.add(caseId.toString()));

        final JsonObjectBuilder payLoad = createObjectBuilder()
                .add("note", pojo.getNote())
                .add("noteType", pojo.getNoteType().toString())
                .add("originatingHearingId", pojo.getOriginatingHearingId().toString())
                .add("noteDateTime", ZonedDateTimes.toString(pojo.getNoteDateTime()))
                .add("prosecutionCases", prosecutionCases)
                .add("courtClerk", Json.createObjectBuilder()
                        .add("userId", pojo.getCourtClerk().getUserId().toString())
                        .add("firstName", pojo.getCourtClerk().getFirstName())
                        .add("lastName", pojo.getCourtClerk().getLastName())
                );

        final JsonNode jsonNode = mapper.valueToTree(payLoad.build());
        hearingCaseNote.setPayload(jsonNode);
        return hearingCaseNote;
    }

    public uk.gov.justice.core.courts.HearingCaseNote fromJPA(final HearingCaseNote entity) {
        if (null == entity) {
            return null;
        }

        final JsonObject entityPayload = jsonFromString(entity.getPayload().toString());
        final JsonObject courtClerk = entityPayload.getJsonObject("courtClerk");

        final NoteType noteType = Optional.ofNullable(entityPayload.getString("noteType")).map(NoteType::valueOf).orElse(null);
        return uk.gov.justice.core.courts.HearingCaseNote.hearingCaseNote()
                .withCourtClerk(DelegatedPowers.delegatedPowers()
                        .withUserId(fromString(courtClerk.getString("userId")))
                        .withLastName(courtClerk.getString("lastName"))
                        .withFirstName(courtClerk.getString("firstName"))
                        .build())
                .withNote(entityPayload.getString("note"))
                .withNoteDateTime(ZonedDateTimes.fromString(entityPayload.getString("noteDateTime")))
                .withNoteType(noteType)
                .withOriginatingHearingId(entity.getHearing().getId())
                .withProsecutionCases(entityPayload.getJsonArray("prosecutionCases")
                        .stream()
                        .map(e -> fromString(((JsonString) e).getString()))
                        .collect(toList()))
                .build();
    }

    public Set<HearingCaseNote> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.HearingCaseNote> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.HearingCaseNote> fromJPA(Set<HearingCaseNote> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(toList());
    }

    private JsonObject jsonFromString(String jsonObjectStr) {

        JsonObject object;
        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr))) {
            object = jsonReader.readObject();
        }

        return object;
    }
}