package uk.gov.moj.cpp.hearing.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.minimumInitiateHearingTemplate;

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
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourDefendantsKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourtDefendants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DB integration tests for {@link HearingYouthCourtDefendantsRepository} class
 */
@ExtendWith(MockitoExtension.class)
class HearingYouthCourtDefendantsRepositoryTest {

    private static final String PERSISTENCE_UNIT = "hearing-test-persistence-unit";

    private static final String DUMMY_COURT_APPLICATIONS_JSON = "{\"courtApplications\":[]}" + " ".repeat(300);

    private static final List<uk.gov.justice.core.courts.Hearing> hearings = new ArrayList<>();

    @RegisterExtension
    static HibernateTestEntityManagerProvider hibernateTestEntityManagerProvider =
            new HibernateTestEntityManagerProvider(PERSISTENCE_UNIT);

    @Mock
    private CourtApplicationsSerializer courtApplicationsSerializer;

    @Mock
    private ApplicationCourtListRestrictionMapper applicationCourtListRestrictionMapper;

    private HearingRepository hearingRepository;
    private HearingYouthCourtDefendantsRepository hearingYouthCourtDefendantsRepository;
    private HearingJPAMapper hearingJPAMapper;

    @BeforeAll
    static void createHearingData() {
        final InitiateHearingCommand initiateHearingCommand = minimumInitiateHearingTemplate();
        hearings.add(initiateHearingCommand.getHearing());
    }

    @BeforeEach
    void openEntityManagerAndCreateRepositories() {
        hearingRepository = new HearingRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingRepository);

        hearingYouthCourtDefendantsRepository = new HearingYouthCourtDefendantsRepository();
        hibernateTestEntityManagerProvider.injectEntityManagerInto(hearingYouthCourtDefendantsRepository);

        when(courtApplicationsSerializer.json(anyList())).thenReturn(DUMMY_COURT_APPLICATIONS_JSON);

        final IndicatedPleaJPAMapper indicatedPleaJPAMapper = new IndicatedPleaJPAMapper();
        final CourtIndicatedSentenceJPAMapper courtIndicatedSentenceJPAMapper = new CourtIndicatedSentenceJPAMapper();
        final AllocationDecisionJPAMapper allocationDecisionJPAMapper = new AllocationDecisionJPAMapper(courtIndicatedSentenceJPAMapper);
        final LaaApplnReferenceJPAMapper laaApplnReferenceJPAMapper = new LaaApplnReferenceJPAMapper();
        final PleaJPAMapper pleaJPAMapper = new PleaJPAMapper(new DelegatedPowersJPAMapper(), new LesserOrAlternativeOffenceForPleaJPAMapper());
        final VerdictJPAMapper verdictJPAMapper = new VerdictJPAMapper(new JurorsJPAMapper(), new LesserOrAlternativeOffenceJPAMapper(), new VerdictTypeJPAMapper());
        final ReportingRestrictionJPAMapper reportingRestrictionJPAMapper = new ReportingRestrictionJPAMapper();
        final OffenceJPAMapper offenceJPAMapper = new OffenceJPAMapper(new NotifiedPleaJPAMapper(), indicatedPleaJPAMapper,
                pleaJPAMapper, new OffenceFactsJPAMapper(), verdictJPAMapper, allocationDecisionJPAMapper,
                laaApplnReferenceJPAMapper, reportingRestrictionJPAMapper);
        final OrganisationJPAMapper organisationJPAMapper = new OrganisationJPAMapper(new AddressJPAMapper(), new ContactNumberJPAMapper());
        final PersonJPAMapper personJPAMapper = new PersonJPAMapper(new AddressJPAMapper(), new ContactNumberJPAMapper(), new EthnicityJPAMapper());
        final AssociatedPersonJPAMapper associatedPersonJPAMapper = new AssociatedPersonJPAMapper(personJPAMapper);
        final PersonDefendantJPAMapper personDefendantJPAMapper = new PersonDefendantJPAMapper(organisationJPAMapper, personJPAMapper, new CustodialEstablishmentJPAMapper());
        final DefenceOrganisationJPAMapper defenceOrganisationJPAMapper = new DefenceOrganisationJPAMapper(new AddressJPAMapper(), new ContactNumberJPAMapper());
        final AssociatedDefenceOrganisationJPAMapper associatedDefenceOrganisationJPAMapper = new AssociatedDefenceOrganisationJPAMapper(defenceOrganisationJPAMapper);
        final DefendantJPAMapper defendantJPAMapper = new DefendantJPAMapper(associatedPersonJPAMapper, organisationJPAMapper,
                offenceJPAMapper, personDefendantJPAMapper, associatedDefenceOrganisationJPAMapper);
        final ProsecutionCaseJPAMapper prosecutionCaseJPAMapper = new ProsecutionCaseJPAMapper(
                new ProsecutionCaseIdentifierJPAMapper(), defendantJPAMapper, new CaseMarkerJPAMapper(), new CpsProsecutorJPAMapper());

        hearingJPAMapper = new HearingJPAMapper(
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
                null,
                applicationCourtListRestrictionMapper,
                null
        );

        hearings.forEach(hearing -> {
            final Hearing hearingEntity = hearingJPAMapper.toJPA(hearing);
            // because h2 incorrectly maps column type TEXT to VARCHAR(255)
            hearingEntity.setCourtApplicationsJson(hearingEntity.getCourtApplicationsJson().substring(0, 255));
            hearingEntity.getProsecutionCases().iterator().next().setMarkers(null);
            hearingRepository.save(hearingEntity);
        });
    }

    @Test
    void shouldFindHearingIdByApplicationId() {
        List<UUID> defendantsIdList = hearings.get(0).getProsecutionCases().get(0).getDefendants().stream()
                .map(d -> d.getId())
                .collect(Collectors.toList());
        final Hearing hearingSaved = hearingRepository.findBy(hearings.get(0).getId());
        saveHearingYouthCourtDefendants(defendantsIdList, hearingSaved);
        final List<HearingYouthCourtDefendants> actual = hearingYouthCourtDefendantsRepository.findAllByHearingId(hearingSaved.getId());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getId().getHearingId(), is(hearingSaved.getId()));
    }

    private void saveHearingYouthCourtDefendants(List<UUID> defendantsIdList, Hearing hearingSaved) {
        defendantsIdList.stream().forEach(id -> {
            final HearingYouthCourtDefendants hearingYouthCourtDefendants = new HearingYouthCourtDefendants();
            HearingYouthCourDefendantsKey hearingYouthCourDefendantsKey = new HearingYouthCourDefendantsKey(id, hearingSaved.getId());
            hearingYouthCourtDefendants.setId(hearingYouthCourDefendantsKey);
            hearingYouthCourtDefendantsRepository.save(hearingYouthCourtDefendants);
        });
    }
}
