package uk.gov.moj.cpp.hearing.mapping;

import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.BailStatus;
import uk.gov.justice.core.courts.CustodyTimeLimit;
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
@SuppressWarnings({"squid:MethodCyclomaticComplexity"})
public class OffenceJPAMapper {

    private NotifiedPleaJPAMapper notifiedPleaJPAMapper;

    private IndicatedPleaJPAMapper indicatedPleaJPAMapper;

    private PleaJPAMapper pleaJPAMapper;

    private OffenceFactsJPAMapper offenceFactsJPAMapper;

    private VerdictJPAMapper verdictJPAMapper;

    private AllocationDecisionJPAMapper allocationDecisionJPAMapper;

    private LaaApplnReferenceJPAMapper laaApplnReferenceJPAMapper;

    private ReportingRestrictionJPAMapper reportingRestrictionJPAMapper;

    @Inject
    public OffenceJPAMapper(final NotifiedPleaJPAMapper notifiedPleaJPAMapper,
                            final IndicatedPleaJPAMapper indicatedPleaJPAMapper,
                            final PleaJPAMapper pleaJPAMapper,
                            final OffenceFactsJPAMapper offenceFactsJPAMapper,
                            final VerdictJPAMapper verdictJPAMapper,
                            final AllocationDecisionJPAMapper allocationDecisionJPAMapper,
                            final LaaApplnReferenceJPAMapper laaApplnReferenceJPAMapper,
                            final ReportingRestrictionJPAMapper reportingRestrictionJPAMapper) {
        this.notifiedPleaJPAMapper = notifiedPleaJPAMapper;
        this.indicatedPleaJPAMapper = indicatedPleaJPAMapper;
        this.pleaJPAMapper = pleaJPAMapper;
        this.offenceFactsJPAMapper = offenceFactsJPAMapper;
        this.verdictJPAMapper = verdictJPAMapper;
        this.allocationDecisionJPAMapper = allocationDecisionJPAMapper;
        this.laaApplnReferenceJPAMapper = laaApplnReferenceJPAMapper;
        this.reportingRestrictionJPAMapper = reportingRestrictionJPAMapper;
    }

    //To keep cditester happy
    public OffenceJPAMapper() {
    }

    @SuppressWarnings("squid:S3776")
    public Offence toJPA(final Hearing hearing, final UUID defendantId, final uk.gov.justice.core.courts.Offence pojo) {
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
        offence.setAllocationDecision(allocationDecisionJPAMapper.toJPA(pojo.getAllocationDecision()));
        if (pojo.getCustodyTimeLimit() != null && (pojo.getCustodyTimeLimit().getDaysSpent() != null
                || pojo.getCustodyTimeLimit().getTimeLimit() != null
                || pojo.getCustodyTimeLimit().getIsCtlExtended() != null)) {
            offence.setCtlDaysSpent(pojo.getCustodyTimeLimit().getDaysSpent());
            offence.setCtlTimeLimit(pojo.getCustodyTimeLimit().getTimeLimit());
            offence.setCtlExtended(pojo.getCustodyTimeLimit().getIsCtlExtended());
        } else {
            offence.setCtlTimeLimit(null);
            offence.setCtlDaysSpent(null);
        }
        offence.setLaidDate(pojo.getLaidDate());

        offence.setLaaApplnReference(laaApplnReferenceJPAMapper.toJpa(pojo.getLaaApplnReference()));
        if(nonNull(pojo.getIsDiscontinued())) {
            offence.setDiscontinued(pojo.getIsDiscontinued());
        }
        if(nonNull(pojo.getIntroducedAfterInitialProceedings())) {
            offence.setIntroduceAfterInitialProceedings(pojo.getIntroducedAfterInitialProceedings());
        }
        if(nonNull(pojo.getProceedingsConcluded())) {
            offence.setProceedingsConcluded(pojo.getProceedingsConcluded());
        }
        if(nonNull(pojo.getReportingRestrictions())) {
            offence.setReportingRestrictions(reportingRestrictionJPAMapper.toJPA(hearing, pojo.getId(), pojo.getReportingRestrictions()));
        }
        if(nonNull(pojo.getEndorsableFlag())) {
            offence.setEndorsableFlag(pojo.getEndorsableFlag());
        }
        if (nonNull(pojo.getCtlClockStopped())) {
            offence.setCtlClockStopped(pojo.getCtlClockStopped());
        }
        if (nonNull(pojo.getBailStatus())) {
            if (nonNull(pojo.getBailStatus().getId())) {
                offence.setBailStatusId(pojo.getBailStatus().getId());
            }
            if (nonNull(pojo.getBailStatus().getCode())) {
                offence.setBailStatusCode(pojo.getBailStatus().getCode());
            }
            if (nonNull(pojo.getBailStatus().getDescription())) {
                offence.setBailStatusDescription(pojo.getBailStatus().getDescription());
            }
        }
        offence.setPreviousDaysHeldInCustody(pojo.getPreviousDaysHeldInCustody());
        offence.setDateHeldInCustodySince(pojo.getDateHeldInCustodySince());
        offence.setListingNumber(pojo.getListingNumber());
        return offence;
    }

    public uk.gov.justice.core.courts.Offence fromJPA(final Offence entity) {
        if (null == entity) {
            return null;
        }
        final UUID offenceId = entity.getId().getId();
        return uk.gov.justice.core.courts.Offence.offence()
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
                .withCustodyTimeLimit(extractCustodyTimeLimit(entity))
                .withNotifiedPlea(notifiedPleaJPAMapper.fromJPA(offenceId, entity.getNotifiedPlea()))
                .withIndicatedPlea(indicatedPleaJPAMapper.fromJPA(offenceId, entity.getIndicatedPlea()))
                .withOffenceFacts(offenceFactsJPAMapper.fromJPA(entity.getOffenceFacts()))
                .withPlea(pleaJPAMapper.fromJPA(offenceId, entity.getPlea()))
                .withVerdict(verdictJPAMapper.fromJPA(offenceId, entity.getVerdict()))
                .withAllocationDecision(allocationDecisionJPAMapper.fromJPA(offenceId, entity.getAllocationDecision()))
                .withLaaApplnReference(laaApplnReferenceJPAMapper.fromJpa(entity.getLaaApplnReference()))
                .withProceedingsConcluded(entity.isProceedingsConcluded())
                .withIsDiscontinued(entity.isDiscontinued())
                .withIntroducedAfterInitialProceedings(entity.isIntroduceAfterInitialProceedings())
                .withLaidDate(entity.getLaidDate())
                .withReportingRestrictions(reportingRestrictionJPAMapper.fromJPA(entity.getReportingRestrictions()))
                .withEndorsableFlag(entity.isEndorsableFlag())
                .withCtlClockStopped(entity.isCtlClockStopped())
                .withBailStatus(extractBailStatus(entity))
                .withDateHeldInCustodySince(entity.getDateHeldInCustodySince())
                .withPreviousDaysHeldInCustody(entity.getPreviousDaysHeldInCustody())
                .withListingNumber(entity.getListingNumber())
                .build();
    }

    public Set<Offence> toJPA(final Hearing hearing, final UUID defendantId, final List<uk.gov.justice.core.courts.Offence> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return pojos.stream().map(pojo -> toJPA(hearing, defendantId, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.Offence> fromJPA(final Set<Offence> entities) {
        if (null == entities) {
            return new ArrayList<>();
        }
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }

    private CustodyTimeLimit extractCustodyTimeLimit(final Offence offence) {
        if (nonNull(offence.getCtlDaysSpent())
                || nonNull(offence.getCtlTimeLimit())
                || nonNull(offence.isCtlExtended())) {
            return new CustodyTimeLimit(offence.getCtlDaysSpent(), offence.isCtlExtended(), offence.getCtlTimeLimit());
        } else {
            return null;
        }
    }

    private BailStatus extractBailStatus(final Offence offence) {
        if (nonNull(offence.getBailStatusId()) || nonNull(offence.getBailStatusCode()) || nonNull(offence.getBailStatusDescription())) {
            return BailStatus.bailStatus()
                    .withId(offence.getBailStatusId())
                    .withCode(offence.getBailStatusCode())
                    .withDescription(offence.getBailStatusDescription())
                    .build();
        } else {
            return null;
        }
    }

}
