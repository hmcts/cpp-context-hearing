package uk.gov.moj.cpp.hearing.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote;

@ApplicationScoped //TODO Will be covered by GPE-5922 story
public class HearingCaseNoteJPAMapper {

    private HearingCaseNote toJPA(final uk.gov.justice.json.schemas.core.HearingCaseNote pojo) {
        if (null == pojo) {
            return null;
        }
        return new HearingCaseNote();
    }

    private uk.gov.justice.json.schemas.core.HearingCaseNote fromJPA(final HearingCaseNote entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.HearingCaseNote.hearingCaseNote()
                .build();
    }

    public List<HearingCaseNote> toJPA(List<uk.gov.justice.json.schemas.core.HearingCaseNote> pojos) {
        if (null == pojos) {
            return new ArrayList<>();
        }
        return pojos.stream().map(this::toJPA).collect(Collectors.toList());
    }

    public List<uk.gov.justice.json.schemas.core.HearingCaseNote> fromJPA(List<HearingCaseNote> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}