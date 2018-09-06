package uk.gov.moj.cpp.hearing.mapping;

import javax.enterprise.context.ApplicationScoped;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors;

@ApplicationScoped
public class JurorsJPAMapper {

    public Jurors toJPA(final uk.gov.justice.json.schemas.core.Jurors pojo) {
        if (null == pojo) {
            return null;
        }
        final Jurors jurors = new Jurors();
        jurors.setNumberOfJurors(pojo.getNumberOfJurors());
        jurors.setNumberOfSplitJurors(pojo.getNumberOfSplitJurors());
        jurors.setUnanimous(pojo.getUnanimous());
        return jurors;
    }

    public uk.gov.justice.json.schemas.core.Jurors fromJPA(final Jurors entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.json.schemas.core.Jurors.jurors()
                .withNumberOfJurors(entity.getNumberOfJurors())
                .withNumberOfSplitJurors(entity.getNumberOfSplitJurors())
                .withUnanimous(entity.getUnanimous())
                .build();
    }
}
