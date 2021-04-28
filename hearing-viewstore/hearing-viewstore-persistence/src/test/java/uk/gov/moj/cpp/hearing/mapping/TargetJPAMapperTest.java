package uk.gov.moj.cpp.hearing.mapping;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asList;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.asSet;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;

import uk.gov.justice.services.test.utils.core.random.RandomGenerator;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ResultLine;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TargetJPAMapperTest {

    @Mock
    private ResultLineJPAMapper resultLineJPAMapper;

    @InjectMocks
    private TargetJPAMapper targetJPAMapper;


    @Test
    public void testFromJPA() {

        UUID hearingId = UUID.randomUUID();
        Hearing hearing = mock(Hearing.class);
        final UUID masterDefendantId = randomUUID();
        when(hearing.getId()).thenReturn(hearingId);

        Target target = new Target();
        target.setId(UUID.randomUUID());
        target.setHearing(hearing);
        target.setDefendantId(UUID.randomUUID());
        target.setOffenceId(UUID.randomUUID());
        target.setDraftResult(RandomGenerator.STRING.next());
        target.setResultLines(asSet(mock(ResultLine.class)));

        uk.gov.justice.core.courts.ResultLine resultLineMock = mock(uk.gov.justice.core.courts.ResultLine.class);
        when(resultLineJPAMapper.fromJPA(target.getResultLines())).thenReturn(asList(resultLineMock));

        assertThat(targetJPAMapper.fromJPA(target, masterDefendantId), isBean(uk.gov.justice.core.courts.Target.class)
                .with(uk.gov.justice.core.courts.Target::getTargetId, is(target.getId()))
                .with(uk.gov.justice.core.courts.Target::getDefendantId, is(target.getDefendantId()))
                .with(uk.gov.justice.core.courts.Target::getMasterDefendantId, is(masterDefendantId))
                .with(uk.gov.justice.core.courts.Target::getOffenceId, is(target.getOffenceId()))
                .with(uk.gov.justice.core.courts.Target::getHearingId, is(hearingId))
                .with(uk.gov.justice.core.courts.Target::getDraftResult, is(target.getDraftResult()))
                .with(uk.gov.justice.core.courts.Target::getResultLines, first(is(resultLineMock)))
        );
    }

    @Test
    public void testFromJPAWithHearingDay() {

        UUID hearingId = UUID.randomUUID();
        Hearing hearing = mock(Hearing.class);
        final LocalDate hearingDay = LocalDate.now();
        final UUID masterDefendantId = randomUUID();
        when(hearing.getId()).thenReturn(hearingId);

        Target target = new Target();
        target.setId(UUID.randomUUID());
        target.setHearing(hearing);
        target.setHearingDay(hearingDay.toString());
        target.setDefendantId(UUID.randomUUID());
        target.setOffenceId(UUID.randomUUID());
        target.setDraftResult(RandomGenerator.STRING.next());
        target.setResultLines(asSet(mock(ResultLine.class)));

        uk.gov.justice.core.courts.ResultLine resultLineMock = mock(uk.gov.justice.core.courts.ResultLine.class);
        when(resultLineJPAMapper.fromJPA(target.getResultLines())).thenReturn(asList(resultLineMock));

        assertThat(targetJPAMapper.fromJPA(target, masterDefendantId), isBean(uk.gov.justice.core.courts.Target.class)
                .with(uk.gov.justice.core.courts.Target::getTargetId, is(target.getId()))
                .with(uk.gov.justice.core.courts.Target::getDefendantId, is(target.getDefendantId()))
                .with(uk.gov.justice.core.courts.Target::getMasterDefendantId, is(masterDefendantId))
                .with(uk.gov.justice.core.courts.Target::getOffenceId, is(target.getOffenceId()))
                .with(uk.gov.justice.core.courts.Target::getHearingId, is(hearingId))
                .with(uk.gov.justice.core.courts.Target::getHearingDay, is(hearingDay))
                .with(uk.gov.justice.core.courts.Target::getDraftResult, is(target.getDraftResult()))
                .with(uk.gov.justice.core.courts.Target::getResultLines, first(is(resultLineMock)))
        );
    }

    @Test
    public void testToJPA() {

        Hearing hearing = mock(Hearing.class);

        uk.gov.justice.core.courts.Target target = uk.gov.justice.core.courts.Target.target()
                .withTargetId(UUID.randomUUID())
                .withDefendantId(UUID.randomUUID())
                .withOffenceId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withDraftResult(RandomGenerator.STRING.next())
                .withResultLines(asList(mock(uk.gov.justice.core.courts.ResultLine.class)))
                .build();

        ResultLine resultLineMock = mock(ResultLine.class);
        when(resultLineJPAMapper.toJPA(any(), eq(target.getResultLines()))).thenReturn(asSet(resultLineMock));

        assertThat(targetJPAMapper.toJPA(hearing, target), isBean(Target.class)
                .with(Target::getId, is(target.getTargetId()))
                .with(Target::getDefendantId, is(target.getDefendantId()))
                .with(Target::getOffenceId, is(target.getOffenceId()))
                .with(Target::getHearing, is(hearing))
                .with(Target::getDraftResult, is(target.getDraftResult()))
                .with(Target::getResultLines, first(is(resultLineMock)))
        );
    }

    @Test
    public void testToJPAWithHearingDay() {

        Hearing hearing = mock(Hearing.class);

        final LocalDate hearingDay = LocalDate.now();

        uk.gov.justice.core.courts.Target target = uk.gov.justice.core.courts.Target.target()
                .withTargetId(UUID.randomUUID())
                .withDefendantId(UUID.randomUUID())
                .withOffenceId(UUID.randomUUID())
                .withHearingId(UUID.randomUUID())
                .withHearingDay(hearingDay)
                .withDraftResult(RandomGenerator.STRING.next())
                .withResultLines(asList(mock(uk.gov.justice.core.courts.ResultLine.class)))
                .build();

        ResultLine resultLineMock = mock(ResultLine.class);
        when(resultLineJPAMapper.toJPA(any(), eq(target.getResultLines()))).thenReturn(asSet(resultLineMock));

        assertThat(targetJPAMapper.toJPA(hearing, target), isBean(Target.class)
                .with(Target::getId, is(target.getTargetId()))
                .with(Target::getDefendantId, is(target.getDefendantId()))
                .with(Target::getOffenceId, is(target.getOffenceId()))
                .with(Target::getHearing, is(hearing))
                .with(Target::getHearingDay, is(hearingDay.toString()))
                .with(Target::getDraftResult, is(target.getDraftResult()))
                .with(Target::getResultLines, first(is(resultLineMock)))
        );
    }
}
