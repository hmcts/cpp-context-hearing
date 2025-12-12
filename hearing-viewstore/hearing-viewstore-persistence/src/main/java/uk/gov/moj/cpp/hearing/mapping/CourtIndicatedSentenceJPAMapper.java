package uk.gov.moj.cpp.hearing.mapping;


import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtIndicatedSentence;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CourtIndicatedSentenceJPAMapper {

    public CourtIndicatedSentence toJPA(final uk.gov.justice.core.courts.CourtIndicatedSentence pojo) {
        if (null == pojo) {
            return null;
        }
        final CourtIndicatedSentence courtIndicatedSentence = new CourtIndicatedSentence();
        courtIndicatedSentence.setCourtIndicatedSentenceTypeId(pojo.getCourtIndicatedSentenceTypeId());
        courtIndicatedSentence.setCourtIndicatedSentenceDescription(pojo.getCourtIndicatedSentenceDescription());

        return courtIndicatedSentence;
    }

    public uk.gov.justice.core.courts.CourtIndicatedSentence fromJPA(final CourtIndicatedSentence entity) {
        if (null == entity) {
            return null;
        }

        return uk.gov.justice.core.courts.CourtIndicatedSentence.courtIndicatedSentence()
                .withCourtIndicatedSentenceTypeId(entity.getCourtIndicatedSentenceTypeId())
                .withCourtIndicatedSentenceDescription(entity.getCourtIndicatedSentenceDescription())
                .build();
    }
}
