package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import org.json.JSONObject;

@ApplicationScoped
public class TargetJPAMapper {

    private ResultLineJPAMapper resultLineJPAMapper;

    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    private ObjectToJsonValueConverter objectToJsonValueConverter;

    @Inject
    public TargetJPAMapper(ResultLineJPAMapper resultLineJPAMapper, ObjectToJsonObjectConverter objectToJsonObjectConverter, ObjectToJsonValueConverter objectToJsonValueConverter) {
        this.resultLineJPAMapper = resultLineJPAMapper;
        this.objectToJsonObjectConverter  = objectToJsonObjectConverter;
        this.objectToJsonValueConverter = objectToJsonValueConverter;
    }

    public TargetJPAMapper() {
    }

    public Target toJPA(final Hearing hearing, final uk.gov.justice.core.courts.Target pojo) {
        if (null == pojo) {
            return null;
        }
        final Target target = new Target();
        final HearingSnapshotKey hearingSnapshotKey = new HearingSnapshotKey(pojo.getTargetId(), hearing.getId());
        target.setId(hearingSnapshotKey);
        target.setHearing(hearing);
        target.setDefendantId(pojo.getDefendantId());
        target.setDraftResult(pojo.getDraftResult());
        target.setOffenceId(pojo.getOffenceId());
        target.setApplicationId(pojo.getApplicationId());
        target.setResultLines(resultLineJPAMapper.toJPA(target, pojo.getResultLines()));
        target.setShadowListed(pojo.getShadowListed());
        if (Objects.nonNull(pojo.getHearingDay())){
            target.setHearingDay(pojo.getHearingDay().toString());
        }
        return target;
    }

    public Target toJPA2(final Hearing hearing, final uk.gov.justice.core.courts.Target2 pojo) {
        if (null == pojo) {
            return null;
        }
        final Target target = new Target();
        final HearingSnapshotKey hearingSnapshotKey = new HearingSnapshotKey(pojo.getTargetId(), hearing.getId());
        target.setId(hearingSnapshotKey);
        target.setHearing(hearing);
        target.setDefendantId(pojo.getDefendantId());
        target.setMasterDefendantId(pojo.getMasterDefendantId());
        target.setDraftResult(pojo.getDraftResult());
        target.setOffenceId(pojo.getOffenceId());
        target.setApplicationId(pojo.getApplicationId());
        target.setCaseId(pojo.getCaseId());
        target.setResultLines(new HashSet<>());

        if (nonNull(pojo.getResultLines()) && !pojo.getResultLines().isEmpty()) {
            final JsonObject resultLinesJsonObject = createObjectBuilder()
                    .add("resultLinesJson", pojo.getResultLines()
                            .stream()
                            .map(resultLine2 -> objectToJsonObjectConverter.convert(resultLine2))
                            .reduce(createArrayBuilder(), JsonArrayBuilder::add, JsonArrayBuilder::add)
                            .build())
                    .build();

                target.setResultLinesJson(objectToJsonValueConverter.convert(resultLinesJsonObject).toString());

        }

        target.setShadowListed(pojo.getShadowListed());
        if (Objects.nonNull(pojo.getHearingDay())){
            target.setHearingDay(pojo.getHearingDay().toString());
        }
        return target;
    }
    public uk.gov.justice.core.courts.Target addMasterDefandantId(final uk.gov.justice.core.courts.Target entity, final UUID masterDefendantId) {
        if (null == entity) {
            return null;
        }
        final uk.gov.justice.core.courts.Target.Builder builder = uk.gov.justice.core.courts.Target.target()
                .withDefendantId(entity.getDefendantId())
                .withMasterDefendantId(masterDefendantId)
                .withDraftResult(new JSONObject().toString())
                .withHearingId(entity.getHearingId())
                .withOffenceId(entity.getOffenceId())
                .withApplicationId(entity.getApplicationId())
                .withTargetId(entity.getTargetId())
                .withResultLines(entity.getResultLines())
                .withShadowListed(entity.getShadowListed());
        if (Objects.nonNull(entity.getHearingDay())) {
            builder.withHearingDay(entity.getHearingDay());
        }
        return builder
                .build();
    }

    public uk.gov.justice.core.courts.Target fromJPA(final Target entity, final UUID masterDefendantId) {
        if (null == entity) {
            return null;
        }
        final uk.gov.justice.core.courts.Target.Builder builder = uk.gov.justice.core.courts.Target.target()
                .withDefendantId(entity.getDefendantId())
                .withMasterDefendantId(masterDefendantId)
                .withDraftResult(entity.getDraftResult())
                .withHearingId(entity.getHearing().getId())
                .withOffenceId(entity.getOffenceId())
                .withApplicationId(entity.getApplicationId())
                .withTargetId(entity.getId().getId())
                .withResultLines(resultLineJPAMapper.fromJPA(entity.getResultLines()))
                .withShadowListed(entity.getShadowListed());
        if (Objects.nonNull(entity.getHearingDay())) {
            builder.withHearingDay(LocalDate.parse(entity.getHearingDay()));
        }
        return builder
                .build();
    }

    public Set<Target> toJPA(Hearing hearing, List<uk.gov.justice.core.courts.Target> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.Target> fromJPA(Set<Target> entities, final Set<ProsecutionCase> prosecutionCases) {
        if (isNull(entities)) {
            return new ArrayList<>();
        }
        return entities
                .stream()
                .map(target -> fromJPA(target, getMasterDefendantId(target.getDefendantId(), prosecutionCases)))
                .collect(Collectors.toList());
    }


    public List<uk.gov.justice.core.courts.Target> setMasterDefandantId(Set<uk.gov.justice.core.courts.Target> entities, final Set<ProsecutionCase> prosecutionCases) {
        if (isNull(entities)) {
            return new ArrayList<>();
        }
        return entities
                .stream()
                .map(target -> addMasterDefandantId(target, getMasterDefendantId(target.getDefendantId(), prosecutionCases)))
                .collect(Collectors.toList());
    }

    public UUID getMasterDefendantId(final UUID defendantId, final Set<ProsecutionCase> prosecutionCases) {
        return prosecutionCases
                .stream()
                .flatMap(prosecutionCase -> prosecutionCase.getDefendants().stream())
                .map(Defendant::getId)
                .map(HearingSnapshotKey::getId)
                .filter(defId -> defId.equals(defendantId))
                .findFirst()
                .orElse(null);
    }
}
