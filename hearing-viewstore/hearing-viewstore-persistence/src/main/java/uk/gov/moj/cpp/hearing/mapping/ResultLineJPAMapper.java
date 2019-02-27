package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ResultLineJPAMapper {

    private PromptJPAMapper promptJPAMapper;

    private DelegatedPowersJPAMapper delegatedPowersJPAMapper;

    @Inject
    public ResultLineJPAMapper(PromptJPAMapper promptJPAMapper, DelegatedPowersJPAMapper delegatedPowersJPAMapper) {
        this.promptJPAMapper = promptJPAMapper;
        this.delegatedPowersJPAMapper = delegatedPowersJPAMapper;
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

    public Set<ResultLine> toJPA(final Target target, final List<uk.gov.justice.core.courts.ResultLine> pojos) {
        if (pojos == null) {
            return new HashSet<>();
        } else {
            return pojos.stream().map(pojo -> toJPA(target, pojo)).collect(Collectors.toSet());
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

    public List<uk.gov.justice.core.courts.ResultLine> fromJPA(Set<ResultLine> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}