package uk.gov.moj.cpp.hearing.mapping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.HearingJPADataTemplate.aNewHearingJPADataTemplate;

import uk.gov.justice.core.courts.ReportingRestriction;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingOffenceReportingRestrictionKey;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Test;

public class ReportingRestrictionJPAMapperTest {

    private final ReportingRestrictionJPAMapper reportingRestrictionJPAMapper = JPACompositeMappers.REPORTING_RESTRICTION_JPA_MAPPER;

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstReportingRestriction(final BeanMatcher<?> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction entity) {
        return ElementAtListMatcher.first(whenReportingRestriction((BeanMatcher<ReportingRestriction>) m, entity));
    }

    @SuppressWarnings("unchecked")
    public static ElementAtListMatcher whenFirstReportingRestriction(final BeanMatcher<?> m, final ReportingRestriction pojo) {
        return ElementAtListMatcher.first(whenReportingRestriction((BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction>) m, pojo));
    }

    public static BeanMatcher<ReportingRestriction> whenReportingRestriction(final BeanMatcher<ReportingRestriction> m, final uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction entity) {

        return m.with(ReportingRestriction::getJudicialResultId, is(entity.getJudicialResultId()))
                .with(ReportingRestriction::getLabel, is(entity.getLabel()))
                .with(ReportingRestriction::getOrderedDate, is(entity.getOrderedDate()))
                .with(ReportingRestriction::getId, is(entity.getId().getId()))
                ;
    }

    public static BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction> whenReportingRestriction(
            final BeanMatcher<uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction> m, final ReportingRestriction pojo) {

        return m.with(uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction::getJudicialResultId, is(pojo.getJudicialResultId()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction::getLabel, is(pojo.getLabel()))
                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction::getOrderedDate, is(pojo.getOrderedDate()))

                .with(uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction::getId, isBean(HearingOffenceReportingRestrictionKey.class)
                        .with(HearingOffenceReportingRestrictionKey::getId, is(pojo.getId())))
                ;
    }

    @Test
    public void testFromJPA() {
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction reportingRestrictionEntity = aNewHearingJPADataTemplate()
                .getHearing()
                .getProsecutionCases().iterator().next()
                .getDefendants().iterator().next()
                .getOffences().iterator().next()
                .getReportingRestrictions().iterator().next();
        assertThat(reportingRestrictionJPAMapper.fromJPA(reportingRestrictionEntity), whenReportingRestriction(isBean(ReportingRestriction.class), reportingRestrictionEntity));
    }

    @Test
    public void testToJPA() {
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.Offence offenceEntity = hearingEntity.getProsecutionCases().iterator().next()
                .getDefendants().iterator().next()
                .getOffences().iterator().next();
        final uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction reportingRestrictionEntity = offenceEntity
                .getReportingRestrictions().iterator().next();
        final ReportingRestriction reportingRestrictionPojo = reportingRestrictionJPAMapper.fromJPA(reportingRestrictionEntity);
        assertThat(reportingRestrictionJPAMapper.toJPA(hearingEntity, offenceEntity.getId().getId(), reportingRestrictionPojo),
                whenReportingRestriction(isBean(uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction.class), reportingRestrictionPojo));
    }

    @Test
    public void shouldRemoveDuplicatesReportingRestrictions() {
        final List<uk.gov.justice.core.courts.ReportingRestriction> reportingRestrictions = new ArrayList<>();
        final UUID reportingRestrictionId1 = UUID.randomUUID();
        reportingRestrictions.add(new uk.gov.justice.core.courts.ReportingRestriction(reportingRestrictionId1, UUID.randomUUID(), "label", LocalDate.now()));
        reportingRestrictions.add(new uk.gov.justice.core.courts.ReportingRestriction(reportingRestrictionId1, UUID.randomUUID(), "label2", LocalDate.now().minusDays(1)));
        reportingRestrictions.add(new uk.gov.justice.core.courts.ReportingRestriction(reportingRestrictionId1, UUID.randomUUID(), "label3", LocalDate.now().plusDays(1)));
        final UUID reportingRestrictionId2 = UUID.randomUUID();
        reportingRestrictions.add(new uk.gov.justice.core.courts.ReportingRestriction(reportingRestrictionId2, UUID.randomUUID(), "label4", LocalDate.now().plusDays(1)));
        final Hearing hearingEntity = aNewHearingJPADataTemplate().getHearing();
        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.ReportingRestriction> reportingRestrictionsSet = reportingRestrictionJPAMapper.toJPA(hearingEntity, UUID.randomUUID(), reportingRestrictions);
        assertThat(reportingRestrictionsSet.size(), is(2));
        assertThat(reportingRestrictionsSet.stream().map(reportingRestriction -> reportingRestriction.getId().getId()).collect(Collectors.toList()), containsInAnyOrder(reportingRestrictionId1, reportingRestrictionId2));
    }
}