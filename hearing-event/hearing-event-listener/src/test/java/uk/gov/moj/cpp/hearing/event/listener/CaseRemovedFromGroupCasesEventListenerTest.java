package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.CasesUpdatedAfterCaseRemovedFromGroupCases;
import uk.gov.moj.cpp.hearing.mapping.ProsecutionCaseJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.repository.ProsecutionCaseRepository;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CaseRemovedFromGroupCasesEventListenerTest {

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @InjectMocks
    private CaseRemovedFromGroupCasesEventListener caseRemovedFromGroupCasesEventListener;

    @Captor
    private ArgumentCaptor<ProsecutionCase> argumentCaptor;

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    ProsecutionCaseRepository prosecutionCaseRepository;

    @Mock
    private ProsecutionCaseJPAMapper prosecutionCaseJPAMapper;

    private final static UUID HEARING_ID = randomUUID();
    private final static UUID GROUP_ID = randomUUID();
    private final static UUID MEMBER_CASE_ID = randomUUID();
    private final static UUID MASTER_CASE_ID = randomUUID();
    private final static UUID NEW_MASTER_CASE_ID = randomUUID();

    @BeforeEach
    public void setUp() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void casesUpdatedAfterMemberCaseRemovedFromGroupCases() {
        final CasesUpdatedAfterCaseRemovedFromGroupCases casesUpdated =
                new CasesUpdatedAfterCaseRemovedFromGroupCases(HEARING_ID, GROUP_ID,
                        getProsecutionCase(GROUP_ID, MEMBER_CASE_ID, false, false),
                        null, null);

        this.caseRemovedFromGroupCasesEventListener.casesUpdatedAfterCaseRemovedFromGroupCases(
                envelopeFrom(metadataWithRandomUUID("hearing.events.cases-updated-after-case-removed-from-group-cases"),
                        objectToJsonObjectConverter.convert(casesUpdated)));

        verify(this.prosecutionCaseRepository, times(1)).save(argumentCaptor.capture());
    }

    @Test
    public void casesUpdatedAfterMasterCaseRemovedFromGroupCases() {
        final CasesUpdatedAfterCaseRemovedFromGroupCases casesUpdated =
                new CasesUpdatedAfterCaseRemovedFromGroupCases(HEARING_ID, GROUP_ID,
                        getProsecutionCase(GROUP_ID, MASTER_CASE_ID, false, false),
                        getProsecutionCase(GROUP_ID, NEW_MASTER_CASE_ID, false, false), null);

        this.caseRemovedFromGroupCasesEventListener.casesUpdatedAfterCaseRemovedFromGroupCases(
                envelopeFrom(metadataWithRandomUUID("hearing.events.cases-updated-after-case-removed-from-group-cases"),
                        objectToJsonObjectConverter.convert(casesUpdated)));

        verify(this.prosecutionCaseRepository, times(2)).save(argumentCaptor.capture());
    }

    @Test
    public void shouldUpdateNumberOfGroupCasesOnHearing_WhenEventCarriesIt() {
        final Hearing hearingEntity = new Hearing();
        hearingEntity.setId(HEARING_ID);
        hearingEntity.setNumberOfGroupCases(3);
        when(hearingRepository.findBy(HEARING_ID)).thenReturn(hearingEntity);

        final CasesUpdatedAfterCaseRemovedFromGroupCases casesUpdated =
                new CasesUpdatedAfterCaseRemovedFromGroupCases(HEARING_ID, GROUP_ID,
                        getProsecutionCase(GROUP_ID, MEMBER_CASE_ID, false, false),
                        null, 2);

        this.caseRemovedFromGroupCasesEventListener.casesUpdatedAfterCaseRemovedFromGroupCases(
                envelopeFrom(metadataWithRandomUUID("hearing.events.cases-updated-after-case-removed-from-group-cases"),
                        objectToJsonObjectConverter.convert(casesUpdated)));

        assertThat(hearingEntity.getNumberOfGroupCases(), is(2));
        verify(this.hearingRepository).save(hearingEntity);
        verify(this.prosecutionCaseRepository, times(1)).save(argumentCaptor.capture());
    }

    @Test
    public void shouldNotChangeNumberOfGroupCasesOnHearing_WhenEventDoesNotCarryIt() {
        final Hearing hearingEntity = new Hearing();
        hearingEntity.setId(HEARING_ID);
        hearingEntity.setNumberOfGroupCases(3);
        when(hearingRepository.findBy(HEARING_ID)).thenReturn(hearingEntity);

        final CasesUpdatedAfterCaseRemovedFromGroupCases casesUpdated =
                new CasesUpdatedAfterCaseRemovedFromGroupCases(HEARING_ID, GROUP_ID,
                        getProsecutionCase(GROUP_ID, MEMBER_CASE_ID, false, false),
                        null, null);

        this.caseRemovedFromGroupCasesEventListener.casesUpdatedAfterCaseRemovedFromGroupCases(
                envelopeFrom(metadataWithRandomUUID("hearing.events.cases-updated-after-case-removed-from-group-cases"),
                        objectToJsonObjectConverter.convert(casesUpdated)));

        assertThat(hearingEntity.getNumberOfGroupCases(), is(3));
        verify(this.hearingRepository, never()).save(any(Hearing.class));
        verify(this.prosecutionCaseRepository, times(1)).save(argumentCaptor.capture());
    }

    private uk.gov.justice.core.courts.ProsecutionCase getProsecutionCase(final UUID groupId, final UUID caseId,
                                                                          final Boolean isGroupMember, final Boolean isGroupMaster) {
        return uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase()
                .withId(caseId)
                .withIsCivil(Boolean.TRUE)
                .withGroupId(groupId)
                .withIsGroupMember(isGroupMember)
                .withIsGroupMaster(isGroupMaster)
                .build();
    }
}
