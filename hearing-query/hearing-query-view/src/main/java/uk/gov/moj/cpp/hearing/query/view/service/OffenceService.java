package uk.gov.moj.cpp.hearing.query.view.service;

import uk.gov.moj.cpp.hearing.persist.PleaHearingRepository;
import uk.gov.moj.cpp.hearing.persist.VerdictHearingRepository;
import uk.gov.moj.cpp.hearing.persist.entity.PleaHearing;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictHearing;
import uk.gov.moj.cpp.hearing.persist.entity.VerdictValue;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Column;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class OffenceService {

    @Inject
    private PleaHearingRepository pleaHearingRepository;

    @Inject
    private VerdictHearingRepository verdictHearingRepository;

    @Transactional
    public JsonObject getOffencesByCaseId(final UUID hearingId) {

        List<PleaHearing> pleaHearings = pleaHearingRepository.findByCaseId(hearingId);
        List<VerdictHearing> verdictHearings = verdictHearingRepository.findByCaseId(hearingId);
        Map<UUID, VerdictHearing> verdictHearingMap = new HashMap<>();
        verdictHearings.forEach(v -> verdictHearingMap.put(v.getOffenceId(), v));

        List<JsonObject> offences = pleaHearings.stream().map(p -> {
            JsonObject plea = Json.createObjectBuilder()
                    .add("pleaId", p.getPleaId().toString())
                    .add("pleaDate", p.getPleaDate() == null ? "" : p.getPleaDate().toString())
                    .add("value", p.getValue().toString()).build();
            VerdictHearing v = verdictHearingMap.get(p.getOffenceId());
            JsonObjectBuilder verdicts = Json.createObjectBuilder();
            if (v != null) {
                verdicts.add("verdictId", v.getVerdictId().toString())
                        .add("value", this.toJsonObjectBuilder(v.getValue()))
                        .add("verdictDate", v.getVerdictDate() == null ? "" : v.getVerdictDate().toString());
            }

            JsonObject offence = Json.createObjectBuilder()
                    .add("offenceId", p.getOffenceId().toString())
                    .add("caseId", p.getCaseId().toString())
                    .add("defendantId", p.getDefendantId().toString())
                    .add("personId", p.getPersonId().toString())
                    .add("plea", plea)
                    .add("verdict", verdicts.build()).build();
            return offence;
        }).collect(Collectors.toList());

        JsonArrayBuilder builder = Json.createArrayBuilder();
        offences.forEach(o -> builder.add(o));
        return Json.createObjectBuilder().add("offences", builder.build()).build();
    }

    private JsonObjectBuilder toJsonObjectBuilder(VerdictValue verdictValue) {
        JsonObjectBuilder result = Json.createObjectBuilder();
        result.add("id", verdictValue.getId().toString());
        result.add("category", verdictValue.getCategory());

        //these null checks will be resolved properly in an uncoming bug fix.
        if (verdictValue.getDescription() != null) {
            result.add("description", verdictValue.getDescription());
        }
        if (verdictValue.getCode() != null) {
            result.add("code", verdictValue.getCode());
        }
        return result;
    }


}
