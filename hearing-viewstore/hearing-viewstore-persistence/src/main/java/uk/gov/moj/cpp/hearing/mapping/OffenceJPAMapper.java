package uk.gov.moj.cpp.hearing.mapping;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class OffenceJPAMapper {

    private NotifiedPleaJPAMapper notifiedPleaJPAMapper;

    private IndicatedPleaJPAMapper indicatedPleaJPAMapper;

    private PleaJPAMapper pleaJPAMapper;

    private OffenceFactsJPAMapper offenceFactsJPAMapper;

    private VerdictJPAMapper verdictJPAMapper;

    @Inject
    public OffenceJPAMapper(NotifiedPleaJPAMapper notifiedPleaJPAMapper, IndicatedPleaJPAMapper indicatedPleaJPAMapper,
            PleaJPAMapper pleaJPAMapper, OffenceFactsJPAMapper offenceFactsJPAMapper,
            VerdictJPAMapper verdictJPAMapper) {
        this.notifiedPleaJPAMapper = notifiedPleaJPAMapper;
        this.indicatedPleaJPAMapper = indicatedPleaJPAMapper;
        this.pleaJPAMapper = pleaJPAMapper;
        this.offenceFactsJPAMapper = offenceFactsJPAMapper;
        this.verdictJPAMapper = verdictJPAMapper;
    }

    //To keep cditester happy
    public OffenceJPAMapper() {
    }

    public Offence toJPA(final Hearing hearing, final UUID defendantId, final uk.gov.justice.json.schemas.core.Offence pojo) {
        if (null == pojo) {
            return null;
        }
        final Offence offence = new Offence();

        offence.setId(new HearingSnapshotKey(pojo.getId(), hearing.getId()));
        offence.setOffenceDefinitionId(pojo.getOffenceDefinitionId());
        offence.setDefendantId(defendantId);

        offence.setOffenceCode(pojo.getOffenceCode());
        offence.setOffenceTitle(pojo.getOffenceTitle());
        offence.setOffenceTitleWelsh(pojo.getOffenceTitleWelsh());
        offence.setOffenceLegislation(pojo.getOffenceLegislation());
        offence.setOffenceLegislationWelsh(pojo.getOffenceLegislationWelsh());

        offence.setModeOfTrial(pojo.getModeOfTrial());
        offence.setWording(pojo.getWording());
        offence.setWordingWelsh(pojo.getWordingWelsh());

        offence.setStartDate(pojo.getStartDate());
        offence.setEndDate(pojo.getEndDate());
        offence.setChargeDate(pojo.getChargeDate());
        offence.setArrestDate(pojo.getArrestDate());

        offence.setOrderIndex(pojo.getOrderIndex());
        offence.setCount(pojo.getCount());
        offence.setConvictionDate(pojo.getConvictionDate());

        offence.setNotifiedPlea(notifiedPleaJPAMapper.toJPA(pojo.getNotifiedPlea()));
        offence.setIndicatedPlea(indicatedPleaJPAMapper.toJPA(pojo.getIndicatedPlea()));
        offence.setOffenceFacts(offenceFactsJPAMapper.toJPA(pojo.getOffenceFacts()));
        offence.setPlea(pleaJPAMapper.toJPA(pojo.getPlea()));
        offence.setVerdict(verdictJPAMapper.toJPA(pojo.getVerdict()));

        return offence;
    }

    public uk.gov.justice.json.schemas.core.Offence fromJPA(final Offence entity) {
        if (null == entity) {
            return null;
        }
        final UUID offenceId = entity.getId().getId();
        return uk.gov.justice.json.schemas.core.Offence.offence()
                .withId(offenceId)
                .withOffenceDefinitionId(entity.getOffenceDefinitionId())
                .withOffenceCode(entity.getOffenceCode())
                .withOffenceTitle(entity.getOffenceTitle())
                .withOffenceTitleWelsh(entity.getOffenceTitleWelsh())
                .withOffenceLegislation(entity.getOffenceLegislation())
                .withOffenceLegislationWelsh(entity.getOffenceLegislationWelsh())

                .withModeOfTrial(entity.getModeOfTrial())
                .withWording(entity.getWording())
                .withWordingWelsh(entity.getWordingWelsh())

                .withStartDate(entity.getStartDate())
                .withEndDate(entity.getEndDate())
                .withChargeDate(entity.getChargeDate())
                .withArrestDate(entity.getArrestDate())

                .withOrderIndex(entity.getOrderIndex())
                .withCount(entity.getCount())
                .withConvictionDate(entity.getConvictionDate())

                .withNotifiedPlea(notifiedPleaJPAMapper.fromJPA(offenceId, entity.getNotifiedPlea()))
                .withIndicatedPlea(indicatedPleaJPAMapper.fromJPA(offenceId, entity.getIndicatedPlea()))
                .withOffenceFacts(offenceFactsJPAMapper.fromJPA(entity.getOffenceFacts()))
                .withPlea(pleaJPAMapper.fromJPA(offenceId, entity.getPlea()))
                .withVerdict(verdictJPAMapper.fromJPA(offenceId, entity.getVerdict()))

                .build();
    }

    public Set<Offence> toJPA(final Hearing hearing, final UUID defendantId, final List<uk.gov.justice.json.schemas.core.Offence> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, defendantId, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.json.schemas.core.Offence> fromJPA(final Set<Offence> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}