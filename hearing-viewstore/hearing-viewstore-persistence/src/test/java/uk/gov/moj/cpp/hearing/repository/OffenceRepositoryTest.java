package uk.gov.moj.cpp.hearing.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

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
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.util.ArrayList;
import java.util.List;

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
}
