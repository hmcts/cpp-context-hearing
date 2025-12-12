package uk.gov.moj.cpp.hearing.event.listener;


import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.Envelope.envelopeFrom;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;

import uk.gov.justice.hearing.courts.ApplicationCourtListRestriction;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.domain.event.CourtListRestricted;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionCase;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CourtListRestrictionEventListenerTest {
    @Captor
    private ArgumentCaptor<Hearing> hearingArgumentCaptor;

    @Mock
    private HearingRepository hearingRepository;

    @InjectMocks
    private CourtListRestrictionEventListener courtListRestrictionEventListener;

    private final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

    @Test
    public void shouldSaveRestrictions() throws IOException {
        final UUID hearingId = UUID.randomUUID();
        final UUID caseId = UUID.randomUUID();
        final UUID defendantId = UUID.randomUUID();
        final UUID offenceId = UUID.randomUUID();

        final List<UUID> caseIds = singletonList(caseId);
        final List<UUID> defendantIds = singletonList(defendantId);
        final List<UUID> offenceIds = singletonList(offenceId);

        final CourtListRestricted courtListRestricted = new CourtListRestricted(caseIds, null, null, null, null, defendantIds, hearingId, offenceIds, true);
        when(hearingRepository.findOptionalBy(any())).thenReturn(Optional.of(hearingEntity(hearingId, caseId, defendantId, offenceId)));
        final Metadata metadata = metadataBuilder()
                .withId(UUID.randomUUID())
                .withName("hearing.event.court-list-restricted")
                .build();

        final Envelope<CourtListRestricted> event = envelopeFrom(metadata, courtListRestricted);
        courtListRestrictionEventListener.processCourtListRestrictions(event);
        verify(hearingRepository, times(1)).save(hearingArgumentCaptor.capture());

        final Hearing hearing = hearingArgumentCaptor.getValue();
        assertThat(hearing.getProsecutionCases().size(), is(2));
        assertThat(hearing.getId(), is(hearingId));

        final Optional<ProsecutionCase> prosecutionCase1 = hearing.getProsecutionCases().stream()
                .filter(pc -> pc.getId().getId().equals(caseId))
                .findFirst();

        assertThat(prosecutionCase1.isPresent(), is(true));
        assertThat(prosecutionCase1.get().getId().getId(), is(caseId));
        assertThat(prosecutionCase1.get().getCourtListRestricted(), is(true));

        final Optional<Defendant> defendant1 = prosecutionCase1.get().getDefendants().stream().findFirst();
        assertThat(defendant1.isPresent(), is(true));
        assertThat(defendant1.get().getId().getId(), is(defendantId));
        assertThat(defendant1.get().getCourtListRestricted(), is(true));

        final Optional<ProsecutionCase> prosecutionCase2 = hearing.getProsecutionCases().stream()
                .filter(pc -> !pc.getId().getId().equals(caseId))
                .findFirst();

        assertThat(prosecutionCase2.isPresent(), is(true));
        assertThat(prosecutionCase2.get().getCourtListRestricted(), is(not(true)));

        final Optional<Defendant> defendant2 = prosecutionCase2.get().getDefendants().stream().findFirst();
        assertThat(defendant2.isPresent(), is(true));
        assertThat(defendant2.get().getCourtListRestricted(), is(not(true)));

        final Optional<Offence> offence2 = defendant2.get().getOffences().stream().findFirst();
        assertThat(offence2.isPresent(), is(true));
    }

    @Test
    public void shouldAddToApplicationRestrictions() throws IOException {
        final UUID hearingId = UUID.randomUUID();
        final UUID applicationId = UUID.randomUUID();
        final UUID applicantId = UUID.randomUUID();
        final UUID respondentId = UUID.randomUUID();

        final List<UUID> applicationIds = singletonList(applicationId);
        final List<UUID> applicantIds = singletonList(applicantId);
        final List<UUID> respondentIds = singletonList(respondentId);

        final CourtListRestricted courtListRestricted = new CourtListRestricted(null, applicantIds, applicationIds, respondentIds, null, null, hearingId, null, true);
        when(hearingRepository.findOptionalBy(any())).thenReturn(Optional.of(hearingEntity(hearingId, asList(UUID.randomUUID()), emptyList(), null)));
        final Metadata metadata = metadataBuilder()
                .withId(UUID.randomUUID())
                .withName("hearing.event.court-list-restricted")
                .build();

        final Envelope<CourtListRestricted> event = envelopeFrom(metadata, courtListRestricted);
        courtListRestrictionEventListener.processCourtListRestrictions(event);
        verify(hearingRepository, times(1)).save(hearingArgumentCaptor.capture());

        final Hearing hearing = hearingArgumentCaptor.getValue();
        assertThat(hearing.getId(), is(hearingId));
        assertThat(hearing.getRestrictCourtListJson(), is(notNullValue()));

        final ApplicationCourtListRestriction applicationCourtListRestriction = mapper.readValue(hearing.getRestrictCourtListJson(), ApplicationCourtListRestriction.class);
        assertThat(applicationCourtListRestriction.getCourtApplicationIds().size(), is(2));
        assertThat(applicationCourtListRestriction.getCourtApplicationIds().contains(applicationId), is(true));

        assertThat(applicationCourtListRestriction.getCourtApplicationApplicantIds().size(), is(1));
        assertThat(applicationCourtListRestriction.getCourtApplicationApplicantIds().get(0), is(applicantId));

        assertThat(applicationCourtListRestriction.getCourtApplicationRespondentIds().size(), is(1));
        assertThat(applicationCourtListRestriction.getCourtApplicationRespondentIds().get(0), is(respondentId));
    }

    @Test
    public void shouldAddToApplicationRestrictionsIfRestrictCourtListJsonIsNull() throws IOException {
        final UUID hearingId = UUID.randomUUID();
        final UUID applicationId = UUID.randomUUID();
        final List<UUID> applicationIds = singletonList(applicationId);

        final CourtListRestricted courtListRestricted = new CourtListRestricted(null, null, applicationIds, null, null, null, hearingId, null, true);
        when(hearingRepository.findOptionalBy(any())).thenReturn(Optional.of(hearingEntity(hearingId)));
        final Metadata metadata = metadataBuilder()
                .withId(UUID.randomUUID())
                .withName("hearing.event.court-list-restricted")
                .build();

        final Envelope<CourtListRestricted> event = envelopeFrom(metadata, courtListRestricted);
        courtListRestrictionEventListener.processCourtListRestrictions(event);
        verify(hearingRepository, times(1)).save(hearingArgumentCaptor.capture());

        final Hearing hearing = hearingArgumentCaptor.getValue();
        assertThat(hearing.getId(), is(hearingId));
        assertThat(hearing.getRestrictCourtListJson(), is(notNullValue()));

        final ApplicationCourtListRestriction applicationCourtListRestriction = mapper.readValue(hearing.getRestrictCourtListJson(), ApplicationCourtListRestriction.class);
        assertThat(applicationCourtListRestriction.getCourtApplicationIds().size(), is(1));
        assertThat(applicationCourtListRestriction.getCourtApplicationIds().get(0), is(applicationId));
    }

    @Test
    public void shouldRemoveFromApplicationRestrictions() throws IOException {
        final UUID hearingId = UUID.randomUUID();
        final UUID applicationId = UUID.randomUUID();
        final UUID applicantId1 = UUID.randomUUID();
        final UUID applicantId2 = UUID.randomUUID();

        final List<UUID> applicationIds = singletonList(applicationId);

        final CourtListRestricted courtListRestricted = new CourtListRestricted(null, singletonList(applicantId2), applicationIds, null, null, null, hearingId, null, false);
        when(hearingRepository.findOptionalBy(any())).thenReturn(Optional.of(hearingEntity(hearingId, singletonList(applicationId), asList(applicantId1, applicantId2), emptyList())));
        final Metadata metadata = metadataBuilder()
                .withId(UUID.randomUUID())
                .withName("hearing.event.court-list-restricted")
                .build();

        final Envelope<CourtListRestricted> event = envelopeFrom(metadata, courtListRestricted);
        courtListRestrictionEventListener.processCourtListRestrictions(event);
        verify(hearingRepository, times(1)).save(hearingArgumentCaptor.capture());

        final Hearing hearing = hearingArgumentCaptor.getValue();
        assertThat(hearing.getId(), is(hearingId));
        assertThat(hearing.getRestrictCourtListJson(), is(notNullValue()));

        final ApplicationCourtListRestriction applicationCourtListRestriction = mapper.readValue(hearing.getRestrictCourtListJson(), ApplicationCourtListRestriction.class);
        assertThat(applicationCourtListRestriction.getCourtApplicationIds().size(), is(0));
        assertThat(applicationCourtListRestriction.getCourtApplicationApplicantIds().size(), is(1));
        assertThat(applicationCourtListRestriction.getCourtApplicationApplicantIds().get(0), is(applicantId1));
        assertThat(applicationCourtListRestriction.getCourtApplicationRespondentIds().size(), is(0));
    }

    @Test
    public void shouldDoNothingIfThereIsNoHearing() throws IOException {
        final UUID hearingId = UUID.randomUUID();
        final UUID applicationId = UUID.randomUUID();
        final UUID applicantId2 = UUID.randomUUID();

        final List<UUID> applicationIds = singletonList(applicationId);

        final CourtListRestricted courtListRestricted = new CourtListRestricted(null, singletonList(applicantId2), applicationIds, null, null, null, hearingId, null, false);
        when(hearingRepository.findOptionalBy(any())).thenReturn(Optional.empty());
        final Metadata metadata = metadataBuilder()
                .withId(UUID.randomUUID())
                .withName("hearing.event.court-list-restricted")
                .build();

        final Envelope<CourtListRestricted> event = envelopeFrom(metadata, courtListRestricted);
        courtListRestrictionEventListener.processCourtListRestrictions(event);
        verify(hearingRepository, never()).save(hearingArgumentCaptor.capture());
    }

    private Hearing hearingEntity(final UUID hearingId, final UUID caseId, final UUID defendantId, final UUID offenceId){
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setProsecutionCases(new HashSet<>(asList(
                caseEntity(hearingId, caseId, defendantId, offenceId),
                caseEntity(hearingId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()))));

        return hearing;
    }

    private Hearing hearingEntity(final UUID hearingId, final List<UUID> applicationIds, final List<UUID> applicantIds, final List<UUID> respondentIds) throws JsonProcessingException {
        ApplicationCourtListRestriction applicationCourtListRestriction = ApplicationCourtListRestriction.applicationCourtListRestriction()
                .withCourtApplicationIds(applicationIds)
                .withCourtApplicationApplicantIds(applicantIds)
                .withCourtApplicationRespondentIds(respondentIds)
                .build();
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        hearing.setRestrictCourtListJson(mapper.writeValueAsString(applicationCourtListRestriction));
        return hearing;
    }

    private Hearing hearingEntity(final UUID hearingId){
        final Hearing hearing = new Hearing();
        hearing.setId(hearingId);
        return hearing;
    }

    private ProsecutionCase caseEntity(final UUID hearingId, final UUID caseId, final UUID defendantId, final UUID offenceId){
        final ProsecutionCase prosecutionCase = new ProsecutionCase();
        prosecutionCase.setId(new HearingSnapshotKey(caseId, hearingId));

        final Defendant defendant = new Defendant();
        defendant.setId(new HearingSnapshotKey(defendantId, hearingId));

        final Offence offence = new Offence();
        offence.setId(new HearingSnapshotKey(offenceId, hearingId));

        prosecutionCase.setDefendants(new HashSet<>(singletonList(defendant)));
        defendant.setOffences(new HashSet<>(singletonList(offence)));
        return prosecutionCase;
    }

}