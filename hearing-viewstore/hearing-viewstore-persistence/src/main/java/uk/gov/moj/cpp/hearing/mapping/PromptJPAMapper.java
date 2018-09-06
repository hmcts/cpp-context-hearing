package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Prompt;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PromptJPAMapper {

    public Prompt toJPA(final ResultLine resultLine, final uk.gov.justice.json.schemas.core.Prompt pojo) {
        if (null == pojo) {
            return null;
        }
        return Prompt.prompt()
                .setId(pojo.getId())
                .setLabel(pojo.getLabel())
                .setValue(pojo.getValue())
                .setFixedListCode(pojo.getFixedListCode())
                .setWelshValue(pojo.getWelshValue())
                .setResultLine(resultLine);
    }

    public List<Prompt> toJPA(final ResultLine resultLine, final List<uk.gov.justice.json.schemas.core.Prompt> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        } else {
            return pojos.stream().map(pojo -> toJPA(resultLine, pojo)).collect(Collectors.toList());
        }
    }

    public uk.gov.justice.json.schemas.core.Prompt fromJPA(final Prompt entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.Prompt.prompt()
                .withFixedListCode(entity.getFixedListCode())
                .withId(entity.getId())
                .withLabel(entity.getLabel())
                .withValue(entity.getValue())
                .withWelshValue(entity.getWelshValue())
                .build();
    }

    public List<uk.gov.justice.json.schemas.core.Prompt> fromJPA(List<Prompt> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}