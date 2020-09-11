package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Plea;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PleaJPAMapper {

    private DelegatedPowersJPAMapper delegatedPowersJPAMapper;
    private LesserOrAlternativeOffenceForPleaJPAMapper lesserOrAlternativeOffenceForPleaJPAMapper;

    @Inject
    public PleaJPAMapper(final DelegatedPowersJPAMapper delegatedPowersJPAMapper,
                         final LesserOrAlternativeOffenceForPleaJPAMapper lesserOrAlternativeOffenceForPleaJPAMapper) {
        this.delegatedPowersJPAMapper = delegatedPowersJPAMapper;
        this.lesserOrAlternativeOffenceForPleaJPAMapper = lesserOrAlternativeOffenceForPleaJPAMapper;
    }

    //To keep cditester happy
    public PleaJPAMapper() {
    }

    public Plea toJPA(final uk.gov.justice.core.courts.Plea pojo) {
        if (null == pojo) {
            return null;
        }
        final Plea plea = new Plea();
        plea.setDelegatedPowers(delegatedPowersJPAMapper.toJPA(pojo.getDelegatedPowers()));
        plea.setLesserOrAlternativeOffence(lesserOrAlternativeOffenceForPleaJPAMapper.toJPA(pojo.getLesserOrAlternativeOffence()));
        plea.setOriginatingHearingId(pojo.getOriginatingHearingId());
        plea.setPleaDate(pojo.getPleaDate());
        plea.setPleaValue(pojo.getPleaValue());
        return plea;
    }

    public uk.gov.justice.core.courts.Plea fromJPA(final UUID offenceId, final Plea entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.Plea.plea()
                .withDelegatedPowers(delegatedPowersJPAMapper.fromJPA(entity.getDelegatedPowers()))
                .withLesserOrAlternativeOffence(lesserOrAlternativeOffenceForPleaJPAMapper.fromJPA(entity.getLesserOrAlternativeOffence()))
                .withPleaDate(entity.getPleaDate())
                .withOriginatingHearingId(entity.getOriginatingHearingId())
                .withPleaValue(entity.getPleaValue())
                .withOffenceId(offenceId)
                .build();
    }
}