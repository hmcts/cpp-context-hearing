package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.HearingLanguage;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.domain.event.HearingDetailChanged;
import uk.gov.moj.cpp.hearing.domain.event.HearingEventVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.mapping.HearingDayJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.JudicialRoleJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.CourtCentre;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingType;
import uk.gov.moj.cpp.hearing.persist.entity.ha.JudicialRole;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ChangeHearingDetailEventListenerTest {

    private static final UUID HEARING_ID = randomUUID();
    private static final UUID VACATED_REASON_ID = randomUUID();

    @Mock
    private HearingRepository hearingRepository;

    @Mock
    private HearingDayJPAMapper hearingDayJPAMapper;

    @Mock
    private JudicialRoleJPAMapper judicialRoleJPAMapper;

    @InjectMocks
    private ChangeHearingDetailEventListener changeHearingDetailEventListener;

    @Spy
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Spy
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Captor
    private ArgumentCaptor<Hearing> ahearingArgumentCaptor;

    @Before
    public void setup() {
        setField(this.jsonObjectToObjectConverter, "objectMapper", new ObjectMapperProducer().objectMapper());
        setField(this.objectToJsonObjectConverter, "mapper", new ObjectMapperProducer().objectMapper());
    }

    @Test
    public void shouldUpdateVacatedReasonToNull() {
        final Hearing hearing = new Hearing();
        hearing.setId(HEARING_ID);
        hearing.setvacatedTrialReasonId(null);
        hearing.setIsVacatedTrial(false);
        hearing.setHasSharedResults(false);

        when(this.hearingRepository.findBy(HEARING_ID)).thenReturn(hearing);

        HearingEventVacatedTrialCleared hearingEventVacatedTrialCleared = new HearingEventVacatedTrialCleared(HEARING_ID);

        changeHearingDetailEventListener.handleHearingVacatedTrialCleared(envelopeFrom(metadataWithRandomUUID("hearing.event.hearing-rescheduled"),
                objectToJsonObjectConverter.convert(hearingEventVacatedTrialCleared)));

        verify(this.hearingRepository).save(ahearingArgumentCaptor.capture());

        final Hearing toBePersisted = ahearingArgumentCaptor.getValue();

        assertThat(hearingEventVacatedTrialCleared.getHearingId(), equalTo(toBePersisted.getId()));
        assertThat(null, equalTo(toBePersisted.getVacatedTrialReasonId()));
        assertThat(false, equalTo(toBePersisted.getIsVacatedTrial()));
    }

    @Test
    public void shouldChangeMandatoryHearingDetails() {

        // Should change Court Centre, Hearing Days, Judicial Roles, Hearing Type, Hearing Language, Jurisdiction Type and Reporting Restrictions
        final HearingType type = createHearingType();
        final CourtCentre courtCentre = createCourtCentre();

        final HearingDay hearingDay = new HearingDay();
        final Set<HearingDay> hearingDays = new HashSet<>(Arrays.asList(createHearingDay(hearingDay)));

        final JudicialRole judicialRole = new JudicialRole();
        final Set<JudicialRole> judicialRoles = new HashSet<>(Arrays.asList(createJudicialRole(judicialRole)));

        final Hearing hearing = new Hearing();
        hearing.setId(HEARING_ID);
        hearing.setCourtCentre(courtCentre);
        hearing.setHearingType(type);
        hearing.setHasSharedResults(false);

        when(this.hearingRepository.findBy(HEARING_ID)).thenReturn(hearing);
        when(this.hearingDayJPAMapper.toJPA(eq(hearing), anyList())).thenReturn(hearingDays);
        when(this.judicialRoleJPAMapper.toJPA(eq(hearing), anyList())).thenReturn(judicialRoles);

        HearingDetailChanged hearingDetailChanged = HearingDetailChanged.hearingDetailChanged()
                .setId(HEARING_ID)
                .setType(uk.gov.justice.core.courts.HearingType.hearingType()
                        .withId(randomUUID())
                        .withDescription(STRING.next())
                        .build())
                .setCourtCentre(uk.gov.justice.core.courts.CourtCentre.courtCentre()
                        .withId(randomUUID())
                        .withName(STRING.next())
                        .withRoomId(randomUUID())
                        .withRoomName(STRING.next())
                        .withWelshName(STRING.next())
                        .withWelshRoomName(STRING.next())
                        .build())
                .setHearingLanguage(HearingLanguage.ENGLISH)
                .setJurisdictionType(JurisdictionType.CROWN)
                .setReportingRestrictionReason(STRING.next());

        changeHearingDetailEventListener.hearingDetailChanged(envelopeFrom(metadataWithRandomUUID("hearing.event.detail-changed"),
                objectToJsonObjectConverter.convert(hearingDetailChanged)));

        verify(this.hearingRepository).save(ahearingArgumentCaptor.capture());

        final Hearing toBePersisted = ahearingArgumentCaptor.getValue();

        assertThat(hearingDetailChanged.getId(), equalTo(toBePersisted.getId()));
        assertThat(hearingDetailChanged.getReportingRestrictionReason(), equalTo(toBePersisted.getReportingRestrictionReason()));
        assertThat(hearingDetailChanged.getJurisdictionType(), equalTo(toBePersisted.getJurisdictionType()));

        assertThat(hearingDetailChanged.getType().getId(), equalTo(toBePersisted.getHearingType().getId()));
        assertThat(hearingDetailChanged.getType().getDescription(), equalTo(toBePersisted.getHearingType().getDescription()));

        assertThat(hearingDetailChanged.getCourtCentre().getId(), equalTo(toBePersisted.getCourtCentre().getId()));
        assertThat(hearingDetailChanged.getCourtCentre().getName(), equalTo(toBePersisted.getCourtCentre().getName()));
        assertThat(hearingDetailChanged.getCourtCentre().getRoomId(), equalTo(toBePersisted.getCourtCentre().getRoomId()));
        assertThat(hearingDetailChanged.getCourtCentre().getRoomName(), equalTo(toBePersisted.getCourtCentre().getRoomName()));
        assertThat(hearingDetailChanged.getCourtCentre().getWelshName(), equalTo(toBePersisted.getCourtCentre().getWelshName()));
        assertThat(hearingDetailChanged.getCourtCentre().getWelshRoomName(), equalTo(toBePersisted.getCourtCentre().getWelshRoomName()));

        assertThat(hearingDay.getSittingDay(), equalTo(toBePersisted.getHearingDays().stream().findFirst().get().getSittingDay()));
        assertThat(hearingDay.getListingSequence(), equalTo(toBePersisted.getHearingDays().stream().findFirst().get().getListingSequence()));
        assertThat(hearingDay.getListedDurationMinutes(), equalTo(toBePersisted.getHearingDays().stream().findFirst().get().getListedDurationMinutes()));

        assertThat(judicialRole.getJudicialId(), equalTo(toBePersisted.getJudicialRoles().stream().findFirst().get().getJudicialId()));
        assertThat(judicialRole.getTitle(), equalTo(toBePersisted.getJudicialRoles().stream().findFirst().get().getTitle()));
        assertThat(judicialRole.getFirstName(), equalTo(toBePersisted.getJudicialRoles().stream().findFirst().get().getFirstName()));
        assertThat(judicialRole.getMiddleName(), equalTo(toBePersisted.getJudicialRoles().stream().findFirst().get().getMiddleName()));
        assertThat(judicialRole.getLastName(), equalTo(toBePersisted.getJudicialRoles().stream().findFirst().get().getLastName()));
        assertThat(judicialRole.getJudicialRoleType(), equalTo(toBePersisted.getJudicialRoles().stream().findFirst().get().getJudicialRoleType()));
        assertThat(judicialRole.getBenchChairman(), equalTo(toBePersisted.getJudicialRoles().stream().findFirst().get().getBenchChairman()));
        assertThat(judicialRole.getDeputy(), equalTo(toBePersisted.getJudicialRoles().stream().findFirst().get().getDeputy()));

        // Should not change the shared results
        assertThat(hearing.getHasSharedResults(), equalTo(toBePersisted.getHasSharedResults()));
    }

    private HearingType createHearingType() {
        final HearingType type = new HearingType();
        type.setId(randomUUID());
        type.setDescription(STRING.next());
        return type;
    }

    private JudicialRole createJudicialRole(final JudicialRole judicialRole) {
        judicialRole.setJudicialId(randomUUID());
        judicialRole.setTitle(STRING.next());
        judicialRole.setFirstName(STRING.next());
        judicialRole.setMiddleName(STRING.next());
        judicialRole.setLastName(STRING.next());
        judicialRole.setJudicialRoleType(CoreTestTemplates.circuitJudge().getJudiciaryType());
        judicialRole.setBenchChairman(true);
        judicialRole.setDeputy(false);
        return judicialRole;
    }

    private HearingDay createHearingDay(final HearingDay hearingDay) {
        hearingDay.setId(new HearingSnapshotKey(randomUUID(), HEARING_ID));
        hearingDay.setSittingDay(ZonedDateTime.now());
        hearingDay.setListingSequence(10);
        hearingDay.setListedDurationMinutes(20);
        return hearingDay;
    }

    private CourtCentre createCourtCentre() {
        final CourtCentre courtCentre = new CourtCentre();
        courtCentre.setWelshRoomName(STRING.next());
        courtCentre.setWelshName(STRING.next());
        courtCentre.setRoomName(STRING.next());
        courtCentre.setRoomId(randomUUID());
        courtCentre.setName(STRING.next());
        courtCentre.setId(randomUUID());
        return courtCentre;
    }
}