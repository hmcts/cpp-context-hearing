package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Jurors;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JurorsJPAMapper {

    public Jurors toJPA(final uk.gov.justice.core.courts.Jurors pojo) {
        if (null == pojo) {
            return null;
        }
        final Jurors jurors = new Jurors();
        jurors.setNumberOfJurors(pojo.getNumberOfJurors());
        jurors.setNumberOfSplitJurors(pojo.getNumberOfSplitJurors());
        jurors.setUnanimous(pojo.getUnanimous());
        return jurors;
    }

    public uk.gov.justice.core.courts.Jurors fromJPA(final Jurors entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.Jurors.jurors()
                .withNumberOfJurors(entity.getNumberOfJurors())
                .withNumberOfSplitJurors(entity.getNumberOfSplitJurors())
                .withUnanimous(entity.getUnanimous())
                .build();
    }
}
