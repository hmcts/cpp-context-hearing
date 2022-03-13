package uk.gov.moj.cpp.hearing.mapping;

import static uk.gov.moj.cpp.util.ReportingRestrictionHelper.dedupReportingRestrictions;

import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingOffenceReportingRestrictionKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReportingRestrictionJPAMapper {

    public ReportingRestriction toJPA(final Hearing hearing, final UUID offenceId, final uk.gov.justice.core.courts.ReportingRestriction pojo) {

        if (null == pojo) {
            return null;
        }

        final ReportingRestriction reportingRestriction = new ReportingRestriction();
        reportingRestriction.setId(new HearingOffenceReportingRestrictionKey(pojo.getId(), hearing.getId(), offenceId));
        reportingRestriction.setJudicialResultId(pojo.getJudicialResultId());
        reportingRestriction.setLabel(pojo.getLabel());
        reportingRestriction.setOrderedDate(pojo.getOrderedDate());
        return reportingRestriction;
    }

    public uk.gov.justice.core.courts.ReportingRestriction fromJPA(final ReportingRestriction entity) {
        if (null == entity) {
            return null;
        }
        final UUID reportingRestrictionId = entity.getId().getId();

        return uk.gov.justice.core.courts.ReportingRestriction.reportingRestriction()
                .withId(reportingRestrictionId)
                .withJudicialResultId(entity.getJudicialResultId())
                .withLabel(entity.getLabel())
                .withOrderedDate(entity.getOrderedDate())
                .build();
    }

    public Set<ReportingRestriction> toJPA(final Hearing hearing, final UUID offenceId, final List<uk.gov.justice.core.courts.ReportingRestriction> pojos) {
        if (null == pojos) {
            return new HashSet<>();
        }
        return dedupReportingRestrictions(pojos).stream().map(pojo -> toJPA(hearing, offenceId, pojo)).collect(Collectors.toSet());
    }

    public List<uk.gov.justice.core.courts.ReportingRestriction> fromJPA(final Set<ReportingRestriction> entities) {
        return entities.stream().map(this::fromJPA).collect(Collectors.toList());
    }
}
