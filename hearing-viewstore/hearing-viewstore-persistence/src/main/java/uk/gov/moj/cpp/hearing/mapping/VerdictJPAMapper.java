package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Verdict;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class VerdictJPAMapper {

    private JurorsJPAMapper jurorsJPAMapper;
    private LesserOrAlternativeOffenceJPAMapper lesserOrAlternativeOffenceJPAMapper;
    private VerdictTypeJPAMapper verdictTypeJPAMapper;

    @Inject
    public VerdictJPAMapper(JurorsJPAMapper jurorsJPAMapper, LesserOrAlternativeOffenceJPAMapper lesserOrAlternativeOffenceJPAMapper, VerdictTypeJPAMapper verdictTypeJPAMapper) {
        this.jurorsJPAMapper = jurorsJPAMapper;
        this.lesserOrAlternativeOffenceJPAMapper = lesserOrAlternativeOffenceJPAMapper;
        this.verdictTypeJPAMapper = verdictTypeJPAMapper;
    }

    //to keep cdi test happy
    public VerdictJPAMapper() {

    }

    public Verdict toJPA(uk.gov.justice.core.courts.Verdict pojo) {
        if (null == pojo) {
            return null;
        }
        final Verdict verdict = new Verdict();
        verdict.setOriginatingHearingId(pojo.getOriginatingHearingId());
        verdict.setVerdictDate(pojo.getVerdictDate());
        verdict.setJurors(jurorsJPAMapper.toJPA(pojo.getJurors()));
        verdict.setLesserOrAlternativeOffence(lesserOrAlternativeOffenceJPAMapper.toJPA(pojo.getLesserOrAlternativeOffence()));
        verdict.setVerdictType(verdictTypeJPAMapper.toJPA(pojo.getVerdictType()));

        return verdict;
    }

    public uk.gov.justice.core.courts.Verdict fromJPA(final UUID offenceId, final Verdict entity) {
        if (null == entity) {
            return null;
        }
        return uk.gov.justice.core.courts.Verdict.verdict()
                .withOffenceId(offenceId)
                .withVerdictDate(entity.getVerdictDate())
                .withOriginatingHearingId(entity.getOriginatingHearingId())
                .withJurors(jurorsJPAMapper.fromJPA(entity.getJurors()))
                .withLesserOrAlternativeOffence(lesserOrAlternativeOffenceJPAMapper.fromJPA(entity.getLesserOrAlternativeOffence()))
                .withVerdictType(verdictTypeJPAMapper.fromJPA(entity.getVerdictType()))
                .build();
    }
}