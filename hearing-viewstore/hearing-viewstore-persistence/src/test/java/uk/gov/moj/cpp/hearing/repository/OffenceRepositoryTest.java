package uk.gov.moj.cpp.hearing.repository;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import org.junit.After;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil;
import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.AllocationDecisionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ApprovalRequestedJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.AssociatedDefenceOrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.AssociatedPersonJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CaseMarkerJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.mapping.CourtCentreJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CpsProsecutorJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CustodialEstablishmentJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefenceOrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefendantAttendanceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefendantJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefendantReferralReasonJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.EthnicityJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingApplicantCounselJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingCaseNoteJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingCompanyRepresentativeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingDefenceCounselJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingInterpreterIntermediaryJPAMapper;
import uk.gov.moj.cpp.hearing.domain.OffenceBailStatus;
import uk.gov.moj.cpp.hearing.mapping.HearingJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingProsecutionCounselJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingRespondentCounselJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.HearingTypeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.IndicatedPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.JudicialRoleJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.LaaApplnReferenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.LesserOrAlternativeOffenceForPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.LesserOrAlternativeOffenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.NotifiedPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OffenceFactsJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OffenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.OrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PersonDefendantJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PersonJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseIdentifierJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ReportingRestrictionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.VerdictTypeJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ApplicationCourtListRestrictionMapper;
import uk.gov.moj.cpp.hearing.mapping.CourtIndicatedSentenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DelegatedPowersJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.JurorsJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.AddressJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ContactNumberJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OffenceRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";
    private static final String GUILTY = "GUILTY";
    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private final List<Offence> offences = new ArrayList<>();

    private static final ZonedDateTime EARLIER_DAY = ZonedDateTime.of(2026, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
    private static final ZonedDateTime LATER_DAY = ZonedDateTime.of(2026, 2, 1, 10, 0, 0, 0, ZoneId.of("UTC"));

    private final List<UUID> bailStatusTestHearingIds = new ArrayList<>();

    private HearingRepository hearingRepository;
    private OffenceRepository offenceRepository;
    private HearingJPAMapper hearingJPAMapper;

    @BeforeAll
    static void createTestData() {
        final InitiateHearingCommand initiateHearingCommand = with(initiateHearingTemplateForMagistrates(), i -> {
            i.getHearing().getProsecutionCases().stream()
                    .flatMap(p -> p.getDefendants().stream())
                    .flatMap(d -> d.getOffences().stream())
                    .forEach(o -> o.setPlea(CoreTestTemplates.plea(o.getId(), o.getConvictionDate(), GUILTY, null).build()));
        });

        hearings.add(initiateHearingCommand.getHearing());
    }

    @BeforeEach
    void openEntityManagerAndCreateRepositories() {
        offences.clear();

        hearingRepository = new HearingRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingRepository);

        offenceRepository = new OffenceRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(offenceRepository);

        hearingJPAMapper = buildHearingJPAMapper();

        hearings.forEach(hearing -> {
            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);

            hearingEntity.getProsecutionCases().forEach(prosecutionCase ->
                    prosecutionCase.getDefendants().forEach(defendant ->
                            defendant.getOffences().forEach(offences::add)));

            // because h2 incorrectly maps column type TEXT to VARCHAR(255)
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingRepository.save(hearingEntity);
        });
    }

    @After
    public void teardown() {
        hearings.forEach(hearing -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearing.getId())));

        bailStatusTestHearingIds.forEach(hearingId -> hearingRepository.attachAndRemove(hearingRepository.findBy(hearingId)));
        bailStatusTestHearingIds.clear();
    }

    @Test
    void shouldFindAll() {
        assertThat(offenceRepository.findAll().size(), is(offences.size()));
    }

    @Test
    void shouldFindByOffenceIdOriginHearingId() {
        final List<Offence> offenceList = offenceRepository.findByOffenceIdAndOriginatingHearingId(
                offences.get(0).getId().getId(),
                hearings.get(0).getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getPlea().getOriginatingHearingId());

        assertThat(offenceList.get(0).getId().getId(), is(offences.get(0).getId().getId()));
        assertThat(offenceList.get(0).getId().getHearingId(), is(hearings.get(0).getId()));
    }

    private static HearingJPAMapper buildHearingJPAMapper() {
        final ObjectMapperProducer objectMapperProducer = new ObjectMapperProducer();

        final ObjectToJsonObjectConverter objectToJsonObjectConverter = new ObjectToJsonObjectConverter();
        ReflectionUtil.setField(objectToJsonObjectConverter, "mapper", objectMapperProducer.objectMapper());

        final JsonObjectToObjectConverter jsonObjectToObjectConverter = new JsonObjectToObjectConverter();
        ReflectionUtil.setField(jsonObjectToObjectConverter, "objectMapper", objectMapperProducer.objectMapper());

        final CourtApplicationsSerializer courtApplicationsSerializer = new CourtApplicationsSerializer();
        ReflectionUtil.setField(courtApplicationsSerializer, "objectToJsonObjectConverter", objectToJsonObjectConverter);
        ReflectionUtil.setField(courtApplicationsSerializer, "jsonObjectToObjectConverter", jsonObjectToObjectConverter);

        final IndicatedPleaJPAMapper indicatedPleaJPAMapper = new IndicatedPleaJPAMapper();
        final CourtIndicatedSentenceJPAMapper courtIndicatedSentenceJPAMapper = new CourtIndicatedSentenceJPAMapper();
        final AllocationDecisionJPAMapper allocationDecisionJPAMapper = new AllocationDecisionJPAMapper(courtIndicatedSentenceJPAMapper);
        final LaaApplnReferenceJPAMapper laaApplnReferenceJPAMapper = new LaaApplnReferenceJPAMapper();
        final PleaJPAMapper pleaJPAMapper = new PleaJPAMapper(new DelegatedPowersJPAMapper(), new LesserOrAlternativeOffenceForPleaJPAMapper());
        final VerdictJPAMapper verdictJPAMapper = new VerdictJPAMapper(new JurorsJPAMapper(), new LesserOrAlternativeOffenceJPAMapper(), new VerdictTypeJPAMapper());
        final ReportingRestrictionJPAMapper reportingRestrictionJPAMapper = new ReportingRestrictionJPAMapper();
        final OffenceJPAMapper offenceJPAMapper = new OffenceJPAMapper(new NotifiedPleaJPAMapper(),
                indicatedPleaJPAMapper, pleaJPAMapper, new OffenceFactsJPAMapper(), verdictJPAMapper,
                allocationDecisionJPAMapper, laaApplnReferenceJPAMapper, reportingRestrictionJPAMapper);

        final OrganisationJPAMapper organisationJPAMapper = new OrganisationJPAMapper(new AddressJPAMapper(), new ContactNumberJPAMapper());
        final PersonJPAMapper personJPAMapper = new PersonJPAMapper(new AddressJPAMapper(), new ContactNumberJPAMapper(), new EthnicityJPAMapper());
        final AssociatedPersonJPAMapper associatedPersonJPAMapper = new AssociatedPersonJPAMapper(personJPAMapper);
        final PersonDefendantJPAMapper personDefendantJPAMapper = new PersonDefendantJPAMapper(organisationJPAMapper, personJPAMapper, new CustodialEstablishmentJPAMapper());
        final DefenceOrganisationJPAMapper defenceOrganisationJPAMapper = new DefenceOrganisationJPAMapper(new AddressJPAMapper(), new ContactNumberJPAMapper());
        final AssociatedDefenceOrganisationJPAMapper associatedDefenceOrganisationJPAMapper = new AssociatedDefenceOrganisationJPAMapper(defenceOrganisationJPAMapper);
        final DefendantJPAMapper defendantJPAMapper = new DefendantJPAMapper(associatedPersonJPAMapper, organisationJPAMapper, offenceJPAMapper, personDefendantJPAMapper, associatedDefenceOrganisationJPAMapper);
        final ProsecutionCaseJPAMapper prosecutionCaseJPAMapper = new ProsecutionCaseJPAMapper(new ProsecutionCaseIdentifierJPAMapper(), defendantJPAMapper, new CaseMarkerJPAMapper(), new CpsProsecutorJPAMapper());

        final HearingYouthCourtDefendantsRepository hearingYouthCourtDefendantsRepository = mock(HearingYouthCourtDefendantsRepository.class);
        final HearingApplicationRepository hearingApplicationRepository = mock(HearingApplicationRepository.class);
        final ApplicationCourtListRestrictionMapper applicationCourtListRestrictionMapper = mock(ApplicationCourtListRestrictionMapper.class);

        return new HearingJPAMapper(
                new CourtCentreJPAMapper(),
                new HearingDefenceCounselJPAMapper(),
                new DefendantAttendanceJPAMapper(),
                new DefendantReferralReasonJPAMapper(),
                new HearingCaseNoteJPAMapper(),
                new HearingDayJPAMapper(),
                new JudicialRoleJPAMapper(),
                prosecutionCaseJPAMapper,
                new HearingProsecutionCounselJPAMapper(),
                new HearingTypeJPAMapper(),
                courtApplicationsSerializer,
                new HearingRespondentCounselJPAMapper(),
                new HearingApplicantCounselJPAMapper(),
                new HearingInterpreterIntermediaryJPAMapper(),
                new HearingCompanyRepresentativeJPAMapper(),
                new ApprovalRequestedJPAMapper(),
                hearingYouthCourtDefendantsRepository,
                applicationCourtListRestrictionMapper,
                hearingApplicationRepository);
    }

    // ── OffenceRepository#offenceBailStatuses: covers hearing.offence-bail-status-for-defendant.
    // Exercised against a real (H2) datasource so that SQL-level bugs (wrong column aliasing,
    // JDBC type mapping, dialect-specific syntax) are caught rather than mocked away. ──────────

    @Test
    public void shouldReturnOffenceOwnBailStatusWhenPresent() {
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        saveHearingWithDefendantAndOffence(defendantId, offenceId, LATER_DAY, true, false, "C", "Remanded into Custody", "U", "Unconditional Bail");

        final List<OffenceBailStatus> result = offenceRepository.offenceBailStatuses(defendantId);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getOffenceId(), is(offenceId));
        assertThat(result.get(0).getBailStatusCode(), is("C"));
        assertThat(result.get(0).getBailStatusDesc(), is("Remanded into Custody"));
    }

    @Test
    public void shouldFallBackToDefendantBailStatusWhenOffenceHasNone() {
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        saveHearingWithDefendantAndOffence(defendantId, offenceId, LATER_DAY, true, false, null, null, "U", "Unconditional Bail");

        final List<OffenceBailStatus> result = offenceRepository.offenceBailStatuses(defendantId);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getOffenceId(), is(offenceId));
        assertThat(result.get(0).getBailStatusCode(), is("U"));
        assertThat(result.get(0).getBailStatusDesc(), is("Unconditional Bail"));
    }

    @Test
    public void shouldReturnNoBailStatusFieldsWhenNeitherOffenceNorDefendantHasOne() {
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        saveHearingWithDefendantAndOffence(defendantId, offenceId, LATER_DAY, true, false, null, null, null, null);

        final List<OffenceBailStatus> result = offenceRepository.offenceBailStatuses(defendantId);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getOffenceId(), is(offenceId));
        assertThat(result.get(0).getBailStatusCode(), is(nullValue()));
        assertThat(result.get(0).getBailStatusDesc(), is(nullValue()));
    }

    @Test
    public void shouldExcludeOffencesWithConcludedProceedings() {
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        saveHearingWithDefendantAndOffence(defendantId, offenceId, LATER_DAY, true, true, "C", "Remanded into Custody", null, null);

        final List<OffenceBailStatus> result = offenceRepository.offenceBailStatuses(defendantId);

        assertThat(result, empty());
    }

    @Test
    public void shouldReturnEmptyListWhenDefendantHasNoOffences() {
        final List<OffenceBailStatus> result = offenceRepository.offenceBailStatuses(randomUUID());

        assertThat(result, empty());
    }

    @Test
    public void shouldReturnOnlyOffenceFromLatestSharedHearingDayAcrossMultipleHearings() {
        final UUID defendantId = randomUUID();
        final UUID offenceId = randomUUID();

        saveHearingWithDefendantAndOffence(defendantId, offenceId, EARLIER_DAY, true, false, "C", "Remanded into Custody", null, null);
        saveHearingWithDefendantAndOffence(defendantId, offenceId, LATER_DAY, true, false, "U", "Unconditional Bail", null, null);

        final List<OffenceBailStatus> result = offenceRepository.offenceBailStatuses(defendantId);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getBailStatusCode(), is("U"));
        assertThat(result.get(0).getBailStatusDesc(), is("Unconditional Bail"));
    }

    private void saveHearingWithDefendantAndOffence(final UUID defendantId, final UUID offenceId, final ZonedDateTime sittingDay,
                                                      final boolean hasSharedResults, final boolean proceedingsConcluded,
                                                      final String offenceBailCode, final String offenceBailDesc,
                                                      final String defendantBailCode, final String defendantBailDesc) {

        final InitiateHearingCommand initiateHearingCommand = initiateHearingTemplateForMagistrates();
        final Hearing hearingEntity = hearingJPAMapper.toJPA(initiateHearingCommand.getHearing());

        // h2 incorrectly maps column type TEXT to VARCHAR(255)
        hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0,
                Math.min(255, hearingEntity.getCourtApplicationsJson().length())));
        hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);

        final UUID hearingId = hearingEntity.getId();

        final Defendant defendant = hearingEntity.getProsecutionCases().iterator().next().getDefendants().iterator().next();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));
        // explicitly clear the template's default bail status so tests control it precisely
        defendant.getPersonDefendant().setBailStatusId(defendantBailCode == null ? null : randomUUID());
        defendant.getPersonDefendant().setBailStatusCode(defendantBailCode);
        defendant.getPersonDefendant().setBailStatusDesc(defendantBailDesc);

        final Offence offence = defendant.getOffences().iterator().next();
        offence.getReportingRestrictions().clear();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));
        offence.setDefendantId(defendantId);
        offence.setProceedingsConcluded(proceedingsConcluded);
        // explicitly clear the template's default bail status so tests control it precisely
        offence.setBailStatusId(offenceBailCode == null ? null : randomUUID());
        offence.setBailStatusCode(offenceBailCode);
        offence.setBailStatusDescription(offenceBailDesc);

        for (final HearingDay hearingDay : hearingEntity.getHearingDays()) {
            hearingDay.setSittingDay(sittingDay);
            hearingDay.setHasSharedResults(hasSharedResults);
        }

        hearingRepository.save(hearingEntity);
        bailStatusTestHearingIds.add(hearingId);
    }
}
