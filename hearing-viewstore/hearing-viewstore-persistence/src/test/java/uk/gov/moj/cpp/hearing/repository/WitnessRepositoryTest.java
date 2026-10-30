package uk.gov.moj.cpp.hearing.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.initiateHearingTemplateForMagistrates;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;

import uk.gov.justice.services.test.utils.persistence.HibernateTestEntityManagerProvider;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.mapping.AddressJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.AllocationDecisionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ApplicationCourtListRestrictionMapper;
import uk.gov.moj.cpp.hearing.mapping.ApprovalRequestedJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.AssociatedDefenceOrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.AssociatedPersonJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CaseMarkerJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.ContactNumberJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CourtApplicationsSerializer;
import uk.gov.moj.cpp.hearing.mapping.CourtCentreJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CourtIndicatedSentenceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CpsProsecutorJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.CustodialEstablishmentJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefenceOrganisationJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefendantAttendanceJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefendantJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DefendantReferralReasonJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.DelegatedPowersJPAMapper;
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
import uk.gov.moj.cpp.hearing.mapping.JurorsJPAMapper;
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
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Witness;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class WitnessRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    // Dummy JSON string longer than 255 chars used as a stand-in for court applications JSON
    private static final String DUMMY_COURT_APPLICATIONS_JSON =
            "{\"courtApplications\":[]}" + " ".repeat(250);

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();
    private static final List<Offence> offences = new ArrayList<>();

    private HearingRepository hearingRepository;
    private HearingJPAMapper hearingJPAMapper;

    @BeforeAll
    static void create() {
        final InitiateHearingCommand initiateHearingCommand = with(initiateHearingTemplateForMagistrates(), i -> {
            i.getHearing().getProsecutionCases().stream()
                    .flatMap(p -> p.getDefendants().stream())
                    .flatMap(d -> d.getOffences().stream());
        });

        hearings.add(initiateHearingCommand.getHearing());
    }

    @BeforeEach
    void openEntityManagerAndCreateRepository() {
        hearingRepository = new HearingRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingRepository);

        final CourtApplicationsSerializer courtApplicationsSerializer = mock(CourtApplicationsSerializer.class);
        when(courtApplicationsSerializer.json(anyList())).thenReturn(DUMMY_COURT_APPLICATIONS_JSON);

        hearingJPAMapper = buildHearingJPAMapper(courtApplicationsSerializer);

        hearings.forEach(hearing -> {
            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);

            hearingEntity.getProsecutionCases().forEach(prosecutionCase ->
                    prosecutionCase.getDefendants().forEach(defendant ->
                            defendant.getOffences().forEach(offences::add)));
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingEntity.getWitnesses().add(new Witness(UUID.randomUUID(), "Test", hearingEntity));
            hearingRepository.save(hearingEntity);
        });
    }

    @Test
    void shouldFindWitnessForHearing() {
        final Hearing hearing = hearingRepository.findAll().get(0);
        assertThat(hearing.getWitnesses().size(), is(1));
    }

    private static HearingJPAMapper buildHearingJPAMapper(final CourtApplicationsSerializer courtApplicationsSerializer) {
        final IndicatedPleaJPAMapper indicatedPleaJPAMapper = new IndicatedPleaJPAMapper();
        final CourtIndicatedSentenceJPAMapper courtIndicatedSentenceJPAMapper = new CourtIndicatedSentenceJPAMapper();
        final AllocationDecisionJPAMapper allocationDecisionJPAMapper =
                new AllocationDecisionJPAMapper(courtIndicatedSentenceJPAMapper);
        final LaaApplnReferenceJPAMapper laaApplnReferenceJPAMapper = new LaaApplnReferenceJPAMapper();
        final PleaJPAMapper pleaJPAMapper =
                new PleaJPAMapper(new DelegatedPowersJPAMapper(), new LesserOrAlternativeOffenceForPleaJPAMapper());
        final VerdictJPAMapper verdictJPAMapper =
                new VerdictJPAMapper(new JurorsJPAMapper(), new LesserOrAlternativeOffenceJPAMapper(), new VerdictTypeJPAMapper());
        final ReportingRestrictionJPAMapper reportingRestrictionJPAMapper = new ReportingRestrictionJPAMapper();
        final OffenceJPAMapper offenceJPAMapper = new OffenceJPAMapper(
                new NotifiedPleaJPAMapper(), indicatedPleaJPAMapper, pleaJPAMapper,
                new OffenceFactsJPAMapper(), verdictJPAMapper, allocationDecisionJPAMapper,
                laaApplnReferenceJPAMapper, reportingRestrictionJPAMapper);
        final AddressJPAMapper addressJPAMapper = new AddressJPAMapper();
        final ContactNumberJPAMapper contactNumberJPAMapper = new ContactNumberJPAMapper();
        final OrganisationJPAMapper organisationJPAMapper =
                new OrganisationJPAMapper(addressJPAMapper, contactNumberJPAMapper);
        final PersonJPAMapper personJPAMapper =
                new PersonJPAMapper(addressJPAMapper, contactNumberJPAMapper, new EthnicityJPAMapper());
        final AssociatedPersonJPAMapper associatedPersonJPAMapper = new AssociatedPersonJPAMapper(personJPAMapper);
        final PersonDefendantJPAMapper personDefendantJPAMapper =
                new PersonDefendantJPAMapper(organisationJPAMapper, personJPAMapper, new CustodialEstablishmentJPAMapper());
        final DefenceOrganisationJPAMapper defenceOrganisationJPAMapper =
                new DefenceOrganisationJPAMapper(addressJPAMapper, contactNumberJPAMapper);
        final AssociatedDefenceOrganisationJPAMapper associatedDefenceOrganisationJPAMapper =
                new AssociatedDefenceOrganisationJPAMapper(defenceOrganisationJPAMapper);
        final DefendantJPAMapper defendantJPAMapper = new DefendantJPAMapper(
                associatedPersonJPAMapper, organisationJPAMapper, offenceJPAMapper,
                personDefendantJPAMapper, associatedDefenceOrganisationJPAMapper);
        final ProsecutionCaseJPAMapper prosecutionCaseJPAMapper = new ProsecutionCaseJPAMapper(
                new ProsecutionCaseIdentifierJPAMapper(), defendantJPAMapper,
                new CaseMarkerJPAMapper(), new CpsProsecutorJPAMapper());

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
                mock(HearingYouthCourtDefendantsRepository.class),
                mock(ApplicationCourtListRestrictionMapper.class),
                mock(HearingApplicationRepository.class));
    }
}
