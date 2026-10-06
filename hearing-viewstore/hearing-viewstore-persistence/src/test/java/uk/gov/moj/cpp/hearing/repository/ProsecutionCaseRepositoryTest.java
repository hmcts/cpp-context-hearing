package uk.gov.moj.cpp.hearing.repository;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.core.courts.HearingDay.hearingDay;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * DB integration tests for {@link ProsecutionCaseRepository#findByCaseIds} (CAD-1609).
 */
@RunWith(CdiTestRunner.class)
public class ProsecutionCaseRepositoryTest {

    private final List<UUID> savedHearingIds = new ArrayList<>();

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private ProsecutionCaseRepository prosecutionCaseRepository;

    @Inject
    private HearingJPAMapper hearingJPAMapper;

    @After
    public void teardown() {
        savedHearingIds.forEach(id -> hearingRepository.attachAndRemove(hearingRepository.findBy(id)));
    }

    @Test
    public void shouldFindCaseRegardlessOfHearingId() {
        final UUID caseId = randomUUID();
        final UUID hearingId = saveHearingWithCase(caseId, ZonedDateTime.now());

        final List<ProsecutionCase> result = prosecutionCaseRepository.findByCaseIds(singletonList(caseId));

        assertThat(result.size(), is(1));
        assertThat(result.get(0).getId().getId(), is(caseId));
        assertThat(result.get(0).getId().getHearingId(), is(hearingId));
    }

    @Test
    public void shouldReturnEmptyWhenCaseIsUnknown() {
        saveHearingWithCase(randomUUID(), ZonedDateTime.now());

        assertThat(prosecutionCaseRepository.findByCaseIds(singletonList(randomUUID())).isEmpty(), is(true));
    }

    @Test
    public void shouldFindMultipleCasesById() {
        final UUID caseId1 = randomUUID();
        final UUID caseId2 = randomUUID();
        saveHearingWithCase(caseId1, ZonedDateTime.now());
        saveHearingWithCase(caseId2, ZonedDateTime.now());
        saveHearingWithCase(randomUUID(), ZonedDateTime.now());

        final List<ProsecutionCase> result = prosecutionCaseRepository.findByCaseIds(asList(caseId1, caseId2));

        assertThat(result.size(), is(2));
    }

    @Test
    public void shouldReturnRowFromMostRecentHearingFirstWhenCaseIsInSeveralHearings() {
        final UUID caseId = randomUUID();
        final ZonedDateTime now = ZonedDateTime.now();
        saveHearingWithCase(caseId, now.minusDays(10));
        final UUID latestHearingId = saveHearingWithCase(caseId, now.plusDays(5));
        saveHearingWithCase(caseId, now.minusDays(2));

        final List<ProsecutionCase> result = prosecutionCaseRepository.findByCaseIds(singletonList(caseId));

        assertThat(result.size(), is(3));
        assertThat(result.get(0).getId().getHearingId(), is(latestHearingId));
    }

    private UUID saveHearingWithCase(final UUID caseId, final ZonedDateTime sittingDay) {
        final InitiateHearingCommand command = minimumInitiateHearingTemplate();
        final uk.gov.justice.core.courts.Hearing hearing = command.getHearing();
        hearing.getProsecutionCases().get(0).setId(caseId);
        hearing.getProsecutionCases().get(0).getDefendants().forEach(d -> d.setProsecutionCaseId(caseId));
        hearing.setHearingDays(new ArrayList<>(singletonList(hearingDay()
                .withListedDurationMinutes(15)
                .withListingSequence(1)
                .withSittingDay(sittingDay)
                .build())));

        final Hearing entity = hearingJPAMapper.toJPA(hearing);
        // because h2 incorrectly maps column type TEXT to VARCHAR(255)
        entity.setCourtApplicationsJson(entity.getCourtApplicationsJson().substring(0, 255));
        entity.getProsecutionCases().iterator().next().setMarkers(null);
        hearingRepository.save(entity);
        savedHearingIds.add(hearing.getId());
        return hearing.getId();
    }
}
