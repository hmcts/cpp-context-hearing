package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.ResultLine2;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
public class ResultLineJPAMapper {

    private PromptJPAMapper promptJPAMapper;

    private DelegatedPowersJPAMapper delegatedPowersJPAMapper;

    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private StringToJsonObjectConverter stringToJsonObjectConverter;


    @Inject
    public ResultLineJPAMapper(final PromptJPAMapper promptJPAMapper,
                               final DelegatedPowersJPAMapper delegatedPowersJPAMapper,
                               final JsonObjectToObjectConverter jsonObjectToObjectConverter,
                               final StringToJsonObjectConverter stringToJsonObjectConverter) {
        this.promptJPAMapper = promptJPAMapper;
        this.delegatedPowersJPAMapper = delegatedPowersJPAMapper;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.stringToJsonObjectConverter = stringToJsonObjectConverter;
    }

    //To kepp cdi tester happy
    public ResultLineJPAMapper() {
    }

    public ResultLine toJPA(final Target target, final uk.gov.justice.core.courts.ResultLine pojo) {
        if (null == pojo) {
            return null;
        }
        final ResultLine resultLine = ResultLine.resultLine()
                .setId(pojo.getResultLineId())
                .setResultDefinitionId(pojo.getResultDefinitionId())
                .setResultLabel(pojo.getResultLabel())
                .setSharedDate(pojo.getSharedDate())
                .setTarget(target)
                .setOrderedDate(pojo.getOrderedDate())
                .setComplete(pojo.getIsComplete())
                .setLevel(pojo.getLevel())
                .setModified(pojo.getIsModified())
                .setDelegatedPowers(delegatedPowersJPAMapper.toJPA(pojo.getDelegatedPowers()));
        resultLine.setPrompts(promptJPAMapper.toJPA(resultLine, pojo.getPrompts()));
        target.getResultLines().add(resultLine);
        return resultLine;
    }
    public ResultLine toJPA2(final Target target, final uk.gov.justice.core.courts.ResultLine2 pojo) {
        if (null == pojo) {
            return null;
        }
        final ResultLine resultLine = ResultLine.resultLine()
                .setApplicationId(pojo.getApplicationId())
                .setOffenceId(pojo.getOffenceId())
                .setCaseId(pojo.getCaseId())
                .setDefendantId(pojo.getDefendantId())
                .setMasterDefendantId(pojo.getMasterDefendantId())
                .setShadowListed(pojo.getShadowListed())
                .setCaseId(pojo.getCaseId())
                .setAmendmentDate(pojo.getAmendmentDate())
                .setAmendmentReason(pojo.getAmendmentReason())
                .setAmendmentReasonId(pojo.getAmendmentReasonId())
                .setChildResultLineIds(StringUtils.join(pojo.getChildResultLineIds(), ','))
                .setParentResultLineIds(StringUtils.join(pojo.getParentResultLineIds(), ','))
                .setShortCode(pojo.getShortCode())
                .setId(pojo.getResultLineId())
                .setResultDefinitionId(pojo.getResultDefinitionId())
                .setResultLabel(pojo.getResultLabel())
                .setSharedDate(pojo.getSharedDate())
                .setTarget(target)
                .setOrderedDate(pojo.getOrderedDate())
                .setComplete(pojo.getIsComplete())
                .setDeleted(pojo.getIsDeleted())
                .setLevel(pojo.getLevel())
                .setModified(pojo.getIsModified())
                .setDelegatedPowers(delegatedPowersJPAMapper.toJPA(pojo.getDelegatedPowers()));
        resultLine.setPrompts(promptJPAMapper.toJPA(resultLine, pojo.getPrompts()));
        target.getResultLines().add(resultLine);
        return resultLine;
    }
    public Set<ResultLine> toJPA(final Target target, final List<uk.gov.justice.core.courts.ResultLine> pojos) {
        if (pojos == null) {
            return new HashSet<>();
        } else {
            return pojos.stream().map(pojo -> toJPA(target, pojo)).collect(Collectors.toSet());
        }
    }
    public Set<ResultLine> toJPA2(final Target target, final List<uk.gov.justice.core.courts.ResultLine2> pojos) {
        if (pojos == null) {
            return new HashSet<>();
        } else {
            return pojos.stream().map(pojo -> toJPA2(target, pojo)).collect(Collectors.toSet());
        }
    }
    public uk.gov.justice.core.courts.ResultLine fromJPA(final ResultLine entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.ResultLine.resultLine()
                .withResultLineId(entity.getId())
                .withDelegatedPowers(delegatedPowersJPAMapper.fromJPA(entity.getDelegatedPowers()))
                .withIsComplete(entity.getComplete())
                .withIsModified(entity.getModified())
                .withLevel(entity.getLevel())
                .withOrderedDate(entity.getOrderedDate())
                .withPrompts(promptJPAMapper.fromJPA(entity.getPrompts()))
                .withResultDefinitionId(entity.getResultDefinitionId())
                .withResultLabel(entity.getResultLabel())
                .withSharedDate(entity.getSharedDate())
                .build();
    }
    public uk.gov.justice.core.courts.ResultLine2 fromJPA2(final ResultLine entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.ResultLine2.resultLine2()
                .withAmendmentDate(entity.getAmendmentDate())
                .withApplicationId(entity.getApplicationId())
                .withOffenceId(entity.getOffenceId())
                .withDefendantId(entity.getDefendantId())
                .withMasterDefendantId(entity.getMasterDefendantId())
                .withShadowListed(entity.getShadowListed())
                .withCaseId(entity.getCaseId())
                .withAmendmentReason(entity.getAmendmentReason())
                .withAmendmentReasonId(entity.getAmendmentReasonId())
                .withChildResultLineIds(toList(entity.getChildResultLineIds()))
                .withParentResultLineIds(toList(entity.getParentResultLineIds()))
                .withResultLineId(entity.getId())
                .withDelegatedPowers(delegatedPowersJPAMapper.fromJPA(entity.getDelegatedPowers()))
                .withIsComplete(entity.getComplete())
                .withIsModified(entity.getModified())
                .withIsDeleted(entity.getDeleted())
                .withLevel(entity.getLevel())
                .withOrderedDate(entity.getOrderedDate())
                .withPrompts(promptJPAMapper.fromJPA(entity.getPrompts()))
                .withResultDefinitionId(entity.getResultDefinitionId())
                .withResultLabel(entity.getResultLabel())
                .withSharedDate(entity.getSharedDate())
                .withShortCode(entity.getShortCode())
                .build();
    }

    public List<uk.gov.justice.core.courts.ResultLine> fromJPA(Set<ResultLine> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }

    public List<uk.gov.justice.core.courts.ResultLine2> fromJPA2(Set<ResultLine> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA2).collect(Collectors.toList());
    }

    public List<uk.gov.justice.core.courts.ResultLine2> fromJPA2(final String resultLinesAsString) {
        return resultLinesAsString != null ? stringToJsonObjectConverter.convert(resultLinesAsString)
                .getJsonArray("resultLinesJson")
                .getValuesAs(JsonObject.class)
                .stream()
                .map(e -> jsonObjectToObjectConverter.convert(e, ResultLine2.class))
                .collect(Collectors.toList()) : new ArrayList<>();
    }

    private static List<UUID> toList(String ids) {
        return nonNull(ids) && !ids.isEmpty()? Arrays.asList(ids.split(",")).stream().map(UUID::fromString).collect(Collectors.toList()): Collections.emptyList();
    }
}