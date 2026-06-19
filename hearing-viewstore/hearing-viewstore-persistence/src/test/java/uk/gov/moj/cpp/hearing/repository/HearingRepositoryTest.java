package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.justice.core.courts.HearingDay.hearingDay;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;

import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.application.ApplicationDraftResult;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplication;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingApplicationKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingCaseNote;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HearingRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();
    private static final List<uk.gov.justice.core.courts.Hearing> hearingsWithHearingDay = new ArrayList<>();
    private static final uk.gov.justice.core.courts.Hearing simpleHearing = minimumInitiateHearingTemplate().getHearing();

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    @Mock
    private HearingJPAMapper hearingJPAMapper;

    private HearingRepository hearingRepository;
    private HearingCaseNoteRepository hearingCaseNoteRepository;
    private HearingProsecutionCounselRepository hearingProsecutionCounselRepository;
    private HearingApplicationRepository hearingApplicationRepository;

    @BeforeAll
    static void createTestData() {
        hearings.clear();
        hearingsWithHearingDay.clear();
        final InitiateHearingCommand initiateHearingCommand = minimumInitiateHearingTemplate();
        final InitiateHearingCommand initiateHearingCommandWithHearingDay = minimumInitiateHearingTemplate();
        hearings.add(initiateHearingCommand.getHearing());
        hearingsWithHearingDay.add(initiateHearingCommandWithHearingDay.getHearing());
    }

    @BeforeEach
    void openEntityManagerAndCreateRepositories() {
        hearingRepository = new HearingRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingRepository);

        hearingCaseNoteRepository = new HearingCaseNoteRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingCaseNoteRepository);

        hearingProsecutionCounselRepository = new HearingProsecutionCounselRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingProsecutionCounselRepository);

        hearingApplicationRepository = new HearingApplicationRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingApplicationRepository);

        when(hearingJPAMapper.toJPA(any(uk.gov.justice.core.courts.Hearing.class))).thenAnswer(invocation -> {
            final uk.gov.justice.core.courts.Hearing domainHearing = invocation.getArgument(0);
            return buildHearingEntity(domainHearing);
        });

        hearings.forEach(hearing -> {
            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingEntity.setTargets(Sets.newHashSet(Target.target().setId(new HearingSnapshotKey(randomUUID(), hearingEntity.getId())).setHearing(hearingEntity)));
            hearingEntity.setApplicationDraftResults(Sets.newHashSet(ApplicationDraftResult.applicationDraftResult().setId(randomUUID()).setHearing(hearingEntity)));
            hearingRepository.save(hearingEntity);
        });

        hearingsWithHearingDay.forEach(hearing -> {
            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingEntity.setTargets(Sets.newHashSet(Target.target().setId(new HearingSnapshotKey(randomUUID(), hearingEntity.getId())).setHearing(hearingEntity).setHearingDay("2021-03-01")));
            hearingEntity.setApplicationDraftResults(Sets.newHashSet(ApplicationDraftResult.applicationDraftResult().setId(randomUUID()).setHearing(hearingEntity)));
            hearingRepository.save(hearingEntity);
        });
    }

    @Test
    void shouldRetrieveNonEmptyListWhenFindByFiltersInvokedAndDataPresent() {
        final uk.gov.justice.core.courts.Hearing hearing = hearings.get(0);
        List<Hearing> hearingList = hearingRepository.findByFilters(hearing.getHearingDays().get(0).getSittingDay().toLocalDate(), hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId());
        assertThat(hearingList, hasItem(isBean(Hearing.class).with(Hearing::getId, is(hearing.getId()))));
    }

    @Test
    void shouldExcludeVacatedHearingFromListWhenFindByFiltersInvoked() {
        final uk.gov.justice.core.courts.Hearing vacatedHearing = addHearingWithVacatedStatus(true);
        assertThat(hearingRepository.findByFilters(vacatedHearing.getHearingDays().get(0).getSittingDay().toLocalDate(), vacatedHearing.getCourtCentre().getId(), vacatedHearing.getCourtCentre().getRoomId()), empty());
    }

    @Test
    void shouldRetrieveHearingFromListWhenHearingDayCancelledNullOrFalseAndFindByFiltersInvoked() {
        final uk.gov.justice.core.courts.Hearing hearingWithCancelledFalse = addHearingWithCancelledStatus(false);
        List<Hearing> hearingList = hearingRepository.findByFilters(hearingWithCancelledFalse.getHearingDays().get(0).getSittingDay().toLocalDate(), hearingWithCancelledFalse.getCourtCentre().getId(), hearingWithCancelledFalse.getCourtCentre().getRoomId());
        assertThat(hearingList, hasItem(isBean(Hearing.class).with(Hearing::getId, is(hearingWithCancelledFalse.getId()))));

        final uk.gov.justice.core.courts.Hearing hearingWithCancelledNull = addHearingWithCancelledStatus(null);
        hearingList = hearingRepository.findByFilters(hearingWithCancelledNull.getHearingDays().get(0).getSittingDay().toLocalDate(), hearingWithCancelledNull.getCourtCentre().getId(), hearingWithCancelledNull.getCourtCentre().getRoomId());
        assertThat(hearingList, hasItem(isBean(Hearing.class).with(Hearing::getId, is(hearingWithCancelledNull.getId()))));
    }

    @Test
    void shouldExcludeHearingFromListWhenHearingDayCancelledTrueAndFindByFiltersInvoked() {
        final uk.gov.justice.core.courts.Hearing vacatedHearing = addHearingWithCancelledStatus(true);
        assertThat(hearingRepository.findByFilters(vacatedHearing.getHearingDays().get(0).getSittingDay().toLocalDate(), vacatedHearing.getCourtCentre().getId(), vacatedHearing.getCourtCentre().getRoomId()), empty());
    }

    @Test
    void shouldReturnNonEmptyListWhenFindByUserFiltersInvokedAndDataPresent() {
        final uk.gov.justice.core.courts.Hearing hearing = hearings.get(0);
        List<Hearing> hearingList = hearingRepository.findByUserFilters(hearing.getHearingDays().get(0).getSittingDay().toLocalDate(), hearing.getJudiciary().get(0).getUserId());
        assertThat(hearingList, hasItem(isBean(Hearing.class).with(Hearing::getId, is(hearing.getId()))));
    }

    @Test
    void shouldExcludeVacatedHearingFromListWhenFindByUserFiltersInvoked() {
        final uk.gov.justice.core.courts.Hearing vacatedHearing = addHearingWithVacatedStatus(true);
        assertThat(hearingRepository.findByUserFilters(vacatedHearing.getHearingDays().get(0).getSittingDay().toLocalDate(), vacatedHearing.getJudiciary().get(0).getUserId()), empty());
        final uk.gov.justice.core.courts.Hearing nonVacatedHearing = addHearingWithVacatedStatus(false);
        assertThat(hearingRepository.findByUserFilters(nonVacatedHearing.getHearingDays().get(0).getSittingDay().toLocalDate(), nonVacatedHearing.getJudiciary().get(0).getUserId()),
                hasItem(isBean(Hearing.class).with(Hearing::getId, is(nonVacatedHearing.getId()))));
    }

    @Test
    void shouldReturnNonEmptyListWhenFindHearingsInvokedAndDataPresent() {
        final uk.gov.justice.core.courts.Hearing hearing = hearings.get(0);
        List<Hearing> hearingList = hearingRepository.findHearings(hearing.getHearingDays().get(0).getSittingDay().toLocalDate(), hearing.getCourtCentre().getId());
        assertThat(hearingList, hasItem(isBean(Hearing.class).with(Hearing::getId, is(hearing.getId()))));
        assertThat(hearingList.get(0).getHearingDays(), hasItem(isBean(HearingDay.class).with(HearingDay::getHasSharedResults, is(true))));
    }

    @Test
    void shouldExcludeVacatedHearingFromListWhenVacatedTrueAndFindHearingsInvoked() {
        final uk.gov.justice.core.courts.Hearing vacatedHearing = addHearingWithVacatedStatus(Boolean.TRUE);
        assertThat(hearingRepository.findHearings(vacatedHearing.getHearingDays().get(0).getSittingDay().toLocalDate(), vacatedHearing.getCourtCentre().getId()), empty());
    }

    @Test
    void shouldRetrieveHearingFromListWhenHearingDayCancelledNullOrFalseAndFindHearingsInvoked() {
        final uk.gov.justice.core.courts.Hearing hearingWithCancelledFalse = addHearingWithCancelledStatus(Boolean.FALSE);
        List<Hearing> hearingsList = hearingRepository.findHearings(hearingWithCancelledFalse.getHearingDays().get(0).getSittingDay().toLocalDate(), hearingWithCancelledFalse.getCourtCentre().getId());
        assertThat(hearingsList, hasItem(isBean(Hearing.class).with(Hearing::getId, is(hearingWithCancelledFalse.getId()))));

        final uk.gov.justice.core.courts.Hearing hearingWithCancelledNull = addHearingWithCancelledStatus(null);
        hearingsList = hearingRepository.findHearings(hearingWithCancelledNull.getHearingDays().get(0).getSittingDay().toLocalDate(), hearingWithCancelledNull.getCourtCentre().getId());
        assertThat(hearingsList, hasItem(isBean(Hearing.class).with(Hearing::getId, is(hearingWithCancelledNull.getId()))));
    }

    @Test
    void shouldExcludeHearingFromListWhenHearingDayCancelledTrueAndFindHearingsInvoked() {
        final uk.gov.justice.core.courts.Hearing hearingWithCancelledDays = addHearingWithCancelledStatus(true);
        assertThat(hearingRepository.findHearings(hearingWithCancelledDays.getHearingDays().get(0).getSittingDay().toLocalDate(), hearingWithCancelledDays.getCourtCentre().getId()), empty());
    }

    @Test
    void shouldFindAll() {
        assertEquals(hearings.size() + hearingsWithHearingDay.size(), hearingRepository.findAll().size());
    }

    @Test
    void shouldFindByHearingId() {
        final UUID hearingId = hearings.get(0).getId();
        final Hearing hearingEntityRetrieved = hearingRepository.findBy(hearingId);
        assertThat(hearingEntityRetrieved, is(notNullValue()));
    }

    @Test
    void shouldFindCourtCenterByHearingID() {
        final UUID hearingId = hearings.get(0).getId();
        final CourtCentre courtCenter = hearingRepository.findCourtCenterByHearingId(hearingId);
        assertThat(courtCenter, is(notNullValue()));
    }

    @Test
    void shouldReturnNullWhenHearingIdIsAbsent() {
        final CourtCentre courtCenter = hearingRepository.findCourtCenterByHearingId(randomUUID());
        assertThat(courtCenter, is(nullValue()));
    }

    @Test
    void shouldFindHearingDayByHearingIdAndDate() {
        final uk.gov.justice.core.courts.Hearing hearing = hearings.get(0);
        final UUID hearingId = hearing.getId();
        final LocalDate date = hearing.getHearingDays().get(0).getSittingDay().toLocalDate();

        final HearingDay matched = hearingRepository.findHearingDayByHearingIdAndDate(hearingId, date);

        assertThat(matched, is(notNullValue()));
        assertEquals(date, matched.getDate());
    }

    @Test
    void shouldReturnNullForFindHearingDayByHearingIdAndDate_whenNoDayMatchesDate() {
        final UUID hearingId = hearings.get(0).getId();
        final LocalDate unknownDate = LocalDate.of(1999, 1, 1);

        assertThat(hearingRepository.findHearingDayByHearingIdAndDate(hearingId, unknownDate), is(nullValue()));
    }

    @Test
    void shouldReturnNullForFindHearingDayByHearingIdAndDate_whenHearingIdIsUnknown() {
        final LocalDate anyDate = hearings.get(0).getHearingDays().get(0).getSittingDay().toLocalDate();
        assertThat(hearingRepository.findHearingDayByHearingIdAndDate(randomUUID(), anyDate), is(nullValue()));
    }

    @Test
    void shouldFindTargetsByHearingId() {
        final UUID hearingId = hearings.get(0).getId();
        final List<Target> targets = hearingRepository.findTargetsByHearingId(hearingId);
        assertThat(targets.size(), is(1));
    }

    @Test
    void shouldFindFutureHearing() {
        LocalDate hearingDay = hearings.get(0).getHearingDays().get(0).getSittingDay().toLocalDate().minusDays(1);
        UUID defendantId = hearings.get(0).getProsecutionCases().get(0).getDefendants().get(0).getId();
        final List<Hearing> hearingList = hearingRepository.findByDefendantAndHearingType(hearingDay, defendantId);
        assertThat(hearingList.size(), is(1));
    }

    @Test
    void shouldNotFindFutureHearing() {
        LocalDate hearingDay = hearings.get(0).getHearingDays().get(0).getSittingDay().toLocalDate().plusDays(1);
        UUID defendantId = hearings.get(0).getProsecutionCases().get(0).getDefendants().get(0).getId();
        final List<Hearing> hearingList = hearingRepository.findByDefendantAndHearingType(hearingDay, defendantId);
        assertThat(hearingList.size(), is(0));
    }

    @Test
    void shouldFindTargetsByFilter() {
        final UUID hearingId = hearings.get(0).getId(); //hearing day null
        final String hearingDay = "2021-03-01";
        final List<Target> targets = hearingRepository.findTargetsByFilters(hearingId, hearingDay);
        assertThat(targets.size(), is(1));

        final UUID hearingId2 = hearingsWithHearingDay.get(0).getId(); //hearing day not null
        final String hearingDay2 = "2021-03-01";
        final List<Target> targetsWithHearingDay = hearingRepository.findTargetsByFilters(hearingId2, hearingDay2);
        assertThat(targetsWithHearingDay.size(), is(1));
    }

    @Test
    void shouldNotFindTargetsByFilter() {
        final UUID hearingId = hearingsWithHearingDay.get(0).getId();
        final String hearingDay = "2021-03-02"; //hearing day exists but do not match
        final List<Target> targets = hearingRepository.findTargetsByFilters(hearingId, hearingDay);
        assertThat(targets.size(), is(0));
    }

    @Test
    void shouldReturnEmptyTargetsIfHearingDoNotHaveTargets() {
        final List<Target> targets = hearingRepository.findTargetsByHearingId(simpleHearing.getId());
        assertThat(targets.size(), is(0));
    }

    @Test
    void shouldFindApplicationDraftResultsByHearingId() {
        final UUID hearingId = hearings.get(0).getId();
        final List<ApplicationDraftResult> applicationDraftResults = hearingRepository.findApplicationDraftResultsByHearingId(hearingId);
        assertThat(applicationDraftResults.size(), is(1));
    }

    @Test
    void shouldReturnEmptyApplicationDraftResultsIfHearingDoNotHaveApplicationDraftResults() {
        final List<ApplicationDraftResult> applicationDraftResults = hearingRepository.findApplicationDraftResultsByHearingId(simpleHearing.getId());
        assertThat(applicationDraftResults.size(), is(0));
    }

    @Test
    void shouldFindProsecutionCasesByHearingId() {
        final UUID hearingId = hearings.get(0).getId();
        final List<ProsecutionCase> prosecutionCases = hearingRepository.findProsecutionCasesByHearingId(hearingId);
        assertThat(prosecutionCases.size(), is(1));
    }

    @Test
    void shouldReturnEmptyProsecutionCasesIfHearingDoNotHaveTargets() {
        final List<ProsecutionCase> prosecutionCases =
                hearingRepository.findProsecutionCasesByHearingId(simpleHearing.getId());
        assertThat(prosecutionCases.size(), is(0));
    }

    @Test
    @Disabled("because of issues with hearing case note jsonb column")
    void shouldFindAssociatedCaseNote() {
        final UUID hearingId = hearings.get(0).getId();
        final Hearing hearing = hearingRepository.findBy(hearingId);

        final HearingCaseNote hearingCaseNote = new HearingCaseNote();
        hearingCaseNote.setHearing(hearing);
        hearingCaseNote.setId(new HearingSnapshotKey(randomUUID(), hearing.getId()));
        hearingCaseNoteRepository.save(hearingCaseNote);

        final Hearing hearingEntityRetrieved = hearingRepository.findBy(hearingId);

        assertThat(hearingEntityRetrieved.getHearingCaseNotes(), is(notNullValue()));
        assertThat(hearingEntityRetrieved.getHearingCaseNotes().size(), is(1));
    }

    @Test
    @Disabled("because of issues with hearing case note jsonb column")
    void shouldNotFindByHearingId() {
        assertThat(hearingRepository.findBy(randomUUID()), is(nullValue()));
    }

    @Test
    void shouldFindHearingsByCaseId() {
        final UUID caseId = hearings.get(0).getProsecutionCases().get(0).getId();

        final List<Hearing> hearingEntityRetrieved = hearingRepository.findByCaseId(caseId);

        assertThat(hearingEntityRetrieved, is(notNullValue()));

        final Hearing hearing = hearingEntityRetrieved.get(0);
        assertThat(hearing.getId(), is(hearings.get(0).getId()));
        final HearingDay hearingDay = hearing.getHearingDays().iterator().next();
        assertThat(hearingDay.getSittingDay(), is(hearings.get(0).getHearingDays().get(0).getSittingDay()));
        assertThat(hearingDay.getListedDurationMinutes(), is(hearings.get(0).getHearingDays().get(0).getListedDurationMinutes()));
        assertThat(hearingDay.getListingSequence(), is(hearings.get(0).getHearingDays().get(0).getListingSequence()));
    }

    @Test
    void shouldFindHearingsByCaseIdAndJurisdictionType() {
        final UUID caseId = hearings.get(0).getProsecutionCases().get(0).getId();
        final JurisdictionType jurisdictionType = hearings.get(0).getJurisdictionType();

        final List<Hearing> hearingEntityRetrieved = hearingRepository.findByCaseIdAndJurisdictionType(caseId, jurisdictionType);

        assertThat(hearingEntityRetrieved, is(notNullValue()));

        final Hearing hearing = hearingEntityRetrieved.get(0);
        assertThat(hearing.getId(), is(hearings.get(0).getId()));
    }

    @Test
    void shouldFindAllHearingsByApplicationId() {
        final UUID applicationId = hearings.get(0).getCourtApplications().get(0).getId();
        final Hearing hearingSaved = hearingRepository.findBy(hearings.get(0).getId());
        saveHearingApplication(hearingSaved, applicationId);

        final List<Hearing> hearingEntityRetrieved = hearingRepository.findAllHearingsByApplicationId(applicationId);
        assertThat(hearingEntityRetrieved, is(notNullValue()));
        final Hearing hearing = hearingEntityRetrieved.get(0);
        assertThat(hearing.getId(), is(hearings.get(0).getId()));
        final HearingDay hearingDay = hearing.getHearingDays().iterator().next();
        assertThat(hearingDay.getSittingDay(), is(hearings.get(0).getHearingDays().get(0).getSittingDay()));
        assertThat(hearingDay.getListedDurationMinutes(), is(hearings.get(0).getHearingDays().get(0).getListedDurationMinutes()));
        assertThat(hearingDay.getListingSequence(), is(hearings.get(0).getHearingDays().get(0).getListingSequence()));
    }

    @Test
    void shouldFindAllHearingsByApplicationIdAndJurisdictionType() {
        final UUID applicationId = hearings.get(0).getCourtApplications().get(0).getId();
        final Hearing hearingSaved = hearingRepository.findBy(hearings.get(0).getId());
        final JurisdictionType jurisdictionType = hearingSaved.getJurisdictionType();
        saveHearingApplication(hearingSaved, applicationId);

        final List<Hearing> hearingEntityRetrieved = hearingRepository.findAllHearingsByApplicationIdAndJurisdictionType(applicationId, jurisdictionType);
        assertThat(hearingEntityRetrieved, is(notNullValue()));
        final Hearing hearing = hearingEntityRetrieved.get(0);
        assertThat(hearing.getId(), is(hearings.get(0).getId()));
    }

    @Test
    void shouldFindHearingsByHearingIdIdAndJurisdictionType() {
        final UUID hearingId = hearings.get(0).getId();
        final JurisdictionType jurisdictionType = hearings.get(0).getJurisdictionType();

        final Hearing hearingEntityRetrieved = hearingRepository.findByHearingIdAndJurisdictionType(hearingId, jurisdictionType);

        assertThat(hearingEntityRetrieved, is(notNullValue()));
        assertThat(hearingEntityRetrieved.getId(), is(hearings.get(0).getId()));
    }

    @Test
    void shouldRemoveTargetsFromHearing() {
        final UUID firstHearingId = hearings.get(0).getId();
        final Hearing firstHearing = hearingRepository.findBy(firstHearingId);
        final UUID firstTargetId = firstHearing.getTargets().stream().findFirst().get().getId().getId();

        firstHearing.getTargets().removeIf(t -> t.getId().equals(firstTargetId));

        hearingRepository.save(firstHearing);

        final Hearing firstHearingPostTargetRemoval = hearingRepository.findBy(firstHearingId);
        assertThat(firstHearingPostTargetRemoval.getTargets().stream().noneMatch(t -> t.getId().equals(firstTargetId)), is(true));
    }

    @Test
    void findByHearingDate() {
        final uk.gov.justice.core.courts.Hearing hearing1 = addSampleHearing(false,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(1).plusMinutes(15));
        final uk.gov.justice.core.courts.Hearing hearing2 = addSampleHearing(false,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(1).plusMinutes(5),
                ZonedDateTime.now().plusDays(2).plusMinutes(5));
        addSampleHearing(false,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(2).plusMinutes(5));
        addSampleHearing(true,
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(1).plusMinutes(5));

        final LocalDate localDate = LocalDate.now().plusDays(1);

        final List<Hearing> byHearingDate = hearingRepository.findByHearingDate(localDate);

        assertThat(byHearingDate.size(), is(2));
        assertThat(byHearingDate, containsInAnyOrder(
                isBean(Hearing.class).with(Hearing::getId, is(hearing1.getId())),
                isBean(Hearing.class).with(Hearing::getId, is(hearing2.getId()))
        ));
    }

    @Test
    void shouldFindHearingsByCaseIdsLaterThan() {
        final List<UUID> caseIdList = new ArrayList<>();
        caseIdList.add(hearingsWithHearingDay.get(0).getProsecutionCases().get(0).getId());
        final List<Hearing> prosecutionCases = hearingRepository.findHearingsByCaseIdsLaterThan(caseIdList, new UtcClock().now().toLocalDate().minusYears(10));
        assertThat(prosecutionCases.size(), is(1));
    }

    public uk.gov.justice.core.courts.Hearing addSampleHearing(final Boolean isBoxHearing, final ZonedDateTime... sittingDays) {
        final InitiateHearingCommand initiateHearingCommand;
        initiateHearingCommand = minimumInitiateHearingTemplate();
        for (final ZonedDateTime sittingDay : sittingDays) {
            initiateHearingCommand.getHearing().getHearingDays().add(hearingDay()
                    .withListedDurationMinutes(15)
                    .withHasSharedResults(true)
                    .withSittingDay(sittingDay)
                    .withListingSequence(2)
                    .build());
        }
        initiateHearingCommand.getHearing().setIsBoxHearing(isBoxHearing);

        saveHearing(initiateHearingCommand);
        return initiateHearingCommand.getHearing();
    }

    public uk.gov.justice.core.courts.Hearing addHearingWithVacatedStatus(final Boolean vacated) {
        final InitiateHearingCommand initiateHearingCommand;
        initiateHearingCommand = minimumInitiateHearingTemplate();
        initiateHearingCommand.getHearing().setIsVacatedTrial(vacated);
        saveHearing(initiateHearingCommand);
        return initiateHearingCommand.getHearing();
    }

    public uk.gov.justice.core.courts.Hearing addHearingWithCancelledStatus(final Boolean cancelled) {
        final InitiateHearingCommand initiateHearingCommand;
        initiateHearingCommand = minimumInitiateHearingTemplate();
        final List<uk.gov.justice.core.courts.HearingDay> hearingDays = initiateHearingCommand.getHearing().getHearingDays();
        for (final uk.gov.justice.core.courts.HearingDay hearingDay : hearingDays) {
            hearingDay.setIsCancelled(cancelled);
        }
        initiateHearingCommand.getHearing().setHearingDays(hearingDays);
        saveHearing(initiateHearingCommand);
        return initiateHearingCommand.getHearing();
    }

    public void saveHearing(final InitiateHearingCommand initiateHearingCommand) {
        final Hearing hearingEntity = hearingJPAMapper.toJPA(initiateHearingCommand.getHearing());
        // because h2 incorrectly maps column type TEXT to VARCHAR(255)
        hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
        hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
        hearingRepository.save(hearingEntity);
    }

    private void saveHearingApplication(final Hearing hearing, final UUID applicationId) {
        final UUID hearingId = hearing.getId();

        final HearingApplication hearingApplication = new HearingApplication();
        final HearingApplicationKey hearingApplicationKey = new HearingApplicationKey(applicationId, hearingId);
        hearingApplication.setId(hearingApplicationKey);
        hearingApplication.setHearing(hearing);
        hearingApplicationRepository.save(hearingApplication);
    }

    /**
     * Builds a JPA Hearing entity from a domain Hearing POJO, mapping the fields needed
     * by all repository queries exercised in this test class.
     */
    private static Hearing buildHearingEntity(final uk.gov.justice.core.courts.Hearing domainHearing) {
        final Hearing hearing = new Hearing();
        hearing.setId(domainHearing.getId());
        hearing.setJurisdictionType(domainHearing.getJurisdictionType());
        hearing.setIsVacatedTrial(domainHearing.getIsVacatedTrial());
        hearing.setIsBoxHearing(domainHearing.getIsBoxHearing());
        hearing.setHasSharedResults(domainHearing.getHasSharedResults());
        hearing.setHearingApplications(new HashSet<>());

        // Court centre
        if (domainHearing.getCourtCentre() != null) {
            final CourtCentre courtCentre = new CourtCentre();
            courtCentre.setId(domainHearing.getCourtCentre().getId());
            courtCentre.setRoomId(domainHearing.getCourtCentre().getRoomId());
            hearing.setCourtCentre(courtCentre);
        }

        // court_applications_json must be non-null and long enough for substring(0, 255)
        hearing.setCourtApplicationsJson("[]" + " ".repeat(300));

        // Hearing days
        final HashSet<HearingDay> hearingDays = new HashSet<>();
        for (final uk.gov.justice.core.courts.HearingDay domainDay : domainHearing.getHearingDays()) {
            final HearingDay hearingDay = new HearingDay();
            hearingDay.setId(new HearingSnapshotKey(randomUUID(), domainHearing.getId()));
            hearingDay.setHearing(hearing);
            hearingDay.setSittingDay(domainDay.getSittingDay());
            hearingDay.setDate(domainDay.getSittingDay() != null ? domainDay.getSittingDay().toLocalDate() : null);
            hearingDay.setListedDurationMinutes(domainDay.getListedDurationMinutes());
            hearingDay.setListingSequence(domainDay.getListingSequence());
            hearingDay.setHasSharedResults(domainDay.getHasSharedResults());
            hearingDay.setIsCancelled(domainDay.getIsCancelled());
            if (domainHearing.getCourtCentre() != null) {
                hearingDay.setCourtCentreId(domainHearing.getCourtCentre().getId());
                hearingDay.setCourtRoomId(domainHearing.getCourtCentre().getRoomId());
            }
            hearingDays.add(hearingDay);
        }
        hearing.setHearingDays(hearingDays);

        // Prosecution cases with defendants
        final HashSet<ProsecutionCase> prosecutionCases = new HashSet<>();
        for (final uk.gov.justice.core.courts.ProsecutionCase domainCase : domainHearing.getProsecutionCases()) {
            final ProsecutionCase prosecutionCase = new ProsecutionCase();
            prosecutionCase.setId(new HearingSnapshotKey(domainCase.getId(), domainHearing.getId()));
            prosecutionCase.setHearing(hearing);

            final HashSet<Defendant> defendants = new HashSet<>();
            for (final uk.gov.justice.core.courts.Defendant domainDefendant : domainCase.getDefendants()) {
                final Defendant defendant = new Defendant();
                defendant.setId(new HearingSnapshotKey(domainDefendant.getId(), domainHearing.getId()));
                defendant.setProsecutionCase(prosecutionCase);
                defendant.setProsecutionCaseId(domainCase.getId());
                defendants.add(defendant);
            }
            prosecutionCase.setDefendants(defendants);
            prosecutionCases.add(prosecutionCase);
        }
        hearing.setProsecutionCases(prosecutionCases);

        // Judicial roles
        final HashSet<JudicialRole> judicialRoles = new HashSet<>();
        for (final uk.gov.justice.core.courts.JudicialRole domainRole : domainHearing.getJudiciary()) {
            final JudicialRole judicialRole = new JudicialRole();
            judicialRole.setId(new HearingSnapshotKey(randomUUID(), domainHearing.getId()));
            judicialRole.setHearing(hearing);
            judicialRole.setUserId(domainRole.getUserId());
            judicialRoles.add(judicialRole);
        }
        hearing.setJudicialRoles(judicialRoles);

        return hearing;
    }
}
